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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V2 API interfaces related to deduction services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum EntrustPayApi implements Matcher {

    /**
     * Official Account pure contract signing.
     */
    ENTRUST_WEB("/papay/entrustweb", "Official Account pure contract signing"),

    /**
     * Official Account pure contract signing (service provider mode).
     */
    PARTNER_ENTRUST_WEB("/papay/partner/entrustweb", "Official Account pure contract signing (service provider mode)"),

    /**
     * APP pure contract signing.
     */
    PRE_ENTRUST_WEB("/papay/preentrustweb", "APP pure contract signing"),

    /**
     * APP pure contract signing (service provider mode).
     */
    PARTNER_PRE_ENTRUST_WEB("/papay/partner/preentrustweb", "APP pure contract signing (service provider mode)"),

    /**
     * H5 pure contract signing.
     */
    H5_ENTRUST_WEB("/papay/h5entrustweb", "H5 pure contract signing"),
    /**
     * H5 pure contract signing (service provider mode).
     */
    PARTNER_H5_ENTRUST_WEB("/papay/partner/h5entrustweb", "H5 pure contract signing (service provider mode)"),

    /**
     * Contract signing during payment.
     */
    PAY_CONTRACT_ORDER("/pay/contractorder", "Contract signing during payment"),

    /**
     * Query contract relationship.
     */
    QUERY_ENTRUST_CONTRACT("/papay/querycontract", "Query contract relationship"),

    /**
     * Query contract relationship (service provider mode).
     */
    PARTNER_QUERY_ENTRUST_CONTRACT("/papay/partner/querycontract",
            "Query contract relationship (service provider mode)"),

    /**
     * Apply for deduction.
     */
    PAP_PAY_APPLY("/pay/pappayapply", "Apply for deduction"),

    /**
     * Apply for deduction (service provider mode).
     */
    PARTNER_PAP_PAY_APPLY("/pay/partner/pappayapply", "Apply for deduction (service provider mode)"),

    /**
     * Apply to terminate a contract.
     */
    DELETE_ENTRUST_CONTRACT("/papay/deletecontract", "Apply to terminate a contract"),
    /**
     * Apply to terminate a contract (service provider mode).
     */
    PARTNER_DELETE_ENTRUST_CONTRACT("/papay/partner/deletecontract",
            "Apply to terminate a contract (service provider mode)");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new EntrustPayApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    EntrustPayApi(String method, String desc) {
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
