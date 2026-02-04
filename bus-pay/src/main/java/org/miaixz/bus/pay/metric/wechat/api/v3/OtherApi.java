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
 * WeChat Pay V3 API interfaces for other capabilities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum OtherApi implements Matcher {

    /**
     * Image upload.
     */
    MERCHANT_UPLOAD_MEDIA("/v3/merchant/media/upload", "Image upload"),

    /**
     * Video upload.
     */
    MERCHANT_UPLOAD_VIDEO("/v3/merchant/media/video_upload", "Video upload"),

    /**
     * Image upload (for marketing only).
     */
    MARKETING_UPLOAD_MEDIA("/v3/marketing/favor/media/image-upload", "Image upload (for marketing only)"),

    /**
     * Get platform certificate list.
     */
    GET_CERTIFICATES("/v3/certificates", "Get platform certificate list"),

    /**
     * Get platform certificate list by algorithm type.
     */
    GET_CERTIFICATES_BY_ALGORITHM_TYPE("/v3/certificates?algorithm_type=%s",
            "Get platform certificate list by algorithm type");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new OtherApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    OtherApi(String method, String desc) {
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
