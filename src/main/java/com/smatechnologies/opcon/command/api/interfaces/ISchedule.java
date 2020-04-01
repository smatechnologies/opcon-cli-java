package com.smatechnologies.opcon.command.api.interfaces;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;

public interface ISchedule {

	public Integer buildSchedule(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public Integer rebuildSchedule(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public Integer actionSchedule(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	
}
