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
 * @since Java 17+
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
