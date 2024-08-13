/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ModifierKit;

/**
 * Bean描述 包括Record自定义字段及对应方法，getter方法与字段名同名，不支持setter
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecordBeanDesc extends AbstractBeanDesc {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param beanClass Bean类
     */
    public RecordBeanDesc(final Class<?> beanClass) {
        super(beanClass);
        initForRecord();
    }

    /**
     * 针对Record类的反射初始化
     */
    private void initForRecord() {
        final Class<?> beanClass = this.beanClass;
        final Map<String, PropDesc> propMap = this.propMap;

        final Method[] getters = MethodKit.getPublicMethods(beanClass, method -> 0 == method.getParameterCount());
        // 排除静态属性和对象子类
        final Field[] fields = FieldKit.getFields(beanClass,
                field -> !ModifierKit.isStatic(field) && !FieldKit.isOuterClassField(field));
        for (final Field field : fields) {
            for (final Method getter : getters) {
                if (field.getName().equals(getter.getName())) {
                    // record对象，getter方法与字段同名
                    final PropDesc prop = new PropDesc(field, getter, null);
                    propMap.putIfAbsent(prop.getFieldName(), prop);
                }
            }
        }
    }

}
