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
import org.miaixz.bus.fabric.protocol.stomp.StompMessage;

/**
 * STOMP subscription topic descriptor.
 *
 * @param id          subscription id
 * @param destination destination
 * @author Kimi Liu
 * @since Java 21+
 */
public record StompTopic(String id, String destination) {

    /**
     * STOMP multi-level destination wildcard suffix.
     */
    private static final String MULTI_LEVEL_WILDCARD = Symbol.SLASH + Symbol.STAR + Symbol.STAR;

    /**
     * STOMP single-level destination wildcard suffix.
     */
    private static final String SINGLE_LEVEL_WILDCARD = Symbol.SLASH + Symbol.STAR;

    /**
     * Creates a validated topic.
     *
     * @param id          id
     * @param destination destination
     */
    public StompTopic {
        destination = validate(destination, "STOMP destination");
        id = StringKit.isBlank(id) ? destination : validate(id, "STOMP subscription id");
    }

    /**
     * Creates a topic.
     *
     * @param id          id
     * @param destination destination
     * @return topic
     */
    public static StompTopic of(final String id, final String destination) {
        return new StompTopic(id, destination);
    }

    /**
     * Returns the subscription id.
     *
     * @return id
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Returns the destination.
     *
     * @return destination
     */
    @Override
    public String destination() {
        return destination;
    }

    /**
     * Returns whether this topic matches a message destination.
     *
     * @param message message
     * @return true when matched
     */
    public boolean matches(final StompMessage message) {
        Assert.notNull(message, () -> new ValidateException("STOMP message must not be null"));
        final String value = message.destination();
        if (destination.equals(value)) {
            return true;
        }
        if (destination.endsWith(MULTI_LEVEL_WILDCARD)) {
            return value.startsWith(destination.substring(Normal._0, destination.length() - Normal._2));
        }
        if (destination.endsWith(SINGLE_LEVEL_WILDCARD)) {
            final String prefix = destination.substring(Normal._0, destination.length() - Normal._1);
            return value.startsWith(prefix) && value.indexOf(Symbol.C_SLASH, prefix.length()) < Normal._0;
        }
        return false;
    }

    /**
     * Validates a single-line token.
     *
     * @param value value
     * @param name  field name
     * @return value
     */
    private static String validate(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

}
