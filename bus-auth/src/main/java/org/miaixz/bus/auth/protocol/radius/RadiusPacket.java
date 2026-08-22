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
package org.miaixz.bus.auth.protocol.radius;

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Enumers;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Defines the common contract of the six typed RADIUS packets implemented by the packet codecs.
 * <p>
 * Historic RADIUS and RFC 9765 use mutually exclusive header meanings. Distinct {@link Header} implementations keep a
 * packet from exposing an Identifier or Authenticator when its wire header actually carries a RADIUS/1.1 Token.
 * </p>
 *
 * @author Kimi Liu
 */
public interface RadiusPacket {

    /**
     * Returns the Code fixed by the concrete packet type.
     *
     * @return registered packet Code
     */
    RadiusCode code();

    /**
     * Returns the version-specific packet header.
     *
     * @return immutable historic or RADIUS/1.1 header
     */
    Header header();

    /**
     * Returns raw Attributes in wire order.
     *
     * @return immutable ordered Attribute list
     */
    List<RadiusAttribute> attributes();

    /**
     * Identifies the header semantics selected by the trusted transport boundary.
     *
     * @author Kimi Liu
     */
    enum Version implements Enumers<Version> {

        /**
         * Historic RADIUS header semantics used by UDP, TCP, and historic RADIUS/TLS.
         */
        RADIUS_1_0(10),

        /**
         * RFC 9765 Token header semantics negotiated with ALPN {@code radius/1.1}.
         */
        RADIUS_1_1(11);

        /**
         * Stable persistence code independent of declaration order.
         */
        private final int code;

        /**
         * Creates a RADIUS wire version.
         *
         * @param code stable persistence code
         */
        Version(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persistence code.
         *
         * @return version persistence code
         */
        @Override
        public int code() {
            return code;
        }

    }

    /**
     * Marks the mutually exclusive historic and RADIUS/1.1 header representations.
     *
     * @author Kimi Liu
     */
    interface Header {

        /**
         * Returns the wire semantics represented by this header.
         *
         * @return exact RADIUS version
         */
        Version version();

    }

    /**
     * Carries the RFC 2865/2866 Identifier and 16-octet Authenticator.
     *
     * @param identifier    unsigned one-octet request correlation identifier
     * @param authenticator exact 16-octet Request or Response Authenticator
     * @author Kimi Liu
     */
    record LegacyHeader(int identifier, byte[] authenticator) implements Header {

        /**
         * Validates and detaches a historic RADIUS header.
         *
         * @param identifier    unsigned Identifier from 0 through 255
         * @param authenticator exact 16 Authenticator octets
         * @throws IllegalArgumentException if a component violates its wire range
         */
        public LegacyHeader {
            Assert.isTrue(identifier >= 0 && identifier <= 255, "RADIUS Identifier must be between 0 and 255");
            Assert.notNull(authenticator, "RADIUS Authenticator must not be null");
            Assert.isTrue(authenticator.length == 16, "RADIUS Authenticator must contain exactly 16 octets");
            authenticator = authenticator.clone();
        }

        /**
         * Returns the historic RADIUS header version.
         *
         * @return RADIUS 1.0 header semantics
         */
        @Override
        public Version version() {
            return Version.RADIUS_1_0;
        }

        /**
         * Returns a detached Authenticator copy.
         *
         * @return copied 16 Authenticator octets
         */
        @Override
        public byte[] authenticator() {
            return authenticator.clone();
        }

        /**
         * Compares Identifier and Authenticator contents.
         *
         * @param other candidate object
         * @return {@code true} when both historic header values match
         */
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof LegacyHeader header && identifier == header.identifier
                    && Arrays.equals(authenticator, header.authenticator);
        }

        /**
         * Computes a content-based historic header hash.
         *
         * @return header content hash
         */
        @Override
        public int hashCode() {
            return 31 * Integer.hashCode(identifier) + Arrays.hashCode(authenticator);
        }

        /**
         * Returns structural diagnostics without rendering Authenticator octets.
         *
         * @return safe historic header text
         */
        @Override
        public String toString() {
            return "LegacyHeader[identifier=" + identifier + ", authenticatorLength=" + authenticator.length
                    + Symbol.BRACKET_RIGHT;
        }

    }

    /**
     * Carries the RFC 9765 four-octet opaque request/response correlation Token.
     *
     * @param token exact four-octet opaque Token
     * @author Kimi Liu
     */
    record Radius11Header(byte[] token) implements Header {

        /**
         * Validates and detaches a RADIUS/1.1 Token.
         *
         * @param token exact four Token octets
         * @throws IllegalArgumentException if the Token is {@code null} or not four octets
         */
        public Radius11Header {
            Assert.notNull(token, "RADIUS/1.1 Token must not be null");
            Assert.isTrue(token.length == 4, "RADIUS/1.1 Token must contain exactly four octets");
            token = token.clone();
        }

        /**
         * Returns the RADIUS/1.1 header version.
         *
         * @return RADIUS 1.1 header semantics
         */
        @Override
        public Version version() {
            return Version.RADIUS_1_1;
        }

        /**
         * Returns a detached opaque Token copy.
         *
         * @return copied four Token octets
         */
        @Override
        public byte[] token() {
            return token.clone();
        }

        /**
         * Compares opaque Token contents.
         *
         * @param other candidate object
         * @return {@code true} when both four-octet Tokens match
         */
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof Radius11Header header && Arrays.equals(token, header.token);
        }

        /**
         * Computes a content-based opaque Token hash.
         *
         * @return Token content hash
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(token);
        }

        /**
         * Returns structural diagnostics without rendering Token octets.
         *
         * @return safe RADIUS/1.1 header text
         */
        @Override
        public String toString() {
            return "Radius11Header[tokenLength=" + token.length + Symbol.BRACKET_RIGHT;
        }

    }

}
