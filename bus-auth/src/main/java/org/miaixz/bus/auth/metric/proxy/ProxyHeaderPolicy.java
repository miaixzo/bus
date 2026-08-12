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
package org.miaixz.bus.auth.metric.proxy;

import java.util.*;

import org.miaixz.bus.auth.metric.ProxyAuth.Config;
import org.miaixz.bus.auth.metric.ProxyAuth.ForwardRequest;
import org.miaixz.bus.auth.metric.ProxyAuth.Identity;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Removes every untrusted hop-by-hop, forwarding, proxy-authorization, and identity header before applying the product
 * allow-list. Trusted identity headers are built only from the authenticated product result.
 *
 * @author Kimi Liu
 */
public final class ProxyHeaderPolicy {

    /**
     * Maximum aggregate sanitized header characters.
     */
    public static final int MAXIMUM_HEADER_CHARACTERS = 32 * Normal._1024;

    /**
     * Exact hop-by-hop and credential headers removed before allow-list processing.
     */
    private static final Set<String> FORBIDDEN = Set.of(
            Http.Header.CONNECTION.toLowerCase(Locale.ROOT),
            "keep-alive",
            "proxy-authenticate",
            Http.Header.PROXY_AUTHORIZATION.toLowerCase(Locale.ROOT),
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "forwarded");

    /**
     * Closed forward-auth policy.
     */
    private final Config configuration;

    /**
     * Normalized incoming allow-list.
     */
    private final Set<String> allowed;

    /**
     * Creates one header policy.
     *
     * @param configuration closed configuration
     */
    public ProxyHeaderPolicy(final Config configuration) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("Proxy configuration must not be null"));
        this.allowed = configuration.allowedRequestHeaders().stream().map(ProxyHeaderPolicy::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        configuration.identityHeaders().keySet().forEach(header -> {
            final String normalized = name(header);
            Assert.isTrue(
                    !forbidden(normalized),
                    () -> new ValidateException("Proxy identity header name is reserved"));
        });
    }

    /**
     * Returns whether a normalized header is always removed.
     *
     * @param header normalized header
     * @return whether forbidden
     */
    static boolean forbidden(final String header) {
        return FORBIDDEN.contains(header) || header.startsWith("x-forwarded-") || header.startsWith("x-auth-");
    }

    /**
     * Normalizes and validates a header name.
     *
     * @param source source name
     * @return normalized lower-case name
     */
    static String name(final String source) {
        final String value = Assert
                .notBlank(source, () -> new ValidateException("Proxy header name must not be blank"));
        Assert.isTrue(
                value.chars().allMatch(character -> Character.isLetterOrDigit(character) || character == '-'),
                () -> new ValidateException("Proxy header name is invalid"));
        return value.toLowerCase(Locale.ROOT);
    }

    /**
     * Validates a header value against line injection.
     *
     * @param value source value
     */
    static void safe(final String value) {
        Assert.isTrue(
                value != null && value.indexOf('\r') < Normal._0 && value.indexOf('\n') < Normal._0,
                () -> new ValidateException("Proxy header value is invalid"));
    }

    /**
     * Sanitizes one incoming request.
     *
     * @param request source request
     * @return request containing only allow-listed safe headers
     */
    public ForwardRequest sanitize(final ForwardRequest request) {
        final ForwardRequest source = Assert
                .notNull(request, () -> new ValidateException("Proxy request must not be null"));
        final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        int characters = Normal._0;
        for (final Map.Entry<String, List<String>> entry : source.headers().entrySet()) {
            final String header = name(entry.getKey());
            if (forbidden(header) || !allowed.contains(header)) {
                continue;
            }
            final List<String> values = List.copyOf(entry.getValue());
            Assert.isTrue(!values.isEmpty(), () -> new ValidateException("Proxy header value list is empty"));
            for (final String value : values) {
                safe(value);
                characters += header.length() + value.length();
            }
            Assert.isTrue(
                    result.putIfAbsent(header, values) == null,
                    () -> new ValidateException("Proxy header is duplicated by case"));
        }
        Assert.isTrue(
                characters <= MAXIMUM_HEADER_CHARACTERS,
                () -> new ValidateException("Proxy headers exceed their size limit"));
        return new ForwardRequest(source.method(), source.uri(), result, source.returnUri());
    }

    /**
     * Builds trusted output identity headers.
     *
     * @param identity authenticated identity
     * @return immutable trusted headers
     */
    public Map<String, List<String>> identity(final Identity identity) {
        final Identity source = Assert
                .notNull(identity, () -> new ValidateException("Proxy identity must not be null"));
        Assert.isTrue(
                source.authenticated(),
                () -> new ValidateException("Proxy identity headers require authentication"));
        final LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        configuration.identityHeaders().forEach((header, attribute) -> {
            final String value = "subject".equals(attribute) ? source.subjectId() : source.attributes().get(attribute);
            if (value != null) {
                safe(value);
                result.put(name(header), List.of(value));
            }
        });
        return Map.copyOf(result);
    }

}
