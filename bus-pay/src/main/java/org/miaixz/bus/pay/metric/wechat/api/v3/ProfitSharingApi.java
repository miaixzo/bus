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
 * WeChat Pay v3 API - Profit Sharing APIs
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ProfitSharingApi implements Matcher {

    /**
     * Request Profit Sharing
     */
    PROFIT_SHARING_ORDERS("/v3/profitsharing/orders", "Request Profit Sharing"),

    /**
     * Query Profit Sharing Result
     */
    PROFIT_SHARING_ORDERS_QUERY("/v3/profitsharing/orders/%s", "Query Profit Sharing Result"),

    /**
     * Request Profit Sharing Return
     */
    PROFIT_SHARING_RETURN_ORDERS("/v3/profitsharing/return-orders", "Request Profit Sharing Return"),

    /**
     * Query Profit Sharing Return Result
     */
    PROFIT_SHARING_RETURN_ORDERS_QUERY("/v3/profitsharing/return-orders/%s", "Query Profit Sharing Return Result"),

    /**
     * Unfreeze Remaining Funds
     */
    PROFIT_SHARING_UNFREEZE("/v3/profitsharing/orders/unfreeze", "Unfreeze Remaining Funds"),

    /**
     * Query Remaining Amount to be Shared
     */
    PROFIT_SHARING_UNFREEZE_QUERY("/v3/profitsharing/transactions/%s/amounts", "Query Remaining Amount to be Shared"),

    /**
     * Query Maximum Profit Sharing Ratio
     */
    PROFIT_SHARING_MERCHANT_CONFIGS("/v3/profitsharing/merchant-configs/%s", "Query Maximum Profit Sharing Ratio"),

    /**
     * Add Profit Sharing Receiver
     */
    PROFIT_SHARING_RECEIVERS_ADD("/v3/profitsharing/receivers/add", "Add Profit Sharing Receiver"),

    /**
     * Delete Profit Sharing Receiver
     */
    PROFIT_SHARING_RECEIVERS_DELETE("/v3/profitsharing/receivers/delete", "Delete Profit Sharing Receiver"),

    /**
     * Apply for Profit Sharing Bill
     */
    PROFIT_SHARING_BILLS("/v3/profitsharing/bills", "Apply for Profit Sharing Bill");

    /**
     * API method
     */
    private final String method;

    /**
     * API description
     */
    private final String desc;

    ProfitSharingApi(String method, String desc) {
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
