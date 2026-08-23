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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML assertion {@code AttributeStatementType} and preserves its repeated attribute CHOICE order.
 *
 * @param attributes one or more plain or encrypted attributes
 * @author Kimi Liu
 */
public record AttributeStatement(List<AttributeContent> attributes) {

    /**
     * Requires and freezes at least one attribute choice entry.
     *
     * @throws IllegalArgumentException if the list or an item is {@code null}
     * @throws ValidateException        if the list is empty
     */
    public AttributeStatement {
        Assert.notNull(attributes, "SAML AttributeStatement attribute list must not be null");
        if (attributes.isEmpty()) {
            throw new ValidateException("SAML AttributeStatement requires at least one attribute");
        }
        for (AttributeContent attribute : attributes) {
            Assert.notNull(attribute, "SAML AttributeStatement attribute must not be null");
        }
        attributes = List.copyOf(attributes);
    }

    /**
     * Seals the plain/encrypted attribute choice.
     *
     * @author Kimi Liu
     */
    public interface AttributeContent {

    }

    /**
     * Wraps one typed SAML Attribute.
     *
     * @param attribute typed attribute
     * @author Kimi Liu
     */
    public record PlainAttribute(Attribute attribute) implements AttributeContent {

        /**
         * Requires a non-null typed attribute.
         */
        public PlainAttribute {
            Assert.notNull(attribute, "SAML Attribute must not be null");
        }

    }

    /**
     * Preserves one complete EncryptedAttribute element.
     *
     * @param xml secure namespace-aware EncryptedAttribute element bytes
     * @author Kimi Liu
     */
    public record EncryptedAttribute(byte[] xml) implements AttributeContent {

        /**
         * Takes ownership through non-empty defensive copying.
         */
        public EncryptedAttribute {
            final byte[] value = Assert.notNull(xml, "SAML EncryptedAttribute XML must not be null");
            if (value.length == 0) {
                throw new ValidateException("SAML EncryptedAttribute XML must not be empty");
            }
            xml = value.clone();
        }

        /**
         * Returns a defensive copy of EncryptedAttribute XML.
         *
         * @return complete EncryptedAttribute element owned by the caller
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

}
