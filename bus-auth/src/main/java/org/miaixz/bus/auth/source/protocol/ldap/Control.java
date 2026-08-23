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
package org.miaixz.bus.auth.source.protocol.ldap;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents one RFC 4511 LDAP {@code Control} sequence.
 *
 * @param controlType  numeric object identifier naming the control
 * @param criticality  effective criticality, with {@code false} representing the ASN.1 default
 * @param controlValue optional opaque control-specific OCTET STRING
 * @author Kimi Liu
 */
public record Control(String controlType, boolean criticality, Optional<byte[]> controlValue) {

    /**
     * RFC 4512 numeric-OID lexical grammar used by LDAPOID.
     */
    private static final Pattern NUMERIC_OID = Pattern.compile("(?:0|[1-9][0-9]*)(?:\\.(?:0|[1-9][0-9]*))+");

    /**
     * Creates an immutable LDAP control and defensively copies its optional value.
     *
     * @param controlType  numeric control object identifier
     * @param criticality  effective criticality
     * @param controlValue optional opaque control value
     * @throws IllegalArgumentException if the identifier is malformed or an option is {@code null}
     */
    public Control {
        requireNumericOid(controlType);
        Assert.notNull(controlValue, "LDAP control value option must not be null");
        controlValue = copy(controlValue);
    }

    /**
     * Clones the value contained by an optional OCTET STRING.
     *
     * @param source source optional value
     * @return independently owned optional value
     */
    private static Optional<byte[]> copy(final Optional<byte[]> source) {
        return source.isEmpty() ? Optional.empty() : Optional.of(source.getOrThrow().clone());
    }

    /**
     * Validates the RFC 4512 numeric-OID form required by LDAPOID fields.
     *
     * @param value numeric object identifier
     * @throws IllegalArgumentException if the value is null, blank, or malformed
     */
    static void requireNumericOid(final String value) {
        Assert.notBlank(value, "LDAP numeric object identifier must not be blank");
        Assert.isTrue(NUMERIC_OID.matcher(value).matches(), "LDAP value must be a numeric object identifier");
    }

    /**
     * Returns a separately owned optional control value.
     *
     * @return empty option or a new copy of the control-value octets
     */
    @Override
    public Optional<byte[]> controlValue() {
        return copy(controlValue);
    }

    /**
     * Compares controls using OCTET STRING content rather than Java array identity.
     *
     * @param other candidate object
     * @return {@code true} when every standard control field is equal
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Control that) || criticality != that.criticality || !controlType.equals(that.controlType)
                || controlValue.isEmpty() != that.controlValue.isEmpty()) {
            return false;
        }
        return controlValue.isEmpty() || Arrays.equals(controlValue.getOrThrow(), that.controlValue.getOrThrow());
    }

    /**
     * Computes a hash from the control fields and optional OCTET STRING content.
     *
     * @return content-based hash code
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hash(controlType, criticality)
                + (controlValue.isEmpty() ? 0 : Arrays.hashCode(controlValue.getOrThrow()));
    }

    /**
     * Returns a structural description that does not expose control-value octets.
     *
     * @return control type, criticality, and value-presence description
     */
    @Override
    public String toString() {
        return "Control[controlType=" + controlType + ", criticality=" + criticality + ", controlValuePresent="
                + !controlValue.isEmpty() + Symbol.BRACKET_RIGHT;
    }

}
