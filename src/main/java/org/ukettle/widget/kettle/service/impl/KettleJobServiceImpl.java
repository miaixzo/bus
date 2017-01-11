package org.ukettle.widget.kettle.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukettle.basics.shiro.entity.Shiro;
import org.ukettle.engine.loader.BasicService;
import org.ukettle.system.entity.User;
import org.ukettle.widget.kettle.entity.KettleLogs;
import org.ukettle.widget.kettle.entity.KettleRepos;
import org.ukettle.widget.kettle.entity.KettleResult;
import org.ukettle.widget.kettle.entity.KettleSpoon;
import org.ukettle.widget.kettle.service.KettleJobService;
import org.ukettle.widget.quartz.entity.QuartzQueue;
import org.ukettle.www.toolkit.Blocking;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.DateUtils;
import org.ukettle.www.toolkit.Message;
import org.ukettle.www.toolkit.ObjectID;
import org.ukettle.www.toolkit.ServerUtils;
import org.ukettle.www.toolkit.StringUtils;

import net.sf.json.JSONObject;

@Service
public class KettleJobServiceImpl<T extends KettleSpoon> implements
        KettleJobService<T> {

	@Autowired
	private BasicService service;

	private Repository repository;
	private JobMeta jobMeta;

	private Map<String, Job> jobMap = new HashMap<String, Job>();
	private Map<String, QuartzQueue<T>> entityMap = new HashMap<String, QuartzQueue<T>>();

	public KettleJobServiceImpl() {
		WaitQueueThread wqt = new WaitQueueThread("WaitQueueThread");
		wqt.setDaemon(true);
		wqt.start();
	}

	/**
	 * @throws execute
	 *             Job
	 * 
	 * @param entity
	 * @throws KettleException
	 * @throws
	 */
	@SuppressWarnings("all")
	@Override
	public void execute(T entity) {
		synchronized (entityMap) {
			if (null != entity.getTid()) {
				QuartzQueue<T> ei = new QuartzQueue<T>();
				ei.setStartTime(System.currentTimeMillis());
				ei.setEntity(entity);
				entityMap.put(entity.getTid(), ei);
			}
		}

		String exception = null;
		Job job = null;
		try {
			job = (Job) getInstance(entity);
			job.setLogLevel(Constant.KETTLE_LOGLEVEL);
			if (StringUtils.isNotEmpty(entity.getLogs())) {
				job.setLogLevel(Constant.logger(entity.getLogs()));
			}
			entity.setCreated(DateUtils.getTime24());
			jobMap.put(entity.getTid(), job);
			long st = System.currentTimeMillis();
			job.start();
			job.waitUntilFinished();
			System.out.println("Date:" + DateUtils.getTime24() + " "
					+ entity.getTid() + " is finish,time is :"
					+ (System.currentTimeMillis() - st) / 1000);
		} catch (Exception e) {
			exception = e.getMessage();
			e.printStackTrace();
		} finally {
			if (null != job && job.isFinished()) {
				if (job.getErrors() > 0
						&& (null == job.getResult().getLogText() || ""
								.equals(job.getResult().getLogText()))) {
					job.getResult().setLogText(exception);
				}
				iMessage(job, entity);
				synchronized (entityMap) {
					QuartzQueue ei = entityMap.remove(entity.getTid());
					ei = null;
					jobMap.remove(entity.getTid());
				}
			}
			entity = null;
			job = null;
		}
	}

	/**
	 * @throws execute
	 *             Job
	 * 
	 * @param entity
	 * @throws KettleException
	 * @throws
	 */
	@SuppressWarnings("all")
	@Override
	public void executeJob(T entity) {
		String exception = null;
		Job job = null;
		try {
			job = (Job) getInstance(entity);
			job.setLogLevel(Constant.KETTLE_LOGLEVEL);
			if (StringUtils.isNotEmpty(entity.getLogs())) {
				job.setLogLevel(Constant.logger(entity.getLogs()));
			}
			entity.setCreated(DateUtils.getTime24());
			job.start();
			job.waitUntilFinished();
		} catch (Exception e) {
			exception = e.getMessage();
			e.printStackTrace();
		} finally {
			if (null != job && job.isFinished()) {
				if (job.getErrors() > 0
						&& (null == job.getResult().getLogText() || ""
								.equals(job.getResult().getLogText()))) {
					job.getResult().setLogText(exception);
				}
				iMessage(job, entity);
				entity = null;
				job = null;
			}
		}
	}

	/**
	 * 根据entity中name和category参数，初始化Job对象
	 * 
	 * @param entity
	 * @return JobMeta
	 * @throws KettleException
	 */
	@Override
	public Object getInstance(T entity) throws KettleException {
		KettleRepos repo = new KettleRepos();
		repo.setName(entity.getRepo());
		repository = (Repository) service.iKettleReposService.get(repo);
		Job job = null;
		if (null != repository) {
			getMetaInstance(entity);
			job = new Job(repository, jobMeta);
		}
		return job;
	}

	/**
	 * 从Map里获得已创建的Job元数据对象，如果此对象为null，初始化新的元数据
	 * 
	 * @param entity
	 * @return JobMeta
	 * @throws KettleException
	 */
	@Override
	public Object getMetaInstance(T entity) throws KettleException {
		RepositoryDirectoryInterface rd = (repository.findDirectory(entity
				.getDir()));
		jobMeta = repository.loadJob(entity.getMethod(), rd, null, null);
		jobMeta = (JobMeta) jobMeta.realClone(false);
		jobMeta.compareTo(jobMeta);
		setParameters(entity);
		return jobMeta;
	}

	/**
	 * 重置旧参数，设置新参数
	 * 
	 * @param entity
	 * @return JobMeta
	 */
	@Override
	public Object setParameters(T entity) throws KettleException {
		jobMeta.clearParameters();
		Iterator<Entry<String, String>> it = entity.getValue().entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, String> obj = it.next();
			try {
				jobMeta.setParameterValue(obj.getKey(), obj.getValue());
			} catch (UnknownParamException e) {
				e.printStackTrace();
			}
		}
		return jobMeta;
	}

	/**
	 * 获取所有参数
	 * 
	 * @param entity
	 * @return String[]
	 */
	@Override
	public Object getParameters(T entity) throws KettleException {
		getInstance(entity);
		String[] key = jobMeta.listParameters();
		Map<String, String> map = new HashMap<String, String>();
		if (null != key) {
			for (int i = 0; i < key.length; i++) {
				String val = jobMeta.getParameterDefault(key[i]);
				map.put(key[i], val == null ? "" : val);
			}
		}
		return map;
	}

	/**
	 * job动态监控
	 * 
	 * @param job
	 * @param entity
	 * @throws KettleException
	 */
	private void iMessage(Job job, T entity) {
		if (null != job && null != entity) {
			if (null != entity.getRid() && !"".equals(entity.getRid())) {
				KettleResult result = new KettleResult();
				result.setId(entity.getRid());
				result.setModifier(Shiro.get().getId());
				result.setModified(DateUtils.getTime24());
				int second = DateUtils.getInterval(entity.getCreated(),
						result.getModified());
				result.setTimes(String.valueOf(second));
				if (null == entity.getAgain() || "".equals(entity.getAgain())) {
					entity.setAgain("0");
				}
				result.setAgain(String.valueOf(Integer.parseInt(entity.getAgain()) + 1));
				if (job.getErrors() < 1) {
					result.setStatus(Constant.STATUS_AGAIN);
				}
				if (job.getErrors() > 0
						&& Integer.parseInt(entity.getAgain()) > 2) {
					iEmail(job, entity);
				}
				if (Constant.STATUS_ERROR.equals(entity.getStatus())) {
					service.iKettleResultService.update(result);
				} else {
					result.setId(entity.getRid());
					result.setStatus("1");
					service.iKettleResultService.update(result);
				}
				return;
			}
			if (null != entity.getTid()) {
				entity.setName(entity.getTid());
			}
			String mid = iMonitor(job, entity);
			entity.setId(mid);
			if (job.getErrors() > 0) {
				iLogging(job, entity);
			}
		}
	}

	/**
	 * 记录job操作信息
	 * 
	 * @param job
	 * @param entity
	 * @throws KettleException
	 */
	private String iMonitor(Job job, T entity) {
		KettleResult m = new KettleResult();
		m.setName(entity.getName());
		m.setParams(JSONObject.fromObject(entity.getValue()).toString());
		m.setStartTime(entity.getCreated());
		m.setEndTime(DateUtils.getTime24());
		m.setDeleted(String.valueOf(job.getResult().getNrLinesDeleted()));
		m.setInput(String.valueOf(job.getResult().getNrLinesInput()));
		m.setError(String.valueOf(job.getErrors()));
		m.setOutput(String.valueOf(job.getResult().getNrLinesOutput()));
		m.setRead(String.valueOf(job.getResult().getNrLinesRead()));
		m.setUpdated(String.valueOf(job.getResult().getNrLinesUpdated()));
		m.setWritten(String.valueOf(job.getResult().getNrLinesWritten()));
		m.setRetrieved(String.valueOf(job.getResult().getNrFilesRetrieved()));
		m.setRejected(String.valueOf(job.getResult().getNrLinesRejected()));
		m.setHost(ServerUtils.getName() + ":" + ServerUtils.getHost());
		m.setCreated(entity.getCreated());
		m.setCreator(Shiro.get().getId());
		m.setModifier(Shiro.get().getId());
		m.setModified(DateUtils.getTime24());
		int second = DateUtils.getInterval(entity.getCreated(), m.getEndTime());
		m.setTimes(String.valueOf(second));
		m.setType(Constant.TYPE_RUNNING);
		if (entity.isTest()) {
			m.setType(Constant.TYPE_TESTING);
		}
		m.setStatus(Constant.STATUS_COMPLETE);
		m.setAgain("0");
		if (job.getErrors() > 0) {
			m.setStatus(Constant.STATUS_ERROR);
		}
		return (String) service.iKettleResultService.insert(m);
	}

	/**
	 * 记录job日志信息
	 * 
	 * @param job
	 * @param entity
	 * @throws KettleException
	 */
	private void iLogging(Job job, T entity) {
		KettleLogs log = new KettleLogs();
		log.setId(ObjectID.id());
		log.setMid(entity.getId());
		log.setLogs(job.getResult().getLogText());
		log.setCreator(Shiro.get().getId());
		log.setCreated(DateUtils.getTime24());
		service.iKettleLogsService.insert(log);
	}

	/**
	 * 邮件预警
	 * 
	 * @param job
	 * @param entity
	 */
	private void iEmail(Job job, T entity) {
		if (null != entity && !entity.isTest()) {
			if (null != job.getResult().getLogText()
					&& !"".equals(job.getResult().getLogText())) {
				User u = new User();
				u.setType(Constant.TYPE_USER_KETTLE);
				u.setStatus(Constant.STATUS_ENABLED);
				@SuppressWarnings("all")
				List<User> list = (List<User>) service.iUserService
						.selectByWhere(u);
				if (null != list && list.size() > 0) {
					for (User user : list) {
						try {
							Message.iMessage(Constant.MAIL_USERNAME,
									Constant.MAIL_PASSWORD, Constant.MAIL_SMTP,
									Integer.valueOf(Constant.MAIL_PORT),
									Constant.MAIL_SENDER, user.getEmail(),
									entity.getName(), job.getResult()
											.getLogText());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	class WaitQueueThread extends Thread {

		private long lastShowTime = 0L;
		private long showTimeInterval = 10000;

		public WaitQueueThread(String name) {
			this.setName(name);
		}

		@SuppressWarnings("all")
		public void run() {
			while (true) {
				try {
					if ((System.currentTimeMillis() - this.lastShowTime) > this.showTimeInterval) {
						this.lastShowTime = System.currentTimeMillis();
						int waitJobSize = Blocking.jobSize();
						System.out.println("date:" + DateUtils.getTime24()
								+ ":current waiting execed job is :"
								+ waitJobSize);
						synchronized (entityMap) {
							Iterator<String> it = entityMap.keySet().iterator();

							while (it.hasNext()) {
								String jk = it.next();
								QuartzQueue<T> rei = entityMap.get(jk);
								long rt = System.currentTimeMillis()
										- rei.getStartTime();
								if (rt > Blocking.DEFAULT_EKETTLE_JOB_RUNNING_TIME) {
									Job cjob = jobMap.remove(jk);
									if (cjob != null) {
										System.out
												.println("date:"
														+ DateUtils.getTime24()
														+ " job["
														+ jk
														+ "] is running "
														+ (rt / 1000)
														+ "s,is too long,will top the job!!");
										cjob.stopAll();
										System.out
												.println("date:"
														+ DateUtils.getTime24()
														+ " job["
														+ jk
														+ "] is stoped!!,retry to exec the job");
										Blocking.addToWaitingQueue(rei
												.getEntity());
									}

								} else {
									System.out.println("date:"
											+ DateUtils.getTime24() + " job["
											+ jk + "] is running "
											+ (rt / 1000) + "s");
								}
							}
						}

					}
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}