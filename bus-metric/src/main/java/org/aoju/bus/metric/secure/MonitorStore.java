/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.secure;

import org.aoju.bus.metric.manual.ApiParam;
import org.aoju.bus.metric.manual.ApiSearch;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8++
 */
public interface MonitorStore {

    /**
     * 清空监控数据.如果name不为空，则删除对应的数据。否则删除全部数据
     *
     * @param name    名称
     * @param version 版本
     */
    void clean(String name, String version);

    /**
     * 统计
     *
     * @param param          请求参数
     * @param starTimeMillis 开始时间
     * @param endTimeMillis  结束时间
     * @param argu           方法参数
     * @param result         返回结果
     * @param e              异常
     */
    void stat(ApiParam param, long starTimeMillis, long endTimeMillis, Object argu, Object result, Exception e);

    /**
     * 返回总记录数
     *
     * @param apiSearch 查询对象
     * @return 返回总数
     */
    int getTotal(ApiSearch apiSearch);

    /**
     * 返回结果集
     *
     * @param apiSearch 查询对象
     * @return 返回结果集
     */
    List<MonitorApiInfo> getList(ApiSearch apiSearch);

    /**
     * 处理错误
     *
     * @param param  请求参数
     * @param argu   方法参数
     * @param result 返回结果
     * @param e      异常
     * @param t      监控信息
     */
    void errorHandler(ApiParam param, Object argu, Object result, Exception e, MonitorApiInfo t);

}
