/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.support.paging;

import java.io.Serial;
import java.io.Serializable;

/**
 * Pageable interface for pagination request information.
 *
 * <p>
 * This interface represents pagination request parameters including page number, page size, sorting information, and
 * whether pagination is enabled.
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Page number and size configuration</li>
 * <li>Sorting support</li>
 * <li>Offset and limit calculations</li>
 * <li>Previous and next page navigation</li>
 * <li>Unpaged support for queries without pagination</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 *
 * // Create a pageable request
 * Pageable pageable = PageRequest.of(1, 10, Sort.by("name").ascending());
 *
 * // Access pagination info
 * int pageNo = pageable.getPageNo(); // 1
 * int pageSize = pageable.getPageSize(); // 10
 * long offset = pageable.getOffset(); // 0
 *
 * // Check if unpaged
 * boolean unpaged = pageable.isUnpaged(); // false
 *
 * // Get next/previous pageable
 * Pageable next = pageable.next(); // page 2, size 10
 * Pageable previous = pageable.previous(); // throws exception (no previous)
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Pageable extends Serializable {

    /**
     * Constant for unpaged page size.
     */
    int UNPAGED_SIZE = Integer.MAX_VALUE;

    /**
     * Creates an unpaged pageable instance.
     *
     * @return an unpaged pageable
     */
    static Pageable unpaged() {
        return PageRequest.unpaged();
    }

    /**
     * Creates a pageable request with the specified page number and size.
     *
     * @param pageNo   the page number (1-based)
     * @param pageSize the page size
     * @return a pageable request
     */
    static Pageable of(int pageNo, int pageSize) {
        return PageRequest.of(pageNo, pageSize);
    }

    /**
     * Creates a pageable request with the specified page number, size, and sorting.
     *
     * @param pageNo   the page number (1-based)
     * @param pageSize the page size
     * @param sort     the sorting information
     * @return a pageable request
     */
    static Pageable of(int pageNo, int pageSize, Sort sort) {
        return PageRequest.of(pageNo, pageSize, sort);
    }

    /**
     * Gets the page number (1-based).
     *
     * @return the page number
     */
    int getPageNo();

    /**
     * Gets the page size.
     *
     * @return the page size
     */
    int getPageSize();

    /**
     * Gets the sorting information.
     *
     * @return the sorting information
     */
    Sort getSort();

    /**
     * Gets the offset (the starting position) for this pageable.
     *
     * @return the offset (0-based)
     */
    default long getOffset() {
        if (isUnpaged()) {
            return 0;
        }
        return (long) (getPageNo() - 1) * (long) getPageSize();
    }

    /**
     * Checks if this pageable is unpaged (no pagination).
     *
     * @return true if unpaged, false otherwise
     */
    default boolean isUnpaged() {
        return getPageSize() == UNPAGED_SIZE;
    }

    /**
     * Checks if this pageable is paged.
     *
     * @return true if paged, false otherwise
     */
    default boolean isPaged() {
        return !isUnpaged();
    }

    /**
     * Checks if the current page has a previous page.
     *
     * @return true if there is a previous page, false otherwise
     */
    default boolean hasPrevious() {
        return getPageNo() > 1;
    }

    /**
     * Gets a pageable for the previous page.
     *
     * @return a pageable for the previous page
     * @throws IllegalStateException if there is no previous page
     */
    Pageable previous() throws IllegalStateException;

    /**
     * Gets a pageable for the next page.
     *
     * @return a pageable for the next page
     */
    Pageable next();

    /**
     * Gets a pageable for the first page.
     *
     * @return a pageable for the first page
     */
    Pageable first();

    /**
     * Gets a pageable with the specified page number.
     *
     * @param pageNo the new page number
     * @return a pageable with the new page number
     */
    Pageable withPage(int pageNo);

    /**
     * Implementation of Pageable interface.
     */
    final class PageRequest implements Pageable {

        @Serial
        private static final long serialVersionUID = 2852289758690L;

        private final int pageNo;
        private final int pageSize;
        private final Sort sort;

        private PageRequest(int pageNo, int pageSize, Sort sort) {
            this.pageNo = Math.max(1, pageNo);
            this.pageSize = Math.max(1, pageSize);
            this.sort = sort != null ? sort : Sort.unsorted();
        }

        /**
         * Creates a page request with the specified page number and size.
         *
         * @param pageNo   the page number (1-based)
         * @param pageSize the page size
         * @return a page request
         */
        public static PageRequest of(int pageNo, int pageSize) {
            return of(pageNo, pageSize, Sort.unsorted());
        }

        /**
         * Creates a page request with the specified page number, size, and sorting.
         *
         * @param pageNo   the page number (1-based)
         * @param pageSize the page size
         * @param sort     the sorting information
         * @return a page request
         */
        public static PageRequest of(int pageNo, int pageSize, Sort sort) {
            return new PageRequest(pageNo, pageSize, sort);
        }

        /**
         * Creates an unpaged page request.
         *
         * @return an unpaged page request
         */
        public static PageRequest unpaged() {
            return new PageRequest(1, UNPAGED_SIZE, Sort.unsorted());
        }

        /**
         * Gets the page number.
         *
         * @return the page number
         */
        @Override
        public int getPageNo() {
            return pageNo;
        }

        /**
         * Gets the page size.
         *
         * @return the page size
         */
        @Override
        public int getPageSize() {
            return pageSize;
        }

        /**
         * Gets the sorting information.
         *
         * @return the sorting information
         */
        @Override
        public Sort getSort() {
            return sort;
        }

        /**
         * Checks if this pageable is unpaged.
         *
         * @return true if unpaged, false otherwise
         */
        @Override
        public boolean isUnpaged() {
            return pageSize == UNPAGED_SIZE;
        }

        /**
         * Gets a pageable for the previous page.
         *
         * @return a pageable for the previous page
         */
        @Override
        public PageRequest previous() {
            return hasPrevious() ? withPage(getPageNo() - 1) : this;
        }

        /**
         * Gets a pageable for the next page.
         *
         * @return a pageable for the next page
         */
        @Override
        public PageRequest next() {
            return withPage(getPageNo() + 1);
        }

        /**
         * Gets a pageable for the first page.
         *
         * @return a pageable for the first page
         */
        @Override
        public PageRequest first() {
            return withPage(1);
        }

        /**
         * Gets a pageable with the specified page number.
         *
         * @param pageNo the new page number
         * @return a pageable with the new page number
         */
        @Override
        public PageRequest withPage(int pageNo) {
            return new PageRequest(pageNo, getPageSize(), getSort());
        }

        /**
         * Creates a new PageRequest with the specified page size.
         *
         * @param pageSize the new page size
         * @return a new PageRequest with the specified page size
         */
        public PageRequest withPageSize(int pageSize) {
            return new PageRequest(getPageNo(), pageSize, getSort());
        }

        /**
         * Creates a new PageRequest with the specified sorting.
         *
         * @param sort the new sorting
         * @return a new PageRequest with the specified sorting
         */
        public PageRequest withSort(Sort sort) {
            return new PageRequest(getPageNo(), getPageSize(), sort);
        }

        /**
         * Checks if this object is equal to another.
         *
         * @param obj the object to compare
         * @return true if equal, false otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof PageRequest))
                return false;

            PageRequest that = (PageRequest) obj;

            if (pageNo != that.pageNo)
                return false;
            if (pageSize != that.pageSize)
                return false;
            return sort.equals(that.sort);
        }

        /**
         * Gets the hash code for this object.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            int result = pageNo;
            result = 31 * result + pageSize;
            result = 31 * result + sort.hashCode();
            return result;
        }

        /**
         * Gets the string representation of this pageable.
         *
         * @return the string representation
         */
        @Override
        public String toString() {
            return String.format("Page request [number: %d, size %d, sort: %s]", pageNo, pageSize, sort);
        }
    }

}
