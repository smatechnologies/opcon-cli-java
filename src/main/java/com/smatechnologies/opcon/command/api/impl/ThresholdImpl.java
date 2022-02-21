package com.smatechnologies.opcon.command.api.impl;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.interfaces.IThreshold;
import com.smatechnologies.opcon.command.api.util.Utilities;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.thresholds.ThresholdsCriteria;
import com.smatechnologies.opcon.restapiclient.api.thresholds.WsThresholds;
import com.smatechnologies.opcon.restapiclient.model.Threshold;
import com.smatechnologies.opcon.restapiclient.model.Version;

public class ThresholdImpl implements IThreshold {

	private static final String InvalidOpConAPI1710VersionMsg =       "OpCon-API Version {0} not supported, must be 17.1.0 or greater";
	private static final String ThresholdCreateSuccessMsg =           "Threshold ({0}) created successfully";
	private static final String ThresholdCreateFailedMsg =            "Threshold ({0}) create failed";
	private static final String ThresholdUpdateSuccessMsg =           "Threshold ({0}) updated to ({1}) successfully";
	private static final String ThresholdUpdateFailedMsg =            "Threshold ({0}) update failed : {1}";

	private static final String ArgumentsMsg =                        "arguments";
	private static final String DisplayThresholdNameArgumentMsg =     "-tn  (threshold name)     : {0}";
	private static final String DisplayThresholdValueArgumentMsg =    "-tv  (threshold value)    : {0}";

	
	private final static Logger LOG = LoggerFactory.getLogger(ThresholdImpl.class);
	private Utilities _Utilities = new Utilities();

	@SuppressWarnings("unused")
	public Integer createThreshold(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			ThresholdsCriteria criteria = new ThresholdsCriteria();
			criteria.setName(_OpConCliArguments.getThresholdName());
			WsThresholds wsThresholds = opconApi.thresholds();
			
			Threshold threshold = new Threshold();
			threshold.setId(0);
			threshold.setName(_OpConCliArguments.getThresholdName());
			threshold.setValue(_OpConCliArguments.getThresholdValue());
			Threshold resultThreshold = opconApi.thresholds().post(threshold);
			if(resultThreshold.getId() > 0) { 
				LOG.info(MessageFormat.format(ThresholdCreateSuccessMsg, _OpConCliArguments.getThresholdName(), String.valueOf(_OpConCliArguments.getThresholdValue())));
				success = 0;
			} else {
				LOG.info(MessageFormat.format(ThresholdCreateFailedMsg, _OpConCliArguments.getThresholdName()));
				success = 0;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

	@SuppressWarnings("unused")
	public Integer updateThreshold(
			OpconApi opconApi,
			OpConCliArguments _OpConCliArguments
			) throws Exception {
		
		Integer success = 1;
		

		Version version = opconApi.getVersion();
		boolean versionOK = _Utilities.versionCheck(version.getOpConRestApiProductVersion(), _OpConCliArguments.getTask());
		if(versionOK) {
			ThresholdsCriteria criteria = new ThresholdsCriteria();
			criteria.setName(_OpConCliArguments.getThresholdName());
			WsThresholds wsThresholds = opconApi.thresholds();
			
			List<Threshold> thresholds = wsThresholds.get(criteria);
			if(thresholds.size() > 0) {
				Threshold threshold = thresholds.get(0);
				threshold.setValue(_OpConCliArguments.getThresholdValue());
				Threshold resultThreshold = opconApi.thresholds().put(threshold);
				LOG.info(MessageFormat.format(ThresholdUpdateSuccessMsg, _OpConCliArguments.getThresholdName(), String.valueOf(_OpConCliArguments.getThresholdValue())));
				success = 0;
			} else {
				LOG.error(MessageFormat.format(ThresholdUpdateFailedMsg, _OpConCliArguments.getThresholdName(), "Threshold not found in Opcon database"));
				LOG.error(ArgumentsMsg);
				LOG.error(MessageFormat.format(DisplayThresholdNameArgumentMsg, _OpConCliArguments.getThresholdName()));
				LOG.error(MessageFormat.format(DisplayThresholdValueArgumentMsg, String.valueOf(_OpConCliArguments.getThresholdValue())));
				success = 1;
			}
		} else {
			success = 1;
			LOG.error(MessageFormat.format(InvalidOpConAPI1710VersionMsg, version.getOpConRestApiProductVersion()));
		}
		return success;
	}

}
