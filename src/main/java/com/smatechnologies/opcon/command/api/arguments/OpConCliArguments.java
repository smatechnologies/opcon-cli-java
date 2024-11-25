package com.smatechnologies.opcon.command.api.arguments;

import com.beust.jcommander.Parameter;

public class OpConCliArguments {

	public static final String DateArgumentDescriptionMsg = "(Optional) Date to execute on (if not present defaults to current date)";
	public static final String FrequencyNameArgumentDescriptionMsg = "(Optional ) Required for JobAdd - The frequency name associated with the request)";
	public static final String JobNameArgumentDescriptionMsg = "(Optional) Required for Dependency, JobAdd, JobAction, JobLog - The name of the job)";
	public static final String JobActionArgumentDescriptionMsg = "(Optional) Required for JobAction - The new status of the job (values cancel, skip, hold, etc)";
	public static final String PropertiesArgumentDescriptionMsg = "(Optional) A list of properties to be added (format : name=value,name=value)";
	public static final String TaskArgumentDescriptionMsg = "Task to execute (values AppToken, Dependency, ExpEval, GetJobLog ,JobAdd, JobAction, JobLog, MachAction, MachAdd, MachGrpAdd, MachGrpRemove, MachUpdate, PropExp, PropUpdate, SchedBuild, SchedAction, SchedRebuild, ThreshUpdate)";
	public static final String ScheduleNameArgumentDescriptionMsg = "(Optional) Required for Dependency, SchedAction, SchedBuild, JobAdd, JobAction, JobLog - The name of the schedule)";
	public static final String PropertyNameArgumentDescriptionMsg = "(Optional) Required for PropUpdate, PropExp - The name of the property";
	public static final String PropertyValueArgumentDescriptionMsg = "(Optional) Required for PropUpdate, PropExp - The value of the property";
	public static final String PropertyEncryptedArgumentDescriptionMsg = "(Optional) Optional for PropUpdate - Indicates if the property is encrypted (values true or false)";
	public static final String BuildOnHoldArgumentDescriptionMsg = "(Optional) Optional for SchedBuild - indicates if the schedule should be built on hold)";
	public static final String JobAddOnHoldArgumentDescriptionMsg = "(Optional) Optional for JobAdd - indicates if the job should be added on hold)";
	public static final String JobAddWaitForCompletionArgumentDescriptionMsg = "(Optional) Optional for JobAdd - indicates if the process should wait for job completion)";
	public static final String JobLogDirectoryArgumentDescriptionMsg = "(Optional) Required for JobLog - full path to directory to write log information into)";
	public static final String ScheduleActionArgumentDescriptionMsg = "(Optional) Required for SchedAction - the action to be applied to the schedule (values hold, release, start, close)";
	public static final String MachineNameArgumentDescriptionMsg = "(Optional) Required for MachAction, MachGrpAdd, MachGrpRemove - The list of machine names to update the status for (format mach1,mach2)";
	public static final String MachineActionArgumentDescriptionMsg = "(Optional) Required for MachAction - the new status of the machine (values up, down, limited, wlimited)";
	public static final String OpConSystemArgumentDescriptionMsg = "The name of the OpCon system to submit the request to - matches a header in the config file";
	public static final String ThresholdNameArgumentDescriptionMsg = "(Optional) Required for ThreshUpdate - The name of the threshold";
	public static final String ThresholdValueArgumentDescriptionMsg = "(Optional) Required for ThreshUpdate - The value of the threshold";
	public static final String MachineGroupNameArgumentDescriptionMsg = "(Optional) Required for MachGrpAdd, MachGrpRemove - The name of the machine group to add to or remove the machine from";
	public static final String MachineAddFileArgumentDescriptionMsg = "(Optional) Required for MachAdd - The full pathname of the file containing the machine(s) definition to add";
	public static final String MachineAddIpAddressArgumentDescriptionMsg = "(Optional) Optional for MachAdd - The IP address of the machine to add (replaces tcpIpAddress value in definition)";
	public static final String MachineAddDnsAddressArgumentDescriptionMsg = "(Optional) Optional for MachAdd - The DNS address of the machine to add (replaces the fullyQualifiedDomainName value in the definition)";
	public static final String MachineAddNameArgumentDescriptionMsg = "(Optional) Optional for MachAdd - The name of the machine to add (replaces the name value in the definition)";
	public static final String ApplicationNameArgumentDescriptionMsg = "(Optional) Required for AppToken - The name of the application to create a token for";
	public static final String MachineUpdateNameArgumentDescriptionMsg = "(Optional) Required for MachUpdate - The name of the machine to update";
	public static final String MachineUpdateNewNameArgumentDescriptionMsg = "(Optional) Optional for MachUpdate - The updated name of the machine";
	public static final String MachineUpdateIpAddressArgumentDescriptionMsg = "(Optional) Optional for MachUpdate - The updated ip address of the machine";
	public static final String MachineUpdateDnsAddressArgumentDescriptionMsg = "(Optional) SOptional for MachUpdate - The updated dns address of the machine";
	public static final String ScheduleRebuildNoOfDayArgumentDescriptionMsg = "(Optional) Required for SchedRebuild - The number of days in advance to rebuild for";
	public static final String ExpressionEvaluationArgumentDescriptionMsg = "(Optional) Required for ExpEval - The expression to evaluate";
	public static final String ScheduleRebuildIndicatorsArgumentDescriptionMsg = "(Optional) Optional for SchedRebuild - An indicator used to determine if a schedule should be rebuilt - checks if the schedule name starts with this value";
	public static final String DefaultErrorCodeArgumentDescriptionMsg = "The error code returned when an error occurs in the opconCli (default -1)";
	public static final String JobLogFileNameArgumentDescriptionMsg = "(Optional) Defines a filename that the returned joblog must be written into (JobLog)";
	public static final String RemoteDependencyJobLogArgumentDescriptionMsg = "(Optional) Defines if the joblog should be retrieved after dependency check completed (Dependency)";
	

