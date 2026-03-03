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
package org.miaixz.bus.core.bean.desc;

import java.io.Serial;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.lang.Assert;

/**
 * An abstract base class for bean descriptors, providing common properties and methods for describing a bean's class
 * and its properties.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractBeanDesc implements BeanDesc {

    @Serial
    private static final long serialVersionUID = 2852291999885L;

    /**
     * The class of the bean being described.
     */
    protected final Class<?> beanClass;

    /**
     * A map of property names to their corresponding {@link PropDesc} descriptors.
     */
    protected final Map<String, PropDesc> propMap = new LinkedHashMap<>();

    /**
     * Constructs a new {@code AbstractBeanDesc}.
     *
     * @param beanClass The class of the bean to describe.
     */
    public AbstractBeanDesc(final Class<?> beanClass) {
        this.beanClass = Assert.notNull(beanClass);
    }

    /**
     * Gets the fully qualified name of the bean class.
     *
     * @return The bean's class name.
     */
    public String getName() {
        return this.beanClass.getName();
    }

    /**
     * Gets the simple name of the bean class.
     *
     * @return The bean's simple class name.
     */
    public String getSimpleName() {
        return this.beanClass.getSimpleName();
    }

    /**
     * Gets the {@link Class} object of the bean.
     *
     * @return The bean's class.
     */
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    /**
     * Gets the {@link Field} object for a given property name.
     *
     * @param fieldName The name of the property.
     * @return The corresponding {@link Field} object, or null if not found.
     */
    public Field getField(final String fieldName) {
        final PropDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getField();
    }

    /**
     * Gets the property map.
     *
     * @param ignoreCase whether to ignore case when comparing keys
     * @return the property map
     */
    @Override
    public Map<String, PropDesc> getPropMap(final boolean ignoreCase) {
        return ignoreCase ? new CaseInsensitiveMap<>(1, this.propMap) : this.propMap;
    }

}
