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
import com.smatechnologies.opcon.command.api.impl.OpConCliImpl;
import com.smatechnologies.opcon.command.api.impl.JobImpl;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IOpConCli;
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
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.BatchUser;
import com.smatechnologies.opcon.restapiclient.model.DailySchedule;
import com.smatechnologies.opcon.restapiclient.model.JobType;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.WindowsDailyJob;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.details.WindowsDetails;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.details.commons.BasicFailureCriteria;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.details.commons.BasicFailureCriteria.Operator;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.StatusPrinter;

public class TestDailyJobUpdate {

	private static final String UrlFormatTls = "https://{0}:{1}/api";
	private static final String JobId = "20200301|133|1|JOB001";
	private static DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	
	private final static Logger LOG = LoggerFactory.getLogger(TestDailyJobUpdate.class);
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	private static final IJob _IJob = new JobImpl();

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
	
	private DailyJob getDailyJobById(
			OpconApi opconApi,
			String jobid
			) throws Exception {
		
		DailyJob dailyJob = null;

		WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
		dailyJob = wsDailyJobs.get(jobid);
		return dailyJob;
	}

	private WindowsDailyJob getWindowsDailyJob(
			OpconApi opconApi,
			OpConCliArguments _CmdLineArguments
			) throws Exception {

		WindowsDailyJob windowsDailyJob = null;
		
		// check if schedule exists in the daily
		DailySchedule dailySchedule = checkIfDailyScheduleExists(opconApi, _CmdLineArguments);
		if(dailySchedule != null) {
			Collection<String> scheduleIds = new ArrayList<String>();
			scheduleIds.add(dailySchedule.getId());
			Collection<LocalDate> ldates = new ArrayList<LocalDate>();
		    LocalDate dateTime = LocalDate.parse(_CmdLineArguments.getTaskDate(), localDateFormatter);
		    ldates.add(dateTime);
			DailyJobsCriteria criteria = new DailyJobsCriteria();
		    criteria.setName(_CmdLineArguments.getJobName());
			criteria.setDates(ldates);
			criteria.setScheduleIds(scheduleIds);
			criteria.setJobType(JobType.WINDOWS.getDescription());
			criteria.setIncludeDetails(true);
			WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
			List<WindowsDailyJob> dailyJobs = wsDailyJobs.get(criteria).stream()
					.filter(dailyJob -> dailyJob instanceof WindowsDailyJob)
					.map(dailyJob -> (WindowsDailyJob) dailyJob)
					.collect(Collectors.toList());
			if(dailyJobs.size() > 0) {
				windowsDailyJob = dailyJobs.get(0);
			}
		} else {
			LOG.error("Schedule not found in Daily");
			return null;
		}
		return windowsDailyJob;
	}	// END : getWindowsDailyJob

	private DailyJob putDailyJob(
			OpconApi opconApi,
			WindowsDailyJob dailyJob
			) throws Exception {
		
		WsDailyJobs wsDailyJobs = opconApi.dailyJobs();
		return wsDailyJobs.put(dailyJob);
	}

	private DailySchedule checkIfDailyScheduleExists(
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
		TestDailyJobUpdate _TestDailyJobUpdate = new TestDailyJobUpdate();
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
			_TestDailyJobUpdate.setLogger(_CmdConfiguration.isDebug());
			String url = MessageFormat.format(UrlFormatTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
			OpconApiProfile profile = new OpconApiProfile(url);
			OpconApi opconApi = _TestDailyJobUpdate.getClient(profile);
			WindowsDailyJob testJob = _TestDailyJobUpdate.getWindowsDailyJob(opconApi, _CmdLineArguments);
			testJob.getDetails().setCommandLine("genericp.exe -t 20");
			testJob.getDetails().setJobPriority(WindowsDetails.Priority.HIGH);
			List<BasicFailureCriteria> failures = new ArrayList<BasicFailureCriteria>();
			BasicFailureCriteria basicFailureCriteria = new BasicFailureCriteria();
			basicFailureCriteria.setOperator(Operator.NOT_EQUAL);
			basicFailureCriteria.setExitCode(0);
			failures.add(basicFailureCriteria);
			testJob.getDetails().setBasicFailureCriteria(failures);
			DailyJob updated = _TestDailyJobUpdate.putDailyJob(opconApi, testJob);
			// if date present check the format
			WindowsDailyJob testJobupdated = _TestDailyJobUpdate.getWindowsDailyJob(opconApi, _CmdLineArguments);
			completionCode = 0;
			
		} catch (com.beust.jcommander.ParameterException pe) {
			jcCmdLineArguments.usage();
			System.exit(1);
		} catch (Exception ex) {
			LOG.error(_Utilities.getExceptionDetails(ex));
			System.exit(1);
		}
		System.exit(completionCode);
	} // END : main

}
