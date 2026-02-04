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
/**
 * bus.notify
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.notify {

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.logger;
    requires bus.extra;
    requires bus.http;

    requires lombok;
    requires jakarta.activation;
    requires jakarta.mail;

    exports org.miaixz.bus.notify;
    exports org.miaixz.bus.notify.cache;
    exports org.miaixz.bus.notify.magic;
    exports org.miaixz.bus.notify.metric;
    exports org.miaixz.bus.notify.metric.aliyun;
    exports org.miaixz.bus.notify.metric.baidu;
    exports org.miaixz.bus.notify.metric.cloopen;
    exports org.miaixz.bus.notify.metric.ctyun;
    exports org.miaixz.bus.notify.metric.dingtalk;
    exports org.miaixz.bus.notify.metric.emay;
    exports org.miaixz.bus.notify.metric.generic;
    exports org.miaixz.bus.notify.metric.huawei;
    exports org.miaixz.bus.notify.metric.jdcloud;
    exports org.miaixz.bus.notify.metric.jpush;
    exports org.miaixz.bus.notify.metric.netease;
    exports org.miaixz.bus.notify.metric.qiniu;
    exports org.miaixz.bus.notify.metric.tencent;
    exports org.miaixz.bus.notify.metric.unisms;
    exports org.miaixz.bus.notify.metric.upyun;
    exports org.miaixz.bus.notify.metric.wechat;
    exports org.miaixz.bus.notify.metric.yunpian;
    exports org.miaixz.bus.notify.metric.zhutong;

}
