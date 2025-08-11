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
package org.miaixz.bus.vortex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.provider.JsonProvider;
import org.miaixz.bus.vortex.provider.XmlProvider;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

/**
 * 数据格式枚举，定义支持的响应数据格式及其相关属性，并提供日志记录功能。
 * <p>
 * 该枚举类用于标识响应数据的格式（如 XML、JSON、PDF、文件流），每个格式关联特定的数据提供者和媒体类型。 同时提供静态方法用于记录不同级别的日志（错误、警告、调试等）以及请求的开始和结束日志。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Format {

    /**
     * XML 格式，表示响应数据以 XML 格式输出。
     */
    XML(new XmlProvider(), MediaType.parseMediaType(MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")),

    /**
     * JSON 格式，表示响应数据以 JSON 格式输出。
     */
    JSON(new JsonProvider(), MediaType.APPLICATION_JSON),

    /**
     * PDF 格式，表示响应数据以 PDF 格式输出。
     */
    PDF,

    /**
     * 二进制文件流，表示响应数据以文件流形式输出。
     */
    BINARY;

    /**
     * 数据格式的提供者，用于处理特定格式的序列化或反序列化。
     * <p>
     * 例如，XML 格式使用 {@link XmlProvider}，JSON 格式使用 {@link JsonProvider}。 对于 {@link #PDF} 和 {@link #BINARY}，该字段为
     * {@code null}。
     * </p>
     */
    private Provider provider;

    /**
     * 对应的 HTTP 媒体类型。
     * <p>
     * 定义响应内容的 MIME 类型，例如 XML 对应 {@code application/xml;charset=UTF-8}， JSON 对应 {@code application/json}。对于 {@link #PDF}
     * 和 {@link #BINARY}，该字段为 {@code null}。
     * </p>
     */
    private MediaType mediaType;

    /**
     * 记录错误级别的日志。
     * <p>
     * 使用 {@link Logger#error(String, Object...)} 记录错误日志，包含跟踪 ID、HTTP 方法、请求路径、操作描述和详细信息。
     * </p>
     *
     * @param exchange  {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @param operation 操作描述，用于标识日志的操作类型
     * @param message   详细的错误信息
     */
    public static void error(ServerWebExchange exchange, String operation, String message) {
        Logger.error("{}[{}] [{}] [{}] - {}", getTraceId(exchange), getMethod(exchange), getPath(exchange), operation,
                message);
    }

    /**
     * 记录警告级别的日志。
     * <p>
     * 使用 {@link Logger#warn(String, Object...)} 记录警告日志，包含跟踪 ID、HTTP 方法、请求路径、操作描述和详细信息。
     * </p>
     *
     * @param exchange  {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @param operation 操作描述，用于标识日志的操作类型
     * @param message   详细的警告信息
     */
    public static void warn(ServerWebExchange exchange, String operation, String message) {
        Logger.warn("{}[{}] [{}] [{}] - {}", getTraceId(exchange), getMethod(exchange), getPath(exchange), operation,
                message);
    }

    /**
     * 记录信息级别的日志。
     * <p>
     * 使用 {@link Logger#info(String, Object...)} 记录信息日志，包含跟踪 ID、HTTP 方法、请求路径、操作描述和详细信息。
     * </p>
     *
     * @param exchange  {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @param operation 操作描述，用于标识日志的操作类型
     * @param message   详细的信息
     */
    public static void info(ServerWebExchange exchange, String operation, String message) {
        Logger.info("{}[{}] [{}] [{}] - {}", getTraceId(exchange), getMethod(exchange), getPath(exchange), operation,
                message);
    }

    /**
     * 记录调试级别的日志。
     * <p>
     * 使用 {@link Logger#debug(String, Object...)} 记录调试日志，包含跟踪 ID、HTTP 方法、请求路径、操作描述和详细信息。
     * </p>
     *
     * @param exchange  {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @param operation 操作描述，用于标识日志的操作类型
     * @param message   详细的调试信息
     */
    public static void debug(ServerWebExchange exchange, String operation, String message) {
        Logger.debug("{}[{}] [{}] [{}] - {}", getTraceId(exchange), getMethod(exchange), getPath(exchange), operation,
                message);
    }

    /**
     * 记录跟踪级别的日志。
     * <p>
     * 使用 {@link Logger#trace(String, Object...)} 记录跟踪日志，包含跟踪 ID、HTTP 方法、请求路径、操作描述和详细信息。
     * </p>
     *
     * @param exchange  {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @param operation 操作描述，用于标识日志的操作类型
     * @param message   详细的跟踪信息
     */
    public static void trace(ServerWebExchange exchange, String operation, String message) {
        Logger.trace("{}[{}] [{}] [{}] - {}", getTraceId(exchange), getMethod(exchange), getPath(exchange), operation,
                message);
    }

    /**
     * 记录请求开始的日志。
     * <p>
     * 记录请求的开始信息，包括 HTTP 方法、请求路径和查询参数。 如果 {@code exchange} 或上下文为 {@code null}，则直接返回，不记录日志。
     * </p>
     *
     * @param exchange {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     */
    public static void requestStart(ServerWebExchange exchange) {
        if (exchange == null) {
            return;
        }
        Context context = Context.get(exchange);
        if (context == null) {
            return;
        }
        String path = getPath(exchange);
        String method = getMethod(exchange);
        String queryString = exchange.getRequest().getQueryParams().toSingleValueMap().toString();
        info(exchange, "REQUEST_START", String.format("Method: %s, Path: %s, Query: %s", method, path, queryString));
    }

    /**
     * 记录请求结束的日志。
     * <p>
     * 记录请求的结束信息，包括 HTTP 方法、请求路径、响应状态码和执行时间。 如果 {@code exchange} 或上下文为 {@code null}，则直接返回，不记录日志。
     * </p>
     *
     * @param exchange   {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @param statusCode 响应状态码
     */
    public static void requestEnd(ServerWebExchange exchange, int statusCode) {
        if (exchange == null) {
            return;
        }
        Context context = Context.get(exchange);
        if (context == null) {
            return;
        }
        long executionTime = System.currentTimeMillis() - context.getStartTime();
        String path = getPath(exchange);
        String method = getMethod(exchange);
        info(exchange, "REQUEST_END", String.format("Method: %s, Path: %s, Status: %d, ExecutionTime: %dms", method,
                path, statusCode, executionTime));
    }

    /**
     * 获取请求的跟踪 ID，并去除两端的方括号。
     * <p>
     * 从 {@code exchange} 的日志前缀中提取跟踪 ID。如果 {@code exchange} 或上下文为 {@code null}， 或日志前缀不可用，则返回 "N/A"。
     * </p>
     *
     * @param exchange {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @return 跟踪 ID 字符串，若不可用则返回 "N/A"
     */
    private static String getTraceId(ServerWebExchange exchange) {
        if (exchange == null) {
            return "N/A";
        }
        Context context = Context.get(exchange);
        if (context == null) {
            return "N/A";
        }
        String traceId = exchange.getLogPrefix();
        if (traceId == null) {
            return "N/A";
        }
        // 去除跟踪 ID 两端的方括号
        if (traceId.startsWith("[") && traceId.endsWith("]")) {
            return traceId.substring(1, traceId.length() - 1).trim();
        }
        return traceId;
    }

    /**
     * 获取请求的 HTTP 方法。
     * <p>
     * 从 {@code exchange} 中提取 HTTP 方法名称。如果 {@code exchange} 或方法为 {@code null}， 则返回 "N/A"。
     * </p>
     *
     * @param exchange {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @return HTTP 方法名称，若不可用则返回 "N/A"
     */
    private static String getMethod(ServerWebExchange exchange) {
        if (exchange == null || exchange.getRequest().getMethod() == null) {
            return "N/A";
        }
        return exchange.getRequest().getMethod().name();
    }

    /**
     * 获取请求路径。
     * <p>
     * 从 {@code exchange} 中提取请求路径。如果 {@code exchange} 为 {@code null}，则返回 "N/A"。
     * </p>
     *
     * @param exchange {@link ServerWebExchange} 对象，包含请求和响应的上下文信息
     * @return 请求路径，若不可用则返回 "N/A"
     */
    private static String getPath(ServerWebExchange exchange) {
        if (exchange == null) {
            return "N/A";
        }
        return exchange.getRequest().getPath().value();
    }

}