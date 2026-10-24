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

import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Materializes bounded configuration values without persistence, identity, codecs or environment lookups. The host
 * supplies exact immutable revisions and freezes relationship options in the existing revision envelope.
 *
 * @author Kimi Liu
 */
public final class ItemMerger {

    /**
     * Schema used only to apply the shared structural bounds.
     */
    private static final JsonValue UNCONSTRAINED = new JsonValue.BooleanValue(true);

    /**
     * Prevents construction of the shared pure-value merger.
     */
    private ItemMerger() {
        // No initialization required.
    }

    /**
     * Applies explicit defaults, shared content, authorized overlay, and application content in that order.
     * Equal-priority defaults must agree. Objects recurse; arrays and scalars replace atomically.
     *
     * @param input        exact source snapshots and normalized reference options
     * @param maximumDepth existing structural depth bound
     * @param maximumSteps existing node/work bound
     * @return immutable value, provenance, metadata and path-only findings
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if structural bounds, schema-definition syntax or merge preconditions are invalid; ordinary validation findings are returned
     */
    public static Result merge(Input input, int maximumDepth, long maximumSteps) {
        Objects.requireNonNull(input);
        Merge merge = new Merge(input, maximumDepth, maximumSteps);
        return merge.materialize();
    }

    /**
     * Creates a fixed error without including source values or identities.
     *
     * @param rule fixed contract failure code
     * @return sanitized failure
     */
    private static IllegalArgumentException invalid(String rule) {
        return new IllegalArgumentException("Invalid configuration merge: " + rule);
    }

    /**
     * Carries one plaintext source whose identity and revision semantics must already be authorized and resolved by its host service.
     * @param item_id immutable source owner
     * @param revision_id host-supplied exact source revision; this constructor only checks that it is nonblank
     * @param value parsed source value
     * @param extension parsed source metadata, or null
     * @param schema optional source schema
     * @author Kimi Liu
     */
    public record Source(String item_id, String revision_id, JsonValue value, JsonValue extension, JsonValue schema) {

        /**
         * Requires nonblank source and revision identifiers and a non-null value. The host, not this constructor,
         * must resolve and authorize immutable revision identity.
         *
         * @param item_id     source owner
         * @param revision_id nonblank revision identifier supplied by the host
         * @param value       materialized plaintext
         * @param extension   optional source metadata
         * @param schema      optional source schema
         * @throws IllegalArgumentException if either identifier is null or blank, or value is null
         */
        public Source {
            if (item_id == null || item_id.isBlank() || revision_id == null || revision_id.isBlank() || value == null) {
                throw invalid("source");
            }
        }
    }

    /**
     * Supplies the existing shared-reference options and the optional application override source.
     * @param shared optional shared source
     * @param application optional application source
     * @param overlay optional reference-local object overlay, in unprefixed source coordinates
     * @param prefix optional root member name; trimmed, defaulted to empty, and limited to 255 code points
     * @param override case-sensitive NONE, KEYS or ALL policy, defaulting to NONE
     * @param keys non-root JSON Pointers, frozen as a sorted set; nonempty exactly when the mode is KEYS
     * @author Kimi Liu
     */
    public record Input(Source shared, Source application, JsonValue.ObjectValue overlay, String prefix,
            String override, Set<String> keys) {

        /**
         * Freezes normalized options without inventing endpoint aliases or path hierarchies.
         *
         * @param shared      optional shared source
         * @param application optional application source
         * @param overlay     optional object overlay
         * @param prefix      root member name, trimmed and limited to 255 code points; null becomes empty
         * @param override    case-sensitive NONE, KEYS or ALL; null becomes NONE
         * @param keys        frozen sorted non-root JSON Pointers; null becomes empty, and the set must be nonempty exactly for KEYS
         * @throws IllegalArgumentException if both sources are absent, options or pointers are invalid, or shared-only options lack a shared source
         * @throws NullPointerException if keys contains a null element
         */
        public Input {
            if (shared == null && application == null)
                throw invalid("source");
            prefix = prefix == null ? "" : prefix.trim();
            if (prefix.codePointCount(0, prefix.length()) > 255)
                throw invalid("prefix");
            override = override == null ? "NONE" : override;
            if (!Set.of("NONE", "KEYS", "ALL").contains(override))
                throw invalid("override");
            keys = Collections.unmodifiableSet(new TreeSet<>(keys == null ? Set.of() : keys));
            for (String key : keys) {
                if (key.isEmpty() || key.codePointCount(0, key.length()) > 4096)
                    throw invalid("keys");
                JsonSchema.tokens(key);
            }
            if ("KEYS".equals(override) == keys.isEmpty())
                throw invalid("keys");
            if (shared == null && (!prefix.isEmpty() || overlay != null && !overlay.values().isEmpty())) {
                throw invalid("reference");
            }
        }
    }

