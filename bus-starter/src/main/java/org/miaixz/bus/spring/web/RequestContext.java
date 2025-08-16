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

import jakarta.servlet.http.HttpServletRequest;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;

import java.util.Enumeration;
import java.util.Map;

/**
 * 请求上下文封装类。
 */
public class RequestContext {

    /**
     * HTTP 请求对象
     */
    private final HttpServletRequest request;
    /**
     * 请求 ID
     */
    private final String requestId;
    /**
     * 请求头映射（延迟初始化）
     */
    private Map<String, String> headers;
    /**
     * 请求参数映射（延迟初始化）
     */
    private Map<String, String> parameters;

    /**
     * 构造一个 RequestContext 对象。
     *
     * @param request HTTP 请求对象
     */
    public RequestContext(HttpServletRequest request) {
        this.request = request;
        this.requestId = generateRequestId(request);
    }

    /**
     * 生成请求 ID。
     *
     * @param request HTTP 请求
     * @return 生成的请求 ID
     */
    private String generateRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 1. 优先使用请求属性中的 ID
        String requestId = (String) request.getAttribute("requestId");
        if (requestId != null) {
            return requestId;
        }
        // 2. 使用会话 ID（如果有）
        String sessionId = request.getRequestedSessionId();
        if (sessionId != null) {
            return request.getRequestURI() + ":" + sessionId;
        }
        // 3. 使用请求 URI 和方法组合
        return request.getMethod() + ":" + request.getRequestURI();
    }

    /**
     * 获取 HTTP 请求对象。
     *
     * @return HTTP 请求对象
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * 获取请求 ID。
     *
     * @return 请求 ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 获取请求头映射（延迟初始化）。
     *
     * @return 请求头键值对映射
     */
    public Map<String, String> getHeaders() {
        if (headers == null) {
            headers = buildHeaders();
        }
        return headers;
    }

    /**
     * 构建请求头映射。
     *
     * @return 请求头键值对映射
     */
    private Map<String, String> buildHeaders() {
        if (request == null) {
            return new CaseInsensitiveMap();
        }
        Map<String, String> headerMap = new CaseInsensitiveMap();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headerMap.put(name, value);
        }
        return headerMap;
    }

    /**
     * 获取请求参数映射（延迟初始化）。
     *
     * @return 请求参数键值对映射
     */
    public Map<String, String> getParameters() {
        if (parameters == null) {
            parameters = buildParameters();
        }
        return parameters;
    }

    /**
     * 构建请求参数映射。
     *
     * @return 请求参数键值对映射
     */
    private Map<String, String> buildParameters() {
        if (request == null) {
            return new CaseInsensitiveMap();
        }
        Map<String, String> parameterMap = new CaseInsensitiveMap();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getParameter(name);
            parameterMap.put(name, value);
        }
        return parameterMap;
    }

}
