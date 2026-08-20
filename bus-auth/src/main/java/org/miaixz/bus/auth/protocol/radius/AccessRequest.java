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
package org.miaixz.bus.auth.protocol.radius;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents an RFC 2865 Access-Request while retaining either historic or RFC 9765 header semantics.
 *
 * @param header     version-specific request header
 * @param attributes raw Attributes in wire order
 * @author Kimi Liu
 */
public record AccessRequest(RadiusPacket.Header header, List<RadiusAttribute> attributes) implements RadiusPacket {

    /**
     * Validates and freezes an Access-Request.
     *
     * @param header     non-null request header
     * @param attributes non-null, element-complete Attribute sequence
     * @throws IllegalArgumentException if the header or list is {@code null}
     */
    public AccessRequest {
        Assert.notNull(header, "RADIUS Access-Request header must not be null");
        Assert.notNull(attributes, "RADIUS Access-Request attributes must not be null");
        attributes = List.copyOf(attributes);
    }

    /**
     * Returns the registered Access-Request Code.
     *
     * @return Code 1
     */
    @Override
    public RadiusCode code() {
        return new RadiusCode(Radius.Codes.ACCESS_REQUEST);
    }

}
