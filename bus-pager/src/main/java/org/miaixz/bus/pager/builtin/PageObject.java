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
package org.miaixz.bus.pager.builtin;

import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.Paging;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 分页参数对象工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PageObject {

    /**
     * request获取方法
     */
    protected static Boolean hasRequest;
    protected static Class<?> requestClass;
    protected static Method getParameterMap;
    protected static Map<String, String> PARAMS = new HashMap<>(6, 1);

    static {
        try {
            requestClass = Class.forName("jakarta.servlet.ServletRequest");
            getParameterMap = requestClass.getMethod("getParameterMap");
            hasRequest = true;
        } catch (Throwable e) {
            hasRequest = false;
        }
        PARAMS.put("pageNo", "pageNo");
        PARAMS.put("pageSize", "pageSize");
        PARAMS.put("count", "countSql");
        PARAMS.put("orderBy", "orderBy");
        PARAMS.put("reasonable", "reasonable");
        PARAMS.put("pageSizeZero", "pageSizeZero");
    }

    /**
     * 对象中获取分页参数
     *
     * @param <T>      对象
     * @param params   参数
     * @param required 是否必须
     * @return 结果
     */
    public static <T> Page<T> getPageFromObject(Object params, boolean required) {
        if (params == null) {
            throw new PageException("unable to get paginated query parameters!");
        }
        if (params instanceof Paging) {
            Paging pageParams = (Paging) params;
            Page page = null;
            if (pageParams.getPageNo() != null && pageParams.getPageSize() != null) {
                page = new Page(pageParams.getPageNo(), pageParams.getPageSize());
            }
            if (StringKit.isNotEmpty(pageParams.getOrderBy())) {
                if (page != null) {
                    page.setOrderBy(pageParams.getOrderBy());
                } else {
                    page = new Page();
                    page.setOrderBy(pageParams.getOrderBy());
                    page.setOrderByOnly(true);
                }
            }
            return page;
        }
        int pageNo;
        int pageSize;
        org.apache.ibatis.reflection.MetaObject paramsObject = null;
        if (hasRequest && requestClass.isAssignableFrom(params.getClass())) {
            try {
                paramsObject = MetaObject.forObject(getParameterMap.invoke(params));
            } catch (Exception e) {
                //忽略
            }
        } else {
            paramsObject = MetaObject.forObject(params);
        }
        if (paramsObject == null) {
            throw new PageException("The pagination query parameter failed to be processed!");
        }
        Object orderBy = getParamValue(paramsObject, "orderBy", false);
        boolean hasOrderBy = false;
        if (orderBy != null && orderBy.toString().length() > 0) {
            hasOrderBy = true;
        }
        try {
            Object _pageNo = getParamValue(paramsObject, "pageNo", required);
            Object _pageSize = getParamValue(paramsObject, "pageSize", required);
            if (_pageNo == null || _pageSize == null) {
                if (hasOrderBy) {
                    Page page = new Page();
                    page.setOrderBy(orderBy.toString());
                    page.setOrderByOnly(true);
                    return page;
                }
                return null;
            }
            pageNo = Integer.parseInt(String.valueOf(_pageNo));
            pageSize = Integer.parseInt(String.valueOf(_pageSize));
        } catch (NumberFormatException e) {
            throw new PageException("pagination parameters are not a valid number type!", e);
        }
        Page page = new Page(pageNo, pageSize);
        // count查询
        Object _count = getParamValue(paramsObject, "count", false);
        if (_count != null) {
            page.setCount(Boolean.valueOf(String.valueOf(_count)));
        }
        // 排序
        if (hasOrderBy) {
            page.setOrderBy(orderBy.toString());
        }
        // 分页合理化
        Object reasonable = getParamValue(paramsObject, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        // 查询全部
        Object pageSizeZero = getParamValue(paramsObject, "pageSizeZero", false);
        if (pageSizeZero != null) {
            page.setPageSizeZero(Boolean.valueOf(String.valueOf(pageSizeZero)));
        }
        return page;
    }

    /**
     * 从对象中取参数
     *
     * @param paramsObject 参数
     * @param paramName    参数名
     * @param required     是否必须
     * @return 结果
     */
    protected static Object getParamValue(org.apache.ibatis.reflection.MetaObject paramsObject, String paramName, boolean required) {
        Object value = null;
        if (paramsObject.hasGetter(PARAMS.get(paramName))) {
            value = paramsObject.getValue(PARAMS.get(paramName));
        }
        if (value != null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            if (values.length == 0) {
                value = null;
            } else {
                value = values[0];
            }
        }
        if (required && value == null) {
            throw new PageException("Paginated queries are missing the necessary parameters:" + PARAMS.get(paramName));
        }
        return value;
    }

    public static void setParams(String params) {
        if (StringKit.isNotEmpty(params)) {
            String[] ps = params.split("[;|,|&]");
            for (String s : ps) {
                String[] ss = s.split("[=|:]");
                if (ss.length == 2) {
                    PARAMS.put(ss[0], ss[1]);
                }
            }
        }
    }

}
