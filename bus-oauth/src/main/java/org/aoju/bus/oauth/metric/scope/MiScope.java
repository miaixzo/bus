package org.aoju.bus.oauth.metric.scope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * 小米平台 OAuth 授权范围
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Getter
@AllArgsConstructor
public enum MiScope implements OauthScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    profile("user/profile", "获取用户的基本信息", true),
    OPENID("user/openIdV2", "获取用户的OpenID", true),
    PHONE_EMAIL("user/phoneAndEmail", "获取用户的手机号和邮箱", true);

    private String scope;
    private String description;
    private boolean isDefault;

}
