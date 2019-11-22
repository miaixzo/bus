/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.pager;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis - 分页对象
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class Page<E> extends ArrayList<E> implements Closeable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码,从1开始
     */
    private int pageNo;
    /**
     * 页面大小
     */
    private int pageSize;
    /**
     * 起始行
     */
    private int startRow;
    /**
     * 末行
     */
    private int endRow;
    /**
     * 总数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 包含count查询
     */
    private boolean count = true;
    /**
     * 分页合理化
     */
    private Boolean reasonable;
    /**
     * 当设置为true的时候,如果pagesize设置为0（或RowBounds的limit=0）,就不执行分页,返回全部结果
     */
    private Boolean pageSizeZero;
    /**
     * 进行count查询的列名
     */
    private String countColumn;
    /**
     * 排序
     */
    private String orderBy;
    /**
     * 只增加排序
     */
    private boolean orderByOnly;

    public Page() {
        super();
    }

    public Page(int pageNo, int pageSize) {
        this(pageNo, pageSize, true, null);
    }

    public Page(int pageNo, int pageSize, boolean count) {
        this(pageNo, pageSize, count, null);
    }

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

    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNo = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }

    public List<E> getResult() {
        return this;
    }

    public int getPages() {
        return pages;
    }

    public Page<E> setPages(int pages) {
        this.pages = pages;
        return this;
    }

    public int getEndRow() {
        return endRow;
    }

    public Page<E> setEndRow(int endRow) {
        this.endRow = endRow;
        return this;
    }

    public int getPageNo() {
        return pageNo;
    }

    public Page<E> setPageNo(int pageNo) {
        //分页合理化,针对不合理的页码自动处理
        this.pageNo = ((reasonable != null && reasonable) && pageNo <= 0) ? 1 : pageNo;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Page<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getStartRow() {
        return startRow;
    }

    public Page<E> setStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public long getTotal() {
        return total;
    }

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
        //分页合理化,针对不合理的页码自动处理
        if ((reasonable != null && reasonable) && pageNo > pages) {
            if (pages != 0) {
                pageNo = pages;
            }
            calculateStartAndEndRow();
        }
    }

    public Boolean getReasonable() {
        return reasonable;
    }

    public Page<E> setReasonable(Boolean reasonable) {
        if (reasonable == null) {
            return this;
        }
        this.reasonable = reasonable;
        //分页合理化,针对不合理的页码自动处理
        if (this.reasonable && this.pageNo <= 0) {
            this.pageNo = 1;
            calculateStartAndEndRow();
        }
        return this;
    }

    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    public Page<E> setPageSizeZero(Boolean pageSizeZero) {
        if (pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public <E> Page<E> setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    public boolean isOrderByOnly() {
        return orderByOnly;
    }

    public void setOrderByOnly(boolean orderByOnly) {
        this.orderByOnly = orderByOnly;
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNo > 0 ? (this.pageNo - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNo > 0 ? 1 : 0);
    }

    public boolean isCount() {
        return this.count;
    }

    public Page<E> setCount(boolean count) {
        this.count = count;
        return this;
    }

    /**
     * 设置页码
     *
     * @param pageNo 页码
     * @return 结果
     */
    public Page<E> pageNo(int pageNo) {
        //分页合理化,针对不合理的页码自动处理
        this.pageNo = ((reasonable != null && reasonable) && pageNo <= 0) ? 1 : pageNo;
        return this;
    }

    /**
     * 设置页面大小
     *
     * @param pageSize 分页大小
     * @return 结果
     */
    public Page<E> pageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
        return this;
    }

    /**
     * 是否执行count查询
     *
     * @param count 统计
     * @return 结果
     */
    public Page<E> count(Boolean count) {
        this.count = count;
        return this;
    }

    /**
     * 设置合理化
     *
     * @param reasonable 合理化
     * @return 结果
     */
    public Page<E> reasonable(Boolean reasonable) {
        setReasonable(reasonable);
        return this;
    }

    /**
     * 当设置为true的时候,如果pagesize设置为0（或RowBounds的limit=0）,就不执行分页,返回全部结果
     *
     * @param pageSizeZero 分页大小
     * @return 结果
     */
    public Page<E> pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    /**
     * 指定 count 查询列
     *
     * @param columnName 列名
     * @return 结果
     */
    public Page<E> countColumn(String columnName) {
        this.countColumn = columnName;
        return this;
    }

    public Pages<E> toPageInfo() {
        Pages<E> pages = new Pages<E>(this);
        return pages;
    }

    public PageSerializable<E> toPageSerializable() {
        PageSerializable<E> serializable = new PageSerializable<E>(this);
        return serializable;
    }

    public <E> Page<E> doSelectPage(ISelect select) {
        select.doSelect();
        return (Page<E>) this;
    }

    public <E> Pages<E> doSelectPageInfo(ISelect select) {
        select.doSelect();
        return (Pages<E>) this.toPageInfo();
    }

    public <E> PageSerializable<E> doSelectPageSerializable(ISelect select) {
        select.doSelect();
        return (PageSerializable<E>) this.toPageSerializable();
    }

    public long doCount(ISelect select) {
        this.pageSizeZero = true;
        this.pageSize = 0;
        select.doSelect();
        return this.total;
    }

    public String getCountColumn() {
        return countColumn;
    }

    public void setCountColumn(String countColumn) {
        this.countColumn = countColumn;
    }

    @Override
    public String toString() {
        return "Page{" +
                "count=" + count +
                ", pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", startRow=" + startRow +
                ", endRow=" + endRow +
                ", total=" + total +
                ", pages=" + pages +
                ", reasonable=" + reasonable +
                ", pageSizeZero=" + pageSizeZero +
                '}' + super.toString();
    }

    @Override
    public void close() {
        PageContext.clearPage();
    }

}
