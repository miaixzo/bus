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
package org.miaixz.bus.auth;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Enumers;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Defines references to credential material owned by an external secure storage implementation.
 * <p>
 * Authentication registrations and typed settings retain only {@link Reference} values. Plaintext passwords, client
 * secrets, private keys, shared secrets, and certificates never become fields of this contract and must not appear in
 * serialization or logs.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Credential {

    /**
     * Prevents instantiation of the credential contract namespace.
     */
    private Credential() {
        // No initialization required.
    }

    /**
     * Classifies the material addressed by a credential reference.
     *
     * @author Kimi Liu
     */
    public enum Type implements Enumers<Type> {

        /**
         * User or service password material.
         */
        PASSWORD(1),

        /**
         * OAuth or Vendor client secret material.
         */
        CLIENT_SECRET(2),

        /**
         * Asymmetric private-key material.
         */
        PRIVATE_KEY(3),

        /**
         * Symmetric shared-secret material.
         */
        SHARED_SECRET(4),

        /**
         * Public certificate or certificate-chain material.
         */
        CERTIFICATE(5);

        /**
         * Stable persistence code independent of declaration order.
         */
        private final int code;

        /**
         * Creates a credential material type with its stable persistence code.
         *
         * @param code stable persistence code
         */
        Type(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persistence code for this credential material type.
         *
         * @return stable credential type code
         */
        @Override
        public int code() {
            return code;
        }

    }

    /**
     * Identifies credential material in an external resolver without containing that material.
     *
     * @param id   opaque external credential record identifier
     * @param type expected material type used to prevent type confusion
     * @author Kimi Liu
     */
    public record Reference(String id, Type type) {

        /**
         * Creates an external credential reference.
         *
         * @param id   non-blank external credential record identifier
         * @param type expected material type
         * @throws IllegalArgumentException if the identifier is blank or the type is {@code null}
         */
        public Reference {
            Assert.notBlank(id, "Credential reference id must not be blank");
            Assert.notNull(type, "Credential reference type must not be null");
        }

        /**
         * Returns a diagnostic representation containing only the external identifier and material type.
         *
         * @return safe reference text without credential material
         */
        @Override
        public String toString() {
            return "Credential.Reference[id=" + id + ", type=" + type.name() + Symbol.BRACKET_RIGHT;
        }

    }

}
