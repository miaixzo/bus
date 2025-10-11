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
 * WeChat Pay V3 API interfaces related to basic payments.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BasePayApi implements Matcher {

    /**
     * JSAPI order.
     */
    JS_API_PAY("/v3/pay/transactions/jsapi", "JSAPI order"),

    /**
     * Service provider mode - JSAPI order.
     */
    PARTNER_JS_API_PAY("/v3/pay/partner/transactions/jsapi", "Service provider mode - JSAPI order"),

    /**
     * APP order.
     */
    APP_PAY("/v3/pay/transactions/app", "APP order"),

    /**
     * Service provider mode - APP order.
     */
    PARTNER_APP_PAY("/v3/pay/partner/transactions/app", "Service provider mode - APP order"),

    /**
     * H5 order.
     */
    H5_PAY("/v3/pay/transactions/h5", "H5 order"),

    /**
     * Service provider mode - H5 order.
     */
    PARTNER_H5_PAY("/v3/pay/partner/transactions/h5", "Service provider mode - H5 order"),

    /**
     * Native order.
     */
    NATIVE_PAY("/v3/pay/transactions/native", "Native order"),

    /**
     * Service provider mode - Native order.
     */
    PARTNER_NATIVE_PAY("/v3/pay/partner/transactions/native", "Service provider mode - Native order"),

    /**
     * Payment code payment.
     */
    CODE_PAY("/v3/pay/transactions/codepay", "Payment code payment"),

    /**
     * Combined APP order.
     */
    COMBINE_TRANSACTIONS_APP("/v3/combine-transactions/app", "Combined APP order"),

    /**
     * Combined JSAPI order.
     */
    COMBINE_TRANSACTIONS_JS("/v3/combine-transactions/jsapi", "Combined JSAPI order"),

    /**
     * Combined H5 order.
     */
    COMBINE_TRANSACTIONS_H5("/v3/combine-transactions/h5", "Combined H5 order"),

    /**
     * Combined Native order.
     */
    COMBINE_TRANSACTIONS_NATIVE("/v3/combine-transactions/native", "Combined Native order"),

    /**
     * Combined order query.
     */
    COMBINE_QUERY_BY_OUT_TRADE_NO("/v3/combine-transactions/out-trade-no/%s", "Combined order query"),

    /**
     * Combined order close.
     */
    COMBINE_CLOSE_BY_OUT_TRADE_NO("/v3/combine-transactions/out-trade-no/%s/close", "Combined order close"),

    /**
     * Combined payment - apply for refund.
     */
    DOMESTIC_REFUND("/v3/refund/domestic/refunds", "Combined payment - apply for refund"),

    /**
     * Combined payment - query single refund.
     */
    DOMESTIC_REFUND_QUERY("/v3/refund/domestic/refunds/%s", "Combined payment - query single refund"),

    /**
     * WeChat Pay order number query.
     */
    ORDER_QUERY_BY_TRANSACTION_ID("/v3/pay/transactions/id/%s", "WeChat Pay order number query"),

    /**
     * Service provider mode - WeChat Pay order number query.
     */
    PARTNER_ORDER_QUERY_BY_TRANSACTION_ID("/v3/pay/partner/transactions/id/%s",
            "Service provider mode - WeChat Pay order number query"),

    /**
     * Merchant order number query.
     */
    ORDER_QUERY_BY_OUT_TRADE_NO("/v3/pay/transactions/out-trade-no/%s", "Merchant order number query"),

    /**
     * Service provider mode - merchant order number query.
     */
    PARTNER_ORDER_QUERY_BY_OUT_TRADE_NO("/v3/pay/partner/transactions/out-trade-no/%s",
            "Service provider mode - merchant order number query"),

    /**
     * Close order.
     */
    CLOSE_ORDER_BY_OUT_TRADE_NO("/v3/pay/transactions/out-trade-no/%s/close", "Close order"),

    /**
     * Reverse order.
     */
    REVERSE_ORDER_BY_OUT_TRADE_NO("/v3/pay/transactions/out-trade-no/%s/reverse", "Reverse order"),

    /**
     * Service provider mode - close order.
     */
    PARTNER_CLOSE_ORDER_BY_OUT_TRADE_NO("/v3/pay/partner/transactions/out-trade-no/%s/close",
            "Service provider mode - close order"),

    /**
     * Apply for refund.
     */
    REFUND("/v3/refund/domestic/refunds", "Apply for refund"),

    /**
     * Query single refund.
     */
    REFUND_QUERY_BY_OUT_REFUND_NO("/v3/refund/domestic/refunds/%s", "Query single refund"),

    /**
     * Apply for trade bill.
     */
    TRADE_BILL("/v3/bill/tradebill", "Apply for trade bill"),

    /**
     * Apply for fund flow bill.
     */
    FUND_FLOW_BILL("/v3/bill/fundflowbill", "Apply for fund flow bill"),

    /**
     * Apply for single sub-merchant fund flow bill.
     */
    SUB_MERCHANT_FUND_FLOW_BILL("/v3/bill/sub-merchant-fundflowbill", "Apply for single sub-merchant fund flow bill"),

    /**
     * Download bill.
     */
    BILL_DOWNLOAD("/v3/billdownload/file", "Download bill");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new BasePayApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    BasePayApi(String method, String desc) {
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
