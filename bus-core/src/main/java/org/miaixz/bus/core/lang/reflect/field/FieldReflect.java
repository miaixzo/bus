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
package org.miaixz.bus.core.lang.reflect.field;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * 字段反射类 此类持有类中字段的缓存，如果字段在类中修改，则需要手动调用clearCaches方法清除缓存。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldReflect {

    private final Class<?> clazz;
    private volatile Field[] declaredFields;
    private volatile Field[] allFields;

    /**
     * 构造
     *
     * @param clazz 类
     */
    public FieldReflect(final Class<?> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * 创建FieldReflect
     *
     * @param clazz 类
     * @return FieldReflect
     */
    public static FieldReflect of(final Class<?> clazz) {
        return new FieldReflect(clazz);
    }

    /**
     * 获取当前类
     *
     * @return 当前类
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 清空缓存
     */
    synchronized public void clearCaches() {
        declaredFields = null;
        allFields = null;
    }

    /**
     * 获得当前类声明的所有字段（包括非public字段），但不包括父类的字段
     *
     * @param predicate 过滤器
     * @return 字段数组
     * @throws SecurityException 安全检查异常
     */
    public Field[] getDeclaredFields(final Predicate<Field> predicate) {
        if (null == declaredFields) {
            synchronized (FieldReflect.class) {
                if (null == declaredFields) {
                    declaredFields = clazz.getDeclaredFields();
                }
            }
        }
        return ArrayKit.filter(declaredFields, predicate);
    }

    /**
     * 获得当前类和父类声明的所有字段（包括非public字段）
     *
     * @param predicate 过滤器
     * @return 字段数组
     * @throws SecurityException 安全检查异常
     */
    public Field[] getAllFields(final Predicate<Field> predicate) {
        if (null == allFields) {
            synchronized (FieldReflect.class) {
                if (null == allFields) {
                    allFields = getFieldsDirectly(true);
                }
            }
        }
        return ArrayKit.filter(allFields, predicate);
    }

    /**
     * 获得一个类中所有字段列表，直接反射获取，无缓存 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param withSuperClassFields 是否包括父类的字段列表
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public Field[] getFieldsDirectly(final boolean withSuperClassFields) throws SecurityException {
        Field[] allFields = null;
        Class<?> searchType = this.clazz;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (null == allFields) {
                allFields = declaredFields;
            } else {
                allFields = ArrayKit.append(allFields, declaredFields);
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }

        return allFields;
    }

}
