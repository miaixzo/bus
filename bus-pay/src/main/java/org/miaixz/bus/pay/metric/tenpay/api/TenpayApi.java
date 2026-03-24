/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric.tenpay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * QQ Wallet Payment APIs.
 *
 * @author Kimi Liu
 * @since Java 21+
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
