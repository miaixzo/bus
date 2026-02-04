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
