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

import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.afdian.AfdianManifest;
import org.miaixz.bus.auth.vendor.afdian.AfdianSourceAdapter;
import org.miaixz.bus.auth.vendor.alipay.AlipayManifest;
import org.miaixz.bus.auth.vendor.alipay.AlipaySourceAdapter;
import org.miaixz.bus.auth.vendor.aliyun.AliyunManifest;
import org.miaixz.bus.auth.vendor.aliyun.AliyunSourceAdapter;
import org.miaixz.bus.auth.vendor.amazon.AmazonManifest;
import org.miaixz.bus.auth.vendor.amazon.AmazonSourceAdapter;
import org.miaixz.bus.auth.vendor.apple.AppleManifest;
import org.miaixz.bus.auth.vendor.apple.AppleSourceAdapter;
import org.miaixz.bus.auth.vendor.baidu.BaiduManifest;
import org.miaixz.bus.auth.vendor.baidu.BaiduSourceAdapter;
import org.miaixz.bus.auth.vendor.coding.CodingManifest;
import org.miaixz.bus.auth.vendor.coding.CodingSourceAdapter;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkManifest;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkSourceAdapter;
import org.miaixz.bus.auth.vendor.douyin.DouyinManifest;
import org.miaixz.bus.auth.vendor.douyin.DouyinSourceAdapter;
import org.miaixz.bus.auth.vendor.eleme.ElemeManifest;
import org.miaixz.bus.auth.vendor.eleme.ElemeSourceAdapter;
import org.miaixz.bus.auth.vendor.facebook.FacebookManifest;
import org.miaixz.bus.auth.vendor.facebook.FacebookSourceAdapter;
import org.miaixz.bus.auth.vendor.feishu.FeishuManifest;
import org.miaixz.bus.auth.vendor.feishu.FeishuSourceAdapter;
import org.miaixz.bus.auth.vendor.figma.FigmaManifest;
import org.miaixz.bus.auth.vendor.figma.FigmaSourceAdapter;
import org.miaixz.bus.auth.vendor.gitee.GiteeManifest;
import org.miaixz.bus.auth.vendor.gitee.GiteeSourceAdapter;
import org.miaixz.bus.auth.vendor.github.GitHubManifest;
import org.miaixz.bus.auth.vendor.github.GitHubSourceAdapter;
import org.miaixz.bus.auth.vendor.gitlab.GitLabManifest;
import org.miaixz.bus.auth.vendor.gitlab.GitLabSourceAdapter;
import org.miaixz.bus.auth.vendor.google.GoogleManifest;
import org.miaixz.bus.auth.vendor.google.GoogleSourceAdapter;
import org.miaixz.bus.auth.vendor.huawei.HuaweiManifest;
import org.miaixz.bus.auth.vendor.huawei.HuaweiSourceAdapter;
import org.miaixz.bus.auth.vendor.jd.JdManifest;
import org.miaixz.bus.auth.vendor.jd.JdSourceAdapter;
import org.miaixz.bus.auth.vendor.kujiale.KujialeManifest;
import org.miaixz.bus.auth.vendor.kujiale.KujialeSourceAdapter;
import org.miaixz.bus.auth.vendor.line.LineManifest;
import org.miaixz.bus.auth.vendor.line.LineSourceAdapter;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInManifest;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInSourceAdapter;
import org.miaixz.bus.auth.vendor.meituan.MeituanManifest;
import org.miaixz.bus.auth.vendor.meituan.MeituanSourceAdapter;
import org.miaixz.bus.auth.vendor.mi.MiManifest;
import org.miaixz.bus.auth.vendor.mi.MiSourceAdapter;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftManifest;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftSourceAdapter;
import org.miaixz.bus.auth.vendor.okta.OktaManifest;
import org.miaixz.bus.auth.vendor.okta.OktaSourceAdapter;
import org.miaixz.bus.auth.vendor.oschina.OsChinaManifest;
import org.miaixz.bus.auth.vendor.oschina.OsChinaSourceAdapter;
import org.miaixz.bus.auth.vendor.pinterest.PinterestManifest;
import org.miaixz.bus.auth.vendor.pinterest.PinterestSourceAdapter;
import org.miaixz.bus.auth.vendor.proginn.ProginnManifest;
import org.miaixz.bus.auth.vendor.proginn.ProginnSourceAdapter;
import org.miaixz.bus.auth.vendor.qq.QqManifest;
import org.miaixz.bus.auth.vendor.qq.QqSourceAdapter;
import org.miaixz.bus.auth.vendor.rednote.RedNoteManifest;
import org.miaixz.bus.auth.vendor.rednote.RedNoteSourceAdapter;
import org.miaixz.bus.auth.vendor.slack.SlackManifest;
import org.miaixz.bus.auth.vendor.slack.SlackSourceAdapter;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowManifest;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowSourceAdapter;
import org.miaixz.bus.auth.vendor.taobao.TaobaoManifest;
import org.miaixz.bus.auth.vendor.taobao.TaobaoSourceAdapter;
import org.miaixz.bus.auth.vendor.teambition.TeambitionManifest;
import org.miaixz.bus.auth.vendor.teambition.TeambitionSourceAdapter;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoManifest;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoSourceAdapter;
import org.miaixz.bus.auth.vendor.twitter.TwitterManifest;
import org.miaixz.bus.auth.vendor.twitter.TwitterSourceAdapter;
import org.miaixz.bus.auth.vendor.vk.VkManifest;
import org.miaixz.bus.auth.vendor.vk.VkSourceAdapter;
import org.miaixz.bus.auth.vendor.wechat.*;
import org.miaixz.bus.auth.vendor.weibo.WeiboManifest;
import org.miaixz.bus.auth.vendor.weibo.WeiboSourceAdapter;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaManifest;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaSourceAdapter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

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
