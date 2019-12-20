/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.builder;

/**
 * 包装一个身份密钥(System.identityHashCode())，以便一个对象只能等于equal()自身.
 * 这对于消除偶尔出现的重复的identityHashCodes是必要的.
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
final class HashKey {

    private final Object value;
    private final int id;

    /**
     * 构造函数
     *
     * @param _value The value
     */
    HashKey(final Object _value) {
        // 这是对象哈希码
        id = System.identityHashCode(_value);
        // 有一些情况(LANG-459)会为不同的对象返回相同的标识哈希码。
        // 因此，还添加了值来消除这些情况的歧义
        value = _value;
    }


    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HashKey)) {
            return false;
        }
        final HashKey hashKey = (HashKey) other;
        if (id != hashKey.id) {
            return false;
        }
        return value == hashKey.value;
    }

}
