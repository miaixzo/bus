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

/**
 * Key-level release difference calculated from immutable revisions. Host parsers normalize JSON, YAML, properties, or
 * text into a value map before comparison; parse failures must never become empty maps. JSON Pointer paths preserve
 * dotted keys, nested arrays, null values, and structural boundaries.
 */
public final class Diff {

    public enum Type {
        ADD, MODIFY, DELETE
    }

    private final String key;
    private final Type type;
    private final Object before;
    private final Object after;

    private Diff(String key, Type type, Object before, Object after) {
        this.key = key;
        this.type = type;
        this.before = before;
        this.after = after;
    }

    public String getKey() {
        return key;
    }

    public Type getType() {
        return type;
    }

    public Object getBefore() {
        return before;
    }

    public Object getAfter() {
        return after;
    }

    /**
     * Compares one item's revisions. Protected contents are never passed to a decoder or exposed in the result. Their
     * immutable stored checksums determine whether a masked change exists.
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
                    new Diff("/", !left ? Type.ADD : !right ? Type.DELETE : Type.MODIFY, left ? "****" : null,
                            right ? "****" : null));
        }
        Objects.requireNonNull(decoder, "Revision decoder is required");
        return compare(
                left ? decoded(decoder.apply(before)) : Map.of(),
                right ? decoded(decoder.apply(after)) : Map.of(),
                secretPaths);
    }

    private static Map<String, ?> decoded(Map<String, ?> values) {
        if (values == null) {
            throw new IllegalArgumentException("Revision parser did not return a value map");
        }
        return values;
    }

    private static boolean exists(Revision revision) {
        return revision != null && Revision.Operation.valueOf(revision.getOperation()) != Revision.Operation.DELETE;
    }

    private static boolean protectedContent(Revision revision) {
        return revision != null && (ItemNormalizer.isEncryptedFlagEnabled(revision.getEncrypted())
                || ItemExposure.SECRET.name().equals(revision.getExposure()));
    }

    /** Compares normalized value maps and redacts explicit paths and commonly sensitive key names. */
    public static List<Diff> compare(Map<String, ?> before, Map<String, ?> after, Set<String> secretPaths) {
        Objects.requireNonNull(before, "Baseline values are required");
        Objects.requireNonNull(after, "Target values are required");
        Map<String, Object> left = new TreeMap<>();
        Map<String, Object> right = new TreeMap<>();
        before.forEach((key, value) -> flatten("/" + escape(key), value, left));
        after.forEach((key, value) -> flatten("/" + escape(key), value, right));
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
                            hasLeft ? secret ? "****" : left.get(key) : null,
                            hasRight ? secret ? "****" : right.get(key) : null));
        }
        return Collections.unmodifiableList(result);
    }

    private static String escape(String key) {
        return Objects.requireNonNull(key, "Configuration key").replace("~", "~0").replace("/", "~1");
    }

    private static void flatten(String path, Object value, Map<String, Object> result) {
        if (value instanceof Map<?, ?> map && !map.isEmpty()) {
            map.forEach((key, child) -> flatten(path + "/" + escape(String.valueOf(key)), child, result));
        } else if (value instanceof List<?> list && !list.isEmpty()) {
            for (int index = 0; index < list.size(); index++) {
                flatten(path + "/" + index, list.get(index), result);
            }
        } else {
            result.put(path, value instanceof Map<?, ?> ? Map.of() : value instanceof List<?> ? List.of() : value);
        }
    }
}
