/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.mapper.reflect;

import org.miaixz.bus.core.exception.InternalException;

import java.lang.reflect.Method;

/**
 * 反射工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MetaObject {

    public static Method method;

    static {
        try {
            // 高版本中的 MetaObject.forObject 有 4 个参数,低版本是 1 个
            // 先判断当前使用的是否为高版本
            Class.forName("org.apache.ibatis.reflection.ReflectorFactory");
            // 下面这个 MetaObjectWithCache 带反射的缓存信息
            Class<?> metaClass = Class.forName("org.miaixz.bus.mapper.reflect.MetaObjectWithCache");
            method = metaClass.getDeclaredMethod("forObject", Object.class);
        } catch (Throwable e1) {
            try {
                Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.SystemMetaObject");
                method = metaClass.getDeclaredMethod("forObject", Object.class);
            } catch (Exception e2) {
                try {
                    Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.MetaObject");
                    method = metaClass.getDeclaredMethod("forObject", Object.class);
                } catch (Exception e3) {
                    throw new InternalException(e3);
                }
            }
        }
    }

    public static org.apache.ibatis.reflection.MetaObject forObject(Object object) {
        try {
            return (org.apache.ibatis.reflection.MetaObject) method.invoke(null, object);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

}
