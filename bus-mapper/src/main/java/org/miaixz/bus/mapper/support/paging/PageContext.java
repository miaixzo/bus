/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.mapper.support.paging;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.mapper.Order;

/**
 * Thread-local holder for pagination information.
 *
 * <p>
 * This class provides a thread-safe way to store and retrieve pagination information for the current thread. It's
 * typically used in conjunction with pagination interceptors to pass pagination parameters through method calls.
 * </p>
 *
 * <p>
 * Usage pattern:
 * </p>
 *
 * <pre>{@code
 *
 * // Set pagination
 * PageContext.of(1, 10);
 * try {
 *     List<User> users = userMapper.selectAll();
 * // users will be automatically paginated
 * } finally {
 *     PageContext.clearPage();
 * }
 *
 * // Or use with auto-clear
 * List<User> users = PageContext.of(1, 10).doSelectPage(() -> userMapper.selectAll());
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class PageContext {

    /**
     * ThreadLocal variable to hold the specific pagination parameters (page number, size, sort) for the current thread.
     */
    private static final ThreadLocal<Pageable> LOCAL_PAGE = new ThreadLocal<>();

    /**
     * ThreadLocal flag indicating whether a total count query should be performed. Defaults to true if not explicitly
     * set.
     */
    private static final ThreadLocal<Boolean> LOCAL_COUNT = ThreadLocal.withInitial(() -> true);

    /**
     * ThreadLocal storage for a custom SQL string used for counting. If set, this SQL will be used instead of the
     * auto-generated count query.
     */
    private static final ThreadLocal<String> LOCAL_COUNT_SQL = new ThreadLocal<>();

    /**
     * ThreadLocal flag used internally to signal if the dialect execution logic specifically requires a count check to
     * be performed.
     */
    private static final ThreadLocal<Boolean> LOCAL_COUNT_REQUIRED = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation. This class is a utility class designed to be used via static
     * methods.
     */
    private PageContext() {

    }

    /**
     * Starts a new page for the current thread with default count behavior (true).
     *
     * @param pageNo   the page number (1-based)
     * @param pageSize the page size
     * @return PageContext instance for chaining method calls (fluent API)
     */
    public static PageContext of(int pageNo, int pageSize) {
        return of(pageNo, pageSize, true);
    }

    /**
     * Starts a new page for the current thread with optional count query control.
     *
     * @param pageNo   the page number (1-based)
     * @param pageSize the page size
     * @param count    {@code true} to execute count query, {@code false} otherwise
     * @return PageContext instance for chaining method calls (fluent API)
     */
    public static PageContext of(int pageNo, int pageSize, boolean count) {
        return of(pageNo, pageSize, count, Sort.unsorted());
    }

    /**
     * Starts a new page for the current thread with optional count query control.
     *
     * @param pageNo   the page number (1-based)
     * @param pageSize the page size
     * @param sort     the sorting information to apply
     * @return PageContext instance for chaining method calls (fluent API)
     */
    public static PageContext of(int pageNo, int pageSize, Sort sort) {
        return of(pageNo, pageSize, true, sort);
    }

    /**
     * Starts a new page for the current thread with sorting specification.
     *
     * @param pageNo   the page number (1-based)
     * @param pageSize the page size
     * @param count    {@code true} to execute count query, {@code false} otherwise
     * @param sort     the sorting information to apply
     * @return PageContext instance for chaining method calls (fluent API)
     */
    public static PageContext of(int pageNo, int pageSize, boolean count, Sort sort) {
        Pageable pageable = Pageable.of(pageNo, pageSize, sort);
        LOCAL_PAGE.set(pageable);
        LOCAL_COUNT.set(count);
        return new PageContext();
    }

    /**
     * Gets the {@link Pageable} object bound to the current thread.
     *
     * @return the current pageable configuration, or {@code null} if pagination is not started
     */
    public static Pageable getLocalPage() {
        return LOCAL_PAGE.get();
    }

    /**
     * Gets whether to perform a count query for the current thread.
     *
     * @return {@code true} if count should be performed, {@code false} otherwise
     */
    public static boolean getLocalCount() {
        Boolean count = LOCAL_COUNT.get();
        return count != null && count;
    }

    /**
     * Sets the {@link Pageable} object for the current thread.
     *
     * @param pageable the pageable to set
     */
    public static void setLocalPage(Pageable pageable) {
        LOCAL_PAGE.set(pageable);
    }

    /**
     * Sets whether to perform a count query for the current thread.
     *
     * @param count {@code true} to perform count query, {@code false} otherwise
     */
    public static void setLocalCount(boolean count) {
        LOCAL_COUNT.set(count);
    }

    /**
     * Clears all pagination information for the current thread.
     * <p>
     * This method must be called after the database operation is complete (usually in a finally block) to prevent
     * memory leaks and thread pollution in thread-pooled environments.
     * </p>
     */
    public static void clearPage() {
        LOCAL_PAGE.remove();
        LOCAL_COUNT.remove();
        LOCAL_COUNT_SQL.remove();
        LOCAL_COUNT_REQUIRED.remove();
    }

    /**
     * Checks if pagination is currently active for the current thread.
     *
     * @return {@code true} if pagination parameters are set, {@code false} otherwise
     */
    public static boolean isPaginationActive() {
        return LOCAL_PAGE.get() != null;
    }

    /**
     * Gets the current page number (1-based).
     *
     * @return the page number, or 1 if no pagination is active
     */
    public static int getCurrentPageNo() {
        Pageable pageable = LOCAL_PAGE.get();
        return pageable != null ? pageable.getPageNo() : 1;
    }

    /**
     * Gets the current page size.
     *
     * @return the page size, or 0 if no pagination is active
     */
    public static int getCurrentPageSize() {
        Pageable pageable = LOCAL_PAGE.get();
        return pageable != null ? pageable.getPageSize() : 0;
    }

    /**
     * Gets the custom count SQL for the current thread.
     *
     * @return the count SQL string, or {@code null} if not set
     */
    public static String getCountQuery() {
        return LOCAL_COUNT_SQL.get();
    }

    /**
     * Sets a custom count SQL for the current thread.
     *
     * @param countSql the raw SQL to use for counting total records
     */
    public static void setCountQuery(String countSql) {
        LOCAL_COUNT_SQL.set(countSql);
    }

    /**
     * Checks if a count query is internally marked as required for the current thread.
     *
     * @return {@code true} if count query is required
     */
    public static boolean isCountRequired() {
        Boolean required = LOCAL_COUNT_REQUIRED.get();
        return required != null && required;
    }

    /**
     * Sets whether a count query is required for the current thread.
     * <p>
     * This is typically used by internal dialect resolution mechanisms.
     * </p>
     *
     * @param countRequired {@code true} if count query is required
     */
    public static void setCountRequired(boolean countRequired) {
        LOCAL_COUNT_REQUIRED.set(countRequired);
    }

    /**
     * Sets the Order BY clause for the current thread by parsing a string.
     * <p>
     * If pagination is already active, this updates the existing Pageable with the new sort order. The format expected
     * is "column1 ASC, column2 DESC".
     * </p>
     *
     * @param orderBy the Order BY clause string
     */
    public static void orderBy(String orderBy) {
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            // Parse the orderBy string into a Sort object
            Sort sort = parseOrderBy(orderBy);
            Pageable current = LOCAL_PAGE.get();
            if (current != null) {
                Pageable newPageable = Pageable.of(current.getPageNo(), current.getPageSize(), sort);
                LOCAL_PAGE.set(newPageable);
            }
        }
    }

    /**
     * Parses an Order BY clause string into a {@link Sort} object.
     * <p>
     * Handles comma-separated fields and optional direction (ASC/DESC). Quotes (` ' ") around field names are stripped.
     * </p>
     *
     * @param orderBy the Order BY clause
     * @return the {@link Sort} object representing the order, or {@link Sort#unsorted()} if input is empty
     */
    private static Sort parseOrderBy(String orderBy) {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return Sort.unsorted();
        }

        // Simple parsing - split by commas and process each part
        String[] parts = orderBy.split("\\s*,\\s*");
        List<Order> orders = new ArrayList<>();

        for (String part : parts) {
            String[] orderParts = part.trim().split("\\s+");
            if (orderParts.length >= 1) {
                String property = orderParts[0].replace("\"", "").replace("'", "").replace("`", "");

                Order order;
                if (orderParts.length >= 2 && "DESC".equalsIgnoreCase(orderParts[1])) {
                    order = Order.descending(property);
                } else {
                    order = Order.ascending(property);
                }
                orders.add(order);
            }
        }

        return Sort.by(orders);
    }

    /**
     * Executes a select operation with automatic page clearing.
     * <p>
     * This method ensures {@link #clearPage()} is called regardless of success or failure.
     * </p>
     *
     * @param <T>      the type of the result object
     * @param selector the functional interface identifying the select operation
     * @return the result of the select operation
     */
    public <T> T doSelect(PageSelector<T> selector) {
        try {
            return selector.select();
        } finally {
            clearPage();
        }
    }

    /**
     * Executes a select page operation with automatic page clearing.
     * <p>
     * This method ensures {@link #clearPage()} is called regardless of success or failure. If the result is not already
     * a {@link Page}, it wraps the list in a new Page object using the current thread's pageable info.
     * </p>
     *
     * @param <E>      the type of elements in the list
     * @param selector the functional interface identifying the select operation that returns a list
     * @return a {@link Page} containing the results and pagination metadata
     */
    public <E> Page<E> doSelectPage(PageSelector<List<E>> selector) {
        try {
            List<E> list = selector.select();
            if (list instanceof Page) {
                return (Page<E>) list;
            }
            // If the interceptor didn't convert to Page, wrap it
            return Page.<E>builder().result(list).pageable(getLocalPage()).total(list.size()).build();
        } finally {
            clearPage();
        }
    }

    /**
     * Functional interface for select operations.
     *
     * @param <T> the type of result expected from the selection
     */
    @FunctionalInterface
    public interface PageSelector<T> {

        /**
         * Executes the select operation.
         *
         * @return the result
         */
        T select();
    }

}
