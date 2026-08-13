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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Applies a bounded SCIM PatchOp document to an independent resource snapshot. Every operation is evaluated against the
 * preceding operation result, while the source resource remains unchanged if any operation fails.
 *
 * @author Kimi Liu
 */
public final class ScimPatch {

    /**
     * Maximum operations in one PatchOp document.
     */
    public static final int MAXIMUM_OPERATIONS = Normal._128;

    /**
     * Prevents construction of the stateless patch engine.
     */
    private ScimPatch() {
        // No initialization required.
    }

    /**
     * Applies all operations atomically to one resource snapshot.
     *
     * @param source     source resource
     * @param operations ordered operations
     * @return patched immutable resource
     * @throws ValidateException if the source, operation list, or any selected path is invalid
     */
    public static ScimResource apply(final ScimResource source, final List<Operation> operations) {
        final ScimResource resource = Assert
                .notNull(source, () -> new ValidateException("SCIM PATCH source must not be null"));
        final List<Operation> patch = List.copyOf(
                Assert.notNull(operations, () -> new ValidateException("SCIM PATCH operations must not be null")));
        Assert.isTrue(
                !patch.isEmpty() && patch.size() <= MAXIMUM_OPERATIONS,
                () -> new ValidateException("SCIM PATCH operation count is invalid"));
        Map<String, Object> attributes = mutable(resource.attributes());
        for (final Operation operation : patch) {
            attributes = execute(attributes, operation);
        }
        return resource.withAttributes(attributes);
    }

