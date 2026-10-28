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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Pure typed configuration materialization; never parses, persists or selects a release.
 *
 * @author Kimi Liu
 */
public final class ItemMerger {

    /**
     * Prevents construction.
     */
    private ItemMerger() {
        // No initialization required.
    }

    /**
     * Applies defaults, shared definitions/overlays and application definitions with one operation budget.
     *
     * @param sources      resolved sources
     * @param options      matching usage options
     * @param maximumDepth maximum depth
     * @param maximumSteps total work
     * @return exact values and provenance
     */
    public static Result merge(
            List<Source> sources,
            List<ReferenceCodec.Options> options,
            int maximumDepth,
            long maximumSteps) {
        if (sources.size() != options.size())
            throw new IllegalArgumentException("Source/options mismatch");
        return new Merge(sources, options, maximumDepth, maximumSteps).materialize();
    }

    /**
     * One resolved source, independent of version-domain objects.
     *
     * @param key      opaque source identity
     * @param document parsed definition
     * @author Kimi Liu
     */
    public record Source(String key, ItemCodec.Document document) {

        /**
         * Requires complete source data.
         *
         * @param key      source identity
         * @param document parsed definition
         */
        public Source {
            if (key == null || key.isBlank())
                throw new IllegalArgumentException("Source required");
            Objects.requireNonNull(document);
        }

    }

    /**
     * Materialized values and safe provenance.
     *
     * @param value   business values
     * @param sources path to source identity
     * @param schema  composed constraints
     * @author Kimi Liu
     */
    public record Result(JsonValue value, Map<String, String> sources, JsonValue schema) {

        /**
         * Freezes provenance.
         *
         * @param value   business tree
         * @param sources origins
         * @param schema  composed Schema
         */
        public Result {
            sources = Collections.unmodifiableMap(new TreeMap<>(sources));
        }

    }

    /**
     * Invocation-local bounded merge state.
     *
     * @author Kimi Liu
     */
    private static final class Merge {

        /**
         * Resolved sources.
         */
        private final List<Source> input;
        /**
         * Matching options.
         */
        private final List<ReferenceCodec.Options> options;
        /**
         * Maximum tree depth.
         */
        private final int maximumDepth;
        /**
         * Maximum total work.
         */
        private final long maximumSteps;
        /**
         * Per-path origin identities.
         */
        private final Map<String, String> origins = new TreeMap<>();
        /**
         * Consumed work.
         */
        private long steps;

        /**
         * Creates local merge state.
         *
         * @param input        sources
         * @param options      relationships
         * @param maximumDepth depth bound
         * @param maximumSteps work bound
         */
        private Merge(List<Source> input, List<ReferenceCodec.Options> options, int maximumDepth, long maximumSteps) {
            this.input = List.copyOf(input);
            this.options = List.copyOf(options);
            this.maximumDepth = maximumDepth;
            this.maximumSteps = maximumSteps;
        }

        /**
         * Materializes in deterministic domain order.
         *
         * @return merged values
         */
        private Result materialize() {
            JsonValue value = new JsonValue.ObjectValue(Map.of());
            List<JsonValue> schemas = new ArrayList<>();
            for (int i = 0; i < input.size(); i++)
                value = layer(
                        value,
                        wrap(input.get(i).document().applicableDefaults(), options.get(i).prefix()),
                        "",
                        input.get(i).key(),
                        true,
                        0);
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < input.size(); i++)
                order.add(i);
            order.sort(
                    Comparator.comparing((Integer i) -> options.get(i).type() == Reference.Type.ITEM_OWNER)
                            .thenComparing(i -> options.get(i).alias()).thenComparing(i -> input.get(i).key()));
            for (int i : order) {
                Source source = input.get(i);
                ReferenceCodec.Options usage = options.get(i);
                JsonValue addition = wrap(source.document().values(), usage.prefix());
                if (usage.type() == Reference.Type.ITEM_OWNER)
                    for (int j = 0; j < input.size(); j++)
                        if (options.get(j).type() == Reference.Type.ITEM_APP)
                            authorize(
                                    wrap(input.get(j).document().effective(), options.get(j).prefix()),
                                    addition,
                                    "",
                                    options.get(j),
                                    0);
                value = layer(value, addition, "", source.key(), false, 0);
                if (usage.overlay() != null && !usage.overlay().values().isEmpty()) {
                    JsonValue overlay = wrap(usage.overlay(), usage.prefix());
                    authorize(wrap(source.document().effective(), usage.prefix()), overlay, "", usage, 0);
                    value = layer(value, overlay, "", source.key(), false, 0);
                }
                JsonValue schema = source.document().schema();
                if (schema != null) {
                    if (!usage.prefix().isEmpty()) {
                        String path = JsonSchema.pointer("/properties", usage.prefix());
                        schema = new JsonValue.ObjectValue(Map.of(
                                "type",
                                new JsonValue.StringValue("object"),
                                "properties",
                                new JsonValue.ObjectValue(Map.of(usage.prefix(), scoped(schema, path))),
                                "required",
                                new JsonValue.ArrayValue(List.of(new JsonValue.StringValue(usage.prefix())))));
                    }
                    schemas.add(scoped(schema, "/allOf/" + schemas.size()));
                }
            }
            JsonValue schema = schemas.isEmpty() ? new JsonValue.BooleanValue(true)
                    : new JsonValue.ObjectValue(Map.of("allOf", new JsonValue.ArrayValue(schemas)));
            JsonSchema.validate(new JsonValue.BooleanValue(true), value, maximumDepth, maximumSteps - steps);
            if (!JsonSchema.validate(schema, value, maximumDepth, maximumSteps - steps).isEmpty())
                throw new IllegalArgumentException("Effective Schema validation failed");
            for (int i = 0; i < input.size(); i++)
                for (var rule : input.get(i).document().rules().entrySet()) {
                    tick(0);
                    String path = options.get(i).prefix().isEmpty() ? rule.getKey()
                            : JsonSchema.pointer("", options.get(i).prefix()) + rule.getKey();
                    JsonValue selected = ItemCodec.at(value, path);
                    if (selected != null && rule.getValue().schema() != null && !JsonSchema
                            .validate(rule.getValue().schema(), selected, maximumDepth, maximumSteps - steps).isEmpty())
                        throw new IllegalArgumentException("Effective key Schema validation failed");
                }
            return new Result(value, origins, schema);
        }

