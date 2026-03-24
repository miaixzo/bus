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
 * bus.pay
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
module bus.pay {

    requires java.xml;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.http;
    requires bus.logger;

    requires lombok;
    requires org.bouncycastle.provider;

    exports org.miaixz.bus.pay;
    exports org.miaixz.bus.pay.cache;
    exports org.miaixz.bus.pay.magic;
    exports org.miaixz.bus.pay.metric;
    exports org.miaixz.bus.pay.metric.alipay;
    exports org.miaixz.bus.pay.metric.alipay.api;
    exports org.miaixz.bus.pay.metric.jdpay;
    exports org.miaixz.bus.pay.metric.jdpay.api;
    exports org.miaixz.bus.pay.metric.jdpay.entity;
    exports org.miaixz.bus.pay.metric.paypal;
    exports org.miaixz.bus.pay.metric.paypal.api;
    exports org.miaixz.bus.pay.metric.paypal.entity;
    exports org.miaixz.bus.pay.metric.tenpay;
    exports org.miaixz.bus.pay.metric.tenpay.api;
    exports org.miaixz.bus.pay.metric.tenpay.entity;
    exports org.miaixz.bus.pay.metric.unionpay;
    exports org.miaixz.bus.pay.metric.unionpay.api;
    exports org.miaixz.bus.pay.metric.unionpay.entity;
    exports org.miaixz.bus.pay.metric.wechat;
    exports org.miaixz.bus.pay.metric.wechat.api;
    exports org.miaixz.bus.pay.metric.wechat.api.v2;
    exports org.miaixz.bus.pay.metric.wechat.api.v3;
    exports org.miaixz.bus.pay.metric.wechat.entity;
    exports org.miaixz.bus.pay.metric.wechat.entity.v2;
    exports org.miaixz.bus.pay.metric.wechat.entity.v3;

}
