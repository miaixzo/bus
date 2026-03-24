/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V2 API interfaces related to micro merchants.
 *
 * @author Kimi Liu
 * @since Java 21+
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