    /**
     * Contains one exact materialization and safe provenance; callers must protect sensitive values before exposure.
     *
     * @param value     merged plaintext value
     * @param revisions source item identifiers mapped to exact revisions
     * @param sources   canonical value pointers mapped to exact source revisions
     * @param keys      merged key metadata with sensitivity preserved
     * @param overrides exact paths changed over shared source values
     * @param findings  bounded path-only Schema findings
     * @param schema    optional composed whole-effective schema with source-local references relocated
     * @author Kimi Liu
     */
    public record Result(JsonValue value, Map<String, String> revisions, Map<String, String> sources,
            Map<String, ItemMetadata.Key> keys, Set<String> overrides, List<JsonSchema.Violation> findings,
            JsonValue schema) {

        /**
         * Defensively freezes all projections accompanying the immutable JSON tree.
         *
         * @param value     merged plaintext
         * @param revisions immutable source coordinates
         * @param sources   per-path origins
         * @param keys      merged metadata
         * @param overrides actual overridden paths
         * @param findings  safe validation results
         * @param schema    optional composed whole-effective schema
         */
        public Result {
            Objects.requireNonNull(value);
            revisions = Collections.unmodifiableMap(new TreeMap<>(revisions));
            sources = Collections.unmodifiableMap(new TreeMap<>(sources));
            keys = Collections.unmodifiableMap(new TreeMap<>(keys));
            overrides = Collections.unmodifiableSet(new TreeSet<>(overrides));
            findings = List.copyOf(findings);
        }
    }

    /**
     * Holds invocation-local counters and projections only; no process-global merge state is retained.
     *
     * @author Kimi Liu
     */
    private static final class Merge {

        /**
         * Immutable options for this materialization.
         */
        private final Input input;
        /**
         * Existing tree-depth limit.
         */
        private final int maximumDepth;
        /**
         * Existing work budget.
         */
        private final long maximumSteps;
        /**
         * Exact source revisions selected by the host.
         */
        private final Map<String, String> revisions = new TreeMap<>();
        /**
         * Per-leaf or atomic-container origins.
         */
        private final Map<String, String> sources = new TreeMap<>();
        /**
         * Merged exact key metadata.
         */
        private final Map<String, ItemMetadata.Key> keys = new TreeMap<>();
        /**
         * Paths actually changed over original shared source values.
         */
        private final Set<String> overrides = new TreeSet<>();
        /**
         * Shared content in effective coordinates, excluding defaults.
         */
        private final JsonValue shared;
        /**
         * Literal prefix encoded as a canonical pointer.
         */
        private final String prefix;
        /**
         * Number of recursive merge and origin operations performed.
         */
        private long steps;

        /**
         * Validates every input tree through the existing shared bounds implementation.
         *
         * @param input        frozen merge options
         * @param maximumDepth structural depth bound
         * @param maximumSteps work bound
         */
        private Merge(Input input, int maximumDepth, long maximumSteps) {
            this.input = input;
            this.maximumDepth = maximumDepth;
            this.maximumSteps = maximumSteps;
            this.prefix = input.prefix().isEmpty() ? "" : JsonSchema.pointer("", input.prefix());
            for (Source source : new Source[] { input.shared(), input.application() }) {
                if (source == null)
                    continue;
                JsonSchema.validate(UNCONSTRAINED, source.value(), maximumDepth, maximumSteps);
                ItemMetadata.keys(source.extension(), maximumDepth, maximumSteps);
                if (source.schema() != null)
                    JsonSchema.requireSchema(source.schema(), maximumDepth, maximumSteps);
                if (revisions.putIfAbsent(source.item_id(), source.revision_id()) != null)
                    throw invalid("duplicateSource");
            }
            if (input.overlay() != null)
                JsonSchema.validate(UNCONSTRAINED, input.overlay(), maximumDepth, maximumSteps);
            this.shared = input.shared() == null ? null : wrap(input.shared().value());
        }

