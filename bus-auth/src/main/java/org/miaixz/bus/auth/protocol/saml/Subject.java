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
package org.miaixz.bus.auth.protocol.saml;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the SAML assertion {@code SubjectType} identifier/confirmation choice.
 *
 * @param identifier    optional BaseID, NameID, or EncryptedID choice
 * @param confirmations ordered subject confirmations
 * @author Kimi Liu
 */
public record Subject(Optional<Identifier> identifier, List<SubjectConfirmation> confirmations) {

    /**
     * Enforces the schema choice requiring either an identifier or at least one confirmation.
     *
     * @throws IllegalArgumentException if a container or item is {@code null}
     * @throws ValidateException        if both identifier and confirmations are absent
     */
    public Subject {
        Assert.notNull(identifier, "SAML Subject identifier container must not be null");
        identifier = Optional.ofNullable(identifier.getOrNull());
        Assert.notNull(confirmations, "SAML Subject confirmation list must not be null");
        for (SubjectConfirmation confirmation : confirmations) {
            Assert.notNull(confirmation, "SAML SubjectConfirmation must not be null");
        }
        confirmations = List.copyOf(confirmations);
        if (identifier.isEmpty() && confirmations.isEmpty()) {
            throw new ValidateException("SAML Subject requires an identifier or SubjectConfirmation");
        }
    }

    /**
     * Deep-copies one required non-empty XML element.
     *
     * @param value complete identifier element bytes
     * @param label diagnostic identifier label
     * @return detached validated element bytes
     */
    private static byte[] xml(final byte[] value, final String label) {
        final byte[] actual = Assert.notNull(value, label + " XML must not be null");
        if (actual.length == 0) {
            throw new ValidateException(label + " XML must not be empty");
        }
        return actual.clone();
    }

    /**
     * Seals the assertion-schema subject identifier choice.
     *
     * @author Kimi Liu
     */
    public interface Identifier {

    }

    /**
     * Wraps a typed {@code NameID} identifier.
     *
     * @param value NameID value
     * @author Kimi Liu
     */
    public record NamedIdentifier(NameID value) implements Identifier {

        /**
         * Requires a non-null NameID.
         */
        public NamedIdentifier {
            Assert.notNull(value, "SAML Subject NameID must not be null");
        }

    }

    /**
     * Preserves a derived {@code BaseID} element whose concrete extension type is not known statically.
     *
     * @param xml complete secure namespace-aware BaseID element bytes
     * @author Kimi Liu
     */
    public record BaseIdentifier(byte[] xml) implements Identifier {

        /**
         * Takes ownership through non-empty defensive copying.
         */
        public BaseIdentifier {
            xml = Subject.xml(xml, "SAML Subject BaseID");
        }

        /**
         * Returns a defensive copy of BaseID XML.
         *
         * @return complete BaseID element owned by the caller
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

    /**
     * Preserves one complete {@code EncryptedID} element for explicit decryption.
     *
     * @param xml complete secure namespace-aware EncryptedID element bytes
     * @author Kimi Liu
     */
    public record EncryptedIdentifier(byte[] xml) implements Identifier {

        /**
         * Takes ownership through non-empty defensive copying.
         */
        public EncryptedIdentifier {
            xml = Subject.xml(xml, "SAML Subject EncryptedID");
        }

        /**
         * Returns a defensive copy of EncryptedID XML.
         *
         * @return complete EncryptedID element owned by the caller
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

}
