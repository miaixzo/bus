/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.strategy;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.Specifics;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.magic.ErrorCode;

/**
 * An abstract base class for {@link Strategy} implementations, providing a collection of common utility methods.
 * <p>
 * This class offers helper methods for tasks frequently performed by strategies, such as validating parameters,
 * enriching the context with request details (like IP and domain), and handling HTTP-specific logic. Concrete
 * strategies should extend this class to reduce boilerplate code.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractStrategy implements Strategy {

    /**
     * Creates an abstract strategy.
     */
    protected AbstractStrategy() {
        // No initialization required.
    }

    /**
     * Resolves one request parameter from the context without mutating the request parameter maps.
     * <p>
     * Parameter names are matched case-insensitively for gateway control fields, while the original request parameter
     * names and values remain untouched for downstream forwarding and signature generation.
     *
     * @param context current request context
     * @param key     canonical gateway parameter name
     * @return resolved value, or {@code null}
     */
    protected String value(Context context, String key) {
        return value(context, context == null ? null : context.getParameters(), key);
    }

    /**
     * Resolves one request parameter from a primary map and then the parsed query map without mutating either map.
     *
     * @param context current request context
     * @param values  primary parameter map
     * @param key     canonical gateway parameter name
     * @return resolved value, or {@code null}
     */
    protected String value(Context context, Map<?, ?> values, String key) {
        String value = value(values, key);
        if (value != null) {
            return value;
        }
        return context == null ? null : value(context.getQuery(), key);
    }

    /**
     * Resolves one value from a map by exact key first, then by case-insensitive key match.
     *
     * @param values source map
     * @param key    canonical key
     * @return resolved value, or {@code null}
     */
    protected String value(Map<?, ?> values, String key) {
        if (values == null || values.isEmpty() || StringKit.isBlank(key)) {
            return null;
        }
        if (values.containsKey(key)) {
            Object exact = values.get(key);
            return exact == null ? null : exact.toString();
        }
        boolean matched = false;
        String matchedValue = null;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            Object name = entry.getKey();
            if (name != null && key.equalsIgnoreCase(name.toString())) {
                Object value = entry.getValue();
                String stringValue = value == null ? null : value.toString();
                if (matched && !Objects.equals(matchedValue, stringValue)) {
                    throw new ValidateException(ErrorCode._100101);
                }
                matched = true;
                matchedValue = stringValue;
            }
        }
        return matchedValue;
    }

    /**
     * Copies parameters while excluding one gateway field by case-insensitive key match.
     * <p>
     * Original parameter names are preserved in the copy so signature generation continues to use the inbound key
     * spelling.
     *
     * @param values source parameter map
     * @param key    canonical key to exclude
     * @return sorted copy preserving original keys except the excluded key variants
     */
    protected Map<String, Object> copyWithoutIgnoreCase(Map<String, Object> values, String key) {
        Map<String, Object> copy = new TreeMap<>();
        if (values == null || values.isEmpty()) {
            return copy;
        }
        values.forEach((name, value) -> {
            if (name != null && !key.equalsIgnoreCase(name)) {
                copy.put(name, value);
            }
        });
        return copy;
    }

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

        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            String hostPrefix = "host" + Symbol.EQUAL;
            Optional<String> authority = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith(hostPrefix))
                    .map(part -> part.substring(hostPrefix.length()).trim().replace(Symbol.DOUBLE_QUOTES, Normal.EMPTY))
                    .findFirst();
            if (authority.isPresent()) {
                Logger.debug(true, "Vortex", "{} found in 'Forwarded' header", authority.get());
                return authority.map(host -> appendPortIfMissing(host, protocol));
            }
        }

        String forwardedHostHeader = headers.getFirst("X-Forwarded-Host");
        if (StringKit.hasText(forwardedHostHeader)) {
            String authority = forwardedHostHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug(true, "Vortex", "{} found in 'X-Forwarded-Host' header", authority);
            return Optional.of(appendPortIfMissing(authority, protocol));
        }

        String hostHeader = headers.getFirst(HTTP.HOST);
        if (StringKit.hasText(hostHeader)) {
            Logger.debug(true, "Vortex", "{} found in 'Host' header", hostHeader);
            return Optional.of(appendPortIfMissing(hostHeader, protocol));
        }

        String uriHost = request.getURI().getHost();
        if (StringKit.hasText(uriHost)) {
            Logger.debug(true, "Vortex", "{} found via request.getURI().getHost() as fallback", uriHost);
            return Optional.of(appendPortIfMissing(uriHost, protocol));
        }

        Logger.debug(
                true,
                "Vortex",
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

        String ip = headers.getFirst("X-Forwarded-For");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            String source = "X-Forwarded-For";
            if (ip.contains(Symbol.COMMA)) {
                String[] ips = ip.split(Symbol.COMMA);
                for (String ipSegment : ips) {
                    String trimmedIp = ipSegment.trim();
                    if (StringKit.hasText(trimmedIp) && !Normal.UNKNOWN.equalsIgnoreCase(trimmedIp)) {
                        Logger.debug(true, "Vortex", "Client IP: '{}' found in {} (list)", trimmedIp, source);
                        return trimmedIp;
                    }
                }
            }
            Logger.debug(true, "Vortex", "Client IP: '{}' found in {}", ip.trim(), source);
            return ip.trim();
        }

        ip = headers.getFirst("X-Real-IP");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            Logger.debug(true, "Vortex", "Client IP: '{}' found in X-Real-IP", ip.trim());
            return ip.trim();
        }

        ip = headers.getFirst("Proxy-Client-IP");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            Logger.debug(true, "Vortex", "Client IP: '{}' found in Proxy-Client-IP", ip.trim());
            return ip.trim();
        }

        ip = headers.getFirst("WL-Proxy-Client-IP");
        if (StringKit.hasText(ip) && !Normal.UNKNOWN.equalsIgnoreCase(ip)) {
            Logger.debug(true, "Vortex", "Client IP: '{}' found in WL-Proxy-Client-IP", ip.trim());
            return ip.trim();
        }

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            String hostString = remoteAddress.getHostString();
            Logger.debug(true, "Vortex", "Client IP: '{}' found via fallback getRemoteAddress()", hostString);
            return hostString;
        }

        Logger.warn(false, "Vortex", "Client IP could not be determined. Falling back to 'unknown'.");
        return Normal.UNKNOWN;
    }

    /**
     * Adds derived request metadata to the context and downstream parameter map.
     *
     * @param exchange current exchange
     * @param context  request context
     */
    protected void enrich(ServerWebExchange exchange, Context context) {
        String x_request_id = context.getX_request_id();
        if (StringKit.isEmpty(x_request_id)) {
            x_request_id = context.getX_request_id();
            context.setX_request_id(x_request_id);
        }
        context.getParameters().put("x_request_id", x_request_id);

        String x_request_ipv4 = context.getX_request_ip();
        if (StringKit.isEmpty(x_request_ipv4)) {
            x_request_ipv4 = this.getClientIp(exchange.getRequest());
            context.setX_request_ipv4(x_request_ipv4);
        }
        context.getParameters().put("x_request_ipv4", x_request_ipv4);

        String x_request_domain = context.getX_request_domain();
        if (StringKit.isEmpty(x_request_domain)) {
            x_request_domain = this.determineRequestDomain(exchange.getRequest());
            context.setX_request_domain(x_request_domain);
        }
        context.getParameters().put("x_request_domain", x_request_domain);
        Logger.info(
                true,
                "Vortex",
                "Request domain enriched: strategy=abstract, clientIp={}, path={}, x_request_domain={}, host={}, xForwardedHost={}, forwarded={}",
                x_request_ipv4,
                exchange.getRequest().getURI().getRawPath(),
                x_request_domain,
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.HOST),
                exchange.getRequest().getHeaders().getFirst("X-Forwarded-Host"),
                exchange.getRequest().getHeaders().getFirst("Forwarded"));
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

        String forwardedHeader = headers.getFirst("Forwarded");
        if (StringKit.hasText(forwardedHeader)) {
            String protoPrefix = "proto" + Symbol.EQUAL;
            Optional<String> proto = Arrays.stream(forwardedHeader.split(Symbol.SEMICOLON)).map(String::trim)
                    .filter(part -> part.toLowerCase().startsWith(protoPrefix))
                    .map(
                            part -> part.substring(protoPrefix.length()).trim()
                                    .replace(Symbol.DOUBLE_QUOTES, Normal.EMPTY))
                    .findFirst();
            if (proto.isPresent()) {
                String protocol = proto.get();
                Logger.debug(true, "Vortex", "Protocol: '{}' found in 'Forwarded' header", protocol);
                return protocol;
            }
        }

        String forwardedProtoHeader = headers.getFirst("X-Forwarded-Proto");
        if (StringKit.hasText(forwardedProtoHeader)) {
            String protocol = forwardedProtoHeader.split(Symbol.COMMA)[0].trim();
            Logger.debug(true, "Vortex", "Protocol: '{}' found in 'X-Forwarded-Proto' header", protocol);
            return protocol;
        }

        String protocol = request.getURI().getScheme();
        Logger.debug(true, "Vortex", "Protocol: '{}' found via fallback getURI().getScheme()", protocol);
        return protocol;
    }

    /**
     * Determines the request domain from proxy-aware authority headers.
     *
     * @param request current request
     * @return request authority or a stable unknown fallback
     */
    protected String determineRequestDomain(ServerHttpRequest request) {
        return getAuthority(request).orElseGet(() -> {
            Logger.warn(
                    true,
                    "Vortex",
                    "Unable to determine request domain (host:port). Using default value: strategy=abstract, {}",
                    (Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO));
            return Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO;
        });
    }

    /**
     * Converts an integer registry verb code to the canonical {@link HTTP.Method}.
     *
     * @param type The integer representation of the request method (e.g., 1 for GET, 2 for POST).
     * @return The matching canonical HTTP method.
     * @throws ValidateException if the type is not a valid or supported HTTP method.
     */
    protected HTTP.Method methodOf(int type) {
        try {
            return HTTP.Method.of(type);
        } catch (IllegalArgumentException e) {
            Logger.warn(
                    false,
                    "Vortex",
                    e,
                    "HTTP method mapping failed: methodCode={}, exception={}",
                    type,
                    e.getClass().getSimpleName());
            throw new ValidateException(ErrorCode._100802);
        }
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
            Logger.debug(true, "Vortex", "Content-Type is missing. Defaulting to: {}", mediaType);

            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.setContentType(mediaType);
            ServerHttpRequest requestDecorator = new ServerHttpRequestDecorator(request) {

                /**
                 * Returns the request headers augmented with the resolved default content type.
                 *
                 * @return mutated request headers
                 */
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
            return authority;
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
        String token = MapKit.getFirstNonNull(context.getHeaders(), Specifics.TOKEN_KEYS);
        if (StringKit.isNotBlank(token)) {
            token = token.trim();
            if (token.regionMatches(true, 0, HTTP.BEARER, 0, HTTP.BEARER.length())) {
                return token.substring(HTTP.BEARER.length()).trim();
            }
            return token;
        }

        return Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), Specifics.TOKEN_KEYS))
                .map(Object::toString).orElse(null);
    }

    /**
     * Searches for an API key in a predefined list of request parameters and headers.
     *
     * @param context The request context.
     * @return The found API key, or {@code null} if not present.
     */
    protected String getApiKey(Context context) {
        String apiKey = MapKit.getFirstNonNull(context.getHeaders(), Specifics.API_KEY_KEYS);
        if (StringKit.isNotBlank(apiKey)) {
            return apiKey.trim();
        }

        return Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), Specifics.API_KEY_KEYS))
                .map(Object::toString).map(String::trim).orElse(null);
    }

}
