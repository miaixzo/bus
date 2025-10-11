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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Normal;

/**
 * Wraps the {@link Page} result, adding pagination-related properties to support navigation and page information
 * display.
 *
 * @param <T> the type of elements in the paginated data
 * @author Kimi Liu
 * @since Java 17+
 */
public class Paginating<T> extends Serialize<T> {

    /**
     * Default number of navigation pages.
     */
    public static final int DEFAULT_NAVIGATE_PAGES = 8;

    /**
     * The current page number.
     */
    private int pageNo;
    /**
     * The number of records per page.
     */
    private int pageSize;
    /**
     * The number of records in the current page.
     */
    private int size;
    /**
     * The row number of the first element in the current page in the database (starts from 1).
     */
    private long startRow;
    /**
     * The row number of the last element in the current page in the database.
     */
    private long endRow;
    /**
     * The total number of pages.
     */
    private int pages;
    /**
     * The page number of the previous page.
     */
    private int prePage;
    /**
     * The page number of the next page.
     */
    private int nextPage;
    /**
     * Flag indicating if the current page is the first page.
     */
    private boolean isFirstPage = false;
    /**
     * Flag indicating if the current page is the last page.
     */
    private boolean isLastPage = false;
    /**
     * Flag indicating if there is a previous page.
     */
    private boolean hasPreviousPage = false;
    /**
     * Flag indicating if there is a next page.
     */
    private boolean hasNextPage = false;
    /**
     * The number of navigation pages to display.
     */
    private int navigatePages;
    /**
     * An array of navigation page numbers.
     */
    private int[] navigatepageNo;
    /**
     * The first page number in the navigation bar.
     */
    private int navigateFirstPage;
    /**
     * The last page number in the navigation bar.
     */
    private int navigateLastPage;

    /**
     * Default constructor for Paginating.
     */
    public Paginating() {

    }

    /**
     * Constructs a Paginating object by wrapping a list of paginated results. Uses {@link #DEFAULT_NAVIGATE_PAGES} for
     * navigation.
     *
     * @param list the list of paginated results
     */
    public Paginating(List<? extends T> list) {
        this(list, DEFAULT_NAVIGATE_PAGES);
    }

    /**
     * Constructs a Paginating object by wrapping a list of paginated results and specifying the number of navigation
     * pages.
     *
     * @param list          the list of paginated results
     * @param navigatePages the number of navigation pages to display
     */
    public Paginating(List<? extends T> list, int navigatePages) {
        super(list);
        if (list instanceof Page) {
            Page page = (Page) list;
            this.pageNo = page.getPageNo();
            this.pageSize = page.getPageSize();
            this.pages = page.getPages();
            this.size = page.size();
            if (this.size == 0) {
                this.startRow = 0;
                this.endRow = 0;
            } else {
                this.startRow = page.getStartRow() + 1;
                this.endRow = this.startRow - 1 + this.size;
            }
        } else if (list instanceof Collection) {
            this.pageNo = 1;
            this.pageSize = list.size();
            this.pages = this.pageSize > 0 ? 1 : 0;
            this.size = list.size();
            this.startRow = 0;
            this.endRow = list.size() > 0 ? list.size() - 1 : 0;
        }
        if (list instanceof Collection) {
            calcByNavigatePages(navigatePages);
        }
    }

    /**
     * Static factory method to create a Paginating object.
     *
     * @param list the list of paginated results
     * @param <T>  the type of elements in the paginated data
     * @return a new Paginating object
     */
    public static <T> Paginating<T> of(List<? extends T> list) {
        return new Paginating<>(list);
    }

    /**
     * Static factory method to create a Paginating object with a specified total number of records.
     *
     * @param total the total number of records
     * @param list  the list of paginated results
     * @param <T>   the type of elements in the paginated data
     * @return a new Paginating object
     */
    public static <T> Paginating<T> of(long total, List<? extends T> list) {
        if (list instanceof Page) {
            Page page = (Page) list;
            page.setTotal(total);
        }
        return new Paginating<>(list);
    }

    /**
     * Static factory method to create a Paginating object with a specified number of navigation pages.
     *
     * @param list          the list of paginated results
     * @param navigatePages the number of navigation pages to display
     * @param <T>           the type of elements in the paginated data
     * @return a new Paginating object
     */
    public static <T> Paginating<T> of(List<? extends T> list, int navigatePages) {
        return new Paginating<>(list, navigatePages);
    }

    /**
     * Static factory method to return an empty Paginating object.
     *
     * @param <T> the type of elements in the paginated data
     * @return an empty Paginating object
     */
    public static <T> Paginating<T> emptyPageInfo() {
        return new Paginating<>(Collections.emptyList(), 0);
    }

