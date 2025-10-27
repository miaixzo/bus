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
package org.miaixz.bus.starter.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.cache.AuthCache;
import org.miaixz.bus.auth.magic.ErrorCode;
import org.miaixz.bus.auth.nimble.afdian.AfDianProvider;
import org.miaixz.bus.auth.nimble.alipay.AlipayProvider;
import org.miaixz.bus.auth.nimble.aliyun.AliyunProvider;
import org.miaixz.bus.auth.nimble.amazon.AmazonProvider;
import org.miaixz.bus.auth.nimble.baidu.BaiduProvider;
import org.miaixz.bus.auth.nimble.coding.CodingProvider;
import org.miaixz.bus.auth.nimble.dingtalk.DingTalkProvider;
import org.miaixz.bus.auth.nimble.douyin.DouyinProvider;
import org.miaixz.bus.auth.nimble.eleme.ElemeProvider;
import org.miaixz.bus.auth.nimble.facebook.FacebookProvider;
import org.miaixz.bus.auth.nimble.feishu.FeishuProvider;
import org.miaixz.bus.auth.nimble.gitee.GiteeProvider;
import org.miaixz.bus.auth.nimble.github.GithubProvider;
import org.miaixz.bus.auth.nimble.gitlab.GitlabProvider;
import org.miaixz.bus.auth.nimble.google.GoogleProvider;
import org.miaixz.bus.auth.nimble.huawei.HuaweiProvider;
import org.miaixz.bus.auth.nimble.jd.JdProvider;
import org.miaixz.bus.auth.nimble.kujiale.KujialeProvider;
import org.miaixz.bus.auth.nimble.line.LineProvider;
import org.miaixz.bus.auth.nimble.linkedin.LinkedinProvider;
import org.miaixz.bus.auth.nimble.meituan.MeituanProvider;
import org.miaixz.bus.auth.nimble.mi.MiProvider;
import org.miaixz.bus.auth.nimble.microsoft.MicrosoftCnProvider;
import org.miaixz.bus.auth.nimble.microsoft.MicrosoftProvider;
import org.miaixz.bus.auth.nimble.oidc.OidcProvider;
import org.miaixz.bus.auth.nimble.okta.OktaProvider;
import org.miaixz.bus.auth.nimble.oschina.OschinaProvider;
import org.miaixz.bus.auth.nimble.pinterest.PinterestProvider;
import org.miaixz.bus.auth.nimble.proginn.ProginnProvider;
import org.miaixz.bus.auth.nimble.qq.QqProvider;
import org.miaixz.bus.auth.nimble.renren.RenrenProvider;
import org.miaixz.bus.auth.nimble.slack.SlackProvider;
import org.miaixz.bus.auth.nimble.stackoverflow.StackOverflowProvider;
import org.miaixz.bus.auth.nimble.taobao.TaobaoProvider;
import org.miaixz.bus.auth.nimble.teambition.TeambitionProvider;
import org.miaixz.bus.auth.nimble.toutiao.ToutiaoProvider;
import org.miaixz.bus.auth.nimble.twitter.TwitterProvider;
import org.miaixz.bus.auth.nimble.wechat.ee.WeChatEeQrcodeProvider;
import org.miaixz.bus.auth.nimble.wechat.ee.WeChatEeThirdQrcodeProvider;
import org.miaixz.bus.auth.nimble.wechat.ee.WeChatEeWebProvider;
import org.miaixz.bus.auth.nimble.wechat.mp.WeChatMpProvider;
import org.miaixz.bus.auth.nimble.wechat.open.WeChatOpenProvider;
import org.miaixz.bus.auth.nimble.weibo.WeiboProvider;
import org.miaixz.bus.auth.nimble.ximalaya.XimalayaProvider;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Authorization service provider class for managing and creating various third-party login/authorization service
 * provider instances. This class maintains a cache of authorization components and supports adding them through
 * configuration or manual registration. It supports a wide range of third-party platforms, including but not limited
 * to:
 * <ul>
 * <li>Domestic Platforms: WeChat, QQ, Weibo, Alipay, Taobao, Baidu, Huawei, etc.</li>
 * <li>International Platforms: GitHub, Google, Facebook, Twitter, Microsoft, etc.</li>
 * <li>Enterprise Platforms: DingTalk, Feishu, WeChat Work, etc.</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>{@code
 * 
 * // Create configuration properties
 * AuthProperties properties = new AuthProperties();
 * // Create the authorization service
 * AuthService service = new AuthService(properties);
 * // Get the GitHub authorization provider
 * Provider provider = service.require(Registry.GITHUB);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuthService {

    /**
     * Cache for storing registered authorization components. Uses {@link ConcurrentHashMap} for thread safety.
     */
    private static final Map<Registry, Context> CACHE = new ConcurrentHashMap<>();

    /**
     * Authorization configuration properties, containing settings for various authorization components.
     */
    public AuthProperties properties;

    /**
     * Cache interface for storing temporary data during the authorization process.
     */
    public CacheX cache;

    /**
     * Constructs an instance of the authorization service provider with the default cache.
     *
     * @param properties The authorization configuration properties (must not be null).
     */
    public AuthService(AuthProperties properties) {
        this(properties, AuthCache.INSTANCE);
    }

    /**
     * Constructs an instance of the authorization service provider with a specified cache.
     *
     * @param properties The authorization configuration properties (must not be null).
     * @param cache      The cache implementation to use (must not be null).
     */
    public AuthService(AuthProperties properties, CacheX cache) {
        this.properties = properties;
        this.cache = cache;
    }

    /**
     * Registers an authorization component in the cache. Throws an exception if a component of the same type is already
     * registered.
     *
     * @param registry The type of the authorization component (must not be null).
     * @param context  The context of the authorization component (must not be null).
     * @throws InternalException if a component of the same type already exists.
     */
    public static void register(Registry registry, Context context) {
        if (CACHE.containsKey(registry)) {
            throw new InternalException("A component with the same name is already registered: " + registry.name());
        }
        CACHE.putIfAbsent(registry, context);
    }

    /**
     * Retrieves the corresponding authorization service provider instance based on the component type. It first
     * searches the cache; if not found, it retrieves from the configuration.
     *
     * @param registry The type of the authorization component (must not be null).
     * @return The corresponding authorization service provider instance.
     * @throws InternalException if the corresponding authorization component cannot be found.
     */
    public Provider require(Registry registry) {
        // Get the authorization component context from the cache
        Context context = CACHE.get(registry);
        // If not in the cache, get it from the properties
        if (ObjectKit.isEmpty(context)) {
            context = properties.getType().get(registry);
        }

        // Create the corresponding provider instance based on the authorization type
        switch (registry) {
            case AFDIAN:
                return new AfDianProvider(context, cache);

            case ALIPAY:
                return new AlipayProvider(context, cache);

            case ALIYUN:
                return new AliyunProvider(context, cache);

            case AMAZON:
                return new AmazonProvider(context, cache);

            case BAIDU:
                return new BaiduProvider(context, cache);

            case CODING:
                return new CodingProvider(context, cache);

            case DINGTALK:
                return new DingTalkProvider(context, cache);

            case DOUYIN:
                return new DouyinProvider(context, cache);

            case ELEME:
                return new ElemeProvider(context, cache);

            case FACEBOOK:
                return new FacebookProvider(context, cache);

            case FEISHU:
                return new FeishuProvider(context, cache);

            case GITEE:
                return new GiteeProvider(context, cache);

            case GITHUB:
                return new GithubProvider(context, cache);

            case GITLAB:
                return new GitlabProvider(context, cache);

            case GOOGLE:
                return new GoogleProvider(context, cache);

            case HUAWEI:
                return new HuaweiProvider(context, cache);

            case JD:
                return new JdProvider(context, cache);

            case KUJIALE:
                return new KujialeProvider(context, cache);

            case LINE:
                return new LineProvider(context, cache);

            case LINKEDIN:
                return new LinkedinProvider(context, cache);

            case MEITUAN:
                return new MeituanProvider(context, cache);

            case MI:
                return new MiProvider(context, cache);

            case MICROSOFT_CN:
                return new MicrosoftCnProvider(context, cache);

            case MICROSOFT:
                return new MicrosoftProvider(context, cache);

            case OIDC:
                return new OidcProvider(context, cache);

            case OKTA:
                return new OktaProvider(context, cache);

            case OSCHINA:
                return new OschinaProvider(context, cache);

            case PINTEREST:
                return new PinterestProvider(context, cache);

            case PROGINN:
                return new ProginnProvider(context, cache);

            case QQ:
                return new QqProvider(context, cache);

            case RENREN:
                return new RenrenProvider(context, cache);

            case SLACK:
                return new SlackProvider(context, cache);

            case STACK_OVERFLOW:
                return new StackOverflowProvider(context, cache);

            case TAOBAO:
                return new TaobaoProvider(context, cache);

            case TEAMBITION:
                return new TeambitionProvider(context, cache);

            case TOUTIAO:
                return new ToutiaoProvider(context, cache);

            case TWITTER:
                return new TwitterProvider(context, cache);

            case WECHAT_EE:
                return new WeChatEeQrcodeProvider(context, cache);

            case WECHAT_EE_QRCODE:
                return new WeChatEeThirdQrcodeProvider(context, cache);

            case WECHAT_EE_WEB:
                return new WeChatEeWebProvider(context, cache);

            case WECHAT_MP:
                return new WeChatMpProvider(context, cache);

            case WECHAT_OPEN:
                return new WeChatOpenProvider(context, cache);

            case WEIBO:
                return new WeiboProvider(context, cache);

            case XIMALAYA:
                return new XimalayaProvider(context, cache);

            default:
                // If no matching authorization type is found, throw an exception
                throw new InternalException(ErrorCode._100803.getValue());
        }
    }

}
