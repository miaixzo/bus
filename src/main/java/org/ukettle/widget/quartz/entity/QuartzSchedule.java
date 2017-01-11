package org.ukettle.widget.quartz.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class QuartzSchedule implements Serializable {

	private static final long serialVersionUID = -536480398093621017L;

	private String id;
	private String userId;
	private String repo;
	private String name;
	private String group;
	private String status;
	private String nextTime;
	private String prevTime;
	private String repeat;
	private String interval;
	private String executor;
	private String execType;
	private String execTime;
	private String startDate;
	private String endDate;
	private String triggerType;

	private String hour;
	private String minute;
	private String second;
	private String quantity;
	private String frequency;

	private String day;
	private String month;
	private String year;
	private String week;
	private String cycle;
	private String remote;
	private String version;
	private String description;

	private Map<String, Object> map = new HashMap<String, Object>();

	public void setValue(String key, String strVal) {
		if ("id".equals(key)) {
			this.setId(strVal);
		} else if ("group".equals(key)) {
			this.setGroup(strVal);
		} else if ("title".equals(key)) {
			this.setName(strVal);
		} else if ("executor".equals(key)) {
			this.setExecutor(strVal);
		} else if ("triggerType".equals(key)) {
			this.setTriggerType(strVal);
		} else if ("second".equals(key)) {
			this.setSecond(strVal);
		} else if ("minute".equals(key)) {
			this.setMinute(strVal);
		} else if ("hour".equals(key)) {
			this.setHour(strVal);
		} else if ("day".equals(key)) {
			this.setDay(strVal);
		} else if ("month".equals(key)) {
			this.setMonth(strVal);
		} else if ("week".equals(key)) {
			this.setWeek(strVal);
		} else if ("quantity".equals(key)) {
			this.setQuantity(strVal);
		} else if ("frequency".equals(key)) {
			this.setFrequency(strVal);
		} else if ("description".equals(key)) {
			this.setDescription(strVal);
		} else {
			map.put(key, strVal);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNextTime() {
		return nextTime;
	}

	public void setNextTime(String nextTime) {
		this.nextTime = nextTime;
	}

	public String getPrevTime() {
		return prevTime;
	}

	public void setPrevTime(String prevTime) {
		this.prevTime = prevTime;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getExecType() {
		return execType;
	}

	public void setExecType(String execType) {
		this.execType = execType;
	}

	public String getExecTime() {
		return execTime;
	}

	public void setExecTime(String execTime) {
		this.execTime = execTime;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getMinute() {
		return minute;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

}