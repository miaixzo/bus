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

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.scim.*;
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.lang.Optional;

/**
 * Defines the external project port for SCIM resource persistence, atomic PATCH, ordered Bulk, and discovery catalogs.
 * <p>
 * Implementations belong to the integrating project. A single PATCH must be atomic. Bulk operations execute in input
 * order and stop after the requested {@code failOnErrors} count without rolling back already completed operations.
 * </p>
 *
 * @author Kimi Liu
 */
public interface ScimResourceStore {

    /**
     * Persists one newly created User or Group.
     *
     * @param providerId compiled server-role Source identifier
     * @param resource   inbound resource without service-provider attributes
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing the service-provider representation or a closed failure
     */
    CompletionStage<Outcome<Resource>> create(String providerId, Resource resource, Context context, Timeout timeout);

    /**
     * Retrieves one User or Group.
     *
     * @param providerId compiled server-role Source identifier
     * @param target     typed target resource reference
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing the current resource or a closed failure
     */
    CompletionStage<Outcome<Resource>> retrieve(
            String providerId,
            ResourceTarget target,
            Context context,
            Timeout timeout);

    /**
     * Atomically replaces one User or Group.
     *
     * @param providerId compiled server-role Source identifier
     * @param target     typed target resource reference
     * @param resource   complete replacement resource with id and conditional version
     * @param ifMatch    optional HTTP entity tag precondition
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing the replacement representation or a closed failure
     */
    CompletionStage<Outcome<Resource>> replace(
            String providerId,
            ResourceTarget target,
            Resource resource,
            Optional<String> ifMatch,
            Context context,
            Timeout timeout);

    /**
     * Atomically applies one ordered RFC 7644 PatchOp.
     *
     * @param providerId compiled server-role Source identifier
     * @param request    target and ordered patch operations
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing the patched representation or a closed failure
     */
    CompletionStage<Outcome<Resource>> patch(String providerId, PatchRequest request, Context context, Timeout timeout);

    /**
     * Deletes one User or Group using its conditional version.
     *
     * @param providerId compiled server-role Source identifier
     * @param target     typed target resource reference
     * @param ifMatch    optional HTTP entity tag precondition
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing empty success or a closed failure
     */
    CompletionStage<Outcome<Void>> delete(
            String providerId,
            ResourceTarget target,
            Optional<String> ifMatch,
            Context context,
            Timeout timeout);

    /**
     * Searches User and/or Group resources.
     *
     * @param providerId compiled server-role Source identifier
     * @param request    normalized standard search request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing a standard ListResponse or a closed failure
     */
    CompletionStage<Outcome<Result<Resource>>> search(
            String providerId,
            SearchQuery request,
            Context context,
            Timeout timeout);

    /**
     * Searches resources through the standard POST {@code /.search} representation.
     *
     * @param providerId compiled server-role Source identifier
     * @param request    standard POST search request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing Bus pagination or a closed failure
     */
    CompletionStage<Outcome<Result<Resource>>> search(
            String providerId,
            SearchRequest request,
            Context context,
            Timeout timeout);

    /**
     * Executes ordered Bulk operations with standard fail-on-errors stopping behavior.
     *
     * @param providerId compiled server-role Source identifier
     * @param request    typed standard Bulk request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing an ordered BulkResponse or a closed failure
     */
    CompletionStage<Outcome<BulkResponse>> bulk(
            String providerId,
            BulkRequest request,
            Context context,
            Timeout timeout);

    /**
     * Loads ResourceType discovery resources for the current Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing a ResourceType-only ListResponse or a closed failure
     */
    CompletionStage<Outcome<ListResponse>> resourceTypes(String providerId, Context context, Timeout timeout);

    /**
     * Loads Schema discovery resources for the current Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return stage containing a Schema-only ListResponse or a closed failure
     */
    CompletionStage<Outcome<ListResponse>> schemas(String providerId, Context context, Timeout timeout);

}
