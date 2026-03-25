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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V3 API interfaces related to the Gold Plan.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum GoldPlanApi implements Matcher {

    /**
     * Gold Plan management.
     */
    CHANGE_GOLD_PLAN_STATUS("/v3/goldplan/merchants/changegoldplanstatus", "Gold Plan management"),

    /**
     * Merchant receipt management.
     */
    CHANGE_CUSTOM_PAGE_STATUS("/v3/goldplan/merchants/changecustompagestatus", "Merchant receipt management"),

    /**
     * Set advertising industry filter.
     */
    SET_ADVERTISING_INDUSTRY_FILTER("/v3/goldplan/merchants/set-advertising-industry-filter",
            "Set advertising industry filter"),

    /**
     * Open advertising display.
     */
    OPEN_ADVERTISING_SHOW("/v3/goldplan/merchants/open-advertising-show", "Open advertising display"),

    /**
     * Close advertising display.
     */
    CLOSE_ADVERTISING_SHOW("/v3/goldplan/merchants/close-advertising-show", "Close advertising display");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new GoldPlanApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    GoldPlanApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * Gets the transaction type.
     *
     * @return The transaction type.
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * Gets the type description.
     *
     * @return The type description.
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * Gets the API method.
     *
     * @return The API method.
     */
    @Override
    public String method() {
        return this.method;
    }

}
