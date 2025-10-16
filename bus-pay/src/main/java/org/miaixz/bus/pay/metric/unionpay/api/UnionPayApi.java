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
