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
 * WeChat Pay V3 API interfaces for brand profit sharing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BrandProfitSharingApi implements Matcher {

    /**
     * Profit sharing/query profit sharing.
     */
    ORDERS("/v3/brand/profitsharing/orders", "Profit sharing/query profit sharing"),

    /**
     * Profit sharing return - query profit sharing return.
     */
    RETURN_ORDERS("/v3/brand/profitsharing/returnorders", "Profit sharing return - query profit sharing return"),

    /**
     * Complete profit sharing.
     */
    FINISH_ORDER("/v3/brand/profitsharing/finish-order", "Complete profit sharing"),

    /**
     * Query remaining amount to be shared for the order.
     */
    QUERY("/v3/brand/profitsharing/orders/%s/amounts", "Query remaining amount to be shared for the order"),

    /**
     * Query maximum profit sharing ratio.
     */
    BRAND__CONFIGS("/v3/brand/profitsharing/brand-configs/%s", "Query maximum profit sharing ratio"),

    /**
     * Add profit sharing receiver.
     */
    RECEIVERS_ADD("/v3/brand/profitsharing/receivers/add", "Add profit sharing receiver"),

    /**
     * Delete profit sharing receiver.
     */
    RECEIVERS_DELETE("/v3/brand/profitsharing/receivers/delete", "Delete profit sharing receiver");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new BrandProfitSharingApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    BrandProfitSharingApi(String method, String desc) {
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
