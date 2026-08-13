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
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.protocol.Handler;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;

/**
 * Defines the sole SCIM 2.0 service-provider and client facades together with immutable product repository contracts.
 * The protocol engine maps HTTP-shaped DTOs only; routing, HTTP server ownership, persistence, and transactions remain
 * product responsibilities exposed through the atomic {@link Repository} port.
 *
 * @author Kimi Liu
 */
public final class SCIM {

    /**
     * Maximum operations accepted in one SCIM bulk request.
     */
    public static final int MAXIMUM_BULK_OPERATIONS = 1000;

    /**
     * SCIM core User schema identifier.
     */
    public static final String USER_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:User";

    /**
     * SCIM core Group schema identifier.
     */
    public static final String GROUP_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Group";

    /**
     * SCIM ListResponse message schema identifier.
     */
    public static final String LIST_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:ListResponse";

    /**
     * SCIM Error message schema identifier.
     */
    public static final String ERROR_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";

    /**
     * SCIM PatchOp message schema identifier.
     */
    public static final String PATCH_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    /**
     * SCIM BulkRequest message schema identifier.
     */
    public static final String BULK_REQUEST_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";

    /**
     * SCIM BulkResponse message schema identifier.
     */
    public static final String BULK_RESPONSE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

    /**
     * Prevents construction of the protocol namespace.
     */
    private SCIM() {
        // No initialization required.
    }

    /**
     * Creates the sole service-provider engine.
     *
     * @param configuration provider configuration
     * @param repository    atomic product repository
     * @param json          JSON provider
     * @param maximumBytes  maximum SCIM JSON bytes
     * @param maximumDepth  maximum SCIM JSON nesting depth
     * @return SCIM service provider
     */
    public static Provider provider(
            final ProviderConfig configuration,
            final Repository repository,
            final JsonProvider json,
            final int maximumBytes,
            final int maximumDepth) {
        return new ScimService(configuration, repository, json, maximumBytes, maximumDepth);
    }

    /**
     * Creates the sole remote SCIM client.
     *
     * @param configuration client configuration
     * @param fabric        Fabric context used for HTTPS exchanges
     * @param json          JSON provider
     * @param maximumBytes  maximum SCIM JSON bytes
     * @param maximumDepth  maximum SCIM JSON nesting depth
     * @return SCIM client
     */
    public static Client client(
            final ClientConfig configuration,
            final org.miaixz.bus.fabric.Context fabric,
            final JsonProvider json,
            final int maximumBytes,
            final int maximumDepth) {
        return new ScimService(configuration, fabric, json, maximumBytes, maximumDepth);
    }

    /**
     * Creates an immutable deep snapshot of multi-value text entries.
     *
     * @param source non-null caller-owned multi-value map
     * @param name   logical member name used by validation failures
     * @return recursively immutable map snapshot
     * @throws ValidateException if the map, a key, a value list, or a list member is invalid
     */
    private static Map<String, List<String>> immutableValues(
            final Map<String, List<String>> source,
            final String name) {
        final Map<String, List<String>> checked = Assert
                .notNull(source, () -> new ValidateException(name + " must not be null"));
        final LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
        checked.forEach(
                (key, values) -> copy.put(
                        Assert.notBlank(key, () -> new ValidateException(name + " contains a blank key")),
                        List.copyOf(
                                Assert.notNull(values, () -> new ValidateException(name + " contains null values")))));
        return Map.copyOf(copy);
    }

    /**
     * Validates one absolute HTTPS service root.
     *
     * @param value source URI
     * @return validated URI
     */
    static URI root(final URI value) {
        final URI result = Assert.notNull(value, () -> new ValidateException("SCIM service root must not be null"));
        Assert.isTrue(
                result.isAbsolute() && Protocol.HTTPS.name().equalsIgnoreCase(result.getScheme())
                        && result.getHost() != null && result.getUserInfo() == null && result.getFragment() == null,
                () -> new ValidateException("SCIM service root must be an absolute HTTPS URI"));
        return result;
    }

