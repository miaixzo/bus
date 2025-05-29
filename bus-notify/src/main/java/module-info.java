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