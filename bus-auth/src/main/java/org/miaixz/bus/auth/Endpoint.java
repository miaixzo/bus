package org.miaixz.bus.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {

    /**
     * 授权端点配置键
     */
    AUTHORIZE,
    /**
     * 访问令牌端点配置键
     */
    ACCESS_TOKEN,
    /**
     * 用户信息端点配置键
     */
    USERINFO,
    /**
     * 刷新令牌端点配置键
     */
    REFRESH,
    /**
     * 撤销授权端点配置键
     */
    REVOKE
}
