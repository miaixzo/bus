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
 * bus.oauth
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.oauth {

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.http;
    requires bus.logger;

    requires lombok;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;

    exports org.miaixz.bus.oauth;
    exports org.miaixz.bus.oauth.cache;
    exports org.miaixz.bus.oauth.magic;
    exports org.miaixz.bus.oauth.metric;
    exports org.miaixz.bus.oauth.metric.afdian;
    exports org.miaixz.bus.oauth.metric.alipay;
    exports org.miaixz.bus.oauth.metric.aliyun;
    exports org.miaixz.bus.oauth.metric.amazon;
    exports org.miaixz.bus.oauth.metric.apple;
    exports org.miaixz.bus.oauth.metric.baidu;
    exports org.miaixz.bus.oauth.metric.coding;
    exports org.miaixz.bus.oauth.metric.dingtalk;
    exports org.miaixz.bus.oauth.metric.douyin;
    exports org.miaixz.bus.oauth.metric.eleme;
    exports org.miaixz.bus.oauth.metric.facebook;
    exports org.miaixz.bus.oauth.metric.feishu;
    exports org.miaixz.bus.oauth.metric.figma;
    exports org.miaixz.bus.oauth.metric.gitee;
    exports org.miaixz.bus.oauth.metric.github;
    exports org.miaixz.bus.oauth.metric.gitlab;
    exports org.miaixz.bus.oauth.metric.google;
    exports org.miaixz.bus.oauth.metric.huawei;
    exports org.miaixz.bus.oauth.metric.jd;
    exports org.miaixz.bus.oauth.metric.kujiale;
    exports org.miaixz.bus.oauth.metric.line;
    exports org.miaixz.bus.oauth.metric.linkedin;
    exports org.miaixz.bus.oauth.metric.meituan;
    exports org.miaixz.bus.oauth.metric.mi;
    exports org.miaixz.bus.oauth.metric.microsoft;
    exports org.miaixz.bus.oauth.metric.okta;
    exports org.miaixz.bus.oauth.metric.oschina;
    exports org.miaixz.bus.oauth.metric.pinterest;
    exports org.miaixz.bus.oauth.metric.proginn;
    exports org.miaixz.bus.oauth.metric.qq;
    exports org.miaixz.bus.oauth.metric.renren;
    exports org.miaixz.bus.oauth.metric.slack;
    exports org.miaixz.bus.oauth.metric.stackoverflow;
    exports org.miaixz.bus.oauth.metric.taobao;
    exports org.miaixz.bus.oauth.metric.teambition;
    exports org.miaixz.bus.oauth.metric.toutiao;
    exports org.miaixz.bus.oauth.metric.twitter;
    exports org.miaixz.bus.oauth.metric.wechat;
    exports org.miaixz.bus.oauth.metric.wechat.ee;
    exports org.miaixz.bus.oauth.metric.wechat.mini;
    exports org.miaixz.bus.oauth.metric.wechat.mp;
    exports org.miaixz.bus.oauth.metric.wechat.open;
    exports org.miaixz.bus.oauth.metric.weibo;
    exports org.miaixz.bus.oauth.metric.ximalaya;

}