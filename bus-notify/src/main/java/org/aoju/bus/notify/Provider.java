package org.aoju.bus.notify;


import org.aoju.bus.notify.magic.Message;
import org.aoju.bus.notify.metric.Template;

import java.util.Map;

/**
 * 通知器,用于发送通知,如: 短信,邮件,语音,微信等s
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
public interface Provider<T extends Template> {

    /**
     * 指定模版ID进行发送.
     * 发送失败或者模版不存在将返回
     *
     * @param templateId 模版id
     * @param context    内容
     * @return 结果
     */
    Message send(String templateId, Map<String, String> context);

    /**
     * 指定模版{@link Template}并发送.
     * <p>
     * 注意:不同等服务商使用的模版实现不同.
     *
     * @param template 模版
     * @param context  内容
     * @return 结果
     */
    Message send(T template, Map<String, String> context);


}
