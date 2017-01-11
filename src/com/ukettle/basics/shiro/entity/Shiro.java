package com.ukettle.basics.shiro.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.ukettle.basics.base.entity.BaseEntity;

public class Shiro extends BaseEntity {

	private static final long serialVersionUID = 865277385297809342L;

	public String id;
	public String email;
	public String name;

	public Shiro(String id, String email, String name) {
		this.id = id;
		this.email = email;
		this.name = name;
	}

	public static final Shiro get() {
		return (Shiro) SecurityUtils.getSubject().getPrincipal();
	}

	public static final Session getSession() {
		return SecurityUtils.getSubject().getSession();
	}

	public static final Subject getSubject() {
		return SecurityUtils.getSubject();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ShiroUser [id=" + id + ", email=" + email + ", name=" + name
				+ "]";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "email");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "email");
	}

}