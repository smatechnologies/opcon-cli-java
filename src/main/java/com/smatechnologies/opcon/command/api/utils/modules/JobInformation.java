package com.smatechnologies.opcon.command.api.utils.modules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobInformation {
	
	private String internalJobId = null;
	private String externalJobId = null;
	private String sparte = null;
	private String bezeichnung = null;
	private Long datumImport = null;
	private Integer dokumentenAnzahlIst = null;
	private Integer dokumentenAnzahlSoll = null;
	private Integer dokumentenAnzahlError = null;
	private String status = null;
	private String externalError = null;
	
	public String getInternalJobId() {
		return internalJobId;
	}
	
	public void setInternalJobId(String internalJobId) {
		this.internalJobId = internalJobId;
	}
	
	public String getExternalJobId() {
		return externalJobId;
	}
	
	public void setExternalJobId(String externalJobId) {
		this.externalJobId = externalJobId;
	}
	
	public String getSparte() {
		return sparte;
	}
	
		public void setSparte(String sparte) {
		this.sparte = sparte;
	}
	
	public String getBezeichnung() {
		return bezeichnung;
	}
	
	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}
	
	public Long getDatumImport() {
		return datumImport;
	}
	
	public void setDatumImport(Long datumImport) {
		this.datumImport = datumImport;
	}
	
	public Integer getDokumentenAnzahlIst() {
		return dokumentenAnzahlIst;
	}
	
	public void setDokumentenAnzahlIst(Integer dokumentenAnzahlIst) {
		this.dokumentenAnzahlIst = dokumentenAnzahlIst;
	}
	
	public Integer getDokumentenAnzahlSoll() {
		return dokumentenAnzahlSoll;
	}
	
	public void setDokumentenAnzahlSoll(Integer dokumentenAnzahlSoll) {
		this.dokumentenAnzahlSoll = dokumentenAnzahlSoll;
	}
	
	public Integer getDokumentenAnzahlError() {
		return dokumentenAnzahlError;
	}
	
	public void setDokumentenAnzahlError(Integer dokumentenAnzahlError) {
		this.dokumentenAnzahlError = dokumentenAnzahlError;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getExternalError() {
		return externalError;
	}

	public void setExternalError(String externalError) {
		this.externalError = externalError;
	}

}
