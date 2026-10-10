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
package org.miaixz.bus.metrics.guard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.MetricRegistration;

/**
 * Provider-local registry that atomically validates metric family identity and reference counts registrations.
 *
 * @author Kimi Liu
 */
public final class MetricFamilyRegistry {

    /**
     * Monitor guarding the registry's compound updates.
     */
    private final Object monitor = new Object();
    /**
     * Live families indexed by their backend-independent identity.
     */
    private final Map<MetricFamilyKey.Logical, Entry> logicalFamilies = new HashMap<>();
    /**
     * Export identities and the logical family that owns each one.
     */
    private final Map<MetricFamilyKey.Export, MetricFamilyKey.Logical> exportFamilies = new HashMap<>();
    /**
     * Final backend export names and the logical family owning each name.
     */
    private final Map<String, MetricFamilyKey.Logical> exportNameOwners = new HashMap<>();
    /**
     * Backend-specific final metric-name function.
     */
    private final Function<MetricDescriptor, String> exportName;
    /**
     * Backend-specific attribute-name normalizer.
     */
    private final UnaryOperator<String> attributeNormalizer;

    /**
     * Creates a registry whose export identity is identical to its logical names.
     */
    public MetricFamilyRegistry() {
        this((Function<MetricDescriptor, String>) MetricDescriptor::name, UnaryOperator.identity());
    }

    /**
     * Creates a registry using backend name normalization rules.
     *
     * @param nameNormalizer      metric-name normalizer
     * @param attributeNormalizer attribute-name normalizer
     */
    public MetricFamilyRegistry(UnaryOperator<String> nameNormalizer, UnaryOperator<String> attributeNormalizer) {
        Objects.requireNonNull(nameNormalizer, "Metric name normalizer must not be null");
        this.exportName = descriptor -> nameNormalizer.apply(descriptor.name());
        this.attributeNormalizer = Objects.requireNonNull(attributeNormalizer, "Attribute normalizer must not be null");
    }

    /**
     * Creates a registry using a descriptor-aware final export-name function.
     *
     * @param exportName          final backend metric-name function
     * @param attributeNormalizer backend attribute-name normalizer
     */
    public MetricFamilyRegistry(Function<MetricDescriptor, String> exportName,
            UnaryOperator<String> attributeNormalizer) {
        this.exportName = Objects.requireNonNull(exportName, "Metric export-name function must not be null");
        this.attributeNormalizer = Objects.requireNonNull(attributeNormalizer, "Attribute normalizer must not be null");
    }

    /**
     * Creates a consistent family-conflict exception.
     *
     * @param field     conflicting identity field
     * @param requested requested descriptor
     * @param existing  registered descriptor
     * @return conflict exception
     */
    private static IllegalArgumentException conflict(
            String field,
            MetricDescriptor requested,
            MetricDescriptor existing) {
        return new IllegalArgumentException("Metric family " + requested.scope() + "/" + requested.name()
                + " conflicts on " + field + "; existing description='" + existing.description()
                + "', requested description='" + requested.description() + "'");
    }

