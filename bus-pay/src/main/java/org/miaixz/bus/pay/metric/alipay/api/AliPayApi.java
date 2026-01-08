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
package org.miaixz.bus.pay.metric.alipay.api;

import org.miaixz.bus.pay.Matcher;

/**
 * Alipay Transaction APIs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AliPayApi implements Matcher {

    /**
     * Web Page Payment.
     */
    PAGE("alipay.trade.page.pay", "Web Page Payment"),
    /**
     * APP Payment.
     */
    APP("alipay.trade.app.pay", "APP Payment"),
    /**
     * Mobile Website Payment.
     */
    WAP("alipay.trade.wap.pay", "Mobile Website Payment"),
    /**
     * Scan Code Payment.
     */
    SWEEPPAY("alipay.trade.precreate", "Scan Code Payment"),
    /**
     * Barcode Payment.
     */
    BAR_CODE("alipay.trade.pay", "Barcode Payment"),
    /**
     * Wave Code Payment.
     */
    WAVE_CODE("alipay.trade.pay", "Wave Code Payment"),
    /**
     * Mini Program Payment.
     */
    MINAPP("alipay.trade.create", "Mini Program Payment"),
    /**
     * Face-scanning Payment.
     */
    SECURITY_CODE("alipay.trade.pay", "Face-scanning Payment"),
    /**
     * Unified Order Settlement Interface.
     */
    SETTLE("alipay.trade.order.settle", "Unified Order Settlement Interface"),
    /**
     * Transaction Order Query.
     */
    QUERY("alipay.trade.query", "Transaction Order Query"),
    /**
     * Transaction Order Close.
     */
    CLOSE("alipay.trade.close", "Transaction Order Close"),
    /**
     * Transaction Order Cancel.
     */
    CANCEL("alipay.trade.cancel", "Transaction Order Cancel"),
    /**
     * Refund.
     */
    REFUND("alipay.trade.refund", "Refund"),
    /**
     * Refund Query.
     */
    REFUNDQUERY("alipay.trade.fastpay.refund.query", "Refund Query"),
    /**
     * Refund Deposit Back Completed Notification. When a refund is made to a bank card, the acquirer sends a refund
     * completion message based on the bank's receipt.
     */
    REFUND_DEPOSITBACK_COMPLETED("alipay.trade.refund.depositback.completed",
            "Refund Deposit Back Completed Notification"),
    /**
     * Download Bill.
     */
    DOWNLOADBILL("alipay.data.dataservice.bill.downloadurl.query", "Download Bill");

    /**
     * The API method name.
     */
    private final String method;
    /**
     * The description of the API.
     */
    private final String desc;

    /**
     * Constructs a new AliPayApi.
     *
     * @param method The API method name.
     * @param desc   The description of the API.
     */
    AliPayApi(String method, String desc) {
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
     * Gets the API method name.
     *
     * @return The API method name.
     */
    @Override
    public String method() {
        return this.method;
    }

}
