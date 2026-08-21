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

import org.miaixz.bus.core.basic.entity.Audit;
import org.miaixz.bus.core.basic.entity.Entity;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Enumers;

/**
 * Defines the single provider-neutral loading model for Library, Provider, and Source registrations.
 * <p>
 * External projects create complete records from databases, files, or remote services. The framework validates and
 * compiles one complete snapshot; this container performs no loading, persistence, protocol option materialization, or
 * Registry access. Each record detaches the framework-owned fields of the complete managed entity so project mutation
 * cannot change a published runtime container.
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
     * Represents one type-safe registration entry accepted by the framework boundary.
     * <p>
     * Projects may load subclasses of the three framework entities, but an entry always projects them to their exact
     * framework base type. This prevents a project subtype from being advertised after its project-owned fields have
     * intentionally been discarded.
     * </p>
     *
     * @author Kimi Liu
     */
    public sealed interface Entry permits LibraryEntry, ProviderEntry, SourceEntry {

        /**
         * Returns the exact resource category.
         *
         * @return registration kind
         */
        Kind kind();

        /**
         * Returns whether this entry participates in the compiled runtime view.
         *
         * @return {@code true} when enabled
         */
        boolean enabled();

        /**
         * Returns a detached framework entity.
         *
         * @return mutable copy that cannot change this entry
         */
        Entity resource();

    }

    /**
     * Carries one detached Library registration.
     *
     * @param enabled  whether the Library participates in the compiled view
     * @param resource project-supplied Library or subclass
     */
    public record LibraryEntry(boolean enabled, Library resource) implements Entry {

        /** Validates and detaches one Library registration. */
        public LibraryEntry {
            resource = copy(Assert.notNull(resource, "Library registration must not be null"));
        }

        @Override
        public Kind kind() {
            return Kind.LIBRARY;
        }

        @Override
        public Library resource() {
            return copy(resource);
        }

    }

    /**
     * Carries one detached Provider registration.
     *
     * @param enabled  whether the Provider participates in the compiled view
     * @param resource project-supplied Provider or subclass
     */
    public record ProviderEntry(boolean enabled, Provider resource) implements Entry {

        /** Validates and detaches one Provider registration. */
        public ProviderEntry {
            resource = copy(Assert.notNull(resource, "Provider registration must not be null"));
        }

        @Override
        public Kind kind() {
            return Kind.PROVIDER;
        }

        @Override
        public Provider resource() {
            return copy(resource);
        }

    }

    /**
     * Carries one detached Source registration.
     *
     * @param enabled  whether the Source participates in the compiled view
     * @param resource project-supplied Source or subclass
     */
    public record SourceEntry(boolean enabled, Source resource) implements Entry {

        /** Validates and detaches one Source registration. */
        public SourceEntry {
            resource = copy(Assert.notNull(resource, "Source registration must not be null"));
        }

        @Override
        public Kind kind() {
            return Kind.SOURCE;
        }

        @Override
        public Source resource() {
            return copy(resource);
        }

    }

    /**
     * Copies common audit identity and timestamps into a detached entity.
     *
     * @param source source audit entity
     * @param target detached target audit entity
     */
    private static void copyAudit(final Audit source, final Audit target) {
        target.setId(source.getId());
        target.setCreator(source.getCreator());
        target.setCreated(source.getCreated());
        target.setModifier(source.getModifier());
        target.setModified(source.getModified());
    }

    /**
     * Creates a detached Library copy.
     *
     * @param source registered Library
     * @return detached Library
     */
    private static Library copy(final Library source) {
        final Library target = new Library();
        copyAudit(source, target);
        target.setNamespace_id(source.getNamespace_id());
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setIcon(source.getIcon());
        target.setUrl(source.getUrl());
        target.setTarget(source.getTarget());
        target.setSort(source.getSort());
        target.setCategory(source.getCategory());
        target.setMetadata(source.getMetadata());
        target.setPublisher(source.getPublisher());
        target.setDescription(source.getDescription());
        return target;
    }

    /**
     * Creates a detached Provider copy.
     *
     * @param source registered Provider
     * @return detached Provider
     */
    private static Provider copy(final Provider source) {
        final Provider target = new Provider();
        copyAudit(source, target);
        target.setLibrary_id(source.getLibrary_id());
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setIcon(source.getIcon());
        target.setSort(source.getSort());
        target.setMetadata(source.getMetadata());
        target.setDescription(source.getDescription());
        return target;
    }

    /**
     * Creates a detached Source copy including detached typed options.
     *
     * @param source registered Source
     * @return detached Source
     */
    private static Source copy(final Source source) {
        final Source target = new Source();
        copyAudit(source, target);
        target.setProvider_id(source.getProvider_id());
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setType(source.getType());
        target.setIcon(source.getIcon());
        target.setSort(source.getSort());
        target.setProtocol(source.getProtocol());
        final Options<?> options = source.getOptions();
        target.setOptions(
                options == null ? null
                        : Assert.notNull(options.snapshot(), "Source options snapshot must not be null"));
        target.setMetadata(source.getMetadata());
        target.setDescription(source.getDescription());
        return target;
    }

}
