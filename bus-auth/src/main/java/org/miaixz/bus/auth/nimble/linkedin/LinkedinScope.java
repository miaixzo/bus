/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.linkedin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * LinkedIn authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum LinkedinScope implements AuthorizeScope {

    /**
     * Use your name, headline, and photo. The meaning of {@code scope} is subject to {@code description}.
     */
    R_LITEPROFILE("r_liteprofile", "Use your name, headline, and photo", true),
    /**
     * Use the primary email address associated with your LinkedIn account.
     */
    R_EMAILADDRESS("r_emailaddress", "Use the primary email address associated with your LinkedIn account", true),
    /**
     * Post, comment and like posts on your behalf.
     */
    W_MEMBER_SOCIAL("w_member_social", "Post, comment and like posts on your behalf", true),
    /**
     * Retrieve your posts, comments, likes, and other engagement data.
     */
    R_MEMBER_SOCIAL("r_member_social", "Retrieve your posts, comments, likes, and other engagement data", false),
    /**
     * View advertising campaigns you manage.
     */
    R_AD_CAMPAIGNS("r_ad_campaigns", "View advertising campaigns you manage", false),
    /**
     * Retrieve your advertising accounts.
     */
    R_ADS("r_ads", "Retrieve your advertising accounts", false),
    /**
     * Access your Lead Gen Forms and retrieve leads.
     */
    R_ADS_LEADGEN_AUTOMATION("r_ads_leadgen_automation", "Access your Lead Gen Forms and retrieve leads", false),
    /**
     * Retrieve reporting for your advertising accounts.
     */
    R_ADS_REPORTING("r_ads_reporting", "Retrieve reporting for your advertising accounts", false),
    /**
     * Use your basic profile including your name, photo, headline, and current positions.
     */
    R_BASICPROFILE("r_basicprofile",
            "Use your basic profile including your name, photo, headline, and current positions", false),
    /**
     * Retrieve your organizations' posts, including any comments, likes and other engagement data.
     */
    R_ORGANIZATION_SOCIAL("r_organization_social",
            "Retrieve your organizations' posts, including any comments, likes and other engagement data", false),
    /**
     * Manage your advertising campaigns.
     */
    RW_AD_CAMPAIGNS("rw_ad_campaigns", "Manage your advertising campaigns", false),
    /**
     * Manage your advertising accounts.
     */
    RW_ADS("rw_ads", "Manage your advertising accounts", false),
    /**
     * For V1 calls: Manage your organization's page and post updates.
     */
    RW_COMPANY_ADMIN("rw_company_admin", "For V1 callsManage your organization's page and post updates", false),
    /**
     * Create and manage your matched audiences.
     */
    RW_DMP_SEGMENTS("rw_dmp_segments", "Create and manage your matched audiences", false),
    /**
     * Manage your organizations' pages and retrieve reporting data.
     */
    RW_ORGANIZATION_ADMIN("rw_organization_admin", "Manage your organizations' pages and retrieve reporting data",
            false),
    /**
     * For V2 calls: Manage your organization's page and post updates.
     */
    RW_ORGANIZATION("rw_organization", "For V2 callsManage your organization's page and post updates", false),
    /**
     * Post, comment and like posts on your organization's behalf.
     */
    W_ORGANIZATION_SOCIAL("w_organization_social", "Post, comment and like posts on your organization's behalf", false),
    /**
     * Post updates to LinkedIn as you.
     */
    W_SHARE("w_share", "Post updates to LinkedIn as you", false);

    /**
     * The scope string as defined by LinkedIn.
     */
    private final String scope;
    /**
     * A description of what the scope grants access to.
     */
    private final String description;
    /**
     * Indicates if this scope is enabled by default.
     */
    private final boolean isDefault;

}
