package com.ukettle.service.router.entity;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.ukettle.basics.base.entity.BaseEntity;
import com.ukettle.www.xstream.annotations.XStream2Field;

@XStreamAlias("response")
public class Message extends BaseEntity {

	private static final long serialVersionUID = 3668575797512981091L;

	@XStreamAlias("code")
	@XStream2Field
	private String code;
	@XStreamAlias("message")
	@XStream2Field
	private String message;

	public Message() {
	}

	public Message(Error code) {
		this.code = String.valueOf(code.getKey());
		this.message = code.getMessage();
	}

	public Message(Error code, String message) {
		this.code = String.valueOf(code.getKey());
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public enum Error {
		/** 访问权限 */
		ACCESS_VALID_FAIL(100, "Authentication Failed"), 
		ACCESS_VALID_ERROR(101, "Permission Error"),
		/** 系统权限 */
		SYSTEM_VALID_USER(200, "Uknown User"), 
		SYSTEM_VALID_ERROR(201,"Unknown Error"),
		/** API业务参数 */
		API_MISSING_PARAMS(300, "Missing Parameter"), 
		API_MISSING_ID(201,"Missing id"), 
		API_MISSING_SIGNATURE(302, "Missing signature"), 
		API_MISSING_TIMESTAMP(303, "Missing timestamp"), 
		API_INVALID_TIMESTAMP(304, "Invalid timestamp"), 
		API_VALID_FAIL(305, "Valid Fail"),
		/** 其他参数 */
		ERROR(0, "service error"), UNKNOWN(-1, "unknown error");
		final int key;
		final String message;

		private Error(int key, String message) {
			this.key = key;
			this.message = message;
		}

		public static Error getErrorCode(int key) {
			for (Error err : values()) {
				if (err.key == key)
					return err;
			}
			return UNKNOWN;
		}

		public int getKey() {
			return key;
		}

		public String getMessage() {
			return message;
		}
	}

}