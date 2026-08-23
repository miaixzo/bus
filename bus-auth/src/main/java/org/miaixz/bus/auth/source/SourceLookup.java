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
package org.miaixz.bus.auth.source;

import java.util.*;

import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.logger.Logger;

/**
 * Freezes the single Source lookup shared by validation, compilation, and runtime discovery.
 * <p>
 * This lookup owns driver declaration order, exact scheme and descriptor indexes, and persisted Source reverse routing.
 * It does not validate Source registrations, compile workers, load project data, or execute authentication
 * capabilities.
 * </p>
 *
 * @author Kimi Liu
 */
public class SourceLookup {

    /**
     * Drivers in deterministic runtime assembly order.
     */
    private final List<SourceDriver<?>> drivers;

    /**
     * Exact Source scheme identifier index over the immutable driver list.
     */
    private final Map<String, SourceDriver<?>> driversByScheme;

    /**
     * Exact management selections in deterministic module and descriptor order.
     */
    private final List<SourceDescriptor> descriptors;

    /**
     * Exact descriptor identifier index over the immutable selection list.
     */
    private final Map<String, SourceDescriptor> descriptorsById;

    /**
     * Creates the single immutable driver and descriptor lookup from complete Source modules.
     *
     * @param modules modules in deterministic assembly order
     * @throws IllegalArgumentException if a module or one of its members is invalid
     * @throws AlreadyExistsException   if driver or descriptor identifiers collide
     */
    public SourceLookup(final Collection<? extends SourceModule> modules) {
        Assert.notNull(modules, "Source module collection must not be null");
        final List<SourceDriver<?>> moduleDrivers = new ArrayList<>();
        final List<SourceDescriptor> moduleDescriptors = new ArrayList<>();
        for (SourceModule candidate : modules) {
            final SourceModule module = Assert.notNull(candidate, "Source module must not be null");
            final List<SourceDriver<?>> drivers = Assert
                    .notNull(module.drivers(), "Source module driver list must not be null");
            final List<SourceDescriptor> descriptors = Assert
                    .notNull(module.descriptors(), "Source module descriptor list must not be null");
            if (drivers.isEmpty() || descriptors.isEmpty()) {
                throw new IllegalArgumentException("Source module drivers and descriptors must not be empty");
            }
            final Map<String, SourceDriver<?>> owned = new LinkedHashMap<>();
            for (SourceDriver<?> driver : drivers) {
                final SourceDriver<?> checked = Assert.notNull(driver, "Source module driver must not be null");
                final String type = Assert.notBlank(
                        Assert.notNull(checked.scheme(), "Source module driver scheme must not be null").id(),
                        "Source module driver type must not be blank");
                if (owned.putIfAbsent(type, checked) != null) {
                    throw new AlreadyExistsException("Duplicate Source module driver type: " + type);
                }
                moduleDrivers.add(checked);
            }
            for (SourceDescriptor descriptor : descriptors) {
                final SourceDescriptor checked = Assert
                        .notNull(descriptor, "Source module descriptor must not be null");
                final SourceDriver<?> owner = owned
                        .get(Assert.notBlank(checked.type(), "Source descriptor type must not be blank"));
                if (owner == null) {
                    throw new IllegalArgumentException(
                            "Source descriptor type is not owned by its module: " + checked.type());
                }
                if (!owner.supports(
                        Assert.notNull(checked.protocol(), "Source descriptor protocol must not be null").name())) {
                    throw new IllegalArgumentException(
                            "Source descriptor protocol is not supported by its driver: " + checked.id());
                }
                moduleDescriptors.add(checked);
            }
        }
        final State state = state(moduleDrivers, moduleDescriptors);
        this.drivers = state.drivers();
        this.driversByScheme = state.driversByScheme();
        this.descriptors = state.descriptors();
        this.descriptorsById = state.descriptorsById();
        Logger.info(
                false,
                "Auth",
                "Source lookup initialized: modules={}, drivers={}, descriptors={}",
                modules.size(),
                this.drivers.size(),
                this.descriptors.size());
    }

