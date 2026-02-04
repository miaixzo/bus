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
