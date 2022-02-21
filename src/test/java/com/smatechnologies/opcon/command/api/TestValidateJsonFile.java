package com.smatechnologies.opcon.command.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.smatechnologies.opcon.command.api.utils.modules.JobInformation;
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
			String fileName = "C:\\test\\OMGTest\\Ausgabe_OMG.JSON";
			File fileStream = new File(fileName, "UTF-8");
			
			JobInformation[] infoArray = _DefaultObjectMapperProvider.getObjectMapper().readValue(
					new InputStreamReader(new FileInputStream(fileName),"UTF8"), JobInformation[].class );
			List<JobInformation> infoList = Arrays.asList(infoArray);
			for(JobInformation info : infoList) {
				System.out.println("jobid " + info.getInternalJobId());
			}
		} catch (JsonParseException ex) {
			System.out.println("json parse error " + ex.getMessage());
		} catch (Exception ex) {
			System.out.println("ex " + ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}

}
