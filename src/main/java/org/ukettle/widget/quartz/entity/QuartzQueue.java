package org.ukettle.widget.quartz.entity;

import org.ukettle.widget.kettle.entity.KettleSpoon;


public class QuartzQueue<T extends KettleSpoon> {

	private T entity;

	private long startTime;

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

}