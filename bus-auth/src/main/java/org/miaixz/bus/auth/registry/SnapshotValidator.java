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

import java.util.*;

import org.miaixz.bus.auth.Blueprint;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.Source;
import org.miaixz.bus.core.basic.entity.Entity;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Validates one complete candidate Registry snapshot before runtime compilation.
 * <p>
 * External projects supply complete {@link org.miaixz.bus.auth.Library}, {@link Provider}, and {@link Source} objects.
 * Validation follows their dependency order and checks identity, ownership relationships, lifecycle compatibility,
 * Source routing identifiers, and the presence of already materialized protocol options. Project presentation, launch,
 * ordering, and management uniqueness rules are deliberately excluded. Concrete option invariants are validated later
 * by the selected Source Driver.
 * </p>
 *
 * @author Kimi Liu
 */
public class SnapshotValidator {

    /**
     * Validates Source routing against the exact drivers assembled for this runtime.
     */
    private final SourceValidator sourceValidator;

    /**
     * Creates a validator for complete candidate snapshots and the frozen runtime Source inventory.
     *
     * @param sourceValidator Source-specific validator bound to assembled drivers
     */
    public SnapshotValidator(final SourceValidator sourceValidator) {
        this.sourceValidator = Assert.notNull(sourceValidator, "Source validator must not be null");
    }

    /**
     * Adds one safe validation fault without including options or credential material.
     *
     * @param faults      mutable fault accumulator
     * @param record      owning registration
     * @param field       safe entity field identifier
     * @param error       shared Bus error code
     * @param description non-sensitive diagnostic description
     */
    private static void fault(
            final List<SnapshotFault> faults,
            final Blueprint.Entry record,
            final String field,
            final Errors error,
            final String description) {
        final String id = StringKit.isBlank(record.resource().getId()) ? record.kind().name()
                : record.resource().getId();
        faults.add(
                SnapshotFault.entry(
                        record.kind(),
                        id,
                        SnapshotFault.Stage.VALIDATE,
                        Optional.of(field),
                        error,
                        description));
    }

    /**
     * Converts field violations into structured snapshot faults.
     *
     * @param faults     destination fault list
     * @param record     registration being validated
     * @param violations field violations to convert
     */
    private static void violations(
            final List<SnapshotFault> faults,
            final Blueprint.Entry record,
            final List<FieldViolation> violations) {
        for (FieldViolation violation : violations) {
            fault(faults, record, violation.field(), violation.error(), violation.description());
        }
    }

    /**
     * Validates required Provider-to-Library ownership and enabled-parent compatibility.
     *
     * @param providers unique Provider records by identifier
     * @param libraries unique Library records by identifier
     * @param faults    mutable fault accumulator
     */
    private void validateProviders(
            final Map<String, Blueprint.Entry> providers,
            final Map<String, Blueprint.Entry> libraries,
            final List<SnapshotFault> faults) {
        for (Blueprint.Entry record : providers.values()) {
            final Provider provider = (Provider) record.resource();
            final String libraryId = provider.getLibrary_id();
            if (StringKit.isBlank(libraryId)) {
                fault(faults, record, "library_id", ErrorCode._100100, "Provider Library id must not be blank");
            } else {
                final Blueprint.Entry libraryRecord = libraries.get(libraryId);
                if (libraryRecord == null) {
                    fault(faults, record, "library_id", ErrorCode._404, "Provider references an unknown Library");
                } else if (record.enabled() && !libraryRecord.enabled()) {
                    fault(
                            faults,
                            record,
                            "library_id",
                            ErrorCode._100101,
                            "An enabled Provider requires an enabled Library");
                }
            }
        }
    }

    /**
     * Validates Source routing, required Provider association, and enabled-parent compatibility.
     *
     * @param sources   unique Source records by identifier
     * @param providers unique Provider records by identifier
     * @param faults    mutable fault accumulator
     */
    private void validateSources(
            final Map<String, Blueprint.Entry> sources,
            final Map<String, Blueprint.Entry> providers,
            final List<SnapshotFault> faults) {
        for (Blueprint.Entry record : sources.values()) {
            final Source source = (Source) record.resource();
            violations(faults, record, sourceValidator.validate(source));
            final String providerId = source.getProvider_id();
            if (StringKit.isNotBlank(providerId)) {
                final Blueprint.Entry providerRecord = providers.get(providerId);
                if (providerRecord == null) {
                    fault(faults, record, "provider_id", ErrorCode._404, "Source references an unknown Provider");
                } else if (record.enabled() && !providerRecord.enabled()) {
                    fault(
                            faults,
                            record,
                            "provider_id",
                            ErrorCode._100101,
                            "An enabled Source requires an enabled Provider");
                }
            }
        }
    }

    /**
     * Validates a complete desired snapshot and returns safe resource-addressable faults.
     *
     * @param snapshot complete desired registration snapshot
     * @return report for the supplied revision
     * @throws IllegalArgumentException if the snapshot is {@code null}
     */
    public Registry.Report validate(final Registry.Snapshot snapshot) {
        Assert.notNull(snapshot, "Registry snapshot must not be null");
        final List<SnapshotFault> faults = new ArrayList<>();
        final Map<String, Blueprint.Entry> libraries = new HashMap<>();
        final Map<String, Blueprint.Entry> providers = new HashMap<>();
        final Map<String, Blueprint.Entry> sources = new HashMap<>();
        final Set<String> identities = new HashSet<>();
        for (Blueprint.Entry record : snapshot.records()) {
            final Entity resource = record.resource();
            final String id = resource.getId();
            if (StringKit.isBlank(id)) {
                fault(faults, record, "id", ErrorCode._100100, "Registration resource identifier must not be blank");
                continue;
            }
            final String identity = record.kind().name() + Symbol.C_COLON + id;
            if (!identities.add(identity)) {
                fault(faults, record, "id", ErrorCode._409, "Duplicate registration resource identifier");
                continue;
            }
            switch (record.kind()) {
                case LIBRARY -> libraries.put(id, record);
                case PROVIDER -> providers.put(id, record);
                case SOURCE -> sources.put(id, record);
            }
        }
        validateProviders(providers, libraries, faults);
        validateSources(sources, providers, faults);
        return new Registry.Report(snapshot.revision(), faults);
    }

}
