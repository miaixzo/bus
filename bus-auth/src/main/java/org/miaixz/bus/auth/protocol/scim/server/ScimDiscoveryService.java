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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.protocol.scim.*;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Produces standard SCIM discovery resources from immutable settings and externally supplied schema catalogs.
 *
 * @author Kimi Liu
 */
public final class ScimDiscoveryService {

    /**
     * Typed standard authentication-scheme catalog in stable discovery order.
     */
    private static final List<ServiceProviderConfig.AuthenticationScheme> AUTHENTICATION_SCHEMES = List.of(
            scheme("oauth", "OAuth 1.0", "OAuth 1.0 request authentication", "rfc5849"),
            scheme("oauth2", "OAuth 2.0", "OAuth 2.0 client authentication", "rfc6749"),
            scheme("oauthbearertoken", "OAuth Bearer Token", "OAuth 2.0 bearer token authentication", "rfc6750"),
            scheme("httpbasic", "HTTP Basic", "HTTP Basic authentication", "rfc7617"),
            scheme("httpdigest", "HTTP Digest", "HTTP Digest authentication", "rfc7616"));

    /**
     * Compiled server-role Source identifier used to isolate catalog lookups.
     */
    private final String providerId;

    /**
     * Validated settings from which ServiceProviderConfig is generated.
     */
    private final ScimProviderSettings settings;

    /**
     * External project implementation of SCIM discovery catalogs.
     */
    private final ScimResourceStore store;

    /**
     * Creates a discovery service for one exact compiled SCIM Provider.
     *
     * @param providerId compiled server-role Source identifier
     * @param settings   validated SCIM Provider settings
     * @param store      externally supplied discovery catalog store
     * @throws IllegalArgumentException if an argument is {@code null} or the identifier is blank
     */
    public ScimDiscoveryService(final String providerId, final ScimProviderSettings settings,
            final ScimResourceStore store) {
        this.providerId = Assert.notBlank(providerId, "SCIM Provider id must not be blank");
        this.settings = Assert.notNull(settings, "SCIM Provider settings must not be null");
        this.store = Assert.notNull(store, "SCIM discovery store must not be null");
    }

    /**
     * Creates one standard authentication scheme citation.
     *
     * @param type        registered scheme type
     * @param name        standard human-readable name
     * @param description non-sensitive scheme description
     * @param rfc         lowercase RFC document identifier
     * @return immutable authentication scheme
     */
    private static ServiceProviderConfig.AuthenticationScheme scheme(
            final String type,
            final String name,
            final String description,
            final String rfc) {
        return new ServiceProviderConfig.AuthenticationScheme(type, name, description,
                Optional.of("https://www.rfc-editor.org/rfc/" + rfc), Optional.empty());
    }

