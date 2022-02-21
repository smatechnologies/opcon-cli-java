package com.smatechnologies.opcon.command.api.utils.modules;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobsToCancelList {

	private List<JobsToCancel> jobsToCancel = null;

	public List<JobsToCancel> getJobsToCancel() {
		return jobsToCancel;
	}

	public void setJobsToCancel(List<JobsToCancel> jobsToCancel) {
		this.jobsToCancel = jobsToCancel;
	}
	
}
