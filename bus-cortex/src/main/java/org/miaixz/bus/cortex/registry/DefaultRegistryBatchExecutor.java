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
package org.miaixz.bus.cortex.registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.builtin.batch.BatchOperation;
import org.miaixz.bus.cortex.builtin.batch.BatchResult;
import org.miaixz.bus.logger.Logger;

/**
 * Default registry batch executor preserving the historical per-entry behavior.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultRegistryBatchExecutor implements RegistryBatchExecutor {

    /**
     * Optional batch resolvers evaluated before per-entry fallback lookup.
     */
    private final List<RegistryBatchResolver> batchResolvers;

    /**
     * Creates a default executor without batch resolvers.
     */
    public DefaultRegistryBatchExecutor() {
        this(List.of());
    }

    /**
     * Creates a default executor with batch resolvers.
     *
     * @param batchResolvers batch resolvers
     */
    public DefaultRegistryBatchExecutor(List<RegistryBatchResolver> batchResolvers) {
        List<RegistryBatchResolver> resolvers = new ArrayList<>();
        if (batchResolvers != null) {
            for (RegistryBatchResolver resolver : batchResolvers) {
                if (resolver != null) {
                    resolvers.add(resolver);
                }
            }
        }
        resolvers.sort(Comparator.comparingInt(RegistryBatchResolver::order));
        this.batchResolvers = List.copyOf(resolvers);
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean supports(BatchOperation operation, RegistryBatchOperations operations) {
        return true;
    }

    @Override
    public BatchResult execute(BatchOperation operation, RegistryBatchOperations operations) {
        BatchResult result = new BatchResult();
        if (operation == null || operation.getEntries() == null || operation.getEntries().isEmpty()) {
            return result;
        }
        RegistryBatchLookup lookup = resolveLookup(operation, operations, result);
        for (Assets source : operation.getEntries()) {
            if (source == null) {
                result.recordFailure(
                        BatchResult.Failure
                                .of(null, operation.getNamespace_id(), null, null, null, "Null batch entry"));
                if (!operations.continueOnError(operation)) {
                    break;
                }
                continue;
            }
            Assets entry = operations.prepareEntry(operation, source);
            try {
                operations.enforce(
                        operation.getOperationType() == null ? "batch"
                                : operation.getOperationType().name().toLowerCase(),
                        RegistryIdentity.type(entry),
                        entry);
                applyBatchEntry(operation, entry, result, operations, lookup);
            } catch (Exception e) {
                result.recordFailure(
                        BatchResult.Failure.of(
                                RegistryIdentity.type(entry),
                                entry.getNamespace_id(),
                                entry.getId(),
                                entry.getMethod(),
                                entry.getVersion(),
                                e.getMessage()));
                if (!operations.continueOnError(operation)) {
                    break;
                }
            }
        }
        return result;
    }

    private RegistryBatchLookup resolveLookup(
            BatchOperation operation,
            RegistryBatchOperations operations,
            BatchResult result) {
        if (batchResolvers.isEmpty()) {
            return RegistryBatchLookup.empty();
        }
        for (RegistryBatchResolver resolver : batchResolvers) {
            boolean supported;
            try {
                supported = resolver.supports(operation, operations);
            } catch (Exception e) {
                Logger.warn(
                        false,
                        "Cortex",
                        e,
                        "Registry batch resolver skipped: resolver={}, operation={}, error={}",
                        resolver.getClass().getName(),
                        operation.getOperationType(),
                        e.getMessage());
                result.getWarnings().add("Batch resolver skipped: " + resolver.getClass().getName());
                continue;
            }
            if (!supported) {
                continue;
            }
            try {
                RegistryBatchLookup lookup = resolver.resolve(operation, operations);
                if (lookup == null) {
                    return RegistryBatchLookup.empty();
                }
                if (lookup.warnings() != null && !lookup.warnings().isEmpty()) {
                    result.getWarnings().addAll(lookup.warnings());
                }
                Logger.debug(
                        true,
                        "Cortex",
                        "Registry batch resolver selected: resolver={}, existingRoutes={}, missingRoutes={}",
                        resolver.getClass().getName(),
                        lookup.existingByRoute().size(),
                        lookup.missingRoutes().size());
                return lookup;
            } catch (Exception e) {
                Logger.warn(
                        false,
                        "Cortex",
                        e,
                        "Registry batch resolver failed: resolver={}, operation={}, error={}",
                        resolver.getClass().getName(),
                        operation.getOperationType(),
                        e.getMessage());
                result.getWarnings().add("Batch resolver failed: " + resolver.getClass().getName());
            }
        }
        return RegistryBatchLookup.empty();
    }

    private void applyBatchEntry(
            BatchOperation operation,
            Assets entry,
            BatchResult result,
            RegistryBatchOperations operations,
            RegistryBatchLookup lookup) {
        BatchOperation.OperationType operationType = operation.getOperationType();
        if (operationType == null) {
            throw new IllegalArgumentException("Batch operation type is required");
        }
        if (operations.dryRun(operation)) {
            result.recordSkip();
            result.getWarnings().add("Dry-run skipped mutation for " + entry.getId());
            return;
        }
        switch (operationType) {
            case DEREGISTER -> {
                Assets existing = resolveExisting(entry, operations, lookup);
                if (existing == null) {
                    result.recordSkip();
                    return;
                }
                operations.delete(RegistryIdentity.type(existing), existing.getNamespace_id(), existing.getId());
                result.recordDelete();
            }
            case REGISTER, UPDATE, UPSERT -> {
                Assets existing = resolveExisting(entry, operations, lookup);
                if (existing != null && operation.getConflictPolicy() == BatchOperation.ConflictPolicy.FAIL_FAST) {
                    throw new IllegalStateException("Conflict detected for " + existing.getId());
                }
                if (existing != null && operation.getConflictPolicy() == BatchOperation.ConflictPolicy.SKIP) {
                    result.recordSkip();
                    result.getWarnings().add("Skipped existing entry " + existing.getId());
                    return;
                }
                if (existing != null && existing.getId() != null) {
                    entry.setId(existing.getId());
                }
                operations.upsert(entry);
                if (existing == null) {
                    result.recordInsert();
                } else {
                    result.recordUpdate();
                }
            }
        }
    }

    private Assets resolveExisting(Assets entry, RegistryBatchOperations operations, RegistryBatchLookup lookup) {
        RegistryRouteKey routeKey = operations.routeKey(entry);
        if (routeKey == null || lookup == null) {
            return operations.resolveExisting(entry);
        }
        Assets existing = lookup.existingByRoute().get(routeKey);
        if (existing != null) {
            return existing;
        }
        if (lookup.authoritativeByRoute() && lookup.missingRoutes().contains(routeKey)) {
            return null;
        }
        if (lookup.skipMethodVersionFallback() && canSkipMethodVersionFallback(entry, routeKey)) {
            return null;
        }
        return operations.resolveExisting(entry);
    }

    private boolean canSkipMethodVersionFallback(Assets entry, RegistryRouteKey routeKey) {
        return entry != null && routeKey != null && Integer.valueOf(Type.API.key()).equals(routeKey.type())
                && entry.getMethod() != null && entry.getVersion() != null && entry.getVerb() != null;
    }

}
