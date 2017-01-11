package org.ukettle.widget.kettle.entity;

import java.util.HashMap;
import java.util.Map;

public class KettleSpoon extends KettleRepos {

	private static final long serialVersionUID = -3189686130094031576L;

	private String tid;
	private String rid;
	private String repo;
	private String logs;
	private String again;
	private String params;
	private String method;
	/** 是否测试 */
	private boolean test = false;
	/** 错误信息通知 */
	private boolean error = false;
	/** 是否同步执行 */
	private boolean async = true;
	/** 是否进入队列 */
	private boolean queue = true;

	private Map<String, String> value = new HashMap<String, String>();

	public void setValue(String key, String strVal) {
		try {
			super.setValue(key, strVal);
			if ("rid".equals(key)) {
				this.setRid(strVal);
			} else if ("repo".equals(key)) {
				this.setRepo(strVal);
			} else if ("logs".equals(key)) {
				this.setLogs(strVal);
			} else if ("error".equals(key)) {
				this.setError(Boolean.valueOf(strVal));
			} else if ("async".equals(key)) {
				this.setAsync(Boolean.valueOf(strVal));
			} else if ("test".equals(key)) {
				this.setTest(Boolean.valueOf(strVal));
			} else if ("queue".equals(key)) {
				this.setQueue(Boolean.valueOf(strVal));
			} else if ("method".equals(key)) {
				this.setMethod(strVal);
			}
			strVal = new String(strVal.getBytes("utf-8"), "utf-8");
			value.put(key, strVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}

	public String getAgain() {
		return again;
	}

	public void setAgain(String again) {
		this.again = again;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isTest() {
		return test;
	}

	public void setTest(boolean test) {
		this.test = test;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isQueue() {
		return queue;
	}

	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	public Map<String, String> getValue() {
		return value;
	}

	public void setValue(Map<String, String> value) {
		this.value = value;
	}

}