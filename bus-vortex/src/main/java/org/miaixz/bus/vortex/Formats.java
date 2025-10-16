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
import org.miaixz.bus.vortex.provider.JsonProvider;
import org.miaixz.bus.vortex.provider.XmlProvider;
import org.springframework.http.MediaType;

/**
 * Enumeration of data formats, defining supported response data formats and their associated properties, along with
 * logging functionalities.
 * <p>
 * This enum class identifies the format of response data (e.g., XML, JSON, PDF, binary stream). Each format is
 * associated with a specific data provider and media type. It also provides static methods for logging at different
 * levels (error, warn, debug, info, trace) and for logging the start and end of requests.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Formats {

    /**
     * XML format, indicating that response data is output in XML format.
     */
    XML(new XmlProvider(), MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")),

    /**
     * JSON format, indicating that response data is output in JSON format.
     */
    JSON(new JsonProvider(), MediaType.APPLICATION_JSON),

    /**
     * PDF format, indicating that response data is output in PDF format.
     */
    PDF,

    /**
     * Binary file stream, indicating that response data is output as a file stream.
     */
    BINARY;

    /**
     * The data format provider, used for serialization or deserialization of specific formats.
     * <p>
     * For example, XML format uses {@link XmlProvider}, and JSON format uses {@link JsonProvider}. For {@link #PDF} and
     * {@link #BINARY}, this field is {@code null}.
     * </p>
     */
    private Provider provider;

    /**
     * The corresponding HTTP media type.
     * <p>
     * Defines the MIME type of the response content, e.g., XML corresponds to {@code application/xml;charset=UTF-8},
     * and JSON corresponds to {@code application/json}. For {@link #PDF} and {@link #BINARY}, this field is
     * {@code null}.
     * </p>
     */
    private MediaType mediaType;

}
