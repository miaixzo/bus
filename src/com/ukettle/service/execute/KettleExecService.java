package com.ukettle.service.execute;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.ukettle.engine.loader.BasicService;
import com.ukettle.widget.kettle.entity.KettleSpoon;
import com.ukettle.www.toolkit.Constant;
import com.ukettle.www.toolkit.DateUtils;

/**
 * <p>
 * 简单的实现了Spring QuartzJobBean接口
 * </p>
 * 
 * @author Kimi Liu
 * @Date Mar 12, 2014
 * @Time 10:27:31
 * @email 839536@QQ.com
 * @version 1.0
 * @since JDK 1.6
 */
public class KettleExecService extends QuartzJobBean {

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
		KettleSpoon entity = new KettleSpoon();
		// 获取定时任务中的所有参数
		for (Object key : context.getJobDetail().getJobDataMap().keySet()) {
			String strVal = context.getJobDetail().getJobDataMap().get(key)
					.toString();
			// 根据定时任务参数封装Kettle所需参数
			setValue(key.toString(), strVal, entity);
		}
		entity.setTid(context.getJobDetail().getKey().toString());
		execute(entity);
	}

	/**
	 * 执行Kettle定时任务
	 */
	private void execute(KettleSpoon entity) {
		if (null != entity && !"".equals(entity)) {
			service.iKettleSpoonService.execute((KettleSpoon) entity);
		}
	}

	/**
	 * 封装Kettle定时任务等先关参数
	 */
	private void setValue(String key, String strVal, KettleSpoon entity) {
		if (null != strVal) {
			if (strVal.startsWith(Constant.STARTS_WITH_USD)) {
				int minute = Integer.parseInt(strVal.split("\\"
						+ Constant.STARTS_WITH_USD)[1]);
				strVal = DateUtils.isAssign(null, minute);
				// 判断是否为复合参数以"-param:"开始
			} else if (strVal.startsWith(Constant.STARTS_WITH_PARAM)) {
				String[] array = strVal.split(Constant.SPLIT_PARAM);
				for (int i = 1; i < array.length; i++) {
					// 分割字符，获取参数、参数值
					String[] val = array[i].split(Constant.SPLIT_EQUAL);
					if (val[1].startsWith(Constant.STARTS_WITH_USD)) {
						// 分割字符，判断是否为自定义日期
						int minute = Integer.parseInt(val[1].split("\\"
								+ Constant.STARTS_WITH_USD)[1]
								.replace("@@", ""));
						String date = DateUtils.isAssign(null, minute);
						if (i + 1 != array.length) {
							date = date + "@@";
						}
						// 重新封装日期参数
						strVal = strVal.replace(val[1], date);
					}
				}
			}
			entity.setValue(key, strVal);
		}
	}

}