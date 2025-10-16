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

/**
 * Base class for pagination parameters. Extend this class to directly control pagination parameters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageParam implements Paging {

    /**
     * The current page number.
     */
    private Integer pageNo;
    /**
     * The number of records per page.
     */
    private Integer pageSize;
    /**
     * The order by clause for sorting.
     */
    private String orderBy;

    /**
     * Default constructor for PageParam.
     */
    public PageParam() {
    }

    /**
     * Constructs a PageParam with a specified page number and page size.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the number of records per page
     */
    public PageParam(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    /**
     * Constructs a PageParam with a specified page number, page size, and order by clause.
     *
     * @param pageNo   the page number (starts from 1)
     * @param pageSize the number of records per page
     * @param orderBy  the order by clause for sorting
     */
    public PageParam(Integer pageNo, Integer pageSize, String orderBy) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.orderBy = orderBy;
    }

    /**
     * Retrieves the current page number.
     *
     * @return the page number
     */
    @Override
    public Integer getPageNo() {
        return pageNo;
    }

    /**
     * Sets the current page number.
     *
     * @param pageNo the page number to set
     */
    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * Retrieves the number of records per page.
     *
     * @return the page size
     */
    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the number of records per page.
     *
     * @param pageSize the page size to set
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Retrieves the order by clause.
     *
     * @return the order by clause
     */
    @Override
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order by clause.
     *
     * @param orderBy the order by clause to set
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

}
