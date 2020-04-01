package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.IMachine;
import com.smatechnologies.opcon.command.api.interfaces.IMachineGroup;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.machinegroups.MachineGroupsCriteria;
import com.smatechnologies.opcon.restapiclient.api.machinegroups.WsMachineGroups;
import com.smatechnologies.opcon.restapiclient.model.MachineGroup;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

public class MachineGroupImpl implements IMachineGroup {

	private static final String InvalidOpConAPI1710VersionMsg =       "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String MachineGroupAddMsg =                  "Adding machine {0} to machine group {1}";
	private static final String MachineGroupAddSuccessMsg =           "Machine {0} added to machine group {1}";
	private static final String MachineGroupRemoveMsg =               "Removing machine {0} from machine group {1}";
	private static final String MachineGroupRemoveSuccessMsg =        "Machine {0} removed from machine group {1}";
	private static final String MachineGroupMachineNotInGroupMsg =    "Machine {0} not in machine group {1}";
	private static final String MachineGroupMachineInGroupMsg =       "Machine {0} already in machine group {1}";
	private static final String MachineGroupMachineNotFoundMsg =      "Machine ({0}) not found in OpCon system";
	private static final String MachineGroupNotFoundMsg =      	      "MachineGroup ({0}) not found in OpCon system";

	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayMachineGroupNameArgumentMsg =  "-mg  (machine group)     : {0}";
	private static final String DisplayMachineNameArgumentMsg =       "-mn  (machine names)      : {0}";

	
	private final static Logger LOG = LoggerFactory.getLogger(MachineGroupImpl.class);
	private Utilities _Utilities = new Utilities();
	private IMachine _IMachine = new MachineImpl();
	
	
	public Integer machineGroupAddRemoveMachine(
			OpconApi opconApi, 
			OpConCliArguments _OpConCliArguments,
			boolean remove
			) throws Exception {

		Integer success = 1;
		boolean partOfGroup = false;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			Machine machine = _IMachine.getMachine(opconApi, _OpConCliArguments);
			if(machine != null) {
				for(MachineGroup group : machine.getGroups()) {
					if(group.getName().equalsIgnoreCase(_OpConCliArguments.getMachineGroupName())) {
						partOfGroup = true;
					}
				}
				if(remove) {
					LOG.info(MessageFormat.format(MachineGroupRemoveMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
					if(partOfGroup) {
						List<MachineGroup> updatedRemoveList = new ArrayList<MachineGroup>();
						for(MachineGroup machineGroup : machine.getGroups()) {
							if(!machineGroup.getName().equalsIgnoreCase(_OpConCliArguments.getMachineGroupName())) {
								updatedRemoveList.add(machineGroup);
							}
						}
						machine.setGroups(updatedRemoveList);
					} else {
						LOG.info(MessageFormat.format(MachineGroupMachineNotInGroupMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
						return 0;
					}
				} else {
					LOG.info(MessageFormat.format(MachineGroupAddMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
					if(!partOfGroup) {
						// get the group information
						MachineGroup machineGroup = getMachineGroup(opconApi, _OpConCliArguments.getMachineGroupName());
						if(machineGroup != null) {
							List<MachineGroup> updatedAddList = machine.getGroups();
							updatedAddList.add(machineGroup);
							machine.setGroups(updatedAddList);
						} else {
							LOG.info(MessageFormat.format(MachineGroupNotFoundMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
							LOG.error(ArgumentsMsg);
							LOG.error(MessageFormat.format(DisplayMachineGroupNameArgumentMsg, _OpConCliArguments.getMachineGroupName()));
							LOG.error(MessageFormat.format(DisplayMachineNameArgumentMsg, _OpConCliArguments.getMachineName()));
							return 1;
						}
					} else {
						LOG.info(MessageFormat.format(MachineGroupMachineInGroupMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
						return 0;
					}
				}
				success = _IMachine.machineUpdateUsingMachineObject(opconApi, machine);
				if(success == 0) {
					if(remove) {
						LOG.info(MessageFormat.format(MachineGroupRemoveSuccessMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
					} else {
						LOG.info(MessageFormat.format(MachineGroupAddSuccessMsg, _OpConCliArguments.getMachineName(), _OpConCliArguments.getMachineGroupName()));
					}
				}
			} else {
				success = 1;
				LOG.error(MessageFormat.format( MachineGroupMachineNotFoundMsg, _OpConCliArguments.getMachineName()));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayMachineGroupNameArgumentMsg, _OpConCliArguments.getMachineGroupName()));
				LOG.error(MessageFormat.format(DisplayMachineNameArgumentMsg, _OpConCliArguments.getMachineName()));
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	private MachineGroup getMachineGroup(
			OpconApi opconApi, 
			String name
			) throws Exception {
		
		MachineGroup machineGroup = null;
		
		MachineGroupsCriteria criteria = new MachineGroupsCriteria();
		criteria.setName(name);
		WsMachineGroups wsMachineGroups = opconApi.machineGroups();
		List<MachineGroup> machineGroups = wsMachineGroups.get(criteria);
		if(machineGroups.size() > 0) {
			machineGroup = machineGroups.get(0);
		}
		return machineGroup;
	}

}
