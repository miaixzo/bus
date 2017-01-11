package com.ukettle.widget.kettle.entity;

import com.ukettle.basics.base.entity.BaseEntity;

public class KettleResult extends BaseEntity {

	private static final long serialVersionUID = -8226577801141330491L;

	private String nick;
	private String group;
	private String startTime;
	private String endTime;
	private String times;
	private String error;
	private String read;
	private String written;
	private String updated;
	private String input;
	private String output;
	private String deleted;
	private String retrieved;
	private String rejected;
	private String params;
	private String host;
	private String again;

	public void setValue(String key, String strVal) {
		super.setValue(key, strVal);
		if ("nick".equals(key)) {
			this.setNick(strVal);
		} else if ("group".equals(key)) {
			this.setGroup(strVal);
		} else if ("startTime".equals(key)) {
			this.setStartTime(strVal);
		} else if ("endTime".equals(key)) {
			this.setEndTime(strVal);
		} else if ("again".equals(key)) {
			this.setAgain(strVal);
		} else if ("error".equals(key)) {
			this.setError(strVal);
		}
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTimes() {
		return times;
	}

	public void setTimes(String times) {
		this.times = times;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getRead() {
		return read;
	}

	public void setRead(String read) {
		this.read = read;
	}

	public String getWritten() {
		return written;
	}

	public void setWritten(String written) {
		this.written = written;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getDeleted() {
		return deleted;
	}

	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	public String getRetrieved() {
		return retrieved;
	}

	public void setRetrieved(String retrieved) {
		this.retrieved = retrieved;
	}

	public String getRejected() {
		return rejected;
	}

	public void setRejected(String rejected) {
		this.rejected = rejected;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getAgain() {
		return again;
	}

	public void setAgain(String again) {
		this.again = again;
	}

}