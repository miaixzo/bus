package org.ukettle.widget.quartz.service.impl;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.StringMatcher.StringOperatorName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukettle.widget.quartz.entity.QuartzSchedule;
import org.ukettle.widget.quartz.mapper.QuartzScheduleMapper;
import org.ukettle.widget.quartz.service.QuartzScheduleService;
import org.ukettle.www.toolkit.Constant;

import net.sf.json.JSONArray;

@Service
public class QuartzScheduleServiceImpl<T extends QuartzSchedule> implements
        QuartzScheduleService<T> {

	private final static String simpleTrigger = "simple";
	private final static String cronTrigger = "cron";
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private QuartzScheduleMapper<T> mapper;

	/**
	 * 添加定时任务
	 * 
	 * @param entity
	 * @return String
	 * @throws SchedulerException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String insert(T entity) throws SchedulerException {
		if (null != entity && null != entity.getName()
				&& !"".equals(entity.getName()) && null != entity.getGroup()
				&& !"".equals(entity.getGroup())) {
			Trigger trigger = null;
			if (simpleTrigger.equals(entity.getTriggerType())) {
				trigger = newTrigger()
						.withIdentity(entity.getName(), entity.getGroup())
						.withSchedule(
								simpleSchedule()
										.withIntervalInMinutes(
												Integer.parseInt(entity
														.getFrequency()))
										.withRepeatCount(
												Integer.parseInt(entity
														.getQuantity())))
						.withDescription(entity.getDescription()).build();
			}
			if (cronTrigger.equals(entity.getTriggerType())) {
				String cronExpression = String.format("%s %s %s %s %s %s",
						entity.getSecond(), entity.getMinute(),
						entity.getHour(), entity.getDay(), entity.getMonth(),
						entity.getWeek());
				boolean isValid = CronExpression
						.isValidExpression(cronExpression);
				if (!isValid) {
					throw new IllegalArgumentException(
							"cronExpression cannot be null");
				}
				trigger = newTrigger()
						.withIdentity(entity.getName(), entity.getGroup())
						.withSchedule(cronSchedule(cronExpression))
						.withDescription(entity.getDescription()).build();
			}
			try {
				Class jobClass = Class.forName(entity.getExecutor());
				JobDetail job = newJob(jobClass)
						.withIdentity(entity.getName(), entity.getGroup())
						.withDescription(entity.getDescription()).build();
				JobDataMap map = job.getJobDataMap();
				Iterator<?> it = entity.getMap().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
					map.put((String) ent.getKey(), ent.getValue());
				}
				scheduler.scheduleJob(job, trigger);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
		return entity.getId();
	}

	/**
	 * 删除定时任务
	 * 
	 * @param value
	 * @return String
	 * @throws SchedulerException
	 */
	@Override
	public String delete(String value) throws SchedulerException {
		JobKey key = null;
		try {
			String[] jobKey = value.split("\\.");
			if (jobKey.length > 0) {
				key = JobKey.jobKey(jobKey[1], jobKey[0]);
				scheduler.deleteJob(key);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return key.toString();
	}

	/**
	 * 更新定时任务
	 * 
	 * @param entity
	 * @return String
	 * @throws SchedulerException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String update(T entity) throws SchedulerException {
		if (null != entity && null != entity.getName()
				&& !"".equals(entity.getName()) && null != entity.getGroup()
				&& !"".equals(entity.getGroup())) {
			Trigger trigger = null;
			if (simpleTrigger.equals(entity.getTriggerType())) {
				trigger = newTrigger()
						.withIdentity(entity.getName(), entity.getGroup())
						.withSchedule(
								simpleSchedule()
										.withIntervalInMinutes(
												Integer.parseInt(entity
														.getFrequency()))
										.withRepeatCount(
												Integer.parseInt(entity
														.getQuantity())))
						.withDescription(entity.getDescription()).build();
			}
			if (cronTrigger.equals(entity.getTriggerType())) {
				String cronExpression = String.format("%s %s %s %s %s %s",
						entity.getSecond(), entity.getMinute(),
						entity.getHour(), entity.getDay(), entity.getMonth(),
						entity.getWeek());
				boolean isValid = CronExpression
						.isValidExpression(cronExpression);
				if (!isValid) {
					throw new IllegalArgumentException(
							"cronExpression cannot be null");
				}
				trigger = newTrigger()
						.withIdentity(entity.getName(), entity.getGroup())
						.withSchedule(cronSchedule(cronExpression))
						.withDescription(entity.getDescription()).build();
			}
			try {
				Class jobClass = Class.forName(entity.getExecutor());
				JobDetail job = newJob(jobClass)
						.withIdentity(entity.getName(), entity.getGroup())
						.withDescription(entity.getDescription()).build();
				JobDataMap map = job.getJobDataMap();
				Iterator<?> it = entity.getMap().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
					map.put((String) ent.getKey(), ent.getValue());
				}
				if (null != delete(entity.getId())) {
					scheduler.scheduleJob(job, trigger);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
		return entity.getId();
	}

	/**
	 * 查询单个定时任务
	 * 
	 * @param entity
	 * @return Map
	 * @throws SchedulerException
	 */
	@Override
	public Object select(T entity) throws SchedulerException {
		String[] keyArray = entity.getId().split("\\.");
		JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(keyArray[1],
				keyArray[0]));
		String params = JSONArray.fromObject(jobDetail.getJobDataMap())
				.toString();
		Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(
				keyArray[1], keyArray[0]));
		Map<String, Object> map = new HashMap<String, Object>();
		if (trigger instanceof SimpleTrigger) {
			map.put("triggerType", simpleTrigger);
		} else {
			map.put("triggerType", cronTrigger);
		}
		for (Object key : jobDetail.getJobDataMap().keySet()) {
			if (Constant.KETTLE_REPO.equals(key.toString())) {
				map.put(Constant.KETTLE_REPO, jobDetail.getJobDataMap().get(key)
						.toString());
			}
		}
		map.put("params", params);
		map.put("jobDetail", jobDetail);
		map.put("trigger", trigger);
		return map;
	}

	/**
	 * 查询所有定时任务
	 * 
	 * @param entity
	 * @return Map
	 * @throws SchedulerException
	 */
	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	@Override
	public Object selectByWhere(T entity) throws SchedulerException {
		List<String> groups = scheduler.getJobGroupNames();
		List<HashMap<String, Object>> jobList = new ArrayList<HashMap<String, Object>>();
		for (String group : groups) {
			if (null != entity.getGroup() && !group.contains(entity.getGroup())) {
				continue;
			}
			Set<JobKey> jobKeys = scheduler.getJobKeys(new GroupMatcher(group,
					StringOperatorName.EQUALS) {
			});
			for (JobKey jobKey : jobKeys) {
				if (null != entity.getName()
						&& !jobKey.toString().contains(entity.getName())) {
					continue;
				}
				JobDetail jobDetail = scheduler.getJobDetail(jobKey);
				HashMap<String, Object> jobInfoMap = new HashMap<String, Object>();
				List<? extends Trigger> triggers = scheduler
						.getTriggersOfJob(jobKey);
				jobInfoMap.put("triggers", triggers);
				jobInfoMap.put("jobDetail", jobDetail);
				jobInfoMap.put("params",
						JSONArray.fromObject(jobDetail.getJobDataMap())
								.toString());
				jobInfoMap.put("type", "Kettle");
				jobList.add(jobInfoMap);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jobList", jobList);
		map.put("scheduler", scheduler);
		return map;
	}

	/**
	 * 判断是否重名
	 * 
	 * @param entity
	 * @return boolean
	 * @throws SchedulerException
	 */
	@Override
	public boolean exists(T entity) {
		if (null != mapper.exists(entity)) {
			return true;
		}
		return false;
	}

	/**
	 * 执行单个定时任务
	 * 
	 * @param strVal
	 * @return String
	 * @throws SchedulerException
	 */
	@Override
	public String execute(String strVal) throws SchedulerException {
		JobKey key = null;
		try {
			String[] jobKey = strVal.split("\\.");
			if (jobKey.length > 0) {
				key = JobKey.jobKey(jobKey[1], jobKey[0]);
				Trigger trigger = newTrigger()
						.withIdentity(jobKey[1] + UUID.randomUUID().toString(),
								jobKey[0]).withPriority(100).forJob(key)
						.build();
				scheduler.scheduleJob(trigger);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return key.toString();
	}

	/**
	 * 暂停单个定时任务
	 * 
	 * @param strVal
	 * @return String
	 * @throws SchedulerException
	 */
	@Override
	public String pause(String strVal) throws SchedulerException {
		JobKey key = null;
		try {
			String[] jobKey = strVal.split("\\.");
			if (jobKey.length > 0) {
				key = JobKey.jobKey(jobKey[1], jobKey[0]);
				scheduler.pauseJob(key);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return key.toString();
	}

	/**
	 * 恢复单个定时任务
	 * 
	 * @param strVal
	 * @return String
	 * @throws SchedulerException
	 */
	@Override
	public String resume(String strVal) throws SchedulerException {
		JobKey key = null;
		try {
			String[] jobKey = strVal.split("\\.");
			if (jobKey.length > 0) {
				key = JobKey.jobKey(jobKey[1], jobKey[0]);
				scheduler.resumeJob(key);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return key.toString();
	}

}