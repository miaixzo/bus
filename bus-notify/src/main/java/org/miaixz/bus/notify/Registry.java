/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.notify;

/**
 * 通知注册器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry {

    /**
     * 阿里云短信
     */
    ALIYUN_SMS,
    /**
     * 阿里云语音
     */
    ALIYUN_VMS,
    /**
     * 阿里云邮件
     */
    ALIYUN_EDM,
    /**
     * 阿里云语音
     */
    BAIDU_SMS,
    /**
     * 容连云
     */
    CLOOPEN_SMS,
    /**
     * 天翼云
     */
    CTYUN_SMS,
    /**
     * 企业钉钉
     */
    DINGTALK,
    /**
     * 亿美软通
     */
    EMAY_SMS,
    /**
     * 系统邮件
     */
    GENERIC_EDM,
    /**
     * 华为云短信
     */
    HUAWEI_SMS,
    /**
     * 京东云短信
     */
    JDCLOUD_SMS,
    /**
     * 极光短信
     */
    JPUSH_SMS,
    /**
     * 网易云短信
     */
    NETEASE_SMS,
    /**
     * 七牛云短信
     */
    QINIU_SMS,
    /**
     * 腾讯云短信
     */
    TENCENT_SMS,
    /**
     * 合一
     */
    UNI_SMS,
    /**
     * 又拍云短信
     */
    UPYUN_SMS,
    /**
     * 微信企业号/企业微信消息
     */
    WECHAT_CP,
    /**
     * 微信客服消息
     */
    WECHAT_KF,
    /**
     * 微信小程序-订阅消息
     */
    WECHAT_MINI,
    /**
     * 微信公众号-订阅/模板消息
     */
    WECHAT_MP,
    /**
     * 云片短信
     */
    YUNPIAN_SMS,
    /**
     * 助通
     */
    ZHUTONG_SMS

}
