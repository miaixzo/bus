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
package org.miaixz.bus.pager.binding;

import java.util.Properties;

import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.Querying;

/**
 * Provides basic pagination methods for configuring and managing MyBatis paginated queries. This class uses a
 * {@link ThreadLocal} to store and retrieve {@link Page} objects, allowing for easy management of pagination parameters
 * within a thread's execution context.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PageMethod {

    /**
     * Stores the pagination parameters for the current thread.
     */
    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();
    /**
     * Default setting for whether to execute a count query. Defaults to true.
     */
    protected static boolean DEFAULT_COUNT = true;

    /**
     * Retrieves the {@link Page} object associated with the current thread.
     *
     * @param <T> the type of elements in the paginated data
     * @return the current Page object, or null if none is set for the current thread
     */
    public static <T> Page<T> getLocalPage() {
        return LOCAL_PAGE.get();
    }

    /**
     * Sets the {@link Page} object for the current thread.
     *
     * @param page the Page object to set
     */
    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * Clears the {@link Page} object from the current thread's context.
     */
    public static void clearPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * Executes a query to get the total count of records without actual pagination.
     *
     * @param select the {@link Querying} object representing the query to be executed
     * @return the total number of records
     */
    public static long count(Querying select) {
        Page<?> page = startPage(1, -1, true).disableAsyncCount();
        select.doSelect();
        return page.getTotal();
    }

    /**
     * Starts pagination based on the properties of a given parameter object. The parameter object is analyzed to
     * extract page number, page size, and order by information.
     *
     * @param params the parameter object containing pagination details
     * @param <E>    the type of elements in the paginated data
     * @return a new {@link Page} object configured with the extracted parameters
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
     * Starts pagination with a specified page number and page size. A count query will be executed by default.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the number of records per page
     * @param <E>      the type of elements in the paginated data
     * @return a new {@link Page} object configured for pagination
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize) {
        return startPage(pageNo, pageSize, DEFAULT_COUNT);
    }

    /**
     * Starts pagination with a specified page number, page size, and whether to execute a count query.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the number of records per page
     * @param count    true to execute a count query, false otherwise
     * @param <E>      the type of elements in the paginated data
     * @return a new {@link Page} object configured for pagination
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, boolean count) {
        return startPage(pageNo, pageSize, count, null, null);
    }

    /**
     * Starts pagination with a specified page number, page size, and order by clause.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the number of records per page
     * @param orderBy  the order by clause for sorting
     * @param <E>      the type of elements in the paginated data
     * @return a new {@link Page} object configured for pagination and sorting
     */
    public static <E> Page<E> startPage(int pageNo, int pageSize, String orderBy) {
        Page<E> page = startPage(pageNo, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * Starts pagination with comprehensive control over page number, page size, count query execution, pagination
     * reasonableness, and zero page size handling.
     *
     * @param pageNo       the page number (starts from 1)
     * @param pageSize     the number of records per page
     * @param count        true to execute a count query, false otherwise
     * @param reasonable   the reasonableness switch for pagination; if null, uses default configuration
     * @param pageSizeZero if true and pageSize is 0, all results are returned; if null, uses default configuration
     * @param <E>          the type of elements in the paginated data
     * @return a new {@link Page} object configured for pagination
     */
    public static <E> Page<E> startPage(
            int pageNo,
            int pageSize,
            boolean count,
            Boolean reasonable,
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
     * Starts pagination based on an offset and limit, similar to {@link org.apache.ibatis.session.RowBounds}. A count
     * query will be executed by default.
     *
     * @param offset the starting offset
     * @param limit  the maximum number of records to return
     * @param <E>    the type of elements in the paginated data
     * @return a new {@link Page} object configured for pagination
     */
    public static <E> Page<E> offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, DEFAULT_COUNT);
    }

    /**
     * Starts pagination based on an offset, limit, and whether to execute a count query.
     *
     * @param offset the starting offset
     * @param limit  the maximum number of records to return
     * @param count  true to execute a count query, false otherwise
     * @param <E>    the type of elements in the paginated data
     * @return a new {@link Page} object configured for pagination
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
     * Sets the order by clause for the current pagination context. If a {@link Page} object is already present in the
     * {@link ThreadLocal}, its order by property is updated. Otherwise, a new {@link Page} object is created with only
     * the order by clause set.
     *
     * @param orderBy the order by clause for sorting
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
     * Sets global static properties for the pagination plugin. This method is typically called during plugin
     * initialization to configure default behaviors.
     *
     * @param properties the plugin configuration properties
     */
    protected static void setStaticProperties(Properties properties) {
        if (properties != null) {
            DEFAULT_COUNT = Boolean.parseBoolean(properties.getProperty("defaultCount", "true"));
        }
    }

}
