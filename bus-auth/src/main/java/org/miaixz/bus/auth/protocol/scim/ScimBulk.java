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

import org.miaixz.bus.auth.protocol.scim.SCIM.Mutation;
import org.miaixz.bus.auth.protocol.scim.SCIM.MutationResult;
import org.miaixz.bus.auth.protocol.scim.SCIM.ResourceType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Validates SCIM BulkRequest limits, request-local bulk identifiers, dependency references, and fail-on-error
 * termination. Repository atomicity remains controlled by the service and its repository port.
 *
 * @author Kimi Liu
 */
public final class ScimBulk {

    /**
     * Maximum encoded BulkRequest size.
     */
    public static final int MAXIMUM_BYTES = 1024 * 1024;

    /**
     * Maximum operations in one BulkRequest.
     */
    public static final int MAXIMUM_OPERATIONS = 1000;

    /**
     * Maximum request-local bulk identifier length.
     */
    public static final int MAXIMUM_BULK_ID_CHARACTERS = Normal._1024;

    /**
     * Prevents construction of the bulk utility.
     */
    private ScimBulk() {
        // No initialization required.
    }

    /**
     * Resolves prior {@code bulkId:identifier} references in a mutation target.
     *
     * @param mutation  source mutation
     * @param locations completed bulk locations by identifier
     * @return resolved mutation
     */
    public static Mutation resolve(final Mutation mutation, final Map<String, String> locations) {
        final Mutation source = Assert
                .notNull(mutation, () -> new ValidateException("SCIM bulk mutation must not be null"));
        final Map<String, String> resolved = Map
                .copyOf(Assert.notNull(locations, () -> new ValidateException("SCIM bulk locations must not be null")));
        final String identifier = reference(source.identifier(), resolved);
        return new Mutation(source.method(), source.type(), identifier, source.resource(), source.version(),
                source.bulkId());
    }

    /**
     * Applies fail-on-error response truncation without changing result order.
     *
     * @param results      ordered results
     * @param failOnErrors failure threshold
     * @return bounded response
     */
    public static Response response(final List<MutationResult> results, final int failOnErrors) {
        final List<MutationResult> source = List
                .copyOf(Assert.notNull(results, () -> new ValidateException("SCIM bulk results must not be null")));
        Assert.isTrue(
                failOnErrors >= Normal._0,
                () -> new ValidateException("SCIM bulk failOnErrors must not be negative"));
        if (failOnErrors == Normal._0) {
            return new Response(source, false);
        }
        final ArrayList<MutationResult> accepted = new ArrayList<>();
        int failures = Normal._0;
        for (final MutationResult result : source) {
            accepted.add(result);
            if (result.status() >= 400 && ++failures >= failOnErrors) {
                return new Response(accepted, accepted.size() < source.size());
            }
        }
        return new Response(accepted, false);
    }

    /**
     * Indexes successful bulk identifiers to their final resource identifier.
     *
     * @param results completed results
     * @return immutable location index
     */
    public static Map<String, String> locations(final List<MutationResult> results) {
        final List<MutationResult> source = List
                .copyOf(Assert.notNull(results, () -> new ValidateException("SCIM bulk results must not be null")));
        final LinkedHashMap<String, String> locations = new LinkedHashMap<>();
        for (final MutationResult result : source) {
            if (result.bulkId() != null && result.status() < 400 && result.location() != null) {
                final String path = result.location().getPath();
                final int separator = path.lastIndexOf('/');
                locations.put(result.bulkId(), path.substring(separator + Normal._1));
            }
        }
        return Map.copyOf(locations);
    }

    /**
     * Resolves one optional bulk reference.
     *
     * @param value     source value
     * @param locations location index
     * @return resolved value
     */
    static String reference(final String value, final Map<String, String> locations) {
        if (value == null || !value.startsWith("bulkId:")) {
            return value;
        }
        final String result = locations.get(value.substring("bulkId:".length()));
        if (result == null) {
            throw new ValidateException("SCIM bulk reference is unresolved");
        }
        return result;
    }

