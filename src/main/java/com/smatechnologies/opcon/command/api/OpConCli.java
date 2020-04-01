package com.smatechnologies.opcon.command.api;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.impl.OpConCliImpl;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IOpConCli;
import com.smatechnologies.opcon.command.api.util.Utilities;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.StatusPrinter;

public class OpConCli {
	
	private static final String SeperatorLineMsg =                    "---------------------------------------------------------------------";
	private static final String InvalidDateFormatMsg =                "Invalid date format format ({0}) - format must be yyyy-MM-dd";
	
	private static final String ProgramNameAndVersionMsg =            "OpConCli                  : Version {0}";
	private static final String DisplayOpConSystemArgumentMsg =       "-o   (OpCon System)       : {0}";
	
	private static final String CompletedProcessingMsg = "Request Completed with Code ({0})";
	
    private final static Logger LOG = LoggerFactory.getLogger(OpConCli.class);
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	

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
	
	public static void main(String[] args) {
		OpConCli _OpConCli = new OpConCli();
		IOpConCli _IOpConCli = new OpConCliImpl(); 
		OpConCliArguments _OpConCliArguments = new OpConCliArguments();
		JCommander jcCmdLineArguments = null;
		
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
					.addObject(_OpConCliArguments)
					.build();
			jcCmdLineArguments.parse(args);
			workingDirectory = System.getProperty(ICmdConstants.SYSTEM_USER_DIRECTORY);
			configFileName = workingDirectory + File.separator + ICmdConstants.CONFIG_FILE_NAME;
			// go get information from ini file
			// set general values
			iniPrefs = new IniPreferences(new Ini(new File(configFileName)));
			_CmdConfiguration = _Utilities.setConfigurationValues(iniPrefs, _CmdConfiguration, _OpConCliArguments.getOpConSystem());
			_OpConCli.setLogger(_CmdConfiguration.isDebug());
			LOG.info("working Directory {" + workingDirectory + "}");
			LOG.info("configFileName {" + configFileName + "}");
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(ProgramNameAndVersionMsg, ICmdConstants.SOFTWARE_VERSION));
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(DisplayOpConSystemArgumentMsg, _OpConCliArguments.getOpConSystem()));
			LOG.info(SeperatorLineMsg);
			
			// if date present check the format
			if(_OpConCliArguments.getTaskDate() != null) {
				if(!_Utilities.checkDateFormat(_OpConCliArguments.getTaskDate())) {
					LOG.error(MessageFormat.format(InvalidDateFormatMsg, _OpConCliArguments.getTaskDate()));
					System.exit(1);
				}
			} else {
				// use current date
				_OpConCliArguments.setTaskDate(_Utilities.getCurrentDate());
			}
			completionCode = _IOpConCli.processRequest(_OpConCliArguments);
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(CompletedProcessingMsg, String.valueOf(completionCode)));
			LOG.info(SeperatorLineMsg);
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
