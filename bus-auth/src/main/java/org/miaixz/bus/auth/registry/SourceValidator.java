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
     * Frozen directory of explicitly supplied Source drivers.
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
     * Validates only Source association, type, protocol, and options required by framework compilation.
     *
     * @param value complete Source entity
     * @return immutable field-level framework invariant violations
     * @throws IllegalArgumentException if the Source is {@code null}
     */
    public List<FieldViolation> validate(final Source value) {
        Assert.notNull(value, "Source must not be null");
        final List<FieldViolation> violations = new ArrayList<>();
        required(violations, "provider_id", value.getProvider_id(), "Source provider id must not be blank");
        if (StringKit.isBlank(value.getType())) {
            violations.add(new FieldViolation("type", ErrorCode._100100, "Source type must not be blank"));
            return List.copyOf(violations);
        }
        final SourceDriver<?> driver;
        try {
            driver = driver(value.getType());
        } catch (RuntimeException ignored) {
            violations.add(new FieldViolation("type", ErrorCode._404, "Source driver is not assembled"));
            return List.copyOf(violations);
        }
        if (StringKit.isBlank(value.getProtocol()) || !driver.supports(value.getProtocol())) {
            violations.add(
                    new FieldViolation("protocol", ErrorCode._100101,
                            "Source protocol is not supported by the selected type"));
        }
        final Options<?> options = value.getOptions();
        if (options == null) {
            violations.add(new FieldViolation("options", ErrorCode._100100, "Source options must not be null"));
        } else {
            try {
                final Class<?> declared = Assert.notNull(options.type(), "Source options type must not be null");
                if (declared != options.getClass()) {
                    throw new ValidateException("Source options type must exactly match its implementation class");
                }
                final Options<?> snapshot = Assert
                        .notNull(options.snapshot(), "Source options snapshot must not be null");
                if (snapshot.type() != snapshot.getClass() || snapshot.type() != declared) {
                    throw new ValidateException("Source options snapshot must preserve its exact implementation type");
                }
                driver.validate(value);
            } catch (RuntimeException ignored) {
                violations.add(
                        new FieldViolation("options", ErrorCode._100101,
                                "Source options do not match the selected type"));
            }
        }
        return List.copyOf(violations);
    }

    /**
     * Adds one missing-required-field violation when text is blank.
     *
     * @param violations  destination violations
     * @param field       field name
     * @param value       candidate value
     * @param description safe violation description
     */
    private static void required(
            final List<FieldViolation> violations,
            final String field,
            final String value,
            final String description) {
        if (StringKit.isBlank(value)) {
            violations.add(new FieldViolation(field, ErrorCode._100100, description));
        }
    }

    /**
     * Resolves the exact assembled driver for a stable Source type.
     *
     * @param type stable Source driver type
     * @return unique matching Source driver
     * @throws ValidateException if the frozen directory has no matching driver
     */
    private SourceDriver<?> driver(final String type) {
        return drivers.require(type);
    }

}
