package org.aoju.bus.oauth.metric.scope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * Coding平台 OAuth 授权范围
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Getter
@AllArgsConstructor
public enum CodingScope implements OauthScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    USER("user", "读取用户的基本信息", false),
    USER_EMAIL("user:email", "读取用户的邮件", false),
    USER_PHONE("user:phone", "读取用户的手机号", false),
    PROJECT("project", "授权项目信息、项目列表，仓库信息，公钥列表、成员", false),
    PROJECT_DEPOT("project:depot", "完整的仓库控制权限", false),
    PROJECT_WIKI("project:wiki", "授权读取与操作 wiki", false);

    private String scope;
    private String description;
    private boolean isDefault;

}
