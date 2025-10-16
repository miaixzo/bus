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
 * WeChat Pay V3 API interfaces related to WeChat Pay Score.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum PayScoreApi implements Matcher {

    /**
     * Create/query Pay Score order.
     */
    PAY_SCORE_SERVICE_ORDER("/v3/payscore/serviceorder", "Create/query Pay Score order"),

    /**
     * Cancel Pay Score order.
     */
    PAY_SCORE_SERVICE_ORDER_CANCEL("/v3/payscore/serviceorder/%s/cancel", "Cancel Pay Score order"),

    /**
     * Modify order amount.
     */
    PAY_SCORE_SERVICE_ORDER_MODIFY("/v3/payscore/serviceorder/%s/modify", "Modify order amount"),

    /**
     * Complete Pay Score order.
     */
    PAY_SCORE_SERVICE_ORDER_COMPLETE("/v3/payscore/serviceorder/%s/complete", "Complete Pay Score order"),

    /**
     * Synchronize service order information.
     */
    PAY_SCORE_SERVICE_ORDER_SYNC("/v3/payscore/serviceorder/%s/sync", "Synchronize service order information"),

    /**
     * Merchant pre-authorization.
     */
    PAY_SCORE_PERMISSIONS("/v3/payscore/permissions", "Merchant pre-authorization"),

    /**
     * Query user authorization record (authorization agreement number).
     */
    PAY_SCORE_PERMISSIONS_AUTHORIZATION_CODE("/v3/payscore/permissions/authorization-code/%s",
            "Query user authorization record (authorization agreement number)"),

    /**
     * Terminate user authorization relationship (authorization agreement number).
     */
    PAY_SCORE_PERMISSIONS_AUTHORIZATION_CODE_TERMINATE("/v3/payscore/permissions/authorization-code/%s/terminate",
            "Terminate user authorization relationship (authorization agreement number)"),

    /**
     * Query user authorization record (openid).
     */
    PAY_SCORE_PERMISSIONS_OPENID("/v3/payscore/permissions/openid/%s", "Query user authorization record (openid)"),

    /**
     * Terminate user authorization relationship (openid).
     */
    PAY_SCORE_PERMISSIONS_OPENID_TERMINATE("/v3/payscore/permissions/openid/%s/terminate",
            "Terminate user authorization relationship (openid)");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new PayScoreApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    PayScoreApi(String method, String desc) {
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
