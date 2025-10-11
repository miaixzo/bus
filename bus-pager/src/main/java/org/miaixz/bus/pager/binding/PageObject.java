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
package org.miaixz.bus.pager.binding;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.Paging;

/**
 * Utility class for handling pagination parameter objects. This class provides methods to extract pagination
 * information from various parameter types, including {@link Paging} objects and objects that can be introspected via
 * MyBatis's MetaObject.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PageObject {

    /**
     * Indicates whether the application has access to `jakarta.servlet.ServletRequest`.
     */
    protected static Boolean hasRequest;
    /**
     * The `jakarta.servlet.ServletRequest` class, if available.
     */
    protected static Class<?> requestClass;
    /**
     * The `getParameterMap` method of `jakarta.servlet.ServletRequest`, if available.
     */
    protected static Method getParameterMap;
    /**
     * A map storing parameter name mappings for pagination properties. Keys are common parameter names (e.g.,
     * "pageNo"), values are the actual property names.
     */
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
     * Extracts pagination parameters from an object and creates a {@link Page} instance. It supports {@link Paging}
     * objects, `jakarta.servlet.ServletRequest` objects, and generic objects that can be introspected by MyBatis's
     * MetaObject.
     *
     * @param <T>      the type of elements in the paginated data
     * @param params   the parameter object from which to extract pagination information
     * @param required if true, throws a {@link PageException} if required pagination parameters are missing
     * @return a {@link Page} object configured with the extracted parameters, or null if no pagination parameters are
     *         found and not required
     * @throws PageException if `params` is null, or if required parameters are missing, or if pagination parameters are
     *                       not valid numbers
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
                // Ignore exception, proceed with other methods
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
        // count query
        Object _count = getParamValue(paramsObject, "count", false);
        if (_count != null) {
            page.setCount(Boolean.valueOf(String.valueOf(_count)));
        }
        // order by
        if (hasOrderBy) {
            page.setOrderBy(orderBy.toString());
        }
        // pagination reasonableness
        Object reasonable = getParamValue(paramsObject, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        // query all
        Object pageSizeZero = getParamValue(paramsObject, "pageSizeZero", false);
        if (pageSizeZero != null) {
            page.setPageSizeZero(Boolean.valueOf(String.valueOf(pageSizeZero)));
        }
        return page;
    }

    /**
     * Retrieves a parameter value from a {@link org.apache.ibatis.reflection.MetaObject}.
     *
     * @param paramsObject the MetaObject representing the parameter object
     * @param paramName    the name of the parameter to retrieve (e.g., "pageNo", "pageSize")
     * @param required     if true, throws a {@link PageException} if the parameter is not found
     * @return the value of the parameter, or null if not found and not required
     * @throws PageException if the parameter is required but not found
     */
    protected static Object getParamValue(
            org.apache.ibatis.reflection.MetaObject paramsObject,
            String paramName,
            boolean required) {
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

    /**
     * Sets custom parameter name mappings for pagination properties. The input string should be a semicolon, comma, or
     * ampersand-separated list of key-value pairs, where keys are common parameter names and values are the actual
     * property names. Example: "pageNo=pageNum;pageSize=limit"
     *
     * @param params a string containing custom parameter mappings
     */
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
