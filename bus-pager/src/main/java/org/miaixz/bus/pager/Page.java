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
package org.miaixz.bus.pager;

import java.io.Closeable;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.pager.binding.PageAutoDialect;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;

/**
 * MyBatis paging object, supporting paginated queries and result set management. This class extends {@link ArrayList}
 * to hold the paginated data and implements {@link Closeable} to manage the paging context.
 *
 * @param <E> the type of elements in this page
 * @author Kimi Liu
 * @since Java 17+
 */
public class Page<E> extends ArrayList<E> implements Closeable {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852289758798L;

    /**
     * Records the current stack trace, allowing the location where the Page object was created to be found (requires
     * page.debug to be enabled).
     */
    private final String stackTrace = Builder.current();
    /**
     * The current page number, starting from 1.
     */
    private int pageNo;
    /**
     * The size of each page.
     */
    private int pageSize;
    /**
     * The starting row number for the current page.
     */
    private long startRow;
    /**
     * The ending row number for the current page.
     */
    private long endRow;
    /**
     * The total number of records across all pages.
     */
    private long total;
    /**
     * The total number of pages.
     */
    private int pages;
    /**
     * Flag indicating whether a count query should be executed.
     */
    private boolean count = true;
    /**
     * Flag for pagination reasonableness. If true, page numbers are adjusted to be within valid range.
     */
    private Boolean reasonable;
    /**
     * If true, and pageSize is 0 (or RowBounds limit is 0), no pagination is performed, and all results are returned.
     */
    private Boolean pageSizeZero;
    /**
     * The column name used for the count query.
     */
    private String countColumn;
    /**
     * The order by clause for sorting.
     */
    private String orderBy;
    /**
     * Flag indicating whether only ordering should be applied without pagination.
     */
    private boolean orderByOnly;
    /**
     * SQL interception handler for BoundSql.
     */
    private BoundSqlBuilder boundSqlHandler;
    /**
     * The chain of BoundSql handlers.
     */
    private transient BoundSqlBuilder.Chain chain;
    /**
     * The dialect class for pagination, can use aliases registered in {@link PageAutoDialect} like "mysql", "oracle".
     */
    private String dialectClass;
    /**
     * Flag indicating whether to retain the order by clause in the count query.
     */
    private Boolean keepOrderBy;
    /**
     * Flag indicating whether to retain the order by clause of sub-queries in the count query.
     */
    private Boolean keepSubSelectOrderBy;
    /**
     * Flag indicating whether to enable asynchronous count queries.
     */
    private Boolean asyncCount;

    /**
     * Default constructor.
     */
    public Page() {
        super();
    }

    /**
     * Constructs a Page object with specified page number and page size.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the size of the page
     */
    public Page(int pageNo, int pageSize) {
        this(pageNo, pageSize, true, null);
    }

    /**
     * Constructs a Page object with specified page number, page size, and whether to perform a count query.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the size of the page
     * @param count    true to perform a count query, false otherwise
     */
    public Page(int pageNo, int pageSize, boolean count) {
        this(pageNo, pageSize, count, null);
    }

    /**
     * Constructs a Page object with specified page number, page size, whether to perform a count query, and the
     * reasonableness switch.
     *
     * @param pageNo     the page number (starts from 1)
     * @param pageSize   the size of the page
     * @param count      true to perform a count query, false otherwise
     * @param reasonable the reasonableness switch for pagination
     */
    private Page(int pageNo, int pageSize, boolean count, Boolean reasonable) {
        super(0);
        if (pageNo == 1 && pageSize == Integer.MAX_VALUE) {
            pageSizeZero = true;
            pageSize = 0;
        }
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.count = count;
        calculateStartAndEndRow();
        setReasonable(reasonable);
    }

