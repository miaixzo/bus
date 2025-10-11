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
 * Extends MyBatis's RowBounds to add pagination result information, such as total records and whether to perform a
 * count query.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RowBounds extends org.apache.ibatis.session.RowBounds {

    /**
     * The total number of records.
     */
    private Long total;
    /**
     * Flag indicating whether a count query should be executed.
     */
    private Boolean count;

    /**
     * Constructs a RowBounds object with a specified offset and limit.
     *
     * @param offset the starting offset
     * @param limit  the number of records per page
     */
    public RowBounds(int offset, int limit) {
        super(offset, limit);
    }

    /**
     * Retrieves the total number of records.
     *
     * @return the total number of records
     */
    public Long getTotal() {
        return total;
    }

    /**
     * Sets the total number of records.
     *
     * @param total the total number of records to set
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * Retrieves whether a count query should be executed.
     *
     * @return true if a count query should be executed, false otherwise
     */
    public Boolean getCount() {
        return count;
    }

    /**
     * Sets whether a count query should be executed.
     *
     * @param count true to execute a count query, false otherwise
     */
    public void setCount(Boolean count) {
        this.count = count;
    }

}
