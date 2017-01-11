package org.ukettle.service.execute;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.ukettle.engine.loader.BasicService;
import org.ukettle.widget.kettle.entity.KettleResult;
import org.ukettle.widget.kettle.entity.KettleSpoon;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.DateUtils;
import org.ukettle.www.toolkit.JSONUtil;

/**
 * <p>
 * 简单的实现了Spring QuartzJobBean接口
 * </p>
 * 
 * @author Kimi Liu
 * @Date Oct 18, 2014
 * @Time 10:21:03
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class AgainExecService extends QuartzJobBean {

	@Autowired
	private BasicService service;

	/**
	 * 执行实际任务
	 * 
	 * @see execute
	 */
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		KettleResult result = new KettleResult();
		for (Object key : context.getJobDetail().getJobDataMap().keySet()) {
			String strVal = context.getJobDetail().getJobDataMap().get(key)
					.toString();
			// 获取定时任务中的所有参数
			if (strVal.startsWith(Constant.STARTS_WITH_USD)) {
				int minute = Integer.parseInt(strVal.split("\\"
						+ Constant.STARTS_WITH_USD)[1]);
				strVal = DateUtils.isAssign(null, minute);
			}
			result.setValue(key.toString(), strVal);
		}
		@SuppressWarnings("all")
		List<KettleResult> list = (List<KettleResult>) service.iKettleResultService
				.selectByWhere(result);
		if (!list.isEmpty()) {
			for (KettleResult r : list) {
				String json = r.getParams();
				if (!Constant.STATUS_ERROR.equals(r.getStatus())) {
					json = json.substring(1, json.length() - 1);
				}
				Map<?, ?> map = JSONUtil.getMapFromJson(json);
				final KettleSpoon entity = new KettleSpoon();
				Iterator<?> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<?, ?> ent = (Entry<?, ?>) it.next();
					String key = (String) ent.getKey();
					String val = (String) ent.getValue();
					if (!Constant.STATUS_ERROR.equals(r.getStatus())) {
						if (key.indexOf("start_") != -1) {
							val = r.getStartTime();
						}
						if (key.indexOf("end_") != -1) {
							val = r.getEndTime();
						}
						if (key.indexOf("nick") != -1) {
							val = r.getNick();
						}
					}
					if (null != val && !"".equals(val)) {
						entity.setValue(key, val);
					}
				}
				entity.setRid(r.getId());
				entity.setTid(entity.getName());
				entity.setQueue(false);
				service.iKettleSpoonService.execute(entity);
			}
		}
	}

}