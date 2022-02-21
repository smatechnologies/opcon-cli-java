package com.smatechnologies.opcon.command.api.config;

import com.smatechnologies.opcon.command.api.interfaces.ICmdConstants;

public class CmdConfiguration {

	private static CmdConfiguration _CmdConfiguration = null;
	
	private String server = null;
	private Integer port = null;
	private boolean usingTls = true;
	private String user = null;
	private String password = null;
	private String token = ICmdConstants.EMPTY_STRING;
	
	private boolean debug = false;

	protected CmdConfiguration() {
	}

	public static CmdConfiguration getInstance() {
		if(_CmdConfiguration == null) {
			_CmdConfiguration = new CmdConfiguration();
		}
		return _CmdConfiguration;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isUsingTls() {
		return usingTls;
	}

	public void setUsingTls(boolean usingTls) {
		this.usingTls = usingTls;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	

}
