package org.ukettle.widget.kettle.entity;

import org.ukettle.basics.base.entity.BaseEntity;

public class KettleLogs extends BaseEntity {

	private static final long serialVersionUID = 2116625402456316153L;

	private String mid;
	private String logs;

	public void setValue(String key, String strVal) {
		super.setValue(key, strVal);
		if ("mid".equals(key)) {
			this.setMid(strVal);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

}