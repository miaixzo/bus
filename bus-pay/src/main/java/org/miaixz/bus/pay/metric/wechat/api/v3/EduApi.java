/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V3 API interfaces related to education and training renewal services.
 *
 * @author Kimi Liu
 * @since Java 17+
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
