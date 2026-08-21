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
package org.miaixz.bus.auth.protocol.ldap.client;

import java.util.*;

import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.ldap.DistinguishedName;
import org.miaixz.bus.auth.protocol.ldap.SearchRequest;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Holds immutable deployment options for one LDAPv3 directory Source.
 * <p>
 * The Source always protects credentials with either implicit TLS or the standard StartTLS extended operation. Optional
 * service-account credentials are retained only as an external password reference; user credentials remain invocation
 * input and never become options. The Source driver additionally constrains the message limit against the shared LDAP
 * security baseline.
 * </p>
 *
 * @param host                remote LDAP server host name or address
 * @param port                remote LDAP server port in the registered TCP port range
 * @param securityMode        mandatory protected-transport establishment mode
 * @param searchBase          base distinguished name for locating the authenticating user
 * @param usernameAttribute   RFC 4512 attribute description matched against the supplied principal hint
 * @param attributes          ordered, case-insensitively unique attributes returned to identity mapping
 * @param bindDn              optional service-account distinguished name used before the user search
 * @param bindCredential      optional external password reference paired with {@code bindDn}
 * @param timeLimitSeconds    positive LDAP search time limit in seconds
 * @param maximumMessageBytes positive maximum encoded LDAP message size
 * @param maximumBerDepth     positive maximum nested BER depth
 * @author Kimi Liu
 */
public record LdapClientOptions(String host, int port, SecurityMode securityMode, DistinguishedName searchBase,
        String usernameAttribute, List<String> attributes, Optional<DistinguishedName> bindDn,
        Optional<Credential.Reference> bindCredential, int timeLimitSeconds, long maximumMessageBytes,
        int maximumBerDepth) implements Options<LdapClientOptions> {

    /**
     * Validates protected transport, search mapping, external credential pairing, and local positive limits.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if a host, port, attribute, credential type, or numeric limit is invalid
     */
    public LdapClientOptions {
        host = normalizeHost(host);
        if (port < Normal._1 || port > Normal._65535) {
            throw new ValidateException("LDAP Source port must be between 1 and 65535");
        }
        Assert.notNull(securityMode, "LDAP Source security mode must not be null");
        Assert.notNull(searchBase, "LDAP Source search base must not be null");
        usernameAttribute = attributeDescription(usernameAttribute, "LDAP Source username attribute");
        attributes = attributes(attributes, usernameAttribute);

        Assert.notNull(bindDn, "LDAP Source bind DN container must not be null");
        bindDn = Optional.ofNullable(bindDn.getOrNull());
        Assert.notNull(bindCredential, "LDAP Source bind credential container must not be null");
        bindCredential = Optional.ofNullable(bindCredential.getOrNull());
        if (bindDn.isPresent() != bindCredential.isPresent()) {
            throw new ValidateException("LDAP Source bind DN and bind credential must be present together");
        }
        final Credential.Reference serviceCredential = bindCredential.getOrNull();
        if (serviceCredential != null && serviceCredential.type() != Credential.Type.PASSWORD) {
            throw new ValidateException("LDAP Source bind credential must reference password material");
        }
        if (timeLimitSeconds <= 0 || maximumMessageBytes <= 0 || maximumBerDepth <= 0) {
            throw new ValidateException("LDAP Source time, message, and BER depth limits must be positive");
        }
    }

    /**
     * Normalizes one single-line remote host without attempting DNS resolution.
     *
     * @param value configured host name or address
     * @return trimmed single-line host
     * @throws ValidateException if the host is blank or contains a line break
     */
    private static String normalizeHost(final String value) {
        final String current = Assert.notBlank(value, "LDAP Source host must not be blank").trim();
        if (current.indexOf(Symbol.C_CR) >= 0 || current.indexOf(Symbol.C_LF) >= 0) {
            throw new ValidateException("LDAP Source host must be a single-line value");
        }
        return current;
    }

    /**
     * Validates an attribute description through the canonical LDAP search-filter model.
     *
     * @param value candidate RFC 4512 attribute description
     * @param label safe field label used in validation diagnostics
     * @return unchanged valid attribute description
     */
    private static String attributeDescription(final String value, final String label) {
        final String current = Assert.notBlank(value, label + " must not be blank");
        new SearchRequest.Present(current);
        return current;
    }

    /**
     * Freezes the ordered case-insensitively unique identity attribute projection.
     *
     * @param values            configured attribute descriptions
     * @param usernameAttribute required username attribute description
     * @return immutable ordered attribute projection
     * @throws ValidateException if the list is empty, contains a duplicate, or omits the username attribute
     */
    private static List<String> attributes(final List<String> values, final String usernameAttribute) {
        Assert.notNull(values, "LDAP Source identity attributes must not be null");
        if (values.isEmpty()) {
            throw new ValidateException("LDAP Source identity attributes must not be empty");
        }
        final List<String> copy = new ArrayList<>(values.size());
        final Set<String> names = new HashSet<>(values.size());
        for (String value : values) {
            final String attribute = attributeDescription(value, "LDAP Source identity attribute");
            final String canonical = attribute.toLowerCase(Locale.ROOT);
            if (!names.add(canonical)) {
                throw new ValidateException("LDAP Source identity attributes must be case-insensitively unique");
            }
            copy.add(attribute);
        }
        if (!names.contains(usernameAttribute.toLowerCase(Locale.ROOT))) {
            throw new ValidateException("LDAP Source identity attributes must include the username attribute");
        }
        return List.copyOf(copy);
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<LdapClientOptions> type() {
        return LdapClientOptions.class;
    }

    @Override
    public LdapClientOptions snapshot() {
        return this;
    }

    /**
     * Returns a diagnostic representation that never exposes the external service credential identifier.
     *
     * @return redacted immutable options summary
     */
    @Override
    public String toString() {
        return "LdapClientOptions[host=" + host + ", port=" + port + ", securityMode=" + securityMode + ", searchBase="
                + searchBase + ", usernameAttribute=" + usernameAttribute + ", attributes=" + attributes + ", bindDn="
                + (bindDn.isPresent() ? "[PRESENT]" : "[ABSENT]") + ", bindCredential="
                + (bindCredential.isPresent() ? "[REDACTED]" : "[ABSENT]") + ", timeLimitSeconds=" + timeLimitSeconds
                + ", maximumMessageBytes=" + maximumMessageBytes + ", maximumBerDepth=" + maximumBerDepth
                + Symbol.BRACKET_RIGHT;
    }

    /**
     * Enumerates the two protected LDAP transport establishment modes supported by the Source.
     *
     * @author Kimi Liu
     */
    public enum SecurityMode {

        /**
         * Opens LDAP directly over a TLS-protected TCP connection.
         */
        LDAPS,

        /**
         * Opens TCP and performs the standard LDAP StartTLS extended operation before Bind.
         */
        START_TLS

    }

}
