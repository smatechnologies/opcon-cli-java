package com.smatechnologies.opcon.command.api.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.enums.JobActions;
import com.smatechnologies.opcon.command.api.enums.TaskType;
import com.smatechnologies.opcon.command.api.interfaces.IOpConCli;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IDependency;
import com.smatechnologies.opcon.command.api.interfaces.IGlobalProperty;
import com.smatechnologies.opcon.command.api.interfaces.IJob;
import com.smatechnologies.opcon.command.api.interfaces.IMachine;
import com.smatechnologies.opcon.command.api.interfaces.IMachineGroup;
import com.smatechnologies.opcon.command.api.interfaces.IExpression;
import com.smatechnologies.opcon.command.api.interfaces.ISchedule;
import com.smatechnologies.opcon.command.api.interfaces.IThreshold;
import com.smatechnologies.opcon.command.api.interfaces.IToken;
import com.smatechnologies.opcon.command.api.interfaces.IVersion;
import com.smatechnologies.opcon.command.api.modules.JobLogData;
import com.smatechnologies.opcon.command.api.ws.WsLogger;
import com.smatechnologies.opcon.restapiclient.DefaultClientBuilder;
import com.smatechnologies.opcon.restapiclient.WsErrorException;
import com.smatechnologies.opcon.restapiclient.WsException;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.OpconApiProfile;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

public class OpConCliImpl implements IOpConCli {

	private static final String ApplicationTokenProcessingMsg =           "Processing task ({0}) arguments : appname ({1})";
	private static final String ApplicationTokenMissingNameMsg =	      "Required -ap (application name) argument missing for AppToken task";

	private static final String DependencyProcessingTaskMsg =             "Processing task ({0}) arguments : date ({1}) schedule ({2}) job ({3} retrieve log files ({4}))";
	private static final String DependencyMissingScheduleNameMsg =        "Required -sn (schedule name) argument missing for JobAdd task";
	private static final String DependencyMissingJobNameMsg =             "Required -jn (job name) argument missing for JobAdd task";

	private static final String ExpressionEvalProcessingTaskMsg =         "Processing task ({0}) arguments : expression ({1})";
	private static final String ExpressionEvalMissingExpressionMsg =      "Required -ev (expression) argument missing for ExpEval task";

	private static final String JobAddProcessingTaskMsg =                 "Processing task ({0}) arguments : date ({1}) schedule ({2}) job ({3}) frequency ({4}) properties ({5}) onhold ({6})";
	private static final String JobAddMissingScheduleNameMsg =            "Required -sn (schedule name) argument missing for JobAdd task";
	private static final String JobAddMissingJobNameMsg =                 "Required -jn (job name) argument missing for JobAdd task";
	private static final String JobAddMissingFrequencyMsg =               "Required -jf (frequency) argument missing for JobAdd task";
	
	private static final String JobActionProcessingTaskMsg =              "Processing task ({0}) arguments : date ({1}) schedule ({2}) job ({3}) action ({4}))";
	private static final String JobActionMissingScheduleNameMsg =         "Required -sn (schedule name) argument missing for JobAction task";
	private static final String JobActionMissingJobNameMsg =              "Required -jn (job name) argument missing for JobAction task";
	private static final String JobActionMissingJobActionMsg =            "Required -ja (job action) argument missing for JobAction task";
	
	private static final String JobLogProcessingTaskMsg =                 "Processing task ({0}) arguments : date ({1}) schedule ({2}) job ({3}) file ({4})";
	private static final String JobLogMissingScheduleNameMsg =            "Required -sn (schedule name) argument missing for JobLog task";
	private static final String JobLogMissingJobNameMsg =                 "Required -jn (job name) argument missing for JobLog task";
	private static final String JobLogFileMsg =                           "Writing joblog to file ({0})";

	private static final String JobStatusProcessingTaskMsg =              "Processing task ({0}) arguments : date ({1}) schedule ({2}) job ({3})";
	private static final String JobStatusMissingScheduleNameMsg =         "Required -sn (schedule name) argument missing for JobStatus task";
	private static final String JobStatusMissingJobNameMsg =              "Required -jn (job name) argument missing for JobStatus task";
	private static final String JobStatusMsg =                            "Job schedule ({0}) job ({1} status : ({2})";

