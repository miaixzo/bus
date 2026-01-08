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
package org.miaixz.bus.core.bean.copier.provider;

import java.lang.reflect.Type;

import org.miaixz.bus.core.bean.DynaBean;
import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.convert.Convert;

/**
 * A {@link ValueProvider} implementation that retrieves values from a {@link DynaBean} object. This provider leverages
 * the dynamic property access capabilities of {@code DynaBean}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DynaBeanValueProvider implements ValueProvider<String> {

    /**
     * The {@link DynaBean} instance from which values are retrieved.
     */
    private final DynaBean dynaBean;
    /**
     * A flag indicating whether errors during value retrieval or conversion should be ignored.
     */
    private final boolean ignoreError;

    /**
     * Constructs a new {@code DynaBeanValueProvider}.
     *
     * @param dynaBean    The {@link DynaBean} instance. Must not be {@code null}.
     * @param ignoreError {@code true} to ignore errors during value retrieval or conversion, {@code false} otherwise.
     */
    public DynaBeanValueProvider(final DynaBean dynaBean, final boolean ignoreError) {
        this.dynaBean = dynaBean;
        this.ignoreError = ignoreError;
    }

    /**
     * Retrieves the value of a property from the {@link DynaBean} based on its key (property name). The value is
     * converted to the specified {@code valueType} if necessary.
     *
     * @param key       The name of the property in the {@link DynaBean}.
     * @param valueType The type to which the retrieved value should be converted.
     * @return The value of the property, converted to {@code valueType}, or {@code null} if the property does not exist
     *         or an error occurs and {@code ignoreError} is {@code true}.
     */
    @Override
    public Object value(final String key, final Type valueType) {
        final Object value = dynaBean.get(key);
        return Convert.convertWithCheck(valueType, value, null, this.ignoreError);
    }

    /**
     * Checks if the {@link DynaBean} contains a property with the specified key.
     *
     * @param key The name of the property in the {@link DynaBean}.
     * @return {@code true} if the {@link DynaBean} contains the property, {@code false} otherwise.
     */
    @Override
    public boolean containsKey(final String key) {
        return dynaBean.containsProp(key);
    }

}
