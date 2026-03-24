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
 * WeChat Pay V3 API interfaces related to education and training renewal services.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum EduApi implements Matcher {

    /**
     * Query contract by contract number.
     */
    QUERY_CONTRACTS_BY_ID("/v3/edu-papay/contracts/id/%s", "Query contract by contract number"),

    /**
     * Pre-sign a contract.
     */
    PRE_SIGN("/v3/edu-papay/contracts/presign", "Pre-sign a contract"),

    /**
     * Terminate contract.
     */
    DELETE_CONTRACTS("/v3/edu-papay/contracts/%s", "Terminate contract"),

    /**
     * Query contract by user identifier.
     */
    QUERY_CONTRACTS_BY_USER("/v3/edu-papay/user/%s/contracts", "Query contract by user identifier"),

    /**
     * Accept deduction.
     */
    TRANSACTIONS("/v3/edu-papay/transactions", "Accept deduction"),

    /**
     * Query order by WeChat transaction ID.
     */
    QUERY_TRANSACTIONS_BY_TRANSACTION_ID("/v3/edu-papay/transactions/id/%s", "Query order by WeChat transaction ID"),

    /**
     * Query order by merchant order number.
     */
    QUERY_TRANSACTIONS_BY_OUT_TRADE_NO("/v3/edu-papay/transactions/out-trade-no/%s",
            "Query order by merchant order number"),

    /**
     * Send deduction pre-notification.
     */
    SEND_NOTIFICATION("/v3/edu-papay/user-notifications/%s/send", "Send deduction pre-notification");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new EduApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    EduApi(String method, String desc) {
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
