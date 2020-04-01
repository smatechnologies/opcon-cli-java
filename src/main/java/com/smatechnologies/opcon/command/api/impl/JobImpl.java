package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.enums.JobActions;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IJob;
import com.smatechnologies.opcon.command.api.modules.JobLogData;
import com.smatechnologies.opcon.command.api.modules.WaitResultInformation;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.dailyjobs.DailyJobsCriteria;
import com.smatechnologies.opcon.restapiclient.api.dailyjobs.WsDailyJobs;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.DailySchedulesCriteria;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.WsDailySchedules;
import com.smatechnologies.opcon.restapiclient.api.jobinstanceactions.WsJobInstanceActions;
import com.smatechnologies.opcon.restapiclient.api.scheduleactions.WsScheduleActions;
import com.smatechnologies.opcon.restapiclient.model.DailySchedule;
import com.smatechnologies.opcon.restapiclient.model.JobAction;
import com.smatechnologies.opcon.restapiclient.model.JobInstanceAction;
import com.smatechnologies.opcon.restapiclient.model.JobStatus;
import com.smatechnologies.opcon.restapiclient.model.JobType;
import com.smatechnologies.opcon.restapiclient.model.Result;
import com.smatechnologies.opcon.restapiclient.model.ScheduleAction;
import com.smatechnologies.opcon.restapiclient.model.ScheduleAction.Action;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.WindowsDailyJob;

public class JobImpl implements IJob {
	
	private static final String InvalidOpConAPI1710VersionMsg =       "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String JobAddSuccessMsg =                    "Date ({0}) : job ({1}) added to schedule ({2}) successfully";
	private static final String JobAddAndWaitSuccessMsg =             "Date ({0}) : job ({1}) added to schedule ({2}) successfully and completed with status ({3})";
	private static final String JobAddFailedMsg =                     "Job add failed : reason {0}";
	private static final String JobAddWaitForExecutionMsg =           "Waiting for execution of Job ({0}) of Schedule ({1}) on Date ({2}) to complete";
	private static final String JobActionSuccessMsg =                 "Date ({0}) : action ({1}) on job ({2}) of schedule ({3}) completed successfully";
	private static final String JobActionFailedMsg =                  "Job action ({0}) failed : {1}";
	private static final String JobNotFoundInDailyMsg =               "Job ({0}) Schedule ({1}) Date ({2}) not found in Daily tables";
	
	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayTaskDateArgumentMsg =          "-d   (date)                : {0}";
	private static final String DisplayScheduleNameArgumentMsg =      "-sn  (schedule name)       : {0}";
	private static final String DisplayJobNameArgumentMsg =           "-jn  (job name)            : {0}";
	private static final String DisplayFrequencyNameArgumentMsg =     "-jf  (frequency name)      : {0}";
	private static final String DisplayJobActionArgumentMsg =         "-ja  (job action)          : {0}";
	private static final String DisplayPropertiesArgumentMsg =        "-ip  (properties)          : {0}";
	private static final String DisplayAddOnHoldArgumentMsg =         "-joh (add on hold)         : {0}";
	private static final String DisplayWaitForCompletionArgumentMsg = "-jw  (wait for completion) : {0}";
	private static final String DisplayJobDirectoryArgumentMsg =      "-jld (job log directory)   : {0}";
	
	private static final String IntermediateLineMsg =                 "----------------------------------------------------------------------------";									
	private static final String NoDataFoundMsg =                      "No Jors Data returned from remote system";									
	private static final String RetrievingJobLogFailedMsg =           "Retrieving Job Log failed : Reason {0}";
	private static final String JobLogHeaderMsg =                     "Job Log : {0}";	

	DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final static Logger LOG = LoggerFactory.getLogger(JobImpl.class);
	private Utilities _Utilities = new Utilities();
	
	private ScheduledExecutorService executorJobAdd = Executors.newScheduledThreadPool(1);
	private ScheduledExecutorService executorWaitForJobCompletion = Executors.newScheduledThreadPool(1);
	private ScheduledExecutorService executorGetJorsFileList = Executors.newScheduledThreadPool(10);
	private ScheduledExecutorService executorGetJorsFileContent = Executors.newScheduledThreadPool(10);
	private ScheduledFuture<?> futureJobAdd = null;
	private ScheduledFuture<?> futureWaitForJobCompletion = null;
	private ScheduledFuture<?> futureGetJorsFileContent = null;
	private ScheduledFuture<?> futureGetJorsFileList = null;
	private boolean isJobAddComplete = false;
	private boolean isJobExecutionComplete = false;
	private boolean isJorsFileListComplete = false;
	private boolean isJorsFileContentComplete = false;

	private String jorsDataFileName = null;
	private List<String> filesToRetrieve = new ArrayList<String>();
	private List<String> jorsDataList = new ArrayList<String>();
	
