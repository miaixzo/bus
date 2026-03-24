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
package org.miaixz.bus.core.net.tls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.net.Protocol;

/**
 * The TLS versions that can be offered when negotiating a secure socket. See
 * {@link javax.net.ssl.SSLSocket#setEnabledProtocols}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum TlsVersion {

    /**
     * TLS 1.3, released in 2018.
     */
    TLSv1_3(Protocol.TLSv1_3.name),
    /**
     * TLS 1.2, released in 2008.
     */
    TLSv1_2(Protocol.TLSv1_2.name),
    /**
     * TLS 1.1, released in 2006.
     */
    TLSv1_1(Protocol.TLSv1_1.name),
    /**
     * TLS 1.0, released in 1999.
     */
    TLSv1(Protocol.TLSv1.name),
    /**
     * SSL 3.0, released in 1996.
     */
    SSLv3(Protocol.SSLv3.name);

    /**
     * The name of the TLS version in the Java runtime.
     */
    public final String javaName;

    /**
     * Constructs a new TlsVersion.
     *
     * @param javaName The name of the TLS version in the Java runtime.
     */
    TlsVersion(String javaName) {
        this.javaName = javaName;
    }

    /**
     * Returns the TlsVersion for the given Java name.
     *
     * @param javaName The name of the TLS version in the Java runtime.
     * @return The TlsVersion.
     * @throws IllegalArgumentException if the TLS version is not supported.
     */
    public static TlsVersion forJavaName(String javaName) {
        if (Protocol.TLSv1_3.name.equals(javaName)) {
            return TLSv1_3;
        }
        if (Protocol.TLSv1_2.name.equals(javaName)) {
            return TLSv1_2;
        }
        if (Protocol.TLSv1_1.name.equals(javaName)) {
            return TLSv1_1;
        }
        if (Protocol.TLSv1.name.equals(javaName)) {
            return TLSv1;
        }
        if (Protocol.SSLv3.name.equals(javaName)) {
            return SSLv3;
        }
        throw new IllegalArgumentException("Unexpected TLS version: " + javaName);
    }

    /**
     * Returns a list of TlsVersions for the given Java names.
     *
     * @param tlsVersions The names of the TLS versions in the Java runtime.
     * @return A list of TlsVersions.
     */
    public static List<TlsVersion> forJavaNames(String... tlsVersions) {
        List<TlsVersion> result = new ArrayList<>(tlsVersions.length);
        for (String tlsVersion : tlsVersions) {
            result.add(forJavaName(tlsVersion));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the name of the TLS version in the Java runtime.
     *
     * @return The name of the TLS version.
     */
    public String javaName() {
        return javaName;
    }

}
