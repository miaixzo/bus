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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V2 API interfaces related to payments.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayApi implements Matcher {

    /**
     * Sandbox environment.
     */
    SAND_BOX_NEW("/sandboxnew", "Sandbox environment"),

    /**
     * V2 version sandbox environment.
     */
    API_V2_SANDBOX("/xdc/apiv2sandbox", "V2 version sandbox environment"),

    /**
     * Get sandbox environment signature key.
     */
    GET_SIGN_KEY("/xdc/apiv2getsignkey/sign/getsignkey", "Get sandbox environment signature key"),

    /**
     * Unified order.
     */
    UNIFIED_ORDER("/pay/unifiedorder", "Unified order"),

    /**
     * Micropay.
     */
    MICRO_PAY("/pay/micropay", "Micropay"),

    /**
     * Order query.
     */
    ORDER_QUERY("/pay/orderquery", "Order query"),

    /**
     * Close order.
     */
    CLOSE_ORDER("/pay/closeorder", "Close order"),

    /**
     * Reverse order.
     */
    REVERSE("/secapi/pay/reverse", "Reverse order"),

    /**
     * Apply for refund.
     */
    REFUND("/secapi/pay/refund", "Apply for refund"),

    /**
     * Single item discount - apply for refund
     * <a href="https://pay.weixin.qq.com/wiki/doc/api/danpin.php?chapter=9_103&index=3">Official documentation</a>
     */
    REFUND_V2("/secapi/pay/refundv2", "Single item discount - apply for refund"),

    /**
     * Refund query.
     */
    REFUND_QUERY("/pay/refundquery", "Refund query"),

    /**
     * Single item discount - refund query.
     */
    REFUND_QUERY_V2("/pay/refundqueryv2", "Single item discount - refund query"),

    /**
     * Download bill.
     */
    DOWNLOAD_BILL("/pay/downloadbill", "Download bill"),

    /**
     * Download fund flow bill.
     */
    DOWNLOAD_FUND_FLOW("/pay/downloadfundflow", "Download fund flow bill"),

    /**
     * Transaction guarantee.
     */
    REPORT("/payitil/report", "Transaction guarantee"),

    /**
     * Query openid by authorization code.
     */
    AUTH_CODE_TO_OPENID("/tools/authcodetoopenid", "Query openid by authorization code"),

    /**
     * Convert to short URL.
     */
    SHORT_URL("/tools/shorturl", "Convert to short URL");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new PayApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    PayApi(String method, String desc) {
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
