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
package org.miaixz.bus.pager.binding;

import java.util.Properties;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.PageContext;
import org.miaixz.bus.pager.Paging;
import org.miaixz.bus.pager.RowBounds;

/**
 * 分页参数配置类，负责管理和解析分页相关参数。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageParams {

    /**
     * 是否将RowBounds的offset作为页码使用，默认false
     */
    protected boolean offsetAsPageNo = false;
    /**
     * RowBounds是否执行count查询，默认false
     */
    protected boolean rowBoundsWithCount = false;
    /**
     * 当为true且pageSize为0（或RowBounds的limit=0）时，返回全部结果
     */
    protected boolean pageSizeZero = false;
    /**
     * 是否启用分页合理化，默认false
     */
    protected boolean reasonable = false;
    /**
     * 是否支持通过接口参数传递分页参数，默认false
     */
    protected boolean supportMethodsArguments = false;
    /**
     * 默认count查询列，默认为"0"
     */
    protected String countColumn = "0";
    /**
     * count查询时是否保留order by排序
     */
    private boolean keepOrderBy = false;
    /**
     * count查询时是否保留子查询的order by排序
     */
    private boolean keepSubSelectOrderBy = false;
    /**
     * 是否启用异步count查询
     */
    private boolean asyncCount = false;

    /**
     * 获取分页参数对象。
     *
     * @param parameterObject 查询参数对象
     * @param rowBounds       MyBatis RowBounds对象
     * @return 分页对象，若无分页参数则返回null
     */
    public Page getPage(Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        Page page = PageContext.getLocalPage();
        if (page == null) {
            if (rowBounds != org.apache.ibatis.session.RowBounds.DEFAULT) {
                if (offsetAsPageNo) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(new int[] { rowBounds.getOffset(), rowBounds.getLimit() }, rowBoundsWithCount);
                    page.setReasonable(false); // offsetAsPageNo=false时禁用合理化
                }
                if (rowBounds instanceof RowBounds) {
                    RowBounds pageRowBounds = (RowBounds) rowBounds;
                    page.setCount(pageRowBounds.getCount() == null || pageRowBounds.getCount());
                }
            } else if (parameterObject instanceof Paging || supportMethodsArguments) {
                try {
                    page = PageObject.getPageFromObject(parameterObject, false);
                } catch (Exception e) {
                    return null;
                }
            }
            if (page == null) {
                return null;
            }
            PageContext.setLocalPage(page);
        }
        if (page.getReasonable() == null) {
            page.setReasonable(reasonable);
        }
        if (page.getPageSizeZero() == null) {
            page.setPageSizeZero(pageSizeZero);
        }
        if (page.getKeepOrderBy() == null) {
            page.setKeepOrderBy(keepOrderBy);
        }
        if (page.getKeepSubSelectOrderBy() == null) {
            page.setKeepSubSelectOrderBy(keepSubSelectOrderBy);
        }
        return page;
    }

    /**
     * 设置分页相关配置属性。
     *
     * @param properties 配置属性
     */
    public void setProperties(Properties properties) {
        this.offsetAsPageNo = Boolean.parseBoolean(properties.getProperty("offsetAsPageNo"));
        this.rowBoundsWithCount = Boolean.parseBoolean(properties.getProperty("rowBoundsWithCount"));
        this.pageSizeZero = Boolean.parseBoolean(properties.getProperty("pageSizeZero"));
        this.reasonable = Boolean.parseBoolean(properties.getProperty("reasonable"));
        this.supportMethodsArguments = Boolean.parseBoolean(properties.getProperty("supportMethodsArguments"));
        String countColumn = properties.getProperty("countColumn");
        if (StringKit.isNotEmpty(countColumn)) {
            this.countColumn = countColumn;
        }
        PageObject.setParams(properties.getProperty("params"));
        this.keepOrderBy = Boolean.parseBoolean(properties.getProperty("keepOrderBy"));
        this.keepSubSelectOrderBy = Boolean.parseBoolean(properties.getProperty("keepSubSelectOrderBy"));
        this.asyncCount = Boolean.parseBoolean(properties.getProperty("asyncCount"));
    }

    /**
     * 是否将offset作为页码使用。
     *
     * @return 是否启用offset作为页码
     */
    public boolean isOffsetAsPageNo() {
        return offsetAsPageNo;
    }

    /**
     * RowBounds是否执行count查询。
     *
     * @return 是否执行count查询
     */
    public boolean isRowBoundsWithCount() {
        return rowBoundsWithCount;
    }

    /**
     * 是否在pageSize为0时返回全部结果。
     *
     * @return 是否启用pageSizeZero
     */
    public boolean isPageSizeZero() {
        return pageSizeZero;
    }

    /**
     * 是否启用分页合理化。
     *
     * @return 是否启用合理化
     */
    public boolean isReasonable() {
        return reasonable;
    }

    /**
     * 是否支持接口参数传递分页参数。
     *
     * @return 是否支持接口参数
     */
    public boolean isSupportMethodsArguments() {
        return supportMethodsArguments;
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
     * 是否启用异步count查询。
     *
     * @return 是否启用异步count
     */
    public boolean isAsyncCount() {
        return asyncCount;
    }

}