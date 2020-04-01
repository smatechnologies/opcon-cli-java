package com.smatechnologies.opcon.command.api.interfaces;

import java.util.List;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

public interface IMachine {
	
	public Machine getMachine(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public Integer machineUpdate(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public Integer machineUpdateUsingMachineObject(OpconApi opconApi, Machine machine) throws Exception;
	public Integer machineAction(OpconApi opconApi, OpConCliArguments _OpConCliArguments) throws Exception;
	public Integer machineAdd(OpconApi opconApi, OpConCliArguments  _OpConCliArguments, List<Machine> machines) throws Exception;

}
