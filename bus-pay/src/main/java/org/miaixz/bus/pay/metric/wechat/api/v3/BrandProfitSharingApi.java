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
 * WeChat Pay V3 API interfaces for brand profit sharing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BrandProfitSharingApi implements Matcher {

    /**
     * Profit sharing/query profit sharing.
     */
    ORDERS("/v3/brand/profitsharing/orders", "Profit sharing/query profit sharing"),

    /**
     * Profit sharing return - query profit sharing return.
     */
    RETURN_ORDERS("/v3/brand/profitsharing/returnorders", "Profit sharing return - query profit sharing return"),

    /**
     * Complete profit sharing.
     */
    FINISH_ORDER("/v3/brand/profitsharing/finish-order", "Complete profit sharing"),

    /**
     * Query remaining amount to be shared for the order.
     */
    QUERY("/v3/brand/profitsharing/orders/%s/amounts", "Query remaining amount to be shared for the order"),

    /**
     * Query maximum profit sharing ratio.
     */
    BRAND__CONFIGS("/v3/brand/profitsharing/brand-configs/%s", "Query maximum profit sharing ratio"),

    /**
     * Add profit sharing receiver.
     */
    RECEIVERS_ADD("/v3/brand/profitsharing/receivers/add", "Add profit sharing receiver"),

    /**
     * Delete profit sharing receiver.
     */
    RECEIVERS_DELETE("/v3/brand/profitsharing/receivers/delete", "Delete profit sharing receiver");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new BrandProfitSharingApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    BrandProfitSharingApi(String method, String desc) {
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
