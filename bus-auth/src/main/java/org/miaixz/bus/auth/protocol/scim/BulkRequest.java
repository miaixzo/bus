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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Models an RFC 7644 bulk request and its ordered resource operations.
 *
 * @param schemas      singleton standard BulkRequest schema URI
 * @param failOnErrors positive error count after which further operations are not attempted
 * @param operations   non-empty operations in execution order
 * @author Kimi Liu
 */
public record BulkRequest(List<String> schemas, Optional<Integer> failOnErrors, List<Operation> operations)
        implements AutoCloseable {

    /**
     * Enforces the BulkRequest schema, fail-on-error bound, operation order, and POST bulkId uniqueness.
     *
     * @throws IllegalArgumentException if a collection, optional container, or operation is {@code null}
     * @throws ValidateException        if the schema, error bound, operation sequence, or bulkId is invalid
     */
    public BulkRequest {
        Assert.notNull(schemas, "SCIM BulkRequest schemas must not be null");
        schemas = List.copyOf(schemas);
        if (!schemas.equals(List.of(Scim.BULK_REQUEST_SCHEMA))) {
            throw new ValidateException("SCIM BulkRequest schemas must contain only the standard schema URI");
        }
        Assert.notNull(failOnErrors, "SCIM BulkRequest failOnErrors container must not be null");
        failOnErrors = Optional.ofNullable(failOnErrors.getOrNull());
        if (!failOnErrors.isEmpty() && failOnErrors.getOrThrow() <= 0) {
            throw new ValidateException("SCIM BulkRequest failOnErrors must be positive");
        }
        Assert.notNull(operations, "SCIM BulkRequest Operations must not be null");
        if (operations.isEmpty()) {
            throw new ValidateException("SCIM BulkRequest Operations must not be empty");
        }
        final Set<String> bulkIds = new HashSet<>();
        for (Operation operation : operations) {
            final Operation item = Assert.notNull(operation, "SCIM BulkRequest operation must not be null");
            if (item.method() == Http.Method.POST && !bulkIds.add(item.bulkId().getOrThrow())) {
                throw new ValidateException("SCIM BulkRequest POST bulkId values must be unique");
            }
        }
        operations = List.copyOf(operations);
    }

    /**
     * Normalizes a required Bus Optional containing non-blank text.
     *
     * @param value required optional container
     * @param label validation label
     * @return independent optional with the same text value
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        if (!value.isEmpty()) {
            Assert.notBlank(value.getOrThrow(), label + " must not be blank");
        }
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Requires the complete weak or strong HTTP entity-tag shape used by a bulk If-Match value.
     *
     * @param value candidate entity-tag
     */
    private static void validateEntityTag(final String value) {
        final int quote = value.startsWith("W/\"") ? 2 : value.startsWith("\"") ? 0 : -1;
        if (quote < 0 || value.length() <= quote + 1 || value.charAt(value.length() - 1) != Symbol.C_DOUBLE_QUOTES) {
            throw new ValidateException("SCIM bulk operation version must be a complete HTTP entity-tag");
        }
        for (int index = quote + 1; index < value.length() - 1; index++) {
            final char character = value.charAt(index);
            if (character == Symbol.C_DOUBLE_QUOTES || character <= 0x20 || character == 0x7f) {
                throw new ValidateException("SCIM bulk operation version contains an invalid entity-tag character");
            }
        }
    }

    /**
     * Returns a fixed redacted representation that does not expose resource data or write-only passwords.
     *
     * @return fixed redacted bulk request representation
     */
    @Override
    public String toString() {
        return "BulkRequest[redacted]";
    }

    /**
     * Idempotently closes every typed data payload so inbound User passwords are erased.
     */
    @Override
    public void close() {
        for (Operation operation : operations) {
            final Data payload = operation.data().getOrNull();
            if (payload != null) {
                payload.close();
            }
        }
    }

    /**
     * Seals bulk operation data to typed standard Resource or PatchOp payloads.
     *
     * @author Kimi Liu
     */
    public sealed interface Data extends AutoCloseable permits ResourceData, PatchData {

        /**
         * Releases sensitive data owned by this payload.
         */
        @Override
        void close();

    }

    /**
     * Models one RFC 7644 bulk resource operation.
     *
     * @param method  standard POST, PUT, PATCH, or DELETE operation method
     * @param bulkId  request-local identifier required for POST resource creation
     * @param target  typed SCIM resource collection or individual-resource target
     * @param version complete HTTP entity-tag used as the operation If-Match value
     * @param data    typed resource or PatchOp data required by body-bearing methods
     * @author Kimi Liu
     */
    public record Operation(Http.Method method, Optional<String> bulkId, ResourceTarget target,
            Optional<String> version, Optional<Data> data) {

        /**
         * Enforces method-specific bulkId, target, version, and data requirements.
         *
         * @throws IllegalArgumentException if a required value or optional container is {@code null}
         * @throws ValidateException        if a method, target, entity-tag, bulkId, or data combination is invalid
         */
        public Operation {
            method = Assert.notNull(method, "SCIM bulk operation method must not be null");
            if (method != Http.Method.POST && method != Http.Method.PUT && method != Http.Method.PATCH
                    && method != Http.Method.DELETE) {
                throw new ValidateException("SCIM bulk operation method must be POST, PUT, PATCH, or DELETE");
            }
            bulkId = optionalText(bulkId, "SCIM bulk operation bulkId");
            target = Assert.notNull(target, "SCIM bulk operation target must not be null");
            version = optionalText(version, "SCIM bulk operation version");
            if (!version.isEmpty()) {
                validateEntityTag(version.getOrThrow());
            }
            Assert.notNull(data, "SCIM bulk operation data container must not be null");
            data = Optional.ofNullable(data.getOrNull());
            if (method == Http.Method.POST
                    && (bulkId.isEmpty() || data.isEmpty() || !(data.getOrThrow() instanceof ResourceData))) {
                throw new ValidateException("SCIM bulk POST requires bulkId and ResourceData");
            }
            if (method == Http.Method.POST && target.resourceId().isPresent()) {
                throw new ValidateException("SCIM bulk POST target must identify a resource collection");
            }
            if (method != Http.Method.POST && target.resourceId().isEmpty()) {
                throw new ValidateException("SCIM bulk PUT, PATCH, and DELETE target must identify one resource");
            }
            if (method == Http.Method.PUT && (data.isEmpty() || !(data.getOrThrow() instanceof ResourceData))) {
                throw new ValidateException("SCIM bulk PUT requires ResourceData");
            }
            if (method == Http.Method.PATCH && (data.isEmpty() || !(data.getOrThrow() instanceof PatchData))) {
                throw new ValidateException("SCIM bulk PATCH requires PatchData");
            }
            if (method == Http.Method.DELETE && !data.isEmpty()) {
                throw new ValidateException("SCIM bulk DELETE prohibits data");
            }
        }

        /**
         * Returns a fixed redacted representation that does not expose resource data.
         *
         * @return fixed redacted operation representation
         */
        @Override
        public String toString() {
            return "Operation[method=" + method + ", redacted]";
        }

    }

    /**
     * Carries typed User or Group data for a bulk POST or PUT operation.
     *
     * @param resource standard User or Group resource
     * @author Kimi Liu
     */
    public record ResourceData(Resource resource) implements Data {

        /**
         * Restricts writable bulk resource data to User and Group.
         *
         * @throws IllegalArgumentException if {@code resource} is {@code null}
         * @throws ValidateException        if the value is a discovery resource
         */
        public ResourceData {
            resource = Assert.notNull(resource, "SCIM bulk resource data must not be null");
            if (!(resource instanceof User) && !(resource instanceof Group)) {
                throw new ValidateException("SCIM bulk resource data must be User or Group");
            }
        }

        /**
         * Erases an inbound User password lease; Group has no sensitive lease.
         */
        @Override
        public void close() {
            if (resource instanceof User user) {
                user.close();
            }
        }

    }

    /**
     * Carries a typed PatchOp for a bulk PATCH operation.
     *
     * @param patch standard PatchOp
     * @author Kimi Liu
     */
    public record PatchData(PatchOp patch) implements Data {

        /**
         * Requires a non-null PatchOp.
         */
        public PatchData {
            patch = Assert.notNull(patch, "SCIM bulk patch data must not be null");
        }

        /**
         * Closes the PatchOp so a password SecretLease is erased when present.
         */
        @Override
        public void close() {
            patch.close();
        }

    }

}
