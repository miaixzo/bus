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
package org.miaixz.bus.goalie;

/**
 * 通用注册表接口，用于管理和操作键值对数据（如路由、限流配置等）
 *
 * @param <T> 注册表中存储的值类型
 * @author Justubborn
 * @since Java 17+
 */
public interface Registry<T> {

    /**
     * 初始化注册表，加载初始数据或配置
     */
    void init();

    /**
     * 添加键值对到注册表
     *
     * @param key 键，唯一标识
     * @param reg 值，待注册的对象
     * @return 如果添加成功返回 true，否则返回 false
     */
    boolean add(String key, T reg);

    /**
     * 从注册表中移除指定键的记录
     *
     * @param key 键，唯一标识
     * @return 如果移除成功返回 true，否则返回 false
     */
    boolean remove(String key);

    /**
     * 修改注册表中的键值对
     *
     * @param key 键，唯一标识
     * @param reg 新的值
     * @return 如果修改成功返回 true，否则返回 false
     */
    boolean amend(String key, T reg);

    /**
     * 刷新注册表，重新加载数据或清空后初始化
     */
    void refresh();

    /**
     * 获取指定键对应的值
     *
     * @param id 键，唯一标识
     * @return 对应的值，若不存在返回 null
     */
    T get(String id);

}