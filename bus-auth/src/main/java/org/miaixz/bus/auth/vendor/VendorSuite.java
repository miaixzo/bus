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

import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.vendor.afdian.AfdianDefinition;
import org.miaixz.bus.auth.vendor.afdian.internal.AfdianSourceAdapter;
import org.miaixz.bus.auth.vendor.alipay.AlipayDefinition;
import org.miaixz.bus.auth.vendor.alipay.internal.AlipaySourceAdapter;
import org.miaixz.bus.auth.vendor.aliyun.AliyunDefinition;
import org.miaixz.bus.auth.vendor.aliyun.internal.AliyunSourceAdapter;
import org.miaixz.bus.auth.vendor.amazon.AmazonDefinition;
import org.miaixz.bus.auth.vendor.amazon.internal.AmazonSourceAdapter;
import org.miaixz.bus.auth.vendor.apple.AppleDefinition;
import org.miaixz.bus.auth.vendor.apple.internal.AppleSourceAdapter;
import org.miaixz.bus.auth.vendor.baidu.BaiduDefinition;
import org.miaixz.bus.auth.vendor.baidu.internal.BaiduSourceAdapter;
import org.miaixz.bus.auth.vendor.coding.CodingDefinition;
import org.miaixz.bus.auth.vendor.coding.internal.CodingSourceAdapter;
import org.miaixz.bus.auth.vendor.dingtalk.DingTalkDefinition;
import org.miaixz.bus.auth.vendor.dingtalk.internal.DingTalkSourceAdapter;
import org.miaixz.bus.auth.vendor.douyin.DouyinDefinition;
import org.miaixz.bus.auth.vendor.douyin.internal.DouyinSourceAdapter;
import org.miaixz.bus.auth.vendor.eleme.ElemeDefinition;
import org.miaixz.bus.auth.vendor.eleme.internal.ElemeSourceAdapter;
import org.miaixz.bus.auth.vendor.facebook.FacebookDefinition;
import org.miaixz.bus.auth.vendor.facebook.internal.FacebookSourceAdapter;
import org.miaixz.bus.auth.vendor.feishu.FeishuDefinition;
import org.miaixz.bus.auth.vendor.feishu.internal.FeishuSourceAdapter;
import org.miaixz.bus.auth.vendor.figma.FigmaDefinition;
import org.miaixz.bus.auth.vendor.figma.internal.FigmaSourceAdapter;
import org.miaixz.bus.auth.vendor.gitee.GiteeDefinition;
import org.miaixz.bus.auth.vendor.gitee.internal.GiteeSourceAdapter;
import org.miaixz.bus.auth.vendor.github.GitHubDefinition;
import org.miaixz.bus.auth.vendor.github.internal.GitHubSourceAdapter;
import org.miaixz.bus.auth.vendor.gitlab.GitLabDefinition;
import org.miaixz.bus.auth.vendor.gitlab.internal.GitLabSourceAdapter;
import org.miaixz.bus.auth.vendor.google.GoogleDefinition;
import org.miaixz.bus.auth.vendor.google.internal.GoogleSourceAdapter;
import org.miaixz.bus.auth.vendor.huawei.HuaweiDefinition;
import org.miaixz.bus.auth.vendor.huawei.internal.HuaweiSourceAdapter;
import org.miaixz.bus.auth.vendor.jd.JdDefinition;
import org.miaixz.bus.auth.vendor.jd.internal.JdSourceAdapter;
import org.miaixz.bus.auth.vendor.kujiale.KujialeDefinition;
import org.miaixz.bus.auth.vendor.kujiale.internal.KujialeSourceAdapter;
import org.miaixz.bus.auth.vendor.line.LineDefinition;
import org.miaixz.bus.auth.vendor.line.internal.LineSourceAdapter;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInDefinition;
import org.miaixz.bus.auth.vendor.linkedin.internal.LinkedInSourceAdapter;
import org.miaixz.bus.auth.vendor.meituan.MeituanDefinition;
import org.miaixz.bus.auth.vendor.meituan.internal.MeituanSourceAdapter;
import org.miaixz.bus.auth.vendor.mi.MiDefinition;
import org.miaixz.bus.auth.vendor.mi.internal.MiSourceAdapter;
import org.miaixz.bus.auth.vendor.microsoft.MicrosoftDefinition;
import org.miaixz.bus.auth.vendor.microsoft.internal.MicrosoftSourceAdapter;
import org.miaixz.bus.auth.vendor.okta.OktaDefinition;
import org.miaixz.bus.auth.vendor.okta.internal.OktaSourceAdapter;
import org.miaixz.bus.auth.vendor.oschina.OsChinaDefinition;
import org.miaixz.bus.auth.vendor.oschina.internal.OsChinaSourceAdapter;
import org.miaixz.bus.auth.vendor.pinterest.PinterestDefinition;
import org.miaixz.bus.auth.vendor.pinterest.internal.PinterestSourceAdapter;
import org.miaixz.bus.auth.vendor.proginn.ProginnDefinition;
import org.miaixz.bus.auth.vendor.proginn.internal.ProginnSourceAdapter;
import org.miaixz.bus.auth.vendor.qq.QqDefinition;
import org.miaixz.bus.auth.vendor.qq.internal.QqSourceAdapter;
import org.miaixz.bus.auth.vendor.rednote.RedNoteDefinition;
import org.miaixz.bus.auth.vendor.rednote.internal.RedNoteSourceAdapter;
import org.miaixz.bus.auth.vendor.slack.SlackDefinition;
import org.miaixz.bus.auth.vendor.slack.internal.SlackSourceAdapter;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowDefinition;
import org.miaixz.bus.auth.vendor.stackoverflow.internal.StackOverflowSourceAdapter;
import org.miaixz.bus.auth.vendor.taobao.TaobaoDefinition;
import org.miaixz.bus.auth.vendor.taobao.internal.TaobaoSourceAdapter;
import org.miaixz.bus.auth.vendor.teambition.TeambitionDefinition;
import org.miaixz.bus.auth.vendor.teambition.internal.TeambitionSourceAdapter;
import org.miaixz.bus.auth.vendor.toutiao.ToutiaoDefinition;
import org.miaixz.bus.auth.vendor.toutiao.internal.ToutiaoSourceAdapter;
import org.miaixz.bus.auth.vendor.twitter.TwitterDefinition;
import org.miaixz.bus.auth.vendor.twitter.internal.TwitterSourceAdapter;
import org.miaixz.bus.auth.vendor.vk.VkDefinition;
import org.miaixz.bus.auth.vendor.vk.internal.VkSourceAdapter;
import org.miaixz.bus.auth.vendor.wechat.WeChatDefinition;
import org.miaixz.bus.auth.vendor.wechat.internal.ee.WeChatEeAdapter;
import org.miaixz.bus.auth.vendor.wechat.internal.mini.WeChatMiniAdapter;
import org.miaixz.bus.auth.vendor.wechat.internal.mp.WeChatMpAdapter;
import org.miaixz.bus.auth.vendor.wechat.internal.open.WeChatOpenAdapter;
import org.miaixz.bus.auth.vendor.weibo.WeiboDefinition;
import org.miaixz.bus.auth.vendor.weibo.internal.WeiboSourceAdapter;
import org.miaixz.bus.auth.vendor.ximalaya.XimalayaDefinition;
import org.miaixz.bus.auth.vendor.ximalaya.internal.XimalayaSourceAdapter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Builds the framework-owned baseline inventories consumed by the public Vendor module builder.
 * <p>
 * Assembly is explicit and deterministic: forty-one Vendor definitions and fifty exact platform-variant adapter
 * bindings are frozen before contribution. The module performs no reflection, service loading, Registry access, network
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
     * Builds the complete Vendor definition directory in stable inventory order.
     *
     * @return immutable forty-one-platform directory
     */
    private static VendorDirectory buildVendorDirectory() {
        return new VendorDirectory(List.of(
                new AfdianDefinition(),
                new AlipayDefinition(),
                new AliyunDefinition(),
                new AmazonDefinition(),
                new AppleDefinition(),
                new BaiduDefinition(),
                new CodingDefinition(),
                new DingTalkDefinition(),
                new DouyinDefinition(),
                new ElemeDefinition(),
                new FacebookDefinition(),
                new FeishuDefinition(),
                new FigmaDefinition(),
                new GiteeDefinition(),
                new GitHubDefinition(),
                new GitLabDefinition(),
                new GoogleDefinition(),
                new HuaweiDefinition(),
                new JdDefinition(),
                new KujialeDefinition(),
                new LineDefinition(),
                new LinkedInDefinition(),
                new MeituanDefinition(),
                new MiDefinition(),
                new MicrosoftDefinition(),
                new OktaDefinition(),
                new OsChinaDefinition(),
                new PinterestDefinition(),
                new ProginnDefinition(),
                new QqDefinition(),
                new RedNoteDefinition(),
                new SlackDefinition(),
                new StackOverflowDefinition(),
                new TaobaoDefinition(),
                new TeambitionDefinition(),
                new ToutiaoDefinition(),
                new TwitterDefinition(),
                new VkDefinition(),
                new WeChatDefinition(),
                new WeiboDefinition(),
                new XimalayaDefinition()));
    }

    /**
     * Builds the complete exact adapter factory directory in stable inventory order.
     *
     * @return immutable fifty-variant adapter factory directory
     */
    private static AdapterBindings buildAdapterBindings() {
        final Map<AdapterBindings.Key, VendorAdapter.Factory<?>> bindings = new LinkedHashMap<>(50);
        register(
                bindings,
                AfdianDefinition.ID,
                AfdianDefinition.DEFAULT,
                factory(AfdianDefinition.class, AfdianSourceAdapter::new));
        register(
                bindings,
                AlipayDefinition.ID,
                AlipayDefinition.DEFAULT,
                factory(AlipayDefinition.class, AlipaySourceAdapter::new));
        register(
                bindings,
                AliyunDefinition.ID,
                AliyunDefinition.DEFAULT,
                factory(AliyunDefinition.class, AliyunSourceAdapter::new));
        register(
                bindings,
                AmazonDefinition.ID,
                AmazonDefinition.DEFAULT,
                factory(AmazonDefinition.class, AmazonSourceAdapter::new));
        register(
                bindings,
                AppleDefinition.ID,
                AppleDefinition.DEFAULT,
                factory(AppleDefinition.class, AppleSourceAdapter::new));
        register(
                bindings,
                BaiduDefinition.ID,
                BaiduDefinition.DEFAULT,
                factory(BaiduDefinition.class, BaiduSourceAdapter::new));
        register(
                bindings,
                CodingDefinition.ID,
                CodingDefinition.DEFAULT,
                factory(CodingDefinition.class, CodingSourceAdapter::new));
        register(
                bindings,
                DingTalkDefinition.ID,
                DingTalkDefinition.OAUTH2,
                factory(DingTalkDefinition.class, DingTalkSourceAdapter::new));
        register(
                bindings,
                DingTalkDefinition.ID,
                DingTalkDefinition.ACCOUNT,
                factory(DingTalkDefinition.class, DingTalkSourceAdapter::new));
        register(
                bindings,
                DouyinDefinition.ID,
                DouyinDefinition.OPEN,
                factory(DouyinDefinition.class, DouyinSourceAdapter::new));
        register(
                bindings,
                DouyinDefinition.ID,
                DouyinDefinition.MINI_PROGRAM,
                factory(DouyinDefinition.class, DouyinSourceAdapter::new));
        register(
                bindings,
                ElemeDefinition.ID,
                ElemeDefinition.DEFAULT,
                factory(ElemeDefinition.class, ElemeSourceAdapter::new));
        register(
                bindings,
                FacebookDefinition.ID,
                FacebookDefinition.DEFAULT,
                factory(FacebookDefinition.class, FacebookSourceAdapter::new));
        register(
                bindings,
                FeishuDefinition.ID,
                FeishuDefinition.DEFAULT,
                factory(FeishuDefinition.class, FeishuSourceAdapter::new));
        register(
                bindings,
                FigmaDefinition.ID,
                FigmaDefinition.DEFAULT,
                factory(FigmaDefinition.class, FigmaSourceAdapter::new));
        register(
                bindings,
                GiteeDefinition.ID,
                GiteeDefinition.DEFAULT,
                factory(GiteeDefinition.class, GiteeSourceAdapter::new));
        register(
                bindings,
                GitHubDefinition.ID,
                GitHubDefinition.DEFAULT,
                factory(GitHubDefinition.class, GitHubSourceAdapter::new));
        register(
                bindings,
                GitLabDefinition.ID,
                GitLabDefinition.DEFAULT,
                factory(GitLabDefinition.class, GitLabSourceAdapter::new));
        register(
                bindings,
                GoogleDefinition.ID,
                GoogleDefinition.DEFAULT,
                factory(GoogleDefinition.class, GoogleSourceAdapter::new));
        register(
                bindings,
                HuaweiDefinition.ID,
                HuaweiDefinition.DEFAULT,
                factory(HuaweiDefinition.class, HuaweiSourceAdapter::new));
        register(bindings, JdDefinition.ID, JdDefinition.DEFAULT, factory(JdDefinition.class, JdSourceAdapter::new));
        register(
                bindings,
                KujialeDefinition.ID,
                KujialeDefinition.DEFAULT,
                factory(KujialeDefinition.class, KujialeSourceAdapter::new));
        register(
                bindings,
                LineDefinition.ID,
                LineDefinition.DEFAULT,
                factory(LineDefinition.class, LineSourceAdapter::new));
        register(
                bindings,
                LinkedInDefinition.ID,
                LinkedInDefinition.DEFAULT,
                factory(LinkedInDefinition.class, LinkedInSourceAdapter::new));
        register(
                bindings,
                MeituanDefinition.ID,
                MeituanDefinition.DEFAULT,
                factory(MeituanDefinition.class, MeituanSourceAdapter::new));
        register(bindings, MiDefinition.ID, MiDefinition.DEFAULT, factory(MiDefinition.class, MiSourceAdapter::new));
        register(
                bindings,
                MicrosoftDefinition.ID,
                MicrosoftDefinition.GLOBAL,
                factory(MicrosoftDefinition.class, MicrosoftSourceAdapter::new));
        register(
                bindings,
                MicrosoftDefinition.ID,
                MicrosoftDefinition.CHINA,
                factory(MicrosoftDefinition.class, MicrosoftSourceAdapter::new));
        register(
                bindings,
                OktaDefinition.ID,
                OktaDefinition.DEFAULT,
                factory(OktaDefinition.class, OktaSourceAdapter::new));
        register(
                bindings,
                OsChinaDefinition.ID,
                OsChinaDefinition.DEFAULT,
                factory(OsChinaDefinition.class, OsChinaSourceAdapter::new));
        register(
                bindings,
                PinterestDefinition.ID,
                PinterestDefinition.DEFAULT,
                factory(PinterestDefinition.class, PinterestSourceAdapter::new));
        register(
                bindings,
                ProginnDefinition.ID,
                ProginnDefinition.DEFAULT,
                factory(ProginnDefinition.class, ProginnSourceAdapter::new));
        register(bindings, QqDefinition.ID, QqDefinition.OPEN, factory(QqDefinition.class, QqSourceAdapter::new));
        register(
                bindings,
                QqDefinition.ID,
                QqDefinition.MINI_PROGRAM,
                factory(QqDefinition.class, QqSourceAdapter::new));
        register(
                bindings,
                RedNoteDefinition.ID,
                RedNoteDefinition.MARKETING,
                factory(RedNoteDefinition.class, RedNoteSourceAdapter::new));
        register(
                bindings,
                SlackDefinition.ID,
                SlackDefinition.DEFAULT,
                factory(SlackDefinition.class, SlackSourceAdapter::new));
        register(
                bindings,
                StackOverflowDefinition.ID,
                StackOverflowDefinition.DEFAULT,
                factory(StackOverflowDefinition.class, StackOverflowSourceAdapter::new));
        register(
                bindings,
                TaobaoDefinition.ID,
                TaobaoDefinition.DEFAULT,
                factory(TaobaoDefinition.class, TaobaoSourceAdapter::new));
        register(
                bindings,
                TeambitionDefinition.ID,
                TeambitionDefinition.DEFAULT,
                factory(TeambitionDefinition.class, TeambitionSourceAdapter::new));
        register(
                bindings,
                ToutiaoDefinition.ID,
                ToutiaoDefinition.DEFAULT,
                factory(ToutiaoDefinition.class, ToutiaoSourceAdapter::new));
        register(
                bindings,
                TwitterDefinition.ID,
                TwitterDefinition.OAUTH1,
                factory(TwitterDefinition.class, TwitterSourceAdapter::new));
        register(bindings, VkDefinition.ID, VkDefinition.DEFAULT, factory(VkDefinition.class, VkSourceAdapter::new));
        register(
                bindings,
                WeChatDefinition.ID,
                WeChatDefinition.OPEN,
                factory(WeChatDefinition.class, WeChatOpenAdapter::new));
        register(
                bindings,
                WeChatDefinition.ID,
                WeChatDefinition.MP,
                factory(WeChatDefinition.class, WeChatMpAdapter::new));
        register(
                bindings,
                WeChatDefinition.ID,
                WeChatDefinition.MINI,
                factory(WeChatDefinition.class, WeChatMiniAdapter::new));
        register(
                bindings,
                WeChatDefinition.ID,
                WeChatDefinition.EE,
                factory(WeChatDefinition.class, WeChatEeAdapter::new));
        register(
                bindings,
                WeChatDefinition.ID,
                WeChatDefinition.EE_QRCODE,
                factory(WeChatDefinition.class, WeChatEeAdapter::new));
        register(
                bindings,
                WeChatDefinition.ID,
                WeChatDefinition.EE_WEB,
                factory(WeChatDefinition.class, WeChatEeAdapter::new));
        register(
                bindings,
                WeiboDefinition.ID,
                WeiboDefinition.DEFAULT,
                factory(WeiboDefinition.class, WeiboSourceAdapter::new));
        register(
                bindings,
                XimalayaDefinition.ID,
                XimalayaDefinition.DEFAULT,
                factory(XimalayaDefinition.class, XimalayaSourceAdapter::new));
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
     * @param factory  exact typed adapter factory
     * @throws ValidateException if the platform-variant key already exists
     */
    private static void register(
            final Map<AdapterBindings.Key, VendorAdapter.Factory<?>> bindings,
            final Vendor.Id vendor,
            final Vendor.Variant variant,
            final VendorAdapter.Factory<?> factory) {
        final AdapterBindings.Key key = new AdapterBindings.Key(vendor, variant);
        if (bindings.putIfAbsent(key, factory) != null) {
            throw new ValidateException(
                    "Duplicate Vendor adapter factory: " + vendor.value() + Symbol.C_SLASH + variant.value());
        }
    }

    /**
     * Adapts a concrete Vendor definition constructor to the module-internal generic factory contract.
     *
     * @param definitionType exact concrete Vendor definition class
     * @param constructor    exact concrete adapter constructor
     * @param <D>            concrete Vendor definition type
     * @param <S>            concrete platform settings type
     * @return type-safe adapter factory with runtime definition-class verification
     */
    private static <D extends VendorDefinition<S>, S extends VendorSettings> VendorAdapter.Factory<S> factory(
            final Class<D> definitionType,
            final AdapterConstructor<D, S> constructor) {
        Assert.notNull(definitionType, "Vendor definition class must not be null");
        Assert.notNull(constructor, "Vendor adapter constructor must not be null");
        return (namespaceId, sourceId, vendorDefinition, variantDefinition, settings, services) -> constructor.create(
                namespaceId,
                sourceId,
                definitionType.cast(vendorDefinition),
                variantDefinition,
                settings,
                services);
    }

    /**
     * Represents one concrete platform adapter constructor without weakening its definition or settings type.
     *
     * @param <D> concrete Vendor definition type
     * @param <S> concrete platform settings type
     * @author Kimi Liu
     */
    @FunctionalInterface
    private interface AdapterConstructor<D extends VendorDefinition<S>, S extends VendorSettings> {

        /**
         * Creates one Source-bound concrete platform adapter.
         *
         * @param namespaceId       registration namespace identifier
         * @param sourceId          registration Source identifier
         * @param vendorDefinition  exact concrete Vendor definition
         * @param variantDefinition exact selected variant definition
         * @param settings          decoded concrete platform settings
         * @param services          complete caller-owned runtime dependencies
         * @return non-null concrete adapter
         */
        VendorAdapter create(
                String namespaceId,
                String sourceId,
                D vendorDefinition,
                VendorDefinition.Definition variantDefinition,
                S settings,
                ExecutionServices services);

    }

}
