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
package org.miaixz.bus.fabric.network.dns.secure.quic;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.server.DnsTransport;

/**
 * DNS-over-QUIC optional dependency isolation.
 *
 * <p>
 * This utility performs all QUIC class-path probing through class names so UDP, TCP, DoT, and DoH runtime paths do not
 * link to Netty Incubator QUIC unless a DoQ endpoint or upstream is explicitly configured.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsQuicRuntime {

    /**
     * Netty Incubator QUIC marker class.
     */
    public static final String QUIC_CLASS = "io.netty.incubator.codec.quic.Quic";

    /**
     * Optional dependency coordinate required by DNS-over-QUIC transports.
     */
    public static final String DEPENDENCY_COORDINATE = "io.netty.incubator:netty-incubator-codec-native-quic";

    /**
     * DNS-over-QUIC ALPN identifier.
     */
    public static final String ALPN = DnsTransport.DOQ.alpn();

    /**
     * Prevents instantiation of this utility class.
     */
    private DnsQuicRuntime() {
        // No initialization required.
    }

    /**
     * Returns whether the optional QUIC runtime dependency is present.
     *
     * @return true when the Netty QUIC marker class can be loaded
     */
    public static boolean available() {
        try {
            Class.forName(QUIC_CLASS, false, DnsQuicRuntime.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Validates that DNS-over-QUIC can be started.
     *
     * @param endpoint endpoint or upstream label used in the error message
     * @throws ValidateException when the optional dependency is absent
     */
    public static void requireAvailable(final String endpoint) {
        if (!available()) {
            throw new ValidateException(
                    "DNS-over-QUIC " + endpoint + " requires optional dependency " + DEPENDENCY_COORDINATE);
        }
    }

    /**
     * Creates the fixed adapter-missing error used after dependency probing succeeds.
     *
     * @param endpoint endpoint or upstream label used in the error message
     * @return validation exception describing the missing adapter
     */
    public static ValidateException adapterUnavailable(final String endpoint) {
        return new ValidateException("DNS-over-QUIC " + endpoint + " requires the isolated bus-fabric DoQ adapter");
    }

}
