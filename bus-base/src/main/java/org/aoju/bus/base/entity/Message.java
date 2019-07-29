package org.aoju.bus.base.entity;

import lombok.Data;

/**
 * <p>
 * 返回值公用类
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
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

    public Message(String errcode, String errmsg) {
        this.errmsg = errmsg;
        this.errcode = errcode;
    }

    public Message(String errcode, String errmsg, Object data) {
        this.errmsg = errmsg;
        this.errcode = errcode;
        this.data = data;
    }

}
