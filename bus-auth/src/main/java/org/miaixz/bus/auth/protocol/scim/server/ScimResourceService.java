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
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Orchestrates typed SCIM resource operations while keeping route, header, and body models distinct.
 *
 * @author Kimi Liu
 */
public class ScimResourceService {

    /**
     * Compiled server-role Source identifier used to isolate external data access.
     */
    private final String providerId;

    /**
     * Typed Provider options and discovery associations.
     */
    private final ScimServerOptions options;

    /**
     * External project implementation of resource persistence.
     */
    private final ScimResourceStore store;

    /**
     * Creates a resource service for one compiled SCIM Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param options    typed Provider options
     * @param store      externally supplied data port
     */
    public ScimResourceService(final String providerId, final ScimServerOptions options,
            final ScimResourceStore store) {
        this.providerId = Assert.notBlank(providerId, "SCIM Provider id must not be blank");
        this.options = Assert.notNull(options, "SCIM Provider options must not be null");
        this.store = Assert.notNull(store, "SCIM resource store must not be null");
    }

    /**
     * Returns the supported standard resource type name.
     *
     * @param resource resource candidate
     * @return User, Group, or {@code null}
     */
    private static String type(final Resource resource) {
        return resource instanceof User ? Scim.ResourceTypes.USER
                : resource instanceof Group ? Scim.ResourceTypes.GROUP : null;
    }

    /**
     * Returns common attributes from a supported resource.
     *
     * @param resource User or Group resource
     * @return common attributes
     */
    private static Resource.Common common(final Resource resource) {
        return resource instanceof User user ? user.common() : ((Group) resource).common();
    }

