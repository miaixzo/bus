package org.aoju.bus.notify.provider.netease;

import lombok.Builder;
import lombok.Getter;
import org.aoju.bus.notify.metric.Template;

/**
 * 云信消息
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Builder
public class NeteaseMsgTemplate extends Template {

    String title;

    String message;

    String content;
}
