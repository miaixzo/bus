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
 * WeChat Pay V3 API interfaces for merchant transfers to balance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TransferApi implements Matcher {

    /**
     * Initiate merchant transfer.
     */
    TRANSFER_BATCHES("/v3/transfer/batches", "Initiate merchant transfer"),

    /**
     * Query batch order by WeChat Pay batch order number.
     */
    TRANSFER_QUERY_BY_BATCH_ID("/v3/transfer/batches/batch-id/%s",
            "Query batch order by WeChat Pay batch order number"),

    /**
     * Service provider mode - query batch order by WeChat Pay batch order number.
     */
    PARTNER_TRANSFER_QUERY_BY_BATCH_ID("/v3/partner-transfer/batches/batch-id/%s",
            "Service provider mode - query batch order by WeChat Pay batch order number"),

    /**
     * Query detail order by WeChat Pay detail order number.
     */
    TRANSFER_QUERY_BY_DETAIL_ID("/v3/transfer/batches/batch-id/%s/details/detail-id/%s",
            "Query detail order by WeChat Pay detail order number"),

    /**
     * Service provider mode - query detail order by WeChat Pay detail order number.
     */
    PARTNER_TRANSFER_QUERY_BY_DETAIL_ID("/v3/partner-transfer/batches/batch-id/%s/details/detail-id/%s",
            "Service provider mode - query detail order by WeChat Pay detail order number"),

    /**
     * Query batch order by merchant batch order number.
     */
    TRANSFER_QUERY_BY_OUT_BATCH_NO("/v3/transfer/batches/out-batch-no/%s",
            "Query batch order by merchant batch order number"),

    /**
     * Service provider mode - query batch order by merchant batch order number.
     */
    PARTNER_TRANSFER_QUERY_BY_OUT_BATCH_NO("/v3/partner-transfer/batches/out-batch-no/%s",
            "Service provider mode - query batch order by merchant batch order number"),

    /**
     * Query detail order by merchant detail order number.
     */
    TRANSFER_QUERY_DETAIL_BY_OUT_BATCH_NO("/v3/transfer/batches/out-batch-no/%s/details/out-detail-no/%s",
            "Query detail order by merchant detail order number"),

    /**
     * Service provider mode - query detail order by merchant detail order number.
     */
    PARTNER_TRANSFER_QUERY_DETAIL_BY_OUT_BATCH_NO(
            "/v3/partner-transfer/batches/out-batch-no/%s/details/out-detail-no/%s",
            "Service provider mode - query detail order by merchant detail order number"),

    /**
     * Transfer electronic receipt application acceptance.
     */
    TRANSFER_BILL_RECEIPT("/v3/transfer/bill-receipt", "Transfer electronic receipt application acceptance"),

    /**
     * Query transfer electronic receipt.
     */
    TRANSFER_BILL_RECEIPT_QUERY("/v3/transfer/bill-receipt/%s", "Query transfer electronic receipt"),

    /**
     * Transfer detail electronic receipt acceptance/query transfer detail electronic receipt acceptance result.
     */
    TRANSFER_ELECTRONIC_RECEIPTS("/v3/transfer-detail/electronic-receipts",
            "Transfer detail electronic receipt acceptance/query transfer detail electronic receipt acceptance result"),

    /**
     * Query special merchant bank incoming funds.
     */
    PARTNER_INCOME_RECORDS("/v3/merchantfund/partner/income-records", "Query special merchant bank incoming funds"),

    /**
     * Query service provider bank incoming funds.
     */
    MERCHANT_INCOME_RECORDS("/v3/merchantfund/merchant/income-records", "Query service provider bank incoming funds");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new TransferApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    TransferApi(String method, String desc) {
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