    /**
     * Requires non-null invocation arguments.
     *
     * @param request operation request
     * @param context invocation context
     * @param timeout shared operation timeout
     */
    private static void require(final Object request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "SCIM operation request must not be null");
        Assert.notNull(context, "SCIM operation context must not be null");
        Assert.notNull(timeout, "SCIM operation timeout must not be null");
    }

    /**
     * Creates a safe shared Bus failure.
     *
     * @param code        shared error code
     * @param description safe description
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a timeout failure.
     *
     * @param operation operation name
     * @return timeout failure
     */
    private static Outcome.Failure timeoutFailure(final String operation) {
        return failure(ErrorCode._408, "SCIM " + operation + " has no remaining timeout");
    }

    /**
     * Creates a store contract failure.
     *
     * @param operation operation name
     * @return store failure
     */
    private static Outcome.Failure storeFailure(final String operation) {
        return failure(ErrorCode._500, "SCIM " + operation + " resource store failed");
    }

    /**
     * Creates a completed asynchronous outcome.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates one User or Group from its standard request body.
     *
     * @param resource inbound resource body
     * @param context  invocation context
     * @param timeout  end-to-end timeout
     * @return created standard resource or framework failure
     */
    public CompletionStage<Outcome<Resource>> create(
            final Resource resource,
            final Context context,
            final Timeout timeout) {
        require(resource, context, timeout);
        final String type = type(resource);
        final Outcome.Failure invalid = writable(resource, type);
        if (invalid != null || !common(resource).id().isEmpty() || !common(resource).meta().isEmpty()) {
            return completed(
                    Outcome.rejected(
                            invalid != null ? invalid
                                    : failure(
                                            ErrorCode._400,
                                            "SCIM create body must omit service-provider id and meta")));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("create")));
        }
        try {
            return resourceResult(store.create(providerId, resource, context, timeout), type);
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("create")));
        }
    }

    /**
     * Retrieves one resource selected by a registered individual target.
     *
     * @param target  route-derived individual target
     * @param context invocation context
     * @param timeout end-to-end timeout
     * @return current resource or framework failure
     */
    public CompletionStage<Outcome<Resource>> retrieve(
            final ResourceTarget target,
            final Context context,
            final Timeout timeout) {
        require(target, context, timeout);
        final Outcome.Failure invalid = target(target, true);
        if (invalid != null) {
            return completed(Outcome.rejected(invalid));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("retrieve")));
        }
        try {
            return resourceResult(store.retrieve(providerId, target, context, timeout), target.resourceType().name());
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("retrieve")));
        }
    }

    /**
     * Replaces one resource while keeping target and If-Match outside the resource body.
     *
     * @param target   route-derived individual target
     * @param resource complete replacement body
     * @param ifMatch  optional If-Match entity-tag
     * @param context  invocation context
     * @param timeout  end-to-end timeout
     * @return replacement resource or framework failure
     */
    public CompletionStage<Outcome<Resource>> replace(
            final ResourceTarget target,
            final Resource resource,
            final Optional<String> ifMatch,
            final Context context,
            final Timeout timeout) {
        require(resource, context, timeout);
        final Outcome.Failure invalid = mutation(target, ifMatch);
        final String type = type(resource);
        final Outcome.Failure bodyFailure = writable(resource, type);
        if (invalid != null || bodyFailure != null || !target.resourceType().name().equals(type)
                || common(resource).id().isPresent()
                        && !common(resource).id().getOrThrow().equals(target.resourceId().getOrThrow())) {
            return completed(
                    Outcome.rejected(
                            invalid != null ? invalid
                                    : bodyFailure != null ? bodyFailure
                                            : failure(
                                                    ErrorCode._400,
                                                    "SCIM replacement body does not match its resource target")));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("replace")));
        }
        try {
            return resourceResult(store.replace(providerId, target, resource, ifMatch, context, timeout), type);
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("replace")));
        }
    }

    /**
     * Applies one atomic PatchOp with its separately modeled target and If-Match header.
     *
     * @param request typed patch request association
     * @param context invocation context
     * @param timeout end-to-end timeout
     * @return patched resource or framework failure
     */
    public CompletionStage<Outcome<Resource>> patch(
            final PatchRequest request,
            final Context context,
            final Timeout timeout) {
        require(request, context, timeout);
        if (!options.serviceProviderConfig().patch().supported()) {
            return completed(Outcome.rejected(failure(ErrorCode._400, "SCIM PATCH is disabled")));
        }
        final Outcome.Failure invalid = mutation(request.target(), request.ifMatch());
        if (invalid != null) {
            return completed(Outcome.rejected(invalid));
        }
        if (!options.serviceProviderConfig().changePassword().supported() && request.patch().operations().stream()
                .anyMatch(operation -> operation.value().getOrNull() instanceof PatchOperation.SecretData)) {
            return completed(Outcome.rejected(failure(ErrorCode._400, "SCIM password change is disabled")));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("patch")));
        }
        try {
            return resourceResult(
                    store.patch(providerId, request, context, timeout),
                    request.target().resourceType().name());
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("patch")));
        }
    }

    /**
     * Deletes one resource with a separately modeled If-Match header.
     *
     * @param target  route-derived individual target
     * @param ifMatch optional If-Match entity-tag
     * @param context invocation context
     * @param timeout end-to-end timeout
     * @return empty success or framework failure
     */
    public CompletionStage<Outcome<Void>> delete(
            final ResourceTarget target,
            final Optional<String> ifMatch,
            final Context context,
            final Timeout timeout) {
        require(target, context, timeout);
        final Outcome.Failure invalid = mutation(target, ifMatch);
        if (invalid != null) {
            return completed(Outcome.rejected(invalid));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("delete")));
        }
        try {
            final CompletionStage<Outcome<Void>> stage = store.delete(providerId, target, ifMatch, context, timeout);
            return stage == null ? completed(Outcome.failed(storeFailure("delete")))
                    : stage.handle(
                            (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                    : Outcome.failed(storeFailure("delete")));
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("delete")));
        }
    }

    /**
     * Searches one collection through the GET query representation.
     *
     * @param request typed collection query
     * @param context invocation context
     * @param timeout end-to-end timeout
     * @return standard ListResponse or framework failure
     */
    public CompletionStage<Outcome<ListResponse>> search(
            final SearchQuery request,
            final Context context,
            final Timeout timeout) {
        require(request, context, timeout);
        final Outcome.Failure invalid = target(request.target(), false);
        return invalid == null ? searchValidated(request, context, timeout) : completed(Outcome.rejected(invalid));
    }

    /**
     * Searches all enabled resource collections through POST {@code /.search}.
     *
     * @param request standard POST search body
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return standard ListResponse or framework failure
     */
    public CompletionStage<Outcome<ListResponse>> search(
            final SearchRequest request,
            final Context context,
            final Timeout timeout) {
        require(request, context, timeout);
        final SearchParameters parameters = request.parameters();
        final Outcome.Failure invalid = searchParameters(parameters);
        if (invalid != null) {
            return completed(Outcome.rejected(invalid));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("search")));
        }
        try {
            final CompletionStage<Outcome<Result<Resource>>> stage = store
                    .search(providerId, request, context, timeout);
            return stage == null ? completed(Outcome.failed(storeFailure("search")))
                    : stage.handle((outcome, thrown) -> listOutcome(outcome, thrown, null, parameters));
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("search")));
        }
    }

    /**
     * Searches one route-selected collection through a POST SearchRequest body.
     *
     * @param target  route-derived collection target
     * @param request standard SearchRequest body
     * @param context invocation context
     * @param timeout end-to-end timeout
     * @return standard ListResponse or framework failure
     */
    public CompletionStage<Outcome<ListResponse>> search(
            final ResourceTarget target,
            final SearchRequest request,
            final Context context,
            final Timeout timeout) {
        require(request, context, timeout);
        final Outcome.Failure invalid = target(target, false);
        return invalid == null ? searchValidated(new SearchQuery(target, request.parameters()), context, timeout)
                : completed(Outcome.rejected(invalid));
    }

    /**
     * Executes one validated search through the Bus pagination contract and maps it to ListResponse.
     *
     * @param request validated collection target and common search parameters
     * @param context invocation context
     * @param timeout end-to-end timeout
     * @return standard list response stage
     */
    private CompletionStage<Outcome<ListResponse>> searchValidated(
            final SearchQuery request,
            final Context context,
            final Timeout timeout) {
        final ResourceTarget target = request.target();
        final SearchParameters parameters = request.parameters();
        final Outcome.Failure invalid = searchParameters(parameters);
        if (invalid != null) {
            return completed(Outcome.rejected(invalid));
        }
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("search")));
        }
        try {
            final CompletionStage<Outcome<Result<Resource>>> stage = store
                    .search(providerId, request, context, timeout);
            if (stage == null) {
                return completed(Outcome.failed(storeFailure("search")));
            }
            return stage.handle((outcome, thrown) -> listOutcome(outcome, thrown, target, parameters));
        } catch (RuntimeException failure) {
            return completed(Outcome.failed(storeFailure("search")));
        }
    }

    /**
     * Validates shared standard search limits before invoking the external store.
     *
     * @param parameters common GET or POST search parameters
     * @return validation failure, or {@code null} when valid
     */
    private Outcome.Failure searchParameters(final SearchParameters parameters) {
        if (!parameters.sortBy().isEmpty() && !options.serviceProviderConfig().sort().supported()) {
            return failure(ErrorCode._400, "SCIM sorting is disabled");
        }
        if (!parameters.count().isEmpty()
                && parameters.count().getOrThrow() > options.serviceProviderConfig().filter().maxResults()) {
            return failure(ErrorCode._400, "SCIM search count exceeds the advertised limit");
        }
        return null;
    }

    /**
     * Validates a registered collection or individual target.
     *
     * @param value      target candidate
     * @param individual whether a resource id is required
     * @return validation failure or {@code null}
     */
    private Outcome.Failure target(final ResourceTarget value, final boolean individual) {
        if (value == null || !options.resourceTypes().contains(value.resourceType())) {
            return failure(ErrorCode._404, "SCIM resource type is not enabled");
        }
        if (individual != value.resourceId().isPresent()) {
            return failure(
                    ErrorCode._400,
                    individual ? "SCIM operation requires an individual resource target"
                            : "SCIM search requires a collection target");
        }
        return null;
    }

    /**
     * Validates one mutation target and optional conditional header.
     *
     * @param target  individual target
     * @param ifMatch optional If-Match entity-tag
     * @return validation failure or {@code null}
     */
    private Outcome.Failure mutation(final ResourceTarget target, final Optional<String> ifMatch) {
        final Outcome.Failure invalid = target(target, true);
        if (invalid != null) {
            return invalid;
        }
        Assert.notNull(ifMatch, "SCIM If-Match container must not be null");
        if (!options.serviceProviderConfig().etag().supported() && !ifMatch.isEmpty()) {
            return failure(ErrorCode._412, "SCIM entity-tag preconditions are disabled");
        }
        return null;
    }

    /**
     * Validates one writable resource body.
     *
     * @param resource resource body
     * @param type     resolved type name
     * @return validation failure or {@code null}
     */
    private Outcome.Failure writable(final Resource resource, final String type) {
        if (type == null || !options.supports(type)) {
            return failure(ErrorCode._404, "SCIM resource type is not enabled");
        }
        if (resource instanceof User user && !user.password().isEmpty()
                && !options.serviceProviderConfig().changePassword().supported()) {
            return failure(ErrorCode._400, "SCIM password change is disabled");
        }
        return null;
    }

    /**
     * Validates an asynchronous resource result.
     *
     * @param stage        external store stage
     * @param expectedType expected resource type
     * @return validated outcome stage
     */
    private CompletionStage<Outcome<Resource>> resourceResult(
            final CompletionStage<Outcome<Resource>> stage,
            final String expectedType) {
        if (stage == null) {
            return completed(Outcome.failed(storeFailure("resource")));
        }
        return stage.handle((outcome, thrown) -> {
            if (thrown != null || outcome == null) {
                return Outcome.failed(storeFailure("resource"));
            }
            if (outcome instanceof Outcome.Succeeded<Resource> succeeded
                    && !validResult(succeeded.value(), expectedType)) {
                return Outcome.failed(storeFailure("resource"));
            }
            return outcome;
        });
    }

    /**
     * Converts the Bus pagination result to the RFC 7644 ListResponse model.
     *
     * @param outcome    store pagination outcome
     * @param thrown     asynchronous failure
     * @param target     requested collection target
     * @param parameters search parameters
     * @return standard list outcome
     */
    private Outcome<ListResponse> listOutcome(
            final Outcome<Result<Resource>> outcome,
            final Throwable thrown,
            final ResourceTarget target,
            final SearchParameters parameters) {
        if (thrown != null || outcome == null) {
            return Outcome.failed(storeFailure("search"));
        }
        return switch (outcome) {
            case Outcome.Succeeded<Result<Resource>> succeeded -> {
                final Result<Resource> result = succeeded.value();
                final List<Resource> rows = result == null || result.getRows() == null ? null
                        : List.copyOf(result.getRows());
                final String expectedType = target == null ? null : target.resourceType().name();
                if (rows == null || result.getTotal() < 0
                        || rows.stream().anyMatch(resource -> !validResult(resource, expectedType))) {
                    yield Outcome.failed(storeFailure("search"));
                }
                yield Outcome.succeeded(
                        new ListResponse(List.of(Scim.LIST_RESPONSE_SCHEMA), result.getTotal(), parameters.startIndex(),
                                Optional.of(rows.size()), rows));
            }
            case Outcome.Rejected<Result<Resource>> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Result<Resource>> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
    }

    /**
     * Tests one successful resource result against its protocol invariants.
     *
     * @param resource     returned resource
     * @param expectedType expected ResourceType name
     * @return whether the result is valid
     */
    private boolean validResult(final Resource resource, final String expectedType) {
        if (resource == null || expectedType != null && !expectedType.equals(type(resource))
                || expectedType == null && type(resource) == null) {
            return false;
        }
        final Resource.Common common = common(resource);
        if (common.id().isEmpty() || resource instanceof User user && !user.password().isEmpty()) {
            return false;
        }
        if (options.serviceProviderConfig().etag().supported()
                && (common.meta().isEmpty() || common.meta().getOrThrow().version().isEmpty())) {
            return false;
        }
        return common.meta().isEmpty() || expectedType.equals(common.meta().getOrThrow().resourceType());
    }

}
