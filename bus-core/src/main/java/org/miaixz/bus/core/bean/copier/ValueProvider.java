/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.bean.copier;

import java.lang.reflect.Type;

/**
 * An abstract interface for value providers, used to supply corresponding values during Bean injection. Implement or
 * anonymously instantiate this interface. During the Bean injection process, the Bean obtains a field name, and an
 * external mechanism uses this field name to find the corresponding field value, which is then injected into the Bean.
 *
 * @param <K> The type of the key, typically {@link String}.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ValueProvider<K> {

    /**
     * Retrieves the value associated with the given key. The returned value generally needs to match the type being
     * injected. If it does not match, a default conversion will be attempted using
     * {@code Convert#convert(Type, Object)}.
     *
     * @param key       The parameter name in the Bean object.
     * @param valueType The type of the value being injected.
     * @return The value corresponding to the parameter name.
     */
    Object value(K key, Type valueType);

    /**
     * Checks if the value provider contains the specified key. If it does not contain the key, injection is ignored.
     * The significance of this method is that for some value providers (e.g., Map), a key might exist but its value is
     * {@code null}. If this {@code null} value needs to be injected, this method is used to determine if the key is
     * present.
     *
     * @param key The parameter name in the Bean object.
     * @return {@code true} if the specified key is contained, {@code false} otherwise.
     */
    boolean containsKey(K key);

}
