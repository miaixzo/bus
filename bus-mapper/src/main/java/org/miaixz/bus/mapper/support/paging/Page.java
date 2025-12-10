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
package org.miaixz.bus.mapper.support.paging;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unified page result class that combines pagination data and metadata.
 *
 * <p>
 * This class replaces the old Page/Paginating/Paging classes with a single unified implementation that provides all
 * pagination functionality in one place.
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Extends ArrayList for MyBatis compatibility</li>
 * <li>Complete pagination metadata (pageNo, pageSize, total, pages)</li>
 * <li>Row tracking (startRow, endRow)</li>
 * <li>Navigation support (isFirst, isLast, hasNext, hasPrevious)</li>
 * <li>Navigation page numbers array for UI rendering</li>
 * <li>Builder pattern for easy construction</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * // Create using builder
 * Page<User> page = Page.<User>builder().result(users).pageable(PageRequest.of(0, 10)).total(100).build();
 *
 * // Access data
 * List<User> users = page.getResult(); // or just use page directly as List
 * for (User user : page) {
 *     // ...
 * }
 *
 * // Access metadata
 * int pageNo = page.getPageNo(); // 1 (1-based)
 * int pages = page.getPages(); // 10
 * long total = page.getTotal(); // 100
 *
 * // Navigation
 * boolean isFirst = page.isFirstPage(); // true
 * boolean hasNext = page.hasNextPage(); // true
 * int[] navPages = page.getNavigatePages(5); // [1, 2, 3, 4, 5]
 * }</pre>
 *
 * @param <T> the type of content in the page
 * @author Kimi Liu
 * @since Java 17+
 */
public class Page<T> extends ArrayList<T> {

    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852289758798L;

    /**
     * The pagination request information.
     */
    private final Pageable pageable;

    /**
     * The total number of elements across all pages.
     */
    private final long total;

    /**
     * Creates a new Page instance.
     *
     * @param result   the content of the page (if null, empty list is used)
     * @param pageable the pageable information (if null, unpaged is used)
     * @param total    the total number of elements (negative values treated as 0)
     */
    public Page(List<T> result, Pageable pageable, long total) {
        super(result != null ? result : Collections.emptyList());
        this.pageable = pageable != null ? pageable : Pageable.unpaged();
        this.total = Math.max(0, total);
    }

