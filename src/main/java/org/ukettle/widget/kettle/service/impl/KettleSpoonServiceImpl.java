package org.ukettle.widget.kettle.service.impl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.ukettle.basics.shiro.entity.Shiro;
import org.ukettle.engine.loader.BasicMapper;
import org.ukettle.engine.loader.BasicService;
import org.ukettle.widget.kettle.entity.KettleSpoon;
import org.ukettle.widget.kettle.service.KettleSpoonService;
import org.ukettle.www.toolkit.Blocking;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.DateUtils;
import org.ukettle.www.toolkit.ObjectID;

@Service
public class KettleSpoonServiceImpl<T extends KettleSpoon> implements
		KettleSpoonService<T> {

	@Autowired
	private BasicMapper mapper;
	@Autowired
	private BasicService service;

	private Hashtable<String, KettleWorker> iThread;

	@Override
	public String insert(T entity) throws DataAccessException {
		if (null != entity) {
			
			String id = Shiro.get().getId();
			entity.setId(ObjectID.id());
			entity.setCreator(id);
			entity.setModifier(id);
			entity.setStatus(Constant.STATUS_ENABLED);
			entity.setCreated(DateUtils.getTime24());
			entity.setModified(DateUtils.getTime24());
			if (mapper.iKettleSpoonMapper.insert(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String update(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setModified(DateUtils.getTime24());
			if (mapper.iKettleSpoonMapper.update(entity) > 0) {
				return entity.getId();
			}
		}
		return null;
	}

	@Override
	public String delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (mapper.iKettleSpoonMapper.delete(id) > 0) {
				return id;
			}
		}
		return null;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleSpoonMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleSpoonMapper.selectByWhere(entity);
		}
		return null;
	}

	@Override
	public Object execute(T entity) {
		if (null == entity) {
			return "params is null";
		}
		if (null == entity.getDir()) {
			return "dir is null";
		}
		if (null == entity.getType()) {
			return "type is null";
		}
		if (Constant.TYPE_JOB.equals(entity.getType())) {
			if (entity.isQueue()) {
				Blocking.addToWaitingQueue((KettleSpoon) entity);
			} else {
				service.iKettleJobService.execute(entity);
			}
			return "execute is success";
		} else if (Constant.TYPE_TRANS.equals(entity.getType())) {
			service.iKettleTransService.execute(entity);
			return "execute is success";
		}
		return "failure,please check the parameter values";
	}

	public void initKettleWorker(int workers) {
		iThread = new Hashtable<String, KettleWorker>(workers);
		for (int i = 0; i < workers; i++) {
			KettleWorker kw = new KettleWorker("KettleWorker-" + i);
			kw.setDaemon(true);
			kw.start();
			iThread.put(String.valueOf(i), kw);
		}
	}

	public void cleanKettleWorker() {
		Blocking.clean();
		Iterator<String> iterator = iThread.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
			KettleWorker dt = (KettleWorker) iThread.get(key);
			dt.stopThread();
			dt = null;
		}
		iThread.clear();
		iThread = null;
	}

	class KettleWorker extends Thread {

		private boolean isRun = false;

		public KettleWorker(String name) {
			this.setName(name);
			this.isRun = true;
		}

		@SuppressWarnings("all")
		public void run() {
			while (isRun) {
				try {
					KettleSpoon entity = Blocking.getObjectFromWaitingQueue();
					if (entity != null) {
						execute((T) entity);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void stopThread() {
			this.isRun = false;
			interrupt();
		}
	}

}