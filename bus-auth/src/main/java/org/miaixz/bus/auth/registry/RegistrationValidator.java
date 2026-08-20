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

import org.miaixz.bus.auth.*;
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
 * their dependency order and checks identity, namespace scope, one-to-many relationships, lifecycle compatibility, and
 * Source routing identifiers. Protocol-specific settings are decoded later by the selected Source Driver.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RegistrationValidator {

    /**
     * Creates a stateless validator for complete registration resources.
     */
    public RegistrationValidator() {
        // No initialization required.
    }

    /**
     * Validates Library namespaces and namespace-local application code uniqueness.
     *
     * @param libraries unique Library records by identifier
     * @param issues    mutable issue accumulator
     */
    private static void validateLibraries(
            final Map<String, Registration.Record<?>> libraries,
            final List<RegistryIssue> issues) {
        final Set<String> codes = new HashSet<>();
        for (Registration.Record<?> record : libraries.values()) {
            final Library library = (Library) record.resource();
            requireNamespace(record, library.getNamespace_id(), issues);
            if (StringKit.isBlank(library.getCode())) {
                issue(issues, record, "code", ErrorCode._100100, "Library code must not be blank");
            } else if (StringKit.isNotBlank(library.getNamespace_id())
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
    private static void validateProviders(
            final Map<String, Registration.Record<?>> providers,
            final Map<String, Registration.Record<?>> libraries,
            final List<RegistryIssue> issues) {
        final Set<String> codes = new HashSet<>();
        for (Registration.Record<?> record : providers.values()) {
            final Provider provider = (Provider) record.resource();
            final String libraryId = provider.getLibrary_id();
            if (StringKit.isBlank(libraryId)) {
                issue(issues, record, "library_id", ErrorCode._100100, "Provider requires one Library identifier");
            } else {
                final Registration.Record<?> libraryRecord = libraries.get(libraryId);
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
            if (StringKit.isBlank(provider.getCode())) {
                issue(issues, record, "code", ErrorCode._100100, "Provider code must not be blank");
            } else if (StringKit.isNotBlank(libraryId) && !codes.add(libraryId + Symbol.C_COLON + provider.getCode())) {
                issue(issues, record, "code", ErrorCode._409, "Provider code must be unique within its Library");
            }
            if (StringKit.isBlank(provider.getName())) {
                issue(issues, record, "name", ErrorCode._100100, "Provider name must not be blank");
            }
            if (provider.getSort() != null && provider.getSort() < 0) {
                issue(issues, record, "sort", ErrorCode._100101, "Provider sort must not be negative");
            }
        }
    }

    /**
     * Validates Source routing, namespace scope, required Provider association, and Provider-local code uniqueness.
     *
     * @param sources   unique Source records by identifier
     * @param providers unique Provider records by identifier
     * @param libraries unique Library records by identifier
     * @param issues    mutable issue accumulator
     */
    private static void validateSources(
            final Map<String, Registration.Record<?>> sources,
            final Map<String, Registration.Record<?>> providers,
            final Map<String, Registration.Record<?>> libraries,
            final List<RegistryIssue> issues) {
        final Set<String> codes = new HashSet<>();
        for (Registration.Record<?> record : sources.values()) {
            final Source source = (Source) record.resource();
            requireNamespace(record, source.getNamespace_id(), issues);
            final String providerId = source.getProvider_id();
            if (StringKit.isBlank(providerId)) {
                issue(issues, record, "provider_id", ErrorCode._100100, "Source requires one Provider identifier");
            } else {
                final Registration.Record<?> providerRecord = providers.get(providerId);
                if (providerRecord == null) {
                    issue(issues, record, "provider_id", ErrorCode._404, "Source references an unknown Provider");
                } else {
                    final Provider provider = (Provider) providerRecord.resource();
                    final Registration.Record<?> libraryRecord = libraries.get(provider.getLibrary_id());
                    if (record.enabled() && !providerRecord.enabled()) {
                        issue(
                                issues,
                                record,
                                "provider_id",
                                ErrorCode._100101,
                                "An enabled Source requires an enabled Provider");
                    }
                    if (libraryRecord != null) {
                        final Library library = (Library) libraryRecord.resource();
                        if (!Objects.equals(source.getNamespace_id(), library.getNamespace_id())) {
                            issue(
                                    issues,
                                    record,
                                    "namespace_id",
                                    ErrorCode._100101,
                                    "Source and associated Provider Library must share a namespace");
                        }
                    }
                }
            }
            if (StringKit.isBlank(source.getCode())) {
                issue(issues, record, "code", ErrorCode._100100, "Source code must not be blank");
            } else if (StringKit.isNotBlank(providerId) && !codes.add(providerId + Symbol.C_COLON + source.getCode())) {
                issue(issues, record, "code", ErrorCode._409, "Source code must be unique within its Provider");
            }
            if (StringKit.isBlank(source.getName())) {
                issue(issues, record, "name", ErrorCode._100100, "Source name must not be blank");
            }
            if (source.getSort() != null && source.getSort() < 0) {
                issue(issues, record, "sort", ErrorCode._100101, "Source sort must not be negative");
            }
            validateSourceProtocol(record, source, issues);
            if (StringKit.isBlank(source.getSettings())) {
                issue(issues, record, "settings", ErrorCode._100100, "Source settings must not be blank");
            }
        }
    }

    /**
     * Validates Source driver type and actual protocol identifiers.
     *
     * @param record owning Source registration
     * @param source complete Source entity
     * @param issues mutable issue accumulator
     */
    private static void validateSourceProtocol(
            final Registration.Record<?> record,
            final Source source,
            final List<RegistryIssue> issues) {
        if (StringKit.isBlank(source.getType())) {
            issue(issues, record, "type", ErrorCode._100100, "Source type must not be blank");
        }
        if (StringKit.isBlank(source.getProtocol())) {
            issue(issues, record, "protocol", ErrorCode._100100, "Source protocol must not be blank");
        }
    }

    /**
     * Requires a Library or Source namespace identifier.
     *
     * @param record      owning registration
     * @param namespaceId namespace identifier
     * @param issues      mutable issue accumulator
     */
    private static void requireNamespace(
            final Registration.Record<?> record,
            final String namespaceId,
            final List<RegistryIssue> issues) {
        if (StringKit.isBlank(namespaceId)) {
            issue(
                    issues,
                    record,
                    "namespace_id",
                    ErrorCode._100100,
                    record.kind() + " registration requires a namespace identifier");
        }
    }

    /**
     * Adds one safe validation issue without including settings or credential material.
     *
     * @param issues      mutable issue accumulator
     * @param record      owning registration
     * @param field       safe entity field identifier
     * @param error       shared Bus error code
     * @param description non-sensitive diagnostic description
     */
    private static void issue(
            final List<RegistryIssue> issues,
            final Registration.Record<?> record,
            final String field,
            final Errors error,
            final String description) {
        final String id = StringKit.isBlank(record.resource().getId()) ? record.kind().name()
                : record.resource().getId();
        issues.add(
                new RegistryIssue(record.kind(), id, RegistryIssue.Stage.VALIDATE, Optional.empty(), Optional.of(field),
                        error, description));
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
        final Map<String, Registration.Record<?>> libraries = new HashMap<>();
        final Map<String, Registration.Record<?>> providers = new HashMap<>();
        final Map<String, Registration.Record<?>> sources = new HashMap<>();
        final Set<String> identities = new HashSet<>();
        for (Registration.Record<?> record : snapshot.records()) {
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
        validateSources(sources, providers, libraries, issues);
        return new Registry.Report(snapshot.revision(), issues);
    }

}