    /**
     * Creates a new builder for Page instances.
     *
     * @param <T> the type of content
     * @return a new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Creates an empty page.
     *
     * @param <T> the type of content
     * @return an empty page
     */
    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), Pageable.unpaged(), 0);
    }

    /**
     * Creates an empty page with the specified pageable.
     *
     * @param <T>      the type of content
     * @param pageable the pageable information
     * @return an empty page with the specified pageable
     */
    public static <T> Page<T> empty(Pageable pageable) {
        return new Page<>(Collections.emptyList(), pageable, 0);
    }

    /**
     * Gets the page content as a list. Since this class extends ArrayList, it returns itself.
     *
     * @return this instance as a list
     */
    public List<T> getResult() {
        return this;
    }

    /**
     * Gets the total number of elements across all pages.
     *
     * @return the total number of elements
     */
    public long getTotal() {
        return total;
    }

    /**
     * Gets the current page number (1-based). Converts from internal 0-based index to user-friendly 1-based.
     *
     * @return the current page number (1-based)
     */
    public int getPageNo() {
        return pageable.getPageNo() + 1;
    }

    /**
     * Gets the page size.
     *
     * @return the expected size of each page
     */
    public int getPageSize() {
        return pageable.getPageSize();
    }

    /**
     * Gets the total number of pages.
     *
     * @return the total number of pages
     */
    public int getPages() {
        if (pageable.isUnpaged() || total == 0) {
            return 1;
        }
        return (int) Math.ceil((double) total / pageable.getPageSize());
    }

    /**
     * Gets the starting row number for the current page (1-based).
     *
     * @return the starting row number
     */
    public long getStartRow() {
        if (pageable.isUnpaged()) {
            return 1;
        }
        return (long) pageable.getPageNo() * pageable.getPageSize() + 1;
    }

    /**
     * Gets the ending row number for the current page (1-based).
     *
     * @return the ending row number
     */
    public long getEndRow() {
        if (pageable.isUnpaged()) {
            return total;
        }
        return getStartRow() + this.size() - 1;
    }

    /**
     * Checks if this is the first page.
     *
     * @return true if this is the first page, false otherwise
     */
    public boolean isFirstPage() {
        return getPageNo() == 1;
    }

    /**
     * Checks if this is the last page.
     *
     * @return true if this is the last page, false otherwise
     */
    public boolean isLastPage() {
        return getPageNo() == getPages();
    }

    /**
     * Checks if there is a previous page.
     *
     * @return true if there is a previous page, false otherwise
     */
    public boolean hasPreviousPage() {
        return getPageNo() > 1;
    }

    /**
     * Checks if there is a next page.
     *
     * @return true if there is a next page, false otherwise
     */
    public boolean hasNextPage() {
        return getPageNo() < getPages();
    }

    /**
     * Gets the previous page number.
     *
     * @return the previous page number, or 0 if no previous page
     */
    public int getPrePage() {
        return hasPreviousPage() ? getPageNo() - 1 : 0;
    }

    /**
     * Gets the next page number.
     *
     * @return the next page number, or 0 if no next page
     */
    public int getNextPage() {
        return hasNextPage() ? getPageNo() + 1 : 0;
    }

    // ==================== Navigation Pages ====================

    /**
     * Gets navigation page numbers for UI rendering with default size (8).
     *
     * @return an array of page numbers for navigation
     */
    public int[] getNavigatePages() {
        return getNavigatePages(8);
    }

    /**
     * Gets navigation page numbers for UI rendering.
     * <p>
     * This method calculates which page numbers to display in a pagination control. It centers the current page when
     * possible and adjusts to boundaries.
     * </p>
     *
     * @param navigatePages the number of page numbers to display
     * @return an array of page numbers for navigation
     */
    public int[] getNavigatePages(int navigatePages) {
        int totalPages = getPages();
        int currentPage = getPageNo();

        if (totalPages <= navigatePages) {
            // Show all pages if total is less than or equal to navigate size
            int[] pages = new int[totalPages];
            for (int i = 0; i < totalPages; i++) {
                pages[i] = i + 1;
            }
            return pages;
        }

        // Calculate start and end page numbers centered around current page
        int[] pages = new int[navigatePages];
        int startPage = currentPage - navigatePages / 2;
        int endPage = currentPage + navigatePages / 2;

        // Adjust if start is less than 1
        if (startPage < 1) {
            startPage = 1;
            endPage = navigatePages;
        }

        // Adjust if end is greater than total pages
        if (endPage > totalPages) {
            endPage = totalPages;
            startPage = totalPages - navigatePages + 1;
        }

        // Fill the array
        for (int i = 0; i < navigatePages; i++) {
            pages[i] = startPage + i;
        }

        return pages;
    }

    /**
     * Gets the first page number in the navigation array.
     *
     * @return the first navigation page number
     */
    public int getNavigateFirstPage() {
        int[] pages = getNavigatePages();
        return pages.length > 0 ? pages[0] : 1;
    }

    /**
     * Gets the last page number in the navigation array.
     *
     * @return the last navigation page number
     */
    public int getNavigateLastPage() {
        int[] pages = getNavigatePages();
        return pages.length > 0 ? pages[pages.length - 1] : 1;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Page))
            return false;

        Page<?> that = (Page<?>) obj;

        if (!super.equals(that))
            return false;
        if (!pageable.equals(that.pageable))
            return false;
        return total == that.total;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + pageable.hashCode();
        result = 31 * result + Long.hashCode(total);
        return result;
    }

    @Override
    public String toString() {
        String contentType = "UNKNOWN";
        if (!this.isEmpty()) {
            contentType = this.get(0).getClass().getSimpleName();
        }
        return String.format("Page %d of %d containing %s instances", getPageNo(), getPages(), contentType);
    }

    // ==================== Builder ====================

    /**
     * Builder for creating Page instances.
     *
     * @param <T> the type of content
     */
    public static class Builder<T> {

        private List<T> result = Collections.emptyList();
        private Pageable pageable = Pageable.unpaged();
        private long total = 0;

        private Builder() {
        }

        /**
         * Sets the content of the page.
         *
         * @param result the list of content
         * @return this builder for method chaining
         */
        public Builder<T> result(List<T> result) {
            this.result = result != null ? new ArrayList<>(result) : Collections.emptyList();
            return this;
        }

        /**
         * Sets the pageable information.
         *
         * @param pageable the pageable information
         * @return this builder for method chaining
         */
        public Builder<T> pageable(Pageable pageable) {
            this.pageable = pageable != null ? pageable : Pageable.unpaged();
            return this;
        }

        /**
         * Sets the total number of elements.
         *
         * @param total the total number of elements
         * @return this builder for method chaining
         */
        public Builder<T> total(long total) {
            this.total = Math.max(0, total);
            return this;
        }

        /**
         * Builds the Page instance.
         *
         * @return the constructed Page
         */
        public Page<T> build() {
            return new Page<>(result, pageable, total);
        }
    }

}
