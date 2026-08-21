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
package org.miaixz.bus.auth;

import java.io.Serializable;
import java.time.Instant;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Enumers;

/**
 * Represents the framework's single immutable authenticated-session concept.
 * <p>
 * Session lifecycle is separate from Registry snapshots, protocol-specific session identifiers, and tokens. The record
 * contains only its stable key, lifecycle state, and validity interval; token and credential material must be retained
 * by their dedicated stores or external loaders.
 * </p>
 *
 * @param key       stable session key
 * @param state     current session lifecycle state
 * @param issuedAt  session creation instant
 * @param expiresAt exclusive session expiration instant
 * @author Kimi Liu
 */
public record Session(Key key, State state, Instant issuedAt, Instant expiresAt) implements Serializable {

    /**
     * Creates an immutable session with a strictly positive validity interval.
     *
     * @param key       stable session key
     * @param state     current lifecycle state
     * @param issuedAt  session creation instant
     * @param expiresAt exclusive expiration instant
     * @throws IllegalArgumentException if a component is {@code null} or expiration is not after issuance
     */
    public Session {
        Assert.notNull(key, "Session key must not be null");
        Assert.notNull(state, "Session state must not be null");
        Assert.notNull(issuedAt, "Session issue time must not be null");
        Assert.notNull(expiresAt, "Session expiration time must not be null");
        Assert.isTrue(expiresAt.isAfter(issuedAt), "Session expiration must be after issue time");
    }

    /**
     * Enumerates the lifecycle states of a framework authentication session.
     *
     * @author Kimi Liu
     */
    public enum State implements Enumers<State> {

        /**
         * Session is eligible for use until its expiration boundary.
         */
        ACTIVE(1),

        /**
         * Session validity interval has elapsed.
         */
        EXPIRED(2),

        /**
         * Session was explicitly ended before or at expiration.
         */
        ENDED(3),

        /**
         * Session ending is owned by the framework but the project login state has not yet confirmed termination.
         * Repeated logout operations must resume this transition until it reaches {@link #ENDED}.
         */
        ENDING(4);

        /**
         * Stable persistence code independent of declaration order.
         */
        private final int code;

        /**
         * Creates a session state with its stable persistence code.
         *
         * @param code stable persistence code
         */
        State(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persistence code for this session state.
         *
         * @return stable session state code
         */
        @Override
        public int code() {
            return code;
        }

    }

    /**
     * Wraps the stable opaque identifier of one authenticated session.
     *
     * @param value stable opaque session identifier
     * @author Kimi Liu
     */
    public record Key(String value) implements Serializable {

        /**
         * Creates a session key.
         *
         * @param value non-blank stable opaque identifier
         * @throws IllegalArgumentException if the value is blank
         */
        public Key {
            Assert.notBlank(value, "Session key value must not be blank");
        }

    }

}