	@Parameter(names="-ap", description = ApplicationNameArgumentDescriptionMsg)
	private String applicationName = null;
	
	@Parameter(names="-d", description = DateArgumentDescriptionMsg)
	private String taskDate = null;
	
	@Parameter(names="-ec", description = DefaultErrorCodeArgumentDescriptionMsg)
	private Integer errorCode = -1;

	@Parameter(names="-ev", description = ExpressionEvaluationArgumentDescriptionMsg)
	private String expression = null;

	@Parameter(names="-jf", description = FrequencyNameArgumentDescriptionMsg)
	private String frequencyName = null;
	
	@Parameter(names="-jn", description = JobNameArgumentDescriptionMsg)
	private String jobName = null;
	
	@Parameter(names="-ja", description = JobActionArgumentDescriptionMsg)
	private String jobAction = null;
	
	@Parameter(names="-joh", description = JobAddOnHoldArgumentDescriptionMsg)
	private boolean addOnHold = false;
	
	@Parameter(names="-jld", description = JobLogDirectoryArgumentDescriptionMsg)
	private String jobLogDirectory = null;

	@Parameter(names="-jw", description = JobAddWaitForCompletionArgumentDescriptionMsg)
	private boolean waitForCompletion = false;
	
	@Parameter(names="-jlf", description = JobLogFileNameArgumentDescriptionMsg)
	private String jobLogFileName = null;

	@Parameter(names="-ip", description = PropertiesArgumentDescriptionMsg)
	private String instanceProperties = null;
	
	@Parameter(names="-t", required=true, description = TaskArgumentDescriptionMsg)
	private String task = null;
	
	@Parameter(names="-sn", description = ScheduleNameArgumentDescriptionMsg)
	private String scheduleName = null;
	
	@Parameter(names="-sa", description = ScheduleActionArgumentDescriptionMsg)
	private String scheduleAction = null;
	
	@Parameter(names="-pe", description = PropertyEncryptedArgumentDescriptionMsg)
	private boolean propertyEncrypted = false;
	
	@Parameter(names="-pn", description = PropertyNameArgumentDescriptionMsg)
	private String propertyName = null;
	
	@Parameter(names="-pv", description = PropertyValueArgumentDescriptionMsg)
	private String propertyValue = null;
	
	@Parameter(names="-soh", description = BuildOnHoldArgumentDescriptionMsg)
	private boolean buildOnHold = false;
	
	@Parameter(names="-md", description = MachineAddDnsAddressArgumentDescriptionMsg)
	private String machineDnsAddress = null;

	@Parameter(names="-mn", description = MachineNameArgumentDescriptionMsg)
	private String machineName = null;
	
	@Parameter(names="-mf", description = MachineAddFileArgumentDescriptionMsg)
	private String machineFileName = null;
	
	@Parameter(names="-mi", description = MachineAddIpAddressArgumentDescriptionMsg)
	private String machineIpAddress = null;
	
	@Parameter(names="-mg", description = MachineGroupNameArgumentDescriptionMsg)
	private String machineGroupName = null;
	
	@Parameter(names="-ma", description = MachineActionArgumentDescriptionMsg)
	private String machineAction = null;
	
	@Parameter(names="-mnu", description = MachineUpdateNewNameArgumentDescriptionMsg)
	private String machineNameUpdate = null;
	
	@Parameter(names="-miu", description = MachineUpdateIpAddressArgumentDescriptionMsg)
	private String machineIpAddressUpdate = null;
	
	@Parameter(names="-mdu", description = MachineUpdateDnsAddressArgumentDescriptionMsg)
	private String machineDnsAddressUpdate = null;
	
	@Parameter(names="-o", required=true, description = OpConSystemArgumentDescriptionMsg)
	private String opConSystem = null;
	
	@Parameter(names="-tn", description = ThresholdNameArgumentDescriptionMsg)
	private String thresholdName = null;
	
	@Parameter(names="-tv", description = ThresholdValueArgumentDescriptionMsg)
	private Integer thresholdValue = null;
	
	@Parameter(names="-sd", description = ScheduleRebuildNoOfDayArgumentDescriptionMsg)
	private Integer noOfDaysToRebuild = 0;
	
