package com.smatechnologies.opcon.command.api;

import java.io.File;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.impl.JobImpl;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IJob;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.command.api.ws.WsLogger;
import com.smatechnologies.opcon.restapiclient.DefaultClientBuilder;
import com.smatechnologies.opcon.restapiclient.WsException;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.OpconApiProfile;
import com.smatechnologies.opcon.restapiclient.api.dailyjobs.DailyJobsCriteria;
import com.smatechnologies.opcon.restapiclient.api.dailyjobs.WsDailyJobs;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.DailySchedulesCriteria;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.WsDailySchedules;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.properties.WsDailySchedulesProperties;
import com.smatechnologies.opcon.restapiclient.api.globalproperties.GlobalPropertiesCriteria;
import com.smatechnologies.opcon.restapiclient.api.globalproperties.WsGlobalProperties;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.DailySchedule;
import com.smatechnologies.opcon.restapiclient.model.GlobalProperty;
import com.smatechnologies.opcon.restapiclient.model.JobType;
import com.smatechnologies.opcon.restapiclient.model.Property;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.WindowsDailyJob;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob.Documentation;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.StatusPrinter;

public class TestGetScheduleInstanceProperty {
	
	private static final String UrlFormatTls = "https://{0}:{1}/api";
	private static final String JobId = "20200922|10|1|WIN001_TEST";
	private static DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final static Logger LOG = LoggerFactory.getLogger(TestGetScheduleInstanceProperty.class);
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	private static final IJob _IJob = new JobImpl();
	private static DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();
	private static Utilities _Utilities = new Utilities();
	
	private static final String OpConInstance = "OI.";
	private static final String ScheduleInstance = "SI.";
	private static final String JobInstance = "JI.";
	

	private void setLogger(
			boolean isDebug
			) throws Exception {
		

        //Debug mode
        if(isDebug) {
            System.setProperty(ICmdConstants.LogBackConstant.LEVEL_STDOUT_KEY, ICmdConstants.LogBackConstant.LEVEL_DEBUG_VALUE);
            System.setProperty(ICmdConstants.LogBackConstant.LEVEL_FILE_KEY, ICmdConstants.LogBackConstant.LEVEL_DEBUG_VALUE);
            System.setProperty(ICmdConstants.LogBackConstant.STDOUT_PATTERN_KEY, ICmdConstants.LogBackConstant.STDOUT_PATTERN_DEBUG_VALUE);
        } else {
            System.clearProperty(ICmdConstants.LogBackConstant.LEVEL_STDOUT_KEY);
            System.clearProperty(ICmdConstants.LogBackConstant.LEVEL_FILE_KEY);
            System.clearProperty(ICmdConstants.LogBackConstant.STDOUT_PATTERN_KEY);
        }

        //Restart logback
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        ContextInitializer contextInitializer = new ContextInitializer(loggerContext);
        URL url = contextInitializer.findURLOfDefaultConfigurationFile(true);

        JoranConfigurator joranConfigurator = new JoranConfigurator();
        joranConfigurator.setContext(loggerContext);
        loggerContext.reset();
        joranConfigurator.doConfigure(url);

        StatusPrinter.printIfErrorsOccured(loggerContext);
		
	}
	
	private OpconApi getClient(
			OpconApiProfile profile
			) throws Exception {
		
		Client client = null;
		OpconApi opconApi;
		ContextResolver<ObjectMapper> ctxObjectMapperProvider; 
				
		try {
	        DefaultClientBuilder clientBuilder = DefaultClientBuilder.get()
	                .setTrustAllCert(true);
	        
	        client = clientBuilder.build();
			DefaultObjectMapperProvider objectMapperProvider = new DefaultObjectMapperProvider();
		    objectMapperProvider.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            client.register(new WsLogger(objectMapperProvider));
	        
            ctxObjectMapperProvider = objectMapperProvider;
            opconApi = new OpconApi(client, profile, new OpconApi.OpconApiListener() {

                @Override
                public void onFailed(WsException e) {
                    if (e.getResponse() == null) {
                    	System.out.println(e.getMessage());
                    }
                }
            }, ctxObjectMapperProvider);
			
			opconApi.login(_CmdConfiguration.getUser(), _CmdConfiguration.getPassword());
			
		} catch (KeyManagementException | NoSuchAlgorithmException | WsException e) {
		    throw new Exception(e);
		}
		return opconApi;
	}	// END : getClient
	
	private List<Property> getDailyScheduleProperties(
			OpconApi opconApi,
			String scheduleId
			) throws Exception {

		List<Property> properties = null;
		
		WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
		WsDailySchedulesProperties wsDailySchedulesProperties = wsDailySchedules.properties(scheduleId);
		properties = wsDailySchedulesProperties.get();
		return properties;
	}	// END : getWindowsDailyJob

