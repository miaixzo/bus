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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML assertion {@code ConditionsType} and preserves its ordered condition choice.
 *
 * @param notBefore    optional inclusive lower time bound
 * @param notOnOrAfter optional exclusive upper time bound
 * @param conditions   ordered standard or extension conditions
 * @author Kimi Liu
 */
public record Conditions(Optional<Instant> notBefore, Optional<Instant> notOnOrAfter, List<Condition> conditions) {

    /**
     * Validates the optional interval and freezes ordered conditions.
     *
     * @throws IllegalArgumentException if a container or condition is {@code null}
     * @throws ValidateException        if the time interval is empty or inverted
     */
    public Conditions {
        Assert.notNull(notBefore, "SAML Conditions NotBefore container must not be null");
        Assert.notNull(notOnOrAfter, "SAML Conditions NotOnOrAfter container must not be null");
        notBefore = Optional.ofNullable(notBefore.getOrNull());
        notOnOrAfter = Optional.ofNullable(notOnOrAfter.getOrNull());
        if (notBefore.isPresent() && notOnOrAfter.isPresent()
                && !notBefore.getOrNull().isBefore(notOnOrAfter.getOrNull())) {
            throw new ValidateException("SAML Conditions NotBefore must precede NotOnOrAfter");
        }
        Assert.notNull(conditions, "SAML condition list must not be null");
        for (Condition condition : conditions) {
            Assert.notNull(condition, "SAML condition must not be null");
        }
        conditions = List.copyOf(conditions);
    }

    /**
     * Requires one non-empty absolute URI.
     *
     * @param value URI lexical value
     * @param label diagnostic condition label
     */
    private static void absolute(final String value, final String label) {
        final String actual = Assert.notBlank(value, label + " must not be blank");
        try {
            if (!new URI(actual).isAbsolute()) {
                throw new ValidateException(label + " must be absolute");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Seals the SAML assertion condition choice retained by the model.
     *
     * @author Kimi Liu
     */
    public interface Condition {

    }

    /**
     * Wraps one typed AudienceRestriction condition.
     *
     * @param restriction audience restriction applied to the assertion
     *
     * @author Kimi Liu
     */
    public record Audience(AudienceRestriction restriction) implements Condition {

        /**
         * Requires a non-null audience restriction.
         */
        public Audience {
            Assert.notNull(restriction, "SAML AudienceRestriction must not be null");
        }

    }

    /**
     * Marks the empty SAML {@code OneTimeUse} condition.
     *
     * @author Kimi Liu
     */
    public record OneTimeUse() implements Condition {

    }

    /**
     * Models SAML {@code ProxyRestrictionType}.
     *
     * @param count     optional non-negative proxy count
     * @param audiences ordered optional audience URIs
     *
     * @author Kimi Liu
     */
    public record ProxyRestriction(Optional<Integer> count, List<String> audiences) implements Condition {

        /**
         * Validates count and absolute audience URIs.
         */
        public ProxyRestriction {
            Assert.notNull(count, "SAML ProxyRestriction Count container must not be null");
            final Integer actual = count.getOrNull();
            if (actual != null && actual < 0) {
                throw new ValidateException("SAML ProxyRestriction Count must be non-negative");
            }
            count = Optional.ofNullable(actual);
            Assert.notNull(audiences, "SAML ProxyRestriction audience list must not be null");
            for (String audience : audiences) {
                absolute(audience, "SAML ProxyRestriction Audience");
            }
            audiences = List.copyOf(audiences);
        }

    }

    /**
     * Preserves a derived extension of abstract {@code ConditionAbstractType}.
     *
     * @param xml complete secure namespace-aware condition element bytes
     *
     * @author Kimi Liu
     */
    public record Extension(byte[] xml) implements Condition {

        /**
         * Takes ownership through non-empty defensive copying.
         */
        public Extension {
            final byte[] actual = Assert.notNull(xml, "SAML extension Condition XML must not be null");
            if (actual.length == 0) {
                throw new ValidateException("SAML extension Condition XML must not be empty");
            }
            xml = actual.clone();
        }

        /**
         * Returns a defensive copy of extension Condition XML.
         *
         * @return complete extension Condition element owned by the caller
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

}
