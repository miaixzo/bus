/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.handler;

import org.aoju.bus.metric.magic.ApiMeta;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器，原理同springmvc拦截器
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public interface ApiHandler {

    /**
     * 预处理回调方法，在方法调用前执行。返回false不继续向下执行，此时可使用response返回错误信息
     *
     * @param request    网络请求
     * @param response   响应信息
     * @param serviceObj service类
     * @param args       方法参数
     * @return 返回false不继续向下执行，此时可使用response返回错误信息
     * @throws Exception 异常
     */
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object serviceObj, Object args)
            throws Exception;

    /**
     * 接口方法执行完后调用此方法。
     *
     * @param request    网络请求
     * @param response   响应信息
     * @param serviceObj service类
     * @param args       参数
     * @param result     方法返回结果
     * @throws Exception 异常
     */
    void postHandle(HttpServletRequest request, HttpServletResponse response, Object serviceObj, Object args,
                    Object result) throws Exception;

    /**
     * 结果包装完成后执行
     *
     * @param request    网络请求
     * @param response   响应信息
     * @param serviceObj service类
     * @param args       参数
     * @param result     最终结果，被包装过
     * @param e          异常
     * @throws Exception 异常
     */
    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object serviceObj, Object args,
                         Object result, Exception e) throws Exception;

    /**
     * 匹配拦截器，返回true则执行该拦截器，否则忽略该拦截器
     *
     * @param apiMeta 接口信息
     * @return 返回true，使用该拦截器，返回false跳过不使用。
     */
    boolean match(ApiMeta apiMeta);

}