    /**
     * Calculates pagination properties based on the number of navigation pages.
     *
     * @param navigatePages the number of navigation pages to display
     */
    public void calcByNavigatePages(int navigatePages) {
        setNavigatePages(navigatePages);
        calcNavigatepageNo();
        calcPage();
        judgePageBoudary();
    }

    /**
     * Calculates the navigation page numbers.
     */
    private void calcNavigatepageNo() {
        if (pages <= navigatePages) {
            navigatepageNo = new int[pages];
            for (int i = 0; i < pages; i++) {
                navigatepageNo[i] = i + 1;
            }
        } else {
            navigatepageNo = new int[navigatePages];
            int startNum = pageNo - navigatePages / 2;
            int endNum = pageNo + navigatePages / 2;

            if (startNum < 1) {
                startNum = 1;
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNo[i] = startNum++;
                }
            } else if (endNum > pages) {
                endNum = pages;
                for (int i = navigatePages - 1; i >= 0; i--) {
                    navigatepageNo[i] = endNum--;
                }
            } else {
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNo[i] = startNum++;
                }
            }
        }
    }

    /**
     * Calculates the previous page, next page, first navigation page, and last navigation page.
     */
    private void calcPage() {
        if (navigatepageNo != null && navigatepageNo.length > 0) {
            navigateFirstPage = navigatepageNo[0];
            navigateLastPage = navigatepageNo[navigatepageNo.length - 1];
            if (pageNo > 1) {
                prePage = pageNo - 1;
            }
            if (pageNo < pages) {
                nextPage = pageNo + 1;
            }
        }
    }

    /**
     * Determines the boundary status of the page (e.g., isFirstPage, isLastPage).
     */
    private void judgePageBoudary() {
        isFirstPage = pageNo == 1;
        isLastPage = pageNo == pages || pages == 0;
        hasPreviousPage = pageNo > 1;
        hasNextPage = pageNo < pages;
    }

    /**
     * Converts the paginated data to a different type using a provided function.
     *
     * @param function the data conversion function
     * @param <E>      the target data type
     * @return a new Paginating object with converted data
     */
    public <E> Paginating<E> convert(FunctionX<T, E> function) {
        List<E> list = new ArrayList<>(this.list.size());
        for (T t : this.list) {
            list.add(function.apply(t));
        }
        Paginating<E> newPaginating = new Paginating<>(list);
        newPaginating.setPageNo(this.pageNo);
        newPaginating.setPageSize(this.pageSize);
        newPaginating.setSize(this.size);
        newPaginating.setStartRow(this.startRow);
        newPaginating.setEndRow(this.endRow);
        newPaginating.setTotal(this.total);
        newPaginating.setPages(this.pages);
        newPaginating.setPrePage(this.prePage);
        newPaginating.setNextPage(this.nextPage);
        newPaginating.setIsFirstPage(this.isFirstPage);
        newPaginating.setIsLastPage(this.isLastPage);
        newPaginating.setHasPreviousPage(this.hasPreviousPage);
        newPaginating.setHasNextPage(this.hasNextPage);
        newPaginating.setNavigatePages(this.navigatePages);
        newPaginating.setNavigateFirstPage(this.navigateFirstPage);
        newPaginating.setNavigateLastPage(this.navigateLastPage);
        newPaginating.setNavigatepageNo(this.navigatepageNo);
        return newPaginating;
    }

    /**
     * Checks if the paginating object contains any data.
     *
     * @return true if it contains data, false otherwise
     */
    public boolean hasContent() {
        return this.size > 0;
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
     * Sets the current page number.
     *
     * @param pageNo the current page number to set
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * Retrieves the number of records per page.
     *
     * @return the number of records per page
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the number of records per page.
     *
     * @param pageSize the number of records per page to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Retrieves the number of records in the current page.
     *
     * @return the number of records in the current page
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the number of records in the current page.
     *
     * @param size the number of records in the current page to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Retrieves the row number of the first element in the current page.
     *
     * @return the row number of the first element
     */
    public long getStartRow() {
        return startRow;
    }

    /**
     * Sets the row number of the first element in the current page.
     *
     * @param startRow the row number of the first element to set
     */
    public void setStartRow(long startRow) {
        this.startRow = startRow;
    }

    /**
     * Retrieves the row number of the last element in the current page.
     *
     * @return the row number of the last element
     */
    public long getEndRow() {
        return endRow;
    }

    /**
     * Sets the row number of the last element in the current page.
     *
     * @param endRow the row number of the last element to set
     */
    public void setEndRow(long endRow) {
        this.endRow = endRow;
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
     * @param pages the total number of pages to set
     */
    public void setPages(int pages) {
        this.pages = pages;
    }

    /**
     * Retrieves the page number of the previous page.
     *
     * @return the previous page number
     */
    public int getPrePage() {
        return prePage;
    }

    /**
     * Sets the page number of the previous page.
     *
     * @param prePage the previous page number to set
     */
    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    /**
     * Retrieves the page number of the next page.
     *
     * @return the next page number
     */
    public int getNextPage() {
        return nextPage;
    }

    /**
     * Sets the page number of the next page.
     *
     * @param nextPage the next page number to set
     */
    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    /**
     * Checks if the current page is the first page.
     *
     * @return true if it is the first page, false otherwise
     */
    public boolean isIsFirstPage() {
        return isFirstPage;
    }

    /**
     * Sets whether the current page is the first page.
     *
     * @param isFirstPage true if it is the first page, false otherwise
     */
    public void setIsFirstPage(boolean isFirstPage) {
        this.isFirstPage = isFirstPage;
    }

    /**
     * Checks if the current page is the last page.
     *
     * @return true if it is the last page, false otherwise
     */
    public boolean isIsLastPage() {
        return isLastPage;
    }

    /**
     * Sets whether the current page is the last page.
     *
     * @param isLastPage true if it is the last page, false otherwise
     */
    public void setIsLastPage(boolean isLastPage) {
        this.isLastPage = isLastPage;
    }

    /**
     * Checks if there is a previous page.
     *
     * @return true if there is a previous page, false otherwise
     */
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    /**
     * Sets whether there is a previous page.
     *
     * @param hasPreviousPage true if there is a previous page, false otherwise
     */
    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    /**
     * Checks if there is a next page.
     *
     * @return true if there is a next page, false otherwise
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }

    /**
     * Sets whether there is a next page.
     *
     * @param hasNextPage true if there is a next page, false otherwise
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    /**
     * Retrieves the number of navigation pages to display.
     *
     * @return the number of navigation pages
     */
    public int getNavigatePages() {
        return navigatePages;
    }

    /**
     * Sets the number of navigation pages to display.
     *
     * @param navigatePages the number of navigation pages to set
     */
    public void setNavigatePages(int navigatePages) {
        this.navigatePages = navigatePages;
    }

    /**
     * Retrieves the array of navigation page numbers.
     *
     * @return an array of navigation page numbers
     */
    public int[] getNavigatepageNo() {
        return navigatepageNo;
    }

    /**
     * Sets the array of navigation page numbers.
     *
     * @param navigatepageNo the array of navigation page numbers to set
     */
    public void setNavigatepageNo(int[] navigatepageNo) {
        this.navigatepageNo = navigatepageNo;
    }

    /**
     * Retrieves the first page number in the navigation bar.
     *
     * @return the first navigation page number
     */
    public int getNavigateFirstPage() {
        return navigateFirstPage;
    }

    /**
     * Sets the first page number in the navigation bar.
     *
     * @param navigateFirstPage the first navigation page number to set
     */
    public void setNavigateFirstPage(int navigateFirstPage) {
        this.navigateFirstPage = navigateFirstPage;
    }

    /**
     * Retrieves the last page number in the navigation bar.
     *
     * @return the last navigation page number
     */
    public int getNavigateLastPage() {
        return navigateLastPage;
    }

    /**
     * Sets the last page number in the navigation bar.
     *
     * @param navigateLastPage the last navigation page number to set
     */
    public void setNavigateLastPage(int navigateLastPage) {
        this.navigateLastPage = navigateLastPage;
    }

    /**
     * Returns a string representation of the Paginating object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Paginating{");
        sb.append("pageNo=").append(pageNo);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", size=").append(size);
        sb.append(", startRow=").append(startRow);
        sb.append(", endRow=").append(endRow);
        sb.append(", total=").append(total);
        sb.append(", pages=").append(pages);
        sb.append(", list=").append(list);
        sb.append(", prePage=").append(prePage);
        sb.append(", nextPage=").append(nextPage);
        sb.append(", isFirstPage=").append(isFirstPage);
        sb.append(", isLastPage=").append(isLastPage);
        sb.append(", hasPreviousPage=").append(hasPreviousPage);
        sb.append(", hasNextPage=").append(hasNextPage);
        sb.append(", navigatePages=").append(navigatePages);
        sb.append(", navigateFirstPage=").append(navigateFirstPage);
        sb.append(", navigateLastPage=").append(navigateLastPage);
        sb.append(", navigatepageNo=");
        if (navigatepageNo == null) {
            sb.append("null");
        } else {
            sb.append('[');
            for (int i = 0; i < navigatepageNo.length; ++i) {
                sb.append(i == 0 ? Normal.EMPTY : ", ").append(navigatepageNo[i]);
            }
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }

}
