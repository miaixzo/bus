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
package org.miaixz.bus.vortex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.provider.BinaryProvider;
import org.miaixz.bus.vortex.provider.JsonProvider;
import org.miaixz.bus.vortex.provider.XmlProvider;
import org.miaixz.bus.vortex.strategy.ResponseStrategy;
import org.springframework.http.MediaType;

/**
 * Enumerates the supported data formats for API responses, associating each format with a specific serialization
 * provider and media type.
 * <p>
 * This enum plays a key role in content negotiation and response formatting. It allows strategies like
 * {@link ResponseStrategy} to dynamically handle different data formats.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Formats {

    /**
     * Represents the XML data format. Uses {@link XmlProvider} for serialization.
     */
    XML(new XmlProvider(), MediaType.APPLICATION_XML),

    /**
     * Represents the JSON data format. This is the default format for the gateway. Uses {@link JsonProvider} for
     * serialization.
     */
    JSON(new JsonProvider(), MediaType.APPLICATION_JSON),

    /**
     * Represents a generic binary file stream. This format is handled by the BinaryProvider for proper binary data
     * handling without string conversion corruption.
     */
    BINARY(new BinaryProvider(), MediaType.APPLICATION_OCTET_STREAM);

    /**
     * The data format provider, responsible for serializing response objects into the specific format.
     */
    private Provider provider;

    /**
     * The corresponding HTTP {@link MediaType}, used to set the {@code Content-Type} header in the response.
     */
    private MediaType mediaType;

    /**
     * Safely retrieves a {@code Formats} enum instance from a string name, ignoring case.
     *
     * @param name The name of the format (e.g., "JSON", "xml").
     * @return The corresponding {@link Formats} instance, or {@link #JSON} if the name is invalid or null.
     */
    public static Formats get(String name) {
        if (StringKit.isBlank(name)) {
            return JSON;
        }
        try {
            return Formats.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JSON; // Default to JSON if the format is unknown
        }
    }

}
