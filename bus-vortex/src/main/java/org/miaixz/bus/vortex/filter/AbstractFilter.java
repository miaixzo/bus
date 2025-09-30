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
package org.miaixz.bus.vortex.filter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 抽象过滤器基类，提供公共方法和模板方法模式 所有具体过滤器继承此类，实现 doFilterInternal 方法
 *
 * @author Justubborn
 * @since Java 17+
 */
public abstract class AbstractFilter implements Filter {

    /**
     * 过滤器主逻辑，获取上下文并调用子类的内部过滤方法
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Format.info(exchange, "FILTER_ENTER", "Entering filter: " + this.getClass().getSimpleName());
        return doFilter(exchange, chain, getContext(exchange)).doOnTerminate(
                () -> Format.debug(exchange, "FILTER_EXIT", "Exiting filter: " + this.getClass().getSimpleName()))
                .doOnError(
                        e -> Format.error(
                                exchange,
                                "FILTER_ERROR",
                                "Error in " + this.getClass().getSimpleName() + ": " + e.getMessage()));
    }

    /**
     * 内部过滤方法，由子类实现具体逻辑
     *
     * @param exchange 当前的 ServerWebExchange 对象
     * @param chain    过滤器链
     * @param context  请求上下文
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    protected abstract Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context);

    /**
     * 获取请求上下文
     *
     * @param exchange ServerWebExchange 对象
     * @return 请求上下文
     */
    protected Context getContext(ServerWebExchange exchange) {
        Context context = Context.get(exchange);
        if (context == null) {
            throw new ValidateException(ErrorCode._100805);
        }
        context.setHttpMethod(exchange.getRequest().getMethod());
        return context;
    }

    /**
     * 获取资产信息
     *
     * @param context 请求上下文
     * @return 资产信息
     */
    protected Assets getAssets(Context context) {
        if (context == null) {
            throw new ValidateException(ErrorCode._100805);
        }
        return context.getAssets();
    }

    /**
     * 获取请求参数映射
     *
     * @param context 请求上下文
     * @return 请求参数映射
     */
    protected Map<String, String> getRequestMap(Context context) {
        if (context == null) {
            throw new ValidateException(ErrorCode._100805);
        }
        return context.getRequestMap();
    }

    /**
     * 设置默认 Content-Type（如果请求头缺失）
     *
     * @param exchange ServerWebExchange 对象
     * @return 更新后的 ServerWebExchange
     */
    protected ServerWebExchange setContentType(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = request.getHeaders().getContentType();
        if (null == mediaType) {
            mediaType = MediaType.APPLICATION_FORM_URLENCODED;
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.setContentType(mediaType);
            ServerHttpRequest requestDecorator = new ServerHttpRequestDecorator(request) {

                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            };
            return exchange.mutate().request(requestDecorator).build();
        }
        return exchange;
    }

    /**
     * 从多个渠道安全地获取请求的原始 Authority（主机+端口），专为代理环境设计。
     * <p>
     * 此方法按照以下优先级顺序查找主机信息，并确保返回结果始终包含端口号：
     * <ol>
     * <li><b>Forwarded Header (RFC 7239):</b> 最现代、最标准的头，优先解析。</li>
     * <li><b>X-Forwarded-Host Header:</b> 最常见的事实标准，广泛用于各类代理。</li>
     * <li><b>Host Header:</b> HTTP/1.1 标准头，一个正确配置的代理应该会传递它。</li>
     * <li><b>Request URI Host:</b> 最后的备选方案，直接从请求URI中获取。</li>
     * </ol>
     * 如果找到的主机信息不包含端口，将根据请求协议（http/https）自动附加默认的 80/443 端口。
     *
     * @param request ServerHttpRequest 对象
     * @return 包含主机和端口的 {@link Optional} 对象。如果所有渠道都无法找到有效主机，则返回 {@link Optional#empty()}。
     */
    public static Optional<String> getOriginalAuthority(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String protocol = getOriginalProtocol(request);

        // 优先级 1: 尝试解析 'Forwarded' (RFC 7239)
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> authority = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("host="))
                    .map(part -> part.substring(5).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (authority.isPresent()) {
                Logger.debug("Authority '{}' found in 'Forwarded' header.", authority.get());
                return authority.map(host -> appendPortIfMissing(host, protocol));
            }
        }

        // 优先级 2: 尝试解析 'X-Forwarded-Host'
        String forwardedHostHeader = headers.getFirst("X-Forwarded-Host");
        if (StringKit.hasText(forwardedHostHeader)) {
            // 在多级代理中，此头可能包含多个域名，第一个是原始域名
            String authority = forwardedHostHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug("Authority '{}' found in 'X-Forwarded-Host' header.", authority);
            return Optional.of(appendPortIfMissing(authority, protocol));
        }