	@Parameter(names="-sri", description = ScheduleRebuildIndicatorsArgumentDescriptionMsg)
	private String scheduleRebuildIndicator = null;
	
	@Parameter(names="-rlf", description = RemoteDependencyJobLogArgumentDescriptionMsg)
	private boolean retrieveLogFiles = false;
	
	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getTaskDate() {
		return taskDate;
	}
	
	public void setTaskDate(String taskDate) {
		this.taskDate = taskDate;
	}
	
	public String getFrequencyName() {
		return frequencyName;
	}
	
	public void setFrequencyName(String frequencyName) {
		this.frequencyName = frequencyName;
	}
	
	public String getJobName() {
		return jobName;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getJobAction() {
		return jobAction;
	}

	public void setJobAction(String jobAction) {
		this.jobAction = jobAction;
	}

	public boolean isAddOnHold() {
		return addOnHold;
	}

	public void setAddOnHold(boolean addOnHold) {
		this.addOnHold = addOnHold;
	}

	public String getInstanceProperties() {
		return instanceProperties;
	}
	
	public void setInstanceProperties(String instanceProperties) {
		this.instanceProperties = instanceProperties;
	}
	
	public String getTask() {
		return task;
	}
	
	public void setTask(String task) {
		this.task = task;
	}
	
	public String getScheduleName() {
		return scheduleName;
	}
	
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getScheduleAction() {
		return scheduleAction;
	}

	public void setScheduleAction(String scheduleAction) {
		this.scheduleAction = scheduleAction;
	}

	public boolean isPropertyEncrypted() {
		return propertyEncrypted;
	}

	public void setPropertyEncrypted(boolean propertyEncrypted) {
		this.propertyEncrypted = propertyEncrypted;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public boolean isBuildOnHold() {
		return buildOnHold;
	}

	public void setBuildOnHold(boolean buildOnHold) {
		this.buildOnHold = buildOnHold;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMachineAction() {
		return machineAction;
	}

	public void setMachineAction(String machineAction) {
		this.machineAction = machineAction;
	}

	public String getOpConSystem() {
		return opConSystem;
	}

	public void setOpConSystem(String opConSystem) {
		this.opConSystem = opConSystem;
	}

	public String getThresholdName() {
		return thresholdName;
	}

	public void setThresholdName(String thresholdName) {
		this.thresholdName = thresholdName;
	}

	public Integer getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(Integer thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	public String getMachineGroupName() {
		return machineGroupName;
	}

	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getMachineFileName() {
		return machineFileName;
	}

	public void setMachineFileName(String machineFileName) {
		this.machineFileName = machineFileName;
	}

	public String getMachineIpAddress() {
		return machineIpAddress;
	}

	public void setMachineIpAddress(String machineIpAddress) {
		this.machineIpAddress = machineIpAddress;
	}

	public String getMachineDnsAddress() {
		return machineDnsAddress;
	}

	public void setMachineDnsAddress(String machineDnsAddress) {
		this.machineDnsAddress = machineDnsAddress;
	}

	public String getMachineNameUpdate() {
		return machineNameUpdate;
	}

	public void setMachineNameUpdate(String machineNameUpdate) {
		this.machineNameUpdate = machineNameUpdate;
	}

	public String getMachineIpAddressUpdate() {
		return machineIpAddressUpdate;
	}

	public void setMachineIpAddressUpdate(String machineIpAddressUpdate) {
		this.machineIpAddressUpdate = machineIpAddressUpdate;
	}

	public String getMachineDnsAddressUpdate() {
		return machineDnsAddressUpdate;
	}

	public void setMachineDnsAddressUpdate(String machineDnsAddressUpdate) {
		this.machineDnsAddressUpdate = machineDnsAddressUpdate;
	}

	public boolean isWaitForCompletion() {
		return waitForCompletion;
	}

	public void setWaitForCompletion(boolean waitForCompletion) {
		this.waitForCompletion = waitForCompletion;
	}

	public String getJobLogDirectory() {
		return jobLogDirectory;
	}

	public void setJobLogDirectory(String jobLogDirectory) {
		this.jobLogDirectory = jobLogDirectory;
	}

	public Integer getNoOfDaysToRebuild() {
		return noOfDaysToRebuild;
	}

	public void setNoOfDaysToRebuild(Integer noOfDaysToRebuild) {
		this.noOfDaysToRebuild = noOfDaysToRebuild;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getScheduleRebuildIndicator() {
		return scheduleRebuildIndicator;
	}

	public void setScheduleRebuildIndicator(String scheduleRebuildIndicator) {
		this.scheduleRebuildIndicator = scheduleRebuildIndicator;
	}

	public String getJobLogFileName() {
		return jobLogFileName;
	}

	public void setJobLogFileName(String jobLogFileName) {
		this.jobLogFileName = jobLogFileName;
	}

	public boolean isRetrieveLogFiles() {
		return retrieveLogFiles;
	}

	public void setRetrieveLogFiles(boolean retrieveLogFiles) {
		this.retrieveLogFiles = retrieveLogFiles;
	}

}
