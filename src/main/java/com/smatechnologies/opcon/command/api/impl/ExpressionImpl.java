package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.interfaces.IExpression;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.model.PropertyExpression;
import com.smatechnologies.opcon.restapiclient.model.Version;

public class ExpressionImpl implements IExpression {

	
	private static final String InvalidOpConAPI1710VersionMsg = "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String ExpressionEvalSuccessMsg =      "Expression evaluation ({0}) completed successfully";
	private static final String ExpressionEvalFailedMsg =       "Expression evaluation ({0}) failed : {1} : message {2}";
	private static final String ArgumentsMsg =                  "arguments";

	private static final String DisplayExpressionEvalArgumentMsg = "-ev  (expression)         : {0}";
	private static final String DisplayPropertyNameArgumentMsg =   "-pn  (property name)      : {0}";
	private static final String DisplayPropertyValueArgumentMsg =  "-pv  (property value)     : {0}";

	private final static Logger LOG = LoggerFactory.getLogger(ExpressionImpl.class);
	private Utilities _Utilities = new Utilities();

	public Integer propertyExpressionRequest(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		PropertyExpression propertyExpression = new PropertyExpression();
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			String correctedPropertyName = wrapScheduleAndJobNames(_OpConCliArguments.getPropertyName());
			StringBuilder sbuilder = new StringBuilder();
			sbuilder.append(ICmdConstants.DOUBLE_LEFT_SQUARE_BRACKET);
			sbuilder.append(correctedPropertyName);
			sbuilder.append(ICmdConstants.DOUBLE_RIGHT_SQUARE_BRACKET);
			sbuilder.append(ICmdConstants.EQUAL);
			sbuilder.append(ICmdConstants.DOUBLE_QUOTES);
			sbuilder.append(_OpConCliArguments.getPropertyValue());
			sbuilder.append(ICmdConstants.DOUBLE_QUOTES);
			propertyExpression.setExpression(sbuilder.toString());
			PropertyExpression resultExpression = opconApi.propertyExpressions().post(propertyExpression);
			if(resultExpression.getResult() != null) {
				LOG.info(MessageFormat.format(ExpressionEvalSuccessMsg,  sbuilder.toString()));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(ExpressionEvalFailedMsg, 
						sbuilder.toString(), resultExpression.getResult(), resultExpression.getMessage()));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayPropertyNameArgumentMsg, _OpConCliArguments.getPropertyName()));
				LOG.error(MessageFormat.format(DisplayPropertyValueArgumentMsg, _OpConCliArguments.getPropertyValue()));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}
	
	public Integer expressionEvaluationRequest(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		PropertyExpression propertyExpression = new PropertyExpression();
		
		
		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			propertyExpression.setExpression(_OpConCliArguments.getExpression());
			PropertyExpression resultExpression = opconApi.propertyExpressions().post(propertyExpression);
			if(resultExpression.getResult() != null) {
				LOG.info(MessageFormat.format(ExpressionEvalSuccessMsg,  _OpConCliArguments.getExpression()));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(ExpressionEvalFailedMsg, 
						_OpConCliArguments.getExpression(), 
						resultExpression.getResult(), 
						resultExpression.getMessage()));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayExpressionEvalArgumentMsg, 
						_OpConCliArguments.getExpression()));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	private String wrapScheduleAndJobNames(
			String property
			) throws Exception {

		StringBuilder sbuilder = new StringBuilder();
		System.out.println("property = {" + property + "}");
		if(property.startsWith(ICmdConstants.PropertyTypes.SCHEDULE_INSTANCE)) {
			// remove SI
			sbuilder.append(ICmdConstants.PropertyTypes.SCHEDULE_INSTANCE);
			property = property.replace(ICmdConstants.PropertyTypes.SCHEDULE_INSTANCE, ICmdConstants.EMPTY_STRING);
			// extract the property name
			String[] pname = extractPropertyFieldByPeriod(property);
			sbuilder.append(pname[0]);
			property = pname[1];
			// extract the date
			String[] pdate = extractPropertyFieldByPeriod(property);
			sbuilder.append(pdate[0]);
			property = pdate[1];
			sbuilder.append(ICmdConstants.QUOTE);
			sbuilder.append(property);
			if(property.endsWith(ICmdConstants.RIGHT_SQUARE_BRACKET)) {
				sbuilder.append(ICmdConstants.SPACE);
			}
			sbuilder.append(ICmdConstants.QUOTE);
		} else if(property.startsWith(ICmdConstants.PropertyTypes.JOB_INSTANCE)) {
			// remove JI
			sbuilder.append(ICmdConstants.PropertyTypes.JOB_INSTANCE);
			property = property.replace(ICmdConstants.PropertyTypes.JOB_INSTANCE, ICmdConstants.EMPTY_STRING);
			// extract the property name
			String[] pname = extractPropertyFieldByPeriod(property);
			sbuilder.append(pname[0]);
			property = pname[1];
			// extract the date
			String[] pdate = extractPropertyFieldByPeriod(property);
			sbuilder.append(pdate[0]);
			property = pdate[1];
			String[] plast = extractLastPropertyField(property);
			sbuilder.append(ICmdConstants.QUOTE);
			sbuilder.append(plast[1]);
			if(property.endsWith(ICmdConstants.RIGHT_SQUARE_BRACKET)) {
				sbuilder.append(ICmdConstants.SPACE);
			}
			sbuilder.append(ICmdConstants.QUOTE);
			sbuilder.append(plast[0]);
		} else if(property.startsWith(ICmdConstants.PropertyTypes.OPCON_INSTANCE)) {
			// remove OI
			sbuilder.append(ICmdConstants.PropertyTypes.OPCON_INSTANCE);
			property = property.replace(ICmdConstants.PropertyTypes.OPCON_INSTANCE, ICmdConstants.EMPTY_STRING);
			sbuilder.append(ICmdConstants.QUOTE);
			sbuilder.append(property);
			sbuilder.append(ICmdConstants.QUOTE);
		} else {
			sbuilder.append(ICmdConstants.QUOTE);
			sbuilder.append(property);
			sbuilder.append(ICmdConstants.QUOTE);
		}
		return sbuilder.toString();
	}

	private String[] extractPropertyFieldByPeriod(
			String value
			) throws Exception {
		
		String[] result = new String[3];
		
		int iPeriod = value.indexOf(ICmdConstants.PERIOD);
		if(iPeriod > -1) {
			result[0] = value.substring(0, iPeriod + 1);
			result[1] = value.substring(iPeriod + 1, value.length());
		} else {
			result[0] = null;
			result[1] = value;
		}
		return result;
	}

	private String[] extractLastPropertyField(
			String value
			) throws Exception {
		
		String[] result = new String[3];
		
		int iPeriod = value.lastIndexOf(ICmdConstants.PERIOD);
		if(iPeriod > -1) {
			result[0] = value.substring(iPeriod, value.length());
			result[1] = value.substring(0, iPeriod);
		} else {
			result[0] = null;
			result[1] = value;
		}
		return result;
	}

}
