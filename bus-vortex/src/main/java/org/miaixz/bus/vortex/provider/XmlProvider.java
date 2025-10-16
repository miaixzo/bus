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
package org.miaixz.bus.vortex.provider;

import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.XmlKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.vortex.Provider;

/**
 * XML serialization provider, implementing the conversion of objects to XML strings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlProvider implements Provider {

    /**
     * Serializes an object into an XML string.
     * <p>
     * This method first constructs a standard XML header. It then attempts to convert the input object into a Map
     * structure using {@link JsonKit#getProvider()} and {@code toMap(Object)}, and subsequently serializes this Map
     * into an XML string using {@link XmlKit#mapToXmlString(Map)}. If any error occurs during serialization, it prints
     * the stack trace and returns an empty string.
     * </p>
     *
     * @param object The object to be serialized.
     * @return The serialized XML string, or an empty string if serialization fails.
     */
    @Override
    public String serialize(Object object) {
        try {
            // Create XML header
            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

            // Convert the object to a Map structure and serialize to XML
            Map<String, Object> map = JsonKit.toMap(object);
            buffer.append(XmlKit.mapToXmlString(map));

            return buffer.toString();
        } catch (Exception e) {
            // Catch exception and print stack trace
            e.printStackTrace();
        }
        // Return an empty string if serialization fails
        return Normal.EMPTY;
    }

}
