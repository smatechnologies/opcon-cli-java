package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.enums.ScheduleActions;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.ISchedule;
import com.smatechnologies.opcon.command.api.modules.UtilDateFormat;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.DailySchedulesCriteria;
import com.smatechnologies.opcon.restapiclient.api.dailyschedules.WsDailySchedules;
import com.smatechnologies.opcon.restapiclient.api.scheduleactions.WsScheduleActions;
import com.smatechnologies.opcon.restapiclient.api.schedulebuilds.WsScheduleBuilds;
import com.smatechnologies.opcon.restapiclient.model.DailySchedule;
import com.smatechnologies.opcon.restapiclient.model.Result;
import com.smatechnologies.opcon.restapiclient.model.ScheduleAction;
import com.smatechnologies.opcon.restapiclient.model.ScheduleBuild;
import com.smatechnologies.opcon.restapiclient.model.Version;

public class ScheduleImpl implements ISchedule {

	
	private static final String InvalidOpConAPI1831VersionMsg = "OpCon-API Version {0} not supported, must be 18.3.1 or greater";
	private static final String ScheduleBuildSuccessMsg =       "Build for schedule(s) ({0}) on date ({1}) successful";
	private static final String ScheduleBuildFailedMsg =        "Build for schedule(s) ({0}) on date ({1}) failed : {2}";
	private static final String ScheduleRebuildSuccessMsg =     "Rebuild for schedule(s) ({0}) on date ({1}) successful";
	private static final String ScheduleRebuildFailedMsg =      "Rebuild for schedule(s) ({0}) on date ({1}) failed : {2}";
	private static final String ScheduleActionSuccessMsg =      "Action ({0}) for schedule(s) ({1}) on date ({2}) successful";
	private static final String ScheduleActionFailedMsg =       "Action ({0}) for schedule(s) ({1}) on date ({2}) failed : {3}";

	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayTaskDateArgumentMsg =          "-d   (date)               : {0}";
	private static final String DisplayScheduleNameArgumentMsg =      "-sn  (schedule name)      : {0}";
	private static final String DisplayPropertiesArgumentMsg =        "-ip  (properties)         : {0}";
	private static final String DisplayBuildOnHoldArgumentMsg =       "-soh (build on hold)      : {0}";
	private static final String DisplayScheduleActionArgumentMsg =    "-sa  (schedule action)    : {0}";
	private static final String DisplayRebuildNoDaysArgumentMsg =     "-sd  (rebuild days)       : {0}";
	private static final String DisplayRebuildIndicatorArgumentMsg =  "-sri (rebuild indicator)  : {0}";

	DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private final static Logger LOG = LoggerFactory.getLogger(ScheduleImpl.class);
	private Utilities _Utilities = new Utilities();

