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
package org.miaixz.bus.auth.registry;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.auth.source.DriverDirectory;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Validates complete Sources against the frozen Source driver scheme catalog.
 * <p>
 * Each Source selects one driver by stable type identifier. The external project supplies an already materialized
 * Options value; Vendor-specific platform and variant invariants remain with the Vendor compiler.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SourceValidator {

    /**
     * Frozen profiles obtained from explicitly supplied Source drivers.
     */
    private final DriverDirectory drivers;

    /**
     * Creates a Source validator backed by explicitly supplied Source drivers.
     *
     * @param drivers Source drivers
     * @throws IllegalArgumentException if the list, a driver, or a scheme is {@code null}
     */
    public SourceValidator(final DriverDirectory drivers) {
        this.drivers = Assert.notNull(drivers, "Source driver directory must not be null");
    }

    /**
     * Validates Source association shape, type, protocol, and options boundary.
     *
     * @param value complete Source entity
     * @throws ValidateException        if a generic Source does not match its protocol scheme
     * @throws IllegalArgumentException if a common required field is {@code null} or blank
     */
    public List<EntityViolation> validate(final Source value) {
        Assert.notNull(value, "Source must not be null");
        final List<EntityViolation> issues = new ArrayList<>();
        required(issues, "provider_id", value.getProvider_id(), "Source provider id must not be blank");
        required(issues, "code", value.getCode(), "Source code must not be blank");
        required(issues, "name", value.getName(), "Source name must not be blank");
        if (value.getSort() != null) {
            if (value.getSort() < 0) {
                issues.add(new EntityViolation("sort", ErrorCode._100101, "Source sort must not be negative"));
            }
        }
        if (StringKit.isBlank(value.getType())) {
            issues.add(new EntityViolation("type", ErrorCode._100100, "Source type must not be blank"));
            return List.copyOf(issues);
        }
        final SourceDriver<?> driver;
        try {
            driver = driver(value.getType());
        } catch (RuntimeException ignored) {
            issues.add(new EntityViolation("type", ErrorCode._404, "Source driver is not assembled"));
            return List.copyOf(issues);
        }
        if (StringKit.isBlank(value.getProtocol()) || !driver.supports(value.getProtocol())) {
            issues.add(
                    new EntityViolation("protocol", ErrorCode._100101,
                            "Source protocol is not supported by the selected type"));
        }
        final Options<?> options = value.getOptions();
        if (options == null) {
            issues.add(new EntityViolation("options", ErrorCode._100100, "Source options must not be null"));
        } else {
            try {
                final Class<?> declared = Assert.notNull(options.type(), "Source options type must not be null");
                if (declared != options.getClass()) {
                    throw new ValidateException("Source options type must exactly match its implementation class");
                }
                final Options<?> snapshot = Assert.notNull(options.snapshot(), "Source options snapshot must not be null");
                if (snapshot.type() != snapshot.getClass() || snapshot.type() != declared) {
                    throw new ValidateException("Source options snapshot must preserve its exact implementation type");
                }
                driver.validate(value);
            } catch (RuntimeException ignored) {
                issues.add(
                        new EntityViolation("options", ErrorCode._100101,
                                "Source options do not match the selected type"));
            }
        }
        return List.copyOf(issues);
    }

    private static void required(
            final List<EntityViolation> issues,
            final String field,
            final String value,
            final String description) {
        if (StringKit.isBlank(value)) {
            issues.add(new EntityViolation(field, ErrorCode._100100, description));
        }
    }

    /**
     * Resolves the unique scheme for a Source category without introducing an implementation dependency.
     *
     * @param type stable Source driver type
     * @return unique matching Source driver
     * @throws ValidateException if the frozen catalog has no matching scheme
     */
    private SourceDriver<?> driver(final String type) {
        return drivers.require(type);
    }

}
