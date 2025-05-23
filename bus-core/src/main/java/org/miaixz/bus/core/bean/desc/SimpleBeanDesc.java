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
 * 简单的Bean描述，只查找getter和setter方法，规则如下：
 * <ul>
 * <li>不匹配字段，只查找getXXX、isXXX、setXXX方法。</li>
 * <li>如果同时存在getXXX和isXXX，返回值为Boolean或boolean，isXXX优先。</li>
 * <li>如果同时存在setXXX的多个重载方法，最小子类优先，如setXXX(List)优先于setXXX(Collection)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleBeanDesc extends AbstractBeanDesc {

    @Serial
    private static final long serialVersionUID = 2852291216519L;

    /**
     * 构造
     *
     * @param beanClass Bean类
     */
    public SimpleBeanDesc(final Class<?> beanClass) {
        super(beanClass);
        init();
    }

    /**
     * 普通Bean初始化，查找和加载getter和setter
     */
    private void init() {
        final Map<String, PropDesc> propMap = this.propMap;

        final Method[] gettersAndSetters = MethodKit.getPublicMethods(this.beanClass,
                MethodKit::isGetterOrSetterIgnoreCase);
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
                        // 如果存在多个重载的setter方法，选择参数类型最匹配的
                        propDesc.setter = MethodInvoker.of(method);
                    }
                } else {
                    if (null == propDesc.getter || (BooleanKit.isBoolean(propDesc.getter.getTypeClass())
                            && BooleanKit.isBoolean(method.getReturnType()) && methodName.startsWith(Normal.IS))) {
                        // 如果返回值为Boolean或boolean，isXXX优先于getXXX
                        propDesc.getter = MethodInvoker.of(method);
                    }
                }
            }
        }
    }

}
