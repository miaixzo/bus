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
package org.miaixz.bus.auth.vendor.microsoft;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorOptions;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Carries externally managed Microsoft web application and tenant values.
 * <p>
 * Official cloud hosts and endpoint paths remain manifest-owned. This record retains only the Application ID, an
 * external Client Secret reference, the exact registered callback, delegated scopes, and the tenant path selector.
 * </p>
 *
 * @param vendor      exact Microsoft platform identifier
 * @param variant     selected global or China cloud variant
 * @param clientId    registered Application (client) ID
 * @param credential  external Client Secret reference
 * @param redirectUri exact registered authorization callback URI
 * @param scopes      ordered delegated scopes, or empty to use manifest defaults
 * @param tenant      Microsoft tenant GUID, verified domain, or registered audience alias
 * @author Kimi Liu
 */
public record MicrosoftOptions(Vendor.Id vendor, Vendor.Variant variant, String clientId,
        Credential.Reference credential, Optional<String> redirectUri, List<String> scopes, String tenant)
        implements VendorOptions<MicrosoftOptions> {

    /**
     * Delegated permission required by the private Microsoft Graph current-user operation.
     */
    private static final String USER_READ = "User.Read";

    /**
     * Validates and freezes one Microsoft Source registration without resolving its Client Secret.
     *
     * @throws IllegalArgumentException if a required component, container, or scope is {@code null} or blank
     * @throws ValidateException        if routing, identifiers, credential type, callback, scopes, or tenant are
     *                                  invalid
     */
    public MicrosoftOptions {
        if (!MicrosoftManifest.ID.equals(vendor)
                || !MicrosoftManifest.GLOBAL.equals(variant) && !MicrosoftManifest.CHINA.equals(variant)) {
            throw new ValidateException("Microsoft options must select microsoft/global or microsoft/china");
        }
        clientId = applicationId(clientId);
        Assert.notNull(credential, "Microsoft credential reference must not be null");
        if (credential.type() != Credential.Type.CLIENT_SECRET) {
            throw new ValidateException("Microsoft credential must reference a Client Secret");
        }
        Assert.notNull(redirectUri, "Microsoft redirect URI container must not be null");
        redirectUri = Optional.ofNullable(redirectUri.getOrNull());
        if (redirectUri.isEmpty()) {
            throw new ValidateException("Microsoft options require a registered redirect URI");
        }
        redirect(redirectUri.getOrNull());

        Assert.notNull(scopes, "Microsoft scopes must not be null");
        final List<String> copy = new ArrayList<>(scopes.size());
        for (String scope : scopes) {
            final String checked = Assert.notBlank(scope, "Microsoft scope must not be blank");
            Scope.parse(checked);
            if (copy.contains(checked)) {
                throw new ValidateException("Microsoft scopes must not contain duplicates");
            }
            copy.add(checked);
        }
        if (!copy.isEmpty() && !copy.contains(USER_READ)) {
            throw new ValidateException("Explicit Microsoft scopes must include User.Read for Graph identity lookup");
        }
        scopes = List.copyOf(copy);
        tenant = tenant(tenant);
        if (MicrosoftManifest.CHINA.equals(variant) && "consumers".equals(tenant)) {
            throw new ValidateException("Microsoft China does not support the consumers tenant audience");
        }
    }

    /**
     * Validates and canonicalizes one Microsoft Application ID UUID.
     *
     * @param value externally loaded Application ID
     * @return canonical lowercase UUID text
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the value is not a canonical UUID
     */
    private static String applicationId(final String value) {
        final String checked = Assert.notBlank(value, "Microsoft Application ID must not be blank");
        try {
            final String canonical = UUID.fromString(checked).toString();
            if (!canonical.equalsIgnoreCase(checked)) {
                throw new ValidateException("Microsoft Application ID must use canonical UUID syntax");
            }
            return canonical;
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Microsoft Application ID must be a UUID", cause);
        }
    }

    /**
     * Validates an exact registered redirect URI for a confidential web client.
     * <p>
     * HTTPS is required for remote hosts. Microsoft development registrations may use HTTP only on a literal loopback
     * host; user information and fragments are prohibited for both forms.
     * </p>
     *
     * @param value registered callback URI
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the URI is not an accepted absolute callback
     */
    private static void redirect(final String value) {
        final String checked = Assert.notBlank(value, "Microsoft redirect URI must not be blank");
        try {
            final URI uri = new URI(checked);
            final String host = uri.getHost();
            final boolean secure = Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme());
            final boolean loopback = Protocol.HTTP.name.equalsIgnoreCase(uri.getScheme()) && loopback(host);
            if (!uri.isAbsolute() || host == null || !secure && !loopback || uri.getRawUserInfo() != null
                    || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "Microsoft redirect URI must be credential-free HTTPS or HTTP loopback without fragment");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("Microsoft redirect URI is invalid", cause);
        }
    }

    /**
     * Reports whether a parsed callback host is a literal local-development loopback.
     *
     * @param host parsed URI host
     * @return {@code true} for localhost, IPv4 loopback, or IPv6 loopback
     */
    private static boolean loopback(final String host) {
        return host != null && (Protocol.HOST_LOCAL.equalsIgnoreCase(host) || Protocol.HOST_IPV4.equals(host)
                || Protocol.HOST_IPV6.equals(host) || Protocol.HOST_IPV6_BRACKETED.equals(host));
    }

    /**
     * Validates and canonicalizes one Microsoft tenant selector.
     *
     * @param value tenant alias, GUID, or verified DNS domain
     * @return canonical tenant value
     * @throws IllegalArgumentException if the value is blank
     * @throws ValidateException        if the value cannot be safely substituted as a tenant path segment
     */
    private static String tenant(final String value) {
        final String checked = Assert.notBlank(value, "Microsoft tenant must not be blank");
        final String lower = checked.toLowerCase(Locale.ROOT);
        if (tenantAlias(lower)) {
            if (!checked.equals(lower)) {
                throw new ValidateException("Microsoft tenant aliases must use lowercase canonical spelling");
            }
            return lower;
        }
        try {
            final String uuid = UUID.fromString(checked).toString();
            if (!uuid.equalsIgnoreCase(checked)) {
                throw new ValidateException("Microsoft tenant UUID must use canonical syntax");
            }
            return uuid;
        } catch (IllegalArgumentException ignored) {
            return domain(checked);
        }
    }

    /**
     * Identifies a tenant audience alias supported by the Microsoft identity platform.
     *
     * @param value lowercase tenant selector
     * @return whether the selector is a registered audience alias
     */
    private static boolean tenantAlias(final String value) {
        return "common".equals(value) || "organizations".equals(value) || "consumers".equals(value);
    }

    /**
     * Validates one ASCII tenant domain without performing DNS resolution.
     *
     * @param value tenant domain text
     * @return canonical lowercase ASCII domain
     * @throws ValidateException if labels violate DNS host syntax or the input is not already ASCII
     */
    private static String domain(final String value) {
        final String ascii;
        try {
            ascii = IDN.toASCII(value, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("Microsoft tenant must be an alias, UUID, or DNS domain", cause);
        }
        if (!value.equalsIgnoreCase(ascii) || ascii.length() > 253 || ascii.indexOf(Symbol.C_DOT) < 1) {
            throw new ValidateException("Microsoft tenant domain must use canonical ASCII DNS syntax");
        }
        for (String label : ascii.split("\\.", -1)) {
            if (label.isEmpty() || label.length() > 63 || label.charAt(0) == Symbol.C_MINUS
                    || label.charAt(label.length() - 1) == Symbol.C_MINUS) {
                throw new ValidateException("Microsoft tenant domain contains an invalid DNS label");
            }
        }
        return ascii;
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<MicrosoftOptions> type() {
        return MicrosoftOptions.class;
    }

    /**
     * Returns the tenant value consumed by manifest-owned endpoint templates.
     *
     * @return present validated tenant path segment
     */
    @Override
    public Optional<String> templateTenant() {
        return Optional.of(tenant);
    }

    /**
     * Returns a diagnostic representation without application, credential, or callback values.
     *
     * @return redacted options description
     */
    @Override
    public String toString() {
        return "MicrosoftOptions[vendor=" + vendor + ", variant=" + variant
                + ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=" + scopes + ", tenant="
                + tenant + Symbol.C_BRACKET_RIGHT;
    }

}
