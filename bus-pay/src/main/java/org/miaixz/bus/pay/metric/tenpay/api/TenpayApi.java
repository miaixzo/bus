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
package org.miaixz.bus.pay.metric.tenpay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * QQ Wallet Payment APIs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TenpayApi implements Matcher {

    /**
     * Micropay URL.
     */
    MICRO_PAY_URL("/pay/qpay_micro_pay.cgi", "Submit Micropay"),
    /**
     * Unified Order URL.
     */
    UNIFIED_ORDER_URL("/pay/qpay_unified_order.cgi", "Unified Order"),
    /**
     * Order Query URL.
     */
    ORDER_QUERY_URL("/pay/qpay_order_query.cgi", "Order Query"),
    /**
     * Close Order URL.
     */
    CLOSE_ORDER_URL("/pay/qpay_close_order.cgi", "Close Order"),
    /**
     * Reverse Order URL.
     */
    ORDER_REVERSE_URL("/pay/qpay_reverse.cgi", "Reverse Order"),
    /**
     * Refund URL.
     */
    ORDER_REFUND_URL("/pay/qpay_refund.cgi", "Apply for Refund"),
    /**
     * Refund Query URL.
     */
    REFUND_QUERY_URL("/pay/qpay_refund_query.cgi", "Refund Query"),
    /**
     * Download Bill URL.
     */
    DOWNLOAD_BILL_URL("/sp_download/qpay_mch_statement_down.cgi", "Download Bill"),
    /**
     * Create Red Packet URL.
     */
    CREATE_READ_PACK_URL("/hongbao/qpay_hb_mch_send.cgi", "Create Red Packet"),
    /**
     * Get Red Packet Info URL.
     */
    GET_HB_INFO_URL("/mch_query/qpay_hb_mch_list_query.cgi", "Query Red Packet Info"),
    /**
     * Download Red Packet Bill URL.
     */
    DOWNLOAD_HB_BILL_URL("/hongbao/qpay_hb_mch_down_list_file.cgi", "Download Red Packet Bill"),
    /**
     * Transfer to Balance URL.
     */
    TRANSFER_URL("/epay/qpay_epay_b2c.cgi", "Transfer to Balance"),
    /**
     * Get Transfer Info URL.
     */
    GET_TRANSFER_INFO_URL("/pay/qpay_epay_query.cgi", "Query Transfer Info"),
    /**
     * Download Transfer Bill URL.
     */
    DOWNLOAD_TRANSFER_BILL_URL("/pay/qpay_epay_statement_down.cgi", "Download Transfer Bill");

    /**
     * The API endpoint.
     */
    private final String method;
    /**
     * The description of the API.
     */
    private final String desc;

    /**
     * Constructs a new TenpayApi.
     *
     * @param method The API endpoint.
     * @param desc   The description of the API.
     */
    TenpayApi(String method, String desc) {
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