	private static final String MachineActionProcessingTaskMsg =          "Processing task ({0}) arguments : machine ({1}) action ({2})";
	private static final String MachineActionMissingNameMsg =		      "Required -mn (machine names) argument missing for MachAction task";
	private static final String MachineActionMissingActionMsg =	  	      "Required -ma (machine action) argument missing for MachAction task";
	
	private static final String MachineAddProcessingTaskMsg =             "Processing task ({0}) arguments : machine file name ({1})";
	private static final String MachineAddMissingFileNameMsg =		      "Required -mf (machine file name) argument missing for MachAdd task";
	private static final String MachineAddJsonParseErrorMsg =		      "MachAdd : Error parsing file : {0} : {1}";
	private static final String MachineAddFileErrorMsg =		          "MachAdd : Error reading file : {0} : {1}";

	private static final String MachineGroupProcessingTaskMsg =           "Processing task ({0}) arguments : machineGroup ({1}) machine name ({2})";
	private static final String MachineGroupMissingNameMsg =		      "Required -mg (machine group) argument missing for MachGroupn task";
	private static final String MachineGroupMissingMachineNameMsg =	  	  "Required -mn (machine name) argument missing for MachGroup task";
	
	private static final String MachineUpdateProcessingTaskMsg =          "Processing task ({0}) arguments : machine ({1}) new name ({2}) ipadr ({3}) dns ({4})";
	private static final String MachineUpdateMissingNameMsg =		      "Required -mn (machine names) argument missing for MachUpdate task";

	private static final String PropertyExpProcessingTaskMsg =            "Processing task ({0}) arguments : expression ({1})";
	private static final String PropertyExpMissingNameMsg = 	  	      "Required -pn (property name) argument missing for PropExp task";
	private static final String PropertyExpMissingValueMsg = 	          "Required -pv (property value) argument missing for PropExp task";

	private static final String PropertyProcessingUpdateTaskMsg =         "Processing task ({0}) arguments : property ({1}) value ({2}) encrypted ({3})";
	private static final String PropertyUpdateMissingNameMsg = 	  	      "Required -pn (property name) argument missing for PropUpdate task";
	private static final String PropertyUpdateMissingValueMsg = 	      "Required -pv (property value) argument missing for PropUpdate task";

	private static final String ScheduleActionProcessingTaskMsg =         "Processing task ({0}) arguments : date ({1}) days ({2}) indicator ({3}))";
	private static final String ScheduleActionMissingScheduleNameMsg =    "Required -sn (schedule name) argument missing for SchedAction task";
	private static final String ScheduleActionMissingActionMsg =          "Required -sa (schedule action) argument missing for SchedAction task";

	private static final String ScheduleBuildProcessingTaskMsg =          "Processing task ({0}) arguments : date ({1}) schedule ({2}) action ({3})";
	private static final String ScheduleBuildMissingNameMsg =		      "Required -sn (schedule name) argument missing for SchedBuild task";
	
	private static final String ScheduleRebuildProcessingTaskMsg =        "Processing task ({0}) arguments : date ({1}) days ({2}) indicator ({3}))";
	private static final String ScheduleRebuildMissingDaysToBuildMsg =	  "Required -sd (no of days) argument missing for SchedRebuild task";

	private static final String ThresholdProcessingCreateTaskMsg =        "Processing task ({0}) arguments : threshold ({1}) value ({2})";
	private static final String ThresholdCreateMissingNameMsg =		      "Required -tn (threshold name) argument missing for ThreshCreate task";
	private static final String ThresholdCreateMissingValueMsg =	      "Required -tv (threshold value) argument missing for ThreshCreate task";
	private static final String ThresholdProcessingUpdateTaskMsg =        "Processing task ({0}) arguments : threshold ({1}) value ({2})";
	private static final String ThresholdUpdateMissingNameMsg =		      "Required -tn (threshold name) argument missing for ThreshUpdate task";
	private static final String ThresholdUpdateMissingValueMsg =	      "Required -tv (threshold value) argument missing for ThreshUpdate task";
	
	private static final String UrlFormatTls = "https://{0}:{1}/api";
	private static final String UrlFormatNonTls = "http://{0}:{1}/api";
	
