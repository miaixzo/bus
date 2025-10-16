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

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.PageContext;
import org.miaixz.bus.pager.Paging;
import org.miaixz.bus.pager.RowBounds;

/**
 * Configuration class for pagination parameters, responsible for managing and parsing pagination-related parameters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageParams {

    /**
     * Whether to use {@link org.apache.ibatis.session.RowBounds#offset} as the page number. Default is false.
     */
    protected boolean offsetAsPageNo = false;
    /**
     * Whether to perform a count query when using {@link org.apache.ibatis.session.RowBounds}. Default is false.
     */
    protected boolean rowBoundsWithCount = false;
    /**
     * If true, and {@code pageSize} is 0 (or {@link org.apache.ibatis.session.RowBounds#limit} is 0), all results are
     * returned.
     */
    protected boolean pageSizeZero = false;
    /**
     * Whether to enable pagination reasonableness. Default is false. If enabled, page numbers will be adjusted to be
     * within valid ranges.
     */
    protected boolean reasonable = false;
    /**
     * Whether to support passing pagination parameters through method arguments. Default is false.
     */
    protected boolean supportMethodsArguments = false;
    /**
     * The default column name for the count query. Defaults to "0".
     */
    protected String countColumn = "0";
    /**
     * Whether to retain the order by clause during a count query.
     */
    private boolean keepOrderBy = false;
    /**
     * Whether to retain the order by clause of sub-queries during a count query.
     */
    private boolean keepSubSelectOrderBy = false;
    /**
     * Whether to enable asynchronous count queries.
     */
    private boolean asyncCount = false;

    /**
     * Retrieves the {@link Page} object based on the provided query parameters and
     * {@link org.apache.ibatis.session.RowBounds}. It checks for existing {@link Page} in {@link PageContext}, or
     * creates a new one from {@code rowBounds} or {@code parameterObject}.
     *
     * @param parameterObject the query parameter object
     * @param rowBounds       the MyBatis RowBounds object
     * @return a {@link Page} object, or null if no pagination parameters are found
     */
    public Page getPage(Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = PageContext.getLocalPage();
        if (page == null) {
            if (rowBounds != org.apache.ibatis.session.RowBounds.DEFAULT) {
                if (offsetAsPageNo) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(new int[] { rowBounds.getOffset(), rowBounds.getLimit() }, rowBoundsWithCount);
                    page.setReasonable(false); // Disable reasonableness when offsetAsPageNo=false
                }
                if (rowBounds instanceof RowBounds) {
                    RowBounds pageRowBounds = (RowBounds) rowBounds;
                    page.setCount(pageRowBounds.getCount() == null || pageRowBounds.getCount());
                }
            } else if (parameterObject instanceof Paging || supportMethodsArguments) {
                try {
                    page = PageObject.getPageFromObject(parameterObject, false);
                } catch (Exception e) {
                    return null;
                }
            }
            if (page == null) {
                return null;
            }
            PageContext.setLocalPage(page);
        }
        if (page.getReasonable() == null) {
            page.setReasonable(reasonable);
        }
        if (page.getPageSizeZero() == null) {
            page.setPageSizeZero(pageSizeZero);
        }
        if (page.getKeepOrderBy() == null) {
            page.setKeepOrderBy(keepOrderBy);
        }
        if (page.getKeepSubSelectOrderBy() == null) {
            page.setKeepSubSelectOrderBy(keepSubSelectOrderBy);
        }
        return page;
    }

    /**
     * Sets the pagination-related configuration properties. This method is typically called during plugin
     * initialization to configure default behaviors.
     *
     * @param properties the configuration properties
     */
    public void setProperties(Properties properties) {
        this.offsetAsPageNo = Boolean.parseBoolean(properties.getProperty("offsetAsPageNo"));
        this.rowBoundsWithCount = Boolean.parseBoolean(properties.getProperty("rowBoundsWithCount"));
        this.pageSizeZero = Boolean.parseBoolean(properties.getProperty("pageSizeZero"));
        this.reasonable = Boolean.parseBoolean(properties.getProperty("reasonable"));
        this.supportMethodsArguments = Boolean.parseBoolean(properties.getProperty("supportMethodsArguments"));
        String countColumn = properties.getProperty("countColumn");
        if (StringKit.isNotEmpty(countColumn)) {
            this.countColumn = countColumn;
        }
        PageObject.setParams(properties.getProperty("params"));
        this.keepOrderBy = Boolean.parseBoolean(properties.getProperty("keepOrderBy"));
        this.keepSubSelectOrderBy = Boolean.parseBoolean(properties.getProperty("keepSubSelectOrderBy"));
        this.asyncCount = Boolean.parseBoolean(properties.getProperty("asyncCount"));
    }

    /**
     * Checks if {@link org.apache.ibatis.session.RowBounds#offset} is used as the page number.
     *
     * @return true if offset is used as page number, false otherwise
     */
    public boolean isOffsetAsPageNo() {
        return offsetAsPageNo;
    }

    /**
     * Checks if a count query is performed when using {@link org.apache.ibatis.session.RowBounds}.
     *
     * @return true if count query is performed with RowBounds, false otherwise
     */
    public boolean isRowBoundsWithCount() {
        return rowBoundsWithCount;
    }

    /**
     * Checks if all results are returned when {@code pageSize} is 0.
     *
     * @return true if pageSize 0 returns all results, false otherwise
     */
    public boolean isPageSizeZero() {
        return pageSizeZero;
    }

    /**
     * Checks if pagination reasonableness is enabled.
     *
     * @return true if reasonableness is enabled, false otherwise
     */
    public boolean isReasonable() {
        return reasonable;
    }

    /**
     * Checks if passing pagination parameters through method arguments is supported.
     *
     * @return true if method arguments are supported for pagination, false otherwise
     */
    public boolean isSupportMethodsArguments() {
        return supportMethodsArguments;
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
     * Checks if asynchronous count queries are enabled.
     *
     * @return true if asynchronous count queries are enabled, false otherwise
     */
    public boolean isAsyncCount() {
        return asyncCount;
    }

}
