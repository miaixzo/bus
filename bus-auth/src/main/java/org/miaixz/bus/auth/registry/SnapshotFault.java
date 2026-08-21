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

import org.miaixz.bus.auth.Registration;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Describes one safe, resource-addressable fault that rejected a Registry snapshot.
 * <p>
 * The value uses shared Bus errors and contains no raw options, credentials, tokens, private payloads, exception stack,
 * or implementation class names. It is diagnostic data and does not define a custom exception or protocol error
 * response.
 * </p>
 *
 * @param kind            optional kind of the registration entry that failed
 * @param id              optional resource identifier of the failing entry
 * @param stage           snapshot processing stage that detected the issue
 * @param standard        optional formal standard reference relevant to the issue
 * @param field           optional safe registration or standard field name
 * @param error           shared Bus error code
 * @param safeDescription non-sensitive diagnostic description
 * @author Kimi Liu
 */
public record SnapshotFault(Optional<Registration.Kind> kind, Optional<String> id, Stage stage,
        Optional<String> standard, Optional<String> field, Errors error, String safeDescription) {

    /**
     * Creates an immutable safe Snapshot fault.
     *
     * @param kind            failing registration kind
     * @param id              failing resource identifier
     * @param stage           processing stage
     * @param standard        optional formal standard reference
     * @param field           optional safe field name
     * @param error           shared Bus error
     * @param safeDescription non-sensitive description
     * @throws IllegalArgumentException if a required value is missing or an optional text value is blank
     */
    public SnapshotFault {
        Assert.notNull(kind, "Snapshot fault kind container must not be null");
        kind = Optional.ofNullable(kind.getOrNull());
        Assert.notNull(id, "Snapshot fault resource id container must not be null");
        if (!id.isEmpty()) {
            Assert.notBlank(id.getOrNull(), "Snapshot fault resource id must not be blank");
        }
        id = Optional.ofNullable(id.getOrNull());
        Assert.notNull(stage, "Snapshot fault stage must not be null");
        Assert.notNull(standard, "Snapshot fault standard container must not be null");
        if (!standard.isEmpty()) {
            Assert.notBlank(standard.getOrNull(), "Snapshot fault standard must not be blank");
        }
        standard = Optional.ofNullable(standard.getOrNull());
        Assert.notNull(field, "Snapshot fault field container must not be null");
        if (!field.isEmpty()) {
            Assert.notBlank(field.getOrNull(), "Snapshot fault field must not be blank");
        }
        field = Optional.ofNullable(field.getOrNull());
        Assert.notNull(error, "Snapshot fault Bus error must not be null");
        Assert.notBlank(safeDescription, "Snapshot fault safe description must not be blank");
    }

    /**
     * Creates an issue associated with one exact registration entry.
     *
     * @param kind        registration kind
     * @param id          registration identifier
     * @param stage       processing stage
     * @param field       optional failing field
     * @param error       stable error classification
     * @param description safe description
     * @return structured entry fault
     */
    public static SnapshotFault entry(
            final Registration.Kind kind,
            final String id,
            final Stage stage,
            final Optional<String> field,
            final Errors error,
            final String description) {
        return new SnapshotFault(Optional.of(kind), Optional.of(id), stage, Optional.empty(), field, error,
                description);
    }

    /**
     * Creates an issue associated with the complete reload attempt rather than one entry.
     *
     * @param stage       processing stage
     * @param field       optional failing field
     * @param error       stable error classification
     * @param description safe description
     * @return structured snapshot fault
     */
    public static SnapshotFault snapshot(
            final Stage stage,
            final Optional<String> field,
            final Errors error,
            final String description) {
        return new SnapshotFault(Optional.empty(), Optional.empty(), stage, Optional.empty(), field, error,
                description);
    }

    /**
     * Identifies the atomic snapshot processing stage that detected an issue.
     *
     * @author Kimi Liu
     */
    public enum Stage {

        /**
         * External snapshot loading failed.
         */
        LOAD,

        /**
         * Raw registration validation failed.
         */
        VALIDATE,

        /**
         * Source worker compilation failed.
         */
        COMPILE,

        /**
         * Atomic view publication failed.
         */
        COMMIT

    }

}
