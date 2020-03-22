package org.aoju.bus.notify.magic;

import lombok.Builder;
import lombok.Getter;

/**
 * 返回消息
 *
 * @author Justubborn
 * @version 5.6.9
 * @since JDK1.8+
 */
@Getter
@Builder
public class Message {

    /**
     * 结果
     */
    boolean result;

    /**
     * 描述
     */
    String desc;
}
