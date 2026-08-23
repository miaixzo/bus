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
 * The module exports protocol-neutral contracts including the non-entity {@code Realm} resource model, project data
 * loaders, pure parsers, runtime and Roster services, standard LDAP, OAuth 2.0, OpenID Connect, RADIUS, SAML, and SCIM
 * models and services, shared security value types, build-time Connector and Registry contracts, and immutable Vendor
 * Source schemes. Protocol and Vendor connectors are discovered through one sealed Source SPI and dispatched to typed
 * build-scoped registries before runtime assembly. They do not establish remote connections; HTTP transport remains
 * owned by {@code bus.fabric}.
 * </p>
 * <p>
 * Protocol drivers, server-side issuers, runtime assembly classes, and Roster snapshot classes use explicit public
 * responsibility packages instead of hidden {@code internal} packages. {@code Authorize} provides the unified runtime
 * assembly entry, while Blueprint queries and capability execution remain separate through Roster and Dispatcher.
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

    uses org.miaixz.bus.auth.source.SourceConnector;

    provides org.miaixz.bus.auth.source.SourceConnector with org.miaixz.bus.auth.source.protocol.ldap.LdapConnector,
            org.miaixz.bus.auth.source.protocol.oauth2.OAuth2Connector,
            org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnector,
            org.miaixz.bus.auth.source.protocol.radius.RadiusConnector,
            org.miaixz.bus.auth.source.protocol.saml.SamlConnector,
            org.miaixz.bus.auth.source.protocol.scim.ScimConnector,
            org.miaixz.bus.auth.source.vendor.afdian.AfdianConnector,
            org.miaixz.bus.auth.source.vendor.alipay.AlipayConnector,
            org.miaixz.bus.auth.source.vendor.aliyun.AliyunConnector,
            org.miaixz.bus.auth.source.vendor.amazon.AmazonConnector,
            org.miaixz.bus.auth.source.vendor.apple.AppleConnector,
            org.miaixz.bus.auth.source.vendor.baidu.BaiduConnector,
            org.miaixz.bus.auth.source.vendor.coding.CodingConnector,
            org.miaixz.bus.auth.source.vendor.dingtalk.DingTalkConnector,
            org.miaixz.bus.auth.source.vendor.douyin.DouyinConnector,
            org.miaixz.bus.auth.source.vendor.eleme.ElemeConnector,
            org.miaixz.bus.auth.source.vendor.facebook.FacebookConnector,
            org.miaixz.bus.auth.source.vendor.feishu.FeishuConnector,
            org.miaixz.bus.auth.source.vendor.figma.FigmaConnector,
            org.miaixz.bus.auth.source.vendor.gitee.GiteeConnector,
            org.miaixz.bus.auth.source.vendor.github.GitHubConnector,
            org.miaixz.bus.auth.source.vendor.gitlab.GitLabConnector,
            org.miaixz.bus.auth.source.vendor.google.GoogleConnector,
            org.miaixz.bus.auth.source.vendor.huawei.HuaweiConnector, org.miaixz.bus.auth.source.vendor.jd.JdConnector,
            org.miaixz.bus.auth.source.vendor.kujiale.KujialeConnector,
            org.miaixz.bus.auth.source.vendor.line.LineConnector,
            org.miaixz.bus.auth.source.vendor.linkedin.LinkedInConnector,
            org.miaixz.bus.auth.source.vendor.meituan.MeituanConnector,
            org.miaixz.bus.auth.source.vendor.mi.MiConnector,
            org.miaixz.bus.auth.source.vendor.microsoft.MicrosoftConnector,
            org.miaixz.bus.auth.source.vendor.okta.OktaConnector,
            org.miaixz.bus.auth.source.vendor.oschina.OsChinaConnector,
            org.miaixz.bus.auth.source.vendor.pinterest.PinterestConnector,
            org.miaixz.bus.auth.source.vendor.proginn.ProginnConnector,
            org.miaixz.bus.auth.source.vendor.qq.QqConnector,
            org.miaixz.bus.auth.source.vendor.rednote.RedNoteConnector,
            org.miaixz.bus.auth.source.vendor.slack.SlackConnector,
            org.miaixz.bus.auth.source.vendor.stackoverflow.StackOverflowConnector,
            org.miaixz.bus.auth.source.vendor.taobao.TaobaoConnector,
            org.miaixz.bus.auth.source.vendor.teambition.TeambitionConnector,
            org.miaixz.bus.auth.source.vendor.toutiao.ToutiaoConnector,
            org.miaixz.bus.auth.source.vendor.twitter.TwitterConnector,
            org.miaixz.bus.auth.source.vendor.vk.VkConnector, org.miaixz.bus.auth.source.vendor.wechat.WeChatConnector,
            org.miaixz.bus.auth.source.vendor.weibo.WeiboConnector,
            org.miaixz.bus.auth.source.vendor.ximalaya.XimalayaConnector;

    exports org.miaixz.bus.auth;
    exports org.miaixz.bus.auth.cache;
    exports org.miaixz.bus.auth.codec;
    exports org.miaixz.bus.auth.guard;
    exports org.miaixz.bus.auth.source.protocol;
    exports org.miaixz.bus.auth.source.protocol.ldap;
    exports org.miaixz.bus.auth.source.protocol.ldap.client;
    exports org.miaixz.bus.auth.source.protocol.ldap.codec;
    exports org.miaixz.bus.auth.source.protocol.ldap.server;
    exports org.miaixz.bus.auth.source.protocol.oauth2;
    exports org.miaixz.bus.auth.source.protocol.oauth2.client;
    exports org.miaixz.bus.auth.source.protocol.oauth2.codec;
    exports org.miaixz.bus.auth.source.protocol.oauth2.grant;
    exports org.miaixz.bus.auth.source.protocol.oauth2.server;
    exports org.miaixz.bus.auth.source.protocol.oidc;
    exports org.miaixz.bus.auth.source.protocol.oidc.client;
    exports org.miaixz.bus.auth.source.protocol.oidc.codec;
    exports org.miaixz.bus.auth.source.protocol.oidc.server;
    exports org.miaixz.bus.auth.source.protocol.radius;
    exports org.miaixz.bus.auth.source.protocol.radius.codec;
    exports org.miaixz.bus.auth.source.protocol.radius.server;
    exports org.miaixz.bus.auth.source.protocol.saml;
    exports org.miaixz.bus.auth.source.protocol.saml.client;
    exports org.miaixz.bus.auth.source.protocol.saml.codec;
    exports org.miaixz.bus.auth.source.protocol.saml.security;
    exports org.miaixz.bus.auth.source.protocol.saml.server;
    exports org.miaixz.bus.auth.source.protocol.scim;
    exports org.miaixz.bus.auth.source.protocol.scim.codec;
    exports org.miaixz.bus.auth.source.protocol.scim.server;
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
    exports org.miaixz.bus.auth.source.vendor;
    exports org.miaixz.bus.auth.source.vendor.afdian;
    exports org.miaixz.bus.auth.source.vendor.alipay;
    exports org.miaixz.bus.auth.source.vendor.aliyun;
    exports org.miaixz.bus.auth.source.vendor.amazon;
    exports org.miaixz.bus.auth.source.vendor.apple;
    exports org.miaixz.bus.auth.source.vendor.baidu;
    exports org.miaixz.bus.auth.source.vendor.coding;
    exports org.miaixz.bus.auth.source.vendor.dingtalk;
    exports org.miaixz.bus.auth.source.vendor.douyin;
    exports org.miaixz.bus.auth.source.vendor.eleme;
    exports org.miaixz.bus.auth.source.vendor.facebook;
    exports org.miaixz.bus.auth.source.vendor.feishu;
    exports org.miaixz.bus.auth.source.vendor.figma;
    exports org.miaixz.bus.auth.source.vendor.gitee;
    exports org.miaixz.bus.auth.source.vendor.github;
    exports org.miaixz.bus.auth.source.vendor.gitlab;
    exports org.miaixz.bus.auth.source.vendor.google;
    exports org.miaixz.bus.auth.source.vendor.huawei;
    exports org.miaixz.bus.auth.source.vendor.jd;
    exports org.miaixz.bus.auth.source.vendor.kujiale;
    exports org.miaixz.bus.auth.source.vendor.line;
    exports org.miaixz.bus.auth.source.vendor.linkedin;
    exports org.miaixz.bus.auth.source.vendor.meituan;
    exports org.miaixz.bus.auth.source.vendor.mi;
    exports org.miaixz.bus.auth.source.vendor.microsoft;
    exports org.miaixz.bus.auth.source.vendor.okta;
    exports org.miaixz.bus.auth.source.vendor.oschina;
    exports org.miaixz.bus.auth.source.vendor.pinterest;
    exports org.miaixz.bus.auth.source.vendor.proginn;
    exports org.miaixz.bus.auth.source.vendor.qq;
    exports org.miaixz.bus.auth.source.vendor.rednote;
    exports org.miaixz.bus.auth.source.vendor.slack;
    exports org.miaixz.bus.auth.source.vendor.stackoverflow;
    exports org.miaixz.bus.auth.source.vendor.taobao;
    exports org.miaixz.bus.auth.source.vendor.teambition;
    exports org.miaixz.bus.auth.source.vendor.toutiao;
    exports org.miaixz.bus.auth.source.vendor.twitter;
    exports org.miaixz.bus.auth.source.vendor.vk;
    exports org.miaixz.bus.auth.source.vendor.wechat;
    exports org.miaixz.bus.auth.source.vendor.weibo;
    exports org.miaixz.bus.auth.source.vendor.ximalaya;
    exports org.miaixz.bus.auth.worker;
    exports org.miaixz.bus.auth.worker.identity;
    exports org.miaixz.bus.auth.worker.loader;

}
