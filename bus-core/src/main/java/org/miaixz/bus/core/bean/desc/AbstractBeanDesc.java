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

    @Override
    public Map<String, PropDesc> getPropMap(final boolean ignoreCase) {
        return ignoreCase ? new CaseInsensitiveMap<>(1, this.propMap) : this.propMap;
    }

}