    /**
     * Normalizes one optional non-blank text value.
     *
     * @param value source text
     * @return normalized text or {@code null}
     */
    static String text(final String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * SCIM resource kind.
     *
     * @author Kimi Liu
     */
    public enum ResourceType {

        /**
         * User resource.
         */
        USER("Users", USER_SCHEMA),

        /**
         * Group resource.
         */
        GROUP("Groups", GROUP_SCHEMA);

        /**
         * Collection path segment.
         */
        private final String path;

        /**
         * Core resource schema.
         */
        private final String schema;

        /**
         * Creates one resource-kind mapping.
         *
         * @param path   collection path
         * @param schema core schema
         */
        ResourceType(final String path, final String schema) {
            this.path = path;
            this.schema = schema;
        }

        /**
         * Returns the collection path segment.
         *
         * @return collection path
         */
        public String path() {
            return path;
        }

        /**
         * Returns the core schema identifier.
         *
         * @return core schema
         */
        public String schema() {
            return schema;
        }
    }

    /**
     * Stable SCIM wire error types.
     *
     * @author Kimi Liu
     */
    public enum ProtocolError implements Errors {

        /**
         * Filter syntax or operator is invalid.
         */
        INVALID_FILTER("invalidFilter", "The filter is invalid"),

        /**
         * Requested result count exceeds the service limit.
         */
        TOO_MANY("tooMany", "The requested result count exceeds the limit"),

        /**
         * A uniqueness constraint was violated.
         */
        UNIQUENESS("uniqueness", "A unique value already exists"),

        /**
         * A read-only or immutable attribute was changed.
         */
        MUTABILITY("mutability", "The requested attribute is not mutable"),

        /**
         * Request body syntax is invalid.
         */
        INVALID_SYNTAX("invalidSyntax", "The request syntax is invalid"),

        /**
         * PATCH path syntax is invalid.
         */
        INVALID_PATH("invalidPath", "The PATCH path is invalid"),

        /**
         * PATCH path selected no target.
         */
        NO_TARGET("noTarget", "The PATCH path selected no target"),

        /**
         * Attribute value is invalid.
         */
        INVALID_VALUE("invalidValue", "An attribute value is invalid"),

        /**
         * Resource version precondition failed.
         */
        INVALID_VERSION("invalidVers", "The resource version is invalid"),

        /**
         * Requested attribute cannot be returned.
         */
        SENSITIVE("sensitive", "The requested attribute is sensitive");

        /**
         * Stable SCIM type.
         */
        private final String key;

        /**
         * Fixed safe detail.
         */
        private final String value;

        /**
         * Creates one unregistered SCIM wire error.
         *
         * @param key   standard scimType
         * @param value fixed safe detail
         */
        ProtocolError(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the standard scimType.
         *
         * @return standard scimType
         */
        @Override
        public String getKey() {
            return key;
        }

        /**
         * Returns fixed safe detail.
         *
         * @return safe detail
         */
        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * SCIM service-provider contract.
     *
     * @author Kimi Liu
     */
    public interface Provider extends Handler<Request, Response> {

        /**
         * Handles one bounded HTTP-shaped SCIM request.
         *
         * @param invocation operation context
         * @param request    SCIM request
         * @return internal outcome whose success value is a standard SCIM response or error
         */
        @Override
        CompletionStage<Outcome<Response>> handle(Context invocation, Request request);
    }

    /**
     * Remote SCIM client contract.
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Client {

        /**
         * Executes one request against the configured service root.
         *
         * @param invocation operation context
         * @param request    SCIM request
         * @return remote SCIM response
         */
        CompletionStage<Response> execute(Context invocation, Request request);
    }

    /**
     * Product repository contract with atomic mutation batches.
     *
     * @author Kimi Liu
     */
    public interface Repository {

        /**
         * Finds one tenant-scoped resource.
         *
         * @param invocation operation context
         * @param type       resource type
         * @param identifier resource identifier
         * @return optional resource
         */
        CompletionStage<Optional<ScimResource>> find(Context invocation, ResourceType type, String identifier);

        /**
         * Searches tenant-scoped resources.
         *
         * @param invocation operation context
         * @param query      bounded query
         * @return immutable page
         */
        CompletionStage<Page> search(Context invocation, Query query);

        /**
         * Applies all mutations atomically in request order.
         *
         * @param invocation operation context
         * @param mutations  non-empty bounded mutations
         * @return mutation results in request order
         */
        CompletionStage<List<MutationResult>> apply(Context invocation, List<Mutation> mutations);
    }

    /**
     * Immutable SCIM HTTP request model owned by the protocol boundary.
     *
     * @param method  HTTP method
     * @param uri     absolute request URI
     * @param headers immutable multi-value headers
     * @param query   immutable multi-value query parameters
     * @param body    copied request body bytes
     * @author Kimi Liu
     */
    public record Request(Http.Method method, URI uri, Map<String, List<String>> headers,
            Map<String, List<String>> query, byte[] body) {

        /**
         * Validates and snapshots one protocol request.
         */
        public Request {
            method = Assert.notNull(method, () -> new ValidateException("SCIM method must not be null"));
            uri = Assert.notNull(uri, () -> new ValidateException("SCIM URI must not be null"));
            headers = immutableValues(headers, "SCIM headers");
            query = immutableValues(query, "SCIM query");
            body = Arrays.copyOf(
                    Assert.notNull(body, () -> new ValidateException("SCIM body must not be null")),
                    body.length);
        }

        /**
         * Returns a defensive copy of the request body.
         *
         * @return newly allocated request body bytes
         */
        @Override
        public byte[] body() {
            return Arrays.copyOf(body, body.length);
        }
    }

    /**
     * Immutable SCIM standard HTTP response model.
     *
     * @param status  HTTP status code
     * @param headers immutable multi-value response headers
     * @param body    copied SCIM response bytes
     * @author Kimi Liu
     */
    public record Response(int status, Map<String, List<String>> headers, byte[] body) {

        /**
         * Validates and snapshots one protocol response.
         */
        public Response {
            Assert.isTrue(
                    status >= 100 && status <= 599,
                    () -> new ValidateException("SCIM response status is invalid"));
            headers = immutableValues(headers, "SCIM response headers");
            body = Arrays.copyOf(
                    Assert.notNull(body, () -> new ValidateException("SCIM response body must not be null")),
                    body.length);
        }

        /**
         * Returns a defensive copy of the response body.
         *
         * @return newly allocated response body bytes
         */
        @Override
        public byte[] body() {
            return Arrays.copyOf(body, body.length);
        }
    }

    /**
     * Immutable provider configuration.
     *
     * @param serviceRoot      absolute HTTPS service root
     * @param maximumPageSize  positive page ceiling
     * @param supportedSchemas immutable supported schema identifiers
     * @author Kimi Liu
     */
    public record ProviderConfig(URI serviceRoot, int maximumPageSize, Set<String> supportedSchemas) {

        /**
         * Validates provider configuration.
         *
         * @param serviceRoot      service root
         * @param maximumPageSize  page ceiling
         * @param supportedSchemas supported schemas
         */
        public ProviderConfig {
            serviceRoot = root(serviceRoot);
            Assert.isTrue(
                    maximumPageSize > Normal._0,
                    () -> new ValidateException("SCIM maximum page size must be positive"));
            supportedSchemas = Set.copyOf(
                    Assert.notNull(
                            supportedSchemas,
                            () -> new ValidateException("SCIM supported schemas must not be null")));
            Assert.isTrue(
                    supportedSchemas.size() <= Normal._128 && supportedSchemas.contains(USER_SCHEMA)
                            && supportedSchemas.contains(GROUP_SCHEMA),
                    () -> new ValidateException("SCIM core User and Group schemas are required"));
        }
    }

    /**
     * Immutable remote-client configuration.
     *
     * @param serviceRoot   absolute HTTPS service root
     * @param addressPolicy Fabric destination and peer address policy restricted to HTTPS
     * @param timeout       Fabric connect, read, write, call, and close timeouts
     * @author Kimi Liu
     */
    public record ClientConfig(URI serviceRoot, AddressPolicy addressPolicy, Timeout timeout) {

        /**
         * Validates remote-client configuration.
         *
         * @param serviceRoot   service root
         * @param addressPolicy Fabric HTTPS destination and peer policy
         * @param timeout       Fabric operation timeouts
         * @throws ValidateException if the root, address policy, or timeout is invalid
         */
        public ClientConfig {
            serviceRoot = root(serviceRoot);
            addressPolicy = Assert
                    .notNull(addressPolicy, () -> new ValidateException("SCIM address policy must not be null"));
            timeout = Assert.notNull(timeout, () -> new ValidateException("SCIM timeout must not be null"));
            Assert.isTrue(
                    addressPolicy.allowedSchemes().equals(Set.of(Protocol.HTTPS)),
                    () -> new ValidateException("SCIM client requires an HTTPS address policy"));
        }
    }

    /**
     * Immutable resource query.
     *
     * @param type       resource type
     * @param filter     optional filter text
     * @param sortBy     optional attribute path
     * @param ascending  sort direction
     * @param startIndex one-based start index
     * @param count      non-negative page count
     * @author Kimi Liu
     */
    public record Query(ResourceType type, String filter, String sortBy, boolean ascending, int startIndex, int count) {

        /**
         * Validates query boundaries.
         *
         * @param type       resource type
         * @param filter     filter text
         * @param sortBy     sort path
         * @param ascending  sort direction
         * @param startIndex start index
         * @param count      page count
         */
        public Query {
            type = Assert.notNull(type, () -> new ValidateException("SCIM resource type must not be null"));
            filter = filter == null || filter.isBlank() ? null : filter;
            sortBy = sortBy == null || sortBy.isBlank() ? null : sortBy;
            Assert.isTrue(
                    startIndex >= Normal._1 && count >= Normal._0,
                    () -> new ValidateException("SCIM page values are invalid"));
        }
    }

    /**
     * Immutable repository page.
     *
     * @param resources    page resources
     * @param totalResults total matching resources
     * @author Kimi Liu
     */
    public record Page(List<ScimResource> resources, int totalResults) {

        /**
         * Validates page output.
         *
         * @param resources    resources
         * @param totalResults total matches
         */
        public Page {
            resources = List.copyOf(
                    Assert.notNull(resources, () -> new ValidateException("SCIM page resources must not be null")));
            Assert.isTrue(
                    totalResults >= resources.size(),
                    () -> new ValidateException("SCIM total results must include the page"));
        }
    }

    /**
     * Immutable atomic repository mutation.
     *
     * @param method     POST, PUT, PATCH, or DELETE
     * @param type       resource type
     * @param identifier optional target identifier
     * @param resource   optional complete mutation resource
     * @param version    optional If-Match version
     * @param bulkId     optional request-local bulk identifier
     * @author Kimi Liu
     */
    public record Mutation(Http.Method method, ResourceType type, String identifier, ScimResource resource,
            String version, String bulkId) {

        /**
         * Validates one repository mutation.
         *
         * @param method     HTTP method
         * @param type       resource type
         * @param identifier target identifier
         * @param resource   resource value
         * @param version    expected version
         * @param bulkId     bulk identifier
         */
        public Mutation {
            method = Assert.notNull(method, () -> new ValidateException("SCIM mutation method must not be null"));
            type = Assert.notNull(type, () -> new ValidateException("SCIM mutation type must not be null"));
            identifier = text(identifier);
            version = text(version);
            bulkId = text(bulkId);
            Assert.isTrue(
                    method == Http.Method.POST || method == Http.Method.PUT || method == Http.Method.PATCH
                            || method == Http.Method.DELETE,
                    () -> new ValidateException("SCIM mutation method is unsupported"));
            Assert.isTrue(
                    method == Http.Method.POST ? identifier == null && resource != null
                            : identifier != null && (method == Http.Method.DELETE || resource != null),
                    () -> new ValidateException("SCIM mutation target or resource is invalid"));
        }
    }

    /**
     * Immutable atomic mutation result.
     *
     * @param method   completed method
     * @param location optional resource location
     * @param resource optional resulting resource
     * @param status   HTTP status
     * @param bulkId   optional request-local bulk identifier
     * @author Kimi Liu
     */
    public record MutationResult(Http.Method method, URI location, ScimResource resource, int status, String bulkId) {

        /**
         * Validates one mutation result.
         *
         * @param method   HTTP method
         * @param location resource location
         * @param resource resulting resource
         * @param status   HTTP status
         * @param bulkId   bulk identifier
         */
        public MutationResult {
            method = Assert.notNull(method, () -> new ValidateException("SCIM result method must not be null"));
            Assert.isTrue(status >= 200 && status <= 599, () -> new ValidateException("SCIM result status is invalid"));
            bulkId = text(bulkId);
        }
    }

}