    /**
     * Acquires one reference to a family, validating both logical and flattened export identities.
     *
     * @param descriptor family descriptor
     * @return idempotent lease
     * @throws IllegalArgumentException if the logical or final export identity conflicts with an existing family
     */
    public Lease acquire(MetricDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "Metric descriptor must not be null");
        MetricFamilyKey key = MetricFamilyKey.of(descriptor, exportName, attributeNormalizer);
        synchronized (monitor) {
            Entry existing = logicalFamilies.get(key.logical());
            if (existing != null) {
                if (!existing.descriptor.description().equals(descriptor.description())) {
                    throw conflict("description", descriptor, existing.descriptor);
                }
                if (!existing.key.export().equals(key.export())) {
                    throw conflict("export identity", descriptor, existing.descriptor);
                }
                existing.references++;
                return new Lease(descriptor, key, false, () -> release(key.logical()));
            }

            MetricFamilyKey.Logical exportOwner = exportFamilies.get(key.export());
            if (exportOwner != null && !exportOwner.equals(key.logical())) {
                throw new IllegalArgumentException("Metric family export collision for " + descriptor.name()
                        + ": normalized identity is already owned by " + exportOwner.scope() + "/"
                        + exportOwner.name());
            }

            MetricFamilyKey.Logical nameOwner = exportNameOwners.get(key.export().name());
            if (nameOwner != null && !nameOwner.equals(key.logical())) {
                throw new IllegalArgumentException("Metric family export-name collision for " + descriptor.name()
                        + ": final name '" + key.export().name() + "' is already owned by " + nameOwner.scope() + "/"
                        + nameOwner.name());
            }

            logicalFamilies.put(key.logical(), new Entry(descriptor, key));
            exportFamilies.put(key.export(), key.logical());
            exportNameOwners.put(key.export().name(), key.logical());
            return new Lease(descriptor, key, true, () -> release(key.logical()));
        }
    }

    /**
     * Returns the number of distinct live families.
     *
     * @return family count
     */
    public int size() {
        synchronized (monitor) {
            return logicalFamilies.size();
        }
    }

    /**
     * Releases one family reference and removes the entry when it reaches zero.
     *
     * @param logical logical family identity
     */
    private void release(MetricFamilyKey.Logical logical) {
        synchronized (monitor) {
            Entry entry = logicalFamilies.get(logical);
            if (entry == null) {
                return;
            }
            entry.references--;
            if (entry.references == 0) {
                logicalFamilies.remove(logical);
                exportFamilies.remove(entry.key.export(), logical);
                exportNameOwners.remove(entry.key.export().name(), logical);
            }
        }
    }

    /**
     * Mutable reference-counted registry entry guarded by the enclosing registry monitor.
     */
    private static final class Entry {

        /**
         * Canonical descriptor established by the first registration.
         */
        private final MetricDescriptor descriptor;
        /**
         * Logical and export identity pair.
         */
        private final MetricFamilyKey key;
        /**
         * Number of live leases for the family.
         */
        private int references = 1;

        /**
         * Creates a first-reference entry.
         *
         * @param descriptor family descriptor
         * @param key        family identities
         */
        private Entry(MetricDescriptor descriptor, MetricFamilyKey key) {
            this.descriptor = descriptor;
            this.key = key;
        }
    }

    /**
     * One reference-counted family acquisition.
     */
    public static final class Lease implements MetricRegistration {

        /**
         * Descriptor acquired by this lease.
         */
        private final MetricDescriptor descriptor;
        /**
         * Family identities acquired by this lease.
         */
        private final MetricFamilyKey key;
        /**
         * Whether the acquisition created a new live family.
         */
        private final boolean firstRegistration;
        /**
         * Registry release action.
         */
        private final Runnable releaser;
        /**
         * Idempotent close state.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a registry lease.
         *
         * @param descriptor        acquired descriptor
         * @param key               acquired identities
         * @param firstRegistration whether this acquisition created the entry
         * @param releaser          registry release action
         */
        private Lease(MetricDescriptor descriptor, MetricFamilyKey key, boolean firstRegistration, Runnable releaser) {
            this.descriptor = descriptor;
            this.key = key;
            this.firstRegistration = firstRegistration;
            this.releaser = releaser;
        }

        /**
         * Returns the acquired descriptor.
         *
         * @return metric descriptor
         */
        public MetricDescriptor descriptor() {
            return descriptor;
        }

        /**
         * Returns the family key pair.
         *
         * @return family keys
         */
        public MetricFamilyKey key() {
            return key;
        }

        /**
         * Returns whether this lease created the registry entry.
         *
         * @return {@code true} for the first live registration
         */
        public boolean firstRegistration() {
            return firstRegistration;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                releaser.run();
            }
        }
    }

}
