/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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

import org.miaixz.bus.core.lang.Normal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 对Page结果进行包装 新增分页的多项属性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Paginating<T> extends Serialize<T> {

    public static final int DEFAULT_NAVIGATE_PAGES = 8;
    /**
     * 当前页
     */
    private int pageNo;
    /**
     * 每页的数量
     */
    private int pageSize;
    /**
     * 当前页的数量
     */
    private int size;
    /**
     * 由于startRow和endRow不常用，这里说个具体的用法 可以在页面中"显示startRow到endRow 共size条数据" 当前页面第一个元素在数据库中的行号
     */
    private long startRow;
    /**
     * 当前页面最后一个元素在数据库中的行号
     */
    private long endRow;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 前一页
     */
    private int prePage;
    /**
     * 下一页
     */
    private int nextPage;
    /**
     * 是否为第一页
     */
    private boolean isFirstPage = false;
    /**
     * 是否为最后一页
     */
    private boolean isLastPage = false;
    /**
     * 是否有前一页
     */
    private boolean hasPreviousPage = false;
    /**
     * 是否有下一页
     */
    private boolean hasNextPage = false;

    /**
     * 导航页码数
     */
    private int navigatePages;
    /**
     * 所有导航页号
     */
    private int[] navigatepageNo;
    /**
     * 导航条上的第一页
     */
    private int navigateFirstPage;
    /**
     * 导航条上的最后一页
     */
    private int navigateLastPage;

    public Paginating() {

    }

    /**
     * 包装Page对象
     *
     * @param list 分页结果
     */
    public Paginating(List<? extends T> list) {
        this(list, DEFAULT_NAVIGATE_PAGES);
    }

    /**
     * 包装Page对象
     *
     * @param list          分页结果
     * @param navigatePages 页码数量
     */
    public Paginating(List<? extends T> list, int navigatePages) {
        super(list);
        if (list instanceof Page) {
            Page page = (Page) list;
            this.pageNo = page.getPageNo();
            this.pageSize = page.getPageSize();

            this.pages = page.getPages();
            this.size = page.size();
            // 由于结果是>startRow的，所以实际的需要+1
            if (this.size == 0) {
                this.startRow = 0;
                this.endRow = 0;
            } else {
                this.startRow = page.getStartRow() + 1;
                // 计算实际的endRow（最后一页的时候特殊）
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

    public static <T> Paginating<T> of(List<? extends T> list) {
        return new Paginating<T>(list);
    }

    /**
     * 手动指定总记录数获取分页信息
     *
     * @param total 总记录数
     * @param list  page结果
     */
    public static <T> Paginating<T> of(long total, List<? extends T> list) {
        if (list instanceof Page) {
            Page page = (Page) list;
            page.setTotal(total);
        }
        return new Paginating<T>(list);
    }

    public static <T> Paginating<T> of(List<? extends T> list, int navigatePages) {
        return new Paginating<T>(list, navigatePages);
    }

    /**
     * 返回一个空的 Pageinfo 对象
     *
     * @return
     */
    public static <T> Paginating<T> emptyPageInfo() {
        return new Paginating(Collections.emptyList(), 0);
    }

    public void calcByNavigatePages(int navigatePages) {
        setNavigatePages(navigatePages);
        // 计算导航页
        calcNavigatepageNo();
        // 计算前后页，第一页，最后一页
        calcPage();
        // 判断页面边界
        judgePageBoudary();
    }

    /**
     * 计算导航页
     */
    private void calcNavigatepageNo() {
        // 当总页数小于或等于导航页码数时
        if (pages <= navigatePages) {
            navigatepageNo = new int[pages];
            for (int i = 0; i < pages; i++) {
                navigatepageNo[i] = i + 1;
            }
        } else { // 当总页数大于导航页码数时
            navigatepageNo = new int[navigatePages];
            int startNum = pageNo - navigatePages / 2;
            int endNum = pageNo + navigatePages / 2;

            if (startNum < 1) {
                startNum = 1;
                // (最前navigatePages页
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNo[i] = startNum++;
                }
            } else if (endNum > pages) {
                endNum = pages;
                // 最后navigatePages页
                for (int i = navigatePages - 1; i >= 0; i--) {
                    navigatepageNo[i] = endNum--;
                }
            } else {
                // 所有中间页
                for (int i = 0; i < navigatePages; i++) {
                    navigatepageNo[i] = startNum++;
                }
            }
        }
    }

    /**
     * 计算前后页，第一页，最后一页
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
     * 判定页面边界
     */
    private void judgePageBoudary() {
        isFirstPage = pageNo == 1;
        isLastPage = pageNo == pages || pages == 0;
        hasPreviousPage = pageNo > 1;
        hasNextPage = pageNo < pages;
    }

    /**
     * 数据对象转换
     *
     * @param function 用以转换数据对象的函数
     * @param <E>      目标类型
     * @return 转换了对象类型的包装结果
     */
    public <E> Paginating<E> convert(Page.Function<T, E> function) {
        List<E> list = new ArrayList<E>(this.list.size());
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
     * 是否包含内容
     */
    public boolean hasContent() {
        return this.size > 0;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getStartRow() {
        return startRow;
    }

    public void setStartRow(long startRow) {
        this.startRow = startRow;
    }

    public long getEndRow() {
        return endRow;
    }

    public void setEndRow(long endRow) {
        this.endRow = endRow;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public boolean isIsFirstPage() {
        return isFirstPage;
    }

    public void setIsFirstPage(boolean isFirstPage) {
        this.isFirstPage = isFirstPage;
    }

    public boolean isIsLastPage() {
        return isLastPage;
    }

    public void setIsLastPage(boolean isLastPage) {
        this.isLastPage = isLastPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getNavigatePages() {
        return navigatePages;
    }

    public void setNavigatePages(int navigatePages) {
        this.navigatePages = navigatePages;
    }

    public int[] getNavigatepageNo() {
        return navigatepageNo;
    }

    public void setNavigatepageNo(int[] navigatepageNo) {
        this.navigatepageNo = navigatepageNo;
    }

    public int getNavigateFirstPage() {
        return navigateFirstPage;
    }

    public void setNavigateFirstPage(int navigateFirstPage) {
        this.navigateFirstPage = navigateFirstPage;
    }

    public int getNavigateLastPage() {
        return navigateLastPage;
    }

    public void setNavigateLastPage(int navigateLastPage) {
        this.navigateLastPage = navigateLastPage;
    }

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
