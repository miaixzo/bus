package org.miaixz.bus.pay.provider;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.PageException;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.pay.Builder;
import org.miaixz.bus.pay.Context;
import org.miaixz.bus.pay.Provider;
import org.miaixz.bus.pay.Registry;
import org.miaixz.bus.pay.metric.PayCache;

public abstract class AbstractProvider implements Provider {

    private static final ThreadLocal<String> TL = new ThreadLocal<>();
    protected Context context;
    protected Registry source;
    protected ExtendCache extendCache;

    public AbstractProvider(Context context, Registry source) {
        this(context, source, PayCache.INSTANCE);
    }

    public AbstractProvider(Context context, Registry source, ExtendCache extendCache) {
        this.context = context;
        this.source = source;
        this.extendCache = extendCache;
        if (!isSupport(context, source)) {
            throw new PageException(Builder.ErrorCode.PARAMETER_INCOMPLETE.getCode());
        }
        // 校验配置合法性
        checkContext(context, source);
    }

    /**
     * 是否支持第三方支付
     *
     * @param context 上下文信息
     * @param source  当前平台
     * @return true or false
     */
    public static boolean isSupport(Context context, Registry source) {
        boolean isSupported = StringKit.isNotEmpty(context.getAppKey())
                && StringKit.isNotEmpty(context.getAppSecret());

        return isSupported;
    }

    /**
     * 检查配置合法性 针对部分平台
     *
     * @param context 上下文信息
     * @param source  当前平台
     */
    public static void checkContext(Context context, Registry source) {

    }

    /**
     * 移除当前线程中的 appId
     */
    public static void remove() {
        TL.remove();
    }

    /**
     * <p>向缓存中设置 AliPayApiConfig </p>
     * <p>每个 appId 只需添加一次，相同 appId 将被覆盖</p>
     *
     * @param context 支付宝支付配置
     */
    public void put(Context context) {
        extendCache.cache(context.getAppId(), context);
    }

    /**
     * 通过 appId 移除支付配置
     *
     * @param appId 支付宝应用编号
     */
    public void remove(String appId) {
        extendCache.remove(appId);
    }

    /**
     * 向当前线程中设置 {@link Context}
     *
     * @param context {@link Context} 支付宝配置对象
     */
    public void setThread(Context context) {
        if (StringKit.isNotEmpty(Context.getAppId())) {
            setThread(Context.getAppId());
        }
        put(context);
    }

    /**
     * 向当前线程中设置 appId
     *
     * @param appId 支付宝应用编号
     */
    public void setThread(String appId) {
        if (StringKit.isEmpty(appId)) {
            appId = (String) this.extendCache.get(appId);
        }
        TL.set(appId);
    }

    /**
     * 获取当前线程中的  appId
     *
     * @return 支付宝应用编号 appId
     */
    public String getAppId() {
        String appId = TL.get();
        if (StringKit.isEmpty(appId)) {
            context = getContext(appId);
        }
        return context.getAppId();
    }

    /**
     * 通过 appId 获取 AliPayApiConfig
     *
     * @param appId 支付宝应用编号
     * @return {@link Context}
     */
    public Context getContext(String appId) {
        Context context = (Context) this.extendCache.get(appId);
        if (context == null) {
            throw new IllegalStateException("需事先调用 AliPayApiConfigKit.putApiConfig(aliPayApiConfig) 将 appId对应的 aliPayApiConfig 对象存入，才可以使用 AliPayApiConfigKit.getAliPayApiConfig() 的系列方法");
        }
        return context;
    }

}
