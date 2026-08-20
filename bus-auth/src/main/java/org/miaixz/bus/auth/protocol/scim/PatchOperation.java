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

import java.util.Locale;

import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Models one ordered RFC 7644 PATCH operation with its standard operation, path, and value semantics.
 *
 * @param op    standard add, remove, or replace operation
 * @param path  RFC 7644 patch path when the operation targets selected attributes
 * @param value operation value, required for add and replace and prohibited for remove
 * @author Kimi Liu
 */
public record PatchOperation(Op op, Optional<Path> path, Optional<Value> value) implements AutoCloseable {

    /**
     * Enforces operation-specific path and value requirements.
     *
     * @throws IllegalArgumentException if an operation or optional container is {@code null}
     * @throws ValidateException        if the path syntax or operation-specific value shape is invalid
     */
    public PatchOperation {
        op = Assert.notNull(op, "SCIM patch operation op must not be null");
        Assert.notNull(path, "SCIM patch operation path container must not be null");
        path = Optional.ofNullable(path.getOrNull());
        Assert.notNull(value, "SCIM patch operation value container must not be null");
        value = Optional.ofNullable(value.getOrNull());
        if (!path.isEmpty()) {
            Assert.notNull(path.getOrThrow(), "SCIM patch operation path must not be null");
        }
        if (op == Op.REMOVE) {
            if (path.isEmpty() || !value.isEmpty()) {
                throw new ValidateException("SCIM remove operation requires path and prohibits value");
            }
        } else {
            if (value.isEmpty()) {
                throw new ValidateException("SCIM add and replace operations require value");
            }
            if (path.isEmpty() && (!(value.getOrThrow() instanceof JsonData json)
                    || !(json.value() instanceof JsonValue.ObjectValue object))) {
                throw new ValidateException("SCIM add or replace without path requires an object value");
            }
            if (path.isEmpty() && containsPassword(((JsonValue.ObjectValue) ((JsonData) value.getOrThrow()).value()))) {
                throw new ValidateException("SCIM pathless patch value must not contain password");
            }
            final boolean password = !path.isEmpty() && passwordPath(path.getOrThrow());
            if (password != (value.getOrThrow() instanceof SecretData)) {
                throw new ValidateException("SCIM password path requires SecretData and other paths prohibit it");
            }
        }
    }

    /**
     * Tests whether a standard patch path directly identifies the core password attribute.
     *
     * @param value validated patch path
     * @return whether the path addresses password
     */
    private static boolean passwordPath(final Path value) {
        final String normalized = value.value().toLowerCase(Locale.ROOT);
        return "password".equals(normalized) || normalized.endsWith(":password");
    }

    /**
     * Tests whether a pathless object contains a case-insensitive top-level password member.
     *
     * @param value pathless patch object
     * @return whether password material is present
     */
    private static boolean containsPassword(final JsonValue.ObjectValue value) {
        return value.values().keySet().stream().anyMatch(name -> "password".equalsIgnoreCase(name));
    }

    /**
     * Idempotently closes a present patch value so password material is erased.
     */
    @Override
    public void close() {
        final Value payload = value.getOrNull();
        if (payload != null) {
            payload.close();
        }
    }

    /**
     * Returns a redacted representation that never exposes a patch value.
     *
     * @return redacted operation representation
     */
    @Override
    public String toString() {
        return "PatchOperation[op=" + op + ", path=" + path + ", value=<redacted>]";
    }

    /**
     * Enumerates the case-insensitive PATCH operation names with their canonical lowercase wire values.
     *
     * @author Kimi Liu
     */
    public enum Op {

        /**
         * Adds a value to the selected attribute or resource.
         */
        ADD("add"),

        /**
         * Removes the value selected by the required path.
         */
        REMOVE("remove"),

        /**
         * Replaces the selected attribute value or complete mutable resource attributes.
         */
        REPLACE("replace");

        /**
         * Canonical lowercase RFC 7644 wire value.
         */
        private final String value;

        /**
         * Associates an operation constant with its canonical wire value.
         *
         * @param value canonical lowercase wire value
         */
        Op(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical lowercase RFC 7644 wire value.
         *
         * @return canonical operation value
         */
        public String value() {
            return value;
        }

    }

    /**
     * Seals patch values to ordinary immutable JSON or an erasable password lease.
     *
     * @author Kimi Liu
     */
    public sealed interface Value extends AutoCloseable permits JsonData, SecretData {

        /**
         * Releases sensitive state owned by this value.
         */
        @Override
        void close();

    }

    /**
     * Retains one RFC 7644 PATCH path, including an optional valuePath filter and trailing sub-attribute.
     *
     * @param value exact path lexical value
     * @author Kimi Liu
     */
    public record Path(String value) {

        /**
         * Validates the structural PATCH-path grammar without resolving a resource schema.
         *
         * @throws ValidateException if the path is malformed
         */
        public Path {
            value = Assert.notBlank(value, "SCIM PATCH path must not be blank");
            final int open = value.indexOf('[');
            if (open < 0) {
                Filter.AttributePath.parse(value);
            } else {
                final int close = value.lastIndexOf(']');
                if (open == 0 || close <= open + 1 || value.indexOf('[', open + 1) >= 0
                        || value.indexOf(']', close + 1) >= 0) {
                    throw new ValidateException("SCIM PATCH valuePath has invalid bracket structure");
                }
                Filter.AttributePath.parse(value.substring(0, open));
                Assert.notBlank(value.substring(open + 1, close), "SCIM PATCH valuePath filter must not be blank");
                if (close + 1 < value.length()) {
                    if (value.charAt(close + 1) != '.') {
                        throw new ValidateException("SCIM PATCH valuePath suffix must be a sub-attribute");
                    }
                    Filter.AttributePath.parse(value.substring(close + 2));
                }
            }
        }

    }

    /**
     * Carries one non-password JSON patch value.
     *
     * @param value immutable provider-neutral JSON value
     * @author Kimi Liu
     */
    public record JsonData(JsonValue value) implements Value {

        /**
         * Requires a non-null JSON value.
         */
        public JsonData {
            value = Assert.notNull(value, "SCIM patch JSON value must not be null");
        }

        /**
         * Performs no action because JsonValue is immutable and contains no password.
         */
        @Override
        public void close() {
            // Ordinary patch values contain no leased secret material.
        }

    }

    /**
     * Owns one write-only password value for the lifetime of a patch operation.
     *
     * @param secret erasable password lease
     * @author Kimi Liu
     */
    public record SecretData(SecretLease secret) implements Value {

        /**
         * Requires a non-null password lease.
         */
        public SecretData {
            secret = Assert.notNull(secret, "SCIM patch password lease must not be null");
        }

        /**
         * Erases the owned password lease.
         */
        @Override
        public void close() {
            secret.close();
        }

        /**
         * Returns a fixed representation that does not disclose password material.
         */
        @Override
        public String toString() {
            return "SecretData[redacted]";
        }

    }

}
