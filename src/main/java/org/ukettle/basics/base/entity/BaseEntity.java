package org.ukettle.basics.base.entity;

/**
 * <p>
 * Entity 基本信息.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Aug 10, 2014
 * @Time 10:12:03
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class BaseEntity extends Entity {

	private static final long serialVersionUID = -601369123580550198L;
	/** 唯一标识ID，主键 */
	protected String id;
	/** 名称 */
	protected String name;
	/** 标题 */
	protected String title;
	/** 类型 */
	protected String type;
	/** 状态 */
	protected String status;
	/** 备注 */
	protected String remark;
	/** 版本 */
	protected String version;
	/** 创建人（ID） */
	protected String creator;
	/** 创建时间 */
	protected String created;
	/** 修改人 */
	protected String modifier;
	/** 修改时间 */
	protected String modified;

	public void setValue(String key, String strVal) {
		if ("id".equals(key)) {
			this.setId(strVal);
		} else if ("name".equals(key)) {
			this.setName(strVal);
		} else if ("title".equals(key)) {
			this.setTitle(strVal);
		} else if ("type".equals(key)) {
			this.setType(strVal);
		} else if ("status".equals(key)) {
			this.setStatus(strVal);
		} else if ("remark".equals(key)) {
			this.setRemark(strVal);
		} else if ("version".equals(key)) {
			this.setVersion(strVal);
		} else if ("creator".equals(key)) {
			this.setCreator(strVal);
		} else if ("created".equals(key)) {
			this.setCreated(strVal);
		} else if ("modifier".equals(key)) {
			this.setModifier(strVal);
		} else if ("modified".equals(key)) {
			this.setModified(strVal);
		}
	}

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

}