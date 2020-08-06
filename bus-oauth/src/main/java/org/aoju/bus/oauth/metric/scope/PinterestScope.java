package org.aoju.bus.oauth.metric.scope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * Pinterest 平台 OAuth 授权范围
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Getter
@AllArgsConstructor
public enum PinterestScope implements OauthScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    READ_PUBLIC("read_public", "Use GET method on a user’s Pins, boards.", true),
    WRITE_PUBLIC("write_public", "Use PATCH, POST and DELETE methods on a user’s Pins and boards.", false),
    READ_RELATIONSHIPS("read_relationships", "Use GET method on a user’s follows and followers (on boards, users and interests).", false),
    WRITE_RELATIONSHIPS("write_relationships", "Use PATCH, POST and DELETE methods on a user’s follows and followers (on boards, users and interests).", false);

    private String scope;
    private String description;
    private boolean isDefault;

}
