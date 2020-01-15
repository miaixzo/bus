/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.lang;

import org.aoju.bus.core.lang.mutable.MutableObject;

/**
 * 为不可变的对象引用提供一个可变的包装,在java中支持引用传递
 *
 * @param <T> 所持有值类型
 * @author Kimi Liu
 * @version 5.5.3
 * @since JDK 1.8+
 */
public final class Holder<T> extends MutableObject<T> {

    /**
     * 构造
     */
    public Holder() {
        super();
    }

    /**
     * 构造
     *
     * @param value 被包装的对象
     */
    public Holder(T value) {
        super(value);
    }

    /**
     * 新建Holder类,持有指定值,当值为空时抛出空指针异常
     *
     * @param <T>   被持有的对象类型
     * @param value 值,不能为空
     * @return Holder
     */
    public static <T> Holder<T> of(T value) throws NullPointerException {
        if (null == value) {
            throw new NullPointerException("Holder can not hold a null value!");
        }
        return new Holder<>(value);
    }

}
