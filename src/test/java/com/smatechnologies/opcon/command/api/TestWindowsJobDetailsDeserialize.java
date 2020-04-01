package com.smatechnologies.opcon.command.api;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smatechnologies.opcon.command.api.impl.JobImpl;
import com.smatechnologies.opcon.command.api.interfaces.IJob;
import com.smatechnologies.opcon.command.api.ws.WsLogger;
import com.smatechnologies.opcon.restapiclient.DefaultClientBuilder;
import com.smatechnologies.opcon.restapiclient.WsException;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.OpconApiProfile;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.dailyjob.DailyJob;

public class TestWindowsJobDetailsDeserialize {

	private static IJob _IJob = new JobImpl();
	
	private static final String address1 = "https://bvhtest02:9010/api";
	private static final String jobid1 = "20200324|360|1|BLOBS_LIST";
	
	private static final String address2 = "https://34.228.8.114:9010/api";
	private static final String jobid2 = "20200313|133|1|JOB001";
	
	private static final String address3 = "https://18.232.140.63:9010/api";
	private static final String jobid3 = "20200329|110|1|JOB001";

	private OpconApi getClient(OpconApiProfile profile) {
		
		Client client = null;
		OpconApi opconApi;
		ContextResolver<ObjectMapper> ctxObjectMapperProvider; 
				
		try {
	        DefaultClientBuilder clientBuilder = DefaultClientBuilder.get()
	                .setTrustAllCert(true);
	        
	        client = clientBuilder.build();
			DefaultObjectMapperProvider objectMapperProvider = new DefaultObjectMapperProvider();
		    objectMapperProvider.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            client.register(new WsLogger(objectMapperProvider));
	        
            ctxObjectMapperProvider = objectMapperProvider;
            opconApi = new OpconApi(client, profile, new OpconApi.OpconApiListener() {

                @Override
                public void onFailed(WsException e) {
                    if (e.getResponse() == null) {
                    	System.out.println(e.getMessage());
                    }
                }
            }, ctxObjectMapperProvider);
			
			opconApi.login("ocadm", "opconxps");
			
		} catch (KeyManagementException | NoSuchAlgorithmException | WsException e) {
		    e.printStackTrace();
		    return null;
		}
		return opconApi;
		
	}
	
	
	public static void main(String[] args) {
		 TestWindowsJobDetailsDeserialize _client = new  TestWindowsJobDetailsDeserialize();
		OpconApiProfile profile = new OpconApiProfile(address3);
		DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();
		
		
		try {
			OpconApi opconApi = _client.getClient(profile);
			Version version = opconApi.getVersion();
			System.out.println("version " + version.getOpConRestApiProductVersion());
			String id = jobid3;
			DailyJob job = _IJob.getDailyJobById(opconApi, id);
			String jsondata = _DefaultObjectMapperProvider.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(job);
			System.out.println("daily Job (" + jsondata);
//			DailyJob job2 = _DefaultObjectMapperProvider.getObjectMapper().readValue(jsondata, DailyJob.class);
//			String jsondata2 = _DefaultObjectMapperProvider.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(job2);
//			System.out.println("daily Job2 (" + jsondata2);
			

			
		} catch (Exception ex) {
			System.out.println("ex " + ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}

}
