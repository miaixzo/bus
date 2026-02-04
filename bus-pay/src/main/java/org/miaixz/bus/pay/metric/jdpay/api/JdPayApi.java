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
