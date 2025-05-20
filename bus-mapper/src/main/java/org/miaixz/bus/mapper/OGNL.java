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
package org.miaixz.bus.mapper;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.support.ClassColumn;
import org.miaixz.bus.mapper.support.ClassField;

/**
 * OGNL 静态方法工具类，提供类型注册、SPI 实例获取及函数式字段名转换功能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OGNL {

    /**
     * 注册新的简单类型。
     *
     * @param clazz 要注册的类型
     */
    public static void registerSimpleType(Class<?> clazz) {
        Args.SIMPLE_TYPE_SET.add(clazz);
    }

    /**
     * 批量注册简单类型，通过逗号分隔的类名字符串。
     *
     * @param classes 类名字符串，格式为全限定类名，逗号分隔
     * @throws RuntimeException 如果类名无效或无法找到
     */
    public static void registerSimpleType(String classes) {
        if (StringKit.isNotEmpty(classes)) {
            String[] cls = classes.split(",");
            for (String c : cls) {
                try {
                    Args.SIMPLE_TYPE_SET.add(Class.forName(c));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to register type: " + c, e);
                }
            }
        }
    }

    /**
     * 静默注册简单类型，忽略类不存在的异常。
     *
     * @param clazz 类名
     */
    public static void registerSimpleTypeSilence(String clazz) {
        try {
            Args.SIMPLE_TYPE_SET.add(Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            Logger.debug("Class not found, ignored: " + clazz);
        }
    }

    /**
     * 判断指定类是否为已知的简单类型。
     *
     * @param clazz 要检查的类
     * @return 如果是简单类型则返回 true，否则返回 false
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return Args.SIMPLE_TYPE_SET.contains(clazz);
    }

    /**
     * 获取指定接口或类的所有 SPI 实现实例，并按 ORDER 接口的顺序排序（如果适用）。
     *
     * @param clazz 接口或类
     * @param <T>   类型参数
     * @return 按顺序排列的实现实例列表
     */
    public static <T> List<T> getInstances(Class<T> clazz) {
        List<T> list = NormalSpiLoader.loadList(false, clazz);
        if (list.size() > 1 && ORDER.class.isAssignableFrom(clazz)) {
            list.sort(Comparator.comparing(f -> ((ORDER) f).getOrder()).reversed());
        }
        return list;
    }

    /**
     * 将函数式接口 Fn 转换为对应的字段名或列名。
     *
     * @param fn 函数式接口实例
     * @return 包含类和字段名/列名的 ClassField 或 ClassColumn 对象
     * @throws RuntimeException 如果反射操作失败
     */
    public static ClassField fnToFieldName(Fn<?, ?> fn) {
        try {
            Class<?> clazz = null;
            if (fn instanceof Fn.FnName<?, ?> field) {
                if (field.column) {
                    return new ClassColumn(field.entityClass, field.name);
                } else {
                    return new ClassField(field.entityClass, field.name);
                }
            }
            if (fn instanceof Fn.FnType) {
                clazz = ((Fn.FnType<?, ?>) fn).entityClass;
                fn = ((Fn.FnType<?, ?>) fn).fn;
                while (fn instanceof Fn.FnType) {
                    fn = ((Fn.FnType<?, ?>) fn).fn;
                }
            }
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
            String getter = serializedLambda.getImplMethodName();
            if (Args.GET_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(3);
            } else if (Args.IS_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(2);
            }
            String field = Introspector.decapitalize(getter);
            if (clazz == null) {
                Matcher matcher = Args.INSTANTIATED_CLASS_PATTERN.matcher(serializedLambda.getInstantiatedMethodType());
                String implClass;
                if (matcher.find()) {
                    implClass = matcher.group("cls").replaceAll("/", "\\.");
                } else {
                    implClass = serializedLambda.getImplClass().replaceAll("/", "\\.");
                }
                clazz = Class.forName(implClass);
            }
            return new ClassField(clazz, field);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to convert Fn to field name", e);
        }
    }

}