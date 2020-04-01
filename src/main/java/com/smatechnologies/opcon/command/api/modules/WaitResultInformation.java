package com.smatechnologies.opcon.command.api.modules;

public class WaitResultInformation {

	private Integer completionCode = null;
	private String failedReason = null;
	private String adjustedJobName = null;
	private String jobid = null;
	
	public Integer getCompletionCode() {
		return completionCode;
	}
	
	public void setCompletionCode(Integer completionCode) {
		this.completionCode = completionCode;
	}
	
	public String getFailedReason() {
		return failedReason;
	}
	
	public void setFailedReason(String failedReason) {
		this.failedReason = failedReason;
	}
	
	public String getAdjustedJobName() {
		return adjustedJobName;
	}
	
	public void setAdjustedJobName(String adjustedJobName) {
		this.adjustedJobName = adjustedJobName;
	}

	public String getJobid() {
		return jobid;
	}

	public void setJobid(String jobid) {
		this.jobid = jobid;
	}
	
}
