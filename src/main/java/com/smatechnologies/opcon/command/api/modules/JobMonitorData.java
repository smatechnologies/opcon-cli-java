package com.smatechnologies.opcon.command.api.modules;

import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;

public class JobMonitorData {

	private Integer type = null;
	private String description = null;
	private Integer exitCode = null;
	private String exitDescription = null;
	private String terminationCode = null;
	private DailyJob dailyJob = null;
	
	public Integer getType() {
		return type;
	}
	
	public void setType(Integer type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getExitCode() {
		return exitCode;
	}

	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}

	public String getExitDescription() {
		return exitDescription;
	}

	public void setExitDescription(String exitDescription) {
		this.exitDescription = exitDescription;
	}

	public String getTerminationCode() {
		return terminationCode;
	}

	public void setTerminationCode(String terminationCode) {
		this.terminationCode = terminationCode;
	}

	public DailyJob getDailyJob() {
		return dailyJob;
	}

	public void setDailyJob(DailyJob dailyJob) {
		this.dailyJob = dailyJob;
	}
	
}

