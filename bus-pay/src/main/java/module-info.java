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
    requires bus.fabric;
    requires bus.logger;

    requires static lombok;
    requires static org.bouncycastle.provider;

    exports org.miaixz.bus.pay;
    exports org.miaixz.bus.pay.cache;
    exports org.miaixz.bus.pay.magic;
    exports org.miaixz.bus.pay.nimble;
    exports org.miaixz.bus.pay.nimble.alipay;
    exports org.miaixz.bus.pay.nimble.alipay.api;
    exports org.miaixz.bus.pay.nimble.jdpay;
    exports org.miaixz.bus.pay.nimble.jdpay.api;
    exports org.miaixz.bus.pay.nimble.jdpay.entity;
    exports org.miaixz.bus.pay.nimble.paypal;
    exports org.miaixz.bus.pay.nimble.paypal.api;
    exports org.miaixz.bus.pay.nimble.paypal.entity;
    exports org.miaixz.bus.pay.nimble.tenpay;
    exports org.miaixz.bus.pay.nimble.tenpay.api;
    exports org.miaixz.bus.pay.nimble.tenpay.entity;
    exports org.miaixz.bus.pay.nimble.unionpay;
    exports org.miaixz.bus.pay.nimble.unionpay.api;
    exports org.miaixz.bus.pay.nimble.unionpay.entity;
    exports org.miaixz.bus.pay.nimble.wechat;
    exports org.miaixz.bus.pay.nimble.wechat.api;
    exports org.miaixz.bus.pay.nimble.wechat.api.v2;
    exports org.miaixz.bus.pay.nimble.wechat.api.v3;
    exports org.miaixz.bus.pay.nimble.wechat.entity;
    exports org.miaixz.bus.pay.nimble.wechat.entity.v2;
    exports org.miaixz.bus.pay.nimble.wechat.entity.v3;

}
