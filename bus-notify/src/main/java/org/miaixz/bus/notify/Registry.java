/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
