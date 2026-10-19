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
package org.miaixz.bus.cortex.setting.item;

import java.util.*;

import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;
import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Owns canonical per-key metadata, safe projections and explicit protected-key edits for configuration sources.
 * Persistence, codecs, identity and transactions remain in their existing host services. The management member is
 * opaque host metadata: the host constructs it from validated state and verifies it at persistence boundaries.
 *
 * @author Kimi Liu
 */
public final class ItemMetadata {

    /**
     * Fixed extension-envelope version shared by source and immutable revision metadata. Hosts reuse this identifier;
     * protocol evolution must preserve existing immutable snapshots.
     */
    public static final String FORMAT = "services-extension-v1";
    /**
     * Fixed protocol members, including framework provenance, protection and opaque host management metadata. Accepting
     * management here does not validate its fields, identity or trustworthiness.
     */
    private static final Set<String> SERVICES = Set
            .of("management", "keys", "defaults", "sources", "references", "protection");
    /**
     * Fixed immutable provenance and protection whitelist shared by framework checks and host input handling. Opaque
     * management fields remain the host's responsibility and are reconstructed from validated state.
     */
    public static final Set<String> RESERVED = Set.of("sources", "references", "protection");
    /**
     * Only supported editable attributes on one key.
     */
    private static final Set<String> ATTRIBUTES = Set.of("sensitive", "description", "schema");
    /**
     * Compatibility alias for the existing whole-source masker's fixed placeholder. Protected replacement checks never
     * treat this safe-view marker as a new secret value.
     */
    public static final String MASK = SecretMasker.MASK;

    /**
     * Prevents instantiation of the shared metadata contract.
     */
    private ItemMetadata() {
        // No initialization required.
    }

    /**
     * Describes one exact key without duplicating its value or inferring a type.
     *
     * @param sensitive   whether values at this exact pointer require protection
     * @param description optional plain description, normalized to an empty string
     * @param schema      optional additional schema rooted at this key's value
     * @author Kimi Liu
     */
    public record Key(boolean sensitive, String description, JsonValue schema) {

        /**
         * Normalizes an optional description while retaining the exact immutable schema.
         *
         * @param sensitive   whether the key is protected
         * @param description optional description, limited to the existing scalar bound
         * @param schema      optional additional schema
         */
        public Key {
            description = description == null ? "" : description;
            if (description.codePointCount(0, description.length()) > 255)
                throw invalid("description");
            if (schema != null && !(schema instanceof JsonValue.ObjectValue)
                    && !(schema instanceof JsonValue.BooleanValue))
                throw invalid("schema");
        }
    }

    /**
     * Returns an immutable protected edit and its matching key metadata.
     *
     * @param value edited source tree
     * @param keys  key metadata after explicit protected deletions
     * @author Kimi Liu
     */
    public record Edit(JsonValue value, Map<String, Key> keys) {

