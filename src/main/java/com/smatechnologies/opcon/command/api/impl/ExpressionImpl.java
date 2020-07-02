package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
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
			StringBuilder sbuilder = new StringBuilder();
			sbuilder.append("[[");
			sbuilder.append(_OpConCliArguments.getPropertyName());
			sbuilder.append("]]=\"");
			sbuilder.append(_OpConCliArguments.getPropertyValue());
			sbuilder.append("\"");
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


}
