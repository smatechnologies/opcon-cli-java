package com.smatechnologies.opcon.command.api.utils.modules;

import com.beust.jcommander.Parameter;

public class CancelJobsArguments {

	public static final String FileNameArgumentDescriptionMsg = "File containing jobs definitions to cancel";
	public static final String DateArgumentDescriptionMsg = "Date (value is yyyy-mm-dd)";
	public static final String OpConSystemArgumentDescriptionMsg = "The name of the OpCon system to submit the request to - matches a header in the config file";

	@Parameter(names="-f", required=true, description = FileNameArgumentDescriptionMsg)
	private String fileName = null;

	@Parameter(names="-d", required=true, description = DateArgumentDescriptionMsg)
	private String eDate = null;

	@Parameter(names="-o", required=true, description = OpConSystemArgumentDescriptionMsg)
	private String opConSystem = null;

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

	public String getOpConSystem() {
		return opConSystem;
	}

	public void setOpConSystem(String opConSystem) {
		this.opConSystem = opConSystem;
	}

}
