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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V3 API interfaces for consumer complaints 2.0.
 *
 * @author Kimi Liu
 * @since Java 21+
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
