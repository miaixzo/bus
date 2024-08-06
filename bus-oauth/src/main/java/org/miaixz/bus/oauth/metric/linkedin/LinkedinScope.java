/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.oauth.metric.linkedin;

import org.miaixz.bus.oauth.metric.AuthorizeScope;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 领英 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum LinkedinScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    R_LITEPROFILE("r_liteprofile", "Use your name, headline, and photo", true),
    R_EMAILADDRESS("r_emailaddress", "Use the primary email address associated with your LinkedIn account", true),
    W_MEMBER_SOCIAL("w_member_social", "Post, comment and like posts on your behalf", true),
    R_MEMBER_SOCIAL("r_member_social", "Retrieve your posts, comments, likes, and other engagement data", false),
    R_AD_CAMPAIGNS("r_ad_campaigns", "View advertising campaigns you manage", false),
    R_ADS("r_ads", "Retrieve your advertising accounts", false),
    R_ADS_LEADGEN_AUTOMATION("r_ads_leadgen_automation", "Access your Lead Gen Forms and retrieve leads", false),
    R_ADS_REPORTING("r_ads_reporting", "Retrieve reporting for your advertising accounts", false),
    R_BASICPROFILE("r_basicprofile",
            "Use your basic profile including your name, photo, headline, and current positions", false),
    R_ORGANIZATION_SOCIAL("r_organization_social",
            "Retrieve your organizations' posts, including any comments, likes and other engagement data", false),
    RW_AD_CAMPAIGNS("rw_ad_campaigns", "Manage your advertising campaigns", false),
    RW_ADS("rw_ads", "Manage your advertising accounts", false),
    RW_COMPANY_ADMIN("rw_company_admin", "For V1 callsManage your organization's page and post updates", false),
    RW_DMP_SEGMENTS("rw_dmp_segments", "Create and manage your matched audiences", false),
    RW_ORGANIZATION_ADMIN("rw_organization_admin", "Manage your organizations' pages and retrieve reporting data",
            false),
    RW_ORGANIZATION("rw_organization", "For V2 callsManage your organization's page and post updates", false),
    W_ORGANIZATION_SOCIAL("w_organization_social", "Post, comment and like posts on your organization's behalf", false),
    W_SHARE("w_share", "Post updates to LinkedIn as you", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}
