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
 * Module: {@code bus.auth}
 *
 * <p>
 * Provides authentication, authorization, and federated identity integrations.
 *
 * <p>
 * Includes protocol-neutral contracts, JWT handling, OAuth 2.0 and OpenID Connect flows, LDAP, RADIUS, SCIM, Shared
 * Signals Framework support, credential resolution, runtime services, and third-party identity providers.
 *
 * @author Kimi Liu
 */
module bus.auth {

    requires java.naming;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.fabric;
    requires bus.logger;

    requires static lombok;
    requires static org.bouncycastle.pkix;
    requires static org.bouncycastle.provider;

    exports org.miaixz.bus.auth;

    exports org.miaixz.bus.auth.cache;
    exports org.miaixz.bus.auth.bridge;
    exports org.miaixz.bus.auth.protocol;
    exports org.miaixz.bus.auth.protocol.jwt;
    exports org.miaixz.bus.auth.protocol.jwt.signature;
    exports org.miaixz.bus.auth.protocol.oauth2;
    exports org.miaixz.bus.auth.protocol.oidc;
    exports org.miaixz.bus.auth.protocol.ldap;
    exports org.miaixz.bus.auth.protocol.radius;
    exports org.miaixz.bus.auth.protocol.scim;
    exports org.miaixz.bus.auth.protocol.ssf;
    exports org.miaixz.bus.auth.registry;
    exports org.miaixz.bus.auth.resolver;
    exports org.miaixz.bus.auth.runtime;
    exports org.miaixz.bus.auth.vendor;
    exports org.miaixz.bus.auth.vendor.afdian;
    exports org.miaixz.bus.auth.vendor.alipay;
    exports org.miaixz.bus.auth.vendor.aliyun;
    exports org.miaixz.bus.auth.vendor.amazon;
    exports org.miaixz.bus.auth.vendor.apple;
    exports org.miaixz.bus.auth.vendor.baidu;
    exports org.miaixz.bus.auth.vendor.coding;
    exports org.miaixz.bus.auth.vendor.dingtalk;
    exports org.miaixz.bus.auth.vendor.douyin;
    exports org.miaixz.bus.auth.vendor.eleme;
    exports org.miaixz.bus.auth.vendor.facebook;
    exports org.miaixz.bus.auth.vendor.feishu;
    exports org.miaixz.bus.auth.vendor.figma;
    exports org.miaixz.bus.auth.vendor.gitee;
    exports org.miaixz.bus.auth.vendor.github;
    exports org.miaixz.bus.auth.vendor.gitlab;
    exports org.miaixz.bus.auth.vendor.google;
    exports org.miaixz.bus.auth.vendor.huawei;
    exports org.miaixz.bus.auth.vendor.jd;
    exports org.miaixz.bus.auth.vendor.kujiale;
    exports org.miaixz.bus.auth.vendor.line;
    exports org.miaixz.bus.auth.vendor.linkedin;
    exports org.miaixz.bus.auth.vendor.meituan;
    exports org.miaixz.bus.auth.vendor.mi;
    exports org.miaixz.bus.auth.vendor.microsoft;
    exports org.miaixz.bus.auth.vendor.oidc;
    exports org.miaixz.bus.auth.vendor.okta;
    exports org.miaixz.bus.auth.vendor.oschina;
    exports org.miaixz.bus.auth.vendor.pinterest;
    exports org.miaixz.bus.auth.vendor.proginn;
    exports org.miaixz.bus.auth.vendor.qq;
    exports org.miaixz.bus.auth.vendor.rednote;
    exports org.miaixz.bus.auth.vendor.renren;
    exports org.miaixz.bus.auth.vendor.router;
    exports org.miaixz.bus.auth.vendor.slack;
    exports org.miaixz.bus.auth.vendor.stackoverflow;
    exports org.miaixz.bus.auth.vendor.taobao;
    exports org.miaixz.bus.auth.vendor.teambition;
    exports org.miaixz.bus.auth.vendor.toutiao;
    exports org.miaixz.bus.auth.vendor.twitter;
    exports org.miaixz.bus.auth.vendor.vk;
    exports org.miaixz.bus.auth.vendor.wechat;
    exports org.miaixz.bus.auth.vendor.wechat.ee;
    exports org.miaixz.bus.auth.vendor.wechat.mini;
    exports org.miaixz.bus.auth.vendor.wechat.mp;
    exports org.miaixz.bus.auth.vendor.wechat.open;
    exports org.miaixz.bus.auth.vendor.weibo;
    exports org.miaixz.bus.auth.vendor.ximalaya;

}
