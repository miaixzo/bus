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
 * WeChat Pay V3 API interfaces related to payroll cards.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayrollCardApi implements Matcher {

    /**
     * Generate authorization token.
     */
    TOKEN("/v3/payroll-card/tokens", "Generate authorization token"),

    /**
     * Query payroll card authorization relationship.
     */
    RELATION("/v3/payroll-card/relations/%s", "Query payroll card authorization relationship"),

    /**
     * Payroll card identity verification pre-order.
     */
    AUTHENTICATION_PRE_ORDER("/v3/payroll-card/authentications/pre-order",
            "Payroll card identity verification pre-order"),

    /**
     * Get identity verification result.
     */
    AUTHENTICATION_RESULT("/v3/payroll-card/authentications/%s", "Get identity verification result"),

    /**
     * Query identity verification records.
     */
    AUTHENTICATION_LIST("/v3/payroll-card/authentications", "Query identity verification records"),

    /**
     * Payroll card identity verification pre-order (authorization completed in the process).
     */
    PRE_ORDER_WITH_AUTH("/v3/payroll-card/authentications/pre-order-with-auth",
            "Payroll card identity verification pre-order (authorization completed in the process)"),

    /**
     * Initiate batch transfer.
     */
    BATCH_TRANSFER("/v3/payroll-card/transfer-batches", "Initiate batch transfer");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new PayrollCardApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    PayrollCardApi(String method, String desc) {
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
