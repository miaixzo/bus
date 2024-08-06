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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.lang.Keys;

/**
 * java.lang.Record 相关工具类封装 来自于FastJSON2
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecordKit {

    private static volatile Class<?> RECORD_CLASS;
    private static volatile Method METHOD_GET_RECORD_COMPONENTS;
    private static volatile Method METHOD_COMPONENT_GET_NAME;
    private static volatile Method METHOD_COMPONENT_GET_GENERIC_TYPE;

    /**
     * 判断给定类是否为Record类
     *
     * @param clazz 类
     * @return 是否为Record类
     */
    public static boolean isRecord(final Class<?> clazz) {
        if (Keys.JVM_VERSION < 14) {
            // JDK14+支持Record类
            return false;
        }
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass == null) {
            return false;
        }

        if (RECORD_CLASS == null) {
            // 此处不使用同步代码，重复赋值并不影响判断
            final String superclassName = superClass.getName();
            if ("java.lang.Record".equals(superclassName)) {
                RECORD_CLASS = superClass;
                return true;
            } else {
                return false;
            }
        }

        return superClass == RECORD_CLASS;
    }

    /**
     * 获取Record类中所有字段名称，getter方法名与字段同名
     *
     * @param recordClass Record类
     * @return 字段数组
     */
    public static Map.Entry<String, Type>[] getRecordComponents(final Class<?> recordClass) {
        if (Keys.JVM_VERSION < 14) {
            // JDK14+支持Record类
            return new Map.Entry[0];
        }
        if (null == METHOD_GET_RECORD_COMPONENTS) {
            METHOD_GET_RECORD_COMPONENTS = MethodKit.getMethod(Class.class, "getRecordComponents");
        }

        final Class<Object> recordComponentClass = ClassKit.loadClass("java.lang.reflect.RecordComponent");
        if (METHOD_COMPONENT_GET_NAME == null) {
            METHOD_COMPONENT_GET_NAME = MethodKit.getMethod(recordComponentClass, "getName");
        }
        if (METHOD_COMPONENT_GET_GENERIC_TYPE == null) {
            METHOD_COMPONENT_GET_GENERIC_TYPE = MethodKit.getMethod(recordComponentClass, "getGenericType");
        }

        final Object[] components = MethodKit.invoke(recordClass, METHOD_GET_RECORD_COMPONENTS);
        final Map.Entry<String, Type>[] entries = new Map.Entry[components.length];
        for (int i = 0; i < components.length; i++) {
            entries[i] = new AbstractMap.SimpleEntry<>(MethodKit.invoke(components[i], METHOD_COMPONENT_GET_NAME),
                    MethodKit.invoke(components[i], METHOD_COMPONENT_GET_GENERIC_TYPE));
        }

        return entries;
    }

    /**
     * 实例化Record类
     *
     * @param recordClass   类
     * @param valueProvider 参数值提供器
     * @return Record类
     */
    public static Object newInstance(final Class<?> recordClass, final ValueProvider<String> valueProvider) {
        final Map.Entry<String, Type>[] recordComponents = getRecordComponents(recordClass);
        final Object[] args = new Object[recordComponents.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = valueProvider.value(recordComponents[i].getKey(), recordComponents[i].getValue());
        }

        return ReflectKit.newInstance(recordClass, args);
    }

}
