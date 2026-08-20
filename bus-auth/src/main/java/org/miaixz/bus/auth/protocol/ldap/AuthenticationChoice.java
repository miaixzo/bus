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
package org.miaixz.bus.auth.protocol.ldap;

import java.util.Arrays;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Defines the two alternatives of the RFC 4511 {@code AuthenticationChoice} used by a BindRequest.
 * <p>
 * Authentication material is never rendered as text. Connection code must keep instances within the bind exchange and
 * must not log or persist them.
 * </p>
 *
 * @author Kimi Liu
 */
public sealed interface AuthenticationChoice permits AuthenticationChoice.Simple, AuthenticationChoice.Sasl {

    /**
     * Represents the context-specific simple password authentication alternative.
     *
     * @param password exact password OCTET STRING, which may be empty for an unauthenticated bind
     * @author Kimi Liu
     */
    record Simple(byte[] password) implements AuthenticationChoice {

        /**
         * Creates a simple authentication value with exclusive immutable storage.
         *
         * @param password password octets
         * @throws IllegalArgumentException if {@code password} is {@code null}
         */
        public Simple {
            password = Assert.notNull(password, "LDAP simple-bind password must not be null").clone();
        }

        /**
         * Returns a defensive copy of the password octets.
         *
         * @return newly allocated password bytes
         */
        @Override
        public byte[] password() {
            return password.clone();
        }

        /**
         * Compares simple credentials by octet content.
         *
         * @param other candidate object
         * @return {@code true} when both credentials contain identical octets
         */
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof Simple that && Arrays.equals(password, that.password);
        }

        /**
         * Computes a content hash for protocol-model equality.
         *
         * @return password-octet hash
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(password);
        }

        /**
         * Returns a redacted authentication description.
         *
         * @return description containing only the credential length
         */
        @Override
        public String toString() {
            return "Simple[passwordLength=" + password.length + Symbol.BRACKET_RIGHT;
        }

    }

    /**
     * Represents the context-specific SASL authentication alternative.
     *
     * @param credentials SASL mechanism and optional mechanism credentials
     * @author Kimi Liu
     */
    record Sasl(SaslCredentials credentials) implements AuthenticationChoice {

        /**
         * Creates a SASL authentication alternative.
         *
         * @param credentials SASL credentials
         * @throws IllegalArgumentException if {@code credentials} is {@code null}
         */
        public Sasl {
            Assert.notNull(credentials, "LDAP SASL credentials must not be null");
        }

    }

}