    /**
     * Validates and freezes the flattened lookup indexes exactly once.
     *
     * @param drivers     flattened drivers
     * @param descriptors flattened descriptors
     * @return detached immutable lookup state
     */
    private static State state(
            final List<? extends SourceDriver<?>> drivers,
            final List<? extends SourceDescriptor> descriptors) {
        Assert.notNull(drivers, "Source driver list must not be null");
        Assert.notNull(descriptors, "Source descriptor list must not be null");
        final List<SourceDriver<?>> ordered = new ArrayList<>(drivers.size());
        final Map<String, SourceDriver<?>> indexed = new LinkedHashMap<>(drivers.size());
        for (SourceDriver<?> driver : drivers) {
            final SourceDriver<?> checked = Assert.notNull(driver, "Source driver must not be null");
            final Scheme<?> scheme = Assert.notNull(checked.scheme(), "Source driver scheme must not be null");
            final String id = Assert.notBlank(scheme.id(), "Source driver scheme id must not be blank");
            if (indexed.putIfAbsent(id, checked) != null) {
                throw new AlreadyExistsException("Duplicate Source driver scheme: " + id);
            }
            ordered.add(checked);
        }
        final List<SourceDescriptor> orderedDescriptors = new ArrayList<>(descriptors.size());
        final Map<String, SourceDescriptor> descriptorsById = new LinkedHashMap<>(descriptors.size());
        for (SourceDescriptor candidate : descriptors) {
            final SourceDescriptor descriptor = Assert.notNull(candidate, "Source descriptor must not be null");
            final String id = Assert.notBlank(descriptor.id(), "Source descriptor id must not be blank");
            final String type = Assert.notBlank(descriptor.type(), "Source descriptor type must not be blank");
            final SourceDriver<?> driver = indexed.get(type);
            if (driver == null) {
                throw new NotFoundException("Source descriptor driver not found: " + type);
            }
            if (!driver.supports(
                    Assert.notNull(descriptor.protocol(), "Source descriptor protocol must not be null").name())) {
                throw new IllegalArgumentException("Source descriptor protocol is not supported: " + id);
            }
            Assert.notNull(descriptor.kind(), "Source descriptor kind must not be null");
            Assert.notNull(descriptor.metadata(), "Source descriptor metadata must not be null");
            Assert.notNull(descriptor.manifest(), "Source descriptor manifest must not be null");
            Assert.notNull(descriptor.conformance(), "Source descriptor conformance must not be null");
            Assert.notNull(descriptor.form(), "Source descriptor form must not be null");
            if (descriptorsById.putIfAbsent(id, descriptor) != null) {
                throw new AlreadyExistsException("Duplicate Source descriptor id: " + id);
            }
            orderedDescriptors.add(descriptor);
        }
        return new State(List.copyOf(ordered), Map.copyOf(indexed), List.copyOf(orderedDescriptors),
                Map.copyOf(descriptorsById));
    }

    /**
     * Returns drivers in deterministic assembly order.
     *
     * @return immutable driver list
     */
    public List<SourceDriver<?>> drivers() {
        return drivers;
    }

    /**
     * Returns exact Source selections in deterministic assembly order.
     *
     * @return immutable descriptor list
     */
    public List<SourceDescriptor> descriptors() {
        return descriptors;
    }

    /**
     * Finds the driver registered for an exact scheme identifier.
     *
     * @param scheme exact scheme identifier
     * @return optional matching driver
     */
    public Optional<SourceDriver<?>> driver(final String scheme) {
        Assert.notBlank(scheme, "Source driver scheme must not be blank");
        return Optional.ofNullable(driversByScheme.get(scheme));
    }

    /**
     * Finds one exact selection by its stable descriptor identifier.
     *
     * @param id exact descriptor identifier
     * @return optional matching descriptor
     */
    public Optional<SourceDescriptor> descriptor(final String id) {
        Assert.notBlank(id, "Source descriptor id must not be blank");
        return Optional.ofNullable(descriptorsById.get(id));
    }

    /**
     * Resolves the unique descriptor matching a persisted Source routing identity.
     *
     * @param source persisted Source
     * @return matching descriptor or empty when no assembled selection matches
     * @throws AlreadyExistsException if more than one descriptor matches the Source
     */
    public Optional<SourceDescriptor> descriptor(final Source source) {
        final Source checked = Assert.notNull(source, "Source descriptor lookup value must not be null");
        SourceDescriptor matched = null;
        for (SourceDescriptor descriptor : descriptors) {
            if (descriptor.matches(checked)) {
                if (matched != null) {
                    throw new AlreadyExistsException("Ambiguous Source descriptor route: " + checked.getId());
                }
                matched = descriptor;
            }
        }
        return Optional.ofNullable(matched);
    }

    /**
     * Requires the driver registered for an exact scheme identifier.
     *
     * @param scheme exact scheme identifier
     * @return matching driver
     * @throws NotFoundException if no matching driver was assembled
     */
    public SourceDriver<?> requireDriver(final String scheme) {
        final SourceDriver<?> driver = driver(scheme).getOrNull();
        if (driver == null) {
            throw new NotFoundException("Source driver not found: " + scheme);
        }
        return driver;
    }

    /**
     * Holds one detached immutable lookup construction result.
     *
     * @param drivers         ordered drivers
     * @param driversByScheme driver type index
     * @param descriptors     ordered descriptors
     * @param descriptorsById descriptor identifier index
     * @author Kimi Liu
     */
    private record State(List<SourceDriver<?>> drivers, Map<String, SourceDriver<?>> driversByScheme,
            List<SourceDescriptor> descriptors, Map<String, SourceDescriptor> descriptorsById) {

    }

}
