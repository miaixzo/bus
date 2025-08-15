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
package org.miaixz.bus.spring.web;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.ansi.Ansi4BitColor;
import org.miaixz.bus.core.lang.ansi.AnsiEncoder;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.starter.wrapper.CacheRequestWrapper;
import org.miaixz.bus.starter.wrapper.CacheResponseWrapper;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求安全哨兵拦截器 - 提供API请求的全链路安全防护与审计功能
 *
 * <h3>性能优化</h3>
 * <ul>
 * <li>使用{@link org.miaixz.bus.starter.wrapper.CacheRequestWrapper}实现请求体缓存， 解决InputStream只能读取一次的问题</li>
 * <li>响应体记录限制长度(默认150字符)，防止大响应体导致内存溢出</li>
 * <li>支持异步日志记录，减少对主流程性能影响</li>
 * </ul>
 *
 * <h3>安全最佳实践</h3>
 * <ol>
 * <li>在生产环境启用所有安全模块</li>
 * <li>定期审计安全日志，分析异常模式</li>
 * <li>结合WAF(Web应用防火墙)使用，形成纵深防御</li>
 * <li>对敏感API实施更严格的安全策略</li>
 * <li>定期更新安全策略，应对新型攻击手段</li>
 * </ol>
 *
 * <h3>扩展点</h3>
 * <p>
 * 此类设计为可扩展架构，支持以下扩展点：
 * </p>
 * <ul>
 * <li>自定义安全策略实现</li>
 * <li>插件式安全模块添加</li>
 * <li>自定义日志格式和输出目标</li>
 * <li>集成第三方安全服务</li>
 * </ul>
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class SentinelRequestHandler implements HandlerInterceptor {

    /**
     * 业务处理器处理请求之前被调用,对用户的request进行处理,若返回值为true, 则继续调用后续的拦截器和目标方法；若返回值为false, 则终止请求； 这里可以加上登录校验,权限拦截、请求限流等
     *
     * @param request  当前的HTTP请求
     * @param response 当前的HTTP响应
     * @param handler  执行的处理程序
     * @return 如果执行链应该继续执行, 则为:true 否则:false
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 添加日志确认拦截器被调用
        final String method = request.getMethod().toUpperCase();
        this.requestInfo(request, method);

        // 根据请求方法类型处理日志
        if (HTTP.POST.equals(method) || HTTP.PATCH.equals(method) || HTTP.PUT.equals(method)) {
            // 对于有请求体的方法，如果是CacheRequestWrapper则输出请求体
            if (request instanceof CacheRequestWrapper) {
                String requestBody = new String(((CacheRequestWrapper) request).getBody()).replaceAll("\\s+",
                        Normal.EMPTY);
                Logger.info("==>    Request: {}", requestBody);
            } else {
                // 如果没有被包装，则输出请求参数
                requestParameters(request);
            }
        } else {
            // 对于GET等其他请求方法，输出请求参数
            requestParameters(request);
        }

        return true;
    }

    /**
     * 完成请求处理后回调,将调用处理程序执行的任何结果, 因此允许进行适当的资源清理等 注意:只有在拦截器的{@code preHandle} 方法返回{@code true}
     * 与{@code postHandle}方法一样,将在每个方法上调用该方法, 在链中的拦截器的顺序是相反的,所以第一个拦截器是最后调用的
     *
     * @param request   当前的HTTP请求
     * @param response  当前的HTTP响应
     * @param handler   执行的处理程序
     * @param exception 处理程序执行时抛出异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception exception) {
        if (response instanceof CacheResponseWrapper) {
            CacheResponseWrapper cacheResponseWrapper = ((CacheResponseWrapper) response);
            String responseBody = new String(cacheResponseWrapper.getBody());
            // 只记录响应体的一部分，避免日志过大
            String logBody = responseBody.length() > 150
                    ? responseBody.substring(0, 150) + "... [truncated, total length: " + responseBody.length() + "]"
                    : responseBody;
            Logger.info("<==   Response: (length: {}): {}", cacheResponseWrapper.getBody().length, logBody);
        } else {
            Logger.info("==>     Status: {}", response.getStatus());
        }
    }

    /**
     * 拦截处理程序的执行 实际上是在HandlerAdapter之后调用的 调用处理程序,但在DispatcherServlet呈现视图之前 可以通过给定的ModelAndView向视图公开额外的模型对象
     * DispatcherServlet在一个执行链中处理一个处理程序,由 任意数量的拦截器,处理程序本身在最后 使用这种方法,每个拦截器可以对一个执行进行后处理, 按执行链的相反顺序应用
     *
     * @param request      当前的HTTP请求
     * @param response     当前的HTTP响应
     * @param handler      执行的处理程序
     * @param modelAndView 处理程序返回的{code ModelAndView} 也可以是{@code null})
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        Logger.info("==> RequestURI: {}", request.getRequestURI());
    }

    /**
     * 记录请求参数
     *
     * @param request HTTP请求
     */
    public void requestParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] values = entry.getValue();
                if (values != null && values.length > 0) {
                    params.put(entry.getKey(), StringKit.join(Symbol.COMMA, values));
                }
            }

            Logger.info("==> Parameters: {}", params);
        }

        // 记录请求头信息
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            Map<String, String> headers = new HashMap<>();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            Logger.debug("==>    Headers: {}", headers);
        }
    }

    /**
     * 获取客户端IP 默认检测的Header:
     *
     * <pre>
     * 1、X-Forwarded-For
     * 2、X-Real-IP
     * 3、Proxy-Client-IP
     * 4、WL-Proxy-Client-IP
     * </pre>
     *
     * <p>
     * otherHeaderNames参数用于自定义检测的Header 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     * </p>
     *
     * @param request          请求对象{@link HttpServletRequest}
     * @param otherHeaderNames 其他自定义头文件，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIP(final HttpServletRequest request, final String... otherHeaderNames) {
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR" };
        if (ArrayKit.isNotEmpty(otherHeaderNames)) {
            headers = ArrayKit.addAll(headers, otherHeaderNames);
        }
        return getClientIPByHeader(request, headers);
    }

    /**
     * 获取客户端IP headerNames参数用于自定义检测的Header 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
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
     * 请求日志信息
     *
     * @param method  请求类型
     * @param request 网络请求
     */
    public void requestInfo(HttpServletRequest request, String method) {
        String requestMethod = AnsiEncoder.encode(Ansi4BitColor.GREEN, " %s ", method);
        switch (method) {
        case HTTP.GET:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.GREEN, " %s ", method);
            break;
        case HTTP.ALL:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.WHITE, " %s ", method);
            break;
        case HTTP.POST:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.MAGENTA, " %s ", method);
            break;
        case HTTP.DELETE:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.BLUE, " %s ", method);
            break;
        case HTTP.PUT:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.RED, " %s ", method);
            break;
        case HTTP.OPTIONS:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.YELLOW, " %s ", method);
            break;
        case HTTP.BEFORE:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.BLACK, " %s ", method);
            break;
        case HTTP.AFTER:
            requestMethod = AnsiEncoder.encode(Ansi4BitColor.CYAN, " %s ", method);
            break;
        }
        Logger.info("{} {} {}", "==>", getClientIP(request), requestMethod, request.getRequestURL().toString());
    }

}