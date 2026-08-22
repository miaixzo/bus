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
package org.miaixz.bus.auth.vendor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.afdian.AfdianManifest;
import org.miaixz.bus.auth.vendor.afdian.AfdianOptions;
import org.miaixz.bus.auth.vendor.afdian.AfdianSourceAdapter;
import org.miaixz.bus.auth.vendor.alipay.AlipayManifest;
import org.miaixz.bus.auth.vendor.alipay.AlipayOptions;
import org.miaixz.bus.auth.vendor.alipay.AlipaySourceAdapter;
import org.miaixz.bus.auth.vendor.aliyun.AliyunManifest;
import org.miaixz.bus.auth.vendor.aliyun.AliyunOptions;
import org.miaixz.bus.auth.vendor.aliyun.AliyunSourceAdapter;
import org.miaixz.bus.auth.vendor.amazon.AmazonManifest;
import org.miaixz.bus.auth.vendor.amazon.AmazonOptions;
import org.miaixz.bus.auth.vendor.amazon.AmazonSourceAdapter;
import org.miaixz.bus.auth.vendor.apple.AppleManifest;
import org.miaixz.bus.auth.vendor.apple.AppleOptions;
import org.miaixz.bus.auth.vendor.apple.AppleSourceAdapter;
import org.miaixz.bus.auth.vendor.baidu.BaiduManifest;
import org.miaixz.bus.auth.vendor.baidu.BaiduOptions;
import org.miaixz.bus.auth.vendor.baidu.BaiduSourceAdapter;
import org.miaixz.bus.auth.vendor.coding.CodingManifest;
import org.miaixz.bus.auth.vendor.coding.CodingOptions;
import org.miaixz.bus.auth.vendor.coding.CodingSourceAdapter;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkManifest;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkOptions;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkSourceAdapter;
import org.miaixz.bus.auth.vendor.douyin.DouyinManifest;
import org.miaixz.bus.auth.vendor.douyin.DouyinOptions;
import org.miaixz.bus.auth.vendor.douyin.DouyinSourceAdapter;
import org.miaixz.bus.auth.vendor.eleme.ElemeManifest;
import org.miaixz.bus.auth.vendor.eleme.ElemeOptions;
import org.miaixz.bus.auth.vendor.eleme.ElemeSourceAdapter;
import org.miaixz.bus.auth.vendor.facebook.FacebookManifest;
import org.miaixz.bus.auth.vendor.facebook.FacebookOptions;
import org.miaixz.bus.auth.vendor.facebook.FacebookSourceAdapter;
import org.miaixz.bus.auth.vendor.feishu.FeishuManifest;
import org.miaixz.bus.auth.vendor.feishu.FeishuOptions;
import org.miaixz.bus.auth.vendor.feishu.FeishuSourceAdapter;
import org.miaixz.bus.auth.vendor.figma.FigmaManifest;
import org.miaixz.bus.auth.vendor.figma.FigmaOptions;
import org.miaixz.bus.auth.vendor.figma.FigmaSourceAdapter;
import org.miaixz.bus.auth.vendor.gitee.GiteeManifest;
import org.miaixz.bus.auth.vendor.gitee.GiteeOptions;
import org.miaixz.bus.auth.vendor.gitee.GiteeSourceAdapter;
import org.miaixz.bus.auth.vendor.github.GitHubManifest;
import org.miaixz.bus.auth.vendor.github.GitHubOptions;
import org.miaixz.bus.auth.vendor.github.GitHubSourceAdapter;
import org.miaixz.bus.auth.vendor.gitlab.GitLabManifest;
import org.miaixz.bus.auth.vendor.gitlab.GitLabOptions;
import org.miaixz.bus.auth.vendor.gitlab.GitLabSourceAdapter;
import org.miaixz.bus.auth.vendor.google.GoogleManifest;
import org.miaixz.bus.auth.vendor.google.GoogleOptions;
import org.miaixz.bus.auth.vendor.google.GoogleSourceAdapter;
import org.miaixz.bus.auth.vendor.huawei.HuaweiManifest;
import org.miaixz.bus.auth.vendor.huawei.HuaweiOptions;
import org.miaixz.bus.auth.vendor.huawei.HuaweiSourceAdapter;
import org.miaixz.bus.auth.vendor.jd.JdManifest;
import org.miaixz.bus.auth.vendor.jd.JdOptions;
import org.miaixz.bus.auth.vendor.jd.JdSourceAdapter;
import org.miaixz.bus.auth.vendor.kujiale.KujialeManifest;
import org.miaixz.bus.auth.vendor.kujiale.KujialeOptions;
import org.miaixz.bus.auth.vendor.kujiale.KujialeSourceAdapter;
import org.miaixz.bus.auth.vendor.line.LineManifest;
import org.miaixz.bus.auth.vendor.line.LineOptions;
import org.miaixz.bus.auth.vendor.line.LineSourceAdapter;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInManifest;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInOptions;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInSourceAdapter;
import org.miaixz.bus.auth.vendor.meituan.MeituanManifest;
import org.miaixz.bus.auth.vendor.meituan.MeituanOptions;
import org.miaixz.bus.auth.vendor.meituan.MeituanSourceAdapter;
import org.miaixz.bus.auth.vendor.mi.MiManifest;
import org.miaixz.bus.auth.vendor.mi.MiOptions;
import org.miaixz.bus.auth.vendor.mi.MiSourceAdapter;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftManifest;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftOptions;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftSourceAdapter;
import org.miaixz.bus.auth.vendor.okta.OktaManifest;
import org.miaixz.bus.auth.vendor.okta.OktaOptions;
import org.miaixz.bus.auth.vendor.okta.OktaSourceAdapter;
import org.miaixz.bus.auth.vendor.oschina.OsChinaManifest;
import org.miaixz.bus.auth.vendor.oschina.OsChinaOptions;
import org.miaixz.bus.auth.vendor.oschina.OsChinaSourceAdapter;
import org.miaixz.bus.auth.vendor.pinterest.PinterestManifest;
import org.miaixz.bus.auth.vendor.pinterest.PinterestOptions;
import org.miaixz.bus.auth.vendor.pinterest.PinterestSourceAdapter;
import org.miaixz.bus.auth.vendor.proginn.ProginnManifest;
import org.miaixz.bus.auth.vendor.proginn.ProginnOptions;
import org.miaixz.bus.auth.vendor.proginn.ProginnSourceAdapter;
import org.miaixz.bus.auth.vendor.qq.QqManifest;
import org.miaixz.bus.auth.vendor.qq.QqOptions;
import org.miaixz.bus.auth.vendor.qq.QqSourceAdapter;
import org.miaixz.bus.auth.vendor.rednote.RedNoteManifest;
import org.miaixz.bus.auth.vendor.rednote.RedNoteOptions;
import org.miaixz.bus.auth.vendor.rednote.RedNoteSourceAdapter;
import org.miaixz.bus.auth.vendor.slack.SlackManifest;
import org.miaixz.bus.auth.vendor.slack.SlackOptions;
import org.miaixz.bus.auth.vendor.slack.SlackSourceAdapter;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowManifest;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowOptions;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowSourceAdapter;
import org.miaixz.bus.auth.vendor.taobao.TaobaoManifest;
import org.miaixz.bus.auth.vendor.taobao.TaobaoOptions;
import org.miaixz.bus.auth.vendor.taobao.TaobaoSourceAdapter;
import org.miaixz.bus.auth.vendor.teambition.TeambitionManifest;
import org.miaixz.bus.auth.vendor.teambition.TeambitionOptions;
import org.miaixz.bus.auth.vendor.teambition.TeambitionSourceAdapter;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoManifest;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoOptions;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoSourceAdapter;
import org.miaixz.bus.auth.vendor.twitter.TwitterManifest;
import org.miaixz.bus.auth.vendor.twitter.TwitterOptions;
import org.miaixz.bus.auth.vendor.twitter.TwitterSourceAdapter;
import org.miaixz.bus.auth.vendor.vk.VkManifest;
import org.miaixz.bus.auth.vendor.vk.VkOptions;
import org.miaixz.bus.auth.vendor.vk.VkSourceAdapter;
import org.miaixz.bus.auth.vendor.wechat.*;
import org.miaixz.bus.auth.vendor.weibo.WeiboManifest;
import org.miaixz.bus.auth.vendor.weibo.WeiboOptions;
import org.miaixz.bus.auth.vendor.weibo.WeiboSourceAdapter;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaManifest;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaOptions;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaSourceAdapter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Builds the framework-owned baseline inventories consumed by the public Vendor module builder.
 * <p>
 * Assembly is explicit and deterministic: forty-one Vendor manifests and fifty exact platform-variant adapter bindings
 * are frozen before contribution. The module performs no reflection, service loading, Registry access, network
 * operation, or runtime registration mutation.
 * </p>
 *
 * @author Kimi Liu
 */
