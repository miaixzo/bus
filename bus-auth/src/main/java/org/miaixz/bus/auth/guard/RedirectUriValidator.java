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
package org.miaixz.bus.auth.guard;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Validates OAuth redirect URIs using exact registration matching and the native loopback port exception.
 * <p>
 * RFC 9700 requires exact string comparison. RFC 8252 permits only a native application's HTTP loopback IP-literal URI
 * to vary the ephemeral port; scheme, loopback address, raw path, raw query, and absence of fragment/userinfo still
 * match. Prefix, wildcard, host normalization, trailing-slash repair, and {@code localhost} exceptions are forbidden.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RedirectUriValidator {

    /**
     * Creates a stateless redirect URI validator.
     */
    public RedirectUriValidator() {
        // No initialization required.
    }

    /**
     * Parses one URI without applying normalization or repair.
     *
     * @param value URI lexical value
     * @return parsed URI
     * @throws ValidateException if syntax is invalid
     */
    private static URI parse(final String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException cause) {
            throw new ValidateException("Redirect URI syntax is invalid", cause);
        }
    }

    /**
     * Enforces redirect URI absolute, userinfo-free, and fragment-free shape.
     *
     * @param value parsed redirect URI
     * @throws ValidateException if a prohibited component is present
     */
    private static void validateShape(final URI value) {
        if (!value.isAbsolute() || value.getScheme() == null || value.getRawFragment() != null
                || value.getRawUserInfo() != null) {
            throw new ValidateException("Redirect URI must be absolute and must not contain userinfo or fragment");
        }
    }

    /**
     * Tests the RFC 8252 native loopback exception while ignoring only the port.
     *
     * @param requested  requested native redirect URI
     * @param registered registered native redirect URI
     * @return {@code true} when both are the same HTTP loopback IP-literal URI except for port
     */
    private static boolean loopbackPortEquivalent(final URI requested, final URI registered) {
        return Protocol.HTTP.name.equals(requested.getScheme()) && Protocol.HTTP.name.equals(registered.getScheme())
                && loopback(requested.getHost()) && Objects.equals(requested.getHost(), registered.getHost())
                && Objects.equals(requested.getRawPath(), registered.getRawPath())
                && Objects.equals(requested.getRawQuery(), registered.getRawQuery());
    }

    /**
     * Tests the IPv4 or IPv6 loopback literal accepted by RFC 8252.
     *
     * @param host parsed URI host
     * @return {@code true} for {@code 127.0.0.1} or {@code ::1}
     */
    private static boolean loopback(final String host) {
        return Protocol.HOST_IPV4.equals(host) || Protocol.HOST_IPV6.equals(host)
                || Protocol.HOST_IPV6_BRACKETED.equals(host);
    }

    /**
     * Validates one requested redirect URI against registered lexical values.
     *
     * @param requested    requested redirect URI exactly as received from the protocol request
     * @param registered   registered redirect URI lexical values
     * @param nativeClient whether the requesting OAuth client is a native application
     * @throws ValidateException        if syntax is invalid or no permitted exact match exists
     * @throws IllegalArgumentException if a required value or list entry is {@code null}
     */
    public void validate(final String requested, final List<String> registered, final boolean nativeClient) {
        Assert.notBlank(requested, "Requested redirect URI must not be blank");
        Assert.notNull(registered, "Registered redirect URI list must not be null");
        final URI requestedUri = parse(requested);
        validateShape(requestedUri);
        for (String candidate : registered) {
            Assert.notBlank(candidate, "Registered redirect URI must not be blank");
            final URI registeredUri = parse(candidate);
            validateShape(registeredUri);
            if (requested.equals(candidate) || nativeClient && loopbackPortEquivalent(requestedUri, registeredUri)) {
                return;
            }
        }
        throw new ValidateException("Requested redirect URI does not exactly match a registered value");
    }

}
