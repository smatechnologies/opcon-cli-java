package com.smatechnologies.opcon.command.api;

import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;
import com.smatechnologies.opcon.command.api.util.Encryption;

public class TestUnixTerminationCodeExtract {

	
	public static void main(String[] args) {
		
		String terminationCode = "+000000000:0000:N";
		
		try {

			int firstColon = terminationCode.indexOf(ICmdConstants.COLON);
			if(firstColon > -1) {
				terminationCode = terminationCode.substring(0, firstColon);
			}
			System.out.println("tcode {" + terminationCode + "}");
			terminationCode = terminationCode.replace(ICmdConstants.PLUS, ICmdConstants.EMPTY_STRING);
			System.out.println("tcode1 {" + terminationCode + "}");
			System.out.println("result {" + Integer.parseInt(terminationCode) + "}");

		} catch (Exception ex) {
			System.out.println("ex " + ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}

}
