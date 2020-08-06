package org.aoju.bus.oauth.metric.scope;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aoju.bus.oauth.metric.OauthScope;

/**
 * 酷家乐平台 OAuth 授权范围
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
@Getter
@AllArgsConstructor
public enum KujialeScope implements OauthScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    GET_USER_INFO("get_user_info", "获取用户的基本信息", true),
    GET_DESIGN("get_design", "获取指定方案详情", false),
    GET_BUDGET_LIST("get_budget_list", "获取清单预算概览数据", false);

    private String scope;
    private String description;
    private boolean isDefault;

}
