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
package org.miaixz.bus.auth.shared.pkce;

import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;

/**
 * Preserves an exact PKCE code_challenge_method registration or extension value.
 *
 * @author Kimi Liu
 */
public final class PkceMethod {

    /**
     * SHA-256 followed by unpadded Base64URL derivation method.
     */
    public static final PkceMethod S256 = new PkceMethod("S256");
    /**
     * Plain verifier compatibility method, disabled unless explicitly allowed.
     */
    public static final PkceMethod PLAIN = new PkceMethod("plain");

    /**
     * Exact case-sensitive wire value.
     */
    private final String value;

    /**
     * Creates one method value.
     *
     * @param value exact registration or extension value
     */
    private PkceMethod(final String value) {
        this.value = Assert.notBlank(value, "PKCE method must not be blank");
    }

    /**
     * Returns a canonical registered value or preserves an unknown extension.
     *
     * @param value exact case-sensitive wire value
     * @return immutable method value
     */
    public static PkceMethod of(final String value) {
        Assert.notBlank(value, "PKCE method must not be blank");
        return switch (value) {
            case "S256" -> S256;
            case "plain" -> PLAIN;
            default -> new PkceMethod(value);
        };
    }

    /**
     * Returns the exact case-sensitive wire value.
     *
     * @return method registration or extension
     */
    public String value() {
        return value;
    }

    /**
     * Compares this exact method value with another object.
     *
     * @param other candidate object
     * @return {@code true} when both objects preserve the same case-sensitive method value
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof PkceMethod method && value.equals(method.value);
    }

    /**
     * Returns the stable hash of the exact method value.
     *
     * @return stable exact-value hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Returns the non-sensitive wire value for diagnostics.
     *
     * @return exact method value
     */
    @Override
    public String toString() {
        return value;
    }

}
