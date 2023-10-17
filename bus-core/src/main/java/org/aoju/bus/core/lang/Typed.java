/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * {@link ParameterizedType} 接口实现，用于重新定义泛型类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Typed implements ParameterizedType, Serializable {

    private static final long serialVersionUID = 1L;

    private final Type[] actualTypeArguments;
    private final Type ownerType;
    private final Type rawType;

    /**
     * 构造
     *
     * @param actualTypeArguments 实际的泛型参数类型
     * @param ownerType           拥有者类型
     * @param rawType             原始类型
     */
    public Typed(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    /**
     * 追加 {@code types} 到 @{code buf}，使用 {@code sep} 分隔
     *
     * @param buf   目标
     * @param sep   分隔符
     * @param types 加入的类型
     * @return {@code buf}
     */
    private static StringBuilder appendAllTo(final StringBuilder buf, final String sep, final Type... types) {
        if (ArrayKit.isNotEmpty(types)) {
            boolean isFirst = true;
            for (Type type : types) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buf.append(sep);
                }

                String typeStr;
                if (type instanceof Class) {
                    typeStr = ((Class<?>) type).getName();
                } else {
                    typeStr = StringKit.toString(type);
                }

                buf.append(typeStr);
            }
        }
        return buf;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();

        final Type useOwner = this.ownerType;
        final Class<?> raw = (Class<?>) this.rawType;
        if (null == useOwner) {
            buf.append(raw.getName());
        } else {
            if (useOwner instanceof Class<?>) {
                buf.append(((Class<?>) useOwner).getName());
            } else {
                buf.append(useOwner.toString());
            }
            buf.append(Symbol.C_DOT).append(raw.getSimpleName());
        }

        appendAllTo(buf.append(Symbol.C_LT), ", ", this.actualTypeArguments).append(Symbol.C_GT);
        return buf.toString();
    }

}
