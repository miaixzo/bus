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

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Supplies the immutable drivers and exact management descriptors contributed by one Source implementation branch.
 * <p>
 * Modules are startup assembly values. They never load registrations, infer descriptors from drivers, compile workers,
 * or mutate a running Runtime.
 * </p>
 *
 * @author Kimi Liu
 */
public interface SourceModule {

    /**
     * Returns drivers in deterministic assembly order.
     *
     * @return immutable non-empty driver list
     */
    List<SourceDriver<?>> drivers();

    /**
     * Returns exact Source choices in deterministic management order.
     *
     * @return immutable non-empty descriptor list
     */
    List<SourceDescriptor> descriptors();

    /**
     * Creates an explicit module for one custom driver and its complete descriptor set.
     *
     * @param driver      custom Source driver
     * @param descriptors exact descriptors owned by the driver
     * @return immutable Source module
     */
    static SourceModule of(final SourceDriver<?> driver, final SourceDescriptor... descriptors) {
        final SourceDriver<?> checked = Assert.notNull(driver, "Source module driver must not be null");
        Assert.notNull(descriptors, "Source module descriptors must not be null");
        final List<SourceDescriptor> selections = Arrays.stream(descriptors)
                .map(descriptor -> Assert.notNull(descriptor, "Source module descriptor must not be null")).toList();
        return new Fixed(List.of(checked), selections);
    }

    /**
     * Stores the explicitly supplied custom driver and descriptors without adding inferred facts.
     *
     * @param drivers     immutable driver list
     * @param descriptors immutable descriptor list
     * @author Kimi Liu
     */
    record Fixed(List<SourceDriver<?>> drivers, List<SourceDescriptor> descriptors) implements SourceModule {

        /**
         * Validates and detaches one explicit module value.
         */
        public Fixed {
            Assert.notNull(drivers, "Source module driver list must not be null");
            Assert.notNull(descriptors, "Source module descriptor list must not be null");
            if (drivers.isEmpty() || descriptors.isEmpty()) {
                throw new ValidateException("Source module drivers and descriptors must not be empty");
            }
            for (SourceDriver<?> driver : drivers) {
                Assert.notNull(driver, "Source module driver must not be null");
            }
            for (SourceDescriptor descriptor : descriptors) {
                Assert.notNull(descriptor, "Source module descriptor must not be null");
            }
            drivers = List.copyOf(drivers);
            descriptors = List.copyOf(descriptors);
        }

    }

}
