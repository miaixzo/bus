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
package org.miaixz.bus.fabric.guard.route;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.guard.GuardResult;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.registry.route.Route;

/**
 * Scheme and proxy consistency guard for routes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RouteGuard {

    /**
     * Rule name.
     */
    private static final String NAME = "route";

    /**
     * Allowed normalized schemes.
     */
    private final Set<String> schemes;

    /**
     * Creates a route guard.
     *
     * @param schemes allowed schemes
     */
    private RouteGuard(final Set<String> schemes) {
        this.schemes = schemes;
    }

    /**
     * Creates a scheme-based route guard.
     *
     * @param schemes allowed schemes
     * @return route guard
     */
    public static RouteGuard schemes(final Set<String> schemes) {
        if (schemes == null || schemes.isEmpty()) {
            throw new ValidateException("Schemes must not be empty");
        }
        final LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (final String scheme : schemes) {
            normalized.add(validateScheme(scheme, true));
        }
        return new RouteGuard(Set.copyOf(normalized));
    }

    /**
     * Checks an address scheme.
     *
     * @param address address
     * @return guard result
     */
    public GuardResult check(final Address address) {
        if (address == null) {
            throw new ValidateException("Address must not be null");
        }
        final String scheme = validateScheme(address.scheme(), false);
        return schemes.contains(scheme) ? GuardResult.pass() : GuardResult.reject("route scheme rejected: " + scheme);
    }

    /**
     * Checks a route and its proxy plan.
     *
     * @param route route
     * @return guard result
     */
    public GuardResult check(final Route route) {
        if (route == null) {
            throw new ValidateException("Route must not be null");
        }
        final GuardResult addressResult = check(route.address());
        if (!addressResult.passed()) {
            return addressResult;
        }
        return compatible(route.address(), route.proxy()) ? GuardResult.pass()
                : GuardResult.reject("route proxy incompatible with scheme: " + route.address().scheme());
    }

    /**
     * Returns rule name.
     *
     * @return rule name
     */
    public String name() {
        return NAME;
    }

    /**
     * Returns whether a proxy plan is compatible with a target.
     *
     * @param address target address
     * @param proxy   proxy plan
     * @return true when compatible
     */
    private static boolean compatible(final Address address, final ProxyPlan proxy) {
        if (proxy == null) {
            throw new ProtocolException("Route proxy must not be null");
        }
        final Optional<Address> proxyAddress = proxy.proxy();
        if (proxyAddress.isEmpty()) {
            return true;
        }
        final String proxyScheme = validateScheme(proxyAddress.get().scheme(), false);
        final boolean httpProxy = "http".equals(proxyScheme);
        if (address.secure() && httpProxy) {
            return proxy.requiresTunnel(address);
        }
        if (!address.secure() && httpProxy) {
            return true;
        }
        return "tcp".equals(proxyScheme) || "socket".equals(proxyScheme) || "aio".equals(proxyScheme)
                || "tls".equals(proxyScheme);
    }

    /**
     * Validates and normalizes a scheme.
     *
     * @param value        scheme
     * @param validateFail true to throw validation errors
     * @return normalized scheme
     */
    private static String validateScheme(final String value, final boolean validateFail) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF) || !legalScheme(value)) {
            if (validateFail) {
                throw new ValidateException("Scheme must be non-blank and single-line");
            }
            throw new ProtocolException("Invalid route scheme");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether a value is a legal URI scheme token.
     *
     * @param value value
     * @return true when legal
     */
    private static boolean legalScheme(final String value) {
        if (!Character.isLetter(value.charAt(0))) {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            final char ch = value.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != Symbol.C_PLUS && ch != Symbol.C_MINUS && ch != Symbol.C_DOT) {
                return false;
            }
        }
        return true;
    }

}
