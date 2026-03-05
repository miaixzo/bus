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
package org.miaixz.bus.pay.metric.paypal.api;

import org.miaixz.bus.pay.Matcher;

/**
 * PayPal Payment APIs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayPalApi implements Matcher {

    /**
     * Get Access Token.
     */
    GET_TOKEN("/v1/oauth2/token", "Get Access Token"),
    /**
     * Checkout Orders.
     */
    CHECKOUT_ORDERS("/v2/checkout/orders", "Checkout Orders"),
    /**
     * Capture an order.
     */
    CAPTURE_ORDER("/v2/checkout/orders/%s/capture", "Capture an order"),
    /**
     * Query a captured order.
     */
    CAPTURE_QUERY("/v2/payments/captures/%s", "Query a captured order"),
    /**
     * Refund a captured payment.
     */
    REFUND("/v2/payments/captures/%s/refund", "Refund a captured payment"),
    /**
     * Query a refund.
     */
    REFUND_QUERY("/v2/payments/refunds/%s", "Query a refund");

    /**
     * The API endpoint.
     */
    private final String method;
    /**
     * The description of the API.
     */
    private final String desc;

    /**
     * Constructs a new PayPalApi.
     *
     * @param method The API endpoint.
     * @param desc   The description of the API.
     */
    PayPalApi(String method, String desc) {
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
     * Gets the description of the transaction type.
     *
     * @return The description of the transaction type.
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * Gets the API endpoint.
     *
     * @return The API endpoint.
     */
    @Override
    public String method() {
        return this.method;
    }

}