        /**
         * Freezes the returned metadata instead of retaining a caller's mutable map.
         *
         * @param value immutable edited source
         * @param keys  matching key metadata
         */
        public Edit {
            value = Objects.requireNonNull(value);
            keys = Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNull(keys)));
        }
    }

    /**
     * Checks the fixed services envelope shape without interpreting opaque host management fields. The host must
     * construct and verify management separately; its presence confers no trust.
     *
     * @param extension parsed plaintext extension, or null for a new source
     * @return immutable service metadata members, or an empty map
     */
    public static Map<String, JsonValue> services(JsonValue extension) {
        if (extension == null)
            return Map.of();
        if (!(extension instanceof JsonValue.ObjectValue root))
            throw invalid("extension");
        JsonValue format = root.values().get("format");
        if (format != null && (!(format instanceof JsonValue.StringValue version) || !FORMAT.equals(version.value()))) {
            throw invalid("format");
        }
        JsonValue services = root.values().get("services");
        if (services == null)
            return Map.of();
        if (!(services instanceof JsonValue.ObjectValue object) || !SERVICES.containsAll(object.values().keySet())) {
            throw invalid("services");
        }
        JsonValue protection = object.values().get("protection");
        if (protection != null && (!(protection instanceof JsonValue.StringValue mode)
                || !Set.of("WHOLE", "KEYS").contains(mode.value())))
            throw invalid("protection");
        return object.values();
    }

    /**
     * Rejects the three framework-owned provenance and protection members in editable metadata. This is not host
     * management validation or authorization: accepted management must be discarded or reconstructed from validated
     * host state before persistence.
     *
     * @param extension parsed client extension
     */
    public static void requireEditable(JsonValue extension) {
        if (services(extension).keySet().stream().anyMatch(RESERVED::contains))
            throw invalid("reserved");
    }

    /**
     * Reads canonical key metadata and validates its schemas before source values are evaluated. Protected
     * ancestor/descendant pairs are rejected so one edit never ambiguously targets two secret owners.
     *
     * @param extension    parsed plaintext extension
     * @param maximumDepth existing parser/evaluation depth bound
     * @param maximumSteps existing node/work bound
     * @return immutable metadata sorted by canonical JSON Pointer
     */
    public static Map<String, Key> keys(JsonValue extension, int maximumDepth, long maximumSteps) {
        if (extension != null)
            JsonSchema.validate(new JsonValue.BooleanValue(true), extension, maximumDepth, maximumSteps);
        JsonValue source = services(extension).get("keys");
        if (source == null)
            return Map.of();
        if (!(source instanceof JsonValue.ObjectValue object))
            throw invalid("keys");
        Map<String, Key> result = new TreeMap<>();
        for (var entry : object.values().entrySet()) {
            path(entry.getKey());
            if (!(entry.getValue() instanceof JsonValue.ObjectValue attributes)
                    || !ATTRIBUTES.containsAll(attributes.values().keySet()))
                throw invalid("keyAttributes");
            JsonValue flag = attributes.values().get("sensitive");
            if (flag != null && !(flag instanceof JsonValue.BooleanValue))
                throw invalid("sensitive");
            JsonValue description = attributes.values().get("description");
            if (description != null && (!(description instanceof JsonValue.StringValue text)
                    || text.value().codePointCount(0, text.value().length()) > 255))
                throw invalid("description");
            JsonValue schema = attributes.values().get("schema");
            if (schema != null)
                JsonSchema.requireSchema(schema, maximumDepth, maximumSteps);
            result.put(
                    entry.getKey(),
                    new Key(flag instanceof JsonValue.BooleanValue value && value.value(),
                            description instanceof JsonValue.StringValue value ? value.value() : "", schema));
        }
        for (var entry : result.entrySet()) {
            if (!entry.getValue().sensitive())
                continue;
            String parent = entry.getKey();
            while (parent.lastIndexOf('/') > 0) {
                parent = parent.substring(0, parent.lastIndexOf('/'));
                if (result.containsKey(parent) && result.get(parent).sensitive())
                    throw invalid("overlappingProtection");
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Reads only the explicit platform-default layer; absent metadata supplies an empty object.
     *
     * @param extension parsed plaintext extension
     * @return explicit default object without environment lookups or schema default insertion
     */
    public static JsonValue.ObjectValue defaults(JsonValue extension) {
        JsonValue defaults = services(extension).get("defaults");
        if (defaults == null)
            return new JsonValue.ObjectValue(Map.of());
        if (!(defaults instanceof JsonValue.ObjectValue object))
            throw invalid("defaults");
        return object;
    }

    /**
     * Writes the same key vocabulary into a detached extension without changing host-owned fields.
     *
     * @param extension parsed plaintext extension, or null
     * @param keys      validated metadata from this contract
     * @return detached canonical extension with the existing envelope version
     */
    public static JsonValue.ObjectValue withKeys(JsonValue extension, Map<String, Key> keys) {
        Map<String, JsonValue> services = new TreeMap<>(services(extension));
        Map<String, JsonValue> root = extension == null ? new TreeMap<>()
                : new TreeMap<>(((JsonValue.ObjectValue) extension).values());
        Map<String, JsonValue> encoded = new TreeMap<>();
        for (var entry : keys.entrySet()) {
            path(entry.getKey());
            Key key = Objects.requireNonNull(entry.getValue());
            Map<String, JsonValue> fields = new TreeMap<>();
            fields.put("sensitive", new JsonValue.BooleanValue(key.sensitive()));
            fields.put("description", new JsonValue.StringValue(key.description()));
            if (key.schema() != null)
                fields.put("schema", key.schema());
            encoded.put(entry.getKey(), new JsonValue.ObjectValue(fields));
        }
        services.put("keys", new JsonValue.ObjectValue(encoded));
        root.put("format", new JsonValue.StringValue(FORMAT));
        root.put("services", new JsonValue.ObjectValue(services));
        return new JsonValue.ObjectValue(root);
    }

    /**
     * Validates a source and its exact key schemas using the unique shared Schema entry point.
     *
     * @param value        parsed plaintext source
     * @param schema       optional whole-source schema
     * @param keys         validated key metadata
     * @param maximumDepth existing depth bound
     * @param maximumSteps existing work bound
     * @return bounded immutable findings containing paths and fixed keywords only
     */
    public static List<JsonSchema.Violation> validate(
            JsonValue value,
            JsonValue schema,
            Map<String, Key> keys,
            int maximumDepth,
            long maximumSteps) {
        List<JsonSchema.Violation> findings = new ArrayList<>(JsonSchema.validate(
                schema == null ? new JsonValue.BooleanValue(true) : schema,
                value,
                maximumDepth,
                maximumSteps));
        for (var entry : keys.entrySet()) {
            if (findings.size() >= JsonSchema.MAX_FINDINGS)
                break;
            JsonValue selected = JsonSchema.at(value, entry.getKey());
            if (selected == null) {
                findings.add(new JsonSchema.Violation(entry.getKey(), "metadataPath"));
            } else if (entry.getValue().schema() != null) {
                for (JsonSchema.Violation finding : JsonSchema
                        .validate(entry.getValue().schema(), selected, maximumDepth, maximumSteps)) {
                    if (findings.size() == JsonSchema.MAX_FINDINGS)
                        break;
                    findings.add(new JsonSchema.Violation(entry.getKey() + finding.path(), finding.keyword()));
                }
            }
        }
        return List.copyOf(findings);
    }

    /**
     * Masks protected pointers while retaining explicit opaque secret references and all unprotected values.
     *
     * @param value bounded plaintext tree
     * @param keys  validated non-overlapping metadata
     * @return detached safe management or export tree
     */
    public static JsonValue mask(JsonValue value, Map<String, Key> keys) {
        JsonValue result = value;
        for (var entry : keys.entrySet()) {
            if (!entry.getValue().sensitive())
                continue;
            JsonValue selected = at(result, entry.getKey());
            if (selected != null && !(selected instanceof JsonValue.StringValue text
                    && PatternKit.isMatch("\\$\\{secret://[^}\\r\\n]+\\}", text.value()))) {
                result = replace(result, entry.getKey(), new JsonValue.StringValue(MASK), false);
            }
        }
        return result;
    }

    /**
     * Reads a validated pointer from an optional layer whose container types may differ from the final source.
     * Incompatible array tokens mean this layer has no corresponding member; malformed pointers still fail. Actual
     * source metadata validation continues to use the strict Schema lookup.
     *
     * @param value   optional bounded immutable layer
     * @param pointer canonical non-root metadata pointer
     * @return the exact value, or null when the layer has no corresponding member
     */
    public static JsonValue at(JsonValue value, String pointer) {
        path(pointer);
        if (value == null)
            return null;
        try {
            return JsonSchema.at(value, pointer);
        } catch (IllegalArgumentException incompatibleArrayMember) {
            return null;
        }
    }

    /**
     * Reports whether a key rule can contain literals belonging to a protected subtree. Both ancestor and descendant
     * rules overlap; escaped pointer tokens retain exact member boundaries.
     *
     * @param keys    validated exact metadata
     * @param pointer canonical non-root rule coordinate
     * @return whether the rule overlaps any protected value
     */
    public static boolean overlapsProtection(Map<String, Key> keys, String pointer) {
        path(pointer);
        return keys.entrySet().stream().anyMatch(
                entry -> entry.getValue().sensitive() && (pointer.equals(entry.getKey())
                        || pointer.startsWith(entry.getKey() + "/") || entry.getKey().startsWith(pointer + "/")));
    }

    /**
     * Rejects safe-view placeholders at any present protected coordinate during explicit whole-source replacement.
     * Missing members remain the responsibility of the source and metadata validator.
     *
     * @param value replacement plaintext tree
     * @param keys  previous or incoming validated protected metadata
     */
    public static void requireReplacement(JsonValue value, Map<String, Key> keys) {
        if (value instanceof JsonValue.StringValue text && MASK.equals(text.value()))
            throw invalid("secretReplace");
        for (var entry : keys.entrySet()) {
            if (!entry.getValue().sensitive())
                continue;
            path(entry.getKey());
            JsonValue replacement = at(value, entry.getKey());
            if (replacement instanceof JsonValue.StringValue text && MASK.equals(text.value())) {
                throw invalid("secretReplace");
            }
        }
    }

    /**
     * Applies only explicit protected-key intent, retaining existing values for KEEP and deleting matching metadata.
     * Root actions remain with the existing whole-source contract in the host.
     *
     * @param previous     original plaintext source, or null for creation
     * @param incoming     newly prepared plaintext source
     * @param previousKeys validated original metadata
     * @param nextKeys     validated incoming metadata
     * @param actions      non-root protected-key actions
     * @return detached edited tree and its matching metadata
     */
    public static Edit edit(
            JsonValue previous,
            JsonValue incoming,
            Map<String, Key> previousKeys,
            Map<String, Key> nextKeys,
            Map<String, Item.SecretAction> actions) {
        Map<String, Item.SecretAction> commands = actions == null ? Map.of() : actions;
        Map<String, Key> updated = new TreeMap<>(nextKeys);
        JsonValue result = incoming;
        for (var entry : previousKeys.entrySet()) {
            if (!entry.getValue().sensitive())
                continue;
            Key next = nextKeys.get(entry.getKey());
            if (!commands.containsKey(entry.getKey()) && (next == null || !next.sensitive() || !Objects
                    .equals(previous == null ? null : at(previous, entry.getKey()), at(incoming, entry.getKey()))))
                throw invalid("secretActionRequired");
        }
        for (var entry : nextKeys.entrySet()) {
            if (!entry.getValue().sensitive())
                continue;
            JsonValue selected = at(incoming, entry.getKey());
            Key before = previousKeys.get(entry.getKey());
            if (selected instanceof JsonValue.StringValue text && MASK.equals(text.value())
                    && (before == null || !before.sensitive())
                    && commands.get(entry.getKey()) != Item.SecretAction.KEEP)
                throw invalid("secretReplace");
        }
        for (var entry : new TreeMap<>(commands).entrySet()) {
            String pointer = entry.getKey();
            path(pointer);
            Item.SecretAction action = entry.getValue();
            Key before = previousKeys.get(pointer), after = nextKeys.get(pointer);
            if (action == null || (before == null || !before.sensitive()) && (after == null || !after.sensitive())) {
                throw invalid("secretAction");
            }
            JsonValue old = previous == null ? null : at(previous, pointer);
            switch (action) {
                case KEEP -> {
                    if (old == null || after == null || !after.sensitive())
                        throw invalid("secretKeep");
                    result = replace(result, pointer, old, false);
                }
                case REPLACE -> {
                    JsonValue replacement = at(result, pointer);
                    if (replacement == null)
                        throw invalid("secretReplace");
                    requireReplacement(result, Map.of(pointer, before != null && before.sensitive() ? before : after));
                }
                case DELETE -> {
                    if (old == null)
                        throw invalid("secretDelete");
                    result = replace(result, pointer, null, true);
                    updated.keySet().removeIf(key -> key.equals(pointer) || key.startsWith(pointer + "/"));
                }
            }
        }
        return new Edit(result, Collections.unmodifiableMap(updated));
    }

    /**
     * Replaces one exact object member or existing array element without growing arrays or shifting protected paths.
     *
     * @param value       bounded immutable input tree
     * @param pointer     canonical non-root pointer
     * @param replacement exact replacement, or null only for deletion
     * @param remove      whether an object member is removed
     * @return detached edited tree
     */
    public static JsonValue replace(JsonValue value, String pointer, JsonValue replacement, boolean remove) {
        path(pointer);
        if (value == null || !remove && replacement == null)
            throw invalid("replacement");
        return replace(value, JsonSchema.tokens(pointer), 0, replacement, remove);
    }

    /**
     * Applies one immutable path edit; missing ancestors are never invented from ambiguous pointer tokens.
     *
     * @param value       current immutable node
     * @param path        exact decoded member sequence
     * @param index       current path component
     * @param replacement replacement value
     * @param remove      whether to remove the final object member
     * @return rebuilt current subtree
     */
    private static JsonValue replace(
            JsonValue value,
            List<String> path,
            int index,
            JsonValue replacement,
            boolean remove) {
        String member = path.get(index);
        boolean last = index + 1 == path.size();
        if (value instanceof JsonValue.ObjectValue object) {
            Map<String, JsonValue> fields = new TreeMap<>(object.values());
            if (last) {
                if (remove)
                    fields.remove(member);
                else
                    fields.put(member, replacement);
            } else {
                JsonValue child = fields.get(member);
                if (child == null && remove)
                    return value;
                if (child == null)
                    throw invalid("replacementPath");
                fields.put(member, replace(child, path, index + 1, replacement, remove));
            }
            return new JsonValue.ObjectValue(fields);
        }
        if (value instanceof JsonValue.ArrayValue array) {
            if (last && remove)
                throw invalid("arraySecretDelete");
            JsonValue selected = JsonSchema.at(value, JsonSchema.pointer("", member));
            if (selected == null)
                throw invalid("replacementPath");
            int position;
            try {
                position = Integer.parseInt(member);
            } catch (NumberFormatException failure) {
                throw invalid("replacementPath");
            }
            List<JsonValue> values = new ArrayList<>(array.values());
            values.set(position, last ? replacement : replace(selected, path, index + 1, replacement, remove));
            return new JsonValue.ArrayValue(values);
        }
        throw invalid("replacementPath");
    }

    /**
     * Validates the shared non-root key coordinate without trimming or interpreting separators.
     *
     * @param pointer exact canonical JSON Pointer
     */
    private static void path(String pointer) {
        if (pointer == null || pointer.isEmpty() || pointer.codePointCount(0, pointer.length()) > 4096)
            throw invalid("pointer");
        JsonSchema.tokens(pointer);
    }

    /**
     * Creates a value-free contract error for host mapping to the existing business error code.
     *
     * @param code fixed contract rule name
     * @return sanitized argument exception
     */
    private static IllegalArgumentException invalid(String code) {
        return new IllegalArgumentException("Invalid item key metadata: " + code);
    }

}
