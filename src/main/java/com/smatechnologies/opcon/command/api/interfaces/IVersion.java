package com.smatechnologies.opcon.command.api.interfaces;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;

public interface IVersion {
	
	public Integer getVersion(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;

}
