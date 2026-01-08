/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.notify;

/**
 * Enumerates various notification service providers for registration and identification.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Registry {

    /**
     * Alibaba Cloud SMS service.
     */
    ALIYUN_SMS,
    /**
     * Alibaba Cloud Voice Messaging Service.
     */
    ALIYUN_VMS,
    /**
     * Alibaba Cloud Email Direct Mail service.
     */
    ALIYUN_EDM,
    /**
     * Baidu Cloud SMS service.
     */
    BAIDU_SMS,
    /**
     * Cloopen Cloud SMS service.
     */
    CLOOPEN_SMS,
    /**
     * China Telecom Cloud SMS service.
     */
    CTYUN_SMS,
    /**
     * DingTalk enterprise messaging service.
     */
    DINGTALK,
    /**
     * Emay Softcom SMS service.
     */
    EMAY_SMS,
    /**
     * Generic email service.
     */
    GENERIC_EDM,
    /**
     * Huawei Cloud SMS service.
     */
    HUAWEI_SMS,
    /**
     * JD Cloud SMS service.
     */
    JDCLOUD_SMS,
    /**
     * JPush SMS service.
     */
    JPUSH_SMS,
    /**
     * NetEase Cloud SMS service.
     */
    NETEASE_SMS,
    /**
     * Qiniu Cloud SMS service.
     */
    QINIU_SMS,
    /**
     * Tencent Cloud SMS service.
     */
    TENCENT_SMS,
    /**
     * Uni SMS service.
     */
    UNI_SMS,
    /**
     * UPYUN Cloud SMS service.
     */
    UPYUN_SMS,
    /**
     * WeChat Enterprise Account/WeCom messaging service.
     */
    WECHAT_CP,
    /**
     * WeChat Customer Service messaging service.
     */
    WECHAT_KF,
    /**
     * WeChat Mini Program subscription messages.
     */
    WECHAT_MINI,
    /**
     * WeChat Official Account subscription/template messages.
     */
    WECHAT_MP,
    /**
     * Yunpian SMS service.
     */
    YUNPIAN_SMS,
    /**
     * Zhutong SMS service.
     */
    ZHUTONG_SMS

}
