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
package org.miaixz.bus.core.scanner;

import java.lang.annotation.Annotation;

/**
 * 用于在{@link Synthetic}中表示一个处于合成状态的注解对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Synthesized extends Annotation {

    /**
     * 获取该合成注解对应的根节点
     *
     * @return 合成注解对应的根节点
     */
    Object getRoot();

    /**
     * 该合成注解是为根对象
     *
     * @return 根对象
     */
    default boolean isRoot() {
        return getRoot() == this;
    }

    /**
     * 获取被合成的注解对象
     *
     * @return 注解对象
     */
    Annotation getAnnotation();

    /**
     * 获取该合成注解与根对象的垂直距离。
     * 默认情况下，该距离即为当前注解与根对象之间相隔的层级数。
     *
     * @return 合成注解与根对象的垂直距离
     */
    int getVerticalDistance();

    /**
     * 获取该合成注解与根对象的水平距离。
     * 默认情况下，该距离即为当前注解与根对象之间相隔的已经被扫描到的注解数。
     *
     * @return 合成注解与根对象的水平距离
     */
    int getHorizontalDistance();

    /**
     * 注解是否存在该名称相同，且类型一致的属性
     *
     * @param attributeName 属性名
     * @param returnType    返回值类型
     * @return 是否存在该属性
     */
    boolean hasAttribute(String attributeName, Class<?> returnType);

    /**
     * 获取属性值
     *
     * @param attributeName 属性名
     * @return 属性值
     */
    Object getAttribute(String attributeName);

}
