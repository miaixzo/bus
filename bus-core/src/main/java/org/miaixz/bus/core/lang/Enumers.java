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
package org.miaixz.bus.core.lang;

import java.io.Serializable;

/**
 * 枚举元素通用接口，在自定义枚举上实现此接口可以用于数据转换
 * 数据库保存时建议保存 intVal()而非ordinal()防备需求变更
 *
 * @param <E> Enum类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Enumers<E extends Enumers<E>> extends Serializable {

    /**
     * 枚举编码
     *
     * @return 编码
     */
    int intVal();

    /**
     * 枚举名称
     *
     * @return 名称
     */
    String name();

    /**
     * 在中文语境下，多数时间枚举会配合一个中文说明
     *
     * @return 描述
     */
    default String text() {
        return name();
    }

    /**
     * 获取所有枚举对象
     *
     * @return 枚举对象数组
     */
    default E[] items() {
        return (E[]) this.getClass().getEnumConstants();
    }

    /**
     * 通过int类型值查找兄弟其他枚举
     *
     * @param intVal int值
     * @return Enum
     */
    default E from(final Integer intVal) {
        if (intVal == null) {
            return null;
        }
        final E[] vs = items();
        for (final E enumItem : vs) {
            if (enumItem.intVal() == intVal) {
                return enumItem;
            }
        }
        return null;
    }

    /**
     * 通过String类型的值转换，根据实现可以用name/text
     *
     * @param strVal String值
     * @return Enum
     */
    default E from(final String strVal) {
        if (strVal == null) {
            return null;
        }
        final E[] vs = items();
        for (final E enumItem : vs) {
            if (strVal.equalsIgnoreCase(enumItem.name())) {
                return enumItem;
            }
        }
        return null;
    }

}

