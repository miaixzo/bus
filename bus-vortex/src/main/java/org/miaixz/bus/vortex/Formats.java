/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
