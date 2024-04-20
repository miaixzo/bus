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
package org.miaixz.bus.goalie;

/**
 * api registry
 *
 * @author Justubborn
 * @since Java 17+
 */
public interface Registry<T> {

    /**
     * 初始化
     */
    void init();

    /**
     * 添加
     *
     * @param key id;
     * @param reg 路由
     * @return true or false
     */
    boolean add(String key, T reg);

    /**
     * 删除
     *
     * @param key id
     * @return true or false
     */
    boolean remove(String key);

    /**
     * 修改
     *
     * @param key id
     * @param reg 路由
     * @return true or false
     */
    boolean amend(String key, T reg);

    /**
     * 刷新路由
     */
    void refresh();

    /**
     * 获取路由
     *
     * @param id 路由标识
     * @return 路由列表
     */
    T get(String id);

}
