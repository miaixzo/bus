package org.aoju.bus.oauth.metric;

/**
 * 各个平台 scope 类的统一接口
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
public interface OauthScope {

    /**
     * 获取字符串 {@code scope}，对应为各平台实际使用的 {@code scope}
     *
     * @return String
     */
    String getScope();

    /**
     * 判断当前 {@code scope} 是否为各平台默认启用的
     *
     * @return boolean
     */
    boolean isDefault();

}
