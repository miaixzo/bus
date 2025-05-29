/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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

import java.io.Closeable;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.mapper.OGNL;
import org.miaixz.bus.pager.binding.PageAutoDialect;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;

/**
 * MyBatis分页对象，支持分页查询及结果集管理。
 *
 * @param <E> 分页数据元素的类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Page<E> extends ArrayList<E> implements Closeable {

    @Serial
    private static final long serialVersionUID = 2852281222526L;

    /**
     * 记录当前堆栈，可查找到Page对象创建位置（需开启page.debug）
     */
    private final String stackTrace = Builder.current();
    /**
     * 页码，从1开始
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
     * 是否包含count查询
     */
    private boolean count = true;
    /**
     * 分页合理化开关
     */
    private Boolean reasonable;
    /**
     * 当为true时，若pageSize为0（或RowBounds的limit=0），不执行分页，返回全部结果
     */
    private Boolean pageSizeZero;
    /**
     * count查询的列名
     */
    private String countColumn;
    /**
     * 排序字段
     */
    private String orderBy;
    /**
     * 是否仅增加排序
     */
    private boolean orderByOnly;
    /**
     * SQL拦截处理
     */
    private BoundSqlBuilder boundSqlHandler;
    /**
     * BoundSql处理链
     */
    private transient BoundSqlBuilder.Chain chain;
    /**
     * 分页实现类，可使用{@link PageAutoDialect}注册的别名，如"mysql"、"oracle"
     */
    private String dialectClass;
    /**
     * 是否在count查询时保留order by排序
     */
    private Boolean keepOrderBy;
    /**
     * 是否在count查询时保留子查询的order by排序
     */
    private Boolean keepSubSelectOrderBy;
    /**
     * 是否启用异步count查询
     */
    private Boolean asyncCount;

    /**
     * 默认构造函数。
     */
    public Page() {
        super();
    }

    /**
     * 构造函数，指定页码和页面大小。
     *
     * @param pageNo   页码（从1开始）
     * @param pageSize 页面大小
     */
    public Page(int pageNo, int pageSize) {
        this(pageNo, pageSize, true, null);
    }

    /**
     * 构造函数，指定页码、页面大小和是否执行count查询。
     *
     * @param pageNo   页码（从1开始）
     * @param pageSize 页面大小
     * @param count    是否执行count查询
     */
    public Page(int pageNo, int pageSize, boolean count) {
        this(pageNo, pageSize, count, null);
    }

    /**
     * 构造函数，指定页码、页面大小、是否执行count查询及合理化开关。
     *
     * @param pageNo     页码（从1开始）
     * @param pageSize   页面大小
     * @param count      是否执行count查询
     * @param reasonable 分页合理化开关
     */
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
     * 构造函数，基于行范围分页。
     *
     * @param rowBounds 行范围数组，0: offset, 1: limit
     * @param count     是否执行count查询
     */
    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
            this.pageNo = 1;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNo = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1]))
                    : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }

    /**
     * 获取创建Page对象的堆栈信息。
     *
     * @return 堆栈信息
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * 获取分页结果集。
     *
     * @return 分页数据列表
     */
    public List<E> getResult() {
        return this;
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
     * @return 当前Page对象
     */
    public Page<E> setPages(int pages) {
        this.pages = pages;
        return this;
    }

    /**
     * 获取末行位置。
     *
     * @return 末行位置
     */
    public long getEndRow() {
        return endRow;
    }

    /**
     * 设置末行位置。
     *
     * @param endRow 末行位置
     * @return 当前Page对象
     */
    public Page<E> setEndRow(long endRow) {
        this.endRow = endRow;
        return this;
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
     * 设置页码，支持合理化处理。
     *
     * @param pageNo 页码
     * @return 当前Page对象
     */
    public Page<E> setPageNo(int pageNo) {
        this.pageNo = ((reasonable != null && reasonable) && pageNo <= 0) ? 1 : pageNo;
        return this;
    }

    /**
     * 获取页面大小。
     *
     * @return 页面大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置页面大小。
     *
     * @param pageSize 页面大小
     * @return 当前Page对象
     */
    public Page<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 获取起始行位置。
     *
     * @return 起始行位置
     */
    public long getStartRow() {
        return startRow;
    }

    /**
     * 设置起始行位置。
     *
     * @param startRow 起始行位置
     * @return 当前Page对象
     */
    public Page<E> setStartRow(long startRow) {
        this.startRow = startRow;
        return this;
    }

    /**
     * 获取总记录数。
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 设置总记录数并计算总页数。
     *
     * @param total 总记录数
     */
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
        if ((reasonable != null && reasonable) && pageNo > pages) {
            if (pages != 0) {
                pageNo = pages;
            }
            calculateStartAndEndRow();
        }
    }

    /**
     * 获取分页合理化开关状态。
     *
     * @return 合理化开关状态
     */
    public Boolean getReasonable() {
        return reasonable;
    }

    /**
     * 设置分页合理化开关。
     *
     * @param reasonable 合理化开关
     * @return 当前Page对象
     */
    public Page<E> setReasonable(Boolean reasonable) {
        if (reasonable == null) {
            return this;
        }
        this.reasonable = reasonable;
        if (this.reasonable && this.pageNo <= 0) {
            this.pageNo = 1;
            calculateStartAndEndRow();
        }
        return this;
    }

    /**
     * 获取pageSizeZero开关状态。
     *
     * @return pageSizeZero开关状态
     */
    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    /**
     * 设置pageSizeZero开关。
     *
     * @param pageSizeZero 当为true时，若pageSize为0则返回全部结果
     * @return 当前Page对象
     */
    public Page<E> setPageSizeZero(Boolean pageSizeZero) {
        if (this.pageSizeZero == null && pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }

    /**
     * 获取排序字段。
     *
     * @return 排序字段
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 设置排序字段，包含SQL注入校验。
     *
     * @param orderBy 排序字段
     * @param <E>     分页数据元素类型
     * @return 当前Page对象
     * @throws PageException 若存在SQL注入风险
     */
    public <E> Page<E> setOrderBy(String orderBy) {
        if (OGNL.check(orderBy)) {
            throw new PageException("order by [" + orderBy + "] has a risk of SQL injection, "
                    + "if you want to avoid SQL injection verification, you can call Page.setUnsafeOrderBy");
        }
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    /**
     * 不安全设置排序字段，需自行确保无SQL注入风险。
     *
     * @param orderBy 排序字段
     * @param <E>     分页数据元素类型
     * @return 当前Page对象
     */
    public <E> Page<E> setUnsafeOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    /**
     * 是否仅增加排序。
     *
     * @return 是否仅增加排序
     */
    public boolean isOrderByOnly() {
        return orderByOnly;
    }

    /**
     * 设置是否仅增加排序。
     *
     * @param orderByOnly 是否仅增加排序
     */
    public void setOrderByOnly(boolean orderByOnly) {
        this.orderByOnly = orderByOnly;
    }

    /**
     * 获取分页实现类。
     *
     * @return 分页实现类
     */
    public String getDialectClass() {
        return dialectClass;
    }

    /**
     * 设置分页实现类。
     *
     * @param dialectClass 分页实现类
     */
    public void setDialectClass(String dialectClass) {
        this.dialectClass = dialectClass;
    }

    /**
     * 获取是否保留count查询的order by排序。
     *
     * @return 是否保留order by排序
     */
    public Boolean getKeepOrderBy() {
        return keepOrderBy;
    }

    /**
     * 设置是否保留count查询的order by排序。
     *
     * @param keepOrderBy 是否保留order by排序
     * @return 当前Page对象
     */
    public Page<E> setKeepOrderBy(Boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    /**
     * 获取是否保留子查询的order by排序。
     *
     * @return 是否保留子查询的order by排序
     */
    public Boolean getKeepSubSelectOrderBy() {
        return keepSubSelectOrderBy;
    }

    /**
     * 设置是否保留子查询的order by排序。
     *
     * @param keepSubSelectOrderBy 是否保留子查询的order by排序
     */
    public void setKeepSubSelectOrderBy(Boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
    }

    /**
     * 获取是否启用异步count查询。
     *
     * @return 是否启用异步count查询
     */
    public Boolean getAsyncCount() {
        return asyncCount;
    }

    /**
     * 设置是否启用异步count查询。
     *
     * @param asyncCount 是否启用异步count查询
     */
    public void setAsyncCount(Boolean asyncCount) {
        this.asyncCount = asyncCount;
    }

    /**
     * 指定使用的分页实现。
     *
     * @param dialect 分页实现类，可使用{@link PageAutoDialect}注册的别名，如"mysql"、"oracle"
     * @return 当前Page对象
     */
    public Page<E> using(String dialect) {
        this.dialectClass = dialect;
        return this;
    }

    /**
     * 计算分页的起始和结束行号。
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNo > 0 ? (this.pageNo - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNo > 0 ? 1 : 0);
    }

    /**
     * 是否执行count查询。
     *
     * @return 是否执行count查询
     */
    public boolean isCount() {
        return this.count;
    }

    /**
     * 设置是否执行count查询。
     *
     * @param count 是否执行count查询
     * @return 当前Page对象
     */
    public Page<E> setCount(boolean count) {
        this.count = count;
        return this;
    }

    /**
     * 设置页码。
     *
     * @param pageNo 页码
     * @return 当前Page对象
     */
    public Page<E> pageNo(int pageNo) {
        this.pageNo = ((reasonable != null && reasonable) && pageNo <= 0) ? 1 : pageNo;
        return this;
    }

    /**
     * 设置页面大小。
     *
     * @param pageSize 分页大小
     * @return 当前Page对象
     */
    public Page<E> pageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
        return this;
    }

    /**
     * 设置是否执行count查询。
     *
     * @param count 是否执行count查询
     * @return 当前Page对象
     */
    public Page<E> count(Boolean count) {
        this.count = count;
        return this;
    }

    /**
     * 设置分页合理化开关。
     *
     * @param reasonable 分页合理化开关
     * @return 当前Page对象
     */
    public Page<E> reasonable(Boolean reasonable) {
        setReasonable(reasonable);
        return this;
    }

    /**
     * 设置pageSizeZero开关。
     *
     * @param pageSizeZero 当为true时，若pageSize为0则返回全部结果
     * @return 当前Page对象
     */
    public Page<E> pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    /**
     * 设置BoundSql拦截器。
     *
     * @param boundSqlHandler 分页拦截器
     * @return 当前Page对象
     */
    public Page<E> boundSqlInterceptor(BoundSqlBuilder boundSqlHandler) {
        setBoundSqlInterceptor(boundSqlHandler);
        return this;
    }

    /**
     * 指定count查询列。
     *
     * @param columnName 列名
     * @return 当前Page对象
     */
    public Page<E> countColumn(String columnName) {
        setCountColumn(columnName);
        return this;
    }

    /**
     * 设置是否保留count查询的order by排序。
     *
     * @param keepOrderBy 是否保留order by排序
     * @return 当前Page对象
     */
    public Page<E> keepOrderBy(boolean keepOrderBy) {
        this.keepOrderBy = keepOrderBy;
        return this;
    }

    /**
     * 检查是否保留count查询的order by排序。
     *
     * @return 是否保留order by排序
     */
    public boolean keepOrderBy() {
        return this.keepOrderBy != null && this.keepOrderBy;
    }

    /**
     * 设置是否保留子查询的order by排序。
     *
     * @param keepSubSelectOrderBy 是否保留子查询的order by排序
     * @return 当前Page对象
     */
    public Page<E> keepSubSelectOrderBy(boolean keepSubSelectOrderBy) {
        this.keepSubSelectOrderBy = keepSubSelectOrderBy;
        return this;
    }

    /**
     * 检查是否保留子查询的order by排序。
     *
     * @return 是否保留子查询的order by排序
     */
    public boolean keepSubSelectOrderBy() {
        return this.keepSubSelectOrderBy != null && this.keepSubSelectOrderBy;
    }

    /**
     * 设置是否启用异步count查询。
     *
     * @param asyncCount 是否启用异步count查询
     * @return 当前Page对象
     */
    public Page<E> asyncCount(boolean asyncCount) {
        this.asyncCount = asyncCount;
        return this;
    }

    /**
     * 启用异步count查询。
     *
     * @return 当前Page对象
     */
    public Page<E> enableAsyncCount() {
        return asyncCount(true);
    }

    /**
     * 禁用异步count查询。
     *
     * @return 当前Page对象
     */
    public Page<E> disableAsyncCount() {
        return asyncCount(false);
    }

    /**
     * 检查是否启用异步count查询。
     *
     * @return 是否启用异步count查询
     */
    public boolean asyncCount() {
        return this.asyncCount != null && this.asyncCount;
    }

    /**
     * 转换为Paginating对象。
     *
     * @return Paginating对象
     */
    public Paginating<E> toPageInfo() {
        return new Paginating<>(this);
    }

    /**
     * 转换分页数据并返回Paginating对象。
     *
     * @param function 数据转换函数
     * @param <T>      转换后数据类型
     * @return Paginating对象
     */
    public <T> Paginating<T> toPageInfo(FunctionX<E, T> function) {
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

    /**
     * 转换为Serialize对象。
     *
     * @return Serialize对象
     */
    public Serialize<E> toPageSerializable() {
        return new Serialize<>(this);
    }

    /**
     * 转换分页数据并返回Serialize对象。
     *
     * @param function 数据转换函数
     * @param <T>      转换后数据类型
     * @return Serialize对象
     */
    public <T> Serialize<T> toPageSerializable(FunctionX<E, T> function) {
        List<T> list = new ArrayList<>(this.size());
        for (E e : this) {
            list.add(function.apply(e));
        }
        Serialize<T> serialize = new Serialize<>(list);
        serialize.setTotal(this.getTotal());
        return serialize;
    }

    /**
     * 执行分页查询。
     *
     * @param select 查询对象
     * @param <E>    分页数据元素类型
     * @return 当前Page对象
     */
    public <E> Page<E> doSelectPage(Querying select) {
        select.doSelect();
        return (Page<E>) this;
    }

    /**
     * 执行分页查询并返回Paginating对象。
     *
     * @param select 查询对象
     * @param <E>    分页数据元素类型
     * @return Paginating对象
     */
    public <E> Paginating<E> doSelectPageInfo(Querying select) {
        select.doSelect();
        return (Paginating<E>) this.toPageInfo();
    }

    /**
     * 执行分页查询并返回Serialize对象。
     *
     * @param select 查询对象
     * @param <E>    分页数据元素类型
     * @return Serialize对象
     */
    public <E> Serialize<E> doSelectPageSerializable(Querying select) {
        select.doSelect();
        return (Serialize<E>) this.toPageSerializable();
    }

    /**
     * 执行count查询。
     *
     * @param select 查询对象
     * @return 总记录数
     */
    public long doCount(Querying select) {
        this.pageSizeZero = true;
        this.pageSize = 0;
        select.doSelect();
        return this.total;
    }

    /**
     * 获取count查询列名。
     *
     * @return count查询列名
     */
    public String getCountColumn() {
        return countColumn;
    }

    /**
     * 设置count查询列名，包含SQL注入校验。
     *
     * @param countColumn 列名
     * @throws PageException 若存在SQL注入风险
     */
    public void setCountColumn(String countColumn) {
        if (!"0".equals(countColumn) && !Symbol.STAR.equals(countColumn) && OGNL.check(countColumn)) {
            throw new PageException("count(" + countColumn + ") has a risk of SQL injection");
        }
        this.countColumn = countColumn;
    }

    /**
     * 获取BoundSql拦截器。
     *
     * @return BoundSql拦截器
     */
    public BoundSqlBuilder getBoundSqlInterceptor() {
        return boundSqlHandler;
    }

    /**
     * 设置BoundSql拦截器。
     *
     * @param boundSqlHandler BoundSql拦截器
     */
    public void setBoundSqlInterceptor(BoundSqlBuilder boundSqlHandler) {
        this.boundSqlHandler = boundSqlHandler;
    }

    /**
     * 获取BoundSql处理链。
     *
     * @return BoundSql处理链
     */
    BoundSqlBuilder.Chain getChain() {
        return chain;
    }

    /**
     * 设置BoundSql处理链。
     *
     * @param chain BoundSql处理链
     */
    void setChain(BoundSqlBuilder.Chain chain) {
        this.chain = chain;
    }

    /**
     * 返回Page对象的字符串表示。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "Page{" + "count=" + count + ", pageNo=" + pageNo + ", pageSize=" + pageSize + ", startRow=" + startRow
                + ", endRow=" + endRow + ", total=" + total + ", pages=" + pages + ", reasonable=" + reasonable
                + ", pageSizeZero=" + pageSizeZero + '}' + super.toString();
    }

    /**
     * 关闭Page对象，清理分页上下文。
     */
    @Override
    public void close() {
        PageContext.clearPage();
    }

}