	private Property getDailySchedulePropertyByName(
			OpconApi opconApi,
			String scheduleId,
			String name
			) throws Exception {

		Property property = null;
		
		WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
		WsDailySchedulesProperties wsDailySchedulesProperties = wsDailySchedules.properties(scheduleId);
		property = wsDailySchedulesProperties.get(name);
		return property;
	}	// END : getWindowsDailyJob

	private Property getPropertyByName(
			OpconApi opconApi,
			String name
			) throws Exception {

		Property property = null;
		
		if(name.startsWith(OpConInstance)) {
			// Opcon Global Property
			name = name.replace(OpConInstance, ICmdConstants.EMPTY_STRING);
			LOG.info("Property name (" + name + ")");
			GlobalPropertiesCriteria criteria = new GlobalPropertiesCriteria();
			criteria.setName(name);
			WsGlobalProperties wsProperties = opconApi.globalProperties();
			List<GlobalProperty> properties = wsProperties.get(criteria);
			if(properties.size() > 0) {
				GlobalProperty gproperty = properties.get(0);
				property = new Property();
				property.setKey(gproperty.getName());
				property.setValue(gproperty.getValue());
			} else {
				LOG.error("Property name (" + name + ") not found");
			}
		} else if(name.startsWith(ScheduleInstance)) {
			// Schedule Instance Property
			name = name.replace(ScheduleInstance, ICmdConstants.EMPTY_STRING);
			LOG.info("Property name (" + name + ")");
			
			String[] definitions = _Utilities.tokenizeParameters(name, false, ICmdConstants.ESCAPED_DOT);
			LOG.info("Definitions (" + definitions.length + ")");
			if(definitions.length == 3) {
				Collection<LocalDate> ldates = new ArrayList<LocalDate>();
			    LocalDate ldate = LocalDate.parse(definitions[1], localDateFormatter);
			    ldates.add(ldate);
				DailySchedule dschedule = getDailySchedule(opconApi, ldates, definitions[2]);
				if(dschedule != null) {
					WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
					WsDailySchedulesProperties wsDailySchedulesProperties = wsDailySchedules.properties(dschedule.getId());
					property = wsDailySchedulesProperties.get(definitions[0]);
				} else {
					LOG.error("Schedule (" + definitions[1] + "." + definitions[2] + ") not found");
					
				}
			}
		} else if(name.startsWith(JobInstance)) {
			// Job Instance Property
			name = name.replace(JobInstance, ICmdConstants.EMPTY_STRING);
			LOG.info("Property name (" + name + ")");
			
			String[] definitions = _Utilities.tokenizeParameters(name, false, ICmdConstants.ESCAPED_DOT);
			LOG.info("Definitions (" + definitions.length + ")");
			if(definitions.length == 4) {
				Collection<LocalDate> ldates = new ArrayList<LocalDate>();
			    LocalDate ldate = LocalDate.parse(definitions[1], localDateFormatter);
			    ldates.add(ldate);
				DailySchedule dschedule = getDailySchedule(opconApi, ldates, definitions[2]);
				if(dschedule != null) {
					WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
					Collection<String> scheduleIds = new ArrayList<String>();
					scheduleIds.add(dschedule.getId());
					DailyJobsCriteria criteria = new DailyJobsCriteria();
				    criteria.setName(definitions[3]);
					criteria.setDates(ldates);
					criteria.setScheduleIds(scheduleIds);
					WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
					List<DailyJob> dailyJobs = wsDailyJobs.get(criteria);
					if(dailyJobs.size() > 0) {
						DailyJob dailyJob = dailyJobs.get(0);
						List<Property> jproperties = dailyJob.getProperties();
						for(Property jproperty : jproperties) {
							if(jproperty.getKey().equals(definitions[0])) {
								property = new Property();
								property.setKey(jproperty.getKey());
								property.setValue(jproperty.getValue());
							}
						}
					} else {
						LOG.error("Job (" + definitions[1] + "." + definitions[2] + "." + definitions[3] + ") not found");
					}
				} else {
					LOG.error("Schedule (" + definitions[1] + "." + definitions[2] + ") not found");
				}
			}
		} else {
			// Opcon Global Property
			name = name.replace(OpConInstance, ICmdConstants.EMPTY_STRING);
			LOG.info("Property name (" + name + ")");
			GlobalPropertiesCriteria criteria = new GlobalPropertiesCriteria();
			criteria.setName(name);
			WsGlobalProperties wsProperties = opconApi.globalProperties();
			List<GlobalProperty> properties = wsProperties.get(criteria);
			if(properties.size() > 0) {
				GlobalProperty gproperty = properties.get(0);
				property = new Property();
				property.setKey(gproperty.getName());
				property.setValue(gproperty.getValue());
			} else {
				LOG.error("Property name (" + name + ") not found");
			}
		}
		return property;
	}	// END : getPropertyBy Name	
	
