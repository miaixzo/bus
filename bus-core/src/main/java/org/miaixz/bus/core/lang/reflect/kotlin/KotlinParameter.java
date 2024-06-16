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
package org.miaixz.bus.core.lang.reflect.kotlin;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * kotlin.reflect.KParameter实例表示类
 * 通过反射获取Kotlin中KParameter相关属性值
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KotlinParameter {

    private static final Method METHOD_GET_NAME;
    private static final Method METHOD_GET_TYPE;
    private static final Method METHOD_GET_JAVA_TYPE;

    static {
        final Class<?> kParameterClass = ClassKit.loadClass("kotlin.reflect.KParameter");
        METHOD_GET_NAME = MethodKit.getMethod(kParameterClass, "getName");
        METHOD_GET_TYPE = MethodKit.getMethod(kParameterClass, "getType");

        final Class<?> kTypeClass = ClassKit.loadClass("kotlin.reflect.jvm.internal.KTypeImpl");
        METHOD_GET_JAVA_TYPE = MethodKit.getMethod(kTypeClass, "getJavaType");
    }

    private final String name;
    private final Class<?> type;

    /**
     * 构造
     *
     * @param kParameterInstance kotlin.reflect.KParameter实例对象
     */
    public KotlinParameter(final Object kParameterInstance) {
        this.name = MethodKit.invoke(kParameterInstance, METHOD_GET_NAME);
        final Object kType = MethodKit.invoke(kParameterInstance, METHOD_GET_TYPE);
        this.type = MethodKit.invoke(kType, METHOD_GET_JAVA_TYPE);
    }

    /**
     * 获取参数名
     *
     * @return 参数名
     */
    public String getName() {
        return name;
    }

    /**
     * 获取参数类型
     *
     * @return 参数类型
     */
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final KotlinParameter that = (KotlinParameter) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "KotlinParameter{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

}
