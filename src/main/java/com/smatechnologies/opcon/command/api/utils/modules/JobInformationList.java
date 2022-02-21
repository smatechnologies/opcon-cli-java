package com.smatechnologies.opcon.command.api.utils.modules;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobInformationList {

	private List<JobInformation> jobInformationList = null;

	public List<JobInformation> getJobInformationList() {
		return jobInformationList;
	}

	public void setJobInformationList(List<JobInformation> jobInformationList) {
		this.jobInformationList = jobInformationList;
	}
	
	
	
//    "JobTo": {
//        "type": "object",
//        "properties": {
//          "internalJobId": {
//            "type": "string",
//            "format": "uuid"
//          },
//          "externalJobId": {
//            "type": "string",
//            "format": "uuid"
//          },
//          "sparte": {
//            "type": "string"
//          },
//          "bezeichnung": {
//            "type": "string"
//          },
//          "datumImport": {
//            "type": "string",
//            "format": "date-time"
//          },
//          "dokumentenAnzahlIst": {
//            "type": "integer",
//            "format": "int32"
//          },
//          "dokumentenAnzahlSoll": {
//            "type": "integer",
//            "format": "int32"
//          },
//          "dokumentenAnzahlError": {
//            "type": "integer",
//            "format": "int32"
//          },
//          "status": {
//            "type": "string",
//            "enum": [
//              "PROCESSING",
//              "FINISHED",
//              "STOPPED",
//              "PROCESSING_WITH_ERRORS",
//              "FINISHED_WITH_ERRORS"
//            ]
//          },
//          "externalError": {
//            "type": "boolean"
//          }
//        }
//      },

}
