package org.ukettle.service.router.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.ukettle.engine.loader.BasicMapper;
import org.ukettle.service.router.entity.Message;
import org.ukettle.service.router.entity.Router;
import org.ukettle.service.router.service.RouterService;
import org.ukettle.www.serialize.FileSerializer;
import org.ukettle.www.serialize.ObjectSerializer;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.DateUtils;

@Service
public class RouterServiceImpl<T extends Router> implements RouterService<T> {

	@Autowired
	private BasicMapper mapper;

	@Override
	public Object select(T entity) throws DataAccessException {
		if (null != entity) {
			entity.setStatus(Constant.STATUS_ENABLED);
			return mapper.iRouterMapper.select(entity);
		}
		return null;
	}

	@Override
	public List<?> selectByWhere(T entity) throws DataAccessException {
		if (null != entity) {
			return mapper.iRouterMapper.selectByWhere(entity);
		}
		return null;
	}

	@Override
	public void on(T entity) {
		try {
			if (entity.getResponse().isCommitted()) {
				entity.getResponse().setStatus(
						HttpURLConnection.HTTP_NO_CONTENT);
				entity.getResponse().getWriter().flush();
			} else {
				if (isValid(entity)) {

				} else {
					write(entity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void forward(T entity) {
		try {
			RequestDispatcher dispatcher = entity.getRequest()
					.getRequestDispatcher("");
			try {
				dispatcher.forward(entity.getRequest(), entity.getResponse());
			} catch (ServletException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {

		}
	}

	@Override
	public boolean isValid(T entity) {
		// 访问权限验证
		if (!access(entity)) {
			return false;
		}
		// 系统级别参数验证
		if (!system(entity)) {
			return false;
		}
		// API级别参数验证
		if (!api(entity)) {
			return false;
		}
		return true;
	}

	private boolean access(T router) {
		return true;
	}

	private boolean system(T router) {
		return true;
	}

	private boolean api(T entity) {
		if (null == entity || "".equals(entity)) {
			return false;
		}
		if (null == entity.getQuery() || "".equals(entity.getQuery())) {
			entity.setMessage(new Message(Message.Error.API_MISSING_PARAMS));
			return false;
		}
		if (null == entity.getId() || "".equals(entity.getId())) {
			entity.setMessage(new Message(Message.Error.API_MISSING_ID));
			return false;
		}
		if (null == entity.getTimestamp() || "".equals(entity.getTimestamp())) {
			entity.setMessage(new Message(Message.Error.API_MISSING_TIMESTAMP));
			return false;
		}
		if (!DateUtils.compareWithNow(entity.getTimestamp())) {
			entity.setMessage(new Message(Message.Error.API_INVALID_TIMESTAMP));
			return false;
		}
		if (null == entity.getSignature() || "".equals(entity.getSignature())) {
			entity.setMessage(new Message(Message.Error.API_MISSING_SIGNATURE));
			return false;
		}
		return true;
	}

	private void write(T entity) throws IOException {
		ObjectSerializer write = (ObjectSerializer) entity.getClazz();
		String serialized = write.serialize(entity.getMessage());
		write(entity, serialized, write.getContentType());
	}

	private void write(T entity, Object content, String contentType)
			throws IOException {
		entity.getResponse().setHeader("Content-Transfer-Encoding", "binary");
		entity.getResponse().setContentType(contentType);
		entity.getResponse().setCharacterEncoding(Constant.DEFAULT_ENCODING);
		entity.getResponse().getWriter()
				.write(append(entity, content.toString()));
		entity.getResponse().getWriter().flush();
		entity.getResponse().getWriter().close();
	}

	private String append(T entity, String content) {
		StringBuilder strVal = new StringBuilder();
		strVal.append(content);
		if (Constant.FORMAT_XML.equals(entity.getFormat())) {
			strVal.append("\n<!--api.value3.com:" + DateUtils.getMillis()
					+ "-->");
		}
		return strVal.toString();
	}

	public void writeFile(T entity) throws IOException {
		FileSerializer write = (FileSerializer) entity.getClazz();
		write(entity, write);
	}

	private void write(T entity, FileSerializer writer) throws IOException {
		if (null == entity.getContentType()) {
			entity.getResponse().setContentType(writer.getContentType());
		} else {
			entity.getResponse().setContentType(entity.getContentType());
		}
		if (null == entity.getAttachment()) {
			entity.setAttachment(entity.getFile().getName());
		}
		entity.getResponse().setHeader("Content-Transfer-Encoding", "binary");
		entity.getResponse().setHeader("Content-Disposition",
				"attachment; filename=" + entity.getAttachment());
		entity.getResponse().setContentLength((int) entity.getFile().length());
		writer.sendFile(entity.getFile(), entity.getResponse()
				.getOutputStream());
	}

}