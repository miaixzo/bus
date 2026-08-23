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
package org.miaixz.bus.auth.source.protocol.saml;

import java.util.List;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML Metadata {@code KeyDescriptorType} with constrained use and typed secure-XML material.
 *
 * @param use               optional standard key use
 * @param keyInfo           required complete {@code ds:KeyInfo} element
 * @param encryptionMethods ordered complete {@code md:EncryptionMethod} elements
 * @author Kimi Liu
 */
public record KeyDescriptor(Optional<Use> use, KeyInfo keyInfo, List<EncryptionMethod> encryptionMethods) {

    /**
     * Validates and freezes one Metadata key descriptor.
     *
     * @throws IllegalArgumentException if a component, item, or optional container is {@code null}
     */
    public KeyDescriptor {
        Assert.notNull(use, "SAML KeyDescriptor use container must not be null");
        use = Optional.ofNullable(use.getOrNull());
        Assert.notNull(keyInfo, "SAML KeyDescriptor KeyInfo must not be null");
        Assert.notNull(encryptionMethods, "SAML KeyDescriptor EncryptionMethod list must not be null");
        encryptionMethods = List.copyOf(encryptionMethods);
        encryptionMethods
                .forEach(method -> Assert.notNull(method, "SAML KeyDescriptor EncryptionMethod must not be null"));
    }

    /**
     * Detaches and validates one complete XML element representation.
     *
     * @param source source bytes
     * @param label  safe element label
     * @return detached non-empty bytes
     */
    private static byte[] bytes(final byte[] source, final String label) {
        final byte[] value = Assert.notNull(source, label + " XML must not be null");
        if (value.length == 0) {
            throw new ValidateException(label + " XML must not be empty");
        }
        return value.clone();
    }

    /**
     * Defines the two standard Metadata {@code KeyTypes} lexical values.
     *
     * @author Kimi Liu
     */
    public enum Use {

        /**
         * Key material used for XML signatures.
         */
        SIGNING(Builder.SIGNING),

        /**
         * Key material used for XML encryption.
         */
        ENCRYPTION("encryption");

        /**
         * Exact XML lexical value.
         */
        private final String value;

        /**
         * Creates one constrained Metadata key use.
         *
         * @param value exact XML lexical value
         */
        Use(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact XML lexical value.
         *
         * @return {@code signing} or {@code encryption}
         */
        public String value() {
            return value;
        }

    }

    /**
     * Carries one complete securely parsed XML Signature {@code KeyInfo} element.
     *
     * @param xml detached complete element bytes
     * @author Kimi Liu
     */
    public record KeyInfo(byte[] xml) {

        /**
         * Takes defensive ownership of the complete element.
         *
         * @throws IllegalArgumentException if {@code xml} is {@code null}
         * @throws ValidateException        if {@code xml} is empty
         */
        public KeyInfo {
            xml = bytes(xml, "SAML KeyInfo");
        }

        /**
         * Returns detached complete element bytes.
         *
         * @return caller-owned XML bytes
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

    /**
     * Carries one complete securely parsed Metadata {@code EncryptionMethod} element.
     *
     * @param xml detached complete element bytes
     * @author Kimi Liu
     */
    public record EncryptionMethod(byte[] xml) {

        /**
         * Takes defensive ownership of the complete element.
         *
         * @throws IllegalArgumentException if {@code xml} is {@code null}
         * @throws ValidateException        if {@code xml} is empty
         */
        public EncryptionMethod {
            xml = bytes(xml, "SAML EncryptionMethod");
        }

        /**
         * Returns detached complete element bytes.
         *
         * @return caller-owned XML bytes
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

}
