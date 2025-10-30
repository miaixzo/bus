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
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
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
     * Validates standard gateway parameters and enriches the context with additional request information.
     * <p>
     * This method performs two main functions:
     * <ol>
     * <li><b>Validation:</b> It checks for the presence and validity of essential gateway parameters like
     * {@code method}, {@code version}, and {@code format}.</li>
     * <li><b>Enrichment:</b> It calls {@link #enrich(ServerWebExchange, Context)} to add derived information like
     * client IP and domain to the context.</li>
     * </ol>
     * This method has the side effect of modifying the passed-in {@code Context} object.
     *
     * @param exchange The current server exchange.
     * @param context  The request context to be validated and enriched.
     * @throws ValidateException  if any standard parameter is missing or invalid.
     * @throws SignatureException if the request signature is invalid.
     */
    protected void validateParameters(ServerWebExchange exchange, Context context) {
        Map<String, Object> params = context.getParameters();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            // Check for "undefined" string values, which can be sent by some clients.
            if (entry.getKey() != null && Normal.UNDEFINED.equals(entry.getKey().toLowerCase())) {
                throw new ValidateException(ErrorCode._100101);
            }
            if (Normal.UNDEFINED.equals(String.valueOf(entry.getValue()).toLowerCase())) {
                throw new ValidateException(ErrorCode._100101);
            }
        }
        // Validate presence of core gateway parameters.
        if (StringKit.isBlank(Optional.ofNullable(params.get(Args.METHOD)).map(Object::toString).orElse(null))) {
            throw new ValidateException(ErrorCode._100110);
        }
        if (StringKit.isBlank(Optional.ofNullable(params.get(Args.VERSION)).map(Object::toString).orElse(null))) {
            throw new ValidateException(ErrorCode._100109);
        }
        if (StringKit.isBlank(Optional.ofNullable(params.get(Args.FORMAT)).map(Object::toString).orElse(null))) {
            throw new ValidateException(ErrorCode._100113);
        }
        if (StringKit.isBlank(Optional.ofNullable(params.get(Args.SIGN)).map(Object::toString).orElse(null))) {
            throw new ValidateException(ErrorCode._100114);
        }
        if (StringKit.isBlank(Optional.ofNullable(params.get(Args.TIMESTAMP)).map(Object::toString).orElse(null))) {
            throw new ValidateException(ErrorCode._100115);
        }

        // Validates if the provided client timestamp is within a 10-minute window of the current server time
        String timestamp = String.valueOf(params.get(Args.TIMESTAMP));
        this.isTimestampValid(timestamp);

        // Validate the request signature to ensure integrity and authenticity.
        String key = ObjectKit.isNotEmpty(params.get(Args.APIKEY)) ? String.valueOf(params.get(Args.APIKEY))
                : String.valueOf(params.get(Args.METHOD));
        if (!this.validateSign(key + timestamp, context.getHttpMethod().name(), context.getParameters())) {
            throw new SignatureException(ErrorCode._100106);
        }
        // Add additional derived parameters to the context.
        this.enrich(exchange, context);
    }

    /**
     * Validates if a given timestamp string is within an acceptable time window.
     * <p>
     * The timestamp is expected to be a string representation of milliseconds since the Unix epoch. A valid timestamp
     * must not be null, empty, non-numeric, or differ from the server's current time by more than a 10-minute window.
     * </p>
     *
     * @param timestamp The timestamp string to validate.
     * @throws ValidateException if the timestamp is null, empty, non-numeric, or outside the allowed time window.
     */
    public static void isTimestampValid(String timestamp) {
        try {
            // 1. Parse the client timestamp string to a long value
            long clientTimestampMs = Long.parseLong(timestamp);

            // 2. Get the current server time in milliseconds
            long currentTimestampMs = DateKit.current();

            // 3. Calculate the absolute difference in milliseconds
            long absoluteDifferenceMs = Math.abs(currentTimestampMs - clientTimestampMs);

            // 4. Check if the difference exceeds the 10-minute window
            if (absoluteDifferenceMs > TimeUnit.MINUTES.toMillis(10)) {

                // --- Aligned log output for debugging ---

                // 1. Define the total width for the log box.
                final int total_width = 49;
                // 2. Calculate the inner content width.
                final int inner_width = total_width - 2; // 47

                // 3. Create a printf-style format string for left-alignment.
                // "%-" pads the string on the right to achieve left-alignment.
                // inner_width specifies the total width. "s" specifies the type (String).
                String contentFormat = "%-" + inner_width + "s";

                // 4. Create the content lines to be logged.
                String srvContent = "  Server Time (ms): " + currentTimestampMs;
                String cliContent = "  Client Time (ms): " + clientTimestampMs;
                String difContent = "  Difference (ms) : " + absoluteDifferenceMs;

                // 5. Log the formatted messages.
                Logger.info("*************************************************");
                // Use String.format to pad the content to 47 chars, then add `*`
                Logger.info("*" + String.format(contentFormat, cliContent) + "*");
                Logger.info("*" + String.format(contentFormat, srvContent) + "*");
                Logger.info("*" + String.format(contentFormat, difContent) + "*");
                Logger.info("*************************************************");

                // --- End of log block ---
                throw new ValidateException(ErrorCode._100107);
            }
        } catch (NumberFormatException e) {
            // Failed: The timestamp string was not a valid long.
            Logger.error("Validation failed: Timestamp format is not a valid millisecond long: " + timestamp);
            throw new ValidateException(ErrorCode._100107);
        }
    }

    /**
     * Verifies if the signature of an API request is valid by recalculating it and comparing.
     *
     * @param key        The application secret key.
     * @param httpMethod The HTTP request method (e.g., GET, POST).
     * @param params     All parameters received from the client as a Map, must include the 'sign' parameter.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    protected boolean validateSign(String key, String httpMethod, Map<String, Object> params) {
        if (params == null || !params.containsKey("sign")) {
            return false; // Parameters are null or missing 'sign', verification fails.
        }

        // 1. Get the signature sent by the client
        String clientSign = String.valueOf(params.get("sign"));
        Logger.info("==>Client Sign:" + clientSign);

        // 2. Prepare parameters for re-calculation (remove the 'sign' key)
        Map<String, Object> paramsForSign = new TreeMap<>(params);
        paramsForSign.remove("sign");

        // 3. Re-calculate the signature using the server-side secret key
        String serverSign = sign(key, httpMethod, paramsForSign);
        Logger.info("==>Server Sign:" + serverSign);

        // 4. Compare the signatures
        return Objects.equals(clientSign, serverSign);
    }

    /**
     * Enriches the context with derived information about the client and request authority.
     * <p>
     * This method extracts the client's IP address and the request's authority (domain and port), then adds them to the
     * context's parameter map. This ensures that this information is available to all downstream strategies and the
     * final service.
     * <p>
     * This method has the side effect of modifying the passed-in {@code Context} object.
     *
     * @param exchange The current server exchange.
     * @param context  The request context to be enriched.
     */
    protected void enrich(ServerWebExchange exchange, Context context) {
        context.getParameters().put("x_request_id", exchange.getRequest().getId());
        // Extract the client's IP address, respecting proxy headers.
        String clientIp = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("x_request_ip")).orElseGet(
                () -> Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")).orElseGet(
                        () -> exchange.getRequest().getRemoteAddress() != null
                                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                : "unknown"));

        context.setX_request_ipv4(clientIp);
        context.getParameters().put("x_request_ipv4", clientIp);

        // Extract the request authority (domain and port), respecting proxy headers.
        Optional<String> authority = getAuthority(exchange.getRequest());

        String unknown = Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO;
        String domain = authority.orElse(unknown);

        if (unknown.equals(domain)) {
            Logger.warn(
                    "==> Filter: Unable to determine the request domain (host:port). Using default value: {}",
                    domain);
        }

        context.setX_request_domain(domain);
        context.getParameters().put("x_request_domain", domain);
    }

    /**
     * Generates an API signature using the HMAC-SHA256 algorithm.
     *
     * @param key        The application secret key.
     * @param httpMethod The HTTP request method (e.g., GET, POST).
     * @param params     All request parameters as a Map, should not include the 'sign' parameter.
     * @return The Base64 encoded signature string.
     */
    protected String sign(String key, String httpMethod, Map<String, Object> params) {
        // 1. Validate core parameters
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("App secret cannot be null or empty.");
        }
        if (httpMethod == null || params == null) {
            throw new IllegalArgumentException("Http method, request url, and params cannot be null.");
        }

        // 2. Filter out empty parameters, URL Encode them, and sort them lexicographically
        Map<String, String> sortedParams = new TreeMap<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String value = String.valueOf(entry.getValue());
            if (value != null && !value.isEmpty()) {
                String encodedKey = UrlEncoder.encodeAll(entry.getKey(), Charset.UTF_8);
                String encodedValue = UrlEncoder.encodeAll(value, Charset.UTF_8);
                sortedParams.put(encodedKey, encodedValue);
            }
        }

        // 3. Concatenate the parameter string
        StringBuilder paramBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            paramBuilder.append(entry.getKey()).append(entry.getValue());
        }

        // 4. Construct the final string to be signed
        String stringToSign = httpMethod + "\n" + paramBuilder;

        // 5. Use bus-crypto's HMac for HMAC-SHA256 signing and Base64 encoding
        HMac hmac = Builder.hmac(Algorithm.HMACSHA256, key.getBytes(Charset.UTF_8));
        byte[] signBytes = hmac.digest(stringToSign.getBytes(Charset.UTF_8));
        return Base64.encode(signBytes);
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
