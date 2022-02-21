package com.smatechnologies.opcon.command.api.interfaces;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;

public interface IOpConCli {

	public OpconApi getOpConApi() throws Exception;
	public Integer processRequest(OpConCliArguments _OpConCliArguments) throws Exception;
	
}