    /**
     * Constructs a Page object based on row bounds for pagination.
     *
     * @param rowBounds an array containing row bounds, where index 0 is offset and index 1 is limit
     * @param count     true to perform a count query, false otherwise
     */
    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
            this.pageNo = 1;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNo = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1]))
                    : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }

    /**
     * Retrieves the stack trace where this Page object was created.
     *
     * @return the stack trace information
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * Retrieves the paginated result set.
     *
     * @return a list of paginated data
     */
    public List<E> getResult() {
        return this;
    }

    /**
     * Retrieves the total number of pages.
     *
     * @return the total number of pages
     */
    public int getPages() {
        return pages;
    }

    /**
     * Sets the total number of pages.
     *
     * @param pages the total number of pages
     * @return the current Page object
     */
    public Page<E> setPages(int pages) {
        this.pages = pages;
        return this;
    }

    /**
     * Retrieves the ending row position.
     *
     * @return the ending row position
     */
    public long getEndRow() {
        return endRow;
    }

    /**
     * Sets the ending row position.
     *
     * @param endRow the ending row position
     * @return the current Page object
     */
    public Page<E> setEndRow(long endRow) {
        this.endRow = endRow;
        return this;
    }

    /**
     * Retrieves the current page number.
     *
     * @return the current page number
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * Sets the page number, with support for reasonableness handling. If reasonableness is enabled and pageNo is less
     * than or equal to 0, it will be set to 1.
     *
     * @param pageNo the page number
     * @return the current Page object
     */
    public Page<E> setPageNo(int pageNo) {
        this.pageNo = ((reasonable != null && reasonable) && pageNo <= 0) ? 1 : pageNo;
        return this;
    }

    /**
     * Retrieves the page size.
     *
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize the page size
     * @return the current Page object
     */
    public Page<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * Retrieves the starting row position.
     *
     * @return the starting row position
     */
    public long getStartRow() {
        return startRow;
    }

    /**
     * Sets the starting row position.
     *
     * @param startRow the starting row position
     * @return the current Page object
     */
    public Page<E> setStartRow(long startRow) {
        this.startRow = startRow;
        return this;
    }

    /**
     * Retrieves the total number of records.
     *
     * @return the total number of records
     */
    public long getTotal() {
        return total;
    }

    /**
     * Sets the total number of records and calculates the total number of pages.
     *
     * @param total the total number of records
     */
    public void setTotal(long total) {
        this.total = total;
        if (total == -1) {
            pages = 1;
            return;
        }
        if (pageSize > 0) {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        } else {
            pages = 0;
        }
        if ((reasonable != null && reasonable) && pageNo > pages) {
            if (pages != 0) {
                pageNo = pages;
            }
            calculateStartAndEndRow();
        }
    }

    /**
     * Retrieves the state of the pagination reasonableness switch.
     *
     * @return the state of the reasonableness switch
     */
    public Boolean getReasonable() {
        return reasonable;
    }

    /**
     * Sets the pagination reasonableness switch. If reasonableness is enabled and the current page number is less than
     * or equal to 0, it will be set to 1.
     *
     * @param reasonable the reasonableness switch
     * @return the current Page object
     */
    public Page<E> setReasonable(Boolean reasonable) {
        if (reasonable == null) {
            return this;
        }
        this.reasonable = reasonable;
        if (this.reasonable && this.pageNo <= 0) {
            this.pageNo = 1;
            calculateStartAndEndRow();
        }
        return this;
    }

    /**
     * Retrieves the state of the pageSizeZero switch.
     *
     * @return the state of the pageSizeZero switch
     */
    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    /**
     * Sets the pageSizeZero switch. If true, and pageSize is 0, all results are returned without pagination.
     *
     * @param pageSizeZero true if pageSize 0 should return all results, false otherwise
     * @return the current Page object
     */
    public Page<E> setPageSizeZero(Boolean pageSizeZero) {
        if (this.pageSizeZero == null && pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }

    /**
     * Retrieves the order by clause.
     *
     * @return the order by clause
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order by clause.
     *
     * @param orderBy the order by clause
     * @param <E>     the type of elements in this page
     * @return the current Page object
     */
    public <E> Page<E> setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    /**
     * Sets the order by clause unsafely. This method does not perform SQL injection checks, so ensure the
     * {@code orderBy} parameter is safe.
     *
     * @param orderBy the order by clause
     * @param <E>     the type of elements in this page
     * @return the current Page object
     */
    public <E> Page<E> setUnsafeOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    /**
     * Checks if only ordering should be applied without pagination.
     *
     * @return true if only ordering should be applied, false otherwise
     */
    public boolean isOrderByOnly() {
        return orderByOnly;
    }

    /**
     * Sets whether only ordering should be applied without pagination.
     *
     * @param orderByOnly true if only ordering should be applied, false otherwise
     */
    public void setOrderByOnly(boolean orderByOnly) {
        this.orderByOnly = orderByOnly;
    }

    /**
     * Retrieves the dialect class used for pagination.
     *
     * @return the dialect class name
     */
    public String getDialectClass() {
        return dialectClass;
    }

    /**
     * Sets the dialect class used for pagination.
     *
     * @param dialectClass the dialect class name
     */
    public void setDialectClass(String dialectClass) {
        this.dialectClass = dialectClass;
    }

    /**
     * Retrieves whether to retain the order by clause in the count query.
     *
     * @return true if the order by clause should be retained, false otherwise
     */
    public Boolean getKeepOrderBy() {
        return keepOrderBy;
    }

    /**
     * Sets whether to retain the order by clause in the count query.
     *
     * @param keepOrderBy true if the order by clause should be retained, false otherwise
     * @return the current Page object
     */
    public Page<E> setKeepOrderBy(Boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    /**
     * Retrieves whether to retain the order by clause of sub-queries in the count query.
     *
     * @return true if the order by clause of sub-queries should be retained, false otherwise
     */
    public Boolean getKeepSubSelectOrderBy() {
        return keepSubSelectOrderBy;
    }

    /**
     * Sets whether to retain the order by clause of sub-queries in the count query.
     *
     * @param keepSubSelectOrderBy true if the order by clause of sub-queries should be retained, false otherwise
     */
    public void setKeepSubSelectOrderBy(Boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
    }

    /**
     * Retrieves whether asynchronous count queries are enabled.
     *
     * @return true if asynchronous count queries are enabled, false otherwise
     */
    public Boolean getAsyncCount() {
        return asyncCount;
    }

    /**
     * Sets whether asynchronous count queries are enabled.
     *
     * @param asyncCount true to enable asynchronous count queries, false otherwise
     */
    public void setAsyncCount(Boolean asyncCount) {
        this.asyncCount = asyncCount;
    }

    /**
     * Specifies the pagination implementation to use.
     *
     * @param dialect the dialect class name, can use aliases registered in {@link PageAutoDialect} like "mysql",
     *                "oracle"
     * @return the current Page object
     */
    public Page<E> using(String dialect) {
        this.dialectClass = dialect;
        return this;
    }

    /**
     * Calculates the starting and ending row numbers for the current page.
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNo > 0 ? (this.pageNo - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNo > 0 ? 1 : 0);
    }

    /**
     * Checks if a count query should be executed.
     *
     * @return true if a count query should be executed, false otherwise
     */
    public boolean isCount() {
        return this.count;
    }

    /**
     * Sets whether a count query should be executed.
     *
     * @param count true to execute a count query, false otherwise
     * @return the current Page object
     */
    public Page<E> setCount(boolean count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the page number. If reasonableness is enabled and pageNo is less than or equal to 0, it will be set to 1.
     *
     * @param pageNo the page number
     * @return the current Page object
     */
    public Page<E> pageNo(int pageNo) {
        this.pageNo = ((reasonable != null && reasonable) && pageNo <= 0) ? 1 : pageNo;
        return this;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize the page size
     * @return the current Page object
     */
    public Page<E> pageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
        return this;
    }

    /**
     * Sets whether a count query should be executed.
     *
     * @param count true to execute a count query, false otherwise
     * @return the current Page object
     */
    public Page<E> count(Boolean count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the pagination reasonableness switch.
     *
     * @param reasonable the pagination reasonableness switch
     * @return the current Page object
     */
    public Page<E> reasonable(Boolean reasonable) {
        setReasonable(reasonable);
        return this;
    }

    /**
     * Sets the pageSizeZero switch.
     *
     * @param pageSizeZero true if pageSize 0 should return all results, false otherwise
     * @return the current Page object
     */
    public Page<E> pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    /**
     * Sets the BoundSql interceptor.
     *
     * @param boundSqlHandler the BoundSql interceptor
     * @return the current Page object
     */
    public Page<E> boundSqlInterceptor(BoundSqlBuilder boundSqlHandler) {
        setBoundSqlInterceptor(boundSqlHandler);
        return this;
    }

    /**
     * Specifies the column name for the count query.
     *
     * @param columnName the column name
     * @return the current Page object
     */
    public Page<E> countColumn(String columnName) {
        setCountColumn(columnName);
        return this;
    }

    /**
     * Sets whether to retain the order by clause in the count query.
     *
     * @param keepOrderBy true if the order by clause should be retained, false otherwise
     * @return the current Page object
     */
    public Page<E> keepOrderBy(boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    /**
     * Checks if the order by clause should be retained in the count query.
     *
     * @return true if the order by clause should be retained, false otherwise
     */
    public boolean keepOrderBy() {
        return this.keepOrderBy != null && this.keepOrderBy;
    }

    /**
     * Sets whether to retain the order by clause of sub-queries in the count query.
     *
     * @param keepSubSelectOrderBy true if the order by clause of sub-queries should be retained, false otherwise
     * @return the current Page object
     */
    public Page<E> keepSubSelectOrderBy(boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
        return this;
    }

    /**
     * Checks if the order by clause of sub-queries should be retained in the count query.
     *
     * @return true if the order by clause of sub-queries should be retained, false otherwise
     */
    public boolean keepSubSelectOrderBy() {
        return this.keepSubSelectOrderBy != null && this.keepSubSelectOrderBy;
    }

    /**
     * Sets whether asynchronous count queries are enabled.
     *
     * @param asyncCount true to enable asynchronous count queries, false otherwise
     * @return the current Page object
     */
    public Page<E> asyncCount(boolean asyncCount) {
        this.asyncCount = asyncCount;
        return this;
    }

    /**
     * Enables asynchronous count queries.
     *
     * @return the current Page object
     */
    public Page<E> enableAsyncCount() {
        return asyncCount(true);
    }

    /**
     * Disables asynchronous count queries.
     *
     * @return the current Page object
     */
    public Page<E> disableAsyncCount() {
        return asyncCount(false);
    }

    /**
     * Checks if asynchronous count queries are enabled.
     *
     * @return true if asynchronous count queries are enabled, false otherwise
     */
    public boolean asyncCount() {
        return this.asyncCount != null && this.asyncCount;
    }

    /**
     * Converts the current Page object to a {@link Paginating} object.
     *
     * @return a {@link Paginating} object
     */
    public Paginating<E> toPageInfo() {
        return new Paginating<>(this);
    }

    /**
     * Converts the paginated data and returns a {@link Paginating} object.
     *
     * @param function the data conversion function
     * @param <T>      the type of the converted data elements
     * @return a {@link Paginating} object with converted data
     */
    public <T> Paginating<T> toPageInfo(FunctionX<E, T> function) {
        List<T> list = new ArrayList<>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        Paginating<T> paginating = new Paginating<>(list);
        paginating.setTotal(this.getTotal());
        paginating.setPageNo(this.getPageNo());
        paginating.setPageSize(this.getPageSize());
        paginating.setPages(this.getPages());
        paginating.setStartRow(this.getStartRow());
        paginating.setEndRow(this.getEndRow());
        paginating.calcByNavigatePages(Paginating.DEFAULT_NAVIGATE_PAGES);
        return paginating;
    }

    /**
     * Converts the current Page object to a {@link Serialize} object.
     *
     * @return a {@link Serialize} object
     */
    public Serialize<E> toPageSerializable() {
        return new Serialize<>(this);
    }

    /**
     * Converts the paginated data and returns a {@link Serialize} object.
     *
     * @param function the data conversion function
     * @param <T>      the type of the converted data elements
     * @return a {@link Serialize} object with converted data
     */
    public <T> Serialize<T> toPageSerializable(FunctionX<E, T> function) {
        List<T> list = new ArrayList<>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        Serialize<T> serialize = new Serialize<>(list);
        serialize.setTotal(this.getTotal());
        return serialize;
    }

    /**
     * Executes a paginated query.
     *
     * @param select the query object
     * @param <E>    the type of elements in this page
     * @return the current Page object
     */
    public <E> Page<E> doSelectPage(Querying select) {
        select.doSelect();
        return (Page<E>) this;
    }

    /**
     * Executes a paginated query and returns a {@link Paginating} object.
     *
     * @param select the query object
     * @param <E>    the type of elements in this page
     * @return a {@link Paginating} object
     */
    public <E> Paginating<E> doSelectPageInfo(Querying select) {
        select.doSelect();
        return (Paginating<E>) this.toPageInfo();
    }

    /**
     * Executes a paginated query and returns a {@link Serialize} object.
     *
     * @param select the query object
     * @param <E>    the type of elements in this page
     * @return a {@link Serialize} object
     */
    public <E> Serialize<E> doSelectPageSerializable(Querying select) {
        select.doSelect();
        return (Serialize<E>) this.toPageSerializable();
    }

    /**
     * Executes a count query.
     *
     * @param select the query object
     * @return the total number of records
     */
    public long doCount(Querying select) {
        this.pageSizeZero = true;
        this.pageSize = 0;
        select.doSelect();
        return this.total;
    }

    /**
     * Retrieves the column name used for the count query.
     *
     * @return the count query column name
     */
    public String getCountColumn() {
        return countColumn;
    }

    /**
     * Sets the column name for the count query, including SQL injection validation.
     *
     * @param countColumn the column name
     */
    public void setCountColumn(String countColumn) {
        this.countColumn = countColumn;
    }

    /**
     * Retrieves the BoundSql interceptor.
     *
     * @return the BoundSql interceptor
     */
    public BoundSqlBuilder getBoundSqlInterceptor() {
        return boundSqlHandler;
    }

    /**
     * Sets the BoundSql interceptor.
     *
     * @param boundSqlHandler the BoundSql interceptor
     */
    public void setBoundSqlInterceptor(BoundSqlBuilder boundSqlHandler) {
        this.boundSqlHandler = boundSqlHandler;
    }

    /**
     * Retrieves the BoundSql processing chain.
     *
     * @return the BoundSql processing chain
     */
    BoundSqlBuilder.Chain getChain() {
        return chain;
    }

    /**
     * Sets the BoundSql processing chain.
     *
     * @param chain the BoundSql processing chain
     */
    void setChain(BoundSqlBuilder.Chain chain) {
        this.chain = chain;
    }

    /**
     * Returns a string representation of the Page object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Page{" + "count=" + count + ", pageNo=" + pageNo + ", pageSize=" + pageSize + ", startRow=" + startRow
                + ", endRow=" + endRow + ", total=" + total + ", pages=" + pages + ", reasonable=" + reasonable
                + ", pageSizeZero=" + pageSizeZero + '}' + super.toString();
    }

    /**
     * Closes the Page object and clears the paging context. This method is part of the {@link Closeable} interface
     * implementation.
     */
    @Override
    public void close() {
        PageContext.clearPage();
    }

}