		private DailySchedule getDailySchedule(
				OpconApi opconApi,
				Collection<LocalDate> ldates,
				String sname
				) throws Exception {
			
			DailySchedule dailySchedule = null;
			DailySchedulesCriteria criteria = new DailySchedulesCriteria();
			
		    criteria.setName(sname);
			criteria.setDates(ldates);
			WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
			List<DailySchedule> schedules = wsDailySchedules.get(criteria);
			if(schedules.size() > 0) {
				dailySchedule = schedules.get(0);
			} 
			return dailySchedule;
		}	// END : checkIfDailyScheduleExists
	

	private DailySchedule getDailySchedule(
			OpconApi opconApi,
			OpConCliArguments _CmdLineArguments
			) throws Exception {
		
		DailySchedule dailySchedule = null;
		DailySchedulesCriteria criteria = new DailySchedulesCriteria();
		
		Collection<LocalDate> ldates = new ArrayList<LocalDate>();
	    LocalDate dateTime = LocalDate.parse(_CmdLineArguments.getTaskDate(), localDateFormatter);
	    ldates.add(dateTime);
	    criteria.setName(_CmdLineArguments.getScheduleName());
		criteria.setDates(ldates);
		WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
		List<DailySchedule> schedules = wsDailySchedules.get(criteria);
		if(schedules.size() > 0) {
			dailySchedule = schedules.get(0);
		} 
		return dailySchedule;
	}	// END : checkIfDailyScheduleExists
	
	public static void main(String[] args) {
		TestGetScheduleInstanceProperty _TestGetScheduleInstanceProperty = new TestGetScheduleInstanceProperty();
		OpConCliArguments _CmdLineArguments = new OpConCliArguments();
		JCommander jcCmdLineArguments = null;
		DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();
		
		Utilities _Utilities = new Utilities();
		
		String workingDirectory = null;
		String configFileName = null;
		Preferences iniPrefs = null;
		Integer completionCode = null;
		
		try {
			// set supported TLS protocols 
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    		// get the arguments
			jcCmdLineArguments = JCommander.newBuilder()
					.addObject(_CmdLineArguments)
					.build();
			jcCmdLineArguments.parse(args);
			workingDirectory = System.getProperty(ICmdConstants.SYSTEM_USER_DIRECTORY);
			configFileName = workingDirectory + File.separator + ICmdConstants.CONFIG_FILE_NAME;
			// go get information from ini file
			// set general values
			iniPrefs = new IniPreferences(new Ini(new File(configFileName)));
			_CmdConfiguration = _Utilities.setConfigurationValues(iniPrefs, _CmdConfiguration, _CmdLineArguments.getOpConSystem());
			_TestGetScheduleInstanceProperty.setLogger(_CmdConfiguration.isDebug());
			String url = MessageFormat.format(UrlFormatTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
			OpconApiProfile profile = new OpconApiProfile(url);
			OpconApi opconApi = _TestGetScheduleInstanceProperty.getClient(profile);
			Property property = _TestGetScheduleInstanceProperty.getPropertyByName(opconApi, "JI.SUB_PATH.2020-10-29.IMS_MAIN.CONT001_ws001working");
			
			
			
			
			
			
			
//			DailySchedule schedule = _TestGetScheduleInstanceProperty.getDailySchedule(opconApi, _CmdLineArguments); 
//			LOG.info("schedule (" + _DefaultObjectMapperProvider.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(schedule) + ")");
//			List<Property> properties = _TestGetScheduleInstanceProperty.getDailyScheduleProperties(opconApi, schedule.getId());
//			for(Property property : properties) {
//				LOG.info("Property name (" + property.getKey() + ") value (" + property.getValue() + ")");
//			}
//			Property namedProperty = _TestGetScheduleInstanceProperty.getDailySchedulePropertyByName(opconApi, schedule.getId(), "MAIN_PATH");
			LOG.info("Named Property name (" + property.getKey() + ") value (" + property.getValue() + ")");
		} catch (com.beust.jcommander.ParameterException pe) {
			jcCmdLineArguments.usage();
			System.exit(1);
		} catch (Exception ex) {
			LOG.error(_Utilities.getExceptionDetails(ex));
			System.exit(1);
		}
		System.exit(0);
	} // END : main


}
