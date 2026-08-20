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

import java.util.List;

import org.miaixz.bus.auth.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Validates complete Sources against the frozen Source driver profile catalog.
 * <p>
 * Each Source selects one driver by stable type identifier. The selected driver validates the actual protocol and
 * decodes the raw settings JSON; Vendor-specific platform and variant invariants remain with the Vendor compiler.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SourceValidator {

    /**
     * Frozen profiles obtained from explicitly supplied Source drivers.
     */
    private final List<? extends SourceDriver<?>> drivers;

    /**
     * Creates a Source validator backed by explicitly supplied Source drivers.
     *
     * @param drivers Source drivers
     * @throws IllegalArgumentException if the list, a driver, or a profile is {@code null}
     */
    public SourceValidator(final List<? extends SourceDriver<?>> drivers) {
        Assert.notNull(drivers, "Source drivers must not be null");
        for (SourceDriver<?> driver : drivers) {
            Assert.notNull(
                    Assert.notNull(driver, "Source driver must not be null").profile(),
                    "Source driver profile must not be null");
        }
        this.drivers = List.copyOf(drivers);
    }

    /**
     * Validates Source namespace, association shape, type, protocol, and settings boundary.
     *
     * @param value complete Source entity
     * @throws ValidateException        if a generic Source does not match its protocol profile
     * @throws IllegalArgumentException if a common required field is {@code null} or blank
     */
    public void validate(final Source value) {
        Assert.notNull(value, "Source must not be null");
        Assert.notBlank(value.getNamespace_id(), "Source namespace id must not be blank");
        Assert.notBlank(value.getProvider_id(), "Source provider id must not be blank");
        Assert.notBlank(value.getCode(), "Source code must not be blank");
        Assert.notBlank(value.getName(), "Source name must not be blank");
        if (value.getSort() != null) {
            Assert.isTrue(value.getSort() >= 0, "Source sort must not be negative");
        }
        final String type = Assert.notBlank(value.getType(), "Source type must not be blank");
        final String protocol = Assert.notBlank(value.getProtocol(), "Source protocol must not be blank");
        final SourceDriver<?> driver = driver(type);
        if (!driver.supports(protocol)) {
            throw new ValidateException("Source protocol is not supported by type " + type);
        }
        driver.decode(value);
    }

    /**
     * Resolves the unique profile for a Source category without introducing an implementation dependency.
     *
     * @param type stable Source driver type
     * @return unique matching Source driver
     * @throws ValidateException if the frozen catalog has no matching profile
     */
    private SourceDriver<?> driver(final String type) {
        for (SourceDriver<?> driver : drivers) {
            if (driver.profile().id().equals(type)) {
                return driver;
            }
        }
        throw new ValidateException("Source driver not found for type " + type);
    }

}
