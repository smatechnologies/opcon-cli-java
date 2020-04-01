package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.interfaces.IToken;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.model.Token;

public class TokenImpl implements IToken {

	private static final String CreateApplicationTokenSuccessMsg =    "Application {0} Token {1} successfully created";
	private static final String CreateApplicationTokenFailedMsg =     "Application Token create failed : {0}";
	
	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayApplicationNameArgumentMsg =   "-ap  (application name)   : {0}";
	
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	private final static Logger LOG = LoggerFactory.getLogger(TokenImpl.class);

	public Integer createApplicationToken(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = null;
		
		Token token = opconApi.tokens().postApp(_CmdConfiguration.getUser(), _CmdConfiguration.getPassword(), _OpConCliArguments.getApplicationName());
		if(token.getId() != null) {
			LOG.info(MessageFormat.format(CreateApplicationTokenSuccessMsg, _OpConCliArguments.getApplicationName(), token.getId()));
			success = 0;
		} else {
			LOG.error(MessageFormat.format(CreateApplicationTokenFailedMsg, "error"));
			LOG.error(ArgumentsMsg);
			LOG.error(MessageFormat.format(DisplayApplicationNameArgumentMsg, _OpConCliArguments.getApplicationName()));
			success = 1;
		}
		return success;
	}


}
