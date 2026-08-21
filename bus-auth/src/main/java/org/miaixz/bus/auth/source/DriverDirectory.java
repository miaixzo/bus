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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Scheme;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.AlreadyExistsException;
import org.miaixz.bus.core.lang.exception.NotFoundException;

/**
 * Freezes the single Source driver inventory shared by validation, compilation, and runtime discovery.
 * <p>
 * This directory owns driver registration order and exact scheme lookup only. It does not validate Source
 * registrations, compile workers, load project data, or execute authentication capabilities.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DriverDirectory {

    private final List<SourceDriver<?>> drivers;
    private final Map<String, SourceDriver<?>> driversByScheme;

    /**
     * Creates one immutable driver directory and rejects duplicate scheme identifiers.
     *
     * @param drivers drivers in deterministic assembly order
     * @throws IllegalArgumentException if the list, a driver, or a scheme identifier is invalid
     * @throws AlreadyExistsException   if two drivers declare the same scheme identifier
     */
    public DriverDirectory(final List<? extends SourceDriver<?>> drivers) {
        Assert.notNull(drivers, "Source driver list must not be null");
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
        this.drivers = List.copyOf(ordered);
        this.driversByScheme = Map.copyOf(indexed);
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
     * Finds the driver registered for an exact scheme identifier.
     *
     * @param scheme exact scheme identifier
     * @return optional matching driver
     */
    public Optional<SourceDriver<?>> find(final String scheme) {
        Assert.notBlank(scheme, "Source driver scheme must not be blank");
        return Optional.ofNullable(driversByScheme.get(scheme));
    }

    /**
     * Requires the driver registered for an exact scheme identifier.
     *
     * @param scheme exact scheme identifier
     * @return matching driver
     * @throws NotFoundException if no matching driver was assembled
     */
    public SourceDriver<?> require(final String scheme) {
        final SourceDriver<?> driver = find(scheme).getOrNull();
        if (driver == null) {
            throw new NotFoundException("Source driver not found: " + scheme);
        }
        return driver;
    }
}
