package com.ukettle.widget.kettle.entity;

import com.ukettle.basics.base.entity.BaseEntity;


public class KettleRepos extends BaseEntity {

	private static final long serialVersionUID = 8179662751261270620L;

	private String user;
	private String pass;
	private String dir;
	private String db;
	private String dialect;
	private String server;
	private String access;
	private String port;
	private String username;
	private String password;

	public void setValue(String key, String strVal) {
		super.setValue(key, strVal);
		if ("user".equals(key)) {
			this.setUser(strVal);
		} else if ("pass".equals(key)) {
			this.setPass(strVal);
		} else if ("dir".equals(key)) {
			this.setDir(strVal);
		} else if ("db".equals(key)) {
			this.setDb(strVal);
		} else if ("dialect".equals(key)) {
			this.setDialect(strVal);
		} else if ("server".equals(key)) {
			this.setServer(strVal);
		} else if ("port".equals(key)) {
			this.setPort(strVal);
		} else if ("username".equals(key)) {
			this.setUsername(strVal);
		} else if ("password".equals(key)) {
			this.setPassword(strVal);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}