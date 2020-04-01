package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.IPropertyExpression;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.model.PropertyExpression;
import com.smatechnologies.opcon.restapiclient.model.Version;

public class PropertyExpressionImpl implements IPropertyExpression {

	
	private static final String InvalidOpConAPI1710VersionMsg = "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String PropertyExpressionSuccessMsg =  "Expression evaluation ({0}) completed successfully";
	private static final String PropertyExpressionFailedMsg =   "Expression evaluation ({0}) failed : {1} : message {2}";
	private static final String ArgumentsMsg =                  "arguments";

	private static final String DisplayPropertyExpressionArgumentMsg = "-ev  (expression)         : {0}";

	private final static Logger LOG = LoggerFactory.getLogger(PropertyExpressionImpl.class);
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
			propertyExpression.setExpression(_OpConCliArguments.getPropertyExpression());
			PropertyExpression resultExpression = opconApi.propertyExpressions().post(propertyExpression);
			if(resultExpression.getResult() != null) {
				LOG.info(MessageFormat.format(PropertyExpressionSuccessMsg,  _OpConCliArguments.getPropertyExpression()));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(PropertyExpressionFailedMsg, _OpConCliArguments.getPropertyExpression(), resultExpression.getResult(), resultExpression.getMessage()));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayPropertyExpressionArgumentMsg, _OpConCliArguments.getPropertyExpression()));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}
	
}
