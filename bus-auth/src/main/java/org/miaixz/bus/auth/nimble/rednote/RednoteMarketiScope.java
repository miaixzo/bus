/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.rednote;

import org.miaixz.bus.auth.nimble.AuthorizeScope;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Xiaohongshu Commercial Platform OAuth authorization scopes.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum RednoteMarketiScope implements AuthorizeScope {

    /**
     * Get account report information. The meaning of {@code scope} is subject to {@code description}.
     */
    report_service("report_service", "Get account report information", true),
    /**
     * Get promotion plan, promotion unit, and promotion creative information.
     */
    ad_query("ad_query", "Get promotion plan, promotion unit, and promotion creative information", false),
    /**
     * Create / modify promotion plans, promotion units, and promotion creatives.
     */
    ad_manage("ad_manage", "Create & modify promotion plans, promotion units, and promotion creatives", false),
    /**
     * Account management.
     */
    account_manage("account_manage", "Account management", false);

    /**
     * The scope string as defined by Xiaohongshu.
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
