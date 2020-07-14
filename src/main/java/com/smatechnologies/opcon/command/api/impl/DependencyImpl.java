package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IDependency;
import com.smatechnologies.opcon.command.api.interfaces.IJob;
import com.smatechnologies.opcon.command.api.modules.JobLogData;
import com.smatechnologies.opcon.command.api.modules.JobMonitorData;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.dailyjobs.WsDailyJobs;
import com.smatechnologies.opcon.restapiclient.model.JobStatus;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;


public class DependencyImpl implements IDependency {

	private static final String InvalidOpConAPI1710VersionMsg = "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String MonitorCompletedMsg =           "Monitor of Job ({0}) of Schedule ({1}) for date ({2}) on system ({3}) completed with code ({4})";
	private static final String MonitorFailedMsg =              "Monitor of Job ({0}) of Schedule ({1}) for date ({2}) on system ({3}) failed ({4})";
	private static final String ArgumentsMsg =                  "arguments";
	private static final String JobLogHeaderMsg =               "Remote Job joblog";									
	private static final String JobLogLineMsg =                 "Job Log : {0}";									
	private static final String SeperatorLineMsg =              "---------------------------------------------------------------------------------";

	private static final String MonitorJobMsg =                      "Monitoring job ({0}) of schedule ({1}) on date ({2}) on system ({3}) for completion status";
	
	private static final String DisplayOpConSystemArgumentMsg =      "-o   (OpCon system)      : {0}";
	private static final String DisplayTaskDateArgumentMsg =         "-d   (date)              : {0}";
	private static final String DisplayScheduleNameArgumentMsg =     "-sn  (schedule name)     : {0}";
	private static final String DisplayJobNameArgumentMsg =          "-jn  (job name)          : {0}";

	DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final static Logger LOG = LoggerFactory.getLogger(DependencyImpl.class);
	private IJob _IJob = new JobImpl();
	private Utilities _Utilities = new Utilities();

	private ScheduledExecutorService executorWaitForDependentJobToFinish = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> futureWaitForDependentJobToFinish = null;
	private boolean dependentJobFinished = false;
	private JobMonitorData jobMonitorData = new JobMonitorData();

	public Integer remoteDependency(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			LOG.info(MessageFormat.format(MonitorJobMsg, _OpConCliArguments.getJobName(), _OpConCliArguments.getScheduleName(), _OpConCliArguments.getTaskDate(), 
					_OpConCliArguments.getOpConSystem()));
			// get the daily job 
			DailyJob dailyJob = _IJob.getDailyJobByName(opconApi, _OpConCliArguments);
			if(dailyJob != null) {
				checkIfJobDependencyFinished(opconApi, dailyJob.getId());
				if(jobMonitorData.getType() == 6) {		// UNIX
					// strip off leading + and only use values up to semi colon (:)
					String uCode = jobMonitorData.getTerminationCode();
					int firstColon = uCode.indexOf(ICmdConstants.COLON);
					if(firstColon > -1) {
						uCode = uCode.substring(0, firstColon);
					}
					uCode = uCode.replaceAll(ICmdConstants.PLUS, ICmdConstants.EMPTY_STRING);
					success = Integer.parseInt(uCode);
				} else if(jobMonitorData.getType() == -1) {		// NULL
					// set NULL job to 0
					success = 0;
				} else {
					success = Integer.parseInt(jobMonitorData.getTerminationCode());
				}
				if((!(jobMonitorData.getType() == -1)) &&
						(!(jobMonitorData.getType() == 15))) {
					List<JobLogData> jobLogDataList = _IJob.getJobLogByDailyJob(opconApi, dailyJob);
					LOG.info(SeperatorLineMsg);
					LOG.info(JobLogHeaderMsg);
					LOG.info(SeperatorLineMsg);
					for(JobLogData jobLogData : jobLogDataList) {
						for(String record : jobLogData.getRecords()) {
							LOG.info(MessageFormat.format(JobLogLineMsg, record));
						}
		
					}
				}
				LOG.info(MessageFormat.format(MonitorCompletedMsg, _OpConCliArguments.getJobName(), _OpConCliArguments.getScheduleName(), 
						_OpConCliArguments.getTaskDate(), _OpConCliArguments.getOpConSystem(), String.valueOf(success)));
			} else {
				LOG.error(MessageFormat.format(MonitorFailedMsg, _OpConCliArguments.getJobName(), _OpConCliArguments.getScheduleName(), 
						_OpConCliArguments.getTaskDate(), _OpConCliArguments.getOpConSystem(), "Job not found in Daily tables"));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayOpConSystemArgumentMsg, _OpConCliArguments.getOpConSystem()));
				LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
				LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, _OpConCliArguments.getScheduleName()));
				LOG.error(MessageFormat.format(DisplayJobNameArgumentMsg, _OpConCliArguments.getJobName()));
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	protected void checkIfJobDependencyFinished(
			OpconApi opconApi,
			final String jobId
			) throws Exception {
		
		try {
		    final Runnable checkIfDependentJobFinishedRoutine = new Runnable() {
		         public void run() { 
		        	 try {
						getDependentJobStatus(opconApi, jobId);
					} catch (Exception ex) {
						try {
							LOG.error(_Utilities.getExceptionDetails(ex));
						} catch (Exception e) {
							LOG.error(_Utilities.getExceptionDetails(e));
						}
					} 
		         }
		       };
		       futureWaitForDependentJobToFinish = executorWaitForDependentJobToFinish.scheduleWithFixedDelay(checkIfDependentJobFinishedRoutine, 4, 2, TimeUnit.SECONDS);				
		       dependentJobFinished = false;;
		       waitForDependentJobToFinish();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : checkIfJobDependencyFinished
	
	private synchronized void waitForDependentJobToFinish(
			) throws Exception {
		
		try {
			while(!dependentJobFinished) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForDependentJobToFinish

	public synchronized void dependentJobCompleted() throws Exception {
		
		try {
			dependentJobFinished = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : dependentJobCompleted

	private void getDependentJobStatus(
			OpconApi opconApi,
			String jobId
			) throws Exception {

		try {
			WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
			DailyJob dailyJob = wsDailyJobs.get(jobId);
			LOG.debug("getDependentJobStatus:dailyJob : (" + dailyJob.getStatus().getId() + ") desc (" + dailyJob.getStatus().getDescription() + ")");
			JobStatus status = dailyJob.getStatus();
			switch (status.getId()){
			
				case 0:
					LOG.debug("getDependentJobStatus:status : On Hold");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;

				case 210:
					LOG.debug("getDependentJobStatus:status : Initialization Error");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;

				case 900:
					LOG.debug("getDependentJobStatus:status : Finished OK");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;
					
				case 910:
					LOG.debug("getDependentJobStatus:status : Failed");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;
					
				case 920:
					LOG.debug("getDependentJobStatus:status : Mark Finished OK");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;

				case 921:
					LOG.debug("getDependentJobStatus:status : Mark Failed");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;

				case 940:
					LOG.debug("getDependentJobStatus:status : Skipped");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;

				case 950:
					LOG.debug("getDependentJobStatus:status : Cancelled");
					jobMonitorData.setType(dailyJob.getJobType().getId());
					jobMonitorData.setExitCode(status.getId());
					jobMonitorData.setExitDescription(status.getDescription().name());
					jobMonitorData.setTerminationCode(dailyJob.getTerminationDescription());
					dependentJobCompleted();
					futureWaitForDependentJobToFinish.cancel(true);
					break;
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}	// END : getDailyJobStatus
	
}
