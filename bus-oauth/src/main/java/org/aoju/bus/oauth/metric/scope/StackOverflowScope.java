package org.aoju.bus.oauth.metric.scope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * Stackoverflow 平台 OAuth 授权范围
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Getter
@AllArgsConstructor
public enum StackOverflowScope implements OauthScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    read_inbox("read_inbox", "access a user's global inbox", true),
    NO_EXPIRY("no_expiry", "access_token's with this scope do not expire", false),
    WRITE_ACCESS("write_access", "perform write operations as a user", false),
    PRIVATE_INFO("private_info", "access full history of a user's private actions on the site", false);

    private String scope;
    private String description;
    private boolean isDefault;

}
