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
 * 对Page结果进行包装，新增分页相关属性以支持导航和页面信息展示。
 *
 * @param <T> 分页数据元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Paginating<T> extends Serialize<T> {

    /**
     * 默认导航页码数
     */
    public static final int DEFAULT_NAVIGATE_PAGES = 8;

    /**
     * 当前页码
     */
    private int pageNo;
    /**
     * 每页记录数
     */
    private int pageSize;
    /**
     * 当前页记录数
     */
    private int size;
    /**
     * 当前页面第一个元素在数据库中的行号（从1开始）
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
     * 前一页页码
     */
    private int prePage;
    /**
     * 下一页页码
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
     * 导航页码数组
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

    /**
     * 默认构造函数。
     */
    public Paginating() {

    }

    /**
     * 构造函数，包装分页结果。
     *
     * @param list 分页结果列表
     */
    public Paginating(List<? extends T> list) {
        this(list, DEFAULT_NAVIGATE_PAGES);
    }

    /**
     * 构造函数，包装分页结果并指定导航页码数。
     *
     * @param list          分页结果列表
     * @param navigatePages 导航页码数
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
     * 静态工厂方法，创建Paginating对象。
     *
     * @param list 分页结果列表
     * @param <T>  分页数据元素类型
     * @return Paginating对象
     */
    public static <T> Paginating<T> of(List<? extends T> list) {
        return new Paginating<>(list);
    }

    /**
     * 静态工厂方法，创建Paginating对象并指定总记录数。
     *
     * @param total 总记录数
     * @param list  分页结果列表
     * @param <T>   分页数据元素类型
     * @return Paginating对象
     */
    public static <T> Paginating<T> of(long total, List<? extends T> list) {
        if (list instanceof Page) {
            Page page = (Page) list;
            page.setTotal(total);
        }
        return new Paginating<>(list);
    }

    /**
     * 静态工厂方法，创建Paginating对象并指定导航页码数。
     *
     * @param list          分页结果列表
     * @param navigatePages 导航页码数
     * @param <T>           分页数据元素类型
     * @return Paginating对象
     */
    public static <T> Paginating<T> of(List<? extends T> list, int navigatePages) {
        return new Paginating<>(list, navigatePages);
    }

    /**
     * 静态工厂方法，返回空的Paginating对象。
     *
     * @param <T> 分页数据元素类型
     * @return 空的Paginating对象
     */
    public static <T> Paginating<T> emptyPageInfo() {
        return new Paginating<>(Collections.emptyList(), 0);
    }

    /**
     * 根据导航页码数计算分页属性。
     *
     * @param navigatePages 导航页码数
     */
    public void calcByNavigatePages(int navigatePages) {
        setNavigatePages(navigatePages);
        calcNavigatepageNo();
        calcPage();
        judgePageBoudary();
    }

    /**
     * 计算导航页码。
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
     * 计算前后页、第一页和最后一页。
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
     * 判断页面边界状态。
     */
    private void judgePageBoudary() {
        isFirstPage = pageNo == 1;
        isLastPage = pageNo == pages || pages == 0;
        hasPreviousPage = pageNo > 1;
        hasNextPage = pageNo < pages;
    }

    /**
     * 转换分页数据类型。
     *
     * @param function 数据转换函数
     * @param <E>      目标数据类型
     * @return 转换后的Paginating对象
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
     * 检查是否包含数据。
     *
     * @return 若包含数据返回true，否则返回false
     */
    public boolean hasContent() {
        return this.size > 0;
    }

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 设置当前页码。
     *
     * @param pageNo 当前页码
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * 获取每页记录数。
     *
     * @return 每页记录数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页记录数。
     *
     * @param pageSize 每页记录数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取当前页记录数。
     *
     * @return 当前页记录数
     */
    public int getSize() {
        return size;
    }

    /**
     * 设置当前页记录数。
     *
     * @param size 当前页记录数
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 获取当前页面第一个元素的行号。
     *
     * @return 第一个元素的行号
     */
    public long getStartRow() {
        return startRow;
    }

    /**
     * 设置当前页面第一个元素的行号。
     *
     * @param startRow 第一个元素的行号
     */
    public void setStartRow(long startRow) {
        this.startRow = startRow;
    }

    /**
     * 获取当前页面最后一个元素的行号。
     *
     * @return 最后一个元素的行号
     */
    public long getEndRow() {
        return endRow;
    }

    /**
     * 设置当前页面最后一个元素的行号。
     *
     * @param endRow 最后一个元素的行号
     */
    public void setEndRow(long endRow) {
        this.endRow = endRow;
    }

    /**
     * 获取总页数。
     *
     * @return 总页数
     */
    public int getPages() {
        return pages;
    }

    /**
     * 设置总页数。
     *
     * @param pages 总页数
     */
    public void setPages(int pages) {
        this.pages = pages;
    }

    /**
     * 获取前一页页码。
     *
     * @return 前一页页码
     */
    public int getPrePage() {
        return prePage;
    }

    /**
     * 设置前一页页码。
     *
     * @param prePage 前一页页码
     */
    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    /**
     * 获取下一页页码。
     *
     * @return 下一页页码
     */
    public int getNextPage() {
        return nextPage;
    }

    /**
     * 设置下一页页码。
     *
     * @param nextPage 下一页页码
     */
    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    /**
     * 是否为第一页。
     *
     * @return 若为第一页返回true
     */
    public boolean isIsFirstPage() {
        return isFirstPage;
    }

    /**
     * 设置是否为第一页。
     *
     * @param isFirstPage 是否为第一页
     */
    public void setIsFirstPage(boolean isFirstPage) {
        this.isFirstPage = isFirstPage;
    }

    /**
     * 是否为最后一页。
     *
     * @return 若为最后一页返回true
     */
    public boolean isIsLastPage() {
        return isLastPage;
    }

    /**
     * 设置是否为最后一页。
     *
     * @param isLastPage 是否为最后一页
     */
    public void setIsLastPage(boolean isLastPage) {
        this.isLastPage = isLastPage;
    }

    /**
     * 是否有前一页。
     *
     * @return 若有前一页返回true
     */
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    /**
     * 设置是否有前一页。
     *
     * @param hasPreviousPage 是否有前一页
     */
    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    /**
     * 是否有下一页。
     *
     * @return 若有下一页返回true
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }

    /**
     * 设置是否有下一页。
     *
     * @param hasNextPage 是否有下一页
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    /**
     * 获取导航页码数。
     *
     * @return 导航页码数
     */
    public int getNavigatePages() {
        return navigatePages;
    }

    /**
     * 设置导航页码数。
     *
     * @param navigatePages 导航页码数
     */
    public void setNavigatePages(int navigatePages) {
        this.navigatePages = navigatePages;
    }

    /**
     * 获取导航页码数组。
     *
     * @return 导航页码数组
     */
    public int[] getNavigatepageNo() {
        return navigatepageNo;
    }

    /**
     * 设置导航页码数组。
     *
     * @param navigatepageNo 导航页码数组
     */
    public void setNavigatepageNo(int[] navigatepageNo) {
        this.navigatepageNo = navigatepageNo;
    }

    /**
     * 获取导航条上的第一页。
     *
     * @return 导航第一页
     */
    public int getNavigateFirstPage() {
        return navigateFirstPage;
    }

    /**
     * 设置导航条上的第一页。
     *
     * @param navigateFirstPage 导航第一页
     */
    public void setNavigateFirstPage(int navigateFirstPage) {
        this.navigateFirstPage = navigateFirstPage;
    }

    /**
     * 获取导航条上的最后一页。
     *
     * @return 导航最后一页
     */
    public int getNavigateLastPage() {
        return navigateLastPage;
    }

    /**
     * 设置导航条上的最后一页。
     *
     * @param navigateLastPage 导航最后一页
     */
    public void setNavigateLastPage(int navigateLastPage) {
        this.navigateLastPage = navigateLastPage;
    }

    /**
     * 返回Paginating对象的字符串表示。
     *
     * @return 字符串表示
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