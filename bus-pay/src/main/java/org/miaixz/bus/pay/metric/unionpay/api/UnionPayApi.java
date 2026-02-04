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
package org.miaixz.bus.pay.metric.unionpay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * UnionPay Cloud QuickPass APIs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum UnionPayApi implements Matcher {

    /**
     * Micropay.
     */
    MICRO_PAY("unified.trade.micropay", "Micropay"),
    /**
     * Native Pay.
     */
    NATIVE("unified.trade.native", "Native Pay"),
    /**
     * WeChat Official Account/Mini Program JS Pay.
     */
    WEI_XIN_JS_PAY("pay.weixin.jspay", "WeChat Official Account/Mini Program JS Pay"),
    /**
     * WeChat App Pay.
     */
    WEI_XIN_APP_PAY("pay.weixin.raw.app", "WeChat App Pay"),
    /**
     * Query Order.
     */
    QUERY("unified.trade.query", "Query Order"),
    /**
     * Apply for Refund.
     */
    REFUND("unified.trade.refund", "Apply for Refund"),
    /**
     * Query Refund.
     */
    REFUND_QUERY("unified.trade.refundquery", "Query Refund"),
    /**
     * Close Order.
     */
    CLOSE("unified.trade.close", "Close Order"),
    /**
     * Reverse Micropay.
     */
    MICRO_PAY_REVERSE("unified.micropay.reverse", "Reverse Micropay"),
    /**
     * Query OpenID by Auth Code.
     */
    AUTH_CODE_TO_OPENID("unified.tools.authcodetoopenid", "Query OpenID by Auth Code"),
    /**
     * Get UserID for UnionPay JS Pay.
     */
    UNION_PAY_USER_ID("pay.unionpay.userid", "Get UserID for UnionPay JS Pay"),
    /**
     * UnionPay JS Pay Order.
     */
    UNION_JS_PAY("pay.unionpay.jspay", "UnionPay JS Pay Order"),
    /**
     * Alipay Service Window Pay.
     */
    ALI_PAY_JS_PAY("pay.alipay.jspay", "Alipay Service Window Pay"),
    /**
     * Download bill for a single merchant.
     */
    BILL_MERCHANT("pay.bill.merchant", "Download bill for a single merchant"),
    /**
     * Download bill for all stores of a chain merchant.
     */
    BILL_BIG_MERCHANT("pay.bill.bigMerchant", "Download bill for all stores of a chain merchant"),
    /**
     * Download bill for all merchants under an internal institution/outsourcing service provider.
     */
    BILL_AGENT("pay.bill.agent",
            "Download bill for all merchants under an internal institution/outsourcing service provider");

    /**
     * The API endpoint.
     */
    private final String method;
    /**
     * The description of the API.
     */
    private final String desc;

    /**
     * Constructs a new UnionPayApi.
     *
     * @param method The API endpoint.
     * @param desc   The description of the API.
     */
    UnionPayApi(String method, String desc) {
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
