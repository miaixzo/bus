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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V3 API interfaces for "Pay Gift Activity".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayGiftActivityApi implements Matcher {

    /**
     * Create a full-amount gift activity.
     */
    PAY_GIFT_ACTIVITY("/v3/marketing/paygiftactivity/unique-threshold-activity", "Create a full-amount gift activity"),

    /**
     * Query activity details interface.
     */
    PAY_GIFT_ACTIVITY_INFO("/v3/marketing/paygiftactivity/activities/%s", "Query activity details interface"),

    /**
     * Query activity coupon issuing merchants.
     */
    PAY_GIFT_ACTIVITY_QUERY_MERCHANTS("/v3/marketing/paygiftactivity/activities/%s/merchants",
            "Query activity coupon issuing merchants"),

    /**
     * Query activity designated product list.
     */
    PAY_GIFT_ACTIVITY_QUERY_GOODS("/v3/marketing/paygiftactivity/activities/%s/goods",
            "Query activity designated product list"),

    /**
     * Terminate activity.
     */
    PAY_GIFT_ACTIVITY_TERMINATE("/v3/marketing/paygiftactivity/activities/%s/terminate", "Terminate activity"),

    /**
     * Add activity coupon issuing merchants.
     */
    PAY_GIFT_ACTIVITY_ADD_MERCHANTS("/v3/marketing/paygiftactivity/activities/%s/merchants/add",
            "Add activity coupon issuing merchants"),

    /**
     * Get pay gift activity list.
     */
    PAY_GIFT_ACTIVITY_LIST("/v3/marketing/paygiftactivity/activities", "Get pay gift activity list"),

    /**
     * Delete activity coupon issuing merchants.
     */
    PAY_GIFT_ACTIVITY_DELETE_MERCHANTS("/v3/marketing/paygiftactivity/activities/%s/merchants/delete",
            "Delete activity coupon issuing merchants");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new PayGiftActivityApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    PayGiftActivityApi(String method, String desc) {
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
