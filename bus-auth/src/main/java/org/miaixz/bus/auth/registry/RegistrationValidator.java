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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.Library;
import org.miaixz.bus.auth.Provider;
import org.miaixz.bus.auth.Registration;
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
 * Validates one complete entity registration snapshot before runtime compilation.
 * <p>
 * External projects supply complete {@link Library}, {@link Provider}, and {@link Source} objects. Validation follows
 * their dependency order and checks identity, Library namespace scope, one-to-many relationships, lifecycle
 * compatibility, Source routing identifiers, and the presence of already materialized protocol options. Concrete
 * option invariants are validated later by the selected Source Driver.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RegistrationValidator {

    /**
     * Validates Source routing against the exact drivers assembled for this runtime.
     */
    private final SourceValidator sourceValidator;
    private final LibraryValidator libraryValidator;
    private final ProviderValidator providerValidator;

    /**
     * Creates a validator for complete registration resources and the frozen runtime Source inventory.
     *
     * @param sourceValidator Source-specific validator bound to assembled drivers
     */
    public RegistrationValidator(final SourceValidator sourceValidator) {
        this.sourceValidator = Assert.notNull(sourceValidator, "Source validator must not be null");
        this.libraryValidator = new LibraryValidator();
        this.providerValidator = new ProviderValidator();
    }

    /**
     * Validates Library namespaces and namespace-local application code uniqueness.
     *
     * @param libraries unique Library records by identifier
     * @param issues    mutable issue accumulator
     */
    private void validateLibraries(final Map<String, Registration.Entry> libraries, final List<RegistryIssue> issues) {
        final Set<String> codes = new HashSet<>();
        for (Registration.Entry record : libraries.values()) {
            final Library library = (Library) record.resource();
            violations(issues, record, libraryValidator.validate(library));
            if (StringKit.isNotBlank(library.getCode()) && StringKit.isNotBlank(library.getNamespace_id())
                    && !codes.add(library.getNamespace_id() + Symbol.C_COLON + library.getCode())) {
                issue(issues, record, "code", ErrorCode._409, "Library code must be unique within its namespace");
            }
        }
    }

    /**
     * Validates Provider identity, Library ownership, and Library-local code uniqueness.
     *
     * @param providers unique Provider records by identifier
     * @param libraries unique Library records by identifier
     * @param issues    mutable issue accumulator
     */
    private void validateProviders(
            final Map<String, Registration.Entry> providers,
            final Map<String, Registration.Entry> libraries,
            final List<RegistryIssue> issues) {
        final Set<String> codes = new HashSet<>();
        for (Registration.Entry record : providers.values()) {
            final Provider provider = (Provider) record.resource();
            violations(issues, record, providerValidator.validate(provider));
            final String libraryId = provider.getLibrary_id();
            if (StringKit.isNotBlank(libraryId)) {
                final Registration.Entry libraryRecord = libraries.get(libraryId);
                if (libraryRecord == null) {
                    issue(issues, record, "library_id", ErrorCode._404, "Provider references an unknown Library");
                } else if (record.enabled() && !libraryRecord.enabled()) {
                    issue(
                            issues,
                            record,
                            "library_id",
                            ErrorCode._100101,
                            "An enabled Provider requires an enabled Library");
                }
            }
            if (StringKit.isNotBlank(provider.getCode()) && StringKit.isNotBlank(libraryId)
                    && !codes.add(libraryId + Symbol.C_COLON + provider.getCode())) {
                issue(issues, record, "code", ErrorCode._409, "Provider code must be unique within its Library");
            }
        }
    }

    /**
     * Adds one safe validation issue without including options or credential material.
     *
     * @param issues      mutable issue accumulator
     * @param record      owning registration
     * @param field       safe entity field identifier
     * @param error       shared Bus error code
     * @param description non-sensitive diagnostic description
     */
    private static void issue(
            final List<RegistryIssue> issues,
            final Registration.Entry record,
            final String field,
            final Errors error,
            final String description) {
        final String id = StringKit.isBlank(record.resource().getId()) ? record.kind().name()
                : record.resource().getId();
        issues.add(
                RegistryIssue.entry(
                        record.kind(),
                        id,
                        RegistryIssue.Stage.VALIDATE,
                        Optional.of(field),
                        error,
                        description));
    }

    private static void violations(
            final List<RegistryIssue> issues,
            final Registration.Entry record,
            final List<EntityViolation> violations) {
        for (EntityViolation violation : violations) {
            issue(issues, record, violation.field(), violation.error(), violation.description());
        }
    }

    /**
     * Validates Source routing, required Provider association, and Provider-local code uniqueness.
     *
     * @param sources   unique Source records by identifier
     * @param providers unique Provider records by identifier
     * @param issues    mutable issue accumulator
     */
    private void validateSources(
            final Map<String, Registration.Entry> sources,
            final Map<String, Registration.Entry> providers,
            final List<RegistryIssue> issues) {
        final Set<String> codes = new HashSet<>();
        for (Registration.Entry record : sources.values()) {
            final Source source = (Source) record.resource();
            violations(issues, record, sourceValidator.validate(source));
            final String providerId = source.getProvider_id();
            if (StringKit.isNotBlank(providerId)) {
                final Registration.Entry providerRecord = providers.get(providerId);
                if (providerRecord == null) {
                    issue(issues, record, "provider_id", ErrorCode._404, "Source references an unknown Provider");
                } else {
                    final Provider provider = (Provider) providerRecord.resource();
                    if (record.enabled() && !providerRecord.enabled()) {
                        issue(
                                issues,
                                record,
                                "provider_id",
                                ErrorCode._100101,
                                "An enabled Source requires an enabled Provider");
                    }
                }
            }
            if (StringKit.isNotBlank(source.getCode()) && StringKit.isNotBlank(providerId)
                    && !codes.add(providerId + Symbol.C_COLON + source.getCode())) {
                issue(issues, record, "code", ErrorCode._409, "Source code must be unique within its Provider");
            }
        }
    }

    /**
     * Validates a complete desired snapshot and returns safe resource-addressable issues.
     *
     * @param snapshot complete desired registration snapshot
     * @return report for the supplied revision
     * @throws IllegalArgumentException if the snapshot is {@code null}
     */
    public Registry.Report validate(final Registry.Snapshot snapshot) {
        Assert.notNull(snapshot, "Registry snapshot must not be null");
        final List<RegistryIssue> issues = new ArrayList<>();
        final Map<String, Registration.Entry> libraries = new HashMap<>();
        final Map<String, Registration.Entry> providers = new HashMap<>();
        final Map<String, Registration.Entry> sources = new HashMap<>();
        final Set<String> identities = new HashSet<>();
        for (Registration.Entry record : snapshot.records()) {
            final Entity resource = record.resource();
            final String id = resource.getId();
            if (StringKit.isBlank(id)) {
                issue(issues, record, "id", ErrorCode._100100, "Registration resource identifier must not be blank");
                continue;
            }
            final String identity = record.kind().name() + Symbol.C_COLON + id;
            if (!identities.add(identity)) {
                issue(issues, record, "id", ErrorCode._409, "Duplicate registration resource identifier");
                continue;
            }
            switch (record.kind()) {
                case LIBRARY -> libraries.put(id, record);
                case PROVIDER -> providers.put(id, record);
                case SOURCE -> sources.put(id, record);
            }
        }
        validateLibraries(libraries, issues);
        validateProviders(providers, libraries, issues);
        validateSources(sources, providers, issues);
        return new Registry.Report(snapshot.revision(), issues);
    }

}
