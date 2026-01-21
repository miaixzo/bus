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
package org.miaixz.bus.vortex.strategy;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.util.*;

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
    protected Optional<String> getAuthority(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String protocol = getProtocol(request);

        // Priority 1: Try to parse 'Forwarded' (RFC 7239)
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> authority = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("host="))
                    .map(part -> part.substring(5).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (authority.isPresent()) {
                Logger.debug(true, "Strategy", "{} found in 'Forwarded' header", authority.get());
                return authority.map(host -> appendPortIfMissing(host, protocol));
            }
        }

        // Priority 2: Try to parse 'X-Forwarded-Host'
        String forwardedHostHeader = headers.getFirst("X-Forwarded-Host");
        if (StringKit.hasText(forwardedHostHeader)) {
            // In multi-level proxies, this header may contain multiple domain names; the first one is the original
            // domain.
            String authority = forwardedHostHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug(true, "Strategy", "{} found in 'X-Forwarded-Host' header", authority);
            return Optional.of(appendPortIfMissing(authority, protocol));
        }

        // Priority 3: Try to parse 'Host'
        String hostHeader = headers.getFirst("Host");
        if (StringKit.hasText(hostHeader)) {
            Logger.debug(true, "Strategy", "{} found in 'Host' header", hostHeader);
            return Optional.of(appendPortIfMissing(hostHeader, protocol));
        }

        // Priority 4: Use getURI().getHost() as a last fallback
        String uriHost = request.getURI().getHost();
        if (StringKit.hasText(uriHost)) {
            Logger.debug(true, "Strategy", "{} found via request.getURI().getHost() as fallback", uriHost);
            return Optional.of(appendPortIfMissing(uriHost, protocol));
        }

        Logger.debug(
                true,
                "Strategy",
                "Could not determine a valid authority from any source for request: {}",
                request.getPath());
        return Optional.empty();
    }

    /**
     * Safely retrieves the original client IP address, even in a reverse-proxy environment.
     * <p>
     * This method searches for the client IP in the following priority order:
     * <ol>
     * <li><b>X-Forwarded-For Header:</b> The standard header for identifying the originating IP address. If it contains
     * multiple IPs, the first one in the list is used.</li>
     * <li><b>X-Real-IP Header:</b> A common header used by proxies like Nginx.</li>
     * <li><b>Proxy-Client-IP / WL-Proxy-Client-IP:</b> Headers used by other proxies.</li>
     * <li><b>{@code request.getRemoteAddress()}:</b> The direct remote address, used as a last fallback.</li>
     * </ol>
     *
     * @param request The {@link ServerHttpRequest} object.
     * @return The client IP address as a String, or "unknown" if it cannot be determined.
     */
    protected String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // Priority 1: X-Forwarded-For
        String ip = headers.getFirst("X-Forwarded-For");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            String source = "X-Forwarded-For";
            // 'X-Forwarded-For' might be a comma-separated list (e.g., "client, proxy1, proxy2")
            if (ip.contains(Symbol.COMMA)) {
                String[] ips = ip.split(Symbol.COMMA);
                for (String ipSegment : ips) {
                    String trimmedIp = ipSegment.trim();
                    if (StringKit.hasText(trimmedIp) && !Normal.UNKNOWN.equalsIgnoreCase(trimmedIp)) {
                        Logger.debug(true, "Strategy", "Client IP: '{}' found in {} (list)", trimmedIp, source);
                        return trimmedIp;
                    }
                }
            }
            // If not a list or list parsing fails, return the trimmed original
            Logger.debug(true, "Strategy", "Client IP: '{}' found in {}", ip.trim(), source);
            return ip.trim();
        }

        // Priority 2: X-Real-IP
        ip = headers.getFirst("X-Real-IP");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            Logger.debug(true, "Strategy", "Client IP: '{}' found in X-Real-IP", ip.trim());
            return ip.trim();
        }

        // Priority 3: Other common proxy headers
        ip = headers.getFirst("Proxy-Client-IP");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            Logger.debug(true, "Strategy", "Client IP: '{}' found in Proxy-Client-IP", ip.trim());
            return ip.trim();
        }

        ip = headers.getFirst("WL-Proxy-Client-IP");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            Logger.debug(true, "Strategy", "Client IP: '{}' found in WL-Proxy-Client-IP", ip.trim());
            return ip.trim();
        }

        // Priority 4: Fallback to getRemoteAddress
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            String hostString = remoteAddress.getHostString();
            Logger.debug(true, "Strategy", "Client IP: '{}' found via fallback getRemoteAddress()", hostString);
            // .getHostString() is preferred over .getHostName() to avoid DNS lookup
            return hostString;
        }

        Logger.warn(true, "Strategy", "Client IP could not be determined. Falling back to 'unknown'.");
        return Normal.UNKNOWN;
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
    protected String getProtocol(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        // Priority 1: Try to parse from 'Forwarded' header (RFC 7239)
        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            Optional<String> proto = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith("proto="))
                    .map(part -> part.substring(6).trim().replace("\"", Normal.EMPTY)).findFirst();
            if (proto.isPresent()) {
                String protocol = proto.get();
                Logger.debug(true, "Strategy", "Protocol: '{}' found in 'Forwarded' header", protocol);
                return protocol;
            }
        }

        // Priority 2: Try to parse from 'X-Forwarded-Proto' header
        String forwardedProtoHeader = headers.getFirst("X-Forwarded-Proto");
        if (StringKit.hasText(forwardedProtoHeader)) {
            String protocol = forwardedProtoHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug(true, "Strategy", "Protocol: '{}' found in 'X-Forwarded-Proto' header", protocol);
            return protocol;
        }

        // Priority 3: Use URI scheme as a last fallback
        String protocol = request.getURI().getScheme();
        Logger.debug(true, "Strategy", "Protocol: '{}' found via fallback getURI().getScheme()", protocol);
        return protocol;
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
            Logger.debug(true, "Strategy", "Content-Type is missing. Defaulting to: {}", mediaType);

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
    private String appendPortIfMissing(String authority, String protocol) {
        if (authority.contains(Symbol.COLON)) {
            return authority; // Port already exists
        }
        if (Protocol.HTTPS.name.equalsIgnoreCase(protocol)) {
            return authority + Symbol.COLON + PORT._443.getPort();
        }
        return authority + Symbol.COLON + PORT._80.getPort();
    }

    /**
     * Extracts the authentication token from the incoming request.
     *
     * <p>
     * The token extraction follows a specific order of precedence to ensure compatibility with both standard and legacy
     * authentication methods:
     * </p>
     *
     * <ol>
     * <li><b>Standard Authorization Header:</b> It first checks for the standard {@code Authorization: Bearer <token>}
     * header. This is the preferred and most secure method.</li>
     * <li><b>Custom Header for Backward Compatibility:</b> If the standard header is not found, it searches for a
     * custom header, {@code X-Access-Token}. This check is performed against a list of common case variations (e.g.,
     * {@code X_ACCESS_TOKEN}, {@code x_access_token}) to accommodate different client implementations.</li>
     * <li><b>Request Parameter as Fallback:</b> As a final fallback, if no token is found in the headers, the method
     * searches for the token in the request parameters (query string) using the same set of keys as the custom header.
     * </li>
     * </ol>
     *
     * @param context The incoming {@link ServerHttpRequest} context containing headers and parameters.
     * @return The extracted token string, or {@code null} if no token is found in any of the checked locations.
     */
    protected String getToken(Context context) {
        // 1. Prioritize the standard `Authorization` header with the `Bearer` scheme.
        String authorization = context.getHeaders().get("Authorization");
        if (StringKit.isNotEmpty(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        // 2. Check for a custom `X-Access-Token` header for backward compatibility.
        final String[] keys = { Args.X_ACCESS_TOKEN, Args.X_ACCESS_TOKEN.toUpperCase(),
                Args.X_ACCESS_TOKEN.toLowerCase(), "X_Access_Token", "X_ACCESS_TOKEN", "x_access_token" };
        String token = MapKit.getFirstNonNull(context.getHeaders(), keys);
        if (StringKit.isNotEmpty(token)) {
            return token;
        }

        // 3. If not found in headers, search in request parameters as a fallback.
        if (StringKit.isBlank(token)) {
            token = Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), keys)).map(Object::toString)
                    .orElse(null);
        }

        return token;
    }

    /**
     * Searches for an API key in a predefined list of request parameters and headers.
     *
     * @param context The request context.
     * @return The found API key, or {@code null} if not present.
     */
    protected String getApiKey(Context context) {
        final String[] keys = { "apiKey", "apikey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID",
                "X-API-KEY", "API-KEY", "API-ID" };

        // First, search in request parameters.
        String apiKey = Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), keys)).map(Object::toString)
                .orElse(null);

        // If not found, search in request headers.
        if (StringKit.isBlank(apiKey)) {
            apiKey = MapKit.getFirstNonNull(context.getHeaders(), keys);
        }

        return apiKey;
    }

}
