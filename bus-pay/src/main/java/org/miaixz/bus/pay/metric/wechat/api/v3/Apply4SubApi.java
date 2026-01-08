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
 * WeChat Pay V3 API interfaces for sub-merchant applications.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Apply4SubApi implements Matcher {

    /**
     * Submit application form.
     */
    APPLY_4_SUB("/v3/applyment4sub/applyment/", "Submit application form"),

    /**
     * Query application status.
     */
    GET_APPLY_STATE("/v3/applyment4sub/applyment/business_code/%s", "Query application status"),

    /**
     * Query application status by application ID.
     */
    GET_APPLY_STATE_BY_ID("/v3/applyment4sub/applyment/applyment_id/%s", "Query application status by application ID"),

    /**
     * Modify settlement account.
     */
    MODIFY_SETTLEMENT("/v3/apply4sub/sub_merchants/%s/modify-settlement", "Modify settlement account"),

    /**
     * Query settlement account.
     */
    GET_SETTLEMENT("/v3/apply4sub/sub_merchants/%s/settlement", "Query settlement account"),

    /**
     * Merchant account opening intention confirmation - submit application form OR query application review result.
     */
    MER_OPEN_APPLY_SUBMIT_OR_RESULT("/v3/apply4subject/applyment",
            "Submit application form/query application review result"),

    /**
     * Merchant account opening intention confirmation - cancel application form.
     */
    MER_OPEN_APPLY_CANCEL("/v3/apply4subject/applyment/%s/cancel", "Cancel application form"),

    /**
     * Merchant account opening intention confirmation - get merchant account opening intention confirmation status.
     */
    GET_MER_OPEN_APPLY_STATE("/v3/apply4subject/applyment/merchants/%s/state",
            "Get merchant account opening intention confirmation status");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new Apply4SubApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    Apply4SubApi(String method, String desc) {
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
