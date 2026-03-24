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
 * WeChat Pay V3 API interfaces for smart business circles.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum BusinessCircleApi implements Matcher {

    /**
     * Smart business circle - synchronize business circle points.
     */
    BUSINESS_CIRCLE_POINTS_NOTIFY("/v3/businesscircle/points/notify",
            "Smart business circle - synchronize business circle points"),

    /**
     * Smart business circle - query business circle points authorization.
     */
    BUSINESS_CIRCLE_USER_AUTHORIZATIONS("/v3/businesscircle/user-authorizations/%s",
            "Smart business circle - query business circle points authorization"),

    /**
     * Smart business circle - query business circle member pending points status.
     */
    BUSINESS_CIRCLE_COMMIT_STATUS("/v3/businesscircle/users/%s/points/commit_status",
            "Smart business circle - query business circle member pending points status"),

    /**
     * Smart business circle - synchronize business circle member parking status.
     */
    BUSINESS_CIRCLE_PARKING("v3/businesscircle/parkings",
            "Smart business circle - synchronize business circle member parking status");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new BusinessCircleApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    BusinessCircleApi(String method, String desc) {
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
