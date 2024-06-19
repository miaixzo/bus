/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.wrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.core.lang.ansi.Ansi4BitColor;
import org.miaixz.bus.core.lang.ansi.AnsiEncoder;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 允许定制处理程序执行链的工作流,可以注册任何数量的现有或自定义拦截器
 * 对于某些处理程序组,添加常见的预处理行为不需要修改每个处理程序实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GenieWrapperHandler implements HandlerInterceptor {

    /**
     * 获取客户端IP
     * 默认检测的Header:
     *
     * <pre>
     * 1、X-Forwarded-For
     * 2、X-Real-IP
     * 3、Proxy-Client-IP
     * 4、WL-Proxy-Client-IP
     * </pre>
     *
     * <p>
     * otherHeaderNames参数用于自定义检测的Header
     * 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     * </p>
     *
     * @param request          请求对象{@link HttpServletRequest}
     * @param otherHeaderNames 其他自定义头文件，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIP(final HttpServletRequest request, final String... otherHeaderNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ArrayKit.isNotEmpty(otherHeaderNames)) {
            headers = ArrayKit.addAll(headers, otherHeaderNames);
        }

        return getClientIPByHeader(request, headers);
    }

    /**
     * 获取客户端IP
     * headerNames参数用于自定义检测的Header
     * 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param headerNames 自定义头，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIPByHeader(final HttpServletRequest request, final String... headerNames) {
        String ip;
        for (final String header : headerNames) {
            ip = request.getHeader(header);
            if (!NetKit.isUnknown(ip)) {
                return NetKit.getMultistageReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return NetKit.getMultistageReverseProxyIp(ip);
    }

    /**
     * 业务处理器处理请求之前被调用,对用户的request进行处理,若返回值为true,
     * 则继续调用后续的拦截器和目标方法；若返回值为false, 则终止请求；
     * 这里可以加上登录校验,权限拦截、请求限流等
     *
     * @param request  当前的HTTP请求
     * @param response 当前的HTTP响应
     * @param handler  执行的处理程序
     * @return 如果执行链应该继续执行, 则为:true 否则:false
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String method = request.getMethod().toUpperCase();
        this.requestInfo(request, method);
        if (Http.GET.equals(method)
                || Http.POST.equals(method)
                || Http.PATCH.equals(method)
                || Http.PUT.equals(method)) {
            if (request instanceof CacheRequestWrapper) {
                CacheRequestWrapper cacheRequestWrapper = ((CacheRequestWrapper) request);
                Logger.info("==> {}", new String(cacheRequestWrapper.getBody()));
            }
        }
        return true;
    }

    /**
     * 完成请求处理后回调,将调用处理程序执行的任何结果,
     * 因此允许进行适当的资源清理等
     * 注意:只有在拦截器的{@code preHandle} 方法返回{@code true}
     * 与{@code postHandle}方法一样,将在每个方法上调用该方法,
     * 在链中的拦截器的顺序是相反的,所以第一个拦截器是最后调用的
     *
     * @param request   当前的HTTP请求
     * @param response  当前的HTTP响应
     * @param handler   执行的处理程序
     * @param exception 处理程序执行时抛出异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        final String method = request.getMethod();
        if (Http.POST.equals(method)
                || Http.PATCH.equals(method)
                || Http.PUT.equals(method)) {
            if (response instanceof CacheResponseWrapper) {
                CacheResponseWrapper cacheResponseWrapper = ((CacheResponseWrapper) response);
                Logger.info("<== {}", new String(cacheResponseWrapper.getBody()).length());
            }
        }
    }

    /**
     * 拦截处理程序的执行 实际上是在HandlerAdapter之后调用的
     * 调用处理程序,但在DispatcherServlet呈现视图之前
     * 可以通过给定的ModelAndView向视图公开额外的模型对象
     * DispatcherServlet在一个执行链中处理一个处理程序,由
     * 任意数量的拦截器,处理程序本身在最后
     * 使用这种方法,每个拦截器可以对一个执行进行后处理,
     * 按执行链的相反顺序应用
     *
     * @param request      当前的HTTP请求
     * @param response     当前的HTTP响应
     * @param handler      执行的处理程序
     * @param modelAndView 处理程序返回的{code ModelAndView} 也可以是{@code null})
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    /**
     * 请求日志信息
     *
     * @param method  请求类型
     * @param request 网络请求
     */
    private void requestInfo(HttpServletRequest request, String method) {
        String requestMethod = AnsiEncoder.encode(Ansi4BitColor.GREEN, " %s ", method);
        switch (method) {
            case Http.ALL:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.WHITE, " %s ", method);
                break;
            case Http.POST:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.MAGENTA, " %s ", method);
                break;
            case Http.DELETE:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.BLUE, " %s ", method);
                break;
            case Http.PUT:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.RED, " %s ", method);
                break;
            case Http.OPTIONS:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.YELLOW, " %s ", method);
                break;
            case Http.BEFORE:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.BLACK, " %s ", method);
                break;
            case Http.AFTER:
                requestMethod = AnsiEncoder.encode(Ansi4BitColor.CYAN, " %s ", method);
                break;
        }
        Logger.info("{} {} {}", "==>", getClientIP(request), requestMethod, request.getRequestURL().toString());
    }

}
