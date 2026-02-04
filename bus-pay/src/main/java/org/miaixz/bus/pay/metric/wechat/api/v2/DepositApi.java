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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V2 API interfaces related to deposits.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum DepositApi implements Matcher {

    /**
     * Pay deposit (JSAPI, APP order).
     */
    PAY("/deposit/unifiedorder", "Pay deposit (JSAPI, APP order)"),

    /**
     * Pay deposit (face payment).
     */
    FACE_PAY("/deposit/facepay", "Pay deposit (face payment)"),

    /**
     * Pay deposit (micropay).
     */
    MICRO_PAY("/deposit/micropay", "Pay deposit (micropay)"),

    /**
     * Query order.
     */
    ORDER_QUERY("/deposit/orderquery", "Query order"),

    /**
     * Reverse order.
     */
    REVERSE("/deposit/reverse", "Reverse order"),

    /**
     * Consume deposit.
     */
    CONSUME("/deposit/consume", "Consume deposit"),

    /**
     * Apply for refund.
     */
    REFUND("/deposit/refund", "Apply for refund"),

    /**
     * Query refund.
     */
    REFUND_QUERY("deposit/refundquery", "Query refund");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new DepositApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    DepositApi(String method, String desc) {
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
