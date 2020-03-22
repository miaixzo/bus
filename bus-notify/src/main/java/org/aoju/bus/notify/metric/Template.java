package org.aoju.bus.notify.metric;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息模版
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Setter
public class Template {

    /**
     * 发送者
     */
    protected String sender;

    /**
     * 接收者
     */
    protected String receive;

}