    /**
     * Recursively snapshots decoded JSON containers and byte values.
     *
     * @param value decoded bulk operation data
     * @return recursively immutable snapshot
     * @throws ValidateException if a map key is not text or a value shape is unsupported
     */
    private static Object snapshot(final Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof ScimResource || value instanceof ScimPatch.Operation) {
            return value;
        }
        if (value instanceof byte[] bytes) {
            return bytes.clone();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(ScimBulk::snapshot).toList();
        }
        if (value instanceof Map<?, ?> map) {
            final LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, entry) -> {
                if (!(key instanceof String name) || name.isBlank()) {
                    throw new ValidateException("SCIM bulk data contains an invalid member name");
                }
                copy.put(name, snapshot(entry));
            });
            return Map.copyOf(copy);
        }
        throw new ValidateException("SCIM bulk data contains an unsupported value");
    }

    /**
     * Immutable decoded bulk operation before PATCH source resolution.
     *
     * @param method     HTTP method
     * @param type       resource type
     * @param identifier optional target identifier or bulk reference
     * @param bulkId     optional request-local identifier
     * @param data       optional resource object or PatchOp operation list
     * @author Kimi Liu
     */
    public record Entry(org.miaixz.bus.core.net.Http.Method method, ResourceType type, String identifier, String bulkId,
            Object data) {

        /**
         * Validates one decoded entry.
         *
         * @param method     HTTP method
         * @param type       resource type
         * @param identifier target identifier
         * @param bulkId     bulk identifier
         * @param data       operation data
         */
        public Entry {
            method = Assert.notNull(method, () -> new ValidateException("SCIM bulk method must not be null"));
            type = Assert.notNull(type, () -> new ValidateException("SCIM bulk resource type must not be null"));
            Assert.isTrue(
                    method == org.miaixz.bus.core.net.Http.Method.POST
                            || method == org.miaixz.bus.core.net.Http.Method.PUT
                            || method == org.miaixz.bus.core.net.Http.Method.PATCH
                            || method == org.miaixz.bus.core.net.Http.Method.DELETE,
                    () -> new ValidateException("SCIM bulk method is unsupported"));
            Assert.isTrue(
                    method == org.miaixz.bus.core.net.Http.Method.POST ? identifier == null && data != null
                            : identifier != null
                                    && (method == org.miaixz.bus.core.net.Http.Method.DELETE || data != null),
                    () -> new ValidateException("SCIM bulk target or data is invalid"));
            Assert.isTrue(
                    bulkId == null || !bulkId.isBlank() && bulkId.length() <= MAXIMUM_BULK_ID_CHARACTERS,
                    () -> new ValidateException("SCIM bulk identifier is invalid"));
            data = snapshot(data);
        }

        /**
         * Returns an independent recursively safe operation-data view.
         *
         * @return immutable containers with independently copied byte values
         */
        @Override
        public Object data() {
            return snapshot(data);
        }
    }

    /**
     * Immutable bulk request.
     *
     * @param operations   ordered decoded operations
     * @param failOnErrors zero to process every operation, or a positive failure threshold
     * @param encodedBytes original request byte count
     * @author Kimi Liu
     */
    public record Request(List<Entry> operations, int failOnErrors, int encodedBytes) {

        /**
         * Validates one request.
         *
         * @param operations   operations
         * @param failOnErrors failure threshold
         * @param encodedBytes request size
         */
        public Request {
            operations = List.copyOf(
                    Assert.notNull(operations, () -> new ValidateException("SCIM bulk operations must not be null")));
            Assert.isTrue(
                    !operations.isEmpty() && operations.size() <= MAXIMUM_OPERATIONS,
                    () -> new ValidateException("SCIM bulk operation count is invalid"));
            Assert.isTrue(
                    failOnErrors >= Normal._0,
                    () -> new ValidateException("SCIM bulk failOnErrors must not be negative"));
            Assert.isTrue(
                    encodedBytes >= Normal._0 && encodedBytes <= MAXIMUM_BYTES,
                    () -> new ValidateException("SCIM bulk request exceeds its byte limit"));
            final java.util.HashSet<String> identifiers = new java.util.HashSet<>();
            for (final Entry operation : operations) {
                if (operation.bulkId() != null) {
                    Assert.isTrue(
                            operation.bulkId().length() <= MAXIMUM_BULK_ID_CHARACTERS
                                    && identifiers.add(operation.bulkId()),
                            () -> new ValidateException("SCIM bulk identifier is invalid or duplicated"));
                }
            }
        }
    }

    /**
     * Immutable bulk response.
     *
     * @param operations completed operation results
     * @param terminated whether failOnErrors stopped processing
     * @author Kimi Liu
     */
    public record Response(List<MutationResult> operations, boolean terminated) {

        /**
         * Snapshots response operations.
         *
         * @param operations operation results
         * @param terminated termination flag
         */
        public Response {
            operations = List.copyOf(
                    Assert.notNull(operations, () -> new ValidateException("SCIM bulk results must not be null")));
        }
    }

}
