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
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Filter;
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
 * Abstract base class for filters, providing common methods and implementing the template method pattern. All concrete
 * filters should extend this class and implement the {@link #doFilter(ServerWebExchange, WebFilterChain, Context)}
 * method.
 *
 * @author Justubborn
 * @since Java 17+
 */
public abstract class AbstractFilter implements Filter {

    /**
     * The main logic of the filter, which obtains the context and calls the internal filtering method of the subclass.
     *
     * @param exchange The current {@link ServerWebExchange} object, containing the request and response.
     * @param chain    The filter chain, used to continue processing the request.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Logger.info("==>     Filter: {}", this.getClass().getSimpleName());
        return doFilter(exchange, chain, getContext(exchange))
                .doOnTerminate(() -> Logger.debug("<==     Filter: {}", this.getClass().getSimpleName()))
                .doOnError(e -> Logger.error("Error in {}: {}", this.getClass().getSimpleName(), e.getMessage()));
    }

    /**
     * Internal filtering method, to be implemented by subclasses for specific logic.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    protected abstract Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context);

    /**
     * Retrieves the request context.
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @return The request context.
     * @throws ValidateException if the context is null.
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
     * Retrieves the asset information.
     *
     * @param context The request context.
     * @return The asset information.
     * @throws ValidateException if the context is null.
     */
    protected Assets getAssets(Context context) {
        if (context == null) {
            throw new ValidateException(ErrorCode._100805);
        }
        return context.getAssets();
    }

    /**
     * Retrieves the request parameter map.
     *
     * @param context The request context.
     * @return The request parameter map.
     * @throws ValidateException if the context is null.
     */
    protected Map<String, String> getRequestMap(Context context) {
        if (context == null) {
            throw new ValidateException(ErrorCode._100805);
        }
        return context.getRequestMap();
    }

    /**
     * Sets the default Content-Type if the request header is missing.
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @return The updated {@link ServerWebExchange} with the Content-Type header set if it was missing.
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
     * Safely retrieves the original Authority (host + port) of the request from multiple channels, designed for proxy
     * environments.
     * <p>
     * This method searches for host information in the following priority order, ensuring the returned result always
     * includes the port number:
     * <ol>
     * <li><b>Forwarded Header (RFC 7239):</b> The most modern and standard header, parsed first.</li>
     * <li><b>X-Forwarded-Host Header:</b> The most common de facto standard, widely used in various proxies.</li>
     * <li><b>Host Header:</b> The HTTP/1.1 standard header, which a correctly configured proxy should pass.</li>
     * <li><b>Request URI Host:</b> The last fallback, directly obtained from the request URI.</li>
     * </ol>
     * If the found host information does not contain a port, the default 80/443 port will be automatically appended
     * based on the request protocol (http/https).
     *
     * @param request The {@link ServerHttpRequest} object.
     * @return An {@link Optional} object containing the host and port. If no valid host can be found from any source,
     *         {@link Optional#empty()} is returned.
     */
    public static Optional<String> getOriginalAuthority(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String protocol = getOriginalProtocol(request);

        // Priority 1: Try to parse 'Forwarded' (RFC 7239)
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> authority = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("host="))
                    .map(part -> part.substring(5).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (authority.isPresent()) {
                Logger.debug("Authority '{}' found in 'Forwarded' header", authority.get());
                return authority.map(host -> appendPortIfMissing(host, protocol));
            }
        }

        // Priority 2: Try to parse 'X-Forwarded-Host'
        String forwardedHostHeader = headers.getFirst("X-Forwarded-Host");
        if (StringKit.hasText(forwardedHostHeader)) {
            // In multi-level proxies, this header may contain multiple domain names; the first one is the original
            // domain.
            String authority = forwardedHostHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug("Authority '{}' found in 'X-Forwarded-Host' header", authority);
            return Optional.of(appendPortIfMissing(authority, protocol));
        }

        // Priority 3: Try to parse 'Host'
        String hostHeader = headers.getFirst("Host");
        if (StringKit.hasText(hostHeader)) {
            Logger.debug("Authority '{}' found in 'Host' header", hostHeader);
            return Optional.of(appendPortIfMissing(hostHeader, protocol));
        }

        // Priority 4: Use getURI().getHost() as a last fallback
        String uriHost = request.getURI().getHost();
        if (StringKit.hasText(uriHost)) {
            Logger.debug("Authority host '{}' found via request.getURI().getHost() as fallback", uriHost);
            return Optional.of(appendPortIfMissing(uriHost, protocol));
        }

        Logger.warn("Could not determine a valid authority from any source for request: {}", request.getPath());
        return Optional.empty();
    }

    /**
     * Retrieves the original protocol (http or https) of the request.
     * <p>
     * Prioritizes retrieval from proxy headers to ensure correct results even after a reverse proxy. Search order:
     * 'Forwarded' (proto=) -> 'X-Forwarded-Proto' -> request.getURI().getScheme().
     *
     * @param request The {@link ServerHttpRequest} object.
     * @return The protocol string, either "https" or "http".
     */
    protected static String getOriginalProtocol(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // Try to parse from 'Forwarded' header
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> proto = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("proto="))
                    .map(part -> part.substring(6).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (proto.isPresent()) {
                return proto.get();
            }
        }

        // Try to parse from 'X-Forwarded-Proto' header
        String forwardedProtoHeader = headers.getFirst("X-Forwarded-Proto");
        if (StringKit.hasText(forwardedProtoHeader)) {
            return forwardedProtoHeader.split(Symbol.COMMA)[0].trim();
        }

        // Use URI scheme as a last fallback
        return request.getURI().getScheme();
    }

    /**
     * Validates request parameters, ensuring that necessary parameters exist and are valid.
     *
     * @param exchange The {@link ServerWebExchange} object.
     * @throws ValidateException if parameters are invalid or missing.
     */
    protected void validate(ServerWebExchange exchange) {
        Context context = getContext(exchange);
        Map<String, String> params = getRequestMap(context);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // Check if the key is null or undefined
            if (entry.getKey() != null && Normal.UNDEFINED.equals(entry.getKey().toLowerCase())) {
                throw new ValidateException(ErrorCode._100101);
            }
            // Check if the value is a string and is undefined
            if (entry.getValue() instanceof String) {
                if (Normal.UNDEFINED.equals(entry.getValue().toLowerCase())) {
                    throw new ValidateException(ErrorCode._100101);
                }
            }
        }
        if (StringKit.isBlank(params.get(Args.METHOD))) {
            throw new ValidateException(ErrorCode._100108);
        }
        if (StringKit.isBlank(params.get(Args.VERSION))) {
            throw new ValidateException(ErrorCode._100107);
        }
        if (StringKit.isBlank(params.get(Args.FORMAT))) {
            throw new ValidateException(ErrorCode._100111);
        }
        if (StringKit.isNotBlank(params.get(Args.SIGN))) {
            context.setSign(Integer.valueOf(params.get(Args.SIGN)));
        }
    }

    /**
     * Converts an integer type to its corresponding {@link HttpMethod}.
     *
     * @param type The integer representation of the request method.
     * @return The {@link HttpMethod} enum corresponding to the given type.
     * @throws ValidateException if the type is not a valid HTTP method.
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
     * Appends a default port to the given authority (host, potentially with a port) if the port is missing.
     *
     * @param authority The host information, e.g., "example.com" or "example.com:8080".
     * @param protocol  The protocol, either "http" or "https".
     * @return The host information always including a port, e.g., "example.com:443" or "example.com:8080".
     */
    private static String appendPortIfMissing(String authority, String protocol) {
        if (authority.contains(Symbol.COLON)) {
            return authority; // Port already exists
        }
        if (Protocol.HTTPS.name.equalsIgnoreCase(protocol)) {
            return authority + Symbol.COLON + PORT._443;
        }
        return authority + Symbol.COLON + PORT._80;
    }

}
