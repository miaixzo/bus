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

import java.util.Set;

/**
 * 可以是 xml、yml、protobuf 等任何一种格式的数据
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CoverWapper {

    /**
     * @return JSON 的键值对数量
     */
    int size();

    /**
     * @return 是否为空
     */
    boolean isEmpty();

    /**
     * @param key 键名
     * @return 子 JsonObj
     */
    CoverWapper getWappers(String key);

    /**
     * @param key 键名
     * @return 子 JsonArr
     */
    CoverArray getArray(String key);

    /**
     * @param key 键名
     * @return boolean 值
     */
    boolean getBool(String key);

    /**
     * @param key 键名
     * @return int 值
     */
    int getInt(String key);

    /**
     * @param key 键名
     * @return long 值
     */
    long getLong(String key);

    /**
     * @param key 键名
     * @return float 值
     */
    float getFloat(String key);

    /**
     * @param key 键名
     * @return double 值
     */
    double getDouble(String key);

    /**
     * @param key 键名
     * @return String 值
     */
    String getString(String key);

    /**
     * @param key 键名
     * @return 是否有该键
     */
    boolean has(String key);

    /**
     * @return JSON 的键集合
     */
    Set<String> keySet();

}