final class VendorSuite {

    /**
     * Prevents instantiation of the framework-owned baseline inventory holder.
     */
    private VendorSuite() {
        // No initialization required.
    }

    /**
     * Creates a fresh immutable directory for external management interfaces.
     *
     * @return complete forty-one-platform Vendor directory
     */
    static VendorDirectory directory() {
        return buildVendorDirectory();
    }

    /**
     * Creates fresh immutable adapter factory bindings for the built-in platform variants.
     *
     * @return complete fifty-variant adapter factory bindings
     */
    static AdapterBindings bindings() {
        return buildAdapterBindings();
    }

    /**
     * Creates fresh immutable Options factory bindings for the supplied built-in directory instances.
     *
     * @param directory exact built-in manifest directory retained by the module builder
     * @return complete fifty-variant Options factory bindings
     */
    static OptionsBindings options(final VendorDirectory directory) {
        return buildOptionsBindings(Assert.notNull(directory, "Vendor Options directory must not be null"));
    }

    /**
     * Builds the complete Vendor manifest directory in stable inventory order.
     *
     * @return immutable forty-one-platform directory
     */
    private static VendorDirectory buildVendorDirectory() {
        return new VendorDirectory(List.of(
                new AfdianManifest(),
                new AlipayManifest(),
                new AliyunManifest(),
                new AmazonManifest(),
                new AppleManifest(),
                new BaiduManifest(),
                new CodingManifest(),
                new DingTalkManifest(),
                new DouyinManifest(),
                new ElemeManifest(),
                new FacebookManifest(),
                new FeishuManifest(),
                new FigmaManifest(),
                new GiteeManifest(),
                new GitHubManifest(),
                new GitLabManifest(),
                new GoogleManifest(),
                new HuaweiManifest(),
                new JdManifest(),
                new KujialeManifest(),
                new LineManifest(),
                new LinkedInManifest(),
                new MeituanManifest(),
                new MiManifest(),
                new MicrosoftManifest(),
                new OktaManifest(),
                new OsChinaManifest(),
                new PinterestManifest(),
                new ProginnManifest(),
                new QqManifest(),
                new RedNoteManifest(),
                new SlackManifest(),
                new StackOverflowManifest(),
                new TaobaoManifest(),
                new TeambitionManifest(),
                new ToutiaoManifest(),
                new TwitterManifest(),
                new VkManifest(),
                new WeChatManifest(),
                new WeiboManifest(),
                new XimalayaManifest()));
    }

