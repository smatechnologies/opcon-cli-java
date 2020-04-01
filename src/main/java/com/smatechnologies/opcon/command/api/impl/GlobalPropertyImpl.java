package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.IGlobalProperty;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.globalproperties.GlobalPropertiesCriteria;
import com.smatechnologies.opcon.restapiclient.api.globalproperties.WsGlobalProperties;
import com.smatechnologies.opcon.restapiclient.model.GlobalProperty;
import com.smatechnologies.opcon.restapiclient.model.Version;

public class GlobalPropertyImpl implements IGlobalProperty {

	private static final String InvalidOpConAPI1810VersionMsg =       "OpCon-API Version {0} not supported, must be 18.1.0 or greater";
	private static final String PropertyUpdateSuccessMsg =            "Property ({0}) updated to ({1}) successfully";
	private static final String PropertyUpdateFailedMsg =             "Property ({0}) update failed : {1}";

	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayPropertyNameArgumentMsg =      "-pn  (property name)      : {0}";
	private static final String DisplayPropertyValueArgumentMsg =     "-pv  (property value)     : {0}";
	private static final String DisplayPropertyEncryptedArgumentMsg = "-pe  (property encrypted) : {0}";

	
	private final static Logger LOG = LoggerFactory.getLogger(GlobalPropertyImpl.class);
	private Utilities _Utilities = new Utilities();

	public Integer updateProperty(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			GlobalPropertiesCriteria criteria = new GlobalPropertiesCriteria();
			criteria.setName(_OpConCliArguments.getPropertyName());
			WsGlobalProperties wsProperties = opconApi.globalProperties();
			List<GlobalProperty> properties = wsProperties.get(criteria);
			if(properties.size() > 0) {
				GlobalProperty property = properties.get(0);
				property.setValue(_OpConCliArguments.getPropertyValue());
				opconApi.globalProperties().put(property);
				LOG.info(MessageFormat.format(PropertyUpdateSuccessMsg, _OpConCliArguments.getPropertyName(), _OpConCliArguments.getPropertyValue()));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(PropertyUpdateFailedMsg, _OpConCliArguments.getPropertyName(), "Property not found in Opcon database"));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayPropertyNameArgumentMsg, _OpConCliArguments.getPropertyName()));
				LOG.error(MessageFormat.format(DisplayPropertyValueArgumentMsg, _OpConCliArguments.getPropertyValue()));
				LOG.error(MessageFormat.format(DisplayPropertyEncryptedArgumentMsg, _OpConCliArguments.isPropertyEncrypted()));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1810VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

}
