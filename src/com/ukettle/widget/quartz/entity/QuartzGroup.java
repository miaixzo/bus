package com.ukettle.widget.quartz.entity;

import java.io.Serializable;

public class QuartzGroup implements Serializable {

	private static final long serialVersionUID = 3366084138623252296L;

	private String id;
	private String name;
	private String type;
	private String status;
	private String description;
	private String creator;
	private String created;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	@Override
	public String toString() {
		return "iGroup [id=" + id + ", name=" + name + ", type=" + type
				+ ", status=" + status + ", description=" + description
				+ ", creator=" + creator + ", created=" + created + "]";
	}

}