    /**
     * Requires non-null discovery invocation arguments.
     *
     * @param context immutable invocation context
     * @param timeout shared time budget
     */
    private static void requireInvocation(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "SCIM discovery context must not be null");
        Assert.notNull(timeout, "SCIM discovery time budget must not be null");
    }

    /**
     * Creates a non-sensitive shared Bus failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic description
     * @return closed failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a discovery timeout failure.
     *
     * @param operation standard discovery operation name
     * @return safe timeout failure
     */
    private static Outcome.Failure timeoutFailure(final String operation) {
        return failure(ErrorCode._408, "SCIM " + operation + " has no remaining time budget");
    }

    /**
     * Creates a discovery store failure.
     *
     * @param operation standard discovery operation name
     * @return safe store failure
     */
    private static Outcome.Failure storeFailure(final String operation) {
        return failure(ErrorCode._500, "SCIM " + operation + " store result is invalid");
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
     * Generates the standard ServiceProviderConfig resource from validated Provider settings.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return completed stage containing ServiceProviderConfig or a timeout failure
     */
    public CompletionStage<Outcome<ServiceProviderConfig>> serviceProviderConfig(
            final Context context,
            final Timeout.Budget timeout) {
        requireInvocation(context, timeout);
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("ServiceProviderConfig")));
        }
        return completed(Outcome.succeeded(settings.serviceProviderConfig()));
    }

    /**
     * Loads the standard ResourceType ListResponse from the external catalog.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing validated ResourceType resources or a closed framework failure
     */
    public CompletionStage<Outcome<ListResponse>> resourceTypes(final Context context, final Timeout.Budget timeout) {
        requireInvocation(context, timeout);
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("ResourceTypes")));
        }
        try {
            final CompletionStage<Outcome<ListResponse>> stage = store.resourceTypes(providerId, context, timeout);
            return discovery(stage, ResourceType.class, true, "ResourceTypes");
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(storeFailure("ResourceTypes")));
        }
    }

    /**
     * Loads the standard Schema ListResponse from the external catalog.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return stage containing validated Schema resources or a closed framework failure
     */
    public CompletionStage<Outcome<ListResponse>> schemas(final Context context, final Timeout.Budget timeout) {
        requireInvocation(context, timeout);
        if (timeout.expired()) {
            return completed(Outcome.failed(timeoutFailure("Schemas")));
        }
        try {
            final CompletionStage<Outcome<ListResponse>> stage = store.schemas(providerId, context, timeout);
            return discovery(stage, Schema.class, false, "Schemas");
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(storeFailure("Schemas")));
        }
    }

    /**
     * Builds authentication scheme resources in stable registered-value order.
     *
     * @return immutable standard authentication scheme list
     */
    private List<ServiceProviderConfig.AuthenticationScheme> authenticationSchemes() {
        final List<ServiceProviderConfig.AuthenticationScheme> result = new ArrayList<>();
        result.addAll(settings.serviceProviderConfig().authenticationSchemes());
        return List.copyOf(result);
    }

    /**
     * Validates a discovery store stage and its homogeneous standard resource list.
     *
     * @param stage              external store stage
     * @param resourceClass      required standard resource class
     * @param exactResourceTypes whether enabled ResourceType names must match settings exactly
     * @param operation          discovery operation name
     * @return validated discovery stage
     */
    private CompletionStage<Outcome<ListResponse>> discovery(
            final CompletionStage<Outcome<ListResponse>> stage,
            final Class<? extends Resource> resourceClass,
            final boolean exactResourceTypes,
            final String operation) {
        if (stage == null) {
            return completed(Outcome.failed(storeFailure(operation)));
        }
        return stage.handle((outcome, thrown) -> {
            if (thrown != null || outcome == null) {
                return Outcome.failed(storeFailure(operation));
            }
            if (outcome instanceof Outcome.Succeeded<ListResponse> succeeded) {
                final ListResponse list = succeeded.value();
                if (list == null) {
                    return Outcome.failed(storeFailure(operation));
                }
                final Set<String> names = new HashSet<>();
                for (Resource resource : list.resources()) {
                    if (!resourceClass.isInstance(resource)) {
                        return Outcome.failed(storeFailure(operation));
                    }
                    if (resource instanceof ResourceType type && !names.add(type.name())) {
                        return Outcome.failed(storeFailure(operation));
                    }
                    if (resource instanceof Schema schema && !names.add(schema.id())) {
                        return Outcome.failed(storeFailure(operation));
                    }
                }
                if (exactResourceTypes
                        && (!settings.resourceTypes().stream().map(ResourceType::name).allMatch(names::contains)
                                || names.size() != settings.resourceTypes().size())) {
                    return Outcome.failed(storeFailure(operation));
                }
                if (!exactResourceTypes && !containsRequiredSchemas(names)) {
                    return Outcome.failed(storeFailure(operation));
                }
            }
            return outcome;
        });
    }

    /**
     * Tests whether the schema catalog includes every enabled core resource schema.
     *
     * @param schemaIds returned schema identifiers
     * @return whether all enabled core schemas are present
     */
    private boolean containsRequiredSchemas(final Set<String> schemaIds) {
        return (!settings.supports(Scim.ResourceTypes.USER) || schemaIds.contains(Scim.USER_SCHEMA))
                && (!settings.supports(Scim.ResourceTypes.GROUP) || schemaIds.contains(Scim.GROUP_SCHEMA));
    }

}