    /**
     * Builds the complete exact adapter factory directory in stable inventory order.
     *
     * @return immutable fifty-variant adapter factory directory
     */
    private static AdapterBindings buildAdapterBindings() {
        final Map<AdapterBindings.Key, AdapterBindings.Binding> bindings = new LinkedHashMap<>(50);
        register(
                bindings,
                AfdianManifest.ID,
                AfdianManifest.DEFAULT,
                factory(AfdianManifest.class, AfdianSourceAdapter::new));
        register(
                bindings,
                AlipayManifest.ID,
                AlipayManifest.DEFAULT,
                factory(AlipayManifest.class, AlipaySourceAdapter::new));
        register(
                bindings,
                AliyunManifest.ID,
                AliyunManifest.DEFAULT,
                factory(AliyunManifest.class, AliyunSourceAdapter::new));
        register(
                bindings,
                AmazonManifest.ID,
                AmazonManifest.DEFAULT,
                factory(AmazonManifest.class, AmazonSourceAdapter::new));
        register(
                bindings,
                AppleManifest.ID,
                AppleManifest.DEFAULT,
                factory(AppleManifest.class, AppleSourceAdapter::new));
        register(
                bindings,
                BaiduManifest.ID,
                BaiduManifest.DEFAULT,
                factory(BaiduManifest.class, BaiduSourceAdapter::new));
        register(
                bindings,
                CodingManifest.ID,
                CodingManifest.DEFAULT,
                factory(CodingManifest.class, CodingSourceAdapter::new));
        register(
                bindings,
                DingTalkManifest.ID,
                DingTalkManifest.OAUTH2,
                factory(DingTalkManifest.class, DingTalkSourceAdapter::new));
        register(
                bindings,
                DingTalkManifest.ID,
                DingTalkManifest.ACCOUNT,
                factory(DingTalkManifest.class, DingTalkSourceAdapter::new));
        register(
                bindings,
                DouyinManifest.ID,
                DouyinManifest.OPEN,
                factory(DouyinManifest.class, DouyinSourceAdapter::new));
        register(
                bindings,
                DouyinManifest.ID,
                DouyinManifest.MINI_PROGRAM,
                factory(DouyinManifest.class, DouyinSourceAdapter::new));
        register(
                bindings,
                ElemeManifest.ID,
                ElemeManifest.DEFAULT,
                factory(ElemeManifest.class, ElemeSourceAdapter::new));
        register(
                bindings,
                FacebookManifest.ID,
                FacebookManifest.DEFAULT,
                factory(FacebookManifest.class, FacebookSourceAdapter::new));
        register(
                bindings,
                FeishuManifest.ID,
                FeishuManifest.DEFAULT,
                factory(FeishuManifest.class, FeishuSourceAdapter::new));
        register(
                bindings,
                FigmaManifest.ID,
                FigmaManifest.DEFAULT,
                factory(FigmaManifest.class, FigmaSourceAdapter::new));
        register(
                bindings,
                GiteeManifest.ID,
                GiteeManifest.DEFAULT,
                factory(GiteeManifest.class, GiteeSourceAdapter::new));
        register(
                bindings,
                GitHubManifest.ID,
                GitHubManifest.DEFAULT,
                factory(GitHubManifest.class, GitHubSourceAdapter::new));
        register(
                bindings,
                GitLabManifest.ID,
                GitLabManifest.DEFAULT,
                factory(GitLabManifest.class, GitLabSourceAdapter::new));
        register(
                bindings,
                GoogleManifest.ID,
                GoogleManifest.DEFAULT,
                factory(GoogleManifest.class, GoogleSourceAdapter::new));
        register(
                bindings,
                HuaweiManifest.ID,
                HuaweiManifest.DEFAULT,
                factory(HuaweiManifest.class, HuaweiSourceAdapter::new));
        register(bindings, JdManifest.ID, JdManifest.DEFAULT, factory(JdManifest.class, JdSourceAdapter::new));
        register(
                bindings,
                KujialeManifest.ID,
                KujialeManifest.DEFAULT,
                factory(KujialeManifest.class, KujialeSourceAdapter::new));
        register(bindings, LineManifest.ID, LineManifest.DEFAULT, factory(LineManifest.class, LineSourceAdapter::new));
        register(
                bindings,
                LinkedInManifest.ID,
                LinkedInManifest.DEFAULT,
                factory(LinkedInManifest.class, LinkedInSourceAdapter::new));
        register(
                bindings,
                MeituanManifest.ID,
                MeituanManifest.DEFAULT,
                factory(MeituanManifest.class, MeituanSourceAdapter::new));
        register(bindings, MiManifest.ID, MiManifest.DEFAULT, factory(MiManifest.class, MiSourceAdapter::new));
        register(
                bindings,
                MicrosoftManifest.ID,
                MicrosoftManifest.GLOBAL,
                factory(MicrosoftManifest.class, MicrosoftSourceAdapter::new));
        register(
                bindings,
                MicrosoftManifest.ID,
                MicrosoftManifest.CHINA,
                factory(MicrosoftManifest.class, MicrosoftSourceAdapter::new));
        register(bindings, OktaManifest.ID, OktaManifest.DEFAULT, factory(OktaManifest.class, OktaSourceAdapter::new));
        register(
                bindings,
                OsChinaManifest.ID,
                OsChinaManifest.DEFAULT,
                factory(OsChinaManifest.class, OsChinaSourceAdapter::new));
        register(
                bindings,
                PinterestManifest.ID,
                PinterestManifest.DEFAULT,
                factory(PinterestManifest.class, PinterestSourceAdapter::new));
        register(
                bindings,
                ProginnManifest.ID,
                ProginnManifest.DEFAULT,
                factory(ProginnManifest.class, ProginnSourceAdapter::new));
        register(bindings, QqManifest.ID, QqManifest.OPEN, factory(QqManifest.class, QqSourceAdapter::new));
        register(bindings, QqManifest.ID, QqManifest.MINI_PROGRAM, factory(QqManifest.class, QqSourceAdapter::new));
        register(
                bindings,
                RedNoteManifest.ID,
                RedNoteManifest.MARKETING,
                factory(RedNoteManifest.class, RedNoteSourceAdapter::new));
        register(
                bindings,
                SlackManifest.ID,
                SlackManifest.DEFAULT,
                factory(SlackManifest.class, SlackSourceAdapter::new));
        register(
                bindings,
                StackOverflowManifest.ID,
                StackOverflowManifest.DEFAULT,
                factory(StackOverflowManifest.class, StackOverflowSourceAdapter::new));
        register(
                bindings,
                TaobaoManifest.ID,
                TaobaoManifest.DEFAULT,
                factory(TaobaoManifest.class, TaobaoSourceAdapter::new));
        register(
                bindings,
                TeambitionManifest.ID,
                TeambitionManifest.DEFAULT,
                factory(TeambitionManifest.class, TeambitionSourceAdapter::new));
        register(
                bindings,
                ToutiaoManifest.ID,
                ToutiaoManifest.DEFAULT,
                factory(ToutiaoManifest.class, ToutiaoSourceAdapter::new));
        register(
                bindings,
                TwitterManifest.ID,
                TwitterManifest.DEFAULT,
                factory(TwitterManifest.class, TwitterSourceAdapter::new));
        register(bindings, VkManifest.ID, VkManifest.DEFAULT, factory(VkManifest.class, VkSourceAdapter::new));
        register(
                bindings,
                WeChatManifest.ID,
                WeChatManifest.OPEN,
                factory(WeChatManifest.class, WeChatOpenAdapter::new));
        register(bindings, WeChatManifest.ID, WeChatManifest.MP, factory(WeChatManifest.class, WeChatMpAdapter::new));
        register(
                bindings,
                WeChatManifest.ID,
                WeChatManifest.MINI,
                factory(WeChatManifest.class, WeChatMiniAdapter::new));
        register(bindings, WeChatManifest.ID, WeChatManifest.EE, factory(WeChatManifest.class, WeChatEeAdapter::new));
        register(
                bindings,
                WeChatManifest.ID,
                WeChatManifest.EE_QRCODE,
                factory(WeChatManifest.class, WeChatEeAdapter::new));
        register(
                bindings,
                WeChatManifest.ID,
                WeChatManifest.EE_WEB,
                factory(WeChatManifest.class, WeChatEeAdapter::new));
        register(
                bindings,
                WeiboManifest.ID,
                WeiboManifest.DEFAULT,
                factory(WeiboManifest.class, WeiboSourceAdapter::new));
        register(
                bindings,
                XimalayaManifest.ID,
                XimalayaManifest.DEFAULT,
                factory(XimalayaManifest.class, XimalayaSourceAdapter::new));
        if (bindings.size() != 50) {
            throw new ValidateException("Vendor adapter inventory must contain exactly fifty variants");
        }
        return new AdapterBindings(bindings);
    }

