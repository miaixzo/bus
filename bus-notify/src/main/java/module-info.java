/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * bus.notify
 *
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.notify {

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.fabric;
    requires bus.logger;

    requires static lombok;
    requires static jakarta.activation;
    requires static jakarta.mail;

    exports org.miaixz.bus.notify;
    exports org.miaixz.bus.notify.cache;
    exports org.miaixz.bus.notify.magic;
    exports org.miaixz.bus.notify.nimble;
    exports org.miaixz.bus.notify.nimble.aliyun;
    exports org.miaixz.bus.notify.nimble.baidu;
    exports org.miaixz.bus.notify.nimble.cloopen;
    exports org.miaixz.bus.notify.nimble.ctyun;
    exports org.miaixz.bus.notify.nimble.dingtalk;
    exports org.miaixz.bus.notify.nimble.emay;
    exports org.miaixz.bus.notify.nimble.generic;
    exports org.miaixz.bus.notify.nimble.huawei;
    exports org.miaixz.bus.notify.nimble.jdcloud;
    exports org.miaixz.bus.notify.nimble.jpush;
    exports org.miaixz.bus.notify.nimble.netease;
    exports org.miaixz.bus.notify.nimble.qiniu;
    exports org.miaixz.bus.notify.nimble.tencent;
    exports org.miaixz.bus.notify.nimble.unisms;
    exports org.miaixz.bus.notify.nimble.upyun;
    exports org.miaixz.bus.notify.nimble.wechat;
    exports org.miaixz.bus.notify.nimble.yunpian;
    exports org.miaixz.bus.notify.nimble.zhutong;

}
