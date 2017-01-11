package org.ukettle.widget.quartz.service;

import org.quartz.SchedulerException;

/**
 * <p>
 * Schedule 操作接口.
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2013
 * @Time 10:21:00
 * @version 1.0
 * @since JDK 1.6
 */
public interface QuartzScheduleService<T> {

	/**
	 * 功能描述：添加定时任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public String insert(T entity) throws SchedulerException;

	/**
	 * 功能描述：删除定时任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public String delete(String id) throws SchedulerException;

	/**
	 * 功能描述：更新定时任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public String update(T entity) throws SchedulerException;

	/**
	 * 功能描述：查询单个任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return Object 任务信息
	 */
	public Object select(T entity) throws SchedulerException;

	/**
	 * 功能描述：查询多任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return Object 任务信息列表
	 */
	public Object selectByWhere(T entity) throws SchedulerException;

	/**
	 * 功能描述：检查任务是否存在
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public boolean exists(T entity);

	/**
	 * 功能描述：执行单一任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public String execute(String strVal) throws SchedulerException;

	/**
	 * 功能描述：暂停单一任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public String pause(String strVal) throws SchedulerException;

	/**
	 * 功能描述：恢复单一任务
	 * 
	 * @param entity
	 *            Object 操作对象
	 * @return String 任务编号
	 */
	public String resume(String strVal) throws SchedulerException;

}