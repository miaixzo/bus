package org.ukettle.system.entity;

import org.ukettle.basics.base.entity.BaseEntity;

public class Menu extends BaseEntity {

	private static final long serialVersionUID = -3760206081326709064L;

	private String uid;
	private String pid;
	private String url;
	private String img;
	private String code;
	private String level;
	private String style;

	public void setValue(String key, String value) {
		super.setValue(key, value);
		if ("pid".equals(key)) {
			this.setPid(value);
		} else if ("uid".equals(key)) {
			this.setUid(value);
		} else if ("url".equals(key)) {
			this.setUrl(value);
		} else if ("code".equals(key)) {
			this.setCode(value);
		} else if ("level".equals(key)) {
			this.setLevel(value);
		} else if ("style".equals(key)) {
			this.setStyle(value);
		}
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}