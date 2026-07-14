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
package org.miaixz.bus.crypto.builtin;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable TLS handshake metadata.
 *
 * @param protocol negotiated protocol
 * @param cipher   negotiated cipher
 * @param peer     peer certificate chain
 * @author Kimi Liu
 * @since Java 21+
 */
public record TlsHandshake(String protocol, String cipher, CertificateChain peer) {

    /**
     * Creates handshake metadata.
     */
    public TlsHandshake {
        protocol = validateToken(protocol, "TLS protocol");
        cipher = validateToken(cipher, "TLS cipher");
        peer = Assert.notNull(peer, () -> new ValidateException("Peer certificate chain must not be null"));
    }

    /**
     * Creates handshake metadata.
     *
     * @param protocol protocol
     * @param cipher   cipher
     * @param peer     peer chain
     * @return handshake
     */
    public static TlsHandshake of(final String protocol, final String cipher, final CertificateChain peer) {
        return new TlsHandshake(protocol, cipher, peer);
    }

    /**
     * Returns the negotiated protocol.
     *
     * @return protocol
     */
    @Override
    public String protocol() {
        return protocol;
    }

    /**
     * Returns the negotiated cipher.
     *
     * @return cipher
     */
    @Override
    public String cipher() {
        return cipher;
    }

    /**
     * Returns the peer certificate chain.
     *
     * @return peer chain
     */
    @Override
    public CertificateChain peer() {
        return peer;
    }

    /**
     * Returns whether the handshake has security metadata.
     *
     * @return true when secure
     */
    public boolean secure() {
        return !protocol.isBlank() && !cipher.isBlank() && !peer.empty();
    }

    /**
     * Validates a single-line token.
     *
     * @param value value
     * @param name  field name
     * @return token
     */
    private static String validateToken(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value.trim();
    }

}
