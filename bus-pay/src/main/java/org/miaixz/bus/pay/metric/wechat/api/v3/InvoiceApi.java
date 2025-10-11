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
 * WeChat Pay V3 API interfaces related to electronic invoices.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum InvoiceApi implements Matcher {

    /**
     * Create electronic invoice card coupon template.
     */
    CARD_TEMPLATE("/v3/new-tax-control-fapiao/card-template", "Create electronic invoice card coupon template"),

    /**
     * Issue electronic invoice.
     */
    INVOICING("/v3/new-tax-control-fapiao/fapiao-applications", "Issue electronic invoice"),

    /**
     * Upload electronic invoice file.
     */
    UPDATE_INVOICE_FILE("/v3/new-tax-control-fapiao/fapiao-applications/upload-fapiao-file",
            "Upload electronic invoice file"),

    /**
     * Query electronic invoice.
     */
    QUERY_INVOICE("/v3/new-tax-control-fapiao/fapiao-applications/%s", "Query electronic invoice"),

    /**
     * Get invoice download information.
     */
    QUERY_INVOICE_DOWNLOAD_INFO("/v3/new-tax-control-fapiao/fapiao-applications/%s/fapiao-files",
            "Get invoice download information"),

    /**
     * Insert electronic invoice into WeChat user card package.
     */
    INSERT_CARDS("/v3/new-tax-control-fapiao/fapiao-applications/%s/insert-cards",
            "Insert electronic invoice into WeChat user card package"),

    /**
     * Reverse electronic invoice.
     */
    REVERSE("/v3/new-tax-control-fapiao/fapiao-applications/%s/reverse", "Reverse electronic invoice"),

    /**
     * Get merchant invoicing basic information.
     */
    MERCHANT_BASE_INFO("/v3/new-tax-control-fapiao/merchant/base-information",
            "Get merchant invoicing basic information"),

    /**
     * Query/configure merchant development options.
     */
    MERCHANT_DEVELOPMENT_CONFIG("/v3/new-tax-control-fapiao/merchant/development-config",
            "Query/configure merchant development options"),

    /**
     * Get merchant's available goods and services tax classification code comparison table.
     */
    MERCHANT_TAX_CODES("/v3/new-tax-control-fapiao/merchant/tax-codes",
            "Get merchant's available goods and services tax classification code comparison table"),

    /**
     * Get user-filled invoice title.
     */
    USER_TITLE("/v3/new-tax-control-fapiao/user-title", "Get user-filled invoice title"),

    /**
     * Get invoice title filling link.
     */
    USER_TITLE_URL("/v3/new-tax-control-fapiao/user-title/title-url", "Get invoice title filling link");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new InvoiceApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    InvoiceApi(String method, String desc) {
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
