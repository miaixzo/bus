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
package org.miaixz.bus.auth.metric.scim;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Request;
import org.miaixz.bus.auth.metric.AuthMetric.Response;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.SCIM.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Implements both SCIM facades. Provider mode routes bounded HTTP-shaped DTOs and delegates persistence only through
 * the tenant-scoped repository; client mode delegates HTTPS exchange only through the runtime protocol transport.
 *
 * @author Kimi Liu
 */
public final class ScimService implements Provider, Client {

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
    private final Runtime runtime;

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
     * @param runtime       runtime
     * @param repository    repository
     */
    public ScimService(final ProviderConfig configuration, final Runtime runtime, final Repository repository) {
        this.provider = Assert
                .notNull(configuration, () -> new ValidateException("SCIM provider configuration must not be null"));
        this.client = null;
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("SCIM runtime must not be null"));
        this.repository = Assert.notNull(repository, () -> new ValidateException("SCIM repository must not be null"));
        this.codec = new ScimCodec(runtime);
    }

    /**
     * Creates client mode.
     *
     * @param configuration client configuration
     * @param runtime       runtime
     */
    public ScimService(final ClientConfig configuration, final Runtime runtime) {
        this.provider = null;
        this.client = Assert
                .notNull(configuration, () -> new ValidateException("SCIM client configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("SCIM runtime must not be null"));
        this.repository = null;
        this.codec = new ScimCodec(runtime);
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
     * Reads one single-valued normalized header.
     *
     * @param request request
     * @param name    header name
     * @return value or {@code null}
     */
    static String header(final Request request, final String name) {
        final List<String> values = request.headers().get(name.toLowerCase(java.util.Locale.ROOT));
        Assert.isTrue(
                values == null || values.size() == Normal._1,
                () -> new ValidateException("SCIM header must occur at most once"));
        return values == null ? null : values.getFirst();
    }

    /**
     * Reads one single-valued query parameter.
     *
     * @param request request
     * @param name    parameter name
     * @return value or {@code null}
     */
    static String parameter(final Request request, final String name) {
        final List<String> values = request.query().get(name);
        Assert.isTrue(
                values == null || values.size() == Normal._1,
                () -> new ValidateException("SCIM query parameter must occur at most once"));
        return values == null ? null : values.getFirst();
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
     * Handles one bounded provider request.
     *
     * @param invocation operation context
     * @param request    request
     * @return response stage
     */
    @Override
    public CompletionStage<Response> handle(final Invocation invocation, final Request request) {
        Assert.notNull(invocation, () -> new ValidateException("SCIM invocation must not be null"));
        Assert.notNull(request, () -> new ValidateException("SCIM request must not be null"));
        Assert.notNull(provider, () -> new ValidateException("SCIM service is not in provider mode"));
        try {
            return route(invocation, request).exceptionally(failure -> failure(failure));
        } catch (final RuntimeException failure) {
            return CompletableFuture.completedFuture(failure(failure));
        }
    }

    /**
     * Executes one bounded remote request.
     *
     * @param invocation operation context
     * @param request    request
     * @return response stage
     */
    @Override
    public CompletionStage<Response> execute(final Invocation invocation, final Request request) {
        Assert.notNull(client, () -> new ValidateException("SCIM service is not in client mode"));
        Assert.notNull(invocation, () -> new ValidateException("SCIM invocation must not be null"));
        final Request outbound = Assert.notNull(request, () -> new ValidateException("SCIM request must not be null"));
        Assert.isTrue(
                under(client.serviceRoot(), outbound.uri()),
                () -> new ValidateException("SCIM request URI is outside the configured service root"));
        return runtime.transports().protocol().exchange(invocation, outbound, client.transportPolicy());
    }

    /**
     * Routes one provider request.
     *
     * @param invocation operation context
     * @param request    request
     * @return response stage
     */
    CompletionStage<Response> route(final Invocation invocation, final Request request) {
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
                            codec.resource(request.body(), target.type()), header(request, Http.Header.IF_MATCH),
                            null));
        }
        if (request.method() == Http.Method.DELETE && target.identifier() != null) {
            return mutate(
                    invocation,
                    request,
                    new Mutation(Http.Method.DELETE, target.type(), target.identifier(), null,
                            header(request, Http.Header.IF_MATCH), null));
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
    CompletionStage<Response> search(final Invocation invocation, final Request request, final ResourceType type) {
        final String filter = parameter(request, "filter");
        if (filter != null) {
            ScimFilterParser.parse(filter);
        }
        final String sortBy = parameter(request, "sortBy");
        if (sortBy != null) {
            ScimFilter.path(sortBy);
        }
        final int start = number(parameter(request, "startIndex"), Normal._1);
        final int count = number(parameter(request, "count"), provider.maximumPageSize());
        Assert.isTrue(
                count <= provider.maximumPageSize(),
                () -> new ValidateException("SCIM requested page exceeds the configured limit"));
        final boolean ascending = !"descending".equals(parameter(request, "sortOrder"));
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
    CompletionStage<Response> read(final Invocation invocation, final Request request, final Target target) {
        return repository.find(invocation, target.type(), target.identifier()).thenApply(value -> {
            if (value.isEmpty()) {
                return error(Http.Status.NOT_FOUND, ProtocolError.NO_TARGET);
            }
            final ScimResource resource = value.orElseThrow();
            final String version = version(resource);
            if (!ScimEtag.excludes(header(request, Http.Header.IF_NONE_MATCH), version, true)) {
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
    CompletionStage<Response> patch(final Invocation invocation, final Request request, final Target target) {
        return repository.find(invocation, target.type(), target.identifier()).thenCompose(value -> {
            if (value.isEmpty()) {
                return completed(error(Http.Status.NOT_FOUND, ProtocolError.NO_TARGET));
            }
            final ScimResource current = value.orElseThrow();
            if (!ScimEtag.matches(header(request, Http.Header.IF_MATCH), version(current), true)) {
                return completed(error(Http.Status.PRECONDITION_FAILED, ProtocolError.INVALID_VERSION));
            }
            final ScimResource patched = ScimPatch.apply(current, codec.patch(request.body()));
            return mutate(
                    invocation,
                    request,
                    new Mutation(Http.Method.PATCH, target.type(), target.identifier(), patched,
                            header(request, Http.Header.IF_MATCH), null));
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
    CompletionStage<Response> mutate(final Invocation invocation, final Request request, final Mutation mutation) {
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
    CompletionStage<Response> bulk(final Invocation invocation, final Request request) {
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
    CompletionStage<Mutation> mutation(final Invocation invocation, final ScimBulk.Entry entry) {
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
            @SuppressWarnings("unchecked")
            final List<ScimPatch.Operation> operations = (List<ScimPatch.Operation>) entry.data();
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
                        runtime.limits().maxScimBulkOperations(),
                        "maxPayloadSize",
                        runtime.limits().maxScimBulkBytes()),
                "filter",
                Map.of("supported", true, "maxResults", provider.maximumPageSize()),
                "sort",
                Map.of("supported", true),
                "etag",
                Map.of("supported", true));
        return json(Http.Status.OK, runtime.json().write(value), null);
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
        return json(Http.Status.OK, runtime.json().write(resources), null);
    }

    /**
     * Emits supported schema identifiers.
     *
     * @return response
     */
    Response schemas() {
        return json(
                Http.Status.OK,
                runtime.json().write(
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
        final Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
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
     */
    record Target(ResourceType type, String identifier) {
    }

}
