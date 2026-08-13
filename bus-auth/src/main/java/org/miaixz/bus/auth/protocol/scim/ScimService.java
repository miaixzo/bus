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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Descriptor;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Outcome.Success;
import org.miaixz.bus.auth.codec.http.HttpValues;
import org.miaixz.bus.auth.protocol.scim.SCIM.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Implements both SCIM facades. Provider mode routes bounded HTTP-shaped DTOs and delegates persistence only through
 * the tenant-scoped repository; client mode delegates HTTPS exchange only through the runtime protocol transport.
 *
 * @author Kimi Liu
 */
public final class ScimService implements Provider, Client {

    /**
     * Immutable HTTPS descriptor declaring SCIM provisioning support.
     */
    private static final Descriptor DESCRIPTOR = new Descriptor("scim", "SCIM 2.0", Protocol.HTTPS,
            java.util.Set.of(Builder.CAPABILITY_PROVISION), Map.of(), Options.empty());

    /**
     * Provider configuration, absent in client mode.
     */
    private final ProviderConfig provider;

    /**
     * Client configuration, absent in provider mode.
     */
    private final ClientConfig client;

    /**
     * Product runtime.
     */
    private final org.miaixz.bus.fabric.Context fabric;

    /**
     * JSON provider used for standard SCIM documents.
     */
    private final JsonProvider json;

    /**
     * Maximum accepted or materialized SCIM bytes.
     */
    private final int maximumBytes;

    /**
     * Atomic repository, absent in client mode.
     */
    private final Repository repository;

    /**
     * Strict SCIM JSON codec.
     */
    private final ScimCodec codec;

    /**
     * Creates provider mode.
     *
     * @param configuration provider configuration
     * @param repository    repository
     * @param json          JSON provider
     * @param maximumBytes  maximum SCIM JSON bytes
     * @param maximumDepth  maximum SCIM JSON nesting depth
     * @throws ValidateException if a dependency or codec bound is invalid
     */
    public ScimService(final ProviderConfig configuration, final Repository repository, final JsonProvider json,
            final int maximumBytes, final int maximumDepth) {
        this.provider = Assert
                .notNull(configuration, () -> new ValidateException("SCIM provider configuration must not be null"));
        this.client = null;
        this.fabric = null;
        this.repository = Assert.notNull(repository, () -> new ValidateException("SCIM repository must not be null"));
        this.json = Assert.notNull(json, () -> new ValidateException("SCIM JSON provider must not be null"));
        this.maximumBytes = maximumBytes;
        this.codec = new ScimCodec(json, maximumBytes, maximumDepth);
    }

    /**
     * Creates client mode.
     *
     * @param configuration client configuration
     * @param fabric        Fabric context used for HTTPS exchanges
     * @param json          JSON provider
     * @param maximumBytes  maximum SCIM JSON bytes
     * @param maximumDepth  maximum SCIM JSON nesting depth
     * @throws ValidateException if a dependency or codec bound is invalid
     */
    public ScimService(final ClientConfig configuration, final org.miaixz.bus.fabric.Context fabric,
            final JsonProvider json, final int maximumBytes, final int maximumDepth) {
        this.provider = null;
        this.client = Assert
                .notNull(configuration, () -> new ValidateException("SCIM client configuration must not be null"));
        this.fabric = Assert.notNull(fabric, () -> new ValidateException("SCIM Fabric context must not be null"));
        this.repository = null;
        this.json = Assert.notNull(json, () -> new ValidateException("SCIM JSON provider must not be null"));
        this.maximumBytes = maximumBytes;
        this.codec = new ScimCodec(json, maximumBytes, maximumDepth);
    }

    /**
     * Creates optional Location headers.
     *
     * @param result mutation result
     * @return headers
     */
    static Map<String, List<String>> location(final MutationResult result) {
        return result.location() == null ? Map.of()
                : Map.of(Http.Header.LOCATION, List.of(result.location().toString()));
    }

    /**
     * Creates a SCIM single-header cardinality failure.
     */
    private static RuntimeException invalidHeader() {
        return new ValidateException("SCIM header must occur at most once");
    }

    /**
     * Creates a SCIM single-query cardinality failure.
     */
    private static RuntimeException invalidParameter() {
        return new ValidateException("SCIM query parameter must occur at most once");
    }

    /**
     * Parses one non-negative decimal query value.
     *
     * @param source       optional source
     * @param defaultValue default value
     * @return parsed value
     */
    static int number(final String source, final int defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        try {
            final int value = Integer.parseInt(source);
            Assert.isTrue(value >= Normal._0, () -> new ValidateException("SCIM numeric query value is negative"));
            return value;
        } catch (final NumberFormatException failure) {
            throw new ValidateException("SCIM numeric query value is invalid");
        }
    }

