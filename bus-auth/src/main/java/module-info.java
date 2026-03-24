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
 * bus.auth
 * 
 * @author Kimi Liu
 * @since Java 21+
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

    exports org.miaixz.bus.auth;
    exports org.miaixz.bus.auth.cache;
    exports org.miaixz.bus.auth.magic;
    exports org.miaixz.bus.auth.metric;
    exports org.miaixz.bus.auth.nimble;
    exports org.miaixz.bus.auth.metric.jwt;
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
    exports org.miaixz.bus.auth.nimble.oidc;
    exports org.miaixz.bus.auth.nimble.okta;
    exports org.miaixz.bus.auth.nimble.oschina;
    exports org.miaixz.bus.auth.nimble.pinterest;
    exports org.miaixz.bus.auth.nimble.proginn;
    exports org.miaixz.bus.auth.nimble.qq;
    exports org.miaixz.bus.auth.nimble.rednote;
    exports org.miaixz.bus.auth.nimble.renren;
    exports org.miaixz.bus.auth.nimble.router;
    exports org.miaixz.bus.auth.nimble.slack;
    exports org.miaixz.bus.auth.nimble.stackoverflow;
    exports org.miaixz.bus.auth.nimble.taobao;
    exports org.miaixz.bus.auth.nimble.teambition;
    exports org.miaixz.bus.auth.nimble.toutiao;
    exports org.miaixz.bus.auth.nimble.twitter;
    exports org.miaixz.bus.auth.nimble.vk;
    exports org.miaixz.bus.auth.nimble.wechat;
    exports org.miaixz.bus.auth.nimble.wechat.ee;
    exports org.miaixz.bus.auth.nimble.wechat.mini;
    exports org.miaixz.bus.auth.nimble.wechat.mp;
    exports org.miaixz.bus.auth.nimble.wechat.open;
    exports org.miaixz.bus.auth.nimble.weibo;
    exports org.miaixz.bus.auth.nimble.ximalaya;

}
