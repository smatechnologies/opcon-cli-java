package com.smatechnologies.opcon.command.api.utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.enums.TaskType;
import com.smatechnologies.opcon.command.api.impl.JobImpl;
import com.smatechnologies.opcon.command.api.impl.OpConCliImpl;
import com.smatechnologies.opcon.command.api.interfaces.IJob;
import com.smatechnologies.opcon.command.api.interfaces.IOpConCli;
import com.smatechnologies.opcon.command.api.utils.modules.CancelJobsArguments;
import com.smatechnologies.opcon.command.api.utils.modules.JobsToCancel;
import com.smatechnologies.opcon.command.api.utils.modules.JobsToCancelList;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;

public class CancelJobsImpl {

	private static final String JobCancelMsg = "Cancel job {0}.{1} current status {2}";
	private static final String JobCancelledMsg = "Job {0}.{1} cancelled";
	private static final String JobRunningMsg = "Job {0}.{1} in a running state";
	private static final String StartAttemptedMsg = "Job {0}.{1} in a start attempted state";
	private static final String FinishedOkMsg = "Job {0}.{1} in a finsihed ok state";
	private static final String JobNotFoundMsg = "Job {0}.{1} not found";

	private final static Logger LOG = LoggerFactory.getLogger(CancelJobsImpl.class);
	private IOpConCli _IOpConCli = new OpConCliImpl();
	private IJob _IJob = new JobImpl();
	
	private DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();

	public boolean processRequest(
			CancelJobsArguments _CancelJobsArguments
			) throws Exception {
		
		boolean success = true;
		boolean cancelJob = true;
		
		OpconApi opconAppi = _IOpConCli.getOpConApi();
		JobsToCancelList jobList = getJobInformation(_CancelJobsArguments.getFileName());
		for( JobsToCancel cancel : jobList.getJobsToCancel()) {
			// check job status
			OpConCliArguments scliArguments = new OpConCliArguments();
			scliArguments.setTask(TaskType.GetJobStatus.toString());
			scliArguments.setTaskDate(_CancelJobsArguments.geteDate());
			scliArguments.setOpConSystem(_CancelJobsArguments.getOpConSystem());
			scliArguments.setScheduleName(cancel.getScheduleName());
			scliArguments.setJobName(cancel.getJobName());
			DailyJob djob = _IJob.getDailyJobByName(opconAppi, scliArguments);
			if(djob != null) {
				LOG.info(MessageFormat.format(JobCancelMsg, cancel.getScheduleName(), cancel.getJobName(), djob.getStatus().getDescription()));
				if(djob.getStatus().getId() == 950) {
					LOG.info(MessageFormat.format(JobCancelledMsg, cancel.getScheduleName(), cancel.getJobName()));
					cancelJob = false;
				} else if(djob.getStatus().getId() == 500) {
					LOG.info(MessageFormat.format(JobRunningMsg, cancel.getScheduleName(), cancel.getJobName()));
					cancelJob = false;
				} else if(djob.getStatus().getId() == 200) {
					LOG.info(MessageFormat.format(StartAttemptedMsg, cancel.getScheduleName(), cancel.getJobName()));
					cancelJob = false;
				} else if(djob.getStatus().getId() == 900) {
					LOG.info(MessageFormat.format(FinishedOkMsg, cancel.getScheduleName(), cancel.getJobName()));
					cancelJob = false;
				} else {
					cancelJob = true;
				}
				if(cancelJob) {
					// go cancel the job
					OpConCliArguments ccliArguments = new OpConCliArguments();
					ccliArguments.setTask(TaskType.JobAction.toString());
					ccliArguments.setTaskDate(_CancelJobsArguments.geteDate());
					ccliArguments.setOpConSystem(_CancelJobsArguments.getOpConSystem());
					ccliArguments.setScheduleName(cancel.getScheduleName());
					ccliArguments.setJobName(cancel.getJobName());
					ccliArguments.setJobAction("cancel");
					Integer cstatus = _IOpConCli.processRequest(ccliArguments);
					if(cstatus == 0) {
						LOG.info(MessageFormat.format(JobCancelledMsg, cancel.getScheduleName(), cancel.getJobName()));
					}
				}
			} else {
				LOG.info(MessageFormat.format(JobNotFoundMsg, cancel.getScheduleName(), cancel.getJobName()));
			}
		}
		return success;
	}

	private JobsToCancelList getJobInformation(
			String filename
			) throws Exception {
		JobsToCancelList jobList = new JobsToCancelList();
		
		jobList = _DefaultObjectMapperProvider.getObjectMapper().readValue(
				new InputStreamReader(new FileInputStream(filename),"UTF8"), JobsToCancelList.class );
		return jobList;
	}

	private boolean canCancel(
			String category
			) throws Exception {
		
		boolean canCancel = false;
		
		if(category.equalsIgnoreCase("HELD")) {
			canCancel = true;
		} else if(category.equalsIgnoreCase("WAITING")) {
			canCancel = true;
		} else if(category.equalsIgnoreCase("MISSED START TIME")) {
			canCancel = true;
		} else if(category.equalsIgnoreCase("FAILED")) {
			canCancel = true;
		} else if(category.equalsIgnoreCase("UNDER REVIEW")) {
			canCancel = true;
		} 
		return canCancel;
	}
}
