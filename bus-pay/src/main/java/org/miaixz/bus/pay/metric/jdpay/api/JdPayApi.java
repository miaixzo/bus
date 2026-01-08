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
package org.miaixz.bus.pay.metric.jdpay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * JD Pay APIs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum JdPayApi implements Matcher {

    /**
     * PC Online Payment Interface.
     */
    PC_SAVE_ORDER_URL("https://wepay.jd.com/jdpay/saveOrder", "PC Online Payment Interface"),
    /**
     * H5 Online Payment Interface.
     */
    H5_SAVE_ORDER_URL("https://h5pay.jd.com/jdpay/saveOrder", "H5 Online Payment Interface"),
    /**
     * Unified Order Interface.
     */
    UNI_ORDER_URL("https://paygate.jd.com/service/uniorder", "Unified Order Interface"),
    /**
     * Merchant QR Code Payment Interface.
     */
    CUSTOMER_PAY_URL("https://h5pay.jd.com/jdpay/customerPay", "Merchant QR Code Payment Interface"),
    /**
     * Payment Code Interface.
     */
    FKM_PAY_URL("https://paygate.jd.com/service/fkmPay", "Payment Code Interface"),
    /**
     * Baitiao Installment Plan Query Interface.
     */
    QUERY_BAI_TIAO_FQ_URL("https://paygate.jd.com/service/queryBaiTiaoFQ", "Baitiao Installment Plan Query Interface"),
    /**
     * Transaction Query Interface.
     */
    QUERY_ORDER_URL("https://paygate.jd.com/service/query", "Transaction Query Interface"),
    /**
     * Refund Application Interface.
     */
    REFUND_URL("https://paygate.jd.com/service/refund", "Refund Application Interface"),
    /**
     * Revocation Application Interface.
     */
    REVOKE_URL("https://paygate.jd.com/service/revoke", "Revocation Application Interface"),
    /**
     * User Relationship Query Interface.
     */
    GET_USER_RELATION_URL("https://paygate.jd.com/service/getUserRelation", "User Relationship Query Interface"),
    /**
     * User Relationship Unbinding Interface.
     */
    CANCEL_USER_RELATION_URL("https://paygate.jd.com/service/cancelUserRelation",
            "User Relationship Unbinding Interface");

    /**
     * The API method URL.
     */
    private final String method;
    /**
     * The description of the API.
     */
    private final String desc;

    /**
     * Constructs a new JdPayApi.
     *
     * @param method The API method URL.
     * @param desc   The description of the API.
     */
    JdPayApi(String method, String desc) {
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
     * Gets the API method URL.
     *
     * @return The API method URL.
     */
    @Override
    public String method() {
        return this.method;
    }

}
