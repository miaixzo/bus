/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
/**
 * bus.pay
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.pay {

    requires java.xml;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.http;
    requires bus.logger;

    requires static lombok;
    requires static org.bouncycastle.provider;

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
