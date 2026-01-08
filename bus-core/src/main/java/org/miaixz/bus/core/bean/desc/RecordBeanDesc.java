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
package org.miaixz.bus.core.bean.desc;

import java.io.Serial;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ModifierKit;

/**
 * Bean description for Java Record classes. This class handles the specific characteristics of Record components, where
 * getter methods are named identically to their corresponding fields, and setters are not supported.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecordBeanDesc extends AbstractBeanDesc {

    @Serial
    private static final long serialVersionUID = 2852227223863L;

    /**
     * Constructs a {@code RecordBeanDesc} for the given Record class.
     *
     * @param beanClass The class of the Record. Must not be {@code null}.
     */
    public RecordBeanDesc(final Class<?> beanClass) {
        super(beanClass);
        initForRecord();
    }

    /**
     * Initializes the property descriptors specifically for Record classes. It identifies fields and their
     * corresponding getter methods (which have the same name as the field).
     */
    private void initForRecord() {
        final Class<?> beanClass = this.beanClass;
        final Map<String, PropDesc> propMap = this.propMap;

        // Get all public methods with no parameters, which are typically getters for Record components.
        final Method[] getters = MethodKit.getPublicMethods(beanClass, method -> 0 == method.getParameterCount());
        // Exclude static fields and outer class fields.
        final Field[] fields = FieldKit
                .getFields(beanClass, field -> !ModifierKit.isStatic(field) && !FieldKit.isOuterClassField(field));
        for (final Field field : fields) {
            for (final Method getter : getters) {
                if (field.getName().equals(getter.getName())) {
                    // For Record objects, the getter method has the same name as the field.
                    final PropDesc prop = new PropDesc(field, getter, null);
                    propMap.putIfAbsent(prop.getFieldName(), prop);
                }
            }
        }
    }

}
