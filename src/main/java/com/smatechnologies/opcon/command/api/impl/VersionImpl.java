package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.IVersion;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.model.Version;

public class VersionImpl implements IVersion {
	
	private static final String OpConAPIVersionMsg =       "OpCon-API Version {0}";

	private final static Logger LOG = LoggerFactory.getLogger(VersionImpl.class);


	public Integer getVersion(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		
		Version version = opconApi.getVersion();
		LOG.info(MessageFormat.format(OpConAPIVersionMsg, version.getOpConRestApiProductVersion()));
		success = 0;
		return success;
	}

}
