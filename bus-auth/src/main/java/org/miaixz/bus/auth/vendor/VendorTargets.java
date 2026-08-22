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
package org.miaixz.bus.auth.vendor;

import java.net.IDN;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.core.net.tls.TlsVersion;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.xyz.PatternKit;

/**
 * Owns the immutable official endpoint targets declared by one platform variant.
 * <p>
 * Resolution performs only deterministic template substitution. Network address, redirect, proxy, DNS, and TLS policy
 * enforcement remains the responsibility of the standard client or platform adapter using the endpoint.
 * </p>
 *
 * @param authorization       authorization endpoint
 * @param token               token or platform credential endpoint
 * @param userInfo            user information endpoint
 * @param refresh             refresh endpoint when distinct from token
 * @param introspection       standard introspection or registered platform token-info endpoint
 * @param revocation          revocation endpoint
 * @param deviceAuthorization device authorization endpoint
 * @param discovery           OpenID or OAuth metadata discovery endpoint
 * @param jwks                JSON Web Key Set endpoint
 * @param endSession          OpenID Connect end-session endpoint
 * @param management          ordered named enterprise management endpoints
 * @author Kimi Liu
 */
public record VendorTargets(Optional<Target> authorization, Optional<Target> token, Optional<Target> userInfo,
        Optional<Target> refresh, Optional<Target> introspection, Optional<Target> revocation,
        Optional<Target> deviceAuthorization, Optional<Target> discovery, Optional<Target> jwks,
        Optional<Target> endSession, Map<String, Target> management) {

    /**
     * Placeholder for a path-segment tenant value.
     */
    private static final String TENANT = Variable.TENANT.token;

    /**
     * Placeholder for a validated platform DNS instance.
     */
    private static final String INSTANCE = Variable.INSTANCE.token;

    /**
     * Placeholder for a path-segment authorization server identifier.
     */
    private static final String AUTHORIZATION_SERVER_ID = Variable.AUTHORIZATION_SERVER_ID.token;

    /**
     * Validates and normalizes every optional endpoint target container.
     *
     * @param authorization       authorization endpoint
     * @param token               token or platform credential endpoint
     * @param userInfo            user information endpoint
     * @param refresh             refresh endpoint when distinct from token
     * @param introspection       introspection or registered platform token-info endpoint
     * @param revocation          revocation endpoint
     * @param deviceAuthorization device authorization endpoint
     * @param discovery           OpenID or OAuth metadata discovery endpoint
     * @param jwks                JSON Web Key Set endpoint
     * @param endSession          OpenID Connect end-session endpoint
     * @param management          ordered named enterprise management endpoints
     * @throws IllegalArgumentException if an optional container or management map entry is null
     * @throws ValidateException        if a management key is blank or contains surrounding whitespace
     */
    public VendorTargets {
        authorization = normalize(authorization, "Vendor authorization endpoint");
        token = normalize(token, "Vendor token endpoint");
        userInfo = normalize(userInfo, "Vendor user information endpoint");
        refresh = normalize(refresh, "Vendor refresh endpoint");
        introspection = normalize(introspection, "Vendor introspection endpoint");
        revocation = normalize(revocation, "Vendor revocation endpoint");
        deviceAuthorization = normalize(deviceAuthorization, "Vendor device authorization endpoint");
        discovery = normalize(discovery, "Vendor discovery endpoint");
        jwks = normalize(jwks, "Vendor JWK Set endpoint");
        endSession = normalize(endSession, "Vendor end-session endpoint");
        management = normalizeManagement(management);
    }

    /**
     * Creates a Vendor target set in the original ten-endpoint source shape without enterprise management targets.
     *
     * @param authorization       authorization endpoint
     * @param token               token or platform credential endpoint
     * @param userInfo            user information endpoint
     * @param refresh             refresh endpoint when distinct from token
     * @param introspection       introspection or registered platform token-info endpoint
     * @param revocation          revocation endpoint
     * @param deviceAuthorization device authorization endpoint
     * @param discovery           OpenID or OAuth metadata discovery endpoint
     * @param jwks                JSON Web Key Set endpoint
     * @param endSession          OpenID Connect end-session endpoint
     */
    public VendorTargets(final Optional<Target> authorization, final Optional<Target> token,
            final Optional<Target> userInfo, final Optional<Target> refresh, final Optional<Target> introspection,
            final Optional<Target> revocation, final Optional<Target> deviceAuthorization,
            final Optional<Target> discovery, final Optional<Target> jwks, final Optional<Target> endSession) {
        this(authorization, token, userInfo, refresh, introspection, revocation, deviceAuthorization, discovery, jwks,
                endSession, Map.of());
    }

    /**
     * Resolves a manifest-owned issuer template with the same constrained variables used by target templates.
     * <p>
     * The returned value is an exact credential-free HTTPS issuer identifier without query or fragment. This method
     * performs deterministic substitution only; it does not perform discovery, network access, or issuer comparison.
     * </p>
     *
     * @param value   manifest-owned fixed issuer or constrained issuer template
     * @param options validated platform options supplying any required template selectors
     * @return exact resolved HTTPS issuer identifier
     * @throws IllegalArgumentException if the value is blank or options are {@code null}
     * @throws ValidateException        if the template or resolved issuer violates the constrained trust boundary
     */
    public static String resolveIdentifier(final String value, final VendorOptions<?> options) {
        validateIdentifierTemplate(value);
        final String resolved = resolveValue(value, Assert.notNull(options, "Vendor Source options must not be null"));
        final Url url = Url.parse(resolved);
        if (!Protocol.HTTPS.name.equalsIgnoreCase(url.scheme()) || url.host() == null || !url.username().isEmpty()
                || !url.query().isEmpty() || url.fragment() != null) {
            throw new ValidateException(
                    "Resolved Vendor identifier must be a credential-free absolute HTTPS URI without query or fragment");
        }
        return resolved;
    }

    /**
     * Validates a manifest-owned issuer template without requiring deployment selector values.
     *
     * @param value manifest-owned fixed issuer or constrained issuer template
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the value is not a credential-free HTTPS identifier template without query or
     *                                  fragment
     */
    static void validateIdentifierTemplate(final String value) {
        validateTemplate(value);
        if (URI.create(parseable(value)).getRawQuery() != null) {
            throw new ValidateException("Vendor identifier template must not contain a query");
        }
    }

    /**
     * Normalizes an optional target container.
     *
     * @param value optional manifest-owned endpoint target
     * @param label diagnostic endpoint slot label
     * @return normalized optional target
     */
    private static Optional<Target> normalize(final Optional<Target> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Normalizes an optional resolved endpoint container.
     *
     * @param value optional resolved endpoint
     * @param label diagnostic endpoint slot label
     * @return normalized optional endpoint
     */
    private static Optional<Endpoint> normalizeEndpoint(final Optional<Endpoint> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Validates and freezes management targets while preserving their declaration order.
     *
     * @param values manifest-owned named management targets
     * @return insertion-ordered immutable target map
     */
    private static Map<String, Target> normalizeManagement(final Map<String, Target> values) {
        Assert.notNull(values, "Vendor management targets must not be null");
        final Map<String, Target> copy = new LinkedHashMap<>(values.size());
        values.forEach(
                (name, target) -> copy
                        .put(managementKey(name), Assert.notNull(target, "Vendor management target must not be null")));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Validates and freezes resolved management endpoints while preserving their declaration order.
     *
     * @param values resolved named management endpoints
     * @return insertion-ordered immutable endpoint map
     */
    private static Map<String, Endpoint> normalizeManagementEndpoints(final Map<String, Endpoint> values) {
        Assert.notNull(values, "Resolved Vendor management endpoints must not be null");
        final Map<String, Endpoint> copy = new LinkedHashMap<>(values.size());
        values.forEach(
                (name, endpoint) -> copy.put(
                        managementKey(name),
                        Assert.notNull(endpoint, "Resolved Vendor management endpoint must not be null")));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Validates one exact management target name without silently changing it.
     *
     * @param value caller-supplied management target name
     * @return original validated management target name
     */
    private static String managementKey(final String value) {
        final String key = Assert.notBlank(value, "Vendor management target name must not be blank");
        if (!key.equals(key.trim())) {
            throw new ValidateException("Vendor management target name must not contain surrounding whitespace");
        }
        return key;
    }

    /**
     * Resolves one optional target.
     *
     * @param target  optional manifest-owned endpoint target
     * @param options validated external Source options
     * @return optional resolved endpoint
     */
    private static Optional<Endpoint> resolve(final Optional<Target> target, final VendorOptions<?> options) {
        return target.isPresent() ? Optional.of(target.getOrNull().resolve(options)) : Optional.empty();
    }

    /**
     * Resolves every named management target in its manifest declaration order.
     *
     * @param targets manifest-owned named management targets
     * @param options validated external Source options
     * @return insertion-ordered immutable resolved endpoint map
     */
    private static Map<String, Endpoint> resolve(final Map<String, Target> targets, final VendorOptions<?> options) {
        final Map<String, Endpoint> resolved = new LinkedHashMap<>(targets.size());
        targets.forEach((name, target) -> resolved.put(name, target.resolve(options)));
        return Collections.unmodifiableMap(resolved);
    }

    /**
     * Requires one non-blank optional template input.
     *
     * @param value optional template input
     * @param label diagnostic template variable label
     * @return required non-blank input value
     */
    private static String require(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        if (value.isEmpty()) {
            throw new ValidateException(label + " is required by the Vendor endpoint template");
        }
        return Assert.notBlank(value.getOrNull(), label + " must not be blank");
    }

    /**
     * Resolves every supported constrained variable in a manifest-owned HTTPS template.
     *
     * @param value   validated endpoint or issuer template
     * @param options validated platform options
     * @return resolved template text
     */
    private static String resolveValue(final String value, final VendorOptions<?> options) {
        String resolved = value;
        if (resolved.contains(TENANT)) {
            resolved = resolved.replace(TENANT, segment(require(options.templateTenant(), "Vendor tenant")));
        }
        if (resolved.contains(AUTHORIZATION_SERVER_ID)) {
            resolved = resolved.replace(
                    AUTHORIZATION_SERVER_ID,
                    segment(require(options.templateAuthorizationServerId(), "Vendor authorization server id")));
        }
        if (resolved.contains(INSTANCE)) {
            final String instance = require(options.templateInstance(), "Vendor instance");
            final String authority = authority(value);
            resolved = resolved.replace(INSTANCE, authority.equals(INSTANCE) ? dnsHost(instance) : dnsLabel(instance));
        }
        return resolved;
    }

    /**
     * Encodes an opaque value as one RFC 3986 path segment.
     *
     * @param value opaque template value
     * @return UTF-8 RFC 3986 path-segment encoding
     */
    private static String segment(final String value) {
        if (value.indexOf(Symbol.C_SLASH) >= 0 || value.indexOf(Symbol.C_BACKSLASH) >= 0) {
            throw new ValidateException("Vendor path-segment selector must not contain path separators");
        }
        return RFC3986.SEGMENT.encode(value, Charset.UTF_8);
    }

    /**
     * Extracts a template authority without interpreting path or query text.
     *
     * @param template validated HTTPS endpoint template
     * @return lexical authority portion of the template
     */
    private static String authority(final String template) {
        final int start = Protocol.HTTPS_PREFIX.length();
        final int slash = template.indexOf(Symbol.C_SLASH, start);
        final int query = template.indexOf(Symbol.C_QUESTION_MARK, start);
        int end = template.length();
        if (slash >= 0) {
            end = Math.min(end, slash);
        }
        if (query >= 0) {
            end = Math.min(end, query);
        }
        return template.substring(start, end);
    }

    /**
     * Validates one complete DNS host and returns its canonical ASCII representation.
     *
     * @param value externally supplied instance host
     * @return lowercase IDNA ASCII host
     */
    private static String dnsHost(final String value) {
        final String ascii;
        try {
            ascii = IDN.toASCII(Assert.notBlank(value, "Vendor instance must not be blank"));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Vendor instance is not a valid DNS host", cause);
        }
        if (ascii.length() > 253 || ascii.endsWith(Symbol.DOT) || PatternKit.isMatch(Regex.IP_ADDRESS, ascii)) {
            throw new ValidateException("Vendor instance must be a canonical DNS host rather than an IP address");
        }
        for (String label : ascii.split("\\.", -1)) {
            dnsLabel(label);
        }
        return ascii.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Validates one DNS label and returns its canonical ASCII representation.
     *
     * @param value externally supplied instance label
     * @return lowercase IDNA ASCII label
     */
    private static String dnsLabel(final String value) {
        final String ascii;
        try {
            ascii = IDN.toASCII(Assert.notBlank(value, "Vendor instance label must not be blank"));
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Vendor instance label is not valid", cause);
        }
        if (ascii.length() > 63 || ascii.indexOf(Symbol.C_DOT) >= 0 || ascii.startsWith(Symbol.MINUS)
                || ascii.endsWith(Symbol.MINUS) || !ascii.chars()
                        .allMatch(character -> Character.isLetterOrDigit(character) || character == Symbol.C_MINUS)) {
            throw new ValidateException("Vendor instance must be one canonical DNS label");
        }
        return ascii.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Validates the immutable syntax and placement of every supported template variable.
     *
     * @param value manifest-owned HTTPS endpoint template
     */
    private static void validateTemplate(final String value) {
        if (!value.startsWith(Protocol.HTTPS_PREFIX) || value.indexOf(Symbol.C_HASH) >= 0) {
            throw new ValidateException("Vendor endpoint template must be HTTPS and must not contain a fragment");
        }
        int opening = 0;
        int closing = 0;
        for (int index = 0; index < value.length(); index++) {
            opening += value.charAt(index) == Symbol.C_BRACE_LEFT ? 1 : 0;
            closing += value.charAt(index) == Symbol.C_BRACE_RIGHT ? 1 : 0;
        }
        if (opening != closing) {
            throw new ValidateException("Vendor endpoint template contains unmatched braces");
        }
        String remainder = value;
        for (Variable variable : Variable.values()) {
            remainder = remainder.replace(variable.token, Normal.EMPTY);
        }
        if (remainder.indexOf(Symbol.C_BRACE_LEFT) >= 0 || remainder.indexOf(Symbol.C_BRACE_RIGHT) >= 0) {
            throw new ValidateException("Vendor endpoint template contains an unsupported variable");
        }
        final String authority = authority(value);
        if (authority.indexOf(Symbol.C_AT) >= 0) {
            throw new ValidateException("Vendor endpoint template must not contain user information");
        }
        if (value.contains(INSTANCE) && !(authority.equals(INSTANCE) || authority.startsWith(INSTANCE + Symbol.DOT))) {
            throw new ValidateException(
                    "Vendor instance variable must occupy the complete host or its first DNS label");
        }
        if (authority.contains(TENANT) || authority.contains(AUTHORIZATION_SERVER_ID)) {
            throw new ValidateException("Vendor tenant and authorization server id variables must be path segments");
        }
        final String parseable = parseable(value);
        final Url url;
        try {
            url = Url.parse(parseable);
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Vendor endpoint template is malformed", cause);
        }
        if (!Protocol.HTTPS.name.equalsIgnoreCase(url.scheme()) || url.host() == null || !url.username().isEmpty()
                || url.fragment() != null) {
            throw new ValidateException("Vendor endpoint template must remain a credential-free absolute HTTPS URI");
        }
    }

    /**
     * Replaces constrained variables with safe lexical examples for construction-time URI validation.
     *
     * @param value endpoint or issuer template
     * @return template text suitable for URI parsing
     */
    private static String parseable(final String value) {
        final String authority = authority(value);
        return value.replace(TENANT, "tenant").replace(AUTHORIZATION_SERVER_ID, "server")
                .replace(INSTANCE, authority.equals(INSTANCE) ? "example.com" : "instance");
    }

    /**
     * Resolves every present endpoint against one platform Source options record.
     *
     * @param options exact platform options selected by its Vendor manifest
     * @return immutable fully resolved endpoint set
     */
    public Resolved resolve(final VendorOptions<?> options) {
        final VendorOptions<?> checkedOptions = Assert.notNull(options, "Vendor Source options must not be null");
        return new Resolved(resolve(authorization, checkedOptions), resolve(token, checkedOptions),
                resolve(userInfo, checkedOptions), resolve(refresh, checkedOptions),
                resolve(introspection, checkedOptions), resolve(revocation, checkedOptions),
                resolve(deviceAuthorization, checkedOptions), resolve(discovery, checkedOptions),
                resolve(jwks, checkedOptions), resolve(endSession, checkedOptions),
                resolve(management, checkedOptions));
    }

    /**
     * Defines the constrained target-template variables understood by deterministic resolution.
     *
     * @author Kimi Liu
     */
    private enum Variable {

        /**
         * Tenant path-segment selector.
         */
        TENANT("{tenant}"),

        /**
         * Platform DNS instance selector.
         */
        INSTANCE("{instance}"),

        /**
         * Authorization-server path-segment selector.
         */
        AUTHORIZATION_SERVER_ID("{authorizationServerId}");

        /**
         * Exact template token.
         */
        private final String token;

        /**
         * Creates one constrained template variable.
         *
         * @param token exact brace-delimited token
         */
        Variable(final String token) {
            this.token = token;
        }

    }

    /**
     * Represents one fixed or constrained-template endpoint target.
     *
     * @author Kimi Liu
     */
    public interface Target {

        /**
         * Resolves this target without performing network activity.
         *
         * @param options exact platform options
         * @return fully resolved immutable endpoint
         */
        Endpoint resolve(VendorOptions<?> options);

    }

    /**
     * Holds a complete immutable official endpoint that accepts no deployment address override.
     *
     * @param endpoint complete endpoint
     * @author Kimi Liu
     */
    public record Fixed(Endpoint endpoint) implements Target {

        /**
         * Validates a fixed endpoint target.
         *
         * @throws IllegalArgumentException if the endpoint is null
         */
        public Fixed {
            Assert.notNull(endpoint, "Fixed Vendor endpoint must not be null");
        }

        /**
         * Returns the fixed endpoint without reading deployment options.
         *
         * @param options exact platform options
         * @return fixed endpoint
         */
        @Override
        public Endpoint resolve(final VendorOptions<?> options) {
            Assert.notNull(options, "Vendor Source options must not be null");
            return endpoint;
        }

    }

    /**
     * Holds one manifest-owned HTTPS endpoint template with a constrained host or path-segment substitution.
     *
     * @param value             immutable official HTTPS template
     * @param method            HTTP request method
     * @param authentication    supported endpoint authentication methods
     * @param minimumTlsVersion optional minimum TLS version
     * @param clientAuth        TLS client authentication requirement
     * @author Kimi Liu
     */
    public record Template(String value, Http.Method method, Set<Endpoint.Authentication> authentication,
            Optional<TlsVersion> minimumTlsVersion, TlsClientAuth clientAuth) implements Target {

        /**
         * Validates and freezes one constrained official endpoint template.
         *
         * @throws IllegalArgumentException if a component or collection member is null
         * @throws ValidateException        if the template is not HTTPS or contains an unsupported or misplaced
         *                                  variable
         */
        public Template {
            Assert.notBlank(value, "Vendor endpoint template must not be blank");
            Assert.notNull(method, "Vendor endpoint template HTTP method must not be null");
            Assert.notNull(authentication, "Vendor endpoint template authentication must not be null");
            final Set<Endpoint.Authentication> methods = new HashSet<>(authentication.size());
            for (Endpoint.Authentication item : authentication) {
                methods.add(Assert.notNull(item, "Vendor endpoint authentication method must not be null"));
            }
            authentication = Set.copyOf(methods);
            Assert.notNull(minimumTlsVersion, "Vendor endpoint minimum TLS version container must not be null");
            minimumTlsVersion = Optional.ofNullable(minimumTlsVersion.getOrNull());
            Assert.notNull(clientAuth, "Vendor endpoint TLS client authentication must not be null");
            validateTemplate(value);
        }

        /**
         * Resolves the constrained host and path variables into a complete HTTPS endpoint.
         *
         * @param options exact platform options
         * @return fully resolved immutable endpoint
         * @throws ValidateException if a required value is absent or is not valid for its constrained position
         */
        @Override
        public Endpoint resolve(final VendorOptions<?> options) {
            final VendorOptions<?> checkedOptions = Assert.notNull(options, "Vendor Source options must not be null");
            final String resolved = resolveValue(value, checkedOptions);
            final Url url = Url.parse(resolved);
            if (!Protocol.HTTPS.name.equalsIgnoreCase(url.scheme()) || !url.username().isEmpty()
                    || url.fragment() != null) {
                throw new ValidateException(
                        "Resolved Vendor endpoint must remain credential-free HTTPS without fragment");
            }
            return new Endpoint(url, Endpoint.Transport.HTTPS, Optional.of(method), authentication, minimumTlsVersion,
                    clientAuth);
        }

    }

    /**
     * Contains the complete endpoints resolved from targets in the same semantic order as {@link VendorTargets}.
     *
     * @param authorization       authorization endpoint
     * @param token               token endpoint
     * @param userInfo            user information endpoint
     * @param refresh             refresh endpoint
     * @param introspection       introspection or token-info endpoint
     * @param revocation          revocation endpoint
     * @param deviceAuthorization device authorization endpoint
     * @param discovery           discovery endpoint
     * @param jwks                JWK Set endpoint
     * @param endSession          end-session endpoint
     * @param management          ordered named enterprise management endpoints
     * @author Kimi Liu
     */
    public record Resolved(Optional<Endpoint> authorization, Optional<Endpoint> token, Optional<Endpoint> userInfo,
            Optional<Endpoint> refresh, Optional<Endpoint> introspection, Optional<Endpoint> revocation,
            Optional<Endpoint> deviceAuthorization, Optional<Endpoint> discovery, Optional<Endpoint> jwks,
            Optional<Endpoint> endSession, Map<String, Endpoint> management) {

        /**
         * Normalizes every resolved endpoint container.
         *
         * @param authorization       authorization endpoint
         * @param token               token endpoint
         * @param userInfo            user information endpoint
         * @param refresh             refresh endpoint
         * @param introspection       introspection or token-info endpoint
         * @param revocation          revocation endpoint
         * @param deviceAuthorization device authorization endpoint
         * @param discovery           discovery endpoint
         * @param jwks                JWK Set endpoint
         * @param endSession          end-session endpoint
         * @param management          ordered named enterprise management endpoints
         * @throws IllegalArgumentException if an optional container or management endpoint is null
         * @throws ValidateException        if a management endpoint key is blank or contains surrounding whitespace
         */
        public Resolved {
            authorization = normalizeEndpoint(authorization, "Resolved Vendor authorization endpoint");
            token = normalizeEndpoint(token, "Resolved Vendor token endpoint");
            userInfo = normalizeEndpoint(userInfo, "Resolved Vendor user information endpoint");
            refresh = normalizeEndpoint(refresh, "Resolved Vendor refresh endpoint");
            introspection = normalizeEndpoint(introspection, "Resolved Vendor introspection endpoint");
            revocation = normalizeEndpoint(revocation, "Resolved Vendor revocation endpoint");
            deviceAuthorization = normalizeEndpoint(
                    deviceAuthorization,
                    "Resolved Vendor device authorization endpoint");
            discovery = normalizeEndpoint(discovery, "Resolved Vendor discovery endpoint");
            jwks = normalizeEndpoint(jwks, "Resolved Vendor JWK Set endpoint");
            endSession = normalizeEndpoint(endSession, "Resolved Vendor end-session endpoint");
            management = normalizeManagementEndpoints(management);
        }

    }

}
