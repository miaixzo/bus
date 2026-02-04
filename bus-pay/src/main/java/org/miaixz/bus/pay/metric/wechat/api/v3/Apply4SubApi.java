/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
