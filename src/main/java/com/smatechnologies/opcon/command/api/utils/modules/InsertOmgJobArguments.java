package com.smatechnologies.opcon.command.api.utils.modules;

import com.beust.jcommander.Parameter;

public class InsertOmgJobArguments {

	public static final String FileNameArgumentDescriptionMsg = "Json File containing job definitions";
	public static final String DateArgumentDescriptionMsg = "Date (value is yyyy-mm-dd)";
	public static final String ScheduleNameArgumentDescriptionMsg = "Name of schedule to insert the jobs into";
	public static final String JobNameArgumentDescriptionMsg = "Name of job to add";
	public static final String OpConSystemArgumentDescriptionMsg = "The name of the OpCon system to submit the request to - matches a header in the config file";
	public static final String FrequencyArgumentDescriptionMsg = "The name of the frequency associated with the job being inserted";

	@Parameter(names="-f", required=true, description = FileNameArgumentDescriptionMsg)
	private String fileName = null;

	@Parameter(names="-d", required=true, description = DateArgumentDescriptionMsg)
	private String eDate = null;

	@Parameter(names="-s", required=true, description = ScheduleNameArgumentDescriptionMsg)
	private String scheduleName = null;

	@Parameter(names="-j", required=true, description = JobNameArgumentDescriptionMsg)
	private String jobName = null;

	@Parameter(names="-o", required=true, description = OpConSystemArgumentDescriptionMsg)
	private String opConSystem = null;

	@Parameter(names="-fr", required=true, description = FrequencyArgumentDescriptionMsg)
	private String frequency = null;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String geteDate() {
		return eDate;
	}

	public void seteDate(String eDate) {
		this.eDate = eDate;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getOpConSystem() {
		return opConSystem;
	}

	public void setOpConSystem(String opConSystem) {
		this.opConSystem = opConSystem;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
}
