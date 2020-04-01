package com.smatechnologies.opcon.command.api;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.smatechnologies.opcon.restapiclient.api.OpconApi;
import com.smatechnologies.opcon.restapiclient.api.OpconApiProfile;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;
import com.smatechnologies.opcon.restapiclient.model.Version;
import com.smatechnologies.opcon.restapiclient.model.machine.Machine;

public class TestValidateJsonFile {

	
	public static void main(String[] args) {
		TestValidateJsonFile _TestValidateJsonFile = new TestValidateJsonFile();
		DefaultObjectMapperProvider _DefaultObjectMapperProvider = new DefaultObjectMapperProvider();
		
		
		try {
			String fileName = "C:\\test\\api.cmd\\template\\machine_test.json";
			Machine[] machines = _DefaultObjectMapperProvider.getObjectMapper().readValue(new FileInputStream(fileName), Machine[].class );
			List<Machine> machineList = Arrays.asList(machines);
			for(Machine machine : machineList) {
				System.out.println("name " + machine.getName());
			}
		} catch (JsonParseException ex) {
			System.out.println("json parse error " + ex.getMessage());
		} catch (Exception ex) {
			System.out.println("ex " + ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}

}
