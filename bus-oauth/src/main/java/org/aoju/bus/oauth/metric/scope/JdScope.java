package org.aoju.bus.oauth.metric.scope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * 京东平台 OAuth 授权范围
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Getter
@AllArgsConstructor
public enum JdScope implements OauthScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    SNSAPI_BASE("snsapi_base", "基础授权", true);

    private String scope;
    private String description;
    private boolean isDefault;

}
