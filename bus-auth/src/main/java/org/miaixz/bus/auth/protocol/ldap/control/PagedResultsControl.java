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
package org.miaixz.bus.auth.protocol.ldap.control;

import org.miaixz.bus.auth.protocol.ldap.codec.BerReader;
import org.miaixz.bus.auth.protocol.ldap.codec.BerWriter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Encodes and decodes RFC 2696 Simple Paged Results request and response controls with the shared bounded BER codec.
 * Page size and opaque cookie length are validated before retention, and cookie bytes are copied on every boundary.
 *
 * @author Kimi Liu
 */
public final class PagedResultsControl {

    /**
     * Registered Simple Paged Results control object identifier.
     */
    public static final String OID = "1.2.840.113556.1.4.319";

    /**
     * Maximum encoded control value.
     */
    private static final int MAXIMUM_ENCODED_BYTES = 64 * (int) Normal.KIBI;

    /**
     * Maximum cookie bytes.
     */
    private static final int MAXIMUM_COOKIE_BYTES = Normal._8192;

    /**
     * Prevents instantiation of the control utility.
     */
    private PagedResultsControl() {
        // No initialization required.
    }

    /**
     * Creates one critical request control.
     *
     * @param size   non-negative requested page size
     * @param cookie copied prior page cookie
     * @return encoded request control
     */
    public static LdapControl request(final int size, final byte[] cookie) {
        return control(new Value(size, cookie), true);
    }

    /**
     * Creates one non-critical response control.
     *
     * @param estimatedSize non-negative estimated result size
     * @param cookie        copied next-page cookie
     * @return encoded response control
     */
    public static LdapControl response(final int estimatedSize, final byte[] cookie) {
        return control(new Value(estimatedSize, cookie), false);
    }

    /**
     * Decodes and validates one paged-results control.
     *
     * @param control     source control
     * @param maximumSize configured request or estimate ceiling
     * @return decoded value
     */
    public static Value decode(final LdapControl control, final int maximumSize) {
        final LdapControl source = Assert
                .notNull(control, () -> new ValidateException("LDAP paged-results control must not be null"));
        Assert.isTrue(
                maximumSize >= Normal._0,
                () -> new ValidateException("LDAP paged-results maximum size must not be negative"));
        if (!OID.equals(source.oid()) || source.value() == null) {
            throw new ProtocolException(ErrorCode._100300);
        }
        final BerReader reader = new BerReader(source.value(), MAXIMUM_ENCODED_BYTES, 4);
        final BerReader sequence = reader.readElement(BerReader.SEQUENCE);
        final int size = sequence.readInteger();
        final byte[] cookie = sequence.readOctets();
        sequence.requireEnd();
        reader.requireEnd();
        if (size < Normal._0 || size > maximumSize || cookie.length > MAXIMUM_COOKIE_BYTES) {
            throw new ProtocolException(ErrorCode._100300);
        }
        return new Value(size, cookie);
    }

    /**
     * Encodes one value into a control.
     *
     * @param value    page value
     * @param critical criticality flag
     * @return encoded control
     */
    private static LdapControl control(final Value value, final boolean critical) {
        final BerWriter writer = new BerWriter(MAXIMUM_ENCODED_BYTES, 4);
        writer.writeConstructed(
                BerReader.SEQUENCE,
                sequence -> sequence.writeInteger(value.size()).writeOctets(value.cookie()));
        return new LdapControl(OID, critical, writer.toByteArray());
    }

    /**
     * Immutable decoded paged-results value.
     *
     * @param size   requested page size or estimated result size
     * @param cookie copied opaque page cookie
     * @author Kimi Liu
     */
    public record Value(int size, byte[] cookie) {

        /**
         * Validates and snapshots one value.
         *
         * @param size   page size or estimate
         * @param cookie page cookie
         */
        public Value {
            Assert.isTrue(
                    size >= Normal._0,
                    () -> new ValidateException("LDAP paged-results size must not be negative"));
            cookie = Assert.notNull(cookie, () -> new ValidateException("LDAP paged-results cookie must not be null"))
                    .clone();
            Assert.isTrue(
                    cookie.length <= MAXIMUM_COOKIE_BYTES,
                    () -> new ValidateException("LDAP paged-results cookie exceeds the maximum length"));
        }

        /**
         * Returns an independent cookie copy.
         *
         * @return copied cookie
         */
        @Override
        public byte[] cookie() {
            return cookie.clone();
        }
    }

}
