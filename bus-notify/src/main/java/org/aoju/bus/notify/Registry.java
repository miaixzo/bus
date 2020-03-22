package org.aoju.bus.notify;


/**
 * 通知注册器
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public enum Registry {

    /**
     * 阿里云短信
     */
    ALIYUN_SMS,

    /**
     * 阿里云语音通知
     */
    ALIYUN_VMS,

    /**
     * 钉钉企业通知
     */
    DINGTALK_CORP_MSG,

    /**
     * 云信通知
     */
    NETEASE_ATTACH_MSG,
    /**
     * 云信普通消息
     */
    NETEASE_MSG;

}
