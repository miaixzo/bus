package org.miaixz.bus.pay.provider.alipay;

import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.pay.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付配置工具
 */
public class AliPayKit {

    private static final ThreadLocal<String> TL = new ThreadLocal<>();
    private static final Map<String, Context> CFG_MAP = new ConcurrentHashMap<>();
    private static final String DEFAULT_CFG_KEY = "_default_key_";

    /**
     * <p>向缓存中设置 AliPayApiConfig </p>
     * <p>每个 appId 只需添加一次，相同 appId 将被覆盖</p>
     *
     * @param context 支付宝支付配置
     * @return {@link Context}
     */
    public static Context putApiConfig(Context context) {
        if (CFG_MAP.size() == 0) {
            CFG_MAP.put(DEFAULT_CFG_KEY, context);
        }
        return CFG_MAP.put(Context.getAppId(), context);
    }

    /**
     * 向当前线程中设置 {@link Context}
     *
     * @param context {@link Context} 支付宝配置对象
     * @return {@link Context}
     */
    public static Context setThreadLocalAliPayApiConfig(Context context) {
        if (StringKit.isNotEmpty(Context.getAppId())) {
            setThreadLocalAppId(Context.getAppId());
        }
        return putApiConfig(context);
    }

    /**
     * 通过 AliPayApiConfig 移除支付配置
     *
     * @param context {@link Context} 支付宝配置对象
     * @return {@link Context}
     */
    public static Context removeApiConfig(Context context) {
        return removeApiConfig(Context.getAppId());
    }

    /**
     * 通过 appId 移除支付配置
     *
     * @param appId 支付宝应用编号
     * @return {@link Context}
     */
    public static Context removeApiConfig(String appId) {
        return CFG_MAP.remove(appId);
    }

    /**
     * 向当前线程中设置 appId
     *
     * @param appId 支付宝应用编号
     */
    public static void setThreadLocalAppId(String appId) {
        if (StringKit.isEmpty(appId)) {
            appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();
        }
        TL.set(appId);
    }

    /**
     * 移除当前线程中的 appId
     */
    public static void removeThreadLocalAppId() {
        TL.remove();
    }

    /**
     * 获取当前线程中的  appId
     *
     * @return 支付宝应用编号 appId
     */
    public static String getAppId() {
        String appId = TL.get();
        if (StringKit.isEmpty(appId)) {
            appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();
        }
        return appId;
    }

    /**
     * 获取当前线程中的 AliPayApiConfig
     *
     * @return {@link Context}
     */
    public static Context getAliPayApiConfig() {
        String appId = getAppId();
        return getApiConfig(appId);
    }

    /**
     * 通过 appId 获取 AliPayApiConfig
     *
     * @param appId 支付宝应用编号
     * @return {@link Context}
     */
    public static Context getApiConfig(String appId) {
        Context context = CFG_MAP.get(appId);
        if (context == null) {
            throw new IllegalStateException("需事先调用 AliPayApiConfigKit.putApiConfig(aliPayApiConfig) 将 appId对应的 aliPayApiConfig 对象存入，才可以使用 AliPayApiConfigKit.getAliPayApiConfig() 的系列方法");
        }
        return context;
    }

}