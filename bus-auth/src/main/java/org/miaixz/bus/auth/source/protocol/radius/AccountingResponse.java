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
package org.miaixz.bus.auth.source.protocol.radius;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents an RFC 2866 Accounting-Response.
 *
 * @param header     version-specific response header
 * @param attributes raw Attributes in wire order
 * @author Kimi Liu
 */
public record AccountingResponse(RadiusPacket.Header header, List<RadiusAttribute> attributes) implements RadiusPacket {

    /**
     * Validates and freezes an Accounting-Response.
     *
     * @param header     non-null response header
     * @param attributes non-null, element-complete Attribute sequence
     * @throws IllegalArgumentException if the header or list is {@code null}
     */
    public AccountingResponse {
        Assert.notNull(header, "RADIUS Accounting-Response header must not be null");
        Assert.notNull(attributes, "RADIUS Accounting-Response attributes must not be null");
        attributes = List.copyOf(attributes);
    }

    /**
     * Returns the registered Accounting-Response Code.
     *
     * @return Code 5
     */
    @Override
    public RadiusCode code() {
        return new RadiusCode(Radius.Codes.ACCOUNTING_RESPONSE);
    }

}