    /**
     * Executes one operation against a mutable working snapshot.
     *
     * @param source    working attributes
     * @param operation operation
     * @return updated attributes
     */
    static Map<String, Object> execute(final Map<String, Object> source, final Operation operation) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>(source);
        if (operation.path() == null) {
            if (!(operation.value() instanceof Map<?, ?> values)) {
                throw new ValidateException("SCIM pathless PATCH value must be an object");
            }
            values.forEach((key, value) -> {
                if (!(key instanceof String name)) {
                    throw new ValidateException("SCIM pathless PATCH attribute name is invalid");
                }
                final String stored = key(result, name);
                if (operation.type() == Type.ADD && stored != null) {
                    result.put(stored, append(result.get(stored), value));
                } else {
                    result.put(stored == null ? name : stored, value);
                }
            });
            return result;
        }
        final Path path = parse(operation.path());
        final String key = key(result, path.attribute());
        if (path.selector() == null && path.subAttribute() == null) {
            switch (operation.type()) {
                case ADD -> result.put(
                        key == null ? path.attribute() : key,
                        key == null ? operation.value() : append(result.get(key), operation.value()));
                case REPLACE -> result.put(key == null ? path.attribute() : key, operation.value());
                case REMOVE -> {
                    if (key == null) {
                        throw new ValidateException("SCIM PATCH path selected no target");
                    }
                    result.remove(key);
                }
            }
            return result;
        }
        if (key == null || !(result.get(key) instanceof List<?> values)) {
            throw new ValidateException("SCIM PATCH value path selected no target");
        }
        final ArrayList<Object> updated = new ArrayList<>();
        boolean matched = false;
        for (final Object item : values) {
            if (!(item instanceof Map<?, ?> map)) {
                updated.add(item);
                continue;
            }
            final LinkedHashMap<String, Object> complex = stringMap(map);
            final boolean selected = path.selector() == null || ScimFilter.evaluate(path.selector(), complex);
            if (!selected) {
                updated.add(complex);
                continue;
            }
            matched = true;
            if (path.subAttribute() == null) {
                if (operation.type() != Type.REMOVE) {
                    updated.add(operation.type() == Type.ADD ? append(complex, operation.value()) : operation.value());
                }
            } else {
                final String subKey = key(complex, path.subAttribute());
                switch (operation.type()) {
                    case ADD -> complex.put(
                            subKey == null ? path.subAttribute() : subKey,
                            subKey == null ? operation.value() : append(complex.get(subKey), operation.value()));
                    case REPLACE -> complex.put(subKey == null ? path.subAttribute() : subKey, operation.value());
                    case REMOVE -> {
                        if (subKey == null) {
                            throw new ValidateException("SCIM PATCH sub-attribute selected no target");
                        }
                        complex.remove(subKey);
                    }
                }
                updated.add(complex);
            }
        }
        if (!matched) {
            throw new ValidateException("SCIM PATCH path selected no target");
        }
        result.put(key, updated);
        return result;
    }

    /**
     * Parses one PATCH path with an optional value-path selector and sub-attribute.
     *
     * @param source path text
     * @return parsed path
     * @throws ValidateException if the path or its value-path selector is malformed or oversized
     */
    static Path parse(final String source) {
        final String value = Assert.notBlank(source, () -> new ValidateException("SCIM PATCH path must not be blank"));
        Assert.isTrue(
                value.length() <= ScimFilterParser.MAXIMUM_CHARACTERS,
                () -> new ValidateException("SCIM PATCH path exceeds its limit"));
        final int open = value.indexOf('[');
        if (open < Normal._0) {
            final int dot = value.indexOf('.');
            final String attribute = dot < Normal._0 ? value : value.substring(Normal._0, dot);
            final String subAttribute = dot < Normal._0 ? null : value.substring(dot + Normal._1);
            return new Path(ScimFilter.path(attribute), null,
                    subAttribute == null ? null : ScimFilter.path(subAttribute));
        }
        final int close = value.lastIndexOf(']');
        Assert.isTrue(
                open > Normal._0 && close > open && value.indexOf('[', open + Normal._1) < Normal._0,
                () -> new ValidateException("SCIM PATCH value path is invalid"));
        final String tail = value.substring(close + Normal._1);
        Assert.isTrue(
                tail.isEmpty() || tail.charAt(Normal._0) == '.',
                () -> new ValidateException("SCIM PATCH value-path suffix is invalid"));
        return new Path(ScimFilter.path(value.substring(Normal._0, open)),
                ScimFilterParser.parse(value.substring(open + Normal._1, close)),
                tail.isEmpty() ? null : ScimFilter.path(tail.substring(Normal._1)));
    }

    /**
     * Appends JSON values using SCIM add semantics.
     *
     * @param current  current value
     * @param addition added value
     * @return combined value
     */
    static Object append(final Object current, final Object addition) {
        if (current instanceof List<?> left) {
            final ArrayList<Object> result = new ArrayList<>(left);
            if (addition instanceof List<?> right) {
                result.addAll(right);
            } else {
                result.add(addition);
            }
            return result;
        }
        if (current instanceof Map<?, ?> left && addition instanceof Map<?, ?> right) {
            final LinkedHashMap<String, Object> result = stringMap(left);
            result.putAll(stringMap(right));
            return result;
        }
        return addition;
    }

    /**
     * Finds one case-insensitive SCIM object key.
     *
     * @param values object
     * @param name   requested name
     * @return stored key or {@code null}
     */
    static String key(final Map<String, ?> values, final String name) {
        return values.keySet().stream().filter(value -> value.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Creates a mutable recursive top-level snapshot.
     *
     * @param source source attributes
     * @return mutable attributes
     * @throws ValidateException if an attribute value lies outside the SCIM JSON domain
     */
    static Map<String, Object> mutable(final Map<String, Object> source) {
        return stringMap(source);
    }

    /**
     * Converts a map with validated string keys.
     *
     * @param source source map
     * @return mutable string-keyed map
     * @throws ValidateException if any key is not a string or any value lies outside the SCIM JSON domain
     */
    static LinkedHashMap<String, Object> stringMap(final Map<?, ?> source) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (!(key instanceof String name)) {
                throw new ValidateException("SCIM complex attribute key is invalid");
            }
            result.put(name, snapshot(value));
        });
        return result;
    }

    /**
     * Recursively copies one SCIM JSON value into mutable map/list containers.
     *
     * @param source source value
     * @return recursively independent value
     * @throws ValidateException if the value lies outside the SCIM JSON domain
     */
    private static Object snapshot(final Object source) {
        if (source == null || source instanceof String || source instanceof Boolean || source instanceof BigDecimal) {
            return source;
        }
        if (source instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (source instanceof Map<?, ?> map) {
            return stringMap(map);
        }
        if (source instanceof List<?> list) {
            final ArrayList<Object> result = new ArrayList<>(list.size());
            list.forEach(value -> result.add(snapshot(value)));
            return result;
        }
        throw new ValidateException("SCIM PATCH value type is unsupported");
    }

    /**
     * Recursively freezes mutable patch snapshots before exposing them.
     *
     * @param source mutable snapshot
     * @return immutable JSON value
     */
    private static Object immutable(final Object source) {
        if (source instanceof Map<?, ?> map) {
            final LinkedHashMap<String, Object> result = stringMap(map);
            result.replaceAll((key, value) -> immutable(value));
            return java.util.Collections.unmodifiableMap(result);
        }
        if (source instanceof List<?> list) {
            return list.stream().map(ScimPatch::immutable).toList();
        }
        return source;
    }

    /**
     * Supported SCIM PATCH operation names.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * Adds an attribute or value.
         */
        ADD,

        /**
         * Removes a selected attribute or value.
         */
        REMOVE,

        /**
         * Replaces a selected attribute or value.
         */
        REPLACE
    }

    /**
     * Immutable PATCH operation.
     *
     * @param type  operation type
     * @param path  optional attribute path or value-path selector
     * @param value optional JSON value
     * @author Kimi Liu
     */
    public record Operation(Type type, String path, Object value) {

        /**
         * Validates one operation.
         *
         * @param type  operation type
         * @param path  operation path
         * @param value operation value
         */
        public Operation {
            type = Assert.notNull(type, () -> new ValidateException("SCIM PATCH operation type must not be null"));
            path = path == null || path.isBlank() ? null : path;
            Assert.isTrue(
                    type != Type.REMOVE || path != null,
                    () -> new ValidateException("SCIM remove operation requires a path"));
            Assert.isTrue(
                    type == Type.REMOVE || value != null,
                    () -> new ValidateException("SCIM add or replace operation requires a value"));
            value = snapshot(value);
            if (path != null) {
                parse(path);
            }
        }

        /**
         * Returns an independent immutable view of the operation value.
         *
         * @return recursively snapshotted JSON value
         */
        @Override
        public Object value() {
            return immutable(snapshot(value));
        }
    }

    /**
     * Parsed PATCH path.
     *
     * @param attribute    top-level attribute
     * @param selector     optional value-path selector
     * @param subAttribute optional complex sub-attribute
     * @author Kimi Liu
     */
    record Path(String attribute, ScimFilter selector, String subAttribute) {
    }

}
