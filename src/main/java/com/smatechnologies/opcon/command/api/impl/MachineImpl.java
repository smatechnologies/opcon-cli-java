package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.enums.MachineActions;
import com.smatechnologies.opcon.command.api.enums.TaskType;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IMachine;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.machines.MachinesCriteria;
import com.smatechnologies.opcon.restapiclient.api.machines.WsMachines;
import com.smatechnologies.opcon.restapiclient.model.MachineAction;
import com.smatechnologies.opcon.restapiclient.model.Result;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

public class MachineImpl implements IMachine {

	private static final String InvalidOpConAPI1710VersionMsg = "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String MachineUpdateSuccessMsg =       "Machine ({0}) update successful";
	private static final String MachineUpdateFailedMsg =        "Machine ({0}) update failed : {1}";
	private static final String MachineActionMsg =        		"Action processing for Machine ({0}) started";
	private static final String MachineWLimitedMsg =        	"Waiting for all current jobs to finish on machines ({0})";
	private static final String MachineNotFoundMsg =        	"Machine ({0}) not found in OpCon system";
	private static final String MachineGetMsg =        			"Get Machine ({0})";
	private static final String MachineAddMsg =        			"Add Machine ({0})";
	private static final String MachineAddSuccessMsg =  		"Add Machine ({0}) success";
	private static final String MachineAddFailedMsg =  			"Add Machine ({0}) failed";

	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayMachineNameArgumentMsg =       "-mn  (machine names)       : {0}";
	private static final String DisplayMachineActionArgumentMsg =     "-ma  (machine action)      : {0}";
	private static final String DisplayMachineFileArgumentMsg =       "-mf  (machine file)        : {0}";
	private static final String DisplayMachineIpAddressArgumentMsg =  "-mi  (machine ip address)  : {0}";
	private static final String DisplayMachineDnsAddressArgumentMsg = "-md  (machine dns address) : {0}";
	private static final String DisplayMachineNameUpdateArgumentMsg = "-mnu (new machine name)    : {0}";
	private static final String DisplayMachineIpUpdateArgumentMsg =   "-miu (new ip address)      : {0}";
	private static final String DisplayMachineDnsUpdateArgumentMsg =  "-mdu (new dns address)     : {0}";

	
	private final static Logger LOG = LoggerFactory.getLogger(MachineImpl.class);
	private Utilities _Utilities = new Utilities();
	
	private ScheduledExecutorService executorWaitForJobsToFinish = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> futureWaitForJobsToFinish = null;
	
	private Hashtable<String, Machine> htblMachines = new Hashtable<String, Machine>();
	private List<String> machineList = new ArrayList<String>();
	private boolean machineJobsFinished = false;
	