    /**
     * Parses one resource target.
     *
     * @param relative relative path
     * @return target or {@code null}
     */
    static Target target(final String relative) {
        for (final ResourceType type : ResourceType.values()) {
            if (relative.equals(type.path())) {
                return new Target(type, null);
            }
            if (relative.startsWith(type.path() + "/")) {
                final String identifier = relative.substring(type.path().length() + Normal._1);
                if (!identifier.isBlank() && identifier.indexOf('/') < Normal._0) {
                    return new Target(type, identifier);
                }
            }
        }
        return null;
    }

    /**
     * Tests whether a URI remains under an exact origin and root path.
     *
     * @param root service root
     * @param uri  request URI
     * @return whether contained
     */
    static boolean under(final URI root, final URI uri) {
        final int rootPort = root.getPort() < Normal._0 ? 443 : root.getPort();
        final int uriPort = uri.getPort() < Normal._0 ? 443 : uri.getPort();
        final String path = root.getPath().endsWith("/") ? root.getPath() : root.getPath() + "/";
        return root.getScheme().equalsIgnoreCase(uri.getScheme()) && root.getHost().equalsIgnoreCase(uri.getHost())
                && rootPort == uriPort && uri.getPath().startsWith(path);
    }

    /**
     * Returns the route path relative to the configured service root.
     *
     * @param root root URI
     * @param uri  request URI
     * @return relative path
     */
    static String relative(final URI root, final URI uri) {
        final String prefix = root.getPath().endsWith("/") ? root.getPath() : root.getPath() + "/";
        return uri.getPath().substring(prefix.length());
    }

    /**
     * Creates a completed response stage.
     *
     * @param response response
     * @return completed stage
     */
    static CompletionStage<Response> completed(final Response response) {
        return CompletableFuture.completedFuture(response);
    }

    /**
     * Validates and snapshots decoded PATCH operations without an unchecked cast.
     *
     * @param value decoded bulk entry data
     * @return immutable typed operation list
     * @throws ValidateException if data is not a non-null operation list
     */
    static List<ScimPatch.Operation> operations(final Object value) {
        if (!(value instanceof List<?> source)) {
            throw new ValidateException("SCIM bulk PATCH data must be an operation list");
        }
        final java.util.ArrayList<ScimPatch.Operation> result = new java.util.ArrayList<>(source.size());
        for (final Object operation : source) {
            if (!(operation instanceof ScimPatch.Operation current)) {
                throw new ValidateException("SCIM bulk PATCH data contains an invalid operation");
            }
            result.add(current);
        }
        return List.copyOf(result);
    }

    /**
     * Returns the immutable SCIM protocol handler descriptor.
     *
     * @return HTTPS descriptor declaring identity provisioning
     */
    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * Handles one bounded provider request.
     *
     * @param invocation operation context
     * @param request    request
     * @return outcome stage whose successful value is a standard SCIM response or error
     * @throws ValidateException if the service mode or either argument is invalid
     */
    @Override
    public CompletionStage<Outcome<Response>> handle(final Context invocation, final Request request) {
        Assert.notNull(invocation, () -> new ValidateException("SCIM invocation must not be null"));
        Assert.notNull(request, () -> new ValidateException("SCIM request must not be null"));
        Assert.notNull(provider, () -> new ValidateException("SCIM service is not in provider mode"));
        try {
            return route(invocation, request)
                    .handle((response, failure) -> new Success<>(failure == null ? response : failure(failure)));
        } catch (final RuntimeException failure) {
            return CompletableFuture.completedFuture(new Success<>(failure(failure)));
        }
    }

    /**
     * Executes one bounded remote request.
     *
     * @param invocation operation context
     * @param request    request
     * @return response stage
     * @throws ValidateException if the service mode, request, context, or destination is invalid
     */
    @Override
    public CompletionStage<Response> execute(final Context invocation, final Request request) {
        Assert.notNull(client, () -> new ValidateException("SCIM service is not in client mode"));
        Assert.notNull(invocation, () -> new ValidateException("SCIM invocation must not be null"));
        final Request outbound = Assert.notNull(request, () -> new ValidateException("SCIM request must not be null"));
        Assert.isTrue(
                under(client.serviceRoot(), outbound.uri()),
                () -> new ValidateException("SCIM request URI is outside the configured service root"));
        return CompletableFuture.supplyAsync(() -> {
            final HttpX.Builder exchange = HttpX.builder(fabric).url(outbound.uri().toASCIIString())
                    .method(outbound.method()).headers(outbound.headers()).body(outbound.body())
                    .addressPolicy(client.addressPolicy()).timeout(client.timeout());
            outbound.query().forEach((name, values) -> values.forEach(value -> exchange.query(name, value)));
            try (HttpResponse response = exchange.build().execute()) {
                return new Response(response.code(), response.headers().asMap(), response.bytes(maximumBytes));
            }
        });
    }

