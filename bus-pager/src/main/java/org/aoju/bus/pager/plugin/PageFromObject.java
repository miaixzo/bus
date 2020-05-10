/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.pager.plugin;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.pager.IPage;
import org.aoju.bus.pager.Page;
import org.aoju.bus.pager.PageException;
import org.aoju.bus.pager.reflect.MetaObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 分页参数对象工具类
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public abstract class PageFromObject {

    protected static Boolean hasRequest;
    protected static Class<?> requestClass;
    protected static Method getParameterMap;
    protected static Map<String, String> PARAMS = new HashMap<>(6, 1);

    static {
        try {
            requestClass = Class.forName("javax.servlet.ServletRequest");
            getParameterMap = requestClass.getMethod("getParameterMap", new Class[]{});
            hasRequest = true;
        } catch (Throwable e) {
            hasRequest = false;
        }
        PARAMS.put("pageNum", "pageNum");
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
            throw new PageException("无法获取分页查询参数!");
        }
        if (params instanceof IPage) {
            IPage pageParams = (IPage) params;
            Page page = null;
            if (pageParams.getPageNum() != null && pageParams.getPageSize() != null) {
                page = new Page(pageParams.getPageNum(), pageParams.getPageSize());
            }
            if (isNotEmpty(pageParams.getOrderBy())) {
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
        int pageNum;
        int pageSize;
        org.apache.ibatis.reflection.MetaObject paramsObject = null;
        if (hasRequest && requestClass.isAssignableFrom(params.getClass())) {
            try {
                paramsObject = MetaObject.forObject(getParameterMap.invoke(params, new Object[]{}));
            } catch (Exception e) {
                //忽略
            }
        } else {
            paramsObject = MetaObject.forObject(params);
        }
        if (paramsObject == null) {
            throw new PageException("分页查询参数处理失败!");
        }
        Object orderBy = getParamValue(paramsObject, "orderBy", false);
        boolean hasOrderBy = false;
        if (orderBy != null && orderBy.toString().length() > 0) {
            hasOrderBy = true;
        }
        try {
            Object _pageNum = getParamValue(paramsObject, "pageNum", required);
            Object _pageSize = getParamValue(paramsObject, "pageSize", required);
            if (_pageNum == null || _pageSize == null) {
                if (hasOrderBy) {
                    Page page = new Page();
                    page.setOrderBy(orderBy.toString());
                    page.setOrderByOnly(true);
                    return page;
                }
                return null;
            }
            pageNum = Integer.parseInt(String.valueOf(_pageNum));
            pageSize = Integer.parseInt(String.valueOf(_pageSize));
        } catch (NumberFormatException e) {
            throw new PageException("分页参数不是合法的数字类型!", e);
        }
        Page page = new Page(pageNum, pageSize);
        //count查询
        Object _count = getParamValue(paramsObject, "count", false);
        if (_count != null) {
            page.setCount(Boolean.valueOf(String.valueOf(_count)));
        }
        //排序
        if (hasOrderBy) {
            page.setOrderBy(orderBy.toString());
        }
        //分页合理化
        Object reasonable = getParamValue(paramsObject, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        //查询全部
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
            throw new PageException("分页查询缺少必要的参数:" + PARAMS.get(paramName));
        }
        return value;
    }

    public static void setParams(String params) {
        if (isNotEmpty(params)) {
            String[] ps = params.split("[;|,|&]");
            for (String s : ps) {
                String[] ss = s.split("[=|:]");
                if (ss.length == 2) {
                    PARAMS.put(ss[0], ss[1]);
                }
            }
        }
    }

    public static boolean isEmpty(Object obj) {
        return obj == null || obj.toString().equals(Normal.EMPTY);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

}
