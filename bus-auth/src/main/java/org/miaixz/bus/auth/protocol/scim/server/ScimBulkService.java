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
package org.miaixz.bus.auth.protocol.scim.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.scim.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates standard SCIM Bulk limits and typed operation data before delegating ordered execution to the store.
 *
 * @author Kimi Liu
 */
public final class ScimBulkService {

    /**
     * Compiled server-role Source identifier used to isolate Bulk execution.
     */
    private final String providerId;

    /**
     * Validated Provider settings governing Bulk and resource policy.
     */
    private final ScimProviderSettings settings;

    /**
     * External project implementation of ordered Bulk execution.
     */
    private final ScimResourceStore store;

    /**
     * Creates a Bulk service for one exact compiled SCIM Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param settings   validated SCIM Provider settings
     * @param store      externally supplied resource store
     * @throws IllegalArgumentException if an argument is {@code null} or the identifier is blank
     */
    public ScimBulkService(final String providerId, final ScimProviderSettings settings,
            final ScimResourceStore store) {
        this.providerId = Assert.notBlank(providerId, "SCIM Provider id must not be blank");
        this.settings = Assert.notNull(settings, "SCIM Provider settings must not be null");
        this.store = Assert.notNull(store, "SCIM Bulk store must not be null");
    }

    /**
     * Creates a non-sensitive framework failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a safe external-store contract failure.
     *
     * @return operational Bulk failure
     */
    private static Outcome.Failure storeFailure() {
        return failure(ErrorCode._500, "SCIM Bulk resource store failed or returned an invalid response");
    }

    /**
     * Creates a completed stage with inferred success type.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed asynchronous stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Executes one ordered RFC 7644 Bulk request without adding rollback semantics.
     *
     * @param request standard typed Bulk request owned by the caller until stage completion
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing the ordered Bulk response or a closed framework failure
     */
    public CompletionStage<Outcome<BulkResponse>> bulk(
            final BulkRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "SCIM Bulk request must not be null");
        Assert.notNull(context, "SCIM Bulk context must not be null");
        Assert.notNull(timeout, "SCIM Bulk time budget must not be null");
        final ServiceProviderConfig.Bulk bulk = settings.serviceProviderConfig().bulk();
        if (!bulk.supported() || request.operations().size() > bulk.maxOperations()) {
            return completed(
                    Outcome.rejected(
                            failure(ErrorCode._413, "SCIM Bulk operation count exceeds the configured limit")));
        }
        for (BulkRequest.Operation operation : request.operations()) {
            final Outcome.Failure invalid = operation(operation);
            if (invalid != null) {
                return completed(Outcome.rejected(invalid));
            }
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(failure(ErrorCode._408, "SCIM Bulk request has no remaining time budget")));
        }
        try {
            final CompletionStage<Outcome<BulkResponse>> stage = store.bulk(providerId, request, context, timeout);
            if (stage == null) {
                return completed(Outcome.failed(storeFailure()));
            }
            return stage.handle((outcome, thrown) -> validate(outcome, thrown, request.operations()));
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(storeFailure()));
        }
    }

    /**
     * Validates one typed Bulk operation against Provider settings and its registered resource target.
     *
     * @param operation candidate operation
     * @return validation failure, or {@code null} when valid
     */
    private Outcome.Failure operation(final BulkRequest.Operation operation) {
        final ResourceTarget target = operation.target();
        final String type = target.resourceType().name();
        if (!settings.supports(type)) {
            return failure(ErrorCode._404, "SCIM Bulk operation resource type is not enabled");
        }
        if (!settings.serviceProviderConfig().etag().supported() && !operation.version().isEmpty()) {
            return failure(ErrorCode._412, "SCIM conditional version is disabled");
        }
        final BulkRequest.Data data = operation.data().getOrNull();
        if (data instanceof BulkRequest.ResourceData resourceData) {
            final Resource resource = resourceData.resource();
            if (Scim.USER_SCHEMA.equals(target.resourceType().schema()) != (resource instanceof User)
                    || Scim.GROUP_SCHEMA.equals(target.resourceType().schema()) != (resource instanceof Group)) {
                return failure(ErrorCode._400, "SCIM Bulk resource data does not match operation target");
            }
            if (resource instanceof User user && !user.password().isEmpty()
                    && !settings.serviceProviderConfig().changePassword().supported()) {
                return failure(ErrorCode._400, "SCIM password change is disabled");
            }
            final Resource.Common common = resource instanceof User user ? user.common() : ((Group) resource).common();
            if (operation.method() == Http.Method.POST && (!common.id().isEmpty() || !common.meta().isEmpty())) {
                return failure(ErrorCode._400, "SCIM Bulk POST data must omit id and meta");
            }
            if (operation.method() == Http.Method.PUT && common.id().isEmpty()) {
                return failure(ErrorCode._400, "SCIM Bulk PUT data requires id");
            }
        }
        if (data instanceof BulkRequest.PatchData patchData) {
            if (!settings.serviceProviderConfig().patch().supported()) {
                return failure(ErrorCode._400, "SCIM PATCH is disabled");
            }
            if (!settings.serviceProviderConfig().changePassword().supported() && patchData.patch().operations()
                    .stream().anyMatch(item -> item.value().getOrNull() instanceof PatchOperation.SecretData)) {
                return failure(ErrorCode._400, "SCIM password change is disabled");
            }
        }
        return null;
    }

    /**
     * Validates that a successful store response corresponds to a processed request prefix.
     *
     * @param outcome  external store outcome
     * @param thrown   asynchronous store failure when present
     * @param requests original ordered operations
     * @return validated Bulk outcome
     */
    private Outcome<BulkResponse> validate(
            final Outcome<BulkResponse> outcome,
            final Throwable thrown,
            final List<BulkRequest.Operation> requests) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(storeFailure());
        }
        if (outcome instanceof Outcome.Succeeded<BulkResponse> succeeded) {
            final BulkResponse response = succeeded.value();
            if (response == null || response.operations().size() > requests.size()) {
                return Outcome.failed(storeFailure());
            }
            for (int index = 0; index < response.operations().size(); index++) {
                final BulkRequest.Operation request = requests.get(index);
                final BulkResponse.Operation result = response.operations().get(index);
                if (request.method() != result.method() || !request.bulkId().equals(result.bulkId())) {
                    return Outcome.failed(storeFailure());
                }
            }
        }
        return outcome;
    }

}
