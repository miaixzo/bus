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
package org.miaixz.bus.vortex.strategy;

import java.util.*;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

/**
 * An abstract base class for {@link Strategy} implementations, providing a collection of common utility methods.
 * <p>
 * This class offers helper methods for tasks frequently performed by strategies, such as validating parameters,
 * enriching the context with request details (like IP and domain), and handling HTTP-specific logic. Concrete
 * strategies should extend this class to reduce boilerplate code.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractStrategy implements Strategy {

    /**
     * The maximum number of automatic retry attempts allowed when processing a request body fails.
     */
    public static final int MAX_RETRY_ATTEMPTS = Consts.THREE;

    /**
     * The base delay in milliseconds between automatic retry attempts for request body processing.
     */
    public static final long RETRY_DELAY_MS = 1000;

    /**
     * The maximum allowed size in bytes for a non-multipart request body (e.g., JSON, form-urlencoded). This is a
     * crucial defense against Denial-of-Service (DoS) attacks via memory exhaustion.
     */
    public static final long MAX_REQUEST_SIZE = 100 * 1024 * 1024;

    /**
     * The maximum allowed size in bytes for a multipart/form-data request, typically used for file uploads.
     */
    public static final long MAX_MULTIPART_REQUEST_SIZE = 512 * 1024 * 1024;

    /**
     * Safely retrieves the original authority (host + port) of the request, even in a reverse-proxy environment.
     * <p>
     * This method searches for host information in the following priority order, ensuring the returned result always
     * includes the port number:
     * <ol>
     * <li><b>Forwarded Header (RFC 7239):</b> The most modern and standard header, parsed first.</li>
     * <li><b>X-Forwarded-Host Header:</b> The most common de facto standard, widely used in various proxies.</li>
     * <li><b>Host Header:</b> The standard HTTP/1.1 header, which a correctly configured proxy should pass.</li>
     * <li><b>Request URI Host:</b> The last fallback, directly obtained from the request URI.</li>
     * </ol>
     * If the found host information does not contain a port, the default port (80/443) will be automatically appended
     * based on the request protocol.
     *
     * @param request The {@link ServerHttpRequest} object.
     * @return An {@link Optional} containing the authority string (e.g., "example.com:443"), or
     *         {@link Optional#empty()} if no valid host can be found.
     */
    public static Optional<String> getAuthority(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String protocol = getProtocol(request);

        // Priority 1: Try to parse 'Forwarded' (RFC 7239)
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> authority = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("host="))
                    .map(part -> part.substring(5).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (authority.isPresent()) {
                Logger.debug("==>  Authority: '{}' found in 'Forwarded' header", authority.get());
                return authority.map(host -> appendPortIfMissing(host, protocol));
            }
        }

        // Priority 2: Try to parse 'X-Forwarded-Host'
        String forwardedHostHeader = headers.getFirst("X-Forwarded-Host");
        if (StringKit.hasText(forwardedHostHeader)) {
            // In multi-level proxies, this header may contain multiple domain names; the first one is the original
            // domain.
            String authority = forwardedHostHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug("==>  Authority: '{}' found in 'X-Forwarded-Host' header", authority);
            return Optional.of(appendPortIfMissing(authority, protocol));
        }

        // Priority 3: Try to parse 'Host'
        String hostHeader = headers.getFirst("Host");
        if (StringKit.hasText(hostHeader)) {
            Logger.debug("==>  Authority: '{}' found in 'Host' header", hostHeader);
            return Optional.of(appendPortIfMissing(hostHeader, protocol));
        }

        // Priority 4: Use getURI().getHost() as a last fallback
        String uriHost = request.getURI().getHost();
        if (StringKit.hasText(uriHost)) {
            Logger.debug("==>  Authority: '{}' found via request.getURI().getHost() as fallback", uriHost);
            return Optional.of(appendPortIfMissing(uriHost, protocol));
        }

        Logger.debug(
                "==>  Authority: Could not determine a valid authority from any source for request: {}",
                request.getPath());
        return Optional.empty();
    }

    /**
     * Retrieves the original protocol (http or https) of the request, prioritizing proxy headers.
     * <p>
     * This ensures correct protocol detection even when the application is behind a reverse proxy that terminates TLS.
     * The search order is: 'Forwarded' (proto=) -> 'X-Forwarded-Proto' -> {@code request.getURI().getScheme()}.
     *
     * @param request The {@link ServerHttpRequest} object.
     * @return The protocol string, either "https" or "http".
     */
    protected static String getProtocol(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // Priority 1: Try to parse from 'Forwarded' header (RFC 7239)
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> proto = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("proto="))
                    .map(part -> part.substring(6).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (proto.isPresent()) {
                return proto.get();
            }
        }

        // Priority 2: Try to parse from 'X-Forwarded-Proto' header
        String forwardedProtoHeader = headers.getFirst("X-Forwarded-Proto");
        if (StringKit.hasText(forwardedProtoHeader)) {
            return forwardedProtoHeader.split(Symbol.COMMA)[0].trim();
        }

        // Priority 3: Use URI scheme as a last fallback
        return request.getURI().getScheme();
    }

    /**
     * Converts an integer representation of an HTTP method to the corresponding {@link HttpMethod} enum.
     *
     * @param type The integer representation of the request method (e.g., 1 for GET, 2 for POST).
     * @return The matching {@link HttpMethod} enum.
     * @throws ValidateException if the type is not a valid or supported HTTP method.
     */
    protected HttpMethod valueOf(int type) {
        return switch (type) {
            case 1 -> HttpMethod.GET;
            case 2 -> HttpMethod.POST;
            case 3 -> HttpMethod.HEAD;
            case 4 -> HttpMethod.PUT;
            case 5 -> HttpMethod.PATCH;
            case 6 -> HttpMethod.DELETE;
            case 7 -> HttpMethod.OPTIONS;
            case 8 -> HttpMethod.TRACE;
            default -> throw new ValidateException(ErrorCode._100802);
        };
    }

    /**
     * Ensures the request has a {@code Content-Type} header, defaulting to {@code application/x-www-form-urlencoded} if
     * it is missing. This is useful for downstream consumers that expect this header to be present.
     *
     * @param exchange The current {@link ServerWebExchange}.
     * @return The original exchange, or a new exchange with the default header if it was missing.
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
     * Appends a default port to a given authority (host) if the port is missing.
     *
     * @param authority The host information, e.g., "example.com" or "example.com:8080".
     * @param protocol  The protocol, either "http" or "https".
     * @return The authority string, guaranteed to include a port (e.g., "example.com:443").
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
