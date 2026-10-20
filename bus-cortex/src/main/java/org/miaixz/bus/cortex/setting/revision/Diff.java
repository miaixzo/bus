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
package org.miaixz.bus.cortex.setting.revision;

import java.util.*;
import java.util.function.Function;

import org.miaixz.bus.cortex.setting.item.ItemExposure;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;
import org.miaixz.bus.extra.json.JsonSchema;

/**
 * Key-level release difference calculated from immutable revisions. Host parsers normalize JSON, YAML, properties, or
 * text into a value map before comparison; parse failures must never become empty maps. JSON Pointer paths preserve
 * dotted keys, nested arrays, null values, and structural boundaries.
 *
 * @author Kimi Liu
 */
public final class Diff {

    /**
     * Describes how one normalized configuration path changed.
     *
     * @author Kimi Liu
     */
    public enum Type {
        /**
         * Path exists only in the target revision.
         */
        ADD,
        /**
         * Path exists in both revisions with different values.
         */
        MODIFY,
        /**
         * Path exists only in the baseline revision.
         */
        DELETE
    }

    /**
     * Escaped JSON Pointer path of the changed value.
     */
    private final String key;
    /**
     * Kind of change at the path.
     */
    private final Type type;
    /**
     * Baseline value, possibly redacted.
     */
    private final Object before;
    /**
     * Target value, possibly redacted.
     */
    private final Object after;

    /**
     * Creates one immutable path-level difference.
     *
     * @param key    JSON Pointer path
     * @param type   change type
     * @param before baseline value
     * @param after  target value
     */
    private Diff(String key, Type type, Object before, Object after) {
        this.key = key;
        this.type = type;
        this.before = before;
        this.after = after;
    }

    /**
     * Returns the escaped JSON Pointer path.
     *
     * @return escaped JSON Pointer path
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the change type.
     *
     * @return change type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the baseline value, possibly redacted.
     *
     * @return baseline value, possibly redacted
     */
    public Object getBefore() {
        return before;
    }

    /**
     * Returns the target value, possibly redacted.
     *
     * @return target value, possibly redacted
     */
    public Object getAfter() {
        return after;
    }

    /**
     * Compares one item's revisions. Protected contents are never passed to a decoder or exposed in the result. Their
     * immutable stored checksums determine whether a masked change exists.
     *
     * @param before      optional baseline immutable revision
     * @param after       optional target immutable revision
     * @param decoder     host decoder for unprotected immutable contents
     * @param secretPaths exact protected paths that must remain masked
     * @return immutable path differences using the shared scalar placeholder
     */
    public static List<Diff> compare(
            Revision before,
            Revision after,
            Function<Revision, Map<String, ?>> decoder,
            Set<String> secretPaths) {
        if (before == null && after == null) {
            return List.of();
        }
        Revision coordinate = before == null ? after : before;
        ItemNormalizer.requireTenant(coordinate.getTenant_id());
        if (coordinate.getItem_id() == null || coordinate.getItem_id().isBlank()) {
            throw new IllegalArgumentException("Item identifier is required");
        }
        if (before != null && after != null && (!Objects.equals(before.getTenant_id(), after.getTenant_id())
                || !Objects.equals(before.getItem_id(), after.getItem_id()))) {
            throw new IllegalArgumentException("Difference revisions must belong to the same tenant and item");
        }
        boolean left = exists(before);
        boolean right = exists(after);
        if (protectedContent(before) || protectedContent(after)) {
            if (left && (before.getChecksum() == null || before.getChecksum().isBlank())
                    || right && (after.getChecksum() == null || after.getChecksum().isBlank())) {
                throw new IllegalArgumentException("Protected revision comparison requires stored checksums");
            }
            if (!left && !right || left && right && Objects.equals(before.getChecksum(), after.getChecksum())) {
                return List.of();
            }
            return List.of(
                    new Diff("/", !left ? Type.ADD : !right ? Type.DELETE : Type.MODIFY,
                            left ? SecretMasker.MASK : null, right ? SecretMasker.MASK : null));
        }
        Objects.requireNonNull(decoder, "Revision decoder is required");
        return compare(
                left ? decoded(decoder.apply(before)) : Map.of(),
                right ? decoded(decoder.apply(after)) : Map.of(),
                secretPaths);
    }

