/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