    /**
     * Routes one provider request.
     *
     * @param invocation operation context
     * @param request    request
     * @return response stage
     */
    CompletionStage<Response> route(final Context invocation, final Request request) {
        Assert.isTrue(
                under(provider.serviceRoot(), request.uri()),
                () -> new ValidateException("SCIM request URI is outside the service root"));
        final String relative = relative(provider.serviceRoot(), request.uri());
        if (request.method() == Http.Method.GET && "ServiceProviderConfig".equals(relative)) {
            return completed(configuration());
        }
        if (request.method() == Http.Method.GET && "ResourceTypes".equals(relative)) {
            return completed(resourceTypes());
        }
        if (request.method() == Http.Method.GET && "Schemas".equals(relative)) {
            return completed(schemas());
        }
        if (request.method() == Http.Method.POST && "Bulk".equals(relative)) {
            return bulk(invocation, request);
        }
        final Target target = target(relative);
        if (target == null) {
            return completed(error(Http.Status.NOT_FOUND, ProtocolError.INVALID_PATH));
        }
        if (request.method() == Http.Method.GET && target.identifier() == null) {
            return search(invocation, request, target.type());
        }
        if (request.method() == Http.Method.GET) {
            return read(invocation, request, target);
        }
        if (request.method() == Http.Method.POST && target.identifier() == null) {
            return mutate(
                    invocation,
                    request,
                    new Mutation(Http.Method.POST, target.type(), null, codec.resource(request.body(), target.type()),
                            null, null));
        }
        if (request.method() == Http.Method.PUT && target.identifier() != null) {
            return mutate(
                    invocation,
                    request,
                    new Mutation(Http.Method.PUT, target.type(), target.identifier(),
                            codec.resource(request.body(), target.type()),
                            HttpValues.header(request.headers(), Http.Header.IF_MATCH, ScimService::invalidHeader),
                            null));
        }
        if (request.method() == Http.Method.DELETE && target.identifier() != null) {
            return mutate(
                    invocation,
                    request,
                    new Mutation(Http.Method.DELETE, target.type(), target.identifier(), null,
                            HttpValues.header(request.headers(), Http.Header.IF_MATCH, ScimService::invalidHeader),
                            null));
        }
        if (request.method() == Http.Method.PATCH && target.identifier() != null) {
            return patch(invocation, request, target);
        }
        return completed(error(Http.Status.METHOD_NOT_ALLOWED, ProtocolError.INVALID_SYNTAX));
    }

    /**
     * Searches one resource collection.
     *
     * @param invocation context
     * @param request    request
     * @param type       resource type
     * @return response stage
     */
    CompletionStage<Response> search(final Context invocation, final Request request, final ResourceType type) {
        final String filter = HttpValues.query(request.query(), "filter", ScimService::invalidParameter);
        if (filter != null) {
            ScimFilterParser.parse(filter);
        }
        final String sortBy = HttpValues.query(request.query(), "sortBy", ScimService::invalidParameter);
        if (sortBy != null) {
            ScimFilter.path(sortBy);
        }
        final int start = number(
                HttpValues.query(request.query(), "startIndex", ScimService::invalidParameter),
                Normal._1);
        final int count = number(
                HttpValues.query(request.query(), "count", ScimService::invalidParameter),
                provider.maximumPageSize());
        Assert.isTrue(
                count <= provider.maximumPageSize(),
                () -> new ValidateException("SCIM requested page exceeds the configured limit"));
        final String sortOrder = HttpValues.query(request.query(), "sortOrder", ScimService::invalidParameter);
        Assert.isTrue(
                sortOrder == null || "ascending".equals(sortOrder) || "descending".equals(sortOrder),
                () -> new ValidateException("SCIM sortOrder is invalid"));
        final boolean ascending = !"descending".equals(sortOrder);
        final Query query = new Query(type, filter, sortBy, ascending, start, count);
        return repository.search(invocation, query)
                .thenApply(page -> json(Http.Status.OK, codec.write(page, start), null));
    }