        /**
         * Executes the frozen layer order and validates the final shared and application projections.
         *
         * @return detached immutable merge result
         */
        private Result materialize() {
            JsonValue value = null;
            if (input.shared() != null) {
                value = layer(
                        value,
                        wrap(ItemMetadata.defaults(input.shared().extension())),
                        "",
                        input.shared().revision_id(),
                        false,
                        true,
                        0);
            }
            if (input.application() != null) {
                value = layer(
                        value,
                        ItemMetadata.defaults(input.application().extension()),
                        "",
                        input.application().revision_id(),
                        false,
                        true,
                        0);
            }
            if (input.shared() != null) {
                value = layer(value, shared, "", input.shared().revision_id(), false, false, 0);
                metadata(input.shared(), prefix);
                if (input.overlay() != null && !input.overlay().values().isEmpty()) {
                    value = layer(value, wrap(input.overlay()), "", input.shared().revision_id(), true, false, 0);
                }
            }
            if (input.application() != null) {
                value = layer(
                        value,
                        input.application().value(),
                        "",
                        input.application().revision_id(),
                        true,
                        false,
                        0);
                metadata(input.application(), "");
            }
            JsonValue materialized = value;
            overrides.removeIf(path -> {
                tick(0);
                JsonValue original = selected(shared, path), current = selected(materialized, path);
                return original == null ? current == null : current != null && equal(original, current);
            });
            JsonSchema.validate(UNCONSTRAINED, value, maximumDepth, maximumSteps);
            // Preserve outer protection while retaining descendant descriptions and validation rules.
            for (var entry : new TreeMap<>(keys).entrySet()) {
                if (!entry.getValue().sensitive())
                    continue;
                String parent = entry.getKey();
                while (parent.lastIndexOf('/') > 0) {
                    parent = parent.substring(0, parent.lastIndexOf('/'));
                    if (keys.containsKey(parent) && keys.get(parent).sensitive()) {
                        ItemMetadata.Key key = entry.getValue();
                        keys.put(
                                entry.getKey(),
                                new ItemMetadata.Key(false, key.description(), key.comment(), key.override(),
                                        key.schema()));
                        break;
                    }
                }
            }
            JsonValue schema = schema();
            List<JsonSchema.Violation> findings = ItemMetadata
                    .validate(value, schema, keys, maximumDepth, maximumSteps);
            return new Result(value, revisions, sources, keys, overrides,
                    findings.subList(0, Math.min(findings.size(), JsonSchema.MAX_FINDINGS)), schema);
        }

        /**
         * Freezes the same source constraints as one whole-effective schema; no annotation default is inserted.
         *
         * @return optional equivalent schema with each local reference retaining its original source root
         */
        private JsonValue schema() {
            JsonValue sharedSchema = input.shared() == null ? null : input.shared().schema();
            JsonValue applicationSchema = input.application() == null ? null : input.application().schema();
            if (sharedSchema != null && !prefix.isEmpty()) {
                String location = JsonSchema.pointer("/properties", input.prefix());
                sharedSchema = new JsonValue.ObjectValue(Map.of(
                        "type",
                        new JsonValue.StringValue("object"),
                        "required",
                        new JsonValue.ArrayValue(List.of(new JsonValue.StringValue(input.prefix()))),
                        "properties",
                        new JsonValue.ObjectValue(Map.of(input.prefix(), scoped(sharedSchema, location)))));
            }
            if (sharedSchema == null)
                return applicationSchema;
            if (applicationSchema == null)
                return sharedSchema;
            return new JsonValue.ObjectValue(Map.of(
                    "allOf",
                    new JsonValue.ArrayValue(
                            List.of(scoped(sharedSchema, "/allOf/0"), scoped(applicationSchema, "/allOf/1")))));
        }

