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
 * WeChat Pay v3 API - Coupon APIs
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum CouponApi implements Matcher {

    /**
     * Create a cash coupon batch
     */
    CREATE_COUPON_STOCKS("/v3/marketing/favor/coupon-stocks", "Create a cash coupon batch"),

    /**
     * Activate a cash coupon batch
     */
    START_COUPON_STOCKS("/v3/marketing/favor/stocks/%s/start", "Activate a cash coupon batch"),

    /**
     * Send a cash coupon
     */
    SEND_COUPON("/v3/marketing/favor/users/%s/coupons", "Send a cash coupon"),

    /**
     * Pause a cash coupon batch
     */
    PAUSE_COUPON_STOCKS("/v3/marketing/favor/stocks/%s/pause", "Pause a cash coupon batch"),

    /**
     * Restart a cash coupon batch
     */
    RESTART_COUPON_STOCKS("/v3/marketing/favor/stocks/%s/restart", "Restart a cash coupon batch"),
    /**
     * Query batch list by conditions
     */
    QUERY_COUPON_STOCKS("/v3/marketing/favor/stocks", "Query batch list by conditions"),
    /**
     * Query batch details
     */
    QUERY_COUPON_STOCKS_INFO("/v3/marketing/favor/stocks/%s", "Query batch details"),
    /**
     * Query cash coupon details
     */
    QUERY_COUPON_INFO("/v3/marketing/favor/users/%s/coupons/%s", "Query cash coupon details"),

    /**
     * Query available merchants for a cash coupon
     */
    QUERY_COUPON_MERCHANTS("/v3/marketing/favor/stocks/%s/merchants", "Query available merchants for a cash coupon"),

    /**
     * Query available items for a cash coupon
     */
    QUERY_COUPON_ITEMS("/v3/marketing/favor/stocks/%s/items", "Query available items for a cash coupon"),

    /**
     * Query user's coupons by merchant ID
     */
    QUERY_USER_COUPON("/v3/marketing/favor/users/%s/coupons", "Query user's coupons by merchant ID"),

    /**
     * Download batch use flow
     */
    DOWNLOAD_COUPON_STOCKS_USER_FLOW("/v3/marketing/favor/stocks/%s/use-flow", "Download batch use flow"),

    /**
     * Download batch refund flow
     */
    DOWNLOAD_COUPON_STOCKS_REFUND_FLOW("/v3/marketing/favor/stocks/%s/refund-flow", "Download batch refund flow"),

    /**
     * Set callback URL for notifications
     */
    SETTING_COUPON_CALLBACKS("/v3/marketing/favor/callbacks", "Set callback URL for notifications"),

    /**
     * Create a business coupon
     */
    CREATE_BUSINESS_COUPON("/v3/marketing/busifavor/stocks", "Create a business coupon"),

    /**
     * Query business coupon batch details
     */
    QUERY_BUSINESS_COUPON_STOCKS_INFO("/v3/marketing/busifavor/stocks/%s", "Query business coupon batch details"),

    /**
     * Use a user's coupon
     */
    USE_BUSINESS_COUPON("/v3/marketing/busifavor/coupons/use", "Use a user's coupon"),

    /**
     * Query user's coupons by filter conditions
     */
    QUERY_BUSINESS_USER_COUPON("/v3/marketing/busifavor/users/%s/coupons", "Query user's coupons by filter conditions"),

    /**
     * Query details of a single user coupon
     */
    QUERY_BUSINESS_USER_COUPON_INFO("/v3/marketing/busifavor/users/%s/coupons/%s/appids/%s",
            "Query details of a single user coupon"),

    /**
     * Upload pre-stocked codes
     */
    BUSINESS_COUPON_UPLOAD_CODE("/v3/marketing/busifavor/stocks/%s/couponcodes", "Upload pre-stocked codes"),

    /**
     * Set/Query business coupon event notification URL
     */
    BUSINESS_COUPON_CALLBACKS("/v3/marketing/busifavor/callbacks", "Set/Query business coupon event notification URL"),

    /**
     * Associate order information
     */
    BUSINESS_COUPON_ASSOCIATE("/v3/marketing/busifavor/coupons/associate", "Associate order information"),

    /**
     * Disassociate order information
     */
    BUSINESS_COUPON_DISASSOCIATE("/v3/marketing/busifavor/coupons/disassociate", "Disassociate order information"),

    /**
     * Modify batch budget
     */
    MODIFY_BUSINESS_COUPON_STOCKS_BUDGET("/v3/marketing/busifavor/stocks/%s/budget", "Modify batch budget"),

    /**
     * Modify business coupon basic information
     */
    MODIFY_BUSINESS_COUPON_INFO("/v3/marketing/busifavor/stocks/%s", "Modify business coupon basic information"),

    /**
     * Apply for a coupon return
     */
    APPLY_REFUND_COUPONS("/v3/marketing/busifavor/coupons/return", "Apply for a coupon return"),

    /**
     * Deactivate a coupon
     */
    COUPON_DEACTIVATE("/v3/marketing/busifavor/coupons/deactivate", "Deactivate a coupon"),

    /**
     * Marketing subsidy payment
     */
    COUPON_SUBSIDY_PAY("/v3/marketing/busifavor/subsidy/pay-receipts", "Marketing subsidy payment"),

    /**
     * Query marketing subsidy payment details
     */
    COUPON_SUBSIDY_PAY_INFO("/v3/marketing/busifavor/subsidy/pay-receipts/%s",
            "Query marketing subsidy payment details"),

    /**
     * Entrusted Marketing - Build partnership
     */
    PARTNERSHIPS_BUILD("/v3/marketing/partnerships/build", "Entrusted Marketing - Build partnership"),

    /**
     * Entrusted Marketing - Terminate partnership
     */
    PARTNERSHIPS_TERMINATE("/v3/marketing/partnerships/terminate", "Entrusted Marketing - Terminate partnership"),

    /**
     * Send a consumption card
     */
    SEND_BUSINESS_COUPON("/v3/marketing/busifavor/coupons/%s/send", "Send a consumption card");

    /**
     * API method
     */
    private final String method;

    /**
     * API description
     */
    private final String desc;

    CouponApi(String method, String desc) {
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
