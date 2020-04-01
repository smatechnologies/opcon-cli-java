package com.smatechnologies.opcon.command.api.interfaces;

import java.util.List;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.modules.JobLogData;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;

public interface IJob {

	public Integer jobActionRequest(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public Integer jobAddRequest(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public List<JobLogData> getJobLog(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public List<JobLogData> getJobLogByDailyJob(OpconApi opconApi, DailyJob dailyJob) throws Exception;
	public DailyJob getDailyJobByName(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public DailyJob getDailyJobById(OpconApi opconApi, String jobId) throws Exception;

}
