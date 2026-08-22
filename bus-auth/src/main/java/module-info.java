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
 * Provides protocol-standard authentication services and registered external identity integrations.
 * <p>
 * The module exports protocol-neutral contracts, project data loaders, pure parsers, runtime and registration services,
 * standard LDAP, OAuth 2.0, OpenID Connect, RADIUS, SAML, and SCIM models and services, shared security value types,
 * and immutable Vendor Source schemes. HTTP transport remains owned by {@code bus.fabric}.
 * </p>
 * <p>
 * Protocol drivers, server-side issuers, runtime assembly classes, and Snapshot Registry classes use explicit public
 * responsibility packages instead of hidden {@code internal} packages. {@code Authorizer} provides the unified runtime
 * assembly entry, while registration queries and capability execution remain separate through Registry and Dispatcher.
 * </p>
 *
 * @author Kimi Liu
 */
module bus.auth {

    requires java.naming;
    requires java.xml;
    requires java.xml.crypto;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.extra;
    requires bus.fabric;

    requires static lombok;

    exports org.miaixz.bus.auth;
    exports org.miaixz.bus.auth.cache;
    exports org.miaixz.bus.auth.codec;
    exports org.miaixz.bus.auth.guard;
    exports org.miaixz.bus.auth.protocol;
    exports org.miaixz.bus.auth.protocol.ldap;
    exports org.miaixz.bus.auth.protocol.ldap.client;
    exports org.miaixz.bus.auth.protocol.ldap.codec;
    exports org.miaixz.bus.auth.protocol.ldap.server;
    exports org.miaixz.bus.auth.protocol.oauth2;
    exports org.miaixz.bus.auth.protocol.oauth2.client;
    exports org.miaixz.bus.auth.protocol.oauth2.codec;
    exports org.miaixz.bus.auth.protocol.oauth2.grant;
    exports org.miaixz.bus.auth.protocol.oauth2.server;
    exports org.miaixz.bus.auth.protocol.oidc;
    exports org.miaixz.bus.auth.protocol.oidc.client;
    exports org.miaixz.bus.auth.protocol.oidc.codec;
    exports org.miaixz.bus.auth.protocol.oidc.server;
    exports org.miaixz.bus.auth.protocol.radius;
    exports org.miaixz.bus.auth.protocol.radius.codec;
    exports org.miaixz.bus.auth.protocol.radius.server;
    exports org.miaixz.bus.auth.protocol.saml;
    exports org.miaixz.bus.auth.protocol.saml.client;
    exports org.miaixz.bus.auth.protocol.saml.codec;
    exports org.miaixz.bus.auth.protocol.saml.security;
    exports org.miaixz.bus.auth.protocol.saml.server;
    exports org.miaixz.bus.auth.protocol.scim;
    exports org.miaixz.bus.auth.protocol.scim.codec;
    exports org.miaixz.bus.auth.protocol.scim.server;
    exports org.miaixz.bus.auth.registry;
    exports org.miaixz.bus.auth.resolver;
    exports org.miaixz.bus.auth.runtime;
    exports org.miaixz.bus.auth.shared;
    exports org.miaixz.bus.auth.shared.claim;
    exports org.miaixz.bus.auth.shared.dpop;
    exports org.miaixz.bus.auth.shared.jose;
    exports org.miaixz.bus.auth.shared.jwt;
    exports org.miaixz.bus.auth.shared.pkce;
    exports org.miaixz.bus.auth.source;
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
    exports org.miaixz.bus.auth.vendor.okta;
    exports org.miaixz.bus.auth.vendor.oschina;
    exports org.miaixz.bus.auth.vendor.pinterest;
    exports org.miaixz.bus.auth.vendor.proginn;
    exports org.miaixz.bus.auth.vendor.qq;
    exports org.miaixz.bus.auth.vendor.rednote;
    exports org.miaixz.bus.auth.vendor.slack;
    exports org.miaixz.bus.auth.vendor.stackoverflow;
    exports org.miaixz.bus.auth.vendor.taobao;
    exports org.miaixz.bus.auth.vendor.teambition;
    exports org.miaixz.bus.auth.vendor.toutiao;
    exports org.miaixz.bus.auth.vendor.twitter;
    exports org.miaixz.bus.auth.vendor.vk;
    exports org.miaixz.bus.auth.vendor.wechat;
    exports org.miaixz.bus.auth.vendor.weibo;
    exports org.miaixz.bus.auth.vendor.ximalaya;
    exports org.miaixz.bus.auth.worker;
    exports org.miaixz.bus.auth.worker.identity;
    exports org.miaixz.bus.auth.worker.loader;

}
