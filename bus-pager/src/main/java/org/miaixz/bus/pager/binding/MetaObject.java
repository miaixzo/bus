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
package org.miaixz.bus.pager.binding;

import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.exception.PageException;

/**
 * Provides a compatibility layer for accessing MyBatis's {@code MetaObject.forObject} method across different MyBatis
 * versions. It dynamically determines the correct method signature based on the available MyBatis reflection classes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MetaObject {

    /**
     * The {@link Method} object representing the {@code forObject} method of MyBatis's MetaObject. This field is
     * initialized statically to ensure compatibility with different MyBatis versions.
     */
    public static Method method;

    static {
        try {
            // In higher versions, MetaObject.forObject has 4 parameters, while lower versions have 1.
            // First, check if the current version is a higher version.
            Class.forName("org.apache.ibatis.reflection.ReflectorFactory");
            // The MetaObjectWithCache below carries reflection cache information.
            Class<?> metaClass = Class.forName("org.miaixz.bus.pager.binding.MetaObjectWithCache");
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
                    throw new PageException(e3);
                }
            }
        }
    }

    /**
     * Invokes the appropriate {@code forObject} method of MyBatis's MetaObject to create a
     * {@link org.apache.ibatis.reflection.MetaObject} instance.
     *
     * @param object the object for which to create a MetaObject
     * @return a {@link org.apache.ibatis.reflection.MetaObject} instance for the given object
     * @throws PageException if an error occurs during method invocation
     */
    public static org.apache.ibatis.reflection.MetaObject forObject(Object object) {
        try {
            return (org.apache.ibatis.reflection.MetaObject) method.invoke(null, object);
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

}