    /**
     * Builds the complete exact Options factory directory for the retained manifest instances.
     *
     * @param directory exact built-in manifest directory
     * @return immutable fifty-variant Options factory directory
     */
    private static OptionsBindings buildOptionsBindings(final VendorDirectory directory) {
        final Map<OptionsBindings.Key, OptionsBindings.Binding> bindings = new LinkedHashMap<>(50);
        registerOptions(bindings, directory, AfdianManifest.ID, simple(AfdianOptions::new));
        registerOptions(
                bindings,
                directory,
                AlipayManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new AlipayOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        requiredString(parameters, "verificationKeyId")));
        registerOptions(bindings, directory, AliyunManifest.ID, simple(AliyunOptions::new));
        registerOptions(
                bindings,
                directory,
                AmazonManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new AmazonOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes, bool(parameters, "pkce", false)));
        registerOptions(
                bindings,
                directory,
                AppleManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new AppleOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes, requiredString(parameters, "teamId"),
                        requiredString(parameters, "keyId")));
        registerOptions(bindings, directory, BaiduManifest.ID, simple(BaiduOptions::new));
        registerOptions(
                bindings,
                directory,
                CodingManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new CodingOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes, requiredString(parameters, "team")));
        registerOptions(
                bindings,
                directory,
                DingTalkManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new DingTalkOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        optionalString(parameters, "orgType"), optionalString(parameters, "corpId"),
                        bool(parameters, "exclusiveLogin", false), optionalString(parameters, "exclusiveCorpId")));
        registerOptions(bindings, directory, DouyinManifest.ID, simple(DouyinOptions::new));
        registerOptions(bindings, directory, ElemeManifest.ID, simple(ElemeOptions::new));
        registerOptions(bindings, directory, FacebookManifest.ID, simple(FacebookOptions::new));
        registerOptions(bindings, directory, FeishuManifest.ID, simple(FeishuOptions::new));
        registerOptions(bindings, directory, FigmaManifest.ID, simple(FigmaOptions::new));
        registerOptions(bindings, directory, GiteeManifest.ID, simple(GiteeOptions::new));
        registerOptions(bindings, directory, GitHubManifest.ID, simple(GitHubOptions::new));
        registerOptions(bindings, directory, GitLabManifest.ID, simple(GitLabOptions::new));
        registerOptions(bindings, directory, GoogleManifest.ID, simple(GoogleOptions::new));
        registerOptions(bindings, directory, HuaweiManifest.ID, simple(HuaweiOptions::new));
        registerOptions(bindings, directory, JdManifest.ID, simple(JdOptions::new));
        registerOptions(bindings, directory, KujialeManifest.ID, simple(KujialeOptions::new));
        registerOptions(bindings, directory, LineManifest.ID, simple(LineOptions::new));
        registerOptions(bindings, directory, LinkedInManifest.ID, simple(LinkedInOptions::new));
        registerOptions(bindings, directory, MeituanManifest.ID, simple(MeituanOptions::new));
        registerOptions(bindings, directory, MiManifest.ID, simple(MiOptions::new));
        registerOptions(
                bindings,
                directory,
                MicrosoftManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new MicrosoftOptions(
                        variant.platform(), variant.variant(), clientId, credential, callback, scopes,
                        string(
                                parameters,
                                "tenant",
                                MicrosoftManifest.CHINA.equals(variant.variant()) ? "organizations" : "common")));
        registerOptions(
                bindings,
                directory,
                OktaManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new OktaOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        requiredString(parameters, "instance"),
                        string(parameters, "authorizationServerId", "default")));
        registerOptions(bindings, directory, OsChinaManifest.ID, simple(OsChinaOptions::new));
        registerOptions(bindings, directory, PinterestManifest.ID, simple(PinterestOptions::new));
        registerOptions(bindings, directory, ProginnManifest.ID, simple(ProginnOptions::new));
        registerOptions(
                bindings,
                directory,
                QqManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new QqOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        bool(parameters, "preferUnionId", false)));
        registerOptions(bindings, directory, RedNoteManifest.ID, simple(RedNoteOptions::new));
        registerOptions(bindings, directory, SlackManifest.ID, simple(SlackOptions::new));
        registerOptions(
                bindings,
                directory,
                StackOverflowManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new StackOverflowOptions(
                        variant.platform(), variant.variant(), clientId, credential, callback, scopes,
                        requiredString(parameters, "key"), requiredString(parameters, "siteId")));
        registerOptions(bindings, directory, TaobaoManifest.ID, simple(TaobaoOptions::new));
        registerOptions(bindings, directory, TeambitionManifest.ID, simple(TeambitionOptions::new));
        registerOptions(bindings, directory, ToutiaoManifest.ID, simple(ToutiaoOptions::new));
        registerOptions(bindings, directory, TwitterManifest.ID, simple(TwitterOptions::new));
        registerOptions(bindings, directory, VkManifest.ID, simple(VkOptions::new));
        registerOptions(
                bindings,
                directory,
                WeChatManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new WeChatOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes, string(parameters, "loginType", ""),
                        string(parameters, "agentId", ""), string(parameters, "language", ""),
                        string(parameters, "userType", "")));
        registerOptions(bindings, directory, WeiboManifest.ID, simple(WeiboOptions::new));
        registerOptions(
                bindings,
                directory,
                XimalayaManifest.ID,
                (variant, clientId, credential, callback, scopes, parameters) -> new XimalayaOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        requiredString(parameters, "deviceId"), requiredString(parameters, "clientOsType"),
                        requiredString(parameters, "packageId")));
        if (bindings.size() != 50) {
            throw new ValidateException("Vendor Options factory inventory must contain exactly fifty variants");
        }
        return new OptionsBindings(bindings);
    }

    /**
     * Registers one factory for every variant declared by an exact built-in platform manifest.
     */
    private static void registerOptions(
            final Map<OptionsBindings.Key, OptionsBindings.Binding> bindings,
            final VendorDirectory directory,
            final Vendor.Id vendor,
            final VendorOptions.Factory<?> factory) {
        final VariantManifest<?> manifest = directory.require(vendor);
        for (VariantManifest.Variant variant : manifest.variants()) {
            final OptionsBindings.Key key = new OptionsBindings.Key(vendor, variant.variant());
            if (bindings.putIfAbsent(key, new OptionsBindings.Binding(manifest, variant, factory)) != null) {
                throw new ValidateException("Duplicate Vendor Options factory: " + vendor.value() + Symbol.C_SLASH
                        + variant.variant().value());
            }
        }
    }

    /**
     * Adapts one six-component immutable Options constructor to the common factory contract.
     */
    private static <S extends VendorOptions<?>> VendorOptions.Factory<S> simple(
            final OptionsConstructor<S> constructor) {
        final OptionsConstructor<S> checked = Assert
                .notNull(constructor, "Vendor Options constructor must not be null");
        return (variant, clientId, credential, callback, scopes, parameters) -> {
            if (!parameters.values().isEmpty()) {
                throw new ValidateException("Vendor variant does not declare additional Options parameters");
            }
            return checked.create(variant.platform(), variant.variant(), clientId, credential, callback, scopes);
        };
    }

    /**
     * Reads one required string parameter without coercion.
     */
    private static String requiredString(final JsonValue.ObjectValue parameters, final String name) {
        final JsonValue value = parameters.values().get(name);
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Vendor Options parameter must be a string: " + name);
        }
        return Assert.notBlank(string.value(), "Vendor Options parameter must not be blank");
    }

    /**
     * Reads one optional string parameter without coercion.
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue parameters, final String name) {
        final JsonValue value = parameters.values().get(name);
        return value == null ? Optional.empty() : Optional.of(requiredString(parameters, name));
    }

    /**
     * Reads one string parameter or applies a non-sensitive framework default.
     */
    private static String string(final JsonValue.ObjectValue parameters, final String name, final String defaultValue) {
        return parameters.values().containsKey(name) ? requiredString(parameters, name) : defaultValue;
    }

    /**
     * Reads one boolean parameter or applies a non-sensitive framework default.
     */
    private static boolean bool(final JsonValue.ObjectValue parameters, final String name, final boolean defaultValue) {
        final JsonValue value = parameters.values().get(name);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof JsonValue.BooleanValue bool)) {
            throw new ValidateException("Vendor Options parameter must be a boolean: " + name);
        }
        return bool.value();
    }

    /**
     * Represents one concrete immutable Options record constructor with the common six components.
     *
     * @param <S> concrete Vendor Options type
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface OptionsConstructor<S extends VendorOptions<?>> {

        /**
         * Creates one immutable concrete Options value.
         */
        S create(
                Vendor.Id vendor,
                Vendor.Variant variant,
                String clientId,
                Credential.Reference credential,
                Optional<String> callback,
                List<String> scopes);

    }

    /**
     * Registers one exact platform-variant factory and rejects inventory duplication.
     *
     * @param bindings mutable build-scoped adapter bindings
     * @param vendor   exact platform identifier
     * @param variant  exact platform variant identifier
     * @param binding  exact typed adapter binding
     * @throws ValidateException if the platform-variant key already exists
     */
    private static void register(
            final Map<AdapterBindings.Key, AdapterBindings.Binding> bindings,
            final Vendor.Id vendor,
            final Vendor.Variant variant,
            final AdapterBindings.Binding binding) {
        final AdapterBindings.Key key = new AdapterBindings.Key(vendor, variant);
        if (bindings.putIfAbsent(key, binding) != null) {
            throw new ValidateException(
                    "Duplicate Vendor adapter factory: " + vendor.value() + Symbol.C_SLASH + variant.value());
        }
    }

    /**
     * Adapts a concrete platform manifest constructor to the framework generic factory contract.
     *
     * @param manifestType exact concrete platform manifest class
     * @param constructor  exact concrete adapter constructor
     * @param <D>          concrete platform manifest type
     * @param <S>          concrete platform options type
     * @return type-safe adapter binding with runtime manifest and options verification
     */
    private static <D extends VariantManifest<S>, S extends VendorOptions<?>> AdapterBindings.Binding factory(
            final Class<D> manifestType,
            final AdapterConstructor<D, S> constructor) {
        Assert.notNull(manifestType, "Vendor manifest class must not be null");
        Assert.notNull(constructor, "Vendor adapter constructor must not be null");
        return AdapterBindings.binding(
                manifestType,
                (namespaceId, sourceId, manifest, variant, options, services) -> constructor
                        .create(namespaceId, sourceId, manifestType.cast(manifest), variant, options, services));
    }

    /**
     * Represents one concrete platform adapter constructor without weakening its manifest or options type.
     *
     * @param <D> concrete platform manifest type
     * @param <S> concrete platform options type
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface AdapterConstructor<D extends VariantManifest<S>, S extends VendorOptions<?>> {

        /**
         * Creates one Source-bound concrete platform adapter.
         *
         * @param namespaceId registration namespace identifier
         * @param sourceId    registration Source identifier
         * @param manifest    exact concrete platform manifest
         * @param variant     exact selected variant
         * @param options     decoded concrete platform options
         * @param services    complete caller-owned runtime dependencies
         * @return non-null concrete adapter
         */
        VendorAdapter create(
                String namespaceId,
                String sourceId,
                D manifest,
                VariantManifest.Variant variant,
                S options,
                DriverServices services);

    }

}