        /**
         * Merges one layer and checks authorization against actual shared content, never default-only paths.
         *
         * @param before    prior value, or null for an absent member
         * @param after     incoming immutable value
         * @param path      canonical effective pointer
         * @param revision  exact origin revision
         * @param authorize whether shared collisions require the reference policy
         * @param defaults  whether same-priority collisions must agree
         * @param depth     current recursion depth
         * @return merged immutable value
         */
        private JsonValue layer(
                JsonValue before,
                JsonValue after,
                String path,
                String revision,
                boolean authorize,
                boolean defaults,
                int depth) {
            tick(depth);
            if (before instanceof JsonValue.ObjectValue left && after instanceof JsonValue.ObjectValue right) {
                Map<String, JsonValue> result = new TreeMap<>(left.values());
                if (!right.values().isEmpty())
                    sources.remove(path);
                for (var member : right.values().entrySet()) {
                    String child = JsonSchema.pointer(path, member.getKey());
                    result.put(
                            member.getKey(),
                            layer(
                                    result.get(member.getKey()),
                                    member.getValue(),
                                    child,
                                    revision,
                                    authorize,
                                    defaults,
                                    depth + 1));
                }
                return new JsonValue.ObjectValue(result);
            }
            if (before != null && equal(before, after)) {
                if (!defaults) {
                    sources.keySet().removeIf(key -> key.equals(path) || key.startsWith(path + "/"));
                    origin(after, path, revision, depth);
                }
                return before;
            }
            if (before != null && defaults)
                throw invalid("defaultConflict");
            if (authorize && shared != null)
                collision(after, path, depth);
            sources.keySet().removeIf(key -> key.equals(path) || key.startsWith(path + "/"));
            origin(after, path, revision, depth);
            return after;
        }

        /**
         * Checks exact conflicting paths, including destruction of an ancestor containing shared members.
         *
         * @param value replacement value
         * @param path  replacement pointer
         * @param depth recursion depth
         */
        private void collision(JsonValue value, String path, int depth) {
            tick(depth);
            JsonValue original = selected(shared, path);
            if (original == null || equal(original, value))
                return;
            String sourcePath = prefix.isEmpty() ? path
                    : path.equals(prefix) ? "" : path.startsWith(prefix + "/") ? path.substring(prefix.length()) : null;
            // Replacing an ancestor destroys the wrapped source and therefore needs whole-source authorization.
            if (sourcePath == null && prefix.startsWith(path + "/"))
                sourcePath = "";
            if (sourcePath == null)
                return;
            if (!"ALL".equals(input.override())
                    && !("KEYS".equals(input.override()) && input.keys().contains(sourcePath))) {
                throw invalid("overrideDenied");
            }
            overrides.add(path);
        }

        /**
         * Looks up an internally generated path across layers whose container types may have changed. An object member
         * is absent from an array with an incompatible index token; client pointer validation remains strict in the
         * shared metadata contract rather than being relaxed by this merge-only projection.
         *
         * @param value optional immutable layer
         * @param path  internally generated canonical pointer
         * @return exact member, or null when this layer has no corresponding coordinate
         */
        private JsonValue selected(JsonValue value, String path) {
            if (value == null)
                return null;
            try {
                return JsonSchema.at(value, path);
            } catch (IllegalArgumentException absentArrayMember) {
                return null;
            }
        }

        /**
         * Records leaf origins while treating arrays as atomic replacement values.
         *
         * @param value    incoming value
         * @param path     canonical value pointer
         * @param revision exact source revision
         * @param depth    recursion depth
         */
        private void origin(JsonValue value, String path, String revision, int depth) {
            tick(depth);
            if (value instanceof JsonValue.ObjectValue object && !object.values().isEmpty()) {
                for (var member : object.values().entrySet()) {
                    origin(member.getValue(), JsonSchema.pointer(path, member.getKey()), revision, depth + 1);
                }
            } else {
                sources.put(path, revision);
            }
        }

