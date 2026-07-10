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
package org.miaixz.bus.fabric.network.tls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * TLS cipher suite name mapper using JSSE Java-name semantics.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsCipherSuite {

    /**
     * Known instances by Java name.
     */
    private static final Map<String, TlsCipherSuite> INSTANCES = new LinkedHashMap<>();

    /**
     * Comparator that treats TLS_ and SSL_ prefixed Java names consistently.
     */
    public static final Comparator<String> ORDER_BY_NAME = (left, right) -> {
        for (int i = 4, limit = Math.min(left.length(), right.length()); i < limit; i++) {
            final char a = left.charAt(i);
            final char b = right.charAt(i);
            if (a != b) {
                return a < b ? -1 : 1;
            }
        }
        return Integer.compare(left.length(), right.length());
    };

    /**
     * TLS 1.3 AES-128 GCM suite with SHA-256 transcript hashing.
     */
    public static final TlsCipherSuite TLS_AES_128_GCM_SHA256 = init("TLS_AES_128_GCM_SHA256");

    /**
     * TLS 1.3 AES-256 GCM suite with SHA-384 transcript hashing.
     */
    public static final TlsCipherSuite TLS_AES_256_GCM_SHA384 = init("TLS_AES_256_GCM_SHA384");

    /**
     * TLS 1.3 ChaCha20-Poly1305 suite with SHA-256 transcript hashing.
     */
    public static final TlsCipherSuite TLS_CHACHA20_POLY1305_SHA256 = init("TLS_CHACHA20_POLY1305_SHA256");

    /**
     * ECDHE/ECDSA AES-128 GCM suite for TLS 1.2 connections.
     */
    public static final TlsCipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");

    /**
     * ECDHE/RSA AES-128 GCM suite for TLS 1.2 connections.
     */
    public static final TlsCipherSuite TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = init(
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");

    /**
     * ECDHE/ECDSA AES-256 GCM suite for TLS 1.2 connections.
     */
    public static final TlsCipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");

    /**
     * ECDHE/RSA AES-256 GCM suite for TLS 1.2 connections.
     */
    public static final TlsCipherSuite TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = init(
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");

    /**
     * ECDHE/ECDSA ChaCha20-Poly1305 suite for TLS 1.2 connections.
     */
    public static final TlsCipherSuite TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 = init(
            "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256");

    /**
     * ECDHE/RSA ChaCha20-Poly1305 suite for TLS 1.2 connections.
     */
    public static final TlsCipherSuite TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 = init(
            "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256");

    /**
     * ECDHE/RSA AES-128 CBC suite retained for peers that do not support AEAD suites.
     */
    public static final TlsCipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = init("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");

    /**
     * ECDHE/RSA AES-256 CBC suite retained for peers that do not support AEAD suites.
     */
    public static final TlsCipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = init("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");

    /**
     * RSA key-exchange AES-128 GCM suite for constrained TLS 1.2 peer compatibility.
     */
    public static final TlsCipherSuite TLS_RSA_WITH_AES_128_GCM_SHA256 = init("TLS_RSA_WITH_AES_128_GCM_SHA256");

    /**
     * RSA key-exchange AES-256 GCM suite for constrained TLS 1.2 peer compatibility.
     */
    public static final TlsCipherSuite TLS_RSA_WITH_AES_256_GCM_SHA384 = init("TLS_RSA_WITH_AES_256_GCM_SHA384");

    /**
     * RSA key-exchange AES-128 CBC suite for constrained TLS 1.0/1.1 peer compatibility.
     */
    public static final TlsCipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = init("TLS_RSA_WITH_AES_128_CBC_SHA");

    /**
     * RSA key-exchange AES-256 CBC suite for constrained TLS 1.0/1.1 peer compatibility.
     */
    public static final TlsCipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = init("TLS_RSA_WITH_AES_256_CBC_SHA");

    /**
     * RSA key-exchange 3DES suite exposed under its JSSE SSL-prefixed name.
     */
    public static final TlsCipherSuite TLS_RSA_WITH_3DES_EDE_CBC_SHA = init("SSL_RSA_WITH_3DES_EDE_CBC_SHA");

    /**
     * Java cipher suite name.
     */
    private final String javaName;

    /**
     * Creates a cipher suite mapper.
     *
     * @param javaName Java name
     */
    private TlsCipherSuite(final String javaName) {
        this.javaName = validateName(javaName);
    }

    /**
     * Returns the suite for a Java cipher name.
     *
     * @param javaName Java name
     * @return suite
     */
    public static synchronized TlsCipherSuite forJavaName(final String javaName) {
        final String name = validateName(javaName);
        TlsCipherSuite result = INSTANCES.get(name);
        if (result == null) {
            result = INSTANCES.get(secondaryName(name));
            if (result == null) {
                result = new TlsCipherSuite(name);
            }
            INSTANCES.put(name, result);
        }
        return result;
    }

    /**
     * Returns suites for Java names.
     *
     * @param javaNames Java names
     * @return suites
     */
    public static List<TlsCipherSuite> forJavaNames(final String... javaNames) {
        if (javaNames == null) {
            throw new ValidateException("TLS cipher names must not be null");
        }
        final ArrayList<TlsCipherSuite> suites = new ArrayList<>(javaNames.length);
        for (final String javaName : javaNames) {
            suites.add(forJavaName(javaName));
        }
        return List.copyOf(suites);
    }

    /**
     * Converts suites to Java names.
     *
     * @param suites suites
     * @return Java names
     */
    public static List<String> javaNames(final Collection<TlsCipherSuite> suites) {
        if (suites == null || suites.stream().anyMatch(suite -> suite == null)) {
            throw new ValidateException("TLS cipher suites must be non-null and contain no null elements");
        }
        return suites.stream().map(TlsCipherSuite::javaName).toList();
    }

    /**
     * Resolves a requested Java name against a supported cipher set, honoring SSL_/TLS_ aliases.
     *
     * @param javaName  requested name
     * @param supported supported Java names
     * @return supported Java name when an alias is present
     */
    static String resolveJavaName(final String javaName, final Set<String> supported) {
        final String name = forJavaName(javaName).javaName();
        if (supported.contains(name)) {
            return name;
        }
        final String secondary = secondaryName(name);
        if (supported.contains(secondary)) {
            return secondary;
        }
        return name;
    }

    /**
     * Returns the Java name.
     *
     * @return Java name
     */
    public String javaName() {
        return javaName;
    }

    /**
     * Returns an alternate SSL_/TLS_ Java name.
     *
     * @param javaName Java name
     * @return alternate name
     */
    private static String secondaryName(final String javaName) {
        if (javaName.startsWith("TLS_")) {
            return "SSL_" + javaName.substring(4);
        }
        if (javaName.startsWith("SSL_")) {
            return "TLS_" + javaName.substring(4);
        }
        return javaName;
    }

    /**
     * Initializes a known suite.
     *
     * @param javaName Java name
     * @return suite
     */
    private static TlsCipherSuite init(final String javaName) {
        final TlsCipherSuite suite = new TlsCipherSuite(javaName);
        INSTANCES.put(suite.javaName, suite);
        return suite;
    }

    /**
     * Validates a cipher name.
     *
     * @param javaName Java name
     * @return normalized Java name
     */
    private static String validateName(final String javaName) {
        if (StringKit.isBlank(javaName) || StringKit.containsAny(javaName, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("TLS cipher name must be non-blank and single-line");
        }
        return javaName.trim();
    }

    @Override
    public String toString() {
        return javaName;
    }

}