	private  WaitResultInformation jobAddRequestResult = new WaitResultInformation();
	private  WaitResultInformation jobCompletionResult = new WaitResultInformation();

	public Integer jobActionRequest(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		JobAction jobAction = new JobAction();

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			// check if schedule exists in the daily
			List<JobAction.Job> jobs = new ArrayList<JobAction.Job>();
			DailySchedule dailySchedule = checkIfDailyScheduleExists(opconApi, _OpConCliArguments);
			if(dailySchedule != null) {
				JobAction.Job job = new JobAction.Job();
				job.setId(dailySchedule.getId() + ICmdConstants.PIPE + _OpConCliArguments.getJobName());
				jobs.add(job);
				jobAction.setAction(getJobAction(_OpConCliArguments.getJobAction()));
				jobAction.setJobs(jobs);
				JobAction retJobAction = opconApi.jobActions().post(jobAction);
				if(retJobAction.getResult() == Result.SUCCESS) {
					LOG.info(MessageFormat.format(JobAddSuccessMsg,  _OpConCliArguments.getTaskDate(), _OpConCliArguments.getJobAction(),  _OpConCliArguments.getJobName(),  _OpConCliArguments.getScheduleName()));
					success = 0;
				} else {
					LOG.error(MessageFormat.format(JobAddFailedMsg, _OpConCliArguments.getJobAction(), retJobAction.getReason()));
					LOG.error(ArgumentsMsg);
					LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
					LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, _OpConCliArguments.getScheduleName()));
					LOG.error(MessageFormat.format(DisplayJobNameArgumentMsg, _OpConCliArguments.getJobName()));
					LOG.error(MessageFormat.format(DisplayJobActionArgumentMsg, _OpConCliArguments.getJobAction()));
					success = 1;
				}
			} else {
				LOG.error(MessageFormat.format(JobAddFailedMsg, _OpConCliArguments.getJobAction(), "Schedule not found in Daily"));
				success = 1;
			}
		} else {
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
			success = 1;
		}
		return success;
	}	// END : jobActionRequest

	public Integer jobAddRequest(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		ScheduleAction scheduleAction = new ScheduleAction();

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			WsScheduleActions wsScheduleActions = opconApi.scheduleActions();
			// check if schedule exists in the daily
			List<ScheduleAction.Schedule> schedules = new ArrayList<ScheduleAction.Schedule>();
			List<ScheduleAction.Schedule.Job> jobs = new ArrayList<ScheduleAction.Schedule.Job>();
			DailySchedule dailySchedule = checkIfDailyScheduleExists(opconApi, _OpConCliArguments);
			if(dailySchedule != null) {
				ScheduleAction.Schedule schedule = new ScheduleAction.Schedule();
				schedule.setId(dailySchedule.getId());
				ScheduleAction.Schedule.Job job = new ScheduleAction.Schedule.Job();
				job.setId(_OpConCliArguments.getJobName());
				job.setFrequency(_OpConCliArguments.getFrequencyName());
				if(_OpConCliArguments.getInstanceProperties() != null) {
					job.setInstanceProperties(getInstancePropertyList(_OpConCliArguments.getInstanceProperties()));
				}
				jobs.add(job);
				schedule.setJobs(jobs);
				schedules.add(schedule);
				if(_OpConCliArguments.isAddOnHold()) {
					scheduleAction.setAction(Action.ADD_JOBS_ON_HOLD);
				} else {
					scheduleAction.setAction(Action.ADD_JOBS);
				}
				scheduleAction.setSchedules(schedules);
				ScheduleAction retScheduleAction =  wsScheduleActions.post(scheduleAction);
				if(retScheduleAction.getResult() == Result.SUCCESS) {
					LOG.info(MessageFormat.format(JobActionSuccessMsg, _OpConCliArguments.getTaskDate(), _OpConCliArguments.getTask(), _OpConCliArguments.getJobName(),  _OpConCliArguments.getScheduleName()));
					success = 0;
				} else if (retScheduleAction.getResult() == Result.SUBMITTED) {
					// lets check for success
					checkJobAddStatus(opconApi, retScheduleAction.getId());
					if(jobAddRequestResult.getCompletionCode() == 0) {
						LOG.info(MessageFormat.format(JobActionSuccessMsg,  _OpConCliArguments.getTaskDate(), _OpConCliArguments.getTask(), jobAddRequestResult.getAdjustedJobName(),  _OpConCliArguments.getScheduleName()));
						if(_OpConCliArguments.isWaitForCompletion()) {
							LOG.info(MessageFormat.format(JobAddWaitForExecutionMsg, _OpConCliArguments.getJobName(),  _OpConCliArguments.getScheduleName(), _OpConCliArguments.getTaskDate()));
							checkForJobCompletion(opconApi, jobAddRequestResult.getJobid());
							LOG.info(MessageFormat.format(JobAddAndWaitSuccessMsg, _OpConCliArguments.getTaskDate(), _OpConCliArguments.getJobName(),  _OpConCliArguments.getScheduleName(), jobCompletionResult.getFailedReason()));
							if(jobCompletionResult.getCompletionCode() == 0) {
								success = 0;
							} else {
								success = 1;
							}
						} else {
							success = 0;
						}
					} else {
						LOG.error(MessageFormat.format(JobAddFailedMsg, jobAddRequestResult.getFailedReason()));
						LOG.error(ArgumentsMsg);
						LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
						LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, _OpConCliArguments.getScheduleName()));
						LOG.error(MessageFormat.format(DisplayJobNameArgumentMsg, _OpConCliArguments.getJobName()));
						LOG.error(MessageFormat.format(DisplayFrequencyNameArgumentMsg, _OpConCliArguments.getFrequencyName()));
						LOG.error(MessageFormat.format(DisplayPropertiesArgumentMsg, _OpConCliArguments.getInstanceProperties()));
						LOG.error(MessageFormat.format(DisplayAddOnHoldArgumentMsg, _OpConCliArguments.isAddOnHold()));
						LOG.error(MessageFormat.format(DisplayWaitForCompletionArgumentMsg, _OpConCliArguments.isWaitForCompletion()));
						success = 1;
					}
				} else {
					LOG.error(MessageFormat.format(JobAddFailedMsg, retScheduleAction.getReason()));
					LOG.error(ArgumentsMsg);
					LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
					LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, _OpConCliArguments.getScheduleName()));
					LOG.error(MessageFormat.format(DisplayJobNameArgumentMsg, _OpConCliArguments.getJobName()));
					LOG.error(MessageFormat.format(DisplayFrequencyNameArgumentMsg, _OpConCliArguments.getFrequencyName()));
					LOG.error(MessageFormat.format(DisplayPropertiesArgumentMsg, _OpConCliArguments.getInstanceProperties()));
					LOG.error(MessageFormat.format(DisplayAddOnHoldArgumentMsg, _OpConCliArguments.isAddOnHold()));
					LOG.error(MessageFormat.format(DisplayWaitForCompletionArgumentMsg, _OpConCliArguments.isWaitForCompletion()));
					success = 1;
				}
			} else {
				LOG.error(MessageFormat.format(JobActionFailedMsg, _OpConCliArguments.getJobAction(), "Schedule not found in Daily"));
				success = 1;
			}
		} else {
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
			success = 1;
		}
		return success;
	}	// END : jobAddRequest

	public List<JobLogData> getJobLog(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		List<JobLogData> jobLogsData = new ArrayList<JobLogData>();
		DailyJob dailyJob = null;
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			dailyJob = getDailyJob(opconApi, _OpConCliArguments);
			if(dailyJob != null) {
				// go get the list of log files
				WsJobInstanceActions wsJobInstanceActions = opconApi.jobInstanceActions();
				JobInstanceAction jobInstanceAction = new JobInstanceAction();
				List<JobInstanceAction.JobInstanceActionItem> jobInstanceActionItems = new ArrayList<JobInstanceAction.JobInstanceActionItem>();
				JobInstanceAction.JobInstanceActionItem jobInstanceActionItem = new JobInstanceAction.JobInstanceActionItem();
				jobInstanceActionItem.setId(dailyJob.getJobNumber());
				jobInstanceActionItems.add(jobInstanceActionItem);
				jobInstanceAction.setJobInstanceActionItems(jobInstanceActionItems);
				jobInstanceAction.getAction();
				jobInstanceAction.setAction(com.smatechnologies.opcon.restapiclient.model.JobInstanceAction.Action.LIST);
				JobInstanceAction retJobInstanceAction = wsJobInstanceActions.post(jobInstanceAction);
				filesToRetrieve.clear();
				checkJorsFileListStatus(opconApi, retJobInstanceAction.getId());
				if(!filesToRetrieve.isEmpty()) {
					// go request the files
					for(String filename : filesToRetrieve) {
						jorsDataList.clear();
						JobLogData jobLogData = new JobLogData();
						JobInstanceAction jobInstanceActionFiles = new JobInstanceAction();
						List<JobInstanceAction.JobInstanceActionItem> jobInstanceActionItemsFiles = new ArrayList<JobInstanceAction.JobInstanceActionItem>();
						JobInstanceAction.JobInstanceActionItem jobInstanceActionItemFile = new JobInstanceAction.JobInstanceActionItem();
						jobInstanceActionItemFile.setId(dailyJob.getJobNumber());
						jobInstanceActionItemFile.setJorsRequestParameters(filename);
						jobInstanceActionItemsFiles.add(jobInstanceActionItemFile);
						jobInstanceActionFiles.setJobInstanceActionItems(jobInstanceActionItemsFiles);
						jobInstanceActionFiles.setAction(com.smatechnologies.opcon.restapiclient.model.JobInstanceAction.Action.FILE);
						JobInstanceAction retJobInstanceActionFiles = wsJobInstanceActions.post(jobInstanceActionFiles);
						checkJorsFileContentStatus(opconApi, retJobInstanceActionFiles.getId());
						jobLogData.setRecords(jorsDataList);
						jobLogData.setFilename(jorsDataFileName);
						jobLogsData.add(jobLogData);
					}
				} else {
					if(jorsDataList.isEmpty()) {
						jorsDataList.add(IntermediateLineMsg);
						jorsDataList.add(NoDataFoundMsg);
						jorsDataList.add(IntermediateLineMsg);
					}
				}
			} else {
				LOG.error(MessageFormat.format(JobNotFoundInDailyMsg, _OpConCliArguments.getJobName(), _OpConCliArguments.getScheduleName(), _OpConCliArguments.getTaskDate()));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
				LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, _OpConCliArguments.getScheduleName()));
				LOG.error(MessageFormat.format(DisplayJobNameArgumentMsg, _OpConCliArguments.getJobName()));
				LOG.error(MessageFormat.format(DisplayJobDirectoryArgumentMsg, _OpConCliArguments.getJobLogDirectory()));
			}
		} else {
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
			return null;
		}
		return jobLogsData;
	}	// END : getJobLog

	public DailyJob getDailyJobByName(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {

		DailyJob dailyJob = null;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			dailyJob = getDailyJob(opconApi, _OpConCliArguments);
		} else {
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
			return null;
		}
		return dailyJob;
	}	// END : getDailyJob

	public WindowsDailyJob getWindowsDailyJobByName(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {

		WindowsDailyJob dailyJob = null;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			dailyJob = getWindowsDailyJob(opconApi, _OpConCliArguments);
		} else {
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
			return null;
		}
		return dailyJob;
	}	// END : getDailyJob

	public DailyJob getDailyJobById(
			OpconApi opconApi,
			String jobId
			) throws Exception {

		DailyJob dailyJob = null;

		WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
		dailyJob = wsDailyJobs.get(jobId);
		return dailyJob;
	}	// END : getDailyJobById

	public List<JobLogData> getJobLogByDailyJob(
			OpconApi opconApi,
			DailyJob dailyJob
			) throws Exception {
		
		List<JobLogData> jobLogsData = new ArrayList<JobLogData>();
		
		// go get the list of log files
		WsJobInstanceActions wsJobInstanceActions = opconApi.jobInstanceActions();
		JobInstanceAction jobInstanceAction = new JobInstanceAction();
		List<JobInstanceAction.JobInstanceActionItem> jobInstanceActionItems = new ArrayList<JobInstanceAction.JobInstanceActionItem>();
		JobInstanceAction.JobInstanceActionItem jobInstanceActionItem = new JobInstanceAction.JobInstanceActionItem();
		jobInstanceActionItem.setId(dailyJob.getJobNumber());
		jobInstanceActionItems.add(jobInstanceActionItem);
		jobInstanceAction.setJobInstanceActionItems(jobInstanceActionItems);
		jobInstanceAction.getAction();
		jobInstanceAction.setAction(com.smatechnologies.opcon.restapiclient.model.JobInstanceAction.Action.LIST);
		JobInstanceAction retJobInstanceAction = wsJobInstanceActions.post(jobInstanceAction);
		filesToRetrieve.clear();
		checkJorsFileListStatus(opconApi, retJobInstanceAction.getId());
		if(!filesToRetrieve.isEmpty()) {
			// go request the files
			for(String filename : filesToRetrieve) {
				jorsDataList.clear();
				JobLogData jobLogData = new JobLogData();
				JobInstanceAction jobInstanceActionFiles = new JobInstanceAction();
				List<JobInstanceAction.JobInstanceActionItem> jobInstanceActionItemsFiles = new ArrayList<JobInstanceAction.JobInstanceActionItem>();
				JobInstanceAction.JobInstanceActionItem jobInstanceActionItemFile = new JobInstanceAction.JobInstanceActionItem();
				jobInstanceActionItemFile.setId(dailyJob.getJobNumber());
				jobInstanceActionItemFile.setJorsRequestParameters(filename);
				jobInstanceActionItemsFiles.add(jobInstanceActionItemFile);
				jobInstanceActionFiles.setJobInstanceActionItems(jobInstanceActionItemsFiles);
				jobInstanceActionFiles.setAction(com.smatechnologies.opcon.restapiclient.model.JobInstanceAction.Action.FILE);
				JobInstanceAction retJobInstanceActionFiles = wsJobInstanceActions.post(jobInstanceActionFiles);
				checkJorsFileContentStatus(opconApi, retJobInstanceActionFiles.getId());
				jobLogData.setRecords(jorsDataList);
				jobLogData.setFilename(jorsDataFileName);
				jobLogsData.add(jobLogData);
			}
		} else {
			if(jorsDataList.isEmpty()) {
				jorsDataList.add(IntermediateLineMsg);
				jorsDataList.add(NoDataFoundMsg);
				jorsDataList.add(IntermediateLineMsg);
			}
		}
		return jobLogsData;
	}	// END : getJobLog

	private DailySchedule checkIfDailyScheduleExists(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		DailySchedule dailySchedule = null;
		DailySchedulesCriteria criteria = new DailySchedulesCriteria();
		
		Collection<LocalDate> ldates = new ArrayList<LocalDate>();
	    LocalDate dateTime = LocalDate.parse(_OpConCliArguments.getTaskDate(), localDateFormatter);
	    ldates.add(dateTime);
	    criteria.setName(_OpConCliArguments.getScheduleName());
		criteria.setDates(ldates);
		WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
		List<DailySchedule> schedules = wsDailySchedules.get(criteria);
		if(schedules.size() > 0) {
			dailySchedule = schedules.get(0);
		} 
		return dailySchedule;
	}	// END : checkIfDailyScheduleExists
	
	private DailyJob getDailyJob(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {

		DailyJob dailyJob = null;

		// check if schedule exists in the daily
		DailySchedule dailySchedule = checkIfDailyScheduleExists(opconApi, _OpConCliArguments);
		if(dailySchedule != null) {
			Collection<String> scheduleIds = new ArrayList<String>();
			scheduleIds.add(dailySchedule.getId());
			Collection<LocalDate> ldates = new ArrayList<LocalDate>();
		    LocalDate dateTime = LocalDate.parse(_OpConCliArguments.getTaskDate(), localDateFormatter);
		    ldates.add(dateTime);
			DailyJobsCriteria criteria = new DailyJobsCriteria();
		    criteria.setName(_OpConCliArguments.getJobName());
			criteria.setDates(ldates);
			criteria.setScheduleIds(scheduleIds);
			WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
			List<DailyJob> dailyJobs = wsDailyJobs.get(criteria);
			if(dailyJobs.size() > 0) {
				dailyJob = dailyJobs.get(0);
			}
		} else {
			LOG.error(MessageFormat.format(JobActionFailedMsg, _OpConCliArguments.getJobAction(), "Schedule not found in Daily"));
			return null;
		}
		return dailyJob;
	}	// END : getDailyJob

	private WindowsDailyJob getWindowsDailyJob(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {

		WindowsDailyJob windowsDailyJob = null;
		
		// check if schedule exists in the daily
		DailySchedule dailySchedule = checkIfDailyScheduleExists(opconApi, _OpConCliArguments);
		if(dailySchedule != null) {
			Collection<String> scheduleIds = new ArrayList<String>();
			scheduleIds.add(dailySchedule.getId());
			Collection<LocalDate> ldates = new ArrayList<LocalDate>();
		    LocalDate dateTime = LocalDate.parse(_OpConCliArguments.getTaskDate(), localDateFormatter);
		    ldates.add(dateTime);
			DailyJobsCriteria criteria = new DailyJobsCriteria();
		    criteria.setName(_OpConCliArguments.getJobName());
			criteria.setDates(ldates);
			criteria.setScheduleIds(scheduleIds);
			criteria.setJobType(JobType.WINDOWS.getDescription());
			WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
			List<WindowsDailyJob> dailyJobs = wsDailyJobs.get(criteria).stream()
					.filter(dailyJob -> dailyJob instanceof WindowsDailyJob)
					.map(dailyJob -> (WindowsDailyJob) dailyJob)
					.collect(Collectors.toList());
			if(dailyJobs.size() > 0) {
				windowsDailyJob = dailyJobs.get(0);
			}
		} else {
			return null;
		}
		return windowsDailyJob;
	}	// END : getWindowsDailyJob

	private JobAction.Action getJobAction(
			String actionType
			) throws Exception {
		
		JobAction.Action action = null;

		JobActions type = JobActions.valueOf(actionType);
		
		switch (type) {
		
			case hold:
				action = JobAction.Action.HOLD;
				break;
				
			case cancel:
				action = JobAction.Action.CANCEL;
				break;
				
			case skip:
				action = JobAction.Action.SKIP;
				break;
				
			case kill:
				action = JobAction.Action.KILL;
				break;
				
			case start:
				action = JobAction.Action.START;
				break;
				
			case restart:
				action = JobAction.Action.RESTART;
				break;
				
			case forceRestart:
				action = JobAction.Action.FORCE_RESTART;
				break;
				
			case restartOnHold:
				action = JobAction.Action.RESTART_ON_HOLD;
				break;
				
			case release:
				action = JobAction.Action.RELEASE;
				break;
				
			case markFinishedOk:
				action = JobAction.Action.MARK_FINISHED_OK;
				break;
				
			case markFailed:
				action = JobAction.Action.MARK_FAILED;
				break;
				
			case markFixed:
				action = JobAction.Action.MARK_FIXED;
				break;
				
			case markUnderReview:
				action = JobAction.Action.MARK_UNDER_REVIEW;
				break;
		
		}
		return action;
	}	// END : getJobAction
	
	private List<ScheduleAction.Schedule.Job.InstanceProperty> getInstancePropertyList(
			String properties
			) throws Exception {
		
		List<ScheduleAction.Schedule.Job.InstanceProperty> instancePropertyList = new ArrayList<ScheduleAction.Schedule.Job.InstanceProperty>();
		
		try {
			if(properties != null) {
				String[] propertyArray = _Utilities.tokenizeParameters(properties, false, ICmdConstants.COMMA);
				for(int cntr = 0; cntr < propertyArray.length; cntr++) {
					String[] propertyDefArray = _Utilities.tokenizeParameters(propertyArray[cntr], false, ICmdConstants.EQUAL);
					ScheduleAction.Schedule.Job.InstanceProperty property = new ScheduleAction.Schedule.Job.InstanceProperty();
					property.setName(propertyDefArray[0]);
					property.setValue(propertyDefArray[1]);
					instancePropertyList.add(property);
				}
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return instancePropertyList;
	}	// END : getInstancePropertyList
	
	protected void checkJobAddStatus(
			final OpconApi opconApi, 
			final int jobid 
			) throws Exception {
		
		try {
		    final Runnable checkJobAddStatusRoutine = new Runnable() {
		         public void run() { 
		        	 try {
						getJobAddStatus(opconApi, jobid);
					} catch (Exception ex) {
						try {
							LOG.error(_Utilities.getExceptionDetails(ex));
						} catch (Exception e) {
							LOG.error(_Utilities.getExceptionDetails(e));
						}
					} 
		         }
		       };
		       futureJobAdd = executorJobAdd.scheduleWithFixedDelay(checkJobAddStatusRoutine, 4, 2, TimeUnit.SECONDS);				
			isJobAddComplete = false;;
			waitForJobAddCompletion();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : checkJobAddStatus

	private void getJobAddStatus(
			final OpconApi opconApi, 
			final int scheduleActionId 
			) throws Exception {
		
		try {
			WsScheduleActions wsScheduleActions = opconApi.scheduleActions();
			ScheduleAction retScheduleAction = wsScheduleActions.get(scheduleActionId);
			if(retScheduleAction.getResult() == Result.SUCCESS) {
				ScheduleAction.Schedule schedule = retScheduleAction.getSchedules().get(0);
				ScheduleAction.Schedule.Job job = schedule.getJobs().get(0);
				jobAddRequestResult.setJobid(job.getId());
				// extract the job name
				int lastColon = job.getId().lastIndexOf(ICmdConstants.PIPE);
				if(lastColon > -1) {
					jobAddRequestResult.setAdjustedJobName(job.getId().substring(lastColon + 1, job.getId().length()));
				}
				jobAddRequestResult.setCompletionCode(0);
				jobAddCompleted();
				futureJobAdd.cancel(true);
			} else if(retScheduleAction.getResult() == Result.FAILED) {
				jobAddRequestResult.setCompletionCode(1);
				jobAddRequestResult.setFailedReason(retScheduleAction.getReason());
				jobAddCompleted();
				futureJobAdd.cancel(true);
			}
		} catch (Exception ex) {
			jobAddCompleted();
			futureJobAdd.cancel(true);
			jobAddRequestResult.setCompletionCode(1);
			jobAddRequestResult.setFailedReason(ex.getMessage());
			LOG.error(_Utilities.getExceptionDetails(ex));
		}
	}	// END : getJobAddStatus
	
	private synchronized void waitForJobAddCompletion(
			) throws Exception {
		
		try {
			while(!isJobAddComplete) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForJobAddCompletion

	private synchronized void jobAddCompleted(
			) throws Exception {
		
		try {
			isJobAddComplete = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : jobAddCompleted
	
	protected void checkForJobCompletion(
			final OpconApi opconApi, 
			final String jobId 
			) throws Exception {
		
		try {
		    final Runnable checkForJobCompletionRoutine = new Runnable() {
		         public void run() { 
		        	 try {
						getJobCompletionStatus(opconApi, jobId);
					} catch (Exception ex) {
						try {
							LOG.error(_Utilities.getExceptionDetails(ex));
						} catch (Exception e) {
							LOG.error(_Utilities.getExceptionDetails(e));

						}
					} 
		         }
		       };
		    futureWaitForJobCompletion = executorWaitForJobCompletion.scheduleWithFixedDelay(checkForJobCompletionRoutine, 5, 3, TimeUnit.SECONDS);				
		    isJobExecutionComplete = false;
			waitForJobExecutionCompletion();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : checkForJobCompletion

	private void getJobCompletionStatus(
			OpconApi opconApi,
			String jobId
			) throws Exception {

		try {
			WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
			DailyJob dailyJob = wsDailyJobs.get(jobId);
			JobStatus status = dailyJob.getStatus();
			switch (status.getId()){
			
				case 210:
					jobCompletionResult.setCompletionCode(1);
					jobCompletionResult.setFailedReason("Initialization Error");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;

				case 900:
					jobCompletionResult.setCompletionCode(0);
					jobCompletionResult.setFailedReason("Finished OK");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;
					
				case 910:
					jobCompletionResult.setCompletionCode(1);
					jobCompletionResult.setFailedReason("Failed");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;
					
				case 920:
					jobCompletionResult.setCompletionCode(0);
					jobCompletionResult.setFailedReason("Mark Finished OK");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;

				case 921:
					jobCompletionResult.setCompletionCode(1);
					jobCompletionResult.setFailedReason("Mark Failed");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;

				case 940:
					jobCompletionResult.setCompletionCode(0);
					jobCompletionResult.setFailedReason("Skipped");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;

				case 950:
					jobCompletionResult.setCompletionCode(0);
					jobCompletionResult.setFailedReason("Cancelled");
					jobExecutionComplete();
					futureWaitForJobCompletion.cancel(true);
					break;
			}
		} catch (Exception ex) {
			jobExecutionComplete();
			futureWaitForJobCompletion.cancel(true);
			jobCompletionResult.setCompletionCode(1);
			jobCompletionResult.setFailedReason(ex.getMessage());
			LOG.error(_Utilities.getExceptionDetails(ex));
		}
	}	// END : getDailyJobStatus
	
	private synchronized void waitForJobExecutionCompletion(
			) throws Exception {
		
		try {
			while(!isJobExecutionComplete) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForJobExecutionCompletion

	private synchronized void jobExecutionComplete(
			) throws Exception {
		
		try {
			isJobExecutionComplete = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : jobExecutionComplete

	protected void checkJorsFileListStatus(
			final OpconApi opconApi, 
			final int id 
			) throws Exception {
		
		try {
		    final Runnable checkJorsFileListRoutine = new Runnable() {
		         public void run() { 
		        	 try {
						getJorsFileListStatus(opconApi, id);
					} catch (Exception ex) {
						LOG.error(_Utilities.getExceptionDetails(ex));
					} 
		         }
		       };
		    futureGetJorsFileList = executorGetJorsFileList.scheduleWithFixedDelay(checkJorsFileListRoutine, 4, 3, TimeUnit.SECONDS);				
		    isJorsFileListComplete = false;;
			waitForJorsFileListCompletion();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : checkJorsFileListStatus
	
	private void getJorsFileListStatus(
			OpconApi opconApi, 
			int id
			) throws Exception {
		
		try {
			WsJobInstanceActions wsJobInstanceActions = opconApi.jobInstanceActions();
			JobInstanceAction retJobInstanceAction = wsJobInstanceActions.get(id);
			if(retJobInstanceAction.getResult() == Result.SUCCESS) {
				// get list of files
				for(JobInstanceAction.JobInstanceActionItem jobactionItems : retJobInstanceAction.getJobInstanceActionItems()) {
					if(jobactionItems.getResult() == Result.SUCCESS) {
						for(String filename : jobactionItems.getFiles()) {
							filesToRetrieve.add(filename);
						}
					} else if(jobactionItems.getResult() == Result.FAILED) {
						LOG.error(MessageFormat.format(RetrievingJobLogFailedMsg, jobactionItems.getMessage()));
					}
				}
				jorsFileListComplete();
				futureGetJorsFileList.cancel(true);
			} else if(retJobInstanceAction.getResult() == Result.SUBMITTED) {
				// retry
			} else if(retJobInstanceAction.getResult() == Result.FAILED) {
				// should have returned headers
				jorsFileListComplete();
				futureGetJorsFileList.cancel(true);
			}
		} catch (Exception ex) {
			jorsFileListComplete();
			futureGetJorsFileList.cancel(true);
			throw new Exception(ex);
		}
	}	// END : getJorsFileListStatus
	
	private synchronized void waitForJorsFileListCompletion(
			) throws Exception {
		
		try {
			while(!isJorsFileListComplete) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForGetJorsFileListCompletion

	private synchronized void jorsFileListComplete(
			) throws Exception {
		
		try {
			isJorsFileListComplete = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : jorsFileListCompleted

	protected void checkJorsFileContentStatus(
			final OpconApi opconApi, 
			final int id
			) throws Exception {
		try {
		    final Runnable checkJorsFileContentStatusRoutine = new Runnable() {
		         public void run() { 
		        	 try {
						getJorsFileContentStatus(opconApi, id);
					} catch (Exception ex) {
						LOG.error(_Utilities.getExceptionDetails(ex));
					}
		         }
		       };
		    futureGetJorsFileContent = executorGetJorsFileContent.scheduleWithFixedDelay(checkJorsFileContentStatusRoutine, 4, 3, TimeUnit.SECONDS);		
		    isJorsFileContentComplete = false;;
			waitForJorsFileContentCompletion();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : checkJorsFileContentStatus
	
	private void getJorsFileContentStatus(
			final OpconApi opconApi, 
			int id
			) throws Exception {
		
		try {
			WsJobInstanceActions wsJobInstanceActions = opconApi.jobInstanceActions();
			JobInstanceAction retJobInstanceActionFile = wsJobInstanceActions.get(id);
			if(retJobInstanceActionFile.getResult() == Result.SUCCESS) {
				// extract the jors file content
				jorsDataList.add(IntermediateLineMsg);
				for(JobInstanceAction.JobInstanceActionItem actionItem : retJobInstanceActionFile.getJobInstanceActionItems()) {
					if(actionItem.getJorsRequestParameters() != null) {
						String fileName = null;
						int lastBackSlash = actionItem.getJorsRequestParameters().lastIndexOf(ICmdConstants.BACKSLASH);
						if(lastBackSlash > -1) {
							fileName = actionItem.getJorsRequestParameters().substring(lastBackSlash + 1, actionItem.getJorsRequestParameters().length());
						} else {
							fileName = actionItem.getJorsRequestParameters();
						}
						jorsDataFileName = fileName;
						jorsDataList.add(MessageFormat.format(JobLogHeaderMsg,fileName));
						jorsDataList.add(IntermediateLineMsg);
					}
					if(actionItem.getResult() == Result.SUCCESS) {
						 String[] lines = splitJorsDataLinesByCRNL(actionItem.getData());
						 for(int cntr = 0; cntr < lines.length; cntr++) {
							 jorsDataList.add(lines[cntr]);
						 }
					} else if(actionItem.getResult() == Result.FAILED) {
						jorsDataList.add(MessageFormat.format(RetrievingJobLogFailedMsg, actionItem.getMessage()));
					}
				}
				jorsDataList.add(IntermediateLineMsg);
				jorsFileContentCompleted();
				futureGetJorsFileContent.cancel(true);
			
			} else if(retJobInstanceActionFile.getResult() == Result.SUBMITTED) {
				// retry
			} else if(retJobInstanceActionFile.getResult() == Result.FAILED) {
				// extract the jors file content
				jorsDataList.add(IntermediateLineMsg);
				for(JobInstanceAction.JobInstanceActionItem actionItem : retJobInstanceActionFile.getJobInstanceActionItems()) {
					if(actionItem.getJorsRequestParameters() != null) {
						String fileName = null;
						int lastBackSlash = actionItem.getJorsRequestParameters().lastIndexOf(ICmdConstants.BACKSLASH);
						if(lastBackSlash > -1) {
							fileName = actionItem.getJorsRequestParameters().substring(lastBackSlash + 1, actionItem.getJorsRequestParameters().length());
						} else {
							fileName = actionItem.getJorsRequestParameters();
						}
						jorsDataFileName = fileName;
						jorsDataList.add(MessageFormat.format(JobLogHeaderMsg,fileName));
						jorsDataList.add(IntermediateLineMsg);
					}
					if(actionItem.getResult() == Result.SUCCESS) {
						 String[] lines = splitJorsDataLinesByCRNL(actionItem.getData());
						 for(int cntr = 0; cntr < lines.length; cntr++) {
							 jorsDataList.add(lines[cntr]);
						 }
					} else if(actionItem.getResult() == Result.FAILED) {
						jorsDataList.add(MessageFormat.format(RetrievingJobLogFailedMsg, actionItem.getMessage()));
					}
				}
				jorsDataList.add(IntermediateLineMsg);
				jorsFileContentCompleted();
				futureGetJorsFileContent.cancel(true);
			
			}			
		} catch (Exception ex) {
			jorsFileContentCompleted();
			futureGetJorsFileContent.cancel(true);
			throw new Exception(ex);

		}
	}	// END : etJorsFileContentStatus

	private synchronized void waitForJorsFileContentCompletion(
			) throws Exception {
		
		try {
			while(!isJorsFileContentComplete) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForJorsFileContentCompletion

	private synchronized void jorsFileContentCompleted(
			) throws Exception {
		
		try {
			isJorsFileContentComplete = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : jorsFileContentCompleted

	private String[] splitJorsDataLinesByCRNL(String data) throws Exception {
		String[] lines = null;
		
		try {
			lines = data.split("[\r\n]+");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception(ex);
		}
		return lines;
	}	// END : splitJorsDataLinesByCRNL

	
	
	
}