        /**
         * Combines metadata without declassifying an inherited sensitive key or dropping inherited Schema rules.
         *
         * @param source immutable source metadata
         * @param base   canonical prefix for the source
         */
        private void metadata(Source source, String base) {
            for (var entry : ItemMetadata.keys(source.extension(), maximumDepth, maximumSteps).entrySet()) {
                tick(0);
                String path = base + entry.getKey();
                if (path.codePointCount(0, path.length()) > 4096)
                    throw invalid("metadataPath");
                ItemMetadata.Key next = entry.getValue();
                ItemMetadata.Key previous = keys.get(path);
                if (previous == null) {
                    keys.put(path, next);
                } else {
                    JsonValue schema = previous.schema() == null ? next.schema()
                            : next.schema() == null ? previous.schema()
                                    : new JsonValue.ObjectValue(Map.of(
                                            "allOf",
                                            new JsonValue.ArrayValue(List.of(
                                                    scoped(previous.schema(), "/allOf/0"),
                                                    scoped(next.schema(), "/allOf/1")))));
                    keys.put(
                            path,
                            new ItemMetadata.Key(previous.sensitive() || next.sensitive(),
                                    next.description() == null ? previous.description() : next.description(),
                                    next.comment() == null ? previous.comment() : next.comment(),
                                    next.override() == null ? previous.override() : next.override(), schema));
                }
            }
        }

        /**
         * Relocates local references when two independently rooted key schemas are composed. Literal const, enum and
         * annotation values are retained without interpreting their member names.
         *
         * @param schema one schema node
         * @param base   new root pointer within the composed schema
         * @return equivalent schema with references rooted at its original source
         */
        private JsonValue scoped(JsonValue schema, String base) {
            if (!(schema instanceof JsonValue.ObjectValue object))
                return schema;
            Map<String, JsonValue> result = new TreeMap<>(object.values());
            for (var entry : object.values().entrySet()) {
                tick(0);
                String name = entry.getKey();
                JsonValue value = entry.getValue();
                if ("$ref".equals(name)) {
                    result.put(
                            name,
                            new JsonValue.StringValue(
                                    "#" + base + ((JsonValue.StringValue) value).value().substring(1)));
                } else if (Set.of("properties", "$defs").contains(name)) {
                    Map<String, JsonValue> children = new TreeMap<>();
                    ((JsonValue.ObjectValue) value).values()
                            .forEach((key, child) -> children.put(key, scoped(child, base)));
                    result.put(name, new JsonValue.ObjectValue(children));
                } else if (Set.of("allOf", "anyOf", "oneOf").contains(name)) {
                    result.put(
                            name,
                            new JsonValue.ArrayValue(((JsonValue.ArrayValue) value).values().stream()
                                    .map(child -> scoped(child, base)).toList()));
                } else if (Set.of("additionalProperties", "items", "not", "if", "then", "else").contains(name)) {
                    result.put(name, scoped(value, base));
                }
            }
            return new JsonValue.ObjectValue(result);
        }

        /**
         * Wraps shared values under one literal member rather than interpreting dotted or slash-separated paths.
         *
         * @param value unprefixed source value
         * @return effective-coordinate value
         */
        private JsonValue wrap(JsonValue value) {
            return prefix.isEmpty() ? value : new JsonValue.ObjectValue(Map.of(input.prefix(), value));
        }

        /**
         * Reuses the shared exact comparator without adding a synthetic Schema layer to the value depth.
         *
         * @param left  first immutable value
         * @param right second immutable value
         * @return whether values are semantically identical
         */
        private boolean equal(JsonValue left, JsonValue right) {
            return JsonSchema.equivalent(left, right, maximumDepth, maximumSteps);
        }

        /**
         * Bounds recursive merge and provenance work independently from the reused input validation limits.
         *
         * @param depth current recursion depth
         */
        private void tick(int depth) {
            if (depth > maximumDepth || ++steps > maximumSteps)
                throw invalid("budget");
        }
    }

}