        /**
         * Combines objects recursively and replaces atomic values.
         *
         * @param before   prior tree
         * @param after    incoming tree
         * @param path     exact path
         * @param source   origin
         * @param defaults same-priority defaults
         * @param depth    recursion depth
         * @return combined tree
         */
        private JsonValue layer(
                JsonValue before,
                JsonValue after,
                String path,
                String source,
                boolean defaults,
                int depth) {
            tick(depth);
            if (before instanceof JsonValue.ObjectValue left && after instanceof JsonValue.ObjectValue right) {
                Map<String, JsonValue> result = new TreeMap<>(left.values());
                for (var e : right.values().entrySet()) {
                    String child = JsonSchema.pointer(path, e.getKey());
                    result.put(
                            e.getKey(),
                            layer(result.get(e.getKey()), e.getValue(), child, source, defaults, depth + 1));
                }
                return new JsonValue.ObjectValue(result);
            }
            if (before != null && defaults && !same(before, after))
                throw new IllegalArgumentException("Conflicting defaults");
            origins.keySet().removeIf(p -> p.equals(path) || p.startsWith(path + "/"));
            origin(after, path, source, depth);
            return after;
        }

        /**
         * Checks actual destructive changes against source-local override rights.
         *
         * @param before shared source values
         * @param after  incoming layer
         * @param path   path
         * @param policy source usage rights
         * @param depth  recursion depth
         */
        private void authorize(
                JsonValue before,
                JsonValue after,
                String path,
                ReferenceCodec.Options policy,
                int depth) {
            tick(depth);
            if (before == null || same(before, after))
                return;
            if (before instanceof JsonValue.ObjectValue left && after instanceof JsonValue.ObjectValue right) {
                for (var e : right.values().entrySet())
                    authorize(
                            left.values().get(e.getKey()),
                            e.getValue(),
                            JsonSchema.pointer(path, e.getKey()),
                            policy,
                            depth + 1);
                return;
            }
            String prefix = policy.prefix().isEmpty() ? "" : JsonSchema.pointer("", policy.prefix());
            String local = path.startsWith(prefix) ? path.substring(prefix.length()) : "";
            if (policy.override() != ReferenceCodec.Override.ALL
                    && !(policy.override() == ReferenceCodec.Override.KEYS && policy.keys().contains(local)))
                throw new IllegalArgumentException("Override denied");
        }

        /**
         * Records leaf/container origins.
         *
         * @param value  tree
         * @param path   path
         * @param source origin
         * @param depth  recursion depth
         */
        private void origin(JsonValue value, String path, String source, int depth) {
            tick(depth);
            if (value instanceof JsonValue.ObjectValue object && !object.values().isEmpty())
                for (var e : object.values().entrySet())
                    origin(e.getValue(), JsonSchema.pointer(path, e.getKey()), source, depth + 1);
            else
                origins.put(path, source);
        }

        /**
         * Applies a literal root prefix.
         *
         * @param value  source tree
         * @param prefix literal root key
         * @return wrapped tree
         */
        private JsonValue wrap(JsonValue value, String prefix) {
            return prefix.isEmpty() ? value : new JsonValue.ObjectValue(Map.of(prefix, value));
        }

        /**
         * Compares semantic exact values.
         *
         * @param left  first tree
         * @param right second tree
         * @return whether equal
         */
        private boolean same(JsonValue left, JsonValue right) {
            return Objects.equals(ItemCodec.encode(left), ItemCodec.encode(right));
        }

        /**
         * Charges shared work.
         *
         * @param depth recursion depth
         */
        private void tick(int depth) {
            if (depth > maximumDepth || ++steps >= maximumSteps)
                throw new IllegalArgumentException("Merge budget exceeded");
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
                                    Symbol.HASH + base + ((JsonValue.StringValue) value).value().substring(1)));
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

    }

}
