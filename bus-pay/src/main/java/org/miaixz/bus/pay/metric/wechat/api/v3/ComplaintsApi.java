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
 * WeChat Pay V3 API interfaces for consumer complaints 2.0.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ComplaintsApi implements Matcher {

    /**
     * Query complaint list.
     */
    COMPLAINTS_V2("/v3/merchant-service/complaints-v2", "Query complaint list"),

    /**
     * Query complaint details.
     */
    COMPLAINTS_DETAIL("/v3/merchant-service/complaints-v2/%s", "Query complaint details"),

    /**
     * Query complaint negotiation history.
     */
    COMPLAINTS_NEGOTIATION_HISTORY("/v3/merchant-service/complaints-v2/%s/negotiation-historys",
            "Query complaint negotiation history"),

    /**
     * Create/query/update/delete complaint notification callback.
     */
    COMPLAINTS_NOTIFICATION("/v3/merchant-service/complaint-notifications",
            "Create/query/update/delete complaint notification callback"),

    /**
     * Submit reply.
     */
    COMPLAINTS_RESPONSE("/v3/merchant-service/complaints-v2/%s/response", "Submit reply"),

    /**
     * Merchant upload feedback image.
     */
    IMAGES_UPLOAD("/v3/merchant-service/images/upload", "Merchant upload feedback image"),

    /**
     * Feedback processing completed.
     */
    COMPLAINTS_COMPLETE("/v3/merchant-service/complaints-v2/%s/complete", "Feedback processing completed");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new ComplaintsApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    ComplaintsApi(String method, String desc) {
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
