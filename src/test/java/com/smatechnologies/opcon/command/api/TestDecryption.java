package com.smatechnologies.opcon.command.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.smatechnologies.opcon.command.api.util.Encryption;
import com.smatechnologies.opcon.command.api.utils.modules.JobInformation;
import com.smatechnologies.opcon.restapiclient.jackson.DefaultObjectMapperProvider;

public class TestDecryption {

	public static void main(String[] args) {
		Encryption _Encryption = new Encryption();
		
		try {
			String tid = "593259314d6d49345a6d59744d474d794e4330304e574e6d4c546b794e6a4974595451784d7a4e6a4e6a41325a444130";
			String cid = "4e57457a4e6d4578596d59744f475530597930304e444e6a4c57466a4f545574596a597a4e4755324f4755314d6d5531";
			String key = "4c575178645768775a7a5258517a6457515870364c6d5a6c4e455a774c6c4a594d554e6e4d7a4a544e6d4a794c513d3d";
			String sub = "4f57557759324e6d4e5459744e7a55354e6930304e5755794c546b30596a67744f5468694e44466c5a57526d4d546377";
			byte[] tidbencrypted = _Encryption.decodeHexString(tid);
			System.out.println("TID {" + (_Encryption.decode64(tidbencrypted)) + "}");
			byte[] cidbencrypted = _Encryption.decodeHexString(cid);
			System.out.println("CID {" + (_Encryption.decode64(cidbencrypted)) + "}");
			byte[] keybencrypted = _Encryption.decodeHexString(key);
			System.out.println("KEY {" + (_Encryption.decode64(keybencrypted)) + "}");
			byte[] subbencrypted = _Encryption.decodeHexString(sub);
			System.out.println("SUB {" + (_Encryption.decode64(subbencrypted)) + "}");
		} catch (Exception ex) {
			System.out.println("ex " + ex.getMessage());
			ex.printStackTrace();
			
		}
		
	}

}

