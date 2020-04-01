package com.smatechnologies.opcon.command.api.interfaces;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;

public interface IMachineGroup {

	public Integer machineGroupAddRemoveMachine(OpconApi opconApi, OpConCliArguments _OpConCliArguments, boolean remove) throws Exception;

}
