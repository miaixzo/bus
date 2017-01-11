package org.ukettle.widget.kettle.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukettle.engine.loader.BasicService;
import org.ukettle.system.entity.User;
import org.ukettle.widget.kettle.entity.KettleLogs;
import org.ukettle.widget.kettle.entity.KettleResult;
import org.ukettle.widget.kettle.entity.KettleSpoon;
import org.ukettle.widget.kettle.service.KettleTransService;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.DateUtils;
import org.ukettle.www.toolkit.Message;
import org.ukettle.www.toolkit.ObjectID;
import org.ukettle.www.toolkit.ServerUtils;
import org.ukettle.www.toolkit.StringUtils;

import net.sf.json.JSONObject;

@Service
public class KettleTransServiceImpl<T extends KettleSpoon> implements
		KettleTransService<T> {

	@Autowired
	private BasicService service;

	private Map<String, String> map = new HashMap<String, String>();
	private Repository repository;
	private TransMeta transMeta;

	/**
	 * execute Job
	 * 
	 * @param entity
	 * @throws KettleException
	 */
	@Override
	public void execute(T entity) {
		synchronized (map) {
			if (map.containsKey(entity.getTid())) {
				int cnt = 0;
				while (map.containsKey(entity.getTid())) {
					try {
						Thread.sleep(60 * 1000);
						cnt++;
						if (cnt > 60) {
							synchronized (map) {
								map.remove(entity.getTid());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				this.map.put(entity.getTid(), entity.getTid());
			}
		}
		Trans trans = null;
		String exception = null;
		try {
			trans = (Trans) getInstance(entity);
			trans.setLogLevel(Constant.KETTLE_LOGLEVEL);
			if (StringUtils.isNotEmpty(entity.getLogs())) {
				trans.setLogLevel(Constant.logger(entity.getLogs()));
			}
			transMeta.setCapturingStepPerformanceSnapShots(true);
			trans.setMonitored(true);
			trans.setInitializing(true);
			trans.setPreparing(true);
			trans.setRunning(true);
			trans.setSafeModeEnabled(true);
			if (entity.isAsync()) {
				trans.startThreads();
			} else {
				trans.execute(null);
				trans.waitUntilFinished();
			}
			// 放入一个MAP存储结果
			Map<String, List<StepPerformanceSnapShot>> map = new HashMap<String, List<StepPerformanceSnapShot>>();
			trans.setStepPerformanceSnapShots(map);
			while (trans.isFinished()) {
				if (trans.getStepPerformanceSnapShots() != null
						&& trans.getStepPerformanceSnapShots().size() > 0) {
					// 得到所有步骤
					Map<String, List<StepPerformanceSnapShot>> SnapShots = trans
							.getStepPerformanceSnapShots();
					// 输出动态监控情况
					Iterator<?> it = SnapShots.entrySet().iterator();
					while (it.hasNext()) {
						Entry<?, ?> en = (Entry<?, ?>) it.next();
						// 步骤当前情况
						List<?> SnapShotList = (ArrayList<?>) en.getValue();
						if (SnapShotList != null && SnapShotList.size() > 0) {
							StepPerformanceSnapShot SnapShot = (StepPerformanceSnapShot) SnapShotList
									.get(SnapShotList.size() - 1);
							System.out.println(JSONObject.fromObject(SnapShot)
									.toString());
						}
					}
				}
			}
		} catch (Exception e) {
			exception = e.getMessage();
			e.printStackTrace();
		} finally {
			if (null != trans && trans.isFinished()) {
				synchronized (map) {
					this.map.remove(entity.getTid());
				}
				iMessage(trans, entity, exception);
			}
		}
	}

	/**
	 * 根据entity中name和category参数，初始化Trans对象
	 * 
	 * @param entity
	 * @return TransMeta
	 * @throws KettleException
	 */
	@Override
	public Object getInstance(T entity) throws KettleException {
		repository = (Repository) service.iKettleReposService
				.selectByWhere(entity.getId());
		Trans trans = null;
		if (null != repository) {
			getMetaInstance(entity);
			trans = new Trans(transMeta);
		}
		return trans;
	}

	/**
	 * 从Map里获得已创建的Trans元数据对象，如果此对象为null，初始化新的元数据
	 * 
	 * @param entity
	 * @return TansMeta
	 * @throws KettleException
	 */
	@Override
	public Object getMetaInstance(T entity) throws KettleException {
		RepositoryDirectoryInterface rd = repository.findDirectory(entity
				.getDir());
		transMeta = repository.loadTransformation(entity.getName(), rd, null,
				false, null);
		setParameters(entity);
		return transMeta;
	}

	/**
	 * 重置旧参数，设置新参数
	 * 
	 * @param entity
	 * @return TansMeta
	 */
	@Override
	public Object setParameters(T entity) {
		transMeta.clearParameters();
		Iterator<Entry<String, String>> it = entity.getValue().entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, String> obj = it.next();
			try {
				transMeta.setParameterValue(obj.getKey(), obj.getValue());
			} catch (UnknownParamException e) {
				e.printStackTrace();
			}
		}
		return transMeta;
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
		String[] key = transMeta.listParameters();
		Map<String, String> map = new HashMap<String, String>();
		if (null != key) {
			for (int i = 0; i < key.length; i++) {
				String val = transMeta.getParameterDefault(key[i]);
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
	private void iMessage(Trans trans, T entity, String exception) {
		if (null != trans && null != entity) {
			String title = entity.getName();
			if (null != entity.getTid() && !"".equals(entity.getTid())) {
				entity.setName(entity.getTid());
			}
			entity.setName(title);
			String id = iMonitor(trans, entity);
			entity.setId(id);
			iLogging(trans, entity, exception);
			if (trans.getErrors() > 0) {
				iEmail(trans, entity, exception);
			}
		}
		trans = null;
	}

	/**
	 * 记录job操作信息
	 * 
	 * @param job
	 * @param entity
	 * @throws KettleException
	 */
	private String iMonitor(Trans trans, T entity) {
		KettleResult result = new KettleResult();
		result.setName(entity.getName());
		result.setParams(JSONObject.fromObject(entity.getValue()).toString());
		result.setStartTime(entity.getCreated());
		result.setEndTime(DateUtils.getTime24());
		result.setDeleted(String.valueOf(trans.getResult().getNrLinesDeleted()));
		result.setInput(String.valueOf(trans.getResult().getNrLinesInput()));
		result.setError(String.valueOf(trans.getErrors()));
		result.setOutput(String.valueOf(trans.getResult().getNrLinesOutput()));
		result.setRead(String.valueOf(trans.getResult().getNrLinesRead()));
		result.setUpdated(String.valueOf(trans.getResult().getNrLinesUpdated()));
		result.setWritten(String.valueOf(trans.getResult().getNrLinesWritten()));
		result.setRetrieved(String.valueOf(trans.getResult()
				.getNrFilesRetrieved()));
		result.setRejected(String.valueOf(trans.getResult()
				.getNrLinesRejected()));
		result.setCreator("Kettle");
		result.setCreated(DateUtils.getTime24());
		result.setType("Running");
		result.setHost(ServerUtils.getName() + ":" + ServerUtils.getHost());
		if (entity.isTest()) {
			result.setType("Testing");
		}
		return (String) service.iKettleResultService.insert(result);
	}

	/**
	 * 记录job日志信息
	 * 
	 * @param job
	 * @param entity
	 * @throws KettleException
	 */
	private void iLogging(Trans trans, T entity, String exception) {
		if (entity.isTest() || trans.getErrors() > 0) {
			String logChannelId = trans.getLogChannelId();
			Log4jBufferAppender appender = CentralLogStore.getAppender();
			String logText = appender.getBuffer(logChannelId, true).toString();
			CentralLogStore.discardLines(logChannelId, true);
			if (null == logText || "".equals(logText)) {
				logText = exception;
			}
			KettleLogs log = new KettleLogs();
			log.setId(ObjectID.id());
			log.setMid(entity.getId());
			log.setLogs(logText);
			log.setCreator("Kettle");
			log.setCreated(DateUtils.getTime24());
			// service.iKettleLogsService.insert(log);
		}
	}

	/**
	 * 邮件预警
	 * 
	 * @param job
	 * @param entity
	 */
	private void iEmail(Trans trans, T entity, String exception) {
		if (null != entity) {
			String content = null;
			if (null != trans.getResult().getLogText()
					&& !"".equals(trans.getResult().getLogText())) {
				content = trans.getResult().getLogText();
			} else if (null != exception && !"".equals(exception)) {
				content = exception;
			}
			if (null != content && !"".equals(content)) {
				User u = new User();
				u.setType("KETTLE");
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
									entity.getName(), content);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
		}
	}
}