    /**
     * Reads one resource with entity-tag preconditions.
     *
     * @param invocation context
     * @param request    request
     * @param target     target
     * @return response stage
     */
    CompletionStage<Response> read(final Context invocation, final Request request, final Target target) {
        return repository.find(invocation, target.type(), target.identifier()).thenApply(value -> {
            if (value.isEmpty()) {
                return error(Http.Status.NOT_FOUND, ProtocolError.NO_TARGET);
            }
            final ScimResource resource = value.orElseThrow();
            final String version = version(resource);
            if (!ScimEtag.excludes(
                    HttpValues.header(request.headers(), Http.Header.IF_NONE_MATCH, ScimService::invalidHeader),
                    version,
                    true)) {
                return new Response(Http.Status.NOT_MODIFIED, Map.of(Http.Header.ETAG, List.of(version)), new byte[0]);
            }
            return json(Http.Status.OK, codec.write(resource), resource);
        });
    }

    /**
     * Applies one PATCH after loading its source snapshot.
     *
     * @param invocation context
     * @param request    request
     * @param target     target
     * @return response stage
     */
    CompletionStage<Response> patch(final Context invocation, final Request request, final Target target) {
        return repository.find(invocation, target.type(), target.identifier()).thenCompose(value -> {
            if (value.isEmpty()) {
                return completed(error(Http.Status.NOT_FOUND, ProtocolError.NO_TARGET));
            }
            final ScimResource current = value.orElseThrow();
            if (!ScimEtag.matches(
                    HttpValues.header(request.headers(), Http.Header.IF_MATCH, ScimService::invalidHeader),
                    version(current),
                    true)) {
                return completed(error(Http.Status.PRECONDITION_FAILED, ProtocolError.INVALID_VERSION));
            }
            final ScimResource patched = ScimPatch.apply(current, codec.patch(request.body()));
            return mutate(
                    invocation,
                    request,
                    new Mutation(Http.Method.PATCH, target.type(), target.identifier(), patched,
                            HttpValues.header(request.headers(), Http.Header.IF_MATCH, ScimService::invalidHeader),
                            null));
        });
    }

    /**
     * Applies one repository mutation and maps its exact result.
     *
     * @param invocation context
     * @param request    request
     * @param mutation   mutation
     * @return response stage
     */
    CompletionStage<Response> mutate(final Context invocation, final Request request, final Mutation mutation) {
        return repository.apply(invocation, List.of(mutation)).thenApply(results -> {
            Assert.isTrue(
                    results.size() == Normal._1,
                    () -> new ValidateException("SCIM repository returned an invalid mutation result count"));
            final MutationResult result = results.getFirst();
            if (result.status() >= 400) {
                return error(
                        result.status(),
                        result.status() == Http.Status.PRECONDITION_FAILED ? ProtocolError.INVALID_VERSION
                                : ProtocolError.INVALID_VALUE);
            }
            if (result.resource() == null) {
                return new Response(result.status(), location(result), new byte[0]);
            }
            final Map<String, List<String>> headers = new LinkedHashMap<>(location(result));
            headers.put(Http.Header.CONTENT_TYPE, List.of(MediaType.APPLICATION_SCIM_JSON));
            headers.put(Http.Header.ETAG, List.of(version(result.resource())));
            return new Response(result.status(), headers, codec.write(result.resource()));
        });
    }

    /**
     * Builds one atomic mutation batch, resolving PATCH source snapshots before the single repository write.
     *
     * @param invocation operation context
     * @param request    request
     * @return response stage
     */
    CompletionStage<Response> bulk(final Context invocation, final Request request) {
        final ScimBulk.Request bulk = codec.bulk(request.body());
        CompletionStage<List<Mutation>> stage = CompletableFuture.completedFuture(new java.util.ArrayList<>());
        for (final ScimBulk.Entry entry : bulk.operations()) {
            stage = stage.thenCompose(mutations -> mutation(invocation, entry).thenApply(mutation -> {
                mutations.add(mutation);
                return mutations;
            }));
        }
        return stage.thenCompose(mutations -> repository.apply(invocation, List.copyOf(mutations)))
                .thenApply(results -> {
                    Assert.isTrue(
                            results.size() == bulk.operations().size(),
                            () -> new ValidateException("SCIM repository returned an invalid bulk result count"));
                    final ScimBulk.Response response = ScimBulk.response(results, bulk.failOnErrors());
                    return json(Http.Status.OK, codec.write(response), null);
                });
    }