        // 优先级 3: 尝试解析 'Host'
        String hostHeader = headers.getFirst("Host");
        if (StringKit.hasText(hostHeader)) {
            Logger.debug("Authority '{}' found in 'Host' header.", hostHeader);
            return Optional.of(appendPortIfMissing(hostHeader, protocol));
        }

        // 优先级 4: 使用 getURI().getHost() 作为最后备选
        String uriHost = request.getURI().getHost();
        if (StringKit.hasText(uriHost)) {
            Logger.debug("Authority host '{}' found via request.getURI().getHost() as fallback.", uriHost);
            return Optional.of(appendPortIfMissing(uriHost, protocol));
        }

        Logger.warn("Could not determine a valid authority from any source for request: {}", request.getPath());
        return Optional.empty();
    }

    /**
     * 获取请求的原始协议（http 或 https）。
     * <p>
     * 优先从代理头中获取，以确保在反向代理后也能得到正确的结果。 查找顺序: 'Forwarded' (proto=) -> 'X-Forwarded-Proto' -> request.getURI().getScheme()
     *
     * @param request ServerHttpRequest 对象
     * @return 协议字符串, "https" 或 "http".
     */
    protected static String getOriginalProtocol(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // 尝试从 'Forwarded' 头解析
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> proto = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("proto="))
                    .map(part -> part.substring(6).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (proto.isPresent()) {
                return proto.get();
            }
        }

        // 尝试从 'X-Forwarded-Proto' 头解析
        String forwardedProtoHeader = headers.getFirst("X-Forwarded-Proto");
        if (StringKit.hasText(forwardedProtoHeader)) {
            return forwardedProtoHeader.split(Symbol.COMMA)[0].trim();
        }

        // 使用 URI scheme 作为最后备选
        return request.getURI().getScheme();
    }

    /**
     * 校验请求参数，确保必要参数存在且有效
     *
     * @param exchange ServerWebExchange 对象
     * @throws ValidateException 如果参数无效或缺失，抛出异常
     */
    protected void validate(ServerWebExchange exchange) {
        Context context = getContext(exchange);
        Map<String, String> params = getRequestMap(context);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // 检查键是否为null或undefined
            if (entry.getKey() != null && Normal.UNDEFINED.equals(entry.getKey().toLowerCase())) {
                throw new ValidateException(ErrorCode._100101);
            }
            // 检查值是否为字符串且为undefined
            if (entry.getValue() instanceof String) {
                if (Normal.UNDEFINED.equals(entry.getValue().toLowerCase())) {
                    throw new ValidateException(ErrorCode._100101);
                }
            }
        }
        if (StringKit.isBlank(params.get(Config.METHOD))) {
            throw new ValidateException(ErrorCode._100108);
        }
        if (StringKit.isBlank(params.get(Config.VERSION))) {
            throw new ValidateException(ErrorCode._100107);
        }
        if (StringKit.isBlank(params.get(Config.FORMAT))) {
            throw new ValidateException(ErrorCode._100111);
        }
        if (StringKit.isNotBlank(params.get(Config.SIGN))) {
            context.setSign(Integer.valueOf(params.get(Config.SIGN)));
        }
    }

    /**
     * 请求方式转换
     *
     * @param type 请求方式
     * @return {@link HttpMethod}
     */
    public HttpMethod valueOf(int type) {
        switch (type) {
            case 1:
                return HttpMethod.GET;

            case 2:
                return HttpMethod.POST;

            case 3:
                return HttpMethod.HEAD;

            case 4:
                return HttpMethod.PUT;

            case 5:
                return HttpMethod.PATCH;

            case 6:
                return HttpMethod.DELETE;

            case 7:
                return HttpMethod.OPTIONS;

            case 8:
                return HttpMethod.TRACE;

            default:
                throw new ValidateException(ErrorCode._100802);
        }
    }

    /**
     * 为给定的主机（可能包含端口）附加默认端口（如果缺少）。
     *
     * @param authority 主机信息，例如 "example.com" 或 "example.com:8080"
     * @param protocol  协议, "http" 或 "https"
     * @return 始终包含端口的主机信息，例如 "example.com:443" 或 "example.com:8080"
     */
    private static String appendPortIfMissing(String authority, String protocol) {
        if (authority.contains(Symbol.COLON)) {
            return authority; // 端口已存在
        }
        if (Protocol.HTTPS.name.equalsIgnoreCase(protocol)) {
            return authority + Symbol.COLON + PORT._443;
        }
        return authority + Symbol.COLON + PORT._80;
    }

}
