package com.ukettle.system.entity;

import com.ukettle.basics.base.entity.BaseEntity;

public class User extends BaseEntity {

	private static final long serialVersionUID = 5259504151652359369L;

	private String password;
	private String random;
	private String email;
	private String mobile;
	private String avatar;
	private String remark;

	public void setValue(String key, String value) {
		super.setValue(key, value);
		if ("password".equals(key)) {
			this.setPassword(value);
		} else if ("email".equals(key)) {
			this.setEmail(value);
		} else if ("mobile".equals(key)) {
			this.setMobile(value);
		} else if ("avatar".equals(key)) {
			this.setAvatar(value);
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}