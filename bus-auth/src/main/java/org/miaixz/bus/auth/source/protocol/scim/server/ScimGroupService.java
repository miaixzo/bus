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
package org.miaixz.bus.auth.source.protocol.scim.server;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.scim.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Narrows the generic SCIM resource service to the standard Group resource endpoint.
 *
 * @author Kimi Liu
 */
public class ScimGroupService {

    /**
     * Generic resource operation delegate.
     */
    private final ScimResourceService delegate;

    /**
     * Creates a Group endpoint service over one generic resource service.
     *
     * @param delegate generic resource operation delegate
     * @throws IllegalArgumentException if {@code delegate} is {@code null}
     */
    public ScimGroupService(final ScimResourceService delegate) {
        this.delegate = Assert.notNull(delegate, "SCIM Group service delegate must not be null");
    }

    /**
     * Tests whether a reference targets the Group resource type.
     *
     * @param request candidate resource reference
     * @return whether the reference is a non-null Group reference
     */
    private static boolean target(final ResourceTarget request) {
        return request != null && Scim.GROUP_SCHEMA.equals(request.resourceType().schema());
    }

    /**
     * Narrows successful generic resource values to Group while preserving closed failures.
     *
     * @param stage generic resource outcome stage
     * @return Group-typed outcome stage
     */
    private static CompletionStage<Outcome<Group>> group(final CompletionStage<Outcome<Resource>> stage) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<Resource> succeeded -> succeeded.value() instanceof Group value
                    ? Outcome.succeeded(value)
                    : Outcome.failed(failure(ErrorCode._500, "SCIM Group store returned another resource type"));
            case Outcome.Rejected<Resource> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Resource> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Creates a completed type-mismatch rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected outcome
     */
    private static <T> CompletionStage<Outcome<T>> rejected() {
        return CompletableFuture.completedFuture(
                Outcome.rejected(failure(ErrorCode._400, "SCIM Group operation targets another resource type")));
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
     * Creates one standard Group resource.
     *
     * @param request inbound Group resource
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the created Group or a closed framework failure
     */
    public CompletionStage<Outcome<Group>> create(final Group request, final Context context, final Timeout timeout) {
        Assert.notNull(request, "SCIM Group create request must not be null");
        return group(delegate.create(request, context, timeout));
    }

    /**
     * Retrieves one standard Group resource.
     *
     * @param request registered Group resource target
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the current Group or a closed framework failure
     */
    public CompletionStage<Outcome<Group>> retrieve(
            final ResourceTarget request,
            final Context context,
            final Timeout timeout) {
        if (!target(request) || request.resourceId().isEmpty()) {
            return rejected();
        }
        return group(delegate.retrieve(request, context, timeout));
    }

    /**
     * Replaces one standard Group resource.
     *
     * @param target  registered individual Group target
     * @param request complete replacement Group body
     * @param ifMatch optional If-Match entity-tag
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the replaced Group or a closed framework failure
     */
    public CompletionStage<Outcome<Group>> replace(
            final ResourceTarget target,
            final Group request,
            final Optional<String> ifMatch,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SCIM Group replace request must not be null");
        if (!target(target) || target.resourceId().isEmpty()) {
            return rejected();
        }
        return group(delegate.replace(target, request, ifMatch, context, timeout));
    }

    /**
     * Applies one atomic PatchOp to a Group resource.
     *
     * @param request Group-targeted standard PatchOp
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the patched Group or a closed framework failure
     */
    public CompletionStage<Outcome<Group>> patch(
            final PatchRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SCIM Group PatchOp must not be null");
        if (!target(request.target())) {
            return rejected();
        }
        return group(delegate.patch(request, context, timeout));
    }

    /**
     * Deletes one standard Group resource.
     *
     * @param target  registered individual Group target
     * @param ifMatch optional If-Match entity-tag
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing empty success or a closed framework failure
     */
    public CompletionStage<Outcome<Void>> delete(
            final ResourceTarget target,
            final Optional<String> ifMatch,
            final Context context,
            final Timeout timeout) {
        if (!target(target) || target.resourceId().isEmpty()) {
            return rejected();
        }
        return delegate.delete(target, ifMatch, context, timeout);
    }

    /**
     * Searches the standard Group resource endpoint.
     *
     * @param request GET search query targeting the Group collection
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the standard ListResponse or a closed framework failure
     */
    public CompletionStage<Outcome<ListResponse>> search(
            final SearchQuery request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SCIM Group search request must not be null");
        if (!target(request.target())) {
            return rejected();
        }
        return delegate.search(request, context, timeout);
    }

    /**
     * Searches the Group collection using a POST SearchRequest body.
     *
     * @param target  typed Group collection target
     * @param request standard POST SearchRequest body
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing the standard ListResponse or a closed framework failure
     */
    public CompletionStage<Outcome<ListResponse>> search(
            final ResourceTarget target,
            final SearchRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "SCIM Group SearchRequest must not be null");
        if (!target(target) || target.resourceId().isPresent()) {
            return rejected();
        }
        return delegate.search(target, request, context, timeout);
    }

}