	public Integer buildSchedule(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			WsScheduleBuilds schedulebuilds = opconApi.scheduleBuilds();
			List<ScheduleBuild.Schedule> schedules = new ArrayList<ScheduleBuild.Schedule>();
			List<ScheduleBuild.InstanceProperty> instanceProperties = new ArrayList<ScheduleBuild.InstanceProperty>();
			List<String> buildDates = new ArrayList<String>();
			
			if(_OpConCliArguments.getInstanceProperties() != null) {
				instanceProperties = getInstancePropertyList(_OpConCliArguments.getInstanceProperties());
				
			}
			ScheduleBuild build = new ScheduleBuild();
			ScheduleBuild.Schedule schedule = new ScheduleBuild.Schedule();
			schedule.setName(_OpConCliArguments.getScheduleName());
			schedules.add(schedule);
			build.setSchedules(schedules);
			buildDates.add(_OpConCliArguments.getTaskDate());
			build.setDates(buildDates);
			build.setHold(_OpConCliArguments.isBuildOnHold());
			build.setOverwrite(true);
			build.setProperties(instanceProperties);
			
			ScheduleBuild buildresponse = schedulebuilds.post(build);
			String snames = getScheduleNamesForDisplay(schedules);
			if(buildresponse.getId() != null) {
				LOG.info(MessageFormat.format(ScheduleBuildSuccessMsg, snames, _OpConCliArguments.getTaskDate()));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(ScheduleBuildFailedMsg, snames, _OpConCliArguments.getTaskDate(), ""));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
				LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, snames));
				LOG.error(MessageFormat.format(DisplayPropertiesArgumentMsg, _OpConCliArguments.getInstanceProperties()));
				LOG.error(MessageFormat.format(DisplayBuildOnHoldArgumentMsg, _OpConCliArguments.isBuildOnHold()));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1831VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	public Integer rebuildSchedule(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			// calculate date list
			List<String> rebuildDates = getRebuildDates(_OpConCliArguments.getTaskDate(), _OpConCliArguments.getNoOfDaysToRebuild());
			for(String rebuildDate : rebuildDates) {
				List<DailySchedule> dailySchedules = getRebuildSchedulesByDate(opconApi, rebuildDate, _OpConCliArguments.getScheduleRebuildIndicator());

				if(!dailySchedules.isEmpty()) {
					ScheduleBuild build = new ScheduleBuild();
					WsScheduleBuilds schedulebuilds = opconApi.scheduleBuilds();
					List<ScheduleBuild.Schedule> schedules = new ArrayList<ScheduleBuild.Schedule>();
					List<ScheduleBuild.InstanceProperty> instanceProperties = new ArrayList<ScheduleBuild.InstanceProperty>();
					List<String> buildDates = new ArrayList<String>();
					for(DailySchedule dailySchedule : dailySchedules) {
						ScheduleBuild.Schedule schedule = new ScheduleBuild.Schedule();
						schedule.setName(dailySchedule.getName());
						schedules.add(schedule);
					}
					build.setSchedules(schedules);
					buildDates.add(rebuildDate);
					build.setDates(buildDates);
					build.setHold(_OpConCliArguments.isBuildOnHold());
					build.setOverwrite(true);
					build.setProperties(instanceProperties);
					ScheduleBuild buildresponse = schedulebuilds.post(build);
					String snames = getScheduleNamesForDisplay(schedules);
					if(buildresponse.getId() != null) {
						LOG.info(MessageFormat.format(ScheduleRebuildSuccessMsg, snames, rebuildDate));
						success = 0;
					} else {
						LOG.error(MessageFormat.format(ScheduleRebuildFailedMsg,snames, rebuildDate, ""));
						LOG.error(ArgumentsMsg);
						LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
						LOG.error(MessageFormat.format(DisplayRebuildNoDaysArgumentMsg, String.valueOf(_OpConCliArguments.getNoOfDaysToRebuild())));
						LOG.error(MessageFormat.format(DisplayRebuildIndicatorArgumentMsg, _OpConCliArguments.getScheduleRebuildIndicator()));
						success = 1;
					}
				}
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1831VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}
	
	public Integer actionSchedule(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		ScheduleAction scheduleAction = new ScheduleAction();

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			WsScheduleActions wsScheduleActions = opconApi.scheduleActions();
			// check if schedule exists in the daily
			List<ScheduleAction.Schedule> schedules = new ArrayList<ScheduleAction.Schedule>();
			DailySchedule dailySchedule = checkIfDailyScheduleExists(opconApi, _OpConCliArguments);
			if(dailySchedule != null) {
				ScheduleAction.Schedule schedule = new ScheduleAction.Schedule();
				schedule.setId(dailySchedule.getId());
				schedules.add(schedule);
				scheduleAction.setSchedules(schedules);
				scheduleAction.setAction(getScheduleAction(_OpConCliArguments.getScheduleAction()));
				ScheduleAction resultAction =  wsScheduleActions.post(scheduleAction);
				if((resultAction.getResult() == Result.SUCCESS) ||
						(resultAction.getResult() == Result.SUBMITTED)) {
					LOG.info(MessageFormat.format(ScheduleActionSuccessMsg, _OpConCliArguments.getScheduleAction(),  _OpConCliArguments.getScheduleName(), _OpConCliArguments.getTaskDate()));
					success = 0;
				} else {
					LOG.error(MessageFormat.format(ScheduleActionFailedMsg, _OpConCliArguments.getScheduleAction(),  _OpConCliArguments.getScheduleName(), _OpConCliArguments.getTaskDate(), resultAction.getReason()));
					LOG.error(ArgumentsMsg);
					LOG.error(MessageFormat.format(DisplayTaskDateArgumentMsg, _OpConCliArguments.getTaskDate()));
					LOG.error(MessageFormat.format(DisplayScheduleNameArgumentMsg, _OpConCliArguments.getScheduleName()));
					LOG.error(MessageFormat.format(DisplayScheduleActionArgumentMsg, _OpConCliArguments.getScheduleAction()));
					success = 1;
				}
			} else {
				LOG.error(MessageFormat.format(ScheduleActionFailedMsg, _OpConCliArguments.getScheduleAction(),  _OpConCliArguments.getScheduleName(), _OpConCliArguments.getTaskDate(), "Schedule not found in Daily"));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1831VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	private List<ScheduleBuild.InstanceProperty> getInstancePropertyList(
			String properties
			) throws Exception {
		
		List<ScheduleBuild.InstanceProperty> propertydefs = new ArrayList<ScheduleBuild.InstanceProperty>();
		
		try {
			if(properties != null) {
				String[] propertyArray = _Utilities.tokenizeParameters(properties, false, ICmdConstants.COMMA);
				for(int cntr = 0; cntr < propertyArray.length; cntr++) {
					String[] propertyDefArray = _Utilities.tokenizeParameters(propertyArray[cntr], false, ICmdConstants.EQUAL);
					ScheduleBuild.InstanceProperty propertydef = new ScheduleBuild.InstanceProperty();
					propertydef.setKey(propertyDefArray[0]);
					propertydef.setValue(propertyDefArray[1]);
					propertydefs.add(propertydef);
				}
			}
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return propertydefs;
	}	// END : getPropertyList

	
	private List<String> getRebuildDates(
			String startDate,
			Integer noOfDays
			) throws Exception {
	
		List<String> rebuildDates = new ArrayList<String>();
		int year = 0;
		int month = 0;
		int day = 0;
		int displayMonth = 0;
		boolean isLeapYear = false;
		StringBuffer sbDailyDate = new StringBuffer();
		
		try {
			UtilDateFormat udformat = _Utilities.getDateFormat();
			GregorianCalendar gcal = _Utilities.setCalendarDate(startDate, udformat);
			year = Integer.parseInt(startDate.substring(0,4));
			if(gcal.isLeapYear(year)) {
				isLeapYear = true;
			}
			startDate = startDate.substring(5, startDate.length());
			month = Integer.parseInt(startDate.substring(0,2));
			month--;
			startDate = startDate.substring(3, startDate.length());
			day = Integer.parseInt(startDate);
			// add current date
			sbDailyDate.setLength(0);
			sbDailyDate.append(String.valueOf(year));
			sbDailyDate.append(ICmdConstants.DASH);
			displayMonth = month;
			displayMonth++;
			sbDailyDate.append(String.format("%02d", displayMonth));
			sbDailyDate.append(ICmdConstants.DASH);
			sbDailyDate.append(String.format("%02d", day));
			rebuildDates.add(sbDailyDate.toString());
			for(int cntr = 1; cntr < noOfDays; cntr++) {

				if(month == 0) {
					// january
					if(day == 31) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 1) {
					// february
					if(day == 28) {
						if(isLeapYear){
							day++;
						} else {
							day = 1;
							month++;
						}
					} else if(day == 29) {
						day = 1;
						month++;
					} else {
						day++;
					} 
				} else if(month == 2) {
					// march
					if(day == 31) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 3) {
					// april
					if(day == 30) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 4) {
					// may
					if(day == 31) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 5) {
					// june
					if(day == 30) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 6) {
					// july
					if(day == 31) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 7) {
					// august
					if(day == 31) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 8) {
					// september
					if(day == 30) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 9) {
					// october
					if(day == 31) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 10) {
					// november
					if(day == 30) {
						day = 1;
						month++;
					} else {
						day++;
					}
				} else if(month == 11) {
					// december
					if(day == 31) {
						day = 1;
						month = 0;
						year++;
					} else {
						day++;
					}
				}
				// add current date
				sbDailyDate.setLength(0);
				sbDailyDate.append(String.valueOf(year));
				sbDailyDate.append(ICmdConstants.DASH);
				displayMonth = month;
				displayMonth++;
				sbDailyDate.append(String.format("%02d", displayMonth));
				sbDailyDate.append(ICmdConstants.DASH);
				sbDailyDate.append(String.format("%02d", day));
				rebuildDates.add(sbDailyDate.toString());
			}
			
		} catch (Exception ex) {
			throw new Exception(ex);
		}
		return rebuildDates;
	}	// END : getRebuildDates
	
	private List<DailySchedule> getRebuildSchedulesByDate(
			OpconApi opconApi,
			String checkDate,
			String indicator
			) throws Exception {
		
		List<DailySchedule> requiredSchedules = new ArrayList<DailySchedule>();
		DailySchedulesCriteria criteria = new DailySchedulesCriteria();
		
		Collection<LocalDate> ldates = new ArrayList<LocalDate>();
	    LocalDate dateTime = LocalDate.parse(checkDate, localDateFormatter);
	    ldates.add(dateTime);
		criteria.setDates(ldates);
		WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
		List<DailySchedule> schedules = wsDailySchedules.get(criteria);
		if(indicator != null) {
			for(DailySchedule schedule : schedules) {
				if(schedule.getName().startsWith(indicator)) {
					requiredSchedules.add(schedule);
				}
			}
		} else {
			requiredSchedules.addAll(schedules);
		}
		return requiredSchedules;
	}	// END : getRebuildSchedulesByDate

	private String getScheduleNamesForDisplay(
			List<ScheduleBuild.Schedule> schedules
			) throws Exception {
		
		StringBuilder sbNames = new StringBuilder();
		for(ScheduleBuild.Schedule schedule : schedules) {
			sbNames.append(schedule.getName());
			sbNames.append(ICmdConstants.COMMA);
		}
		sbNames.deleteCharAt(sbNames.length() -1);
		return sbNames.toString();
	}	// END : getScheduleNamesForDisplay

	private DailySchedule checkIfDailyScheduleExists(
			OpconApi opconApi,
			OpConCliArguments _CmdLineArguments
			) throws Exception {
		
		DailySchedule dailySchedule = null;
		DailySchedulesCriteria criteria = new DailySchedulesCriteria();
		
		Collection<LocalDate> ldates = new ArrayList<LocalDate>();
	    LocalDate dateTime = LocalDate.parse(_CmdLineArguments.getTaskDate(), localDateFormatter);
	    ldates.add(dateTime);
	    criteria.setName(_CmdLineArguments.getScheduleName());
		criteria.setDates(ldates);
		WsDailySchedules wsDailySchedules = opconApi.dailySchedules();
		List<DailySchedule> schedules = wsDailySchedules.get(criteria);
		if(schedules.size() > 0) {
			dailySchedule = schedules.get(0);
		} 
		return dailySchedule;
	}	// END : checkIfDailyScheduleExists

	private ScheduleAction.Action getScheduleAction(
			String actionType
			) throws Exception {
		
		ScheduleAction.Action action = null;

		ScheduleActions type = ScheduleActions.valueOf(actionType);
		
		switch (type) {
		
			case hold:
				action = ScheduleAction.Action.HOLD;
				break;
				
			case release:
				action = ScheduleAction.Action.RELEASE;
				break;
				
			case start:
				action = ScheduleAction.Action.START;
				break;
				
			case close:
				action = ScheduleAction.Action.CLOSE;
				break;
		
		}
		return action;
	}	// END : getScheduleAction


}
