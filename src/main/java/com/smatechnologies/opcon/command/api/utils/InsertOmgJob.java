package com.smatechnologies.opcon.command.api.utils;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.smatechnologies.opcon.command.api.EncryptValue;
import com.smatechnologies.opcon.command.api.arguments.EncryptValueArguments;
import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.util.Encryption;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.command.api.utils.modules.InsertOmgJobArguments;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.StatusPrinter;

public class InsertOmgJob {

	private static final String SeperatorLineMsg =                "---------------------------------------------------------------------";
	
	private static final String ProgramNameAndVersionMsg =        "InsertOmgJob          : Version {0}";
	private static final String DisplayDateArgumentMsg =          "-d  (date)            : {0}";
	private static final String DisplayScheduleNameArgumentMsg =  "-s  (schedule namee)  : {0}";
	private static final String DisplayJobNameArgumentMsg =       "-d  (job name)        : {0}";
	private static final String DisplayFileNameArgumentMsg =      "-f  (file name)       : {0}";
	private static final String DisplayFrequencyArgumentMsg =     "-fr (frequency name)  : {0}";
	private static final String DisplayOpConSystemArgumentMsg =   "-o  (OpCon system)    : {0}";
	
    private final static Logger LOG = LoggerFactory.getLogger(InsertOmgJob.class);
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	
	public static void main(String[] args) {

		InsertOmgJob _InsertOmgJob = new InsertOmgJob();
		InsertOmgJobImpl _InsertOmgJobImpl = new InsertOmgJobImpl();
		InsertOmgJobArguments _InsertOmgJobArguments = new InsertOmgJobArguments();
		JCommander jcInsertOmgJobArguments = null;
		Utilities _Utilities = new Utilities();
		
		String workingDirectory = null;
		String configFileName = null;
		Preferences iniPrefs = null;
		Integer completionCode = null;
		
		try {
			// set supported TLS protocols 
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    		// get the arguments
			jcInsertOmgJobArguments = JCommander.newBuilder()
					.addObject(_InsertOmgJobArguments)
					.build();
			jcInsertOmgJobArguments.parse(args);
			workingDirectory = System.getProperty(ICmdConstants.SYSTEM_USER_DIRECTORY);
			configFileName = workingDirectory + File.separator + ICmdConstants.CONFIG_FILE_NAME;
			// go get information from ini file
			// set general values
			iniPrefs = new IniPreferences(new Ini(new File(configFileName)));
			_CmdConfiguration = _Utilities.setConfigurationValues(iniPrefs, _CmdConfiguration, _InsertOmgJobArguments.getOpConSystem());
			_InsertOmgJob.setLogger(_CmdConfiguration.isDebug());
			LOG.info("working Directory {" + workingDirectory + "}");
			LOG.info("configFileName {" + configFileName + "}");
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(ProgramNameAndVersionMsg, ICmdConstants.SOFTWARE_VERSION));
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(DisplayDateArgumentMsg, _InsertOmgJobArguments.geteDate()));
			LOG.info(MessageFormat.format(DisplayScheduleNameArgumentMsg, _InsertOmgJobArguments.getScheduleName()));
			LOG.info(MessageFormat.format(DisplayJobNameArgumentMsg, _InsertOmgJobArguments.getJobName()));
			LOG.info(MessageFormat.format(DisplayFrequencyArgumentMsg, _InsertOmgJobArguments.getFrequency()));
			LOG.info(MessageFormat.format(DisplayFileNameArgumentMsg, _InsertOmgJobArguments.getFileName()));
			LOG.info(MessageFormat.format(DisplayOpConSystemArgumentMsg, _InsertOmgJobArguments.getOpConSystem()));
			LOG.info(SeperatorLineMsg);
			_InsertOmgJobImpl.processRequest(_InsertOmgJobArguments);
			LOG.info(SeperatorLineMsg);
		} catch (com.beust.jcommander.ParameterException pe) {
			jcInsertOmgJobArguments.usage();
			System.exit(1);
		} catch (Exception ex) {
			LOG.error(_Utilities.getExceptionDetails(ex));
			ex.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	} // END : main

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

}