    /**
     * Converts one decoded bulk entry to an atomic repository mutation.
     *
     * @param invocation operation context
     * @param entry      decoded entry
     * @return mutation stage
     */
    CompletionStage<Mutation> mutation(final Context invocation, final ScimBulk.Entry entry) {
        if (entry.method() == Http.Method.POST || entry.method() == Http.Method.PUT) {
            return CompletableFuture.completedFuture(
                    new Mutation(entry.method(), entry.type(), entry.identifier(), (ScimResource) entry.data(), null,
                            entry.bulkId()));
        }
        if (entry.method() == Http.Method.DELETE) {
            return CompletableFuture.completedFuture(
                    new Mutation(entry.method(), entry.type(), entry.identifier(), null, null, entry.bulkId()));
        }
        Assert.isTrue(
                !entry.identifier().startsWith("bulkId:"),
                () -> new ValidateException("SCIM bulk PATCH cannot target an unresolved bulk reference"));
        return repository.find(invocation, entry.type(), entry.identifier()).thenApply(value -> {
            final ScimResource source = value
                    .orElseThrow(() -> new ValidateException("SCIM bulk PATCH target does not exist"));
            final List<ScimPatch.Operation> operations = operations(entry.data());
            return new Mutation(Http.Method.PATCH, entry.type(), entry.identifier(),
                    ScimPatch.apply(source, operations), null, entry.bulkId());
        });
    }

    /**
     * Emits service-provider configuration.
     *
     * @return response
     */
    Response configuration() {
        final Map<String, Object> value = Map.of(
                "schemas",
                List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
                "patch",
                Map.of("supported", true),
                "bulk",
                Map.of(
                        "supported",
                        true,
                        "maxOperations",
                        SCIM.MAXIMUM_BULK_OPERATIONS,
                        "maxPayloadSize",
                        maximumBytes),
                "filter",
                Map.of("supported", true, "maxResults", provider.maximumPageSize()),
                "sort",
                Map.of("supported", true),
                "etag",
                Map.of("supported", true));
        return json(Http.Status.OK, json.write(value), null);
    }

    /**
     * Emits supported resource types.
     *
     * @return response
     */
    Response resourceTypes() {
        final List<Map<String, Object>> resources = java.util.Arrays.stream(ResourceType.values())
                .map(
                        type -> Map.<String, Object>of(
                                "id",
                                type.name(),
                                "name",
                                type.name(),
                                "endpoint",
                                "/" + type.path(),
                                "schema",
                                type.schema()))
                .toList();
        return json(Http.Status.OK, json.write(resources), null);
    }

    /**
     * Emits supported schema identifiers.
     *
     * @return response
     */
    Response schemas() {
        return json(
                Http.Status.OK,
                json.write(
                        provider.supportedSchemas().stream().map(schema -> Map.of("id", schema, "name", schema))
                                .toList()),
                null);
    }

    /**
     * Creates one JSON response.
     *
     * @param status   status
     * @param body     body
     * @param resource optional resource
     * @return response
     */
    Response json(final int status, final byte[] body, final ScimResource resource) {
        final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
        headers.put(Http.Header.CONTENT_TYPE, List.of(MediaType.APPLICATION_SCIM_JSON));
        if (resource != null) {
            headers.put(Http.Header.ETAG, List.of(version(resource)));
            if (resource.meta() != null) {
                headers.put(Http.Header.LOCATION, List.of(resource.meta().location().toString()));
            }
        }
        return new Response(status, headers, body);
    }

    /**
     * Creates one stable error response.
     *
     * @param status HTTP status
     * @param error  protocol error
     * @return response
     */
    Response error(final int status, final ProtocolError error) {
        return json(status, codec.error(status, error), null);
    }

    /**
     * Maps an asynchronous or synchronous failure to a stable error response.
     *
     * @param failure failure
     * @return response
     */
    Response failure(final Throwable failure) {
        final Throwable cause = ExceptionKit.unwrap(failure);
        final boolean validation = cause instanceof ValidateException;
        final boolean filter = validation && cause.getMessage() != null && cause.getMessage().contains("filter");
        return error(
                validation ? Http.Status.BAD_REQUEST : Http.Status.INTERNAL_SERVER_ERROR,
                filter ? ProtocolError.INVALID_FILTER
                        : validation ? ProtocolError.INVALID_SYNTAX : ProtocolError.INVALID_VALUE);
    }

    /**
     * Returns a resource version.
     *
     * @param resource resource
     * @return entity tag
     */
    String version(final ScimResource resource) {
        return resource.meta() == null ? ScimEtag.create(codec.canonical(resource)) : resource.meta().version();
    }

    /**
     * Immutable parsed route target.
     *
     * @param type       resource type
     * @param identifier optional resource identifier
     * @author Kimi Liu
     */
    record Target(ResourceType type, String identifier) {
    }

}
