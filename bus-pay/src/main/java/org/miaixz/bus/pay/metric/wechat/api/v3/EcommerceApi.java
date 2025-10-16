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
 * WeChat Pay v3 API - E-commerce Acquiring related APIs
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum EcommerceApi implements Matcher {

    /**
     * Sub-merchant Onboarding
     */
    APPLY("/v3/ecommerce/applyments/", "Sub-merchant Onboarding"),

    /**
     * Query Onboarding Application Status
     */
    APPLY_STATE("/v3/ecommerce/applyments/%s", "Query Onboarding Application Status"),

    /**
     * Query Application Status by Business Application Number
     */
    APPLY_STATE_BY_NO("/v3/ecommerce/applyments/out-request-no/%s",
            "Query Application Status by Business Application Number"),

    /**
     * Profit Sharing API - Request Profit Sharing/Query Profit Sharing Result
     */
    PROFIT_SHARING_ORDERS("/v3/ecommerce/profitsharing/orders",
            "Profit Sharing API - Request Profit Sharing/Query Profit Sharing Result"),

    /**
     * Profit Sharing API - Query Profit Sharing Return Result
     */
    PROFIT_SHARING_RETURN_ORDERS("/v3/ecommerce/profitsharing/returnorders",
            "Profit Sharing API - Query Profit Sharing Return Result"),

    /**
     * Profit Sharing API - Finish Profit Sharing
     */
    PROFIT_SHARING_FINISH_ORDER("/v3/ecommerce/profitsharing/finish-order",
            "Profit Sharing API - Finish Profit Sharing"),

    /**
     * Query Remaining Amount to be Shared for an Order
     */
    PROFIT_SHARING_QUERY("/v3/ecommerce/profitsharing/orders/%s/amounts",
            "Query Remaining Amount to be Shared for an Order"),

    /**
     * Add Profit Sharing Receiver
     */
    PROFIT_SHARING_RECEIVERS_ADD("/v3/ecommerce/profitsharing/receivers/add", "Add Profit Sharing Receiver"),

    /**
     * Delete Profit Sharing Receiver
     */
    PROFIT_SHARING_RECEIVERS_DELETE("/v3/ecommerce/profitsharing/receivers/delete", "Delete Profit Sharing Receiver"),

    /**
     * Subsidy API - Request Subsidy
     */
    CREATE_SUBSIDIES("/v3/ecommerce/subsidies/create", "Subsidy API - Request Subsidy"),

    /**
     * Subsidy API - Return Subsidy
     */
    RETURN_SUBSIDIES("/v3/ecommerce/subsidies/return", "Subsidy API - Return Subsidy"),

    /**
     * Subsidy API - Cancel Subsidy
     */
    CANCEL_SUBSIDIES("/v3/ecommerce/subsidies/cancel", "Subsidy API - Cancel Subsidy"),

    /**
     * Refund API - Apply for Refund
     */
    REFUNDS("/v3/ecommerce/refunds/apply", "Refund API - Apply for Refund"),

    /**
     * Refund API - Query Refund by WeChat Pay Refund ID
     */
    QUERY_REFUND("/v3/ecommerce/refunds/id/%s", "Refund API - Query Refund by WeChat Pay Refund ID"),

    /**
     * Refund API - Query Refund by Merchant Refund ID
     */
    QUERY_REFUNDS_BY_REFUND_NO("/v3/ecommerce/refunds/out-refund-no/%s",
            "Refund API - Query Refund by Merchant Refund ID"),

    /**
     * Refund API - Query/Advance Refund Replenishment
     */
    RETURN_ADVANCE_OR_QUERY("/v3/ecommerce/refunds/%s/return-advance",
            "Refund API - Query/Advance Refund Replenishment"),

    /**
     * Query Sub-merchant Real-time Account Balance
     */
    QUERY_BALANCE("/v3/ecommerce/fund/balance/%s", "Query Sub-merchant Real-time Account Balance"),

    /**
     * Query Sub-merchant End-of-day Account Balance
     */
    QUERY_END_DAY_BALANCE("/v3/ecommerce/fund/enddaybalance/%s", "Query Sub-merchant End-of-day Account Balance"),

    /**
     * Query E-commerce Platform Real-time Account Balance
     */
    QUERY_MERCHANT_BALANCE("/v3/merchant/fund/balance/%s", "Query E-commerce Platform Real-time Account Balance"),

    /**
     * Query E-commerce Platform End-of-day Account Balance
     */
    QUERY_MERCHANT_END_DAY_BALANCE("/v3/merchant/fund/dayendbalance/%s",
            "Query E-commerce Platform End-of-day Account Balance"),

    /**
     * Withdrawal API - Sub-merchant Scheduled Withdrawal
     */
    WITHDRAW("/v3/ecommerce/fund/withdraw", "Withdrawal API - Sub-merchant Scheduled Withdrawal"),

    /**
     * Withdrawal API - Query Sub-merchant Scheduled Withdrawal Status
     */
    WITHDRAW_QUERY("/v3/ecommerce/fund/withdraw/%s", "Withdrawal API - Query Sub-merchant Scheduled Withdrawal Status"),

    /**
     * Withdrawal API - E-commerce Platform Scheduled Withdrawal
     */
    MERCHANT_WITHDRAW("/v3/merchant/fund/withdraw", "Withdrawal API - E-commerce Platform Scheduled Withdrawal"),

    /**
     * Withdrawal API - Query E-commerce Platform Scheduled Withdrawal Status
     */
    MERCHANT_WITHDRAW_QUERY("/v3/merchant/fund/withdraw/withdraw-id/%s",
            "Withdrawal API - Query E-commerce Platform Scheduled Withdrawal Status"),

    /**
     * Withdrawal API - Query by Merchant Scheduled Withdrawal ID
     */
    MERCHANT_WITHDRAW_QUERY_BY_OUT_REQUEST_NO("/v3/merchant/fund/withdraw/out-request-no/%s",
            "Withdrawal API - Query by Merchant Scheduled Withdrawal ID"),

    /**
     * Withdrawal API - Download Daily Withdrawal Exception File
     */
    WITHDRAW_BILL("/v3/merchant/fund/withdraw/bill-type/%s",
            "Withdrawal API - Download Daily Withdrawal Exception File"),

    /**
     * Query Remaining Cross-border Balance for an Order
     */
    AVAILABLE_ABROAD_AMOUNTS("/v3/funds-to-oversea/transactions/%s/available_abroad_amounts",
            "Query Remaining Cross-border Balance for an Order"),

    /**
     * Apply for Cross-border Funds Transfer
     */
    FUNDS_TO_OVERSEA("/v3/funds-to-oversea/orders", "Apply for Cross-border Funds Transfer"),

    /**
     * Query Cross-border Transfer Result
     */
    FUNDS_TO_OVERSEA_QUERY("/v3/funds-to-oversea/orders/%s", "Query Cross-border Transfer Result"),

    /**
     * Get Download Link for Foreign Exchange Purchase and Payment Bill
     */
    FUNDS_TO_OVERSEA_BILL("/v3/funds-to-oversea/bill-download-url",
            "Get Download Link for Foreign Exchange Purchase and Payment Bill"),

    /**
     * Apply for Sub-merchant Fund Bill
     */
    FUND_FLOW_BILL("/v3/ecommerce/bill/fundflowbill", "Apply for Sub-merchant Fund Bill");

    /**
     * API method
     */
    private final String method;

    /**
     * API description
     */
    private final String desc;

    EcommerceApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * Transaction type
     *
     * @return the string
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * Type description
     *
     * @return the string
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * API method
     *
     * @return the string
     */
    @Override
    public String method() {
        return this.method;
    }

}