    /**
     * Requires a decoder to return an explicit normalized value map.
     *
     * @param values decoded values
     * @return non-null decoded map
     */
    private static Map<String, ?> decoded(Map<String, ?> values) {
        if (values == null) {
            throw new IllegalArgumentException("Revision parser did not return a value map");
        }
        return values;
    }

    /**
     * Tests whether a revision represents materialized content rather than a tombstone.
     *
     * @param revision revision to inspect
     * @return {@code true} when content exists
     */
    private static boolean exists(Revision revision) {
        return revision != null && Revision.Operation.valueOf(revision.getOperation()) != Revision.Operation.DELETE;
    }

    /**
     * Tests whether revision content must be compared without decoding its values.
     *
     * @param revision revision to inspect
     * @return {@code true} for encrypted or secret content
     */
    private static boolean protectedContent(Revision revision) {
        return revision != null && (ItemNormalizer.isEncryptedFlagEnabled(revision.getEncrypted())
                || ItemExposure.SECRET.name().equals(revision.getExposure()));
    }

    /**
     * Compares normalized value maps and redacts explicit paths and commonly sensitive key names.
     *
     * @param before      normalized baseline values
     * @param after       normalized target values
     * @param secretPaths exact protected paths whose descendants remain masked
     * @return immutable path differences using the shared scalar placeholder
     */
    public static List<Diff> compare(Map<String, ?> before, Map<String, ?> after, Set<String> secretPaths) {
        Objects.requireNonNull(before, "Baseline values are required");
        Objects.requireNonNull(after, "Target values are required");
        Map<String, Object> left = new TreeMap<>();
        Map<String, Object> right = new TreeMap<>();
        before.forEach(
                (key, value) -> flatten(
                        JsonSchema.pointer("", Objects.requireNonNull(key, "Configuration key")),
                        value,
                        left));
        after.forEach(
                (key, value) -> flatten(
                        JsonSchema.pointer("", Objects.requireNonNull(key, "Configuration key")),
                        value,
                        right));
        Set<String> keys = new TreeSet<>(left.keySet());
        keys.addAll(right.keySet());
        List<Diff> result = new ArrayList<>();
        for (String key : keys) {
            boolean hasLeft = left.containsKey(key);
            boolean hasRight = right.containsKey(key);
            if (hasLeft && hasRight && Objects.equals(left.get(key), right.get(key))) {
                continue;
            }
            boolean secret = key.toLowerCase(Locale.ROOT)
                    .matches(".*(password|passwd|secret|token|credential|private[_-]?key).*")
                    || secretPaths != null
                            && secretPaths.stream().anyMatch(path -> key.equals(path) || key.startsWith(path + "/"));
            result.add(
                    new Diff(key, !hasLeft ? Type.ADD : !hasRight ? Type.DELETE : Type.MODIFY,
                            hasLeft ? secret ? SecretMasker.MASK : left.get(key) : null,
                            hasRight ? secret ? SecretMasker.MASK : right.get(key) : null));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Flattens nested maps and lists into JSON Pointer paths while preserving empty containers.
     *
     * @param path   current JSON Pointer path
     * @param value  value at the path
     * @param result flattened destination map
     */
    private static void flatten(String path, Object value, Map<String, Object> result) {
        if (value instanceof Map<?, ?> map && !map.isEmpty()) {
            map.forEach((key, child) -> flatten(JsonSchema.pointer(path, String.valueOf(key)), child, result));
        } else if (value instanceof List<?> list && !list.isEmpty()) {
            for (int index = 0; index < list.size(); index++) {
                flatten(JsonSchema.pointer(path, Integer.toString(index)), list.get(index), result);
            }
        } else {
            result.put(path, value instanceof Map<?, ?> ? Map.of() : value instanceof List<?> ? List.of() : value);
        }
    }

}
