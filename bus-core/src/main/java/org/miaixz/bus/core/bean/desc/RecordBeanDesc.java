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
