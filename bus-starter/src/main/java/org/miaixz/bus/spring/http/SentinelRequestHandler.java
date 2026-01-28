/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.ansi.Ansi4BitColor;
import org.miaixz.bus.core.lang.ansi.AnsiEncoder;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * A request security sentinel interceptor that provides full-lifecycle security protection and auditing for API
 * requests.
 *
 * <p>
 * <b>Performance Optimizations</b>
 * <ul>
 * <li>Uses {@link MutableRequestWrapper} to cache the request body, solving the issue of an InputStream only being
 * readable once.</li>
 * <li>Limits the length of the logged response body (default 150 characters) to prevent memory overflow with large
 * responses.</li>
 * <li>Supports asynchronous logging to reduce performance impact on the main thread.</li>
 * </ul>
 *
 * <p>
 * <b>Security Best Practices</b>
 * <ol>
 * <li>Enable all security modules in a production environment.</li>
 * <li>Regularly audit security logs to analyze abnormal patterns.</li>
 * <li>Use in conjunction with a WAF (Web Application Firewall) for defense-in-depth.</li>
 * <li>Implement stricter security policies for sensitive APIs.</li>
 * <li>Periodically update security policies to counter new attack methods.</li>
 * </ol>
 *
 * <p>
 * <b>Extensibility</b>
 * <p>
 * This class is designed with an extensible architecture, supporting the following:
 * <ul>
 * <li>Custom security policy implementations.</li>
 * <li>Pluggable security modules.</li>
 * <li>Custom log formats and output destinations.</li>
 * <li>Integration with third-party security services.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SentinelRequestHandler implements HandlerInterceptor {

    /**
     * Called before the target handler is executed. This method can be used for pre-processing tasks like
     * authentication, authorization, and rate limiting.
     *
     * @param request  the current HTTP request.
     * @param response the current HTTP response.
     * @param handler  the handler to be executed.
     * @return {@code true} to continue the execution chain, or {@code false} to abort it.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Log to confirm the interceptor is called.
        final String method = request.getMethod().toUpperCase();
        this.requestInfo(request, method);

        // Handle logging based on the request method type.
        if (HTTP.POST.equals(method) || HTTP.PATCH.equals(method) || HTTP.PUT.equals(method)) {
            // For methods with a request body, log the body if it's a MutableRequestWrapper.
            if (request instanceof MutableRequestWrapper) {
                String requestBody = new String(((MutableRequestWrapper) request).getBody())
                        .replaceAll("\\s+", Normal.EMPTY);
                Logger.info(true, "Sentinel", "Body: {}", requestBody);
            } else {
                // If not wrapped, log the request parameters.
                requestParameters(request);
            }
        } else {
            // For GET and other methods, log the request parameters.
            requestParameters(request);
        }

        return true;
    }

    /**
     * Called after the request is completed and the view is rendered. This method is suitable for final logging.
     *
     * @param request   the current HTTP request.
     * @param response  the current HTTP response.
     * @param handler   the handler that was executed.
     * @param exception any exception thrown on handler execution, or null if none.
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        if (response instanceof MutableResponseWrapper mutableResponseWrapper) {
            String responseBody = new String(mutableResponseWrapper.getBody());
            // Log only a portion of the response body to avoid overly large logs.
            String logBody = responseBody.length() > 150
                    ? responseBody.substring(0, 150) + "... [truncated, total length: " + responseBody.length() + "]"
                    : responseBody;
            Logger.info(
                    false,
                    "Sentinel",
                    "Response (length: {}): {}",
                    mutableResponseWrapper.getBody().length,
                    logBody);
        } else {
            Logger.info(false, "Sentinel", "Status: {}", response.getStatus());
        }
    }

    /**
     * Called after the handler is executed but before the view is rendered. This allows for modifying the
     * {@link ModelAndView} before it is presented to the user.
     *
     * @param request      the current HTTP request.
     * @param response     the current HTTP response.
     * @param handler      the handler that was executed.
     * @param modelAndView the {@code ModelAndView} that the handler returned (can be {@code null}).
     */
    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {
        Logger.info(false, "Sentinel", "URI: {}", request.getRequestURI());
    }

    /**
     * Logs the request parameters and headers.
     *
     * @param request The HTTP request.
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
            Logger.info(true, "Sentinel", "Body: {}", params);
        }

        // Log request headers.
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            Map<String, String> headers = new HashMap<>();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            Logger.debug(true, "Sentinel", "Headers: {}", headers);
        }
    }

    /**
     * Gets the client's IP address by inspecting common proxy headers.
     * <p>
     * Default headers checked:
     *
     * <pre>
     * 1. X-Forwarded-For
     * 2. X-Real-IP
     * 3. Proxy-Client-IP
     * 4. WL-Proxy-Client-IP
     * </pre>
     * <p>
     * Note: To prevent IP spoofing, ensure these headers are properly configured and managed by your proxy server
     * (e.g., Nginx).
     *
     * @param request          The {@link HttpServletRequest} object.
     * @param otherHeaderNames Additional custom header names to check.
     * @return The client's IP address.
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
     * Gets the client's IP address by inspecting a custom list of headers.
     *
     * @param request     The {@link HttpServletRequest} object.
     * @param headerNames The custom header names to check.
     * @return The client's IP address.
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
     * Logs basic request information, including IP, method, and URL, with color-coding for the method.
     *
     * @param request The web request.
     * @param method  The request method type.
     */
    public void requestInfo(HttpServletRequest request, String method) {
        // Define a map of HTTP methods to colors.
        Map<String, Ansi4BitColor> methodColorMap = new HashMap<>();
        methodColorMap.put(HTTP.GET, Ansi4BitColor.GREEN);
        methodColorMap.put(HTTP.POST, Ansi4BitColor.MAGENTA);
        methodColorMap.put(HTTP.DELETE, Ansi4BitColor.BLUE);
        methodColorMap.put(HTTP.PUT, Ansi4BitColor.RED);
        methodColorMap.put(HTTP.OPTIONS, Ansi4BitColor.YELLOW);
        methodColorMap.put(HTTP.ALL, Ansi4BitColor.WHITE);
        methodColorMap.put(HTTP.BEFORE, Ansi4BitColor.BLACK);
        methodColorMap.put(HTTP.AFTER, Ansi4BitColor.CYAN);

        // Get the color for the method, defaulting to green.
        Ansi4BitColor color = methodColorMap.getOrDefault(method, Ansi4BitColor.GREEN);
        // Format the HTTP method with an ANSI color.
        String requestMethod = AnsiEncoder.encode(color, method);
        // Log the request information.
        Logger.info(
                true,
                "Sentinel",
                "Request: {ip={}, method={}, url={}}",
                getClientIP(request),
                requestMethod,
                request.getRequestURL().toString());
    }

}