	public Integer machineUpdate(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			MachinesCriteria criteria = new MachinesCriteria();
			criteria.setName(_OpConCliArguments.getMachineName());
			criteria.setExtendedProperties(true);
			WsMachines wsMachines = opconApi.machines();
			List<Machine> machines = wsMachines.get(criteria);
			if(machines.size() > 0) {
				Machine machine = machines.get(0);
				if(_OpConCliArguments.getMachineNameUpdate() != null) {
					machine.setName(_OpConCliArguments.getMachineNameUpdate());
				}
				if(_OpConCliArguments.getMachineIpAddressUpdate() != null) {
					machine.setTcpIpAddress(_OpConCliArguments.getMachineIpAddressUpdate());
				}
				if(_OpConCliArguments.getMachineDnsAddressUpdate() != null) {
					machine.setFullyQualifiedDomainName(_OpConCliArguments.getMachineDnsAddressUpdate());
				}
				Machine retMachine = opconApi.machines().put(machine);
				if(retMachine.getId() != null) {
					LOG.info(MessageFormat.format(MachineUpdateSuccessMsg, _OpConCliArguments.getMachineName()));
					success = 0;
				} else {
					LOG.error(MessageFormat.format(MachineUpdateFailedMsg, _OpConCliArguments.getMachineName()));
					LOG.error(ArgumentsMsg);
					LOG.error(MessageFormat.format(DisplayMachineNameArgumentMsg, _OpConCliArguments.getMachineName()));
					LOG.error(MessageFormat.format(DisplayMachineNameUpdateArgumentMsg, _OpConCliArguments.getMachineNameUpdate()));
					LOG.error(MessageFormat.format(DisplayMachineIpUpdateArgumentMsg, _OpConCliArguments.getMachineIpAddressUpdate()));
					LOG.error(MessageFormat.format(DisplayMachineDnsUpdateArgumentMsg, _OpConCliArguments.getMachineDnsAddressUpdate()));
					success = 1;
				}
			} else {
				LOG.error(MessageFormat.format(MachineNotFoundMsg, _OpConCliArguments.getMachineName()));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	public Integer machineUpdateUsingMachineObject(
			OpconApi opconApi,
			Machine machine
			) throws Exception {
		
		Integer success = 1;
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), TaskType.MachUpdate.name());
		if(versionOK) {
			Machine retMachine = opconApi.machines().put(machine);
			if(retMachine.getId() != null) {
				LOG.info(MessageFormat.format(MachineUpdateSuccessMsg, machine.getName()));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(MachineUpdateFailedMsg, machine.getName()));
				LOG.error(ArgumentsMsg);
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}	// END : machineUpdate - Machine object

	public Integer machineAction(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			htblMachines.clear();
			machineList.clear();
			WsMachines wsMachines = opconApi.machines();
			String[] machinesToChange = _Utilities.tokenizeParameters(_OpConCliArguments.getMachineName(), false, ICmdConstants.COMMA);
			for(int iMachCntr = 0; iMachCntr < machinesToChange.length; iMachCntr++) {
				machineList.add(machinesToChange[iMachCntr]);
				MachinesCriteria criteria = new MachinesCriteria();
				criteria.setName(machinesToChange[iMachCntr]);
				List<Machine> machines = wsMachines.get(criteria);
				if(machines.size() > 0) {
					Machine machine = machines.get(0);
					htblMachines.put(machine.getName(), machine);
				}					
			}
			Set<String> machineKeys = htblMachines.keySet();
			List<MachineAction.MachineActionMachine> machineActions = new ArrayList<MachineAction.MachineActionMachine>();
			for(String machineKey : machineKeys) {
				Machine machine = htblMachines.get(machineKey);
				LOG.info(MessageFormat.format(MachineActionMsg, machine.getName()));
				if(machine != null) {
					MachineAction.MachineActionMachine machineActionMachine = new MachineAction.MachineActionMachine();
					machineActionMachine.setId(machine.getId());
					machineActions.add(machineActionMachine);
				}
			}
			MachineAction machineAction = new MachineAction();
			machineAction.setMachines(machineActions);
			machineAction.setAction(getMachineAction(_OpConCliArguments.getMachineAction()));
			MachineAction retAction = opconApi.machineActions().post(machineAction);
			List<MachineAction.MachineActionMachine> retMachineActions = retAction.getMachines();
			for(MachineAction.MachineActionMachine retMachineAction : retMachineActions) {
				success = 0;
				if(retMachineAction.getResult() == Result.FAILED) {
					// submitted or success are good values
					LOG.error(ArgumentsMsg);
					LOG.error(MessageFormat.format(DisplayMachineNameArgumentMsg, _OpConCliArguments.getMachineName()));
					LOG.error(MessageFormat.format(DisplayMachineActionArgumentMsg, _OpConCliArguments.getMachineAction()));
					success = 1;
				} else {
					if(_OpConCliArguments.getMachineAction().equalsIgnoreCase("wlimited")) {
						LOG.info(MessageFormat.format(MachineWLimitedMsg, _OpConCliArguments.getMachineName()));
						checkIfMachineJobsFinished(opconApi);
					}
					success = 0;
				}
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}	// END : machineAction

	public Machine getMachine(
			OpconApi opconApi, 
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Machine machine = null;
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			LOG.info(MessageFormat.format(MachineGetMsg, _OpConCliArguments.getMachineName()));
			MachinesCriteria criteria = new MachinesCriteria();
			criteria.setName(_OpConCliArguments.getMachineName());
			criteria.setExtendedProperties(true);
			WsMachines wsMachines = opconApi.machines();
			List<Machine> machines = wsMachines.get(criteria);
			if(machines.size() > 0) {
				machine = machines.get(0);
			} else {
				LOG.error(MessageFormat.format(MachineNotFoundMsg, _OpConCliArguments.getMachineName()));
			}
		} else {
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return machine;
	}	// END : getMachine

	public Integer machineAdd(
			OpconApi opconApi, 
			OpConCliArguments  _OpConCliArguments, 
			List<Machine> machines
			) throws Exception {
		
		Integer success = 1;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			for(Machine machine : machines) {
				LOG.info(MessageFormat.format(MachineAddMsg, machine.getName()));
				WsMachines wsMachines = opconApi.machines();
				Machine newMachine = wsMachines.post(machine);
				if(newMachine.getId() == null) {
					LOG.error(MessageFormat.format(MachineAddFailedMsg, machine.getName()));
					LOG.error(ArgumentsMsg);
					LOG.error(MessageFormat.format(DisplayMachineFileArgumentMsg, _OpConCliArguments.getMachineFileName()));
					LOG.error(MessageFormat.format(DisplayMachineNameArgumentMsg, _OpConCliArguments.getMachineName()));
					LOG.error(MessageFormat.format(DisplayMachineIpAddressArgumentMsg, _OpConCliArguments.getMachineIpAddress()));
					LOG.error(MessageFormat.format(DisplayMachineDnsAddressArgumentMsg, _OpConCliArguments.getMachineDnsAddress()));
					success = 1;
					break;
				} else {
					LOG.error(MessageFormat.format(MachineAddSuccessMsg, machine.getName()));
					success = 0;
				}
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}	// END : machineAdd
	
	private void checkIfMachineJobsFinished(
			OpconApi opconApi 
			) throws Exception {
		
		try {
		    final Runnable checkIfMachineJobsFinishedRoutine = new Runnable() {
		         public void run() { 
		        	 try {
						getMachineJobStatus(opconApi);
					} catch (Exception ex) {
						try {
							LOG.error(_Utilities.getExceptionDetails(ex));
						} catch (Exception e) {
						}
					} 
		         }
		       };
		    futureWaitForJobsToFinish = executorWaitForJobsToFinish.scheduleWithFixedDelay(checkIfMachineJobsFinishedRoutine, 5, 3, TimeUnit.SECONDS);				
			machineJobsFinished = false;
			waitForJobsToFinish();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} // END : checkIfCurrentJobsFinished
	
	
	private void getMachineJobStatus(
			OpconApi opconApi 
			) throws Exception {
		
		boolean allJobsFinished = false;
		
		try {
			WsMachines wsMachines = opconApi.machines();
			Set<String> machineKeys = htblMachines.keySet();
			allJobsFinished = true;
			if(!machineKeys.isEmpty()) {
				for(String machineKey : machineKeys) {
					Machine machine = htblMachines.get(machineKey);
					if(machine != null) {
						if(machine.getCurrentJobs() > 0) {
							// get machine details, check current jobs, if non zero reset
							MachinesCriteria criteria = new MachinesCriteria();
							List<Integer> machineIds = new ArrayList<Integer>();
							machineIds.add(machine.getId());
							criteria.setIds(machineIds);
							List<Machine> machines = wsMachines.get(criteria);
							if(!machines.isEmpty()) {
								Machine machineCheck = machines.get(0);
								if(machineCheck.getCurrentJobs() > 0) {
									htblMachines.put(machineKey, machineCheck);
									allJobsFinished = false;
								}
							}
						}
					}
				}
			}
			if(allJobsFinished) {
				jobsFinished();
				futureWaitForJobsToFinish.cancel(true);
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}	// END : getMachineJobStatus

	private synchronized void waitForJobsToFinish() throws Exception {
		
		try {
			while(!machineJobsFinished) {
				wait();
			}
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END : waitForCurrentJobsToFinish

	private synchronized void jobsFinished() throws Exception {
		
		try {
			machineJobsFinished = true;
			notify();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	} 	// END :jobsFinished


	private MachineAction.Action getMachineAction(
			String actionType
			) throws Exception {
		
		MachineAction.Action action = null;

		MachineActions type = MachineActions.valueOf(actionType);
		
		switch (type) {
		
			case up:
				action = MachineAction.Action.ENABLE_FULL;
				break;
				
			case down:
				action = MachineAction.Action.DISABLE;
				break;
				
			case limited:
				action = MachineAction.Action.ENABLE_LIMITED;
				break;
				
			case wlimited:
				action = MachineAction.Action.ENABLE_LIMITED;
				break;
		
		}
		return action;
	}	// END : getMachineAction

	
}
