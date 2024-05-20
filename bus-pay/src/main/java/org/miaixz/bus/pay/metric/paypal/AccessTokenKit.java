package org.miaixz.bus.pay.metric.paypal;

import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.magic.Results;
import org.miaixz.bus.pay.metric.PayCache;
import org.miaixz.bus.pay.metric.RetryKit;

/**
 * 令牌工具
 */
public class AccessTokenKit {

    private static final PayCache cache = PayCache.INSTANCE;


    /**
     * 获取当前线程中的 AccessToken
     *
     * @return {@link AccessToken}
     */
    public static AccessToken get() {
        return get((String) cache.get("clientId"), false);
    }

    /**
     * 获取当前线程中的 AccessToken
     *
     * @param forceRefresh 是否强制刷新
     * @return {@link AccessToken}
     */
    public static AccessToken get(boolean forceRefresh) {
        return get((String) cache.get("clientId"), forceRefresh);
    }

    /**
     * 通过 clientId 来获取  AccessToken
     *
     * @param clientId 应用编号
     * @return {@link AccessToken}
     */
    public static AccessToken get(String clientId) {
        return get(clientId, false);
    }

    /**
     * 通过 clientId 来获取  AccessToken
     *
     * @param clientId     应用编号
     * @param forceRefresh 是否强制刷新
     * @return {@link AccessToken}
     */
    public static AccessToken get(String clientId, boolean forceRefresh) {
        // 从缓存中获取 AccessToken
        if (!forceRefresh) {
            String json = (String) cache.get(clientId);
            if (StringKit.isNotEmpty(json)) {
                AccessToken accessToken = new AccessToken(json, 200);
                if (accessToken.isAvailable()) {
                    return accessToken;
                }
            }
        }

        Context context = (Context) cache.get("clientId");

        AccessToken result = RetryKit.retryOnException(3, () -> {
            Results response = PaypalProvider.getToken(context);
            return new AccessToken(response.getBody(), response.getStatus());
        });

        // 三次请求如果仍然返回了不可用的 AccessToken 仍然 put 进去，便于上层通过 AccessToken 中的属性判断底层的情况
        if (null != result) {
            // 利用 clientId 与 accessToken 建立关联，支持多账户
            cache.cache(clientId, result.getCacheJson());
        }
        return result;
    }

}
