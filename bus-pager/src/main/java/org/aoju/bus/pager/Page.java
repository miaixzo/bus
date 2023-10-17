/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.pager;

import org.aoju.bus.core.exception.PageException;
import org.aoju.bus.pager.plugins.BoundSqlHandler;
import org.aoju.bus.pager.plugins.SqlInjection;
import org.aoju.bus.pager.proxy.PageAutoDialect;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis - 分页对象
 *
 * @author Kimi Liu
 * @since Java 17+
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
    private long startRow;
    /**
     * 末行
     */
    private long endRow;
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
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
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
    /**
     * 转换count查询时保留查询的 order by 排序
     */
    private Boolean keepOrderBy;
    /**
     * 转换count查询时保留子查询的 order by 排序
     */
    private Boolean keepSubSelectOrderBy;
    /**
     * sql拦截处理
     */
    private BoundSqlHandler boundSqlHandler;
    private transient BoundSqlHandler.Chain chain;
    /**
     * 分页实现类，可以使用 {@link PageAutoDialect} 类中注册的别名，例如 "mysql", "oracle"
     */
    private String dialectClass;

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

    /**
     * @param rowBounds 分页对象
     * @param count     总数
     */
    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
            this.pageNo = 1;
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

    public long getEndRow() {
        return endRow;
    }

    public Page<E> setEndRow(long endRow) {
        this.endRow = endRow;
        return this;
    }

    public int getPageNo() {
        return pageNo;
    }

    public Page<E> setPageNo(int pageNo) {
        // 分页合理化，针对不合理的页码自动处理
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

    public long getStartRow() {
        return startRow;
    }

    public Page<E> setStartRow(long startRow) {
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
        // 分页合理化，针对不合理的页码自动处理
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
        // 分页合理化，针对不合理的页码自动处理
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
        if (this.pageSizeZero == null && pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 设置排序字段，增加 SQL 注入校验，如果需要在 order by 使用函数，可以使用 {@link #setUnsafeOrderBy(String)} 方法
     *
     * @param orderBy 排序字段
     */
    public <E> Page<E> setOrderBy(String orderBy) {
        if (SqlInjection.check(orderBy)) {
            throw new PageException("order by [" + orderBy + "] 存在 SQL 注入风险, 如想避免 SQL 注入校验，可以调用 Page.setUnsafeOrderBy");
        }
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    /**
     * 不安全的设置排序方法，如果从前端接收参数，请自行做好注入校验。
     * <p>
     * 请不要故意使用该方法注入然后提交漏洞!!!
     *
     * @param orderBy 排序字段
     */
    public <E> Page<E> setUnsafeOrderBy(String orderBy) {
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
        // 分页合理化，针对不合理的页码自动处理
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
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
     *
     * @param pageSizeZero 分页大小
     * @return 结果
     */
    public Page<E> pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    /**
     * 设置 BoundSql 拦截器
     *
     * @param boundSqlHandler 分页拦截器
     * @return 结果
     */
    public Page<E> boundSqlInterceptor(BoundSqlHandler boundSqlHandler) {
        setBoundSqlInterceptor(boundSqlHandler);
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

    public Page<E> keepOrderBy(boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    public boolean keepOrderBy() {
        return this.keepOrderBy != null && this.keepOrderBy;
    }

    public Boolean getKeepOrderBy() {
        return keepOrderBy;
    }

    public void setKeepOrderBy(Boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
    }

    public Boolean getKeepSubSelectOrderBy() {
        return keepSubSelectOrderBy;
    }

    public void setKeepSubSelectOrderBy(Boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
    }

    public Paginating<E> toPageInfo() {
        return new Paginating<>(this);
    }

    /**
     * 数据对象转换
     *
     * @param function 函数
     * @param <T>      泛型对象
     * @return 分页属性
     */
    public <T> Paginating<T> toPageInfo(Function<E, T> function) {
        List<T> list = new ArrayList<>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        Paginating<T> paginating = new Paginating<>(list);
        paginating.setTotal(this.getTotal());
        paginating.setPageNo(this.getPageNo());
        paginating.setPageSize(this.getPageSize());
        paginating.setPages(this.getPages());
        paginating.setStartRow(this.getStartRow());
        paginating.setEndRow(this.getEndRow());
        paginating.calcByNavigatePages(Paginating.DEFAULT_NAVIGATE_PAGES);
        return paginating;
    }

    public Serialize<E> toPageSerializable() {
        return new Serialize<>(this);
    }

    /**
     * 数据对象转换
     *
     * @param function 函数
     * @param <T>      泛型对象
     * @return 分页属性
     */
    public <T> Serialize<T> toPageSerializable(Function<E, T> function) {
        List<T> list = new ArrayList<>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        Serialize<T> serialize = new Serialize<>(list);
        serialize.setTotal(this.getTotal());
        return serialize;
    }

    public <E> Page<E> doSelectPage(Querying select) {
        select.doSelect();
        return (Page<E>) this;
    }

    public <E> Paginating<E> doSelectPageInfo(Querying select) {
        select.doSelect();
        return (Paginating<E>) this.toPageInfo();
    }

    public <E> Serialize<E> doSelectPageSerializable(Querying select) {
        select.doSelect();
        return (Serialize<E>) this.toPageSerializable();
    }

    public long doCount(Querying select) {
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

    public BoundSqlHandler getBoundSqlInterceptor() {
        return boundSqlHandler;
    }

    public void setBoundSqlInterceptor(BoundSqlHandler boundSqlHandler) {
        this.boundSqlHandler = boundSqlHandler;
    }

    BoundSqlHandler.Chain getChain() {
        return chain;
    }

    void setChain(BoundSqlHandler.Chain chain) {
        this.chain = chain;
    }

    public String getDialectClass() {
        return dialectClass;
    }

    public void setDialectClass(String dialectClass) {
        this.dialectClass = dialectClass;
    }

    /**
     * 指定使用的分页实现，如果自己使用的很频繁，建议自己增加一层封装再使用
     *
     * @param dialect 分页实现类，可以使用 {@link PageAutoDialect} 类中注册的别名，例如 "mysql", "oracle"
     * @return this
     */
    public Page<E> using(String dialect) {
        this.dialectClass = dialect;
        return this;
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

    /**
     * 兼容低版本 Java 7
     */
    public interface Function<E, T> {

        /**
         * 将此函数应用于给定的参数
         *
         * @param t 函数参数
         * @return 函数结构
         */
        T apply(E t);

    }

}
