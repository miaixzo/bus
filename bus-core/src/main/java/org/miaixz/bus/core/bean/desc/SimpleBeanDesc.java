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

import java.beans.Introspector;
import java.io.Serial;
import java.lang.reflect.Method;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A simple Bean description that primarily focuses on finding getter and setter methods. The rules for method discovery
 * are as follows:
 * <ul>
 * <li>It does not match fields directly; it only searches for {@code getXXX}, {@code isXXX}, and {@code setXXX}
 * methods.</li>
 * <li>If both {@code getXXX} and {@code isXXX} methods exist, and the return type is {@code Boolean} or
 * {@code boolean}, {@code isXXX} takes precedence.</li>
 * <li>If multiple overloaded {@code setXXX} methods exist, the method with the most specific parameter type takes
 * precedence (e.g., {@code setXXX(List)} over {@code setXXX(Collection)}).</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SimpleBeanDesc extends AbstractBeanDesc {

    @Serial
    private static final long serialVersionUID = 2852227709360L;

    /**
     * Constructs a {@code SimpleBeanDesc} for the given Bean class.
     *
     * @param beanClass The class of the Bean. Must not be {@code null}.
     */
    public SimpleBeanDesc(final Class<?> beanClass) {
        super(beanClass);
        init();
    }

    /**
     * Initializes the Bean description by finding and loading getter and setter methods.
     */
    private void init() {
        final Map<String, PropDesc> propMap = this.propMap;

        final Method[] gettersAndSetters = MethodKit
                .getPublicMethods(this.beanClass, MethodKit::isGetterOrSetterIgnoreCase);
        boolean isSetter;
        int nameIndex;
        String methodName;
        String fieldName;
        for (final Method method : gettersAndSetters) {
            methodName = method.getName();
            switch (methodName.charAt(0)) {
                case 's':
                    isSetter = true;
                    nameIndex = 3;
                    break;

                case 'g':
                    isSetter = false;
                    nameIndex = 3;
                    break;

                case 'i':
                    isSetter = false;
                    nameIndex = 2;
                    break;

                default:
                    continue;
            }

            fieldName = Introspector.decapitalize(StringKit.toStringOrNull(methodName.substring(nameIndex)));
            PropDesc propDesc = propMap.get(fieldName);
            if (null == propDesc) {
                propDesc = new PropDesc(fieldName, isSetter ? null : method, isSetter ? method : null);
                propMap.put(fieldName, propDesc);
            } else {
                if (isSetter) {
                    if (null == propDesc.setter
                            || propDesc.setter.getTypeClass().isAssignableFrom(method.getParameterTypes()[0])) {
                        // If multiple overloaded setter methods exist, choose the one with the most specific parameter
                        // type.
                        propDesc.setter = MethodInvoker.of(method);
                    }
                } else {
                    if (null == propDesc.getter || (BooleanKit.isBoolean(propDesc.getter.getTypeClass())
                            && BooleanKit.isBoolean(method.getReturnType()) && methodName.startsWith(Normal.IS))) {
                        // If the return type is Boolean or boolean, isXXX takes precedence over getXXX.
                        propDesc.getter = MethodInvoker.of(method);
                    }
                }
            }
        }
    }

}
