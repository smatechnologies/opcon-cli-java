package com.smatechnologies.opcon.command.api.utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smatechnologies.opcon.command.api.arguments.OpConCliArguments;
import com.smatechnologies.opcon.command.api.config.CmdConfiguration;
import com.smatechnologies.opcon.command.api.impl.JobImpl;
import com.smatechnologies.opcon.command.api.utils.modules.InsertOmgJobArguments;
import com.smatechnologies.opcon.command.api.utils.modules.JobInformation;
import com.smatechnologies.opcon.command.api.utils.modules.JobInformationList;
import com.smatechnologies.opcon.command.api.ws.WsLogger;
import com.smatechnologies.opcon.restapiclient.DefaultClientBuilder;
import com.smatechnologies.opcon.restapiclient.WsErrorException;
import com.smatechnologies.opcon.restapiclient.WsException;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.OpconApiProfile;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;

public class InsertOmgJobImpl {
	
	private static final String UrlFormatTls = "https://{0}:{1}/api";
	private static final String UrlFormatNonTls = "http://{0}:{1}/api";

	private final static Logger LOG = LoggerFactory.getLogger(InsertOmgJobImpl.class);
	private static CmdConfiguration _CmdConfiguration = CmdConfiguration.getInstance();
	private JobImpl _JobImpl = new JobImpl();

	private ObjectMapper _ObjectMapper = new ObjectMapper();
	DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();


	public Integer processRequest(
			InsertOmgJobArguments _InsertOmgJobArguments
			) throws Exception {
		
		Integer completionCode = null;
		String url = null;
		
		// create client connection
		if(_CmdConfiguration.isUsingTls()) {
			url = MessageFormat.format(UrlFormatTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
		} else {
			url = MessageFormat.format(UrlFormatNonTls, _CmdConfiguration.getServer(), String.valueOf(_CmdConfiguration.getPort()));
		}
		OpconApiProfile profile = new OpconApiProfile(url);
		OpconApi opconApi = getClient(profile);
		
		// read file
		List<JobInformation> list = getJobInformation(_InsertOmgJobArguments.getFileName());
		for(JobInformation jobInfo : list) {
			LOG.info("processing OMG internal JobId = " + jobInfo.getInternalJobId());
			// does job exist?
			boolean exists = doesJobExistInSchedule(opconApi, _InsertOmgJobArguments, jobInfo.getInternalJobId());
			if(!exists) {
				// add job to schedule
				LOG.info("Adding OMG internal JobId = " + jobInfo.getInternalJobId() + " as " + "JOMG_" + jobInfo.getInternalJobId());
				Integer result = createOmgJob(opconApi, _InsertOmgJobArguments, jobInfo.getInternalJobId());
			} else {
				LOG.info("OMG internal JobId = " + jobInfo.getInternalJobId() + " exists as " + "JOMG_" + jobInfo.getInternalJobId());
			}
		}
		return 0;
	}	
	
	private OpconApi getClient(
			OpconApiProfile profile
			) throws Exception {
		
		OpconApi opconApi;
		Client client = null;
		ContextResolver<ObjectMapper> ctxObjectMapperProvider; 
		
		try {
			if(_CmdConfiguration.isDebug()) {
		        DefaultClientBuilder clientBuilder = DefaultClientBuilder.get()
		                .setTrustAllCert(true);
		        
		        client = clientBuilder.build();
				DefaultObjectMapperProvider objectMapperProvider = new DefaultObjectMapperProvider();
			    objectMapperProvider.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
	            client.register(new WsLogger(objectMapperProvider));
		        
	            ctxObjectMapperProvider = objectMapperProvider;
			} else {
		        DefaultClientBuilder clientBuilder = DefaultClientBuilder.get()
		                .setTrustAllCert(true);
		        
		        client = clientBuilder.build();
		        DefaultObjectMapperProvider objectMapperProvider = new DefaultObjectMapperProvider();
			    objectMapperProvider.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
	            ctxObjectMapperProvider = objectMapperProvider;
			}
            opconApi = new OpconApi(client, profile, new OpconApi.OpconApiListener() {

                @Override
                public void onFailed(WsException e) {
                    if (e.getResponse() == null) {
                        LOG.error("[OpconApi] A web service call has failed.", e);
                    } else if (e instanceof WsErrorException) {
                        LOG.warn("[OpconApi] A web service call return API Error: {}", e.getResponse().readEntity(String.class));
                    } else {
                        LOG.error("[OpconApi] A web service call has failed. Response: Header={} Body={}", e.getResponse().getHeaders(), e.getResponse().readEntity(String.class), e);
                    }
                }
            }, ctxObjectMapperProvider);
			
			opconApi.login(_CmdConfiguration.getUser(), _CmdConfiguration.getPassword());
			
		} catch (KeyManagementException | NoSuchAlgorithmException | WsException e) {
		    throw new Exception(e);
		}
		return opconApi;
	}	// END : getClient

	private List<JobInformation> getJobInformation(
			String filename
			) throws Exception {
		List<JobInformation> infoList = new ArrayList<>();
		
		JobInformation[] infoArray = _DefaultObjectMapperProvider.getObjectMapper().readValue(
				new InputStreamReader(new FileInputStream(filename),"UTF8"), JobInformation[].class );
		infoList = Arrays.asList(infoArray);
		return infoList;
	}

	private Integer createOmgJob(
			OpconApi opconApi,
			InsertOmgJobArguments _InsertOmgJobArguments,
			String internalJobId
			) throws Exception {
		
		Integer result = -1;
		
		OpConCliArguments opConCliArguments = new OpConCliArguments();
		opConCliArguments.setTask("JobAdd");
		opConCliArguments.setTaskDate(_InsertOmgJobArguments.geteDate());
		opConCliArguments.setScheduleName(_InsertOmgJobArguments.getScheduleName());
		opConCliArguments.setJobName(_InsertOmgJobArguments.getJobName());
		opConCliArguments.setFrequencyName(_InsertOmgJobArguments.getFrequency());
		StringBuilder sbProperties = new StringBuilder();
		sbProperties.append("JOBID=");
		sbProperties.append(internalJobId);
		opConCliArguments.setInstanceProperties(sbProperties.toString());
		opConCliArguments.setBuildOnHold(false);
		result = _JobImpl.jobAddRequest(opconApi, opConCliArguments);
		return result;
	}
	
	private boolean doesJobExistInSchedule(
			OpconApi opconApi,
			InsertOmgJobArguments _InsertOmgJobArguments,
			String internalJobId
			) throws Exception {
		
		boolean exists = false;
		
		OpConCliArguments opConCliArguments = new OpConCliArguments();
		opConCliArguments.setTask("JobAdd");
		opConCliArguments.setTaskDate(_InsertOmgJobArguments.geteDate());
		opConCliArguments.setScheduleName(_InsertOmgJobArguments.getScheduleName());
		StringBuilder sbJobName = new StringBuilder();
		sbJobName.append("JOMG_");
		sbJobName.append(internalJobId);
		opConCliArguments.setJobName(sbJobName.toString());
		DailyJob djob = _JobImpl.getDailyJobByName(opconApi, opConCliArguments);
		if(djob != null) {
			exists = true;
		}
		return exists;
	}
	
}
