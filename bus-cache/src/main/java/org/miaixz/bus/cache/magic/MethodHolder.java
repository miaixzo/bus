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
package org.miaixz.bus.cache.magic;

import lombok.Getter;
import lombok.Setter;

/**
 * 方法持有者类
 * <p>
 * 用于存储方法的相关信息，包括返回类型、内部返回类型和是否返回集合类型。 使用Lombok注解简化getter和setter方法的编写。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class MethodHolder {

    /**
     * 内部返回类型，用于集合类型时表示集合元素的类型
     */
    private Class<?> innerReturnType;

    /**
     * 方法返回类型
     */
    private Class<?> returnType;

    /**
     * 是否返回集合类型
     */
    private boolean collection;

    /**
     * 构造方法
     *
     * @param collection 是否返回集合类型
     */
    public MethodHolder(boolean collection) {
        this.collection = collection;
    }

    /**
     * 判断是否返回集合类型
     *
     * @return 如果返回集合类型则返回true，否则返回false
     */
    public boolean isCollection() {
        return collection;
    }

    /**
     * 获取方法返回类型
     *
     * @return 方法返回类型
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * 设置方法返回类型
     *
     * @param returnType 方法返回类型
     */
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    /**
     * 获取内部返回类型
     *
     * @return 内部返回类型
     */
    public Class<?> getInnerReturnType() {
        return innerReturnType;
    }

    /**
     * 设置内部返回类型
     *
     * @param innerReturnType 内部返回类型
     */
    public void setInnerReturnType(Class<?> innerReturnType) {
        this.innerReturnType = innerReturnType;
    }

}