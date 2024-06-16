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
package org.miaixz.bus.http.plugin.httpv;

/**
 * 可以是 xml、yml、protobuf 等任何一种格式的数据
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoverArray {

    /**
     * @return JSON 的键值对数量
     */
    int size();

    /**
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * @param index 元素下标
     * @return 子 JsonObj
     */
    CoverWapper getMapper(int index);

    /**
     * @param index 元素下标
     * @return 子 JsonArr
     */
    CoverArray getArray(int index);

    /**
     * @param index 元素下标
     * @return boolean 值
     */
    boolean getBool(int index);

    /**
     * @param index 元素下标
     * @return int 值
     */
    int getInt(int index);

    /**
     * @param index 元素下标
     * @return long 值
     */
    long getLong(int index);

    /**
     * @param index 元素下标
     * @return float 值
     */
    float getFloat(int index);

    /**
     * @param index 元素下标
     * @return double 值
     */
    double getDouble(int index);

    /**
     * @param index 元素下标
     * @return String 值
     */
    String getString(int index);

}
