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
 * WeChat Pay V2 API interfaces related to micro merchants.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum MicroMchApi implements Matcher {

    /**
     * Apply for entry.
     */
    SUBMIT("/applyment/micro/submit", "Apply for entry"),

    /**
     * Query application status.
     */
    GET_SUBMIT_STATE("/applyment/micro/getstate", "Query application status"),

    /**
     * Query withdrawal status.
     */
    QUERY_AUTO_WITH_DRAW_BY_DATE("/fund/queryautowithdrawbydate", "Query withdrawal status"),

    /**
     * Modify settlement bank card.
     */
    MODIFY_ARCHIVES("/applyment/micro/modifyarchives", "Modify settlement bank card"),

    /**
     * Modify contact information.
     */
    MODIFY_CONTACT_INFO("/applyment/micro/modifycontactinfo", "Modify contact information"),

    /**
     * Follow configuration.
     */
    ADD_RECOMMEND_CONF("/secapi/mkt/addrecommendconf", "Follow configuration"),

    /**
     * Add development configuration.
     */
    ADD_SUB_DEV_CONFIG("/secapi/mch/addsubdevconfig", "Add development configuration"),

    /**
     * Query development configuration.
     */
    QUERY_SUB_DEV_CONFIG("/secapi/mch/querysubdevconfig", "Query development configuration"),

    /**
     * Submit upgrade application.
     */
    SUBMIT_UPGRADE("/applyment/micro/submitupgrade", "Submit upgrade application"),

    /**
     * Query upgrade application status.
     */
    GET_UPGRADE_STATE("/applyment/micro/getupgradestate", "Query upgrade application status"),

    /**
     * Service provider helps micro merchants re-initiate automatic withdrawal.
     */
    RE_AUTO_WITH_DRAW_BY_DATE("/fund/reautowithdrawbydate",
            "Service provider helps micro merchants re-initiate automatic withdrawal");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new MicroMchApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    MicroMchApi(String method, String desc) {
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
