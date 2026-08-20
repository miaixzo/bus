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
package org.miaixz.bus.auth.vendor;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Declares one officially documented platform wire deviation required at runtime.
 * <p>
 * The record is descriptive and immutable. It does not disable validation, contain arbitrary extension data, or carry
 * documentation and test evidence that belongs to the design and verification artifacts.
 * </p>
 *
 * @param operation    exact standard or platform-scoped operation affected by the deviation
 * @param location     wire location of the platform field or envelope
 * @param vendorName   exact platform field name
 * @param standardName corresponding standard field name when one exists
 * @param mediaType    exact media type when the deviation changes representation
 * @param method       exact HTTP method used by the platform operation
 * @param enveloped    whether the platform wraps the operation response in a non-standard envelope
 * @author Kimi Liu
 */
public record VendorDeviation(String operation, Location location, String vendorName, Optional<String> standardName,
        Optional<MediaType> mediaType, Http.Method method, boolean enveloped) {

    /**
     * Validates and freezes one exact platform deviation declaration.
     *
     * @throws IllegalArgumentException if required text is blank or a component container is null
     */
    public VendorDeviation {
        Assert.notBlank(operation, "Vendor deviation operation must not be blank");
        Assert.notNull(location, "Vendor deviation location must not be null");
        Assert.notBlank(vendorName, "Vendor deviation field name must not be blank");
        Assert.notNull(standardName, "Vendor deviation standard-name container must not be null");
        standardName = Optional.ofNullable(standardName.getOrNull());
        if (standardName.isPresent()) {
            Assert.notBlank(standardName.getOrNull(), "Vendor deviation standard field name must not be blank");
        }
        Assert.notNull(mediaType, "Vendor deviation media-type container must not be null");
        mediaType = Optional.ofNullable(mediaType.getOrNull());
        Assert.notNull(method, "Vendor deviation HTTP method must not be null");
    }

    /**
     * Enumerates the exact wire locations in which a platform deviation can occur.
     *
     * @author Kimi Liu
     */
    public enum Location {

        /**
         * HTTP URI query parameter.
         */
        QUERY,

        /**
         * Application/x-www-form-urlencoded body field.
         */
        FORM,

        /**
         * HTTP header field.
         */
        HEADER,

        /**
         * JSON request or response member.
         */
        JSON,

        /**
         * Whole response representation or envelope.
         */
        RESPONSE

    }

}
