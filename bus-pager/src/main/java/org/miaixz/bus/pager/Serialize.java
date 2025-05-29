/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.pager;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页信息类，用于存储分页查询的结果集和总记录数，支持序列化。
 *
 * @param <T> 分页数据元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Serialize<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282139500L;

    /**
     * 总记录数
     */
    protected long total;
    /**
     * 分页结果集
     */
    protected List<T> list;

    /**
     * 默认构造函数。
     */
    public Serialize() {

    }

    /**
     * 构造函数，基于结果集初始化。
     *
     * @param list 分页结果列表
     */
    public Serialize(List<? extends T> list) {
        this.list = (List<T>) list;
        if (list instanceof Page) {
            this.total = ((Page<?>) list).getTotal();
        } else {
            this.total = list.size();
        }
    }

    /**
     * 静态工厂方法，创建Serialize对象。
     *
     * @param list 分页结果列表
     * @param <T>  分页数据元素类型
     * @return Serialize对象
     */
    public static <T> Serialize<T> of(List<? extends T> list) {
        return new Serialize<>(list);
    }

    /**
     * 获取总记录数。
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 设置总记录数。
     *
     * @param total 总记录数
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * 获取分页结果集。
     *
     * @return 分页结果列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 设置分页结果集。
     *
     * @param list 分页结果列表
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * 返回Serialize对象的字符串表示。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "Serialize{" + "total=" + total + ", list=" + list + '}';
    }

}