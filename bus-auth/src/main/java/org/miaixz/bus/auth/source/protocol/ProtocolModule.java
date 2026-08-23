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
package org.miaixz.bus.auth.source.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.miaixz.bus.auth.source.SourceDescriptor;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.source.SourceModule;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Freezes all standards-based protocol drivers and their one-to-one descriptors as one Source module.
 *
 * @author Kimi Liu
 */
public class ProtocolModule implements SourceModule {

    /**
     * Drivers in deterministic connector and role order.
     */
    private final List<SourceDriver<?>> drivers;

    /**
     * Exact protocol descriptors in the same order as their drivers.
     */
    private final List<SourceDescriptor> descriptors;

    /**
     * Creates an immutable Protocol module from a complete non-empty driver collection.
     *
     * @param drivers protocol drivers in deterministic order
     */
    public ProtocolModule(final Collection<? extends ProtocolDriver<?>> drivers) {
        Assert.notNull(drivers, "Protocol module driver collection must not be null");
        if (drivers.isEmpty()) {
            throw new ValidateException("Protocol module must contain at least one driver");
        }
        final List<SourceDriver<?>> compiled = new ArrayList<>(drivers.size());
        final List<SourceDescriptor> selections = new ArrayList<>(drivers.size());
        for (ProtocolDriver<?> candidate : drivers) {
            final ProtocolDriver<?> driver = Assert.notNull(candidate, "Protocol module driver must not be null");
            compiled.add(driver);
            selections.add(new ProtocolDescriptor(driver.scheme()));
        }
        this.drivers = List.copyOf(compiled);
        this.descriptors = List.copyOf(selections);
    }

    /**
     * Returns protocol drivers in deterministic assembly order.
     *
     * @return immutable protocol driver list
     */
    @Override
    public List<SourceDriver<?>> drivers() {
        return drivers;
    }

    /**
     * Returns exact protocol choices in matching deterministic order.
     *
     * @return immutable protocol descriptor list
     */
    @Override
    public List<SourceDescriptor> descriptors() {
        return descriptors;
    }

}
