package com.smatechnologies.opcon.command.api;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.util.Encryption;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.beust.jcommander.JCommander;
import com.smatechnologies.opcon.command.api.arguments.EncryptValueArguments;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;

public class EncryptValue {

	private static final String SeperatorLineMsg =                "---------------------------------------------------------------------";
	
	private static final String ProgramNameAndVersionMsg =        "EncryptValue          : Version {0}";
	private static final String DisplayValueArgumentMsg =         "-v  (value)           : {0}";
	private static final String EncryptedMsg =                    "ev : {0}";
	
    private final static Logger LOG = LoggerFactory.getLogger(EncryptValue.class);
	
	public static void main(String[] args) {

		Encryption _Encryption = new Encryption();
		EncryptValueArguments _EncryptValueArguments = new EncryptValueArguments();
		JCommander jcEncryptValueArguments = null;
		Utilities _Utilities = new Utilities();
		
		try {
    		// get the arguments
			jcEncryptValueArguments = JCommander.newBuilder()
					.addObject(_EncryptValueArguments)
					.build();
			jcEncryptValueArguments.parse(args);
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(ProgramNameAndVersionMsg, ICmdConstants.SOFTWARE_VERSION));
			LOG.info(SeperatorLineMsg);
			LOG.info(MessageFormat.format(DisplayValueArgumentMsg, _EncryptValueArguments.getValue()));
			LOG.info(SeperatorLineMsg);
			byte[] encoded =  _Encryption.encode64(_EncryptValueArguments.getValue());
			String hexEncoded = _Encryption.encodeHexString(encoded);
			LOG.info(MessageFormat.format(EncryptedMsg, hexEncoded));
			LOG.info(SeperatorLineMsg);
		} catch (com.beust.jcommander.ParameterException pe) {
			jcEncryptValueArguments.usage();
			System.exit(1);
		} catch (Exception ex) {
			LOG.error(_Utilities.getExceptionDetails(ex));
			ex.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	} // END : main

}
