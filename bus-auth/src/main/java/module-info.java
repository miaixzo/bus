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
 * bus.auth
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.auth {

    requires java.naming;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.http;
    requires bus.logger;

    requires lombok;
    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;
    requires io.vertx.core;
    requires io.vertx.web;
    requires io.vertx.codegen.api;
    requires io.vertx.auth.common;
    requires io.vertx.config;
    requires io.vertx.sql.client;
    requires io.vertx.sql.client.pg;
    requires io.vertx.web.client;
    requires io.vertx.mail.client;

    exports org.miaixz.bus.auth;
    exports org.miaixz.bus.auth.cache;
    exports org.miaixz.bus.auth.magic;
    exports org.miaixz.bus.auth.metric;
    exports org.miaixz.bus.auth.nimble;
    exports org.miaixz.bus.auth.nimble.afdian;
    exports org.miaixz.bus.auth.nimble.alipay;
    exports org.miaixz.bus.auth.nimble.aliyun;
    exports org.miaixz.bus.auth.nimble.amazon;
    exports org.miaixz.bus.auth.nimble.apple;
    exports org.miaixz.bus.auth.nimble.baidu;
    exports org.miaixz.bus.auth.nimble.coding;
    exports org.miaixz.bus.auth.nimble.dingtalk;
    exports org.miaixz.bus.auth.nimble.douyin;
    exports org.miaixz.bus.auth.nimble.eleme;
    exports org.miaixz.bus.auth.nimble.facebook;
    exports org.miaixz.bus.auth.nimble.feishu;
    exports org.miaixz.bus.auth.nimble.figma;
    exports org.miaixz.bus.auth.nimble.gitee;
    exports org.miaixz.bus.auth.nimble.github;
    exports org.miaixz.bus.auth.nimble.gitlab;
    exports org.miaixz.bus.auth.nimble.google;
    exports org.miaixz.bus.auth.nimble.huawei;
    exports org.miaixz.bus.auth.nimble.jd;
    exports org.miaixz.bus.auth.nimble.kujiale;
    exports org.miaixz.bus.auth.nimble.line;
    exports org.miaixz.bus.auth.nimble.linkedin;
    exports org.miaixz.bus.auth.nimble.meituan;
    exports org.miaixz.bus.auth.nimble.mi;
    exports org.miaixz.bus.auth.nimble.microsoft;
    exports org.miaixz.bus.auth.nimble.okta;
    exports org.miaixz.bus.auth.nimble.oschina;
    exports org.miaixz.bus.auth.nimble.pinterest;
    exports org.miaixz.bus.auth.nimble.proginn;
    exports org.miaixz.bus.auth.nimble.qq;
    exports org.miaixz.bus.auth.nimble.renren;
    exports org.miaixz.bus.auth.nimble.slack;
    exports org.miaixz.bus.auth.nimble.stackoverflow;
    exports org.miaixz.bus.auth.nimble.taobao;
    exports org.miaixz.bus.auth.nimble.teambition;
    exports org.miaixz.bus.auth.nimble.toutiao;
    exports org.miaixz.bus.auth.nimble.twitter;
    exports org.miaixz.bus.auth.nimble.wechat;
    exports org.miaixz.bus.auth.nimble.wechat.ee;
    exports org.miaixz.bus.auth.nimble.wechat.mini;
    exports org.miaixz.bus.auth.nimble.wechat.mp;
    exports org.miaixz.bus.auth.nimble.wechat.open;
    exports org.miaixz.bus.auth.nimble.weibo;
    exports org.miaixz.bus.auth.nimble.ximalaya;

}