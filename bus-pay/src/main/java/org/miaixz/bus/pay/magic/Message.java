package org.miaixz.bus.pay.magic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一授权响应类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * 请求返回码,错误为具体返回码,正确为 0
     */
    String errcode;

    /**
     * 请求返回消息
     */
    String errmsg;

    /**
     * 请求返回数据 JSON
     */
    Object data;

}
