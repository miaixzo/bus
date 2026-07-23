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
package org.miaixz.bus.fabric.protocol.stomp.broker;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.protocol.stomp.StompMessage;

/**
 * STOMP subscription topic descriptor.
 *
 * @param id          non-blank subscription identifier, defaulted from the destination when omitted
 * @param destination exact or wildcard STOMP destination pattern
 * @author Kimi Liu
 * @since Java 21+
 */
public record StompTopic(String id, String destination) {

    /**
     * Creates a validated topic.
     *
     * @param id          optional subscription identifier; blank text selects the destination
     * @param destination non-blank, single-line destination pattern
     * @throws ValidateException if the destination or explicit identifier is invalid
     */
    public StompTopic {
        destination = validate(destination, "STOMP destination");
        id = StringKit.isBlank(id) ? destination : validate(id, "STOMP subscription id");
    }

    /**
     * Creates a topic.
     *
     * @param id          optional subscription identifier; blank text selects the destination
     * @param destination exact or wildcard destination pattern
     * @return validated immutable topic descriptor
     * @throws ValidateException if the destination or explicit identifier is invalid
     */
    public static StompTopic of(final String id, final String destination) {
        return new StompTopic(id, destination);
    }

    /**
     * Returns the subscription id.
     *
     * @return explicit identifier or the destination selected as its default
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Returns the destination.
     *
     * @return exact destination or trailing single-/multi-level wildcard pattern
     */
    @Override
    public String destination() {
        return destination;
    }

    /**
     * Returns whether this topic matches a message destination.
     *
     * @param message message whose destination is compared with this topic pattern
     * @return {@code true} for an exact match or a match accepted by a trailing {@code /*} or {@code /**} wildcard
     * @throws ValidateException if {@code message} is {@code null}
     */
    public boolean matches(final StompMessage message) {
        Assert.notNull(message, () -> new ValidateException("STOMP message must not be null"));
        final String value = message.destination();
        if (destination.equals(value)) {
            return true;
        }
        if (destination.endsWith(Builder.STOMP_TOPIC_MULTI_LEVEL_WILDCARD)) {
            return value.startsWith(destination.substring(Normal._0, destination.length() - Normal._2));
        }
        if (destination.endsWith(Builder.STOMP_TOPIC_SINGLE_LEVEL_WILDCARD)) {
            final String prefix = destination.substring(Normal._0, destination.length() - Normal._1);
            return value.startsWith(prefix) && value.indexOf(Symbol.C_SLASH, prefix.length()) < Normal._0;
        }
        return false;
    }

    /**
     * Validates a single-line token.
     *
     * @param value identifier or destination text to validate
     * @param name  logical field name included in the validation error
     * @return unchanged non-blank, single-line text
     * @throws ValidateException if the text is blank or contains a line break
     */
    private static String validate(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

}
