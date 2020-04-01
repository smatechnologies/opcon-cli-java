package com.smatechnologies.opcon.command.api;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smatechnologies.opcon.command.api.impl.MachineGroupImpl;
import com.smatechnologies.opcon.command.api.ws.WsLogger;
import com.smatechnologies.opcon.restapiclient.DefaultClientBuilder;
import com.smatechnologies.opcon.restapiclient.WsErrorException;
import com.smatechnologies.opcon.restapiclient.WsException;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.OpconApiProfile;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

public class TestClientBuilder {

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
		TestClientBuilder _TestClientBuilder = new TestClientBuilder();
		OpconApiProfile profile = new OpconApiProfile("https://bvhtest02:9010/api");
		
		
		try {
			System.out.println("get opconApi");
			OpconApi opconApi = _TestClientBuilder.getClient(profile);
			System.out.println("get version ");
			Version version = opconApi.getVersion();
			System.out.println("version " + version.getOpConRestApiProductVersion());
			List<Machine> machines = opconApi.machines().get(null);
			for(Machine machine : machines) {
				System.out.println("machine " + machine.getName());
			}
			
		} catch (Exception ex) {
			System.out.println("ex " + ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}
}
