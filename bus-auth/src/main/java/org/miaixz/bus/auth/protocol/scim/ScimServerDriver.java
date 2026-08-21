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
package org.miaixz.bus.auth.protocol.scim;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.scim.server.*;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceDriver;
import org.miaixz.bus.auth.worker.BindingLoader;
import org.miaixz.bus.auth.worker.SourceWorker;
import org.miaixz.bus.auth.worker.WorkerSlots;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Compiles one server-role SCIM 2.0 Source registration into typed User, Group, Bulk, and discovery operations.
 * <p>
 * Resource storage and discovery catalogs are resolved as one exact external {@link ScimResourceStore} binding for the
 * registration. The driver performs no data loading, persistence, HTTP routing, Controller behavior, or wire encoding
 * and retains no raw options object.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ScimServerDriver implements SourceDriver<ScimServerOptions> {

    /**
     * Creates a stateless SCIM Provider driver.
     */
    public ScimServerDriver() {
        // No initialization required.
    }

    /**
     * Builds the exact ordered capability set enabled by one SCIM Provider options value.
     *
     * @param options validated Provider options
     * @return immutable endpoint-accurate manifest
     */
    private static Capability.Manifest manifest(final ScimServerOptions options) {
        final List<Capability<?, ?>> capabilities = new ArrayList<>();
        capabilities.add(ScimServerScheme.CREATE);
        capabilities.add(ScimServerScheme.RETRIEVE);
        capabilities.add(ScimServerScheme.REPLACE);
        if (options.serviceProviderConfig().patch().supported()) {
            capabilities.add(ScimServerScheme.PATCH);
        }
        capabilities.add(ScimServerScheme.DELETE);
        capabilities.add(ScimServerScheme.SEARCH_GET);
        capabilities.add(ScimServerScheme.SEARCH_POST);
        capabilities.add(ScimServerScheme.BULK);
        capabilities.add(ScimServerScheme.SERVICE_PROVIDER_CONFIG);
        capabilities.add(ScimServerScheme.RESOURCE_TYPES);
        capabilities.add(ScimServerScheme.SCHEMAS);
        return new Capability.Manifest(capabilities);
    }

    /**
     * Returns the SCIM server scheme bound to this driver.
     *
     * @return immutable SCIM Provider scheme
     */
    @Override
    public ScimServerScheme scheme() {
        return new ScimServerScheme();
    }

    @Override
    public ScimServerOptions require(final Options<?> options) {
        if (options instanceof ScimServerOptions value) {
            return value;
        }
        throw new ValidateException("SCIM server driver requires ScimServerOptions");
    }

    @Override
    public WorkerSlots slots(final Source source, final ScimServerOptions options) {
        return WorkerSlots.of(WorkerSlots.Slot.BINDING);
    }

    @Override
    public Dependencies dependencies(final Source source, final ScimServerOptions options) {
        return Dependencies.of(Dependencies.Service.SECURITY_BASELINE);
    }

    /**
     * Consumes typed options, resolves the external store, and assembles an immutable SCIM runtime.
     *
     * @param prepared one-time validated Source graph, Options and dependency declaration
     * @param services dependency-scoped runtime services
     * @return executable immutable SCIM Source worker
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if routing, options, baseline, or external binding validation fails
     */
    @Override
    public SourceWorker compile(final Prepared<ScimServerOptions> prepared, final DriverServices services) {
        Assert.notNull(prepared, "SCIM Provider preparation must not be null");
        Assert.notNull(services, "SCIM Provider execution services must not be null");
        final Registration.SourceEntry record = prepared.registration();
        final Provider provider = prepared.provider();
        final Library library = prepared.library();
        final Source source = record.resource();
        if (!scheme().id().equals(source.getType()) || !supports(source.getProtocol())
                || !provider.getId().equals(source.getProvider_id())
                || !library.getId().equals(provider.getLibrary_id())) {
            throw new ValidateException("SCIM server driver requires a matching Source registration");
        }
        final ScimServerOptions options = prepared.options();
        if (options.maximumRequestBytes() > services.securityBaseline().require(Protocol.SCIM).maximumMessageBytes()) {
            throw new ValidateException("SCIM Provider request limit exceeds the shared security baseline");
        }
        final BindingLoader.Key<ScimResourceStore> binding = new BindingLoader.Key<>("scim-resource",
                ScimResourceStore.class);
        final ScimResourceStore store = Assert.notNull(
                binding.require(services.bindingLoader().load(record, binding)),
                "SCIM external resource store binding must not be null");
        final ScimResourceService resources = new ScimResourceService(source.getId(), options, store);
        return new CompiledServer(manifest(options), options.resourceTypes(), resources, new ScimUserService(resources),
                new ScimGroupService(resources), new ScimBulkService(source.getId(), options, store),
                new ScimDiscoveryService(source.getId(), options, store));
    }

    /**
     * Routes exact SCIM capabilities to the compiled resource-type, Bulk, and discovery services.
     *
     * @author Kimi Liu
     */
    private static final class CompiledServer implements SourceWorker {

        /**
         * Exact options-narrowed capability manifest.
         */
        private final Capability.Manifest manifest;

        /**
         * Registered resource types indexed by their standard names.
         */
        private final Map<String, ResourceType> resourceTypes;

        /**
         * Generic resource service used for an unscoped system-root search.
         */
        private final ScimResourceService resources;

        /**
         * Typed standard User resource service.
         */
        private final ScimUserService users;

        /**
         * Typed standard Group resource service.
         */
        private final ScimGroupService groups;

        /**
         * Ordered standard Bulk service.
         */
        private final ScimBulkService bulk;

        /**
         * Standard ServiceProviderConfig, ResourceTypes, and Schemas service.
         */
        private final ScimDiscoveryService discovery;

        /**
         * Creates one immutable compiled SCIM Provider.
         *
         * @param manifest      exact enabled capability manifest
         * @param resourceTypes registered resource type definitions
         * @param resources     generic resource service
         * @param users         typed User service
         * @param groups        typed Group service
         * @param bulk          ordered Bulk service
         * @param discovery     discovery resource service
         */
        private CompiledServer(final Capability.Manifest manifest, final List<ResourceType> resourceTypes,
                final ScimResourceService resources, final ScimUserService users, final ScimGroupService groups,
                final ScimBulkService bulk, final ScimDiscoveryService discovery) {
            this.manifest = Assert.notNull(manifest, "SCIM Provider manifest must not be null");
            Assert.notNull(resourceTypes, "SCIM Provider resource types must not be null");
            final Map<String, ResourceType> indexed = new LinkedHashMap<>();
            for (ResourceType resourceType : resourceTypes) {
                final ResourceType value = Assert.notNull(resourceType, "SCIM Provider resource type must not be null");
                indexed.put(value.name(), value);
            }
            this.resourceTypes = Map.copyOf(indexed);
            this.resources = Assert.notNull(resources, "SCIM resource service must not be null");
            this.users = Assert.notNull(users, "SCIM User service must not be null");
            this.groups = Assert.notNull(groups, "SCIM Group service must not be null");
            this.bulk = Assert.notNull(bulk, "SCIM Bulk service must not be null");
            this.discovery = Assert.notNull(discovery, "SCIM discovery service must not be null");
        }

        /**
         * Tests exact capability request and response class declarations.
         *
         * @param capability   candidate capability
         * @param requestType  required request class
         * @param responseType required response class
         * @return whether both declared classes match by identity
         */
        private static boolean types(
                final Capability<?, ?> capability,
                final Class<?> requestType,
                final Class<?> responseType) {
            return capability.requestType() == requestType && capability.responseType() == responseType;
        }

        /**
         * Narrows a delegated outcome through the capability's exact response class.
         *
         * @param stage        delegated outcome stage
         * @param responseType exact declared response class
         * @param <S>          expected success type
         * @return type-safe delegated outcome
         */
        private static <S> CompletionStage<Outcome<S>> narrow(
                final CompletionStage<? extends Outcome<?>> stage,
                final Class<S> responseType) {
            return stage.thenApply(outcome -> switch (outcome) {
                case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
                case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            });
        }

        /**
         * Returns a safe rejection for an unavailable or disabled capability.
         *
         * @param <S> expected success type
         * @return completed not-found outcome
         */
        private static <S> CompletionStage<Outcome<S>> missing() {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._404, "SCIM Provider capability is not available",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Returns a safe rejection for an incompatible request or declared response class.
         *
         * @param <S> expected success type
         * @return completed bad-request outcome
         */
        private static <S> CompletionStage<Outcome<S>> mismatch() {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "SCIM Provider capability type does not match the request",
                                    new JsonValue.ObjectValue(Map.of()))));
        }

        /**
         * Creates a type-inferred completed outcome stage.
         *
         * @param outcome completed outcome
         * @param <T>     success type
         * @return completed stage
         */
        private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
            return CompletableFuture.completedFuture(outcome);
        }

        /**
         * Returns the conditional entity tag stored in resource metadata.
         *
         * @param common common resource attributes
         * @return optional exact entity tag
         */
        private static Optional<String> version(final Resource.Common common) {
            final Resource.Meta meta = common.meta().getOrNull();
            return meta == null ? Optional.empty() : meta.version();
        }

        /**
         * Returns the exact options-backed SCIM capability manifest.
         *
         * @return immutable capability manifest
         */
        @Override
        public Capability.Manifest manifest() {
            return manifest;
        }

        /**
         * Invokes one exact declared SCIM server-role Source capability using only standard protocol models.
         *
         * @param capability exact declared capability object
         * @param request    exact standard request or {@code null} for discovery
         * @param context    immutable invocation context
         * @param timeout    shared end-to-end budget
         * @param <Q>        request type
         * @param <S>        success type
         * @return delegated typed outcome or a closed 400/404 rejection
         */
        @Override
        public <Q, S> CompletionStage<Outcome<S>> invoke(
                final Capability<Q, S> capability,
                final Q request,
                final Context context,
                final Timeout.Budget timeout) {
            Assert.notNull(capability, "SCIM Provider capability must not be null");
            Assert.notNull(context, "SCIM Provider context must not be null");
            Assert.notNull(timeout, "SCIM Provider time budget must not be null");
            if (!manifest.capabilities().contains(capability)) {
                return missing();
            }
            if (capability == ScimServerScheme.CREATE) {
                if (!types(capability, Resource.class, Resource.class) || !(request instanceof Resource resource)) {
                    return mismatch();
                }
                return resource(resource, Operation.CREATE, context, timeout, capability.responseType());
            }
            if (capability == ScimServerScheme.RETRIEVE) {
                if (!types(capability, Resource.Reference.class, Resource.class)
                        || !(request instanceof Resource.Reference reference)) {
                    return mismatch();
                }
                return reference(reference, Operation.RETRIEVE, context, timeout, capability.responseType());
            }
            if (capability == ScimServerScheme.REPLACE) {
                if (!types(capability, Resource.class, Resource.class) || !(request instanceof Resource resource)) {
                    return mismatch();
                }
                return resource(resource, Operation.REPLACE, context, timeout, capability.responseType());
            }
            if (capability == ScimServerScheme.PATCH) {
                if (!types(capability, PatchRequest.class, Resource.class)
                        || !(request instanceof PatchRequest patch)) {
                    return mismatch();
                }
                final CompletionStage<? extends Outcome<?>> stage = Scim.USER_SCHEMA
                        .equals(patch.target().resourceType().schema())
                                ? users.patch(patch, context, timeout)
                                : Scim.GROUP_SCHEMA.equals(patch.target().resourceType().schema())
                                        ? groups.patch(patch, context, timeout)
                                        : null;
                return stage == null ? mismatch() : narrow(stage, capability.responseType());
            }
            if (capability == ScimServerScheme.DELETE) {
                if (!types(capability, Resource.Reference.class, Void.class)
                        || !(request instanceof Resource.Reference reference)) {
                    return mismatch();
                }
                return delete(reference, context, timeout, capability.responseType());
            }
            if (capability == ScimServerScheme.SEARCH_GET) {
                if (!types(capability, SearchQuery.class, ListResponse.class)
                        || !(request instanceof SearchQuery search)) {
                    return mismatch();
                }
                final CompletionStage<? extends Outcome<?>> stage = Scim.USER_SCHEMA
                        .equals(search.target().resourceType().schema())
                                ? users.search(search, context, timeout)
                                : Scim.GROUP_SCHEMA.equals(search.target().resourceType().schema())
                                        ? groups.search(search, context, timeout)
                                        : null;
                return stage == null ? mismatch() : narrow(stage, capability.responseType());
            }
            if (capability == ScimServerScheme.SEARCH_POST) {
                if (!types(capability, SearchRequest.class, ListResponse.class)
                        || !(request instanceof SearchRequest search)) {
                    return mismatch();
                }
                return narrow(resources.search(search, context, timeout), capability.responseType());
            }
            if (capability == ScimServerScheme.BULK) {
                if (!types(capability, BulkRequest.class, BulkResponse.class)
                        || !(request instanceof BulkRequest bulkRequest)) {
                    return mismatch();
                }
                return narrow(bulk.bulk(bulkRequest, context, timeout), capability.responseType());
            }
            if (capability == ScimServerScheme.SERVICE_PROVIDER_CONFIG) {
                if (!types(capability, Void.class, ServiceProviderConfig.class) || request != null) {
                    return mismatch();
                }
                return narrow(discovery.serviceProviderConfig(context, timeout), capability.responseType());
            }
            if (capability == ScimServerScheme.RESOURCE_TYPES) {
                if (!types(capability, Void.class, ListResponse.class) || request != null) {
                    return mismatch();
                }
                return narrow(discovery.resourceTypes(context, timeout), capability.responseType());
            }
            if (capability == ScimServerScheme.SCHEMAS) {
                if (!types(capability, Void.class, ListResponse.class) || request != null) {
                    return mismatch();
                }
                return narrow(discovery.schemas(context, timeout), capability.responseType());
            }
            return missing();
        }

        /**
         * Routes one User or Group create/replace request to its typed service.
         *
         * @param request      standard writable resource
         * @param operation    selected create or replace operation
         * @param context      immutable invocation context
         * @param timeout      shared end-to-end budget
         * @param responseType declared response class
         * @param <S>          expected success type
         * @return delegated typed outcome
         */
        private <S> CompletionStage<Outcome<S>> resource(
                final Resource request,
                final Operation operation,
                final Context context,
                final Timeout.Budget timeout,
                final Class<S> responseType) {
            final CompletionStage<? extends Outcome<?>> stage = switch (request) {
                case User user -> operation == Operation.CREATE ? users.create(user, context, timeout)
                        : replace(user, context, timeout);
                case Group group -> operation == Operation.CREATE ? groups.create(group, context, timeout)
                        : replace(group, context, timeout);
                default -> null;
            };
            return stage == null ? mismatch() : narrow(stage, responseType);
        }

        /**
         * Replaces one User using its body identity and conditional metadata.
         *
         * @param user    replacement User
         * @param context immutable invocation context
         * @param timeout shared operation budget
         * @return delegated replacement outcome, or {@code null} when identity is absent
         */
        private CompletionStage<Outcome<User>> replace(
                final User user,
                final Context context,
                final Timeout.Budget timeout) {
            final ResourceTarget target = target(Scim.ResourceTypes.USER, user.common().id().getOrNull());
            return target == null ? null : users.replace(target, user, version(user.common()), context, timeout);
        }

        /**
         * Replaces one Group using its body identity and conditional metadata.
         *
         * @param group   replacement Group
         * @param context immutable invocation context
         * @param timeout shared operation budget
         * @return delegated replacement outcome, or {@code null} when identity is absent
         */
        private CompletionStage<Outcome<Group>> replace(
                final Group group,
                final Context context,
                final Timeout.Budget timeout) {
            final ResourceTarget target = target(Scim.ResourceTypes.GROUP, group.common().id().getOrNull());
            return target == null ? null : groups.replace(target, group, version(group.common()), context, timeout);
        }

        /**
         * Routes one User or Group retrieve reference to its typed service.
         *
         * @param request      standard target reference
         * @param operation    selected retrieve operation
         * @param context      immutable invocation context
         * @param timeout      shared end-to-end budget
         * @param responseType declared response class
         * @param <S>          expected success type
         * @return delegated typed outcome
         */
        private <S> CompletionStage<Outcome<S>> reference(
                final Resource.Reference request,
                final Operation operation,
                final Context context,
                final Timeout.Budget timeout,
                final Class<S> responseType) {
            if (operation != Operation.RETRIEVE) {
                return mismatch();
            }
            final ResourceTarget target = target(request.resourceType(), request.id());
            final CompletionStage<? extends Outcome<?>> stage = target == null ? null
                    : Scim.USER_SCHEMA.equals(target.resourceType().schema()) ? users.retrieve(target, context, timeout)
                            : Scim.GROUP_SCHEMA.equals(target.resourceType().schema())
                                    ? groups.retrieve(target, context, timeout)
                                    : null;
            return stage == null ? mismatch() : narrow(stage, responseType);
        }

        /**
         * Routes one User or Group delete reference to its typed service.
         *
         * @param request      standard target reference
         * @param context      immutable invocation context
         * @param timeout      shared end-to-end budget
         * @param responseType declared Void response class
         * @param <S>          expected success type
         * @return delegated typed outcome
         */
        private <S> CompletionStage<Outcome<S>> delete(
                final Resource.Reference request,
                final Context context,
                final Timeout.Budget timeout,
                final Class<S> responseType) {
            final ResourceTarget target = target(request.resourceType(), request.id());
            final CompletionStage<? extends Outcome<?>> stage = target == null ? null
                    : Scim.USER_SCHEMA.equals(target.resourceType().schema())
                            ? users.delete(target, request.version(), context, timeout)
                            : Scim.GROUP_SCHEMA.equals(target.resourceType().schema())
                                    ? groups.delete(target, request.version(), context, timeout)
                                    : null;
            return stage == null ? mismatch() : narrow(stage, responseType);
        }

        /**
         * Creates a typed resource target from one registered type name and resource identifier.
         *
         * @param name registered ResourceType name
         * @param id   service-provider resource identifier
         * @return typed target, or {@code null} when the type or identifier is absent
         */
        private ResourceTarget target(final String name, final String id) {
            final ResourceType resourceType = resourceTypes.get(name);
            return resourceType == null || id == null ? null : new ResourceTarget(resourceType, Optional.of(id));
        }

        /**
         * Enumerates the two resource operations sharing the typed resource router.
         *
         * @author Kimi Liu
         */
        private enum Operation {
            /**
             * Creates a new standard resource.
             */
            CREATE,
            /**
             * Retrieves one existing standard resource.
             */
            RETRIEVE,
            /**
             * Replaces one existing standard resource.
             */
            REPLACE

        }

    }

}
