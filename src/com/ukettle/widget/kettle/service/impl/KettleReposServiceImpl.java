package com.ukettle.widget.kettle.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.ukettle.basics.shiro.entity.Shiro;
import com.ukettle.engine.kettle.KettleEnvironment;
import com.ukettle.engine.loader.BasicMapper;
import com.ukettle.engine.loader.BasicService;
import com.ukettle.widget.kettle.entity.KettleRepos;
import com.ukettle.widget.kettle.entity.KettleTree;
import com.ukettle.widget.kettle.service.KettleReposService;
import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.toolkit.DateUtils;
import com.ukettle.www.toolkit.ObjectID;

@Service
public class KettleReposServiceImpl<T extends KettleRepos> implements
		KettleReposService<T> {

	@Autowired
	private BasicMapper mapper;
	@Autowired
	private BasicService service;

	private Repository repo;
	private List<Repository> list;

	@Override
	public String insert(T entity) throws DataAccessException {
		if (null != entity) {
			String id = Shiro.get().getId();
			String date = DateUtils.getTime24();
			entity.setId(ObjectID.id());
			entity.setCreator(id);
			entity.setCreated(date);
			entity.setModifier(id);
			if (mapper.iKettleReposMapper.insert(entity) > 0) {
				return entity.getId();
			}
		}
		return entity.getId();
	}

	@Override
	public String update(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setModified(DateUtils.getTime24());
			if (mapper.iKettleReposMapper.update(entity) > 0) {
				return entity.getId();
			}
		}
		return entity.getId();
	}

	@Override
	public String delete(String id) throws DataAccessException {
		if (null != id && !"".equals(id)) {
			if (mapper.iKettleReposMapper.delete(id) > 0) {
				return id;
			}
		}
		return id;
	}

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleReposMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iKettleReposMapper.selectByWhere(entity);
		}
		return null;
	}

	@Override
	public Object get(T entity) throws KettleException {
		if (null != entity && null != entity.getName()) {
			if (null != repo && null != repo.getName()
					&& entity.getName().equals(repo.getName())) {
				connect(entity.getName());
				return repo;
			} else {
				if (null == list || list.size() < 1) {
					init();
				}
				for (Repository repo : list) {
					if (null != repo.getName()
							&& entity.getName().equals(repo.getName())) {
						this.repo = repo;
						connect(entity.getName());
						return this.repo;
					}
				}
			}
			throw new KettleException("repository is not found ...");
		}
		return repo;
	}

	@SuppressWarnings("all")
	@Override
	public Object loading(T entity) throws KettleException {
		if (null == entity) {
			entity = (T) new KettleRepos();
		}
		entity.setStatus(Constant.STATUS_ENABLED);
		List<T> isList = (List<T>) mapper.iKettleReposMapper
				.selectByWhere(entity);
		if (null != isList && isList.size() > 0) {
			// 运行环境初始化（设置主目录、注册必须的插件等）
			KettleEnvironment.init();
			list = new ArrayList<Repository>();
			for (T repos : isList) {
				initRepository(repos, false);
			}
		}
		return list;
	}

	@Override
	public Object discard(T entity) throws KettleException {
		if (entity != null && null != entity.getName()) {
			if (null != selectByWhere(entity)) {
				repo.disconnect();
				repo.clearSharedObjectCache();
			}
		}
		return entity.getName();
	}

	@SuppressWarnings("all")
	@Override
	public Object getParameters(T entity) throws KettleException {
		if (null != entity) {
			if (null != get(entity)) {
				if (Constant.TYPE_JOB.equals(entity.getType())) {
					return service.iKettleJobService.getParameters(entity);
				} else if (Constant.TYPE_TRANS.equals(entity.getType())) {
					return service.iKettleTransService.getParameters(entity);
				}
			}
		}
		return null;
	}

	@Override
	public List<?> getJobAndTrans(T entity) throws KettleException {
		List<KettleTree> list = new ArrayList<KettleTree>();
		if (null != get(entity)) {
			RepositoryDirectoryInterface rdi = repo
					.loadRepositoryDirectoryTree().findDirectory(
							entity.getDir());
			getDirectory(rdi, list);
			ObjectId id = new StringObjectId(entity.getId());
			List<RepositoryElementMetaInterface> li = repo
					.getJobAndTransformationObjects(id, false);
			if (null != li) {
				for (RepositoryElementMetaInterface repel : li) {
					if (Constant.TYPE_JOB.equals(repel.getObjectType()
							.toString())
							|| Constant.TYPE_TRANS.equals(repel.getObjectType()
									.toString())) {
						KettleTree c = new KettleTree();
						c.setId(id + repel.getObjectId().toString());
						c.setpId(id.toString());
						c.setName(repel.getName());
						c.setAlt(repel.getObjectType().toString());
						c.setaId(repel.getObjectId().toString());
						c.setClick("true");
						c.setOpen("false");
						c.setIsParent("false");
						c.setDir(rdi.getPath());
						c.setType(repel.getObjectType().toString());
						list.add(c);
					}
				}
			}
		}
		return list;
	}

	/**
	 * 容器启动初始化Kettle资源库
	 * 
	 * @throws KettleException
	 *             返回相关异常
	 */
	@PostConstruct
	public void init() throws KettleException {
		loading(null);
	}

	/**
	 * 获取资源库所有目录信息
	 * 
	 * @param rdi
	 * @param list
	 */
	private List<?> getDirectory(RepositoryDirectoryInterface rdi,
			List<KettleTree> list) {
		try {
			RepositoryDirectoryInterface tree = repo
					.loadRepositoryDirectoryTree().findDirectory(
							rdi.getObjectId());
			for (int i = 0; i < rdi.getNrSubdirectories(); i++) {
				RepositoryDirectory subTree = tree.getSubdirectory(i);
				KettleTree d = new KettleTree();
				d.setId(subTree.getObjectId().toString());
				d.setpId(rdi.getObjectId().toString());
				d.setName(subTree.getName());
				d.setDir(subTree.getPath());
				d.setaId(subTree.getObjectId().toString());
				d.setClick("false");
				d.setOpen("false");
				d.setIsParent("true");
				d.setType("dir");
				list.add(d);
			}
		} catch (Exception e) {
		}
		return list;
	}

	/**
	 * 初始化资源库
	 * 
	 * @param repo
	 *            资源库
	 * @param isConnected
	 *            (是否重新连接)
	 */
	private void initRepository(T entity, boolean isConnected)
			throws KettleException {
		if (null != entity) {
			RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
			DatabaseMeta db = new DatabaseMeta();
			db.setDatabaseType(entity.getDialect());
			db.setAccessType(Integer.parseInt(entity.getAccess()));
			db.setDBName(entity.getDb());
			db.setHostname(entity.getServer());
			db.setDBPort(entity.getPort());
			db.setUsername(entity.getUsername());
			db.setPassword(entity.getPassword());
			KettleDatabaseRepositoryMeta rmeta = new KettleDatabaseRepositoryMeta();
			rmeta.setConnection(db);
			rmeta.setId(entity.getType());
			rmeta.setName(entity.getName());
			repositoriesMeta.addDatabase(db);
			repositoriesMeta.addRepository(rmeta);
			// 选择加载过的资源库
			RepositoryMeta meta = repositoriesMeta.findRepository(entity
					.getName());
			// 获得资源库实例
			repo = PluginRegistry.getInstance().loadClass(
					RepositoryPluginType.class, meta.getId(), Repository.class);
			repo.init(meta);
			// 连接资源库
			repo.connect(entity.getUser(), entity.getPass());
			if (!isConnected) {
				list.add(repo);
			}
		}
	}

	/**
	 * 连接资源库
	 * 
	 * @param name
	 */
	@SuppressWarnings("all")
	private void connect(String name) throws KettleException {
		if (!repo.isConnected()) {
			T entity = (T) new KettleRepos();
			entity.setName(name);
			entity = (T) mapper.iKettleReposMapper.select(entity);
			initRepository(entity, true);
		}
	}

}