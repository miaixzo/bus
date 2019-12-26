package org.aoju.bus.socket.spring.support;

import lombok.Data;

/**
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
@Data
public class MessageFrom {

    private String type; // 消息类型
    private String subId; // 订阅id
    private String destination; // 主题
    private Integer contentLength; // 内容长度
    private String content; // 内容

}