	private final static Logger LOG = LoggerFactory.getLogger(OpConCliImpl.class);
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	private DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();
	private IDependency _IDependency = new DependencyImpl();
	private IJob _IJob = new JobImpl();
	private IGlobalProperty _IGlobalProperty = new GlobalPropertyImpl();
	private IMachine _IMachine = new MachineImpl();
	private IMachineGroup _IMachineGroup = new MachineGroupImpl();
	private IExpression _IExpression = new ExpressionImpl();
	private ISchedule _ISchedule = new ScheduleImpl();
	private IThreshold _IThreshold = new ThresholdImpl();
	private IToken _IToken = new TokenImpl();
	private IVersion _IVersion = new VersionImpl();
	
	private String displayProperties = null;
	
	public OpconApi getOpConApi(
			) throws Exception {

		String url = null;
		
		// create client connection
		if(_CmdConfiguration.isUsingTls()) {
			url = MessageFormat.format(UrlFormatTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
		} else {
			url = MessageFormat.format(UrlFormatNonTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
		}
		OpconApiProfile profile = new OpconApiProfile(url);
		OpconApi opconApi = getClient(profile);
		return opconApi;
	}
	
	public Integer processRequest(
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer completionCode = null;
		String url = null;
		
		// create client connection
		if(_CmdConfiguration.isUsingTls()) {
			url = MessageFormat.format(UrlFormatTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
		} else {
			url = MessageFormat.format(UrlFormatNonTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
		}
		OpconApiProfile profile = new OpconApiProfile(url);
		OpconApi opconApi = getClient(profile);
		
		try {

			TaskType taskType = TaskType.valueOf(_OpConCliArguments.getTask());
			
			switch (taskType) {
			
				case AppToken:
					if(_OpConCliArguments.getApplicationName() == null) {
						LOG.error(ApplicationTokenMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(ApplicationTokenProcessingMsg, _OpConCliArguments.getTask(), _OpConCliArguments.getApplicationName()));
					completionCode = _IToken.createApplicationToken(opconApi, _OpConCliArguments);
					break;
					
				case Dependency:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(DependencyMissingScheduleNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getJobName() == null) {
						LOG.error(DependencyMissingJobNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(DependencyProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getTaskDate(),
							_OpConCliArguments.getScheduleName(),
							_OpConCliArguments.getJobName(),
							_OpConCliArguments.getOpConSystem(),
							_OpConCliArguments.isRetrieveLogFiles()
							));
					completionCode = _IDependency.remoteDependency(opconApi, _OpConCliArguments);
					break;
				
				case ExpEval:
					
					if(_OpConCliArguments.getExpression() == null) {
						LOG.error(ExpressionEvalMissingExpressionMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(ExpressionEvalProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getExpression()
							));
					completionCode = _IExpression.expressionEvaluationRequest(opconApi, _OpConCliArguments);
					break;
	
				case GetJobLog:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(JobLogMissingScheduleNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getJobName() == null) {
						LOG.error(JobLogMissingJobNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(JobLogProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getTaskDate(),
							_OpConCliArguments.getScheduleName(),
							_OpConCliArguments.getJobName(),
							_OpConCliArguments.getJobLogDirectory()
							));
					List<JobLogData> jobLogsData = _IJob.getJobLog(opconApi, _OpConCliArguments);
					for(JobLogData jobLogData : jobLogsData) {
						if(!jobLogData.getRecords().isEmpty()) {
							if(_OpConCliArguments.getJobLogDirectory() != null) {  
								String filename = null;
								if(_OpConCliArguments.getJobLogFileName() != null) {
									filename = _OpConCliArguments.getJobLogDirectory() + File.separator + _OpConCliArguments.getJobLogFileName();
								} else {
									filename = _OpConCliArguments.getJobLogDirectory() + File.separator + jobLogData.getFilename();
								}
								LOG.info(MessageFormat.format(JobLogFileMsg, filename));
								FileWriter fwriter = new FileWriter(filename);
								BufferedWriter bwrite = new BufferedWriter(fwriter);
								// now add the shout entries
								for (String record : jobLogData.getRecords()) {
									StringBuffer sbWriteRecord = new StringBuffer();
									sbWriteRecord.append(record);
									sbWriteRecord.append(System.getProperty("line.separator"));
									bwrite.write(sbWriteRecord.toString());
								}
								bwrite.close();
							} else {
								// append joblog to OpConCli output
								for (String record : jobLogData.getRecords()) {
									LOG.info(record);
								}
							}
						}
					}
					completionCode = 0;
					break;

				case GetJobStatus:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(JobStatusMissingScheduleNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getJobName() == null) {
						LOG.error(JobStatusMissingJobNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(JobStatusProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getTaskDate(),
							_OpConCliArguments.getScheduleName(),
							_OpConCliArguments.getJobName()
							));
					
					DailyJob dailyJob = _IJob.getDailyJobByName(opconApi, _OpConCliArguments);
					LOG.info(MessageFormat.format(JobStatusMsg, _OpConCliArguments.getScheduleName(), _OpConCliArguments.getJobName(), dailyJob.getStatus().getDescription()));
					
					completionCode = dailyJob.getStatus().getId();
					break;

				case JobAction:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(JobActionMissingScheduleNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getJobName() == null) {
						LOG.error(JobActionMissingJobNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getJobAction() == null) {
						LOG.error(JobActionMissingJobActionMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(JobActionProcessingTaskMsg,_OpConCliArguments.getTask(),_OpConCliArguments.getTaskDate(),_OpConCliArguments.getScheduleName(),
							_OpConCliArguments.getJobName(),_OpConCliArguments.getJobAction()));
					completionCode = _IJob.jobActionRequest(opconApi, _OpConCliArguments);
					break;
			
				case JobAdd:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(JobAddMissingScheduleNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getJobName() == null) {
						LOG.error(JobAddMissingJobNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getFrequencyName() == null) {
						LOG.error(JobAddMissingFrequencyMsg);
						return 1;
					}
					if(_OpConCliArguments.getInstanceProperties() == null) {
						displayProperties = "None";
					} else {
						displayProperties = _OpConCliArguments.getInstanceProperties();
					}
					LOG.info(MessageFormat.format(JobAddProcessingTaskMsg, _OpConCliArguments.getTask(), _OpConCliArguments.getTaskDate(),
							_OpConCliArguments.getScheduleName(), _OpConCliArguments.getJobName(), _OpConCliArguments.getFrequencyName(),
							_OpConCliArguments.getInstanceProperties(), _OpConCliArguments.isAddOnHold()
							));
					completionCode = _IJob.jobAddRequest(opconApi, _OpConCliArguments);
					break;

				case MachAction:
					if(_OpConCliArguments.getMachineName() == null) {
						LOG.error(MachineActionMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getMachineAction() == null) {
						LOG.error(MachineActionMissingActionMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(MachineActionProcessingTaskMsg, _OpConCliArguments.getTask(), _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineAction()));
					completionCode = _IMachine.machineAction(opconApi, _OpConCliArguments);
					break;

				case MachAdd:
					if(_OpConCliArguments.getMachineFileName() == null) {
						LOG.error(MachineAddMissingFileNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					// get machine data
					List<Machine> machines = null;
					try {
						machines = getMachineData(_OpConCliArguments.getMachineFileName());
					} catch (JsonParseException pex) {
						LOG.error(MessageFormat.format(MachineAddJsonParseErrorMsg, pex.getMessage(), _OpConCliArguments.getMachineFileName()));
						return _OpConCliArguments.getErrorCode();
					} catch (Exception ex) {
						LOG.error(MessageFormat.format(MachineAddFileErrorMsg, ex.getMessage(), _OpConCliArguments.getMachineFileName()));
						return _OpConCliArguments.getErrorCode();
					}
					// if only 1 machine definition, check if we need to replace name, ip-address or dns values
					if(machines.size() == 1) {
						List<Machine> machinesUpdate = new ArrayList<Machine>();
						Machine machine = machines.get(0);
						if(_OpConCliArguments.getMachineName() != null) {
							machine.setName(_OpConCliArguments.getMachineName());
						}
						if(_OpConCliArguments.getMachineIpAddress() != null) {
							machine.setTcpIpAddress(_OpConCliArguments.getMachineIpAddress());
							machine.setFullyQualifiedDomainName("<Default>");
						}
						if(_OpConCliArguments.getMachineDnsAddress() != null) {
							machine.setTcpIpAddress("<Default>");
							machine.setFullyQualifiedDomainName(_OpConCliArguments.getMachineDnsAddress());
						}
						machinesUpdate.add(machine);
						machines = machinesUpdate;
					}
					LOG.info(MessageFormat.format(MachineAddProcessingTaskMsg, _OpConCliArguments.getTask(), _OpConCliArguments.getMachineFileName()));
					completionCode = _IMachine.machineAdd(opconApi, _OpConCliArguments, machines);
					break;
					
				case MachGrpAdd:
					if(_OpConCliArguments.getMachineGroupName() == null) {
						LOG.error(MachineGroupMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getMachineName() == null) {
						LOG.error(MachineGroupMissingMachineNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(MachineGroupProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getMachineGroupName(),
							_OpConCliArguments.getMachineName(),
							"add"
							));
					completionCode = _IMachineGroup.machineGroupAddRemoveMachine(opconApi, _OpConCliArguments, false);
					break;

				case MachGrpRemove:
					if(_OpConCliArguments.getMachineGroupName() == null) {
						LOG.error(MachineGroupMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getMachineName() == null) {
						LOG.error(MachineGroupMissingMachineNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(MachineGroupProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getMachineGroupName(),
							_OpConCliArguments.getMachineName(),
							"add"
							));
					completionCode = _IMachineGroup.machineGroupAddRemoveMachine(opconApi, _OpConCliArguments, true);
					break;

				case MachUpdate:
					if(_OpConCliArguments.getMachineName() == null) {
						LOG.error(MachineUpdateMissingNameMsg);
						return 1;
					}
					LOG.info(MessageFormat.format(MachineUpdateProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getMachineName(),
							_OpConCliArguments.getMachineNameUpdate(),
							_OpConCliArguments.getMachineIpAddressUpdate(),
							_OpConCliArguments.getMachineDnsAddressUpdate()
							));
					completionCode = _IMachine.machineUpdate(opconApi, _OpConCliArguments);
					break;

				case PropExp:
					if(_OpConCliArguments.getPropertyName() == null) {
						LOG.error(PropertyExpMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getPropertyValue() == null) {
						LOG.error(PropertyExpMissingValueMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(PropertyExpProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getPropertyName(),
							_OpConCliArguments.getPropertyValue()
							));
					completionCode = _IExpression.propertyExpressionRequest(opconApi, _OpConCliArguments);
					break;
	
				case PropUpdate:
					if(_OpConCliArguments.getPropertyName() == null) {
						LOG.error(PropertyUpdateMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getPropertyValue() == null) {
						LOG.error(PropertyUpdateMissingValueMsg);
						return _OpConCliArguments.getErrorCode();
					}

					LOG.info(MessageFormat.format(PropertyProcessingUpdateTaskMsg,
							_OpConCliArguments.getTask(), _OpConCliArguments.getPropertyName(), 
							_OpConCliArguments.getPropertyValue(), 
							_OpConCliArguments.isPropertyEncrypted()));
					completionCode = _IGlobalProperty.updateProperty(opconApi, _OpConCliArguments);
					break;

				case SchedAction:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(ScheduleActionMissingScheduleNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getScheduleAction() == null) {
						LOG.error(ScheduleActionMissingActionMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(ScheduleActionProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getTaskDate(),
							_OpConCliArguments.getScheduleName(),
							_OpConCliArguments.getScheduleAction()
							));
					completionCode = _ISchedule.actionSchedule(opconApi, _OpConCliArguments);
					break;
					
				case SchedBuild:
					if(_OpConCliArguments.getScheduleName() == null) {
						LOG.error(ScheduleBuildMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getInstanceProperties() == null) {
						displayProperties = "None";
					} else {
						displayProperties = _OpConCliArguments.getInstanceProperties();
					}
					LOG.info(MessageFormat.format(ScheduleBuildProcessingTaskMsg, _OpConCliArguments.getTask(), _OpConCliArguments.getTaskDate(), _OpConCliArguments.getScheduleName(), displayProperties, _OpConCliArguments.isBuildOnHold()));
					completionCode = _ISchedule.buildSchedule(opconApi, _OpConCliArguments);
					break;

				case SchedRebuild:
					if(_OpConCliArguments.getNoOfDaysToRebuild() == null) {
						LOG.error(ScheduleRebuildMissingDaysToBuildMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(ScheduleRebuildProcessingTaskMsg, 
							_OpConCliArguments.getTask(),
							_OpConCliArguments.getTaskDate(),
							String.valueOf(_OpConCliArguments.getNoOfDaysToRebuild()),
							_OpConCliArguments.getScheduleRebuildIndicator()
							));
					completionCode = _ISchedule.rebuildSchedule(opconApi, _OpConCliArguments);
					break;

				case ThreshUpdate:
					if(_OpConCliArguments.getThresholdName() == null) {
						LOG.error(ThresholdUpdateMissingNameMsg);
						return _OpConCliArguments.getErrorCode();
					}
					if(_OpConCliArguments.getThresholdValue() == null) {
						LOG.error(ThresholdUpdateMissingValueMsg);
						return _OpConCliArguments.getErrorCode();
					}
					LOG.info(MessageFormat.format(ThresholdProcessingUpdateTaskMsg, _OpConCliArguments.getTask(), _OpConCliArguments.getThresholdName(), _OpConCliArguments.getThresholdValue()));
					completionCode = _IThreshold.updateThreshold(opconApi, _OpConCliArguments);
					break;

				case Version:
					completionCode = _IVersion.getVersion(opconApi, _OpConCliArguments);
					break;

			}

		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return completionCode;
	}
	
	private OpconApi getClient(
			OpconApiProfile profile
			) throws Exception {
		
		OpconApi opconApi;
		Client client = null;
		ContextResolver<ObjectMapper> ctxObjectMapperProvider; 
		
		try {
			if(_CmdConfiguration.isDebug()) {
		        DefaultClientBuilder clientBuilder = DefaultClientBuilder.get()
		                .setTrustAllCert(true);
		        
		        client = clientBuilder.build();
				DefaultObjectMapperProvider objectMapperProvider = new DefaultObjectMapperProvider();
			    objectMapperProvider.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	            client.register(new WsLogger(objectMapperProvider));
		        
	            ctxObjectMapperProvider = objectMapperProvider;
			} else {
		        DefaultClientBuilder clientBuilder = DefaultClientBuilder.get()
		                .setTrustAllCert(true);
		        
		        client = clientBuilder.build();
		        DefaultObjectMapperProvider objectMapperProvider = new DefaultObjectMapperProvider();
			    objectMapperProvider.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	            ctxObjectMapperProvider = objectMapperProvider;
			}
            opconApi = new OpconApi(client, profile, new OpconApi.OpconApiListener() {

                @Override
                public void onFailed(WsException e) {
                    if (e.getResponse() == null) {
                        LOG.error("[OpconApi] A web service call has failed.", e);
                    } else if (e instanceof WsErrorException) {
                        LOG.warn("[OpconApi] A web service call return API Error: {}", e.getResponse().readEntity(String.class));
                    } else {
                        LOG.error("[OpconApi] A web service call has failed. Response: Header={} Body={}", e.getResponse().getHeaders(), e.getResponse().readEntity(String.class), e);
                    }
                }
            }, ctxObjectMapperProvider);
            if(_CmdConfiguration.getToken() != null) {
                if(!_CmdConfiguration.getToken().equals(ICmdConstants.EMPTY_STRING)) {
                	opconApi.login(_CmdConfiguration.getToken());
                } else {
        			opconApi.login(_CmdConfiguration.getUser(), _CmdConfiguration.getPassword());
                }
            } else {
    			opconApi.login(_CmdConfiguration.getUser(), _CmdConfiguration.getPassword());
            }
		} catch (KeyManagementException | NoSuchAlgorithmException | WsException e) {
		    throw new Exception(e);
		}
		return opconApi;
	}	// END : getClient

	private List<Machine> getMachineData(
			String fileName
			) throws JsonParseException, Exception{
		
		Machine[] machines = _DefaultObjectMapperProvider.getObjectMapper().readValue(new FileInputStream(fileName), Machine[].class );
		return Arrays.asList(machines);
		
	}
 
}
