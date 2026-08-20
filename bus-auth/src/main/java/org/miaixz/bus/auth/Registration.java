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

import org.miaixz.bus.core.basic.entity.Entity;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Enumers;

/**
 * Defines the single provider-neutral loading model for Library, Provider, and Source registrations.
 * <p>
 * External projects create complete records from databases, files, or remote services. The framework validates and
 * compiles one complete snapshot; this container performs no loading, persistence, protocol option materialization, or
 * Registry access. Each record carries the complete managed entity instead of duplicating entity fields or transporting
 * persistence representations.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Registration {

    /**
     * Prevents instantiation of the registration namespace.
     */
    private Registration() {
        // No initialization required.
    }

    /**
     * Identifies the managed resource represented by a registration record.
     *
     * @author Kimi Liu
     */
    public enum Kind implements Enumers<Kind> {

        /**
         * Authentication application catalog entry.
         */
        LIBRARY(1),

        /**
         * Protocol-neutral Provider entity that groups one or more Sources under a Library.
         */
        PROVIDER(2),

        /**
         * Protocol or Vendor Source, including both client-role and server-role registrations.
         */
        SOURCE(3);

        /**
         * Stable persistence code independent of declaration order.
         */
        private final int code;

        /**
         * Creates a resource kind with its stable persistence code.
         *
         * @param code stable persistence code
         */
        Kind(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persistence code for this resource kind.
         *
         * @return stable resource kind code
         */
        @Override
        public int code() {
            return code;
        }

    }

    /**
     * Represents the monotonically increasing version of one registration record.
     * <p>
     * This value is not a Registry snapshot revision and is unrelated to any protocol version.
     * </p>
     *
     * @param value non-negative record generation
     * @author Kimi Liu
     */
    public record Generation(long value) {

        /**
         * Creates a record generation.
         *
         * @param value non-negative record generation
         * @throws IllegalArgumentException if the generation is negative
         */
        public Generation {
            Assert.isTrue(value >= 0, "Registration generation must not be negative");
        }

    }

    /**
     * Carries one resource registration loaded by an external project.
     * <p>
     * Resource identity, namespace, protocol, relationships, presentation data, and materialized options remain on the
     * mutable resource object that owns them. The record components are fixed, but this wrapper does not freeze or copy
     * the enclosed project-supplied entity; it adds only registration state and generation information.
     * </p>
     *
     * @param kind       managed resource kind
     * @param enabled    whether the record participates in the compiled Registry view
     * @param generation version of this individual record
     * @param resource   complete Library, Provider, or Source entity supplied by the external project
     * @param <R>        concrete managed resource type
     * @author Kimi Liu
     */
    public record Record<R extends Entity>(Kind kind, boolean enabled, Generation generation, R resource) {

        /**
         * Creates a fixed registration wrapper whose kind matches its complete resource object.
         *
         * @param kind       managed resource kind
         * @param enabled    whether the record participates in the compiled Registry view
         * @param generation version of this individual record
         * @param resource   complete managed resource entity
         * @throws IllegalArgumentException if a required value is null or the kind does not match the resource type
         */
        public Record {
            Assert.notNull(kind, "Registration kind must not be null");
            Assert.notNull(generation, "Registration generation must not be null");
            Assert.notNull(resource, "Registration resource must not be null");
            Assert.isTrue(matches(kind, resource), "Registration kind does not match resource type: {}", kind);
        }

        /**
         * Determines whether a resource kind owns the supplied concrete entity category.
         *
         * @param kind     declared resource kind
         * @param resource complete resource entity
         * @return {@code true} when the kind and entity category match
         */
        private static boolean matches(final Kind kind, final Entity resource) {
            return switch (kind) {
                case LIBRARY -> resource instanceof Library;
                case PROVIDER -> resource instanceof Provider;
                case SOURCE -> resource instanceof Source;
            };
        }

    }

}
