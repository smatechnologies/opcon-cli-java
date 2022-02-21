package com.smatechnologies.opcon.command.api.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.enums.TaskType;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.modules.UtilDateFormat;

public class Utilities {

	private Encryption _Encryption = new Encryption();
	
	public CmdConfiguration setConfigurationValues(
			Preferences iniPrefs, 
			CmdConfiguration _CmdConfiguration, 
			String system
			) throws Exception {
		
		_CmdConfiguration.setServer(iniPrefs.node(system).get(ICmdConstants.OPCON_API_SERVER, null));
		_CmdConfiguration.setPort(Integer.parseInt(iniPrefs.node(system).get(ICmdConstants.OPCON_API_PORT, null)));
		String usingTls = iniPrefs.node(system).get(ICmdConstants.OPCON_API_USING_TLS, null);
		if(usingTls.equalsIgnoreCase(ICmdConstants.TRUE)) {
			_CmdConfiguration.setUsingTls(true);
		} else {
			_CmdConfiguration.setUsingTls(false);
		}
		String user = iniPrefs.node(system).get(ICmdConstants.OPCON_API_USER, null);
		if(user != null) {
			byte[] bencrypted = _Encryption.decodeHexString(user);
			_CmdConfiguration.setUser(_Encryption.decode64(bencrypted));
		}
		String password = iniPrefs.node(system).get(ICmdConstants.OPCON_API_PASSWORD, null);
		if(password != null) {
			byte[] bencrypted = _Encryption.decodeHexString(password);
			_CmdConfiguration.setPassword(_Encryption.decode64(bencrypted));
		}
		_CmdConfiguration.setToken(iniPrefs.node(system).get(ICmdConstants.OPCON_API_TOKEN, null));
		String debug = iniPrefs.node(ICmdConstants.GENERAL_HEADER).get(ICmdConstants.GENERAL_DEBUG, null);
		if(debug.equalsIgnoreCase(ICmdConstants.DEBUG_ON)) {
			_CmdConfiguration.setDebug(true);
		} else {
			_CmdConfiguration.setDebug(false);
		}
		return _CmdConfiguration;
	}	// END : setConfigurationValues
	
	public String getExceptionDetails(
			Exception e
			) {
		
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionDetails = sw.toString();
		return exceptionDetails;
	}

	public String getLogDateTimeStamp(
			) throws Exception {
		
		SimpleDateFormat timeStampFormat = new SimpleDateFormat(ICmdConstants.LOG_DATE_TIME_FORMAT);
		String timeStamp = null;
		
		Calendar cal = Calendar.getInstance();
		timeStamp = timeStampFormat.format(cal.getTime());
	    return timeStamp;
	}
	
	public boolean checkDateFormat(
			String checkDateFormat
			) throws Exception {

		String regex = "^[0-9]{4}-(1[0-2]|0[1-9])-(3[01]|[12][0-9]|0[1-9])$";
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(checkDateFormat);
		return matcher.matches();
	}

	public boolean versionCheck(
			String version,
			String task
			) throws Exception {
		
		boolean versionOK = false;
		Integer testNumber183 = 1830;
		Integer testNumber181 = 1810;
		Integer testNumber171 = 1710;
		
		// convert version to integer value for check
		int lastPeriod = version.lastIndexOf(ICmdConstants.PERIOD);
		String apiVersion = version.substring(0,lastPeriod);
		apiVersion = apiVersion.replaceAll("\\.", ICmdConstants.EMPTY_STRING);
		apiVersion = apiVersion + "0000";
		apiVersion = apiVersion.substring(0,4);
		Integer checkNumber = Integer.parseInt(apiVersion);
		
		TaskType taskType = TaskType.valueOf(task);
		
		switch (taskType) {
		
			case AppToken:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case ExpEval:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case Dependency:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case GetJobLog:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case GetJobStatus:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case JobAction:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;
				
			case JobAdd:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case MachAction:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case MachAdd:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case MachGrpAdd:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case MachGrpRemove:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;


			case MachUpdate:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case PropExp:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;
			
			case PropUpdate:
				if(checkNumber >= testNumber181) {
					versionOK = true;
				}
				break;
				
			case SchedAction:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;
				
			case SchedBuild:
				if(checkNumber >= testNumber183) {
					versionOK = true;
				}
				break;

			case SchedRebuild:
				if(checkNumber >= testNumber183) {
					versionOK = true;
				}
				break;

			case ThreshGet:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case ThreshCreate:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case ThreshUpdate:
				if(checkNumber >= testNumber171) {
					versionOK = true;
				}
				break;

			case Version:
				versionOK = true;
				break;
}
		return versionOK;
	}	// END : versionCheck
	
