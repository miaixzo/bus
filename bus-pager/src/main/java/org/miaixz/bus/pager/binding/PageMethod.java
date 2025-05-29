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
package org.miaixz.bus.pager.binding;

import java.util.Properties;

import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.Querying;

/**
 * 提供基础分页方法，用于配置和管理MyBatis分页查询。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PageMethod {

    /**
     * 存储当前线程的分页参数
     */
    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();
    /**
     * 默认是否执行count查询
     */
    protected static boolean DEFAULT_COUNT = true;

    /**
     * 获取当前线程的分页参数。
     *
     * @param <T> 分页数据元素类型
     * @return 当前分页对象
     */
    public static <T> Page<T> getLocalPage() {
        return LOCAL_PAGE.get();
    }

    /**
     * 设置当前线程的分页参数。
     *
     * @param page 分页对象
     */
    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 清除当前线程的分页参数。
     */
    public static void clearPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * 获取任意查询的count总数。
     *
     * @param select 查询对象
     * @return 总记录数
     */
    public static long count(Querying select) {
        Page<?> page = startPage(1, -1, true).disableAsyncCount();
        select.doSelect();
        return page.getTotal();
    }

    /**
     * 从参数对象开始分页。
     *
     * @param params 参数对象
     * @param <E>    分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> startPage(Object params) {
        Page<E> page = PageObject.getPageFromObject(params, true);
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 开始分页，指定页码和页面大小。
     *
     * @param pageNo   页码（从1开始）
     * @param pageSize 每页记录数
     * @param <E>      分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize) {
        return startPage(pageNo, pageSize, DEFAULT_COUNT);
    }

    /**
     * 开始分页，指定页码、页面大小和是否执行count查询。
     *
     * @param pageNo   页码（从1开始）
     * @param pageSize 每页记录数
     * @param count    是否执行count查询
     * @param <E>      分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, boolean count) {
        return startPage(pageNo, pageSize, count, null, null);
    }

    /**
     * 开始分页，指定页码、页面大小和排序字段。
     *
     * @param pageNo   页码（从1开始）
     * @param pageSize 每页记录数
     * @param orderBy  排序字段
     * @param <E>      分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, String orderBy) {
        Page<E> page = startPage(pageNo, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页，指定页码、页面大小、count查询、分页合理化和零页大小处理。
     *
     * @param pageNo       页码（从1开始）
     * @param pageSize     每页记录数
     * @param count        是否执行count查询
     * @param reasonable   分页合理化开关，null时使用默认配置
     * @param pageSizeZero 当为true且pageSize=0时返回全部结果，null时使用默认配置
     * @param <E>          分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, boolean count, Boolean reasonable,
            Boolean pageSizeZero) {
        Page<E> page = new Page<>(pageNo, pageSize, count);
        page.setReasonable(reasonable);
        page.setPageSizeZero(pageSizeZero);
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 开始分页，基于偏移量和限制数。
     *
     * @param offset 起始偏移量
     * @param limit  每页记录数
     * @param <E>    分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, DEFAULT_COUNT);
    }

    /**
     * 开始分页，基于偏移量、限制数和是否执行count查询。
     *
     * @param offset 起始偏移量
     * @param limit  每页记录数
     * @param count  是否执行count查询
     * @param <E>    分页数据元素类型
     * @return 分页对象
     */
    public static <E> Page<E> offsetPage(int offset, int limit, boolean count) {
        Page<E> page = new Page<>(new int[] { offset, limit }, count);
        Page<E> oldPage = getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        setLocalPage(page);
        return page;
    }

    /**
     * 设置排序字段。
     *
     * @param orderBy 排序字段
     */
    public static void orderBy(String orderBy) {
        Page<?> page = getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
            if (page.getPageSizeZero() != null && page.getPageSizeZero() && page.getPageSize() == 0) {
                page.setOrderByOnly(true);
            }
        } else {
            page = new Page<>();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            setLocalPage(page);
        }
    }

    /**
     * 设置全局静态属性。
     *
     * @param properties 插件配置属性
     */
    protected static void setStaticProperties(Properties properties) {
        if (properties != null) {
            DEFAULT_COUNT = Boolean.parseBoolean(properties.getProperty("defaultCount", "true"));
        }
    }

}