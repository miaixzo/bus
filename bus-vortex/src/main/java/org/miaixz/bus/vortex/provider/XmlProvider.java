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
 * An implementation of {@link Provider} for serializing objects into XML strings.
 * <p>
 * This provider uses a two-step process: it first converts the Java object into a generic {@code Map} using
 * {@link JsonKit}, and then serializes that map into an XML string. This means the resulting XML structure will mirror
 * the object's JSON representation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlProvider implements Provider {

    /**
     * Serializes the given Java object into its XML string representation.
     * <p>
     * This method first constructs a standard XML header. It then converts the input object into a {@code Map} and
     * subsequently serializes this map into an XML string. Any exceptions during serialization will bubble up as
     * runtime exceptions and be handled by the global error handler.
     *
     * @param bean The object to be serialized.
     * @return The serialized XML string.
     */
    @Override
    public String serialize(Object bean) {
        Map<String, Object> map = JsonKit.toMap(bean);
        String buffer = XmlKit.mapToXmlString(map, "response");
        return buffer.replaceFirst(" standalone=\"[^\"]*\"", Normal.EMPTY);
    }

}
