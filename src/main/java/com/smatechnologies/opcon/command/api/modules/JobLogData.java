package com.smatechnologies.opcon.command.api.modules;

import java.util.ArrayList;
import java.util.List;

public class JobLogData {
	
	private String filename = null;
	private List<String> records = new ArrayList<String>();
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<String> getRecords() {
		return records;
	}

	public void setRecords(List<String> records) {
		this.records = records;
	}

}
