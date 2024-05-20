/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.lang.reflect;

import org.miaixz.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;

/**
 * Type类型参考
 * 通过构建一个类型参考子类，可以获取其泛型参数中的Type类型。例如：
 *
 * <pre>
 * TypeReference&lt;List&lt;String&gt;&gt; list = new TypeReference&lt;List&lt;String&gt;&gt;() {};
 * Type t = tr.getType();
 * </pre>
 * 此类无法应用于通配符泛型参数（wildcard parameters），比如：{@code Class<?>} 或者 {@code List? extends CharSequence>}
 *
 * <p>
 * 此类参考FastJSON的TypeReference实现
 * </p>
 *
 * @param <T> 需要自定义的参考类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TypeReference<T> implements Type {

    /**
     * 泛型参数
     */
    private final Type type;

    /**
     * 构造
     */
    public TypeReference() {
        this.type = TypeKit.getTypeArgument(getClass());
    }

    /**
     * 获取用户定义的泛型参数
     *
     * @return 泛型参数
     */
    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }

}
