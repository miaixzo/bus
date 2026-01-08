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
