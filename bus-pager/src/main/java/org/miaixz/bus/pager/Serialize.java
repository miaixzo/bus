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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Paging information class used to store the result set and total number of records for a paginated query. This class
 * supports serialization.
 *
 * @param <T> the type of elements in the paginated data
 * @author Kimi Liu
 * @since Java 17+
 */
public class Serialize<T> implements Serializable {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852290178259L;

    /**
     * The total number of records.
     */
    protected long total;
    /**
     * The paginated result set.
     */
    protected List<T> list;

    /**
     * Default constructor for Serialize.
     */
    public Serialize() {

    }

    /**
     * Constructs a Serialize object based on a given list of results. If the list is an instance of {@link Page}, the
     * total will be retrieved from it; otherwise, the total will be the size of the list.
     *
     * @param list the list of paginated results
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
     * Static factory method to create a Serialize object.
     *
     * @param list the list of paginated results
     * @param <T>  the type of elements in the paginated data
     * @return a new Serialize object
     */
    public static <T> Serialize<T> of(List<? extends T> list) {
        return new Serialize<>(list);
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
     * Sets the total number of records.
     *
     * @param total the total number of records to set
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * Retrieves the paginated result set.
     *
     * @return the list of paginated results
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Sets the paginated result set.
     *
     * @param list the list of paginated results to set
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * Returns a string representation of the Serialize object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Serialize{" + "total=" + total + ", list=" + list + '}';
    }

}
