package com.smatechnologies.opcon.command.api.interfaces;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;

public interface IOpConCli {

	public Integer processRequest(OpConCliArguments _OpConCliArguments) throws Exception;
	
}