	public String getCurrentDate(
			) throws Exception {
		
		String currentDate = null;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ICmdConstants.CURRENT_DATE_PATTERN);
		currentDate = simpleDateFormat.format(new Date());
		return currentDate;
	}	// END : getCurrentDate

	public GregorianCalendar setCalendarDate(
			String sdate, 
			UtilDateFormat ddate
			) throws Exception {

		Collection <String> dateFields = new ArrayList<String>();
		String sDay = null;
		String sYear = null;
		int iMonth = 0;
		GregorianCalendar gcal = null;
		int fieldCntr = 0;
		
		if(ddate.getDfDateSeperator() != null) {
			dateFields = extractFields(sdate, ddate.getDfDateSeperator(), 4);
			fieldCntr = 0;
			for(Iterator<String> i_dateFields = dateFields.iterator(); i_dateFields.hasNext();) {
				String field = (String)i_dateFields.next();
				if(fieldCntr == 0){
					// extract date position one
					switch (ddate.getDfDatePositionOne()) {
					
						case ICmdConstants.DAY_PARAMETER:
							// day in position one
							sDay = field;
							break;
							
						case ICmdConstants.MONTH_PARAMETER:
							// month in position one
							iMonth = Integer.parseInt(field);
							iMonth--;
							break;
			
						case ICmdConstants.YEAR_PARAMETER:
							// insert year in position one
							sYear = field;
							break;
					}
				} else if (fieldCntr == 1){
					// extract date position two
					switch (ddate.getDfDatePositionTwo()) {
					
						case ICmdConstants.DAY_PARAMETER:
							// day in position two
							sDay = field;
							break;
							
						case ICmdConstants.MONTH_PARAMETER:
							// month in position two
							iMonth = Integer.parseInt(field);
							iMonth--;
							break;
			
						case ICmdConstants.YEAR_PARAMETER:
							// insert year in position two
							sYear = field;
							break;
					}
				} else if (fieldCntr == 2){
					// extract date position three
					switch (ddate.getDfDatePositionThree()) {
					
						case ICmdConstants.DAY_PARAMETER:
							// day in position three
							sDay = field;
							break;
							
						case ICmdConstants.MONTH_PARAMETER:
							// month in position three
							iMonth = Integer.parseInt(field);
							iMonth--;
							break;
			
						case ICmdConstants.YEAR_PARAMETER:
							// insert year in position three
							sYear = field;
							break;
					}
				}
				fieldCntr++;
			}
		} else {
			if(ddate.getDfDateSeperator() == null) {
				if(sdate.length() == 8) {
					switch (ddate.getDfDatePositionOne()) {

					case ICmdConstants.DAY_PARAMETER:
							// day in position three
							sDay = sdate.substring(0,2);
							break;
							
						case ICmdConstants.MONTH_PARAMETER:
							// month in position three
							iMonth = Integer.parseInt(sdate.substring(0,2));
							iMonth--;
							break;
			
						case ICmdConstants.YEAR_PARAMETER:
							// insert year in position three
							sYear = sdate.substring(0,4);
							break;
					}
				} else if(sdate.length() == 7){
					
				} else if(sdate.length() == 6) {
					
				}
			}
		}
		// set the day in the GregorianCalendar
		gcal = new GregorianCalendar(Integer.parseInt(sYear), iMonth, Integer.parseInt(sDay));
		return gcal;
	} // END : setCalendarDate

	public String[] tokenizeParameters(
			String parameters, 
			boolean keepQuote, 
			String delimiter
			) throws Exception {

		final char QUOTE = ICmdConstants.QUOTE.toCharArray()[0];
		final char BACK_SLASH = ICmdConstants.BACKSLASH.toCharArray()[0];
		char prevChar = 0;
		char currChar = 0;
		StringBuffer sb = new StringBuffer(parameters.length());

		if (!keepQuote) {
			for (int i = 0; i < parameters.length(); i++) {
				if (i > 0) {
					prevChar = parameters.charAt(i - 1);
				}
				currChar = parameters.charAt(i);

				if (currChar != QUOTE || (currChar == QUOTE && prevChar == BACK_SLASH)) {
					sb.append(parameters.charAt(i));
				}
			}

			if (sb.length() > 0) {
				parameters = sb.toString();
			}
		}
		return parameters.split(delimiter);
	}	// END : tokenizeParameters
	
	public List<String> extractFields(
			String inputrec, 
			String delimiter, 
			int fields) throws Exception {
		
		List<String> colExtractedFields = new	ArrayList<String>();
		String sNullString = null;
		int ifieldCounter = 0;
		int iEndOfField = 0;
		int iCharCntr = 0;
		int iRecLength = inputrec.length();
		
		while(iCharCntr < iRecLength) {
			// process the input record
			if(ifieldCounter > fields) {
				// only process the first 8 fields
				// terminate
				iCharCntr = iRecLength;
			} else {
				iEndOfField = inputrec.indexOf(delimiter);
				// get first occurrence of delimiter character
				if(iEndOfField == -1) {
					// no more detected?
					String sLastField =	inputrec.substring(0, inputrec.length());
					// YES, so everything to end of string is last field

					colExtractedFields.add(sLastField);
					// inserted extracted field into field collection
					break;
					// end the while loop
				} else {
					if(iEndOfField == 0) {
						// we got a null field?

						colExtractedFields.add(sNullString);
						// YES, insert a null string into the field

						// collection
						inputrec = inputrec.substring(1, inputrec.length());				
						//	adjust the input record
						iCharCntr =	iCharCntr + 1;	// increment characters processed count
						ifieldCounter++; // increment the field counter
					} else {
						String sField = inputrec.substring(0, iEndOfField);
							// NO, extract the field, from beginning of input 
							// rec to next delim			
						colExtractedFields.add(sField);
						// inserted extracted field into field collection
						inputrec = inputrec.substring(iEndOfField + 1, inputrec.length());	
							// adjust the input record
						iCharCntr =	iCharCntr + iEndOfField+1;
						// increment characters processed count
						ifieldCounter++;
						// increment the field counter
					}
				}
			}
		}
	    // return extracted information
	    return colExtractedFields;
	} // END : extractFields

	public UtilDateFormat getDateFormat(
			) throws Exception {
		
		UtilDateFormat dateformat = new UtilDateFormat();
		
		dateformat.setDfDateSeperator(ICmdConstants.DASH);
		dateformat.setDfDatePositionOne(ICmdConstants.YEAR_PARAMETER);
		dateformat.setDfDatePositionTwo(ICmdConstants.MONTH_PARAMETER);
		dateformat.setDfDatePositionThree(ICmdConstants.DAY_PARAMETER);
	    return dateformat;
	} // END : getDateFormat

}
