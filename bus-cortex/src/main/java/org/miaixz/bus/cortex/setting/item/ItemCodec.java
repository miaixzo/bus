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

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.EnumValue.Format;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonSchema;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Single parser and canonical definition protocol; owns no persistence or release state.
 *
 * @author Kimi Liu
 */
public final class ItemCodec {

    /**
     * Current supported definition metadata protocol.
     */
    public static final String FORMAT = "services-extension-v2";
    /**
     * Maximum parsed tree depth.
     */
    private final int maximumDepth;
    /**
     * Maximum work within one definition operation.
     */
    private final long maximumSteps;

    /**
     * Creates a bounded definition parser.
     *
     * @param maximumDepth tree depth
     * @param maximumSteps work budget
     */
    public ItemCodec(int maximumDepth, long maximumSteps) {
        if (maximumDepth < 1 || maximumDepth > 64 || maximumSteps < 1 || maximumSteps > 100000)
            throw invalid("budget");
        this.maximumDepth = maximumDepth;
        this.maximumSteps = maximumSteps;
    }

    /**
     * Builds a transient definition tree from typed members, without reparsing.
     *
     * @param document typed definition
     * @return exact envelope
     */
    private static JsonValue.ObjectValue definition(Document document) {
        Map<String, JsonValue> result = new TreeMap<>();
        document.properties().forEach((k, v) -> {
            Map<String, JsonValue> fields = new TreeMap<>();
            if (v.value() != null)
                fields.put("value", v.value());
            if (v.defaultValue() != null)
                fields.put("default", v.defaultValue());
            if (v.desc() != null)
                fields.put("desc", new JsonValue.StringValue(v.desc()));
            result.put(k, new JsonValue.ObjectValue(fields));
        });
        return new JsonValue.ObjectValue(result);
    }

    /**
     * Parses strict bounded JSON for this domain's explicit envelopes.
     *
     * @param json encoded JSON
     * @return tree or null for absence
     */
    public static JsonValue tree(String json) {
        if (json == null)
            return null;
        JsonValue value = JsonKit.readValue(json.getBytes(Charset.UTF_8), 64, true);
        JsonSchema.validate(new JsonValue.BooleanValue(true), value, 64, 100000);
        return value;
    }

    /**
     * Canonically encodes a domain tree with exact decimal values.
     *
     * @param value tree
     * @return canonical JSON
     */
    public static String encode(JsonValue value) {
        if (value == null)
            return null;
        StringBuilder out = new StringBuilder();
        append(value, out, 0, new long[1]);
        return out.toString();
    }

    /**
     * Converts a literal top-level key or explicit JSON Pointer to its canonical pointer coordinate.
     *
     * @param key literal object key, enum code or exact identity
     * @return the encoded value or exact identity; null when the documented optional result is absent
     */
    private static String coordinate(String key) {
        String pointer = key.isEmpty() || key.startsWith(Symbol.SLASH) ? key : JsonSchema.pointer(Normal.EMPTY, key);
        path(pointer);
        return pointer;
    }

    /**
     * Indexes definition entries by canonical pointers without interpreting business objects as envelopes.
     *
     * @param definition validated definition envelope
     * @return canonical pointer-to-definition members
     */
    private static Map<String, JsonValue> entries(JsonValue definition) {
        if (!(definition instanceof JsonValue.ObjectValue object))
            throw invalid("definition");
        Map<String, JsonValue> result = new TreeMap<>();
        object.values().forEach((key, value) -> result.put(coordinate(key), value));
        return result;
    }

    /**
     * Builds one definition member layer while rejecting same-layer ancestor and descendant writes.
     *
     * @param definition validated definition envelope
     * @param member     definition member or literal object property
     * @return the exact present-member layer
     */
    private static JsonValue layer(JsonValue definition, String member) {
        Map<String, JsonValue> writes = new TreeMap<>();
        entries(definition).forEach((path, entry) -> {
            if (!(entry instanceof JsonValue.ObjectValue fields))
                throw invalid("definitionEntry");
            if (fields.values().containsKey(member))
                writes.put(path, fields.values().get(member));
        });
        String previous = null;
        for (String pointer : writes.keySet()) {
            if (previous != null && ancestor(previous, pointer))
                throw invalid("ancestorWrite");
            previous = pointer;
        }
        JsonValue result = new JsonValue.ObjectValue(Map.of());
        for (var entry : writes.entrySet())
            result = insert(result, JsonSchema.tokens(entry.getKey()), 0, entry.getValue());
        return result;
    }

    /**
     * Builds missing object containers without guessing array types from numeric tokens.
     *
     * @param value       immutable subtree being edited
     * @param tokens      decoded JSON Pointer tokens
     * @param index       current decoded path component
     * @param replacement replacement value, absent only for explicit deletion
     * @return the updated immutable object tree
     */
    private static JsonValue insert(JsonValue value, List<String> tokens, int index, JsonValue replacement) {
        if (index == tokens.size())
            return replacement;
        if (!(value instanceof JsonValue.ObjectValue object))
            throw invalid("pathContainer");
        Map<String, JsonValue> fields = new TreeMap<>(object.values());
        String key = tokens.get(index);
        JsonValue child = fields.getOrDefault(key, new JsonValue.ObjectValue(Map.of()));
        fields.put(key, insert(child, tokens, index + 1, replacement));
        return new JsonValue.ObjectValue(fields);
    }

    /**
     * Applies an explicit path write without sparse-array growth or scalar-container guessing.
     *
     * @param value       immutable subtree being edited
     * @param path        decoded JSON Pointer tokens
     * @param index       current decoded path component
     * @param replacement replacement value, absent only for explicit deletion
     * @return the exact updated immutable tree
     */
    private static JsonValue put(JsonValue value, List<String> path, int index, JsonValue replacement) {
        if (index == path.size())
            return replacement;
        if (value instanceof JsonValue.ArrayValue array) {
            String token = path.get(index);
            if (!PatternKit.isMatch(Pattern.get("0|[1-9][0-9]*", 0), token))
                throw invalid("arrayPath");
            int position;
            try {
                position = Integer.parseInt(token);
            } catch (NumberFormatException invalid) {
                throw invalid("arrayPath");
            }
            if (position >= array.values().size())
                throw invalid("sparseArray");
            List<JsonValue> values = new ArrayList<>(array.values());
            values.set(position, put(values.get(position), path, index + 1, replacement));
            return new JsonValue.ArrayValue(values);
        }
        if (!(value instanceof JsonValue.ObjectValue object))
            throw invalid("pathContainer");
        Map<String, JsonValue> values = new TreeMap<>(object.values());
        String key = path.get(index);
        values.put(
                key,
                put(values.getOrDefault(key, new JsonValue.ObjectValue(Map.of())), path, index + 1, replacement));
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Returns an immutable exact-path replacement or explicit deletion without sparse-array mutation.
     *
     * @param value       immutable subtree being edited
     * @param pointer     strict canonical JSON Pointer
     * @param replacement replacement value, absent only for explicit deletion
     * @param remove      whether to remove the exact selected member
     * @return the immutable edited business tree
     */
    public static JsonValue replace(JsonValue value, String pointer, JsonValue replacement, boolean remove) {
        path(pointer);
        if (pointer.isEmpty())
            return remove ? new JsonValue.ObjectValue(Map.of()) : Objects.requireNonNull(replacement);
        if (value == null || !remove && replacement == null)
            throw invalid("replacement");
        return replace(value, JsonSchema.tokens(pointer), 0, replacement, remove);
    }

    /**
     * Validates a strict JSON Pointer and its maximum coordinate length.
     *
     * @param pointer strict canonical JSON Pointer
     */
    private static void path(String pointer) {
        if (pointer == null || pointer.length() > 4096)
            throw invalid("pointer");
        JsonSchema.tokens(pointer);
    }

    /**
     * Tests canonical pointer ancestry using member boundaries, including the root pointer.
     *
     * @param parent possible canonical ancestor pointer
     * @param child  possible canonical descendant pointer
     * @return true when the parent is the root, equals the child or is a complete ancestor pointer
     */
    private static boolean ancestor(String parent, String child) {
        return parent.isEmpty() || parent.equals(child) || child.startsWith(parent + Symbol.SLASH);
    }

    /**
     * Reads an exact JSON Pointer while distinguishing missing members from JSON null.
     *
     * @param value   JSON tree to inspect
     * @param pointer strict canonical JSON Pointer
     * @return the selected JSON node, or Java null for absence
     */
    public static JsonValue at(JsonValue value, String pointer) {
        path(pointer);
        if (value == null)
            return null;
        try {
            return JsonSchema.at(value, pointer);
        } catch (IllegalArgumentException incompatible) {
            return null;
        }
    }

    /**
     * Creates a value-free contract error without exposing sensitive input.
     *
     * @param rule fixed value-free validation rule name
     * @return a value-free definition contract failure
     */
    private static IllegalArgumentException invalid(String rule) {
        return new IllegalArgumentException("Invalid configuration definition: " + rule);
    }

    /**
     * Applies present explicit values over defaults; null, false, zero and empty strings never trigger fallback.
     *
     * @param definition validated definition envelope
     * @return the resolved business tree with explicit values taking priority
     */
    private static JsonValue effectiveDefinition(JsonValue definition) {
        JsonValue result = layer(definition, "default");
        Map<String, JsonValue> explicit = new TreeMap<>();
        entries(definition).forEach((path, entry) -> {
            JsonValue value = ((JsonValue.ObjectValue) entry).values().get("value");
            if (value != null)
                explicit.put(path, value);
        });
        for (var write : explicit.entrySet()) {
            if (write.getKey().isEmpty())
                result = write.getValue();
            else
                result = put(result, JsonSchema.tokens(write.getKey()), 0, write.getValue());
        }
        return result;
    }

    /**
     * Returns an immutable exact-path replacement or explicit deletion without sparse-array mutation.
     *
     * @param value       immutable subtree being edited
     * @param path        decoded JSON Pointer tokens
     * @param index       current decoded path component
     * @param replacement replacement value, absent only for explicit deletion
     * @param remove      whether to remove the exact selected member
     * @return the immutable edited business tree
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
            JsonValue selected = JsonSchema.at(value, JsonSchema.pointer(Normal.EMPTY, member));
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
     * Writes deterministic bounded JSON using sorted object keys and exact decimal numbers.
     *
     * @param value  JSON subtree to serialize
     * @param output canonical JSON output buffer
     * @param depth  current nesting depth, starting at zero
     * @param steps  shared mutable counter of serialized JSON nodes
     */
    private static void append(JsonValue value, StringBuilder output, int depth, long[] steps) {
        if (depth > 64 || ++steps[0] > 100000)
            throw new IllegalArgumentException("JSON budget exceeded");
        if (value instanceof JsonValue.ObjectValue object) {
            output.append(Symbol.C_BRACE_LEFT);
            boolean first = true;
            for (var entry : new TreeMap<>(object.values()).entrySet()) {
                if (!first)
                    output.append(Symbol.C_COMMA);
                first = false;
                output.append(new String(JsonKit.writeValue(new JsonValue.StringValue(entry.getKey())), Charset.UTF_8))
                        .append(Symbol.C_COLON);
                append(entry.getValue(), output, depth + 1, steps);
            }
            output.append(Symbol.C_BRACE_RIGHT);
        } else if (value instanceof JsonValue.ArrayValue array) {
            output.append(Symbol.C_BRACKET_LEFT);
            boolean first = true;
            for (JsonValue child : array.values()) {
                if (!first)
                    output.append(Symbol.C_COMMA);
                first = false;
                append(child, output, depth + 1, steps);
            }
            output.append(Symbol.C_BRACKET_RIGHT);
        } else if (value instanceof JsonValue.NumberValue number) {
            output.append(number.value().stripTrailingZeros().toPlainString());
        } else {
            output.append(new String(JsonKit.writeValue(value), Charset.UTF_8));
        }
    }

    /**
     * Applies a decoded definition path without guessing arrays.
     *
     * @param value       tree
     * @param pointer     exact pointer
     * @param replacement replacement or absent
     * @param remove      explicit removal
     * @return updated tree
     */
    public static JsonValue write(JsonValue value, String pointer, JsonValue replacement, boolean remove) {
        return remove ? replace(value, pointer, null, true)
                : put(value, JsonSchema.tokens(pointer), 0, Objects.requireNonNull(replacement));
    }

    /**
     * Decodes one plaintext definition and its rules once.
     *
     * @param content   canonical JSON envelope
     * @param format    delivery format
     * @param schema    whole Schema
     * @param extension metadata envelope
     * @return immutable parsed definition
     */
    public Document decode(String content, String format, String schema, String extension) {
        Format.valueOf(Objects.requireNonNull(format));
        JsonValue parsed = tree(Objects.requireNonNull(content));
        JsonSchema.validate(new JsonValue.BooleanValue(true), parsed, maximumDepth, maximumSteps);
        if (!(parsed instanceof JsonValue.ObjectValue object))
            throw invalid("definition");
        Map<String, Property> properties = new TreeMap<>();
        Set<String> paths = new HashSet<>();
        for (var e : object.values().entrySet()) {
            if (!paths.add(coordinate(e.getKey())))
                throw invalid("duplicatePath");
            if (!(e.getValue() instanceof JsonValue.ObjectValue attrs)
                    || !Set.of("value", "default", "desc").containsAll(attrs.values().keySet()))
                throw invalid("member");
            JsonValue desc = attrs.values().get("desc");
            if (desc != null && !(desc instanceof JsonValue.StringValue))
                throw invalid("desc");
            properties.put(
                    e.getKey(),
                    new Property(attrs.values().get("value"), attrs.values().get("default"),
                            desc == null ? null : ((JsonValue.StringValue) desc).value()));
        }
        JsonValue whole = tree(schema);
        if (whole != null)
            JsonSchema.requireSchema(whole, maximumDepth, maximumSteps);
        Map<String, Rule> rules = new TreeMap<>();
        JsonValue.ObjectValue management = new JsonValue.ObjectValue(Map.of());
        Protection protection = Protection.NONE;
        if (extension != null) {
            JsonValue raw = tree(extension);
            if (!(raw instanceof JsonValue.ObjectValue ext)
                    || !Set.of("format", "services").containsAll(ext.values().keySet())
                    || !new JsonValue.StringValue(FORMAT).equals(ext.values().get("format")))
                throw invalid("extension");
            JsonValue node = ext.values().get("services");
            if (!(node instanceof JsonValue.ObjectValue services)
                    || !Set.of("management", "keys", "protection").containsAll(services.values().keySet()))
                throw invalid("services");
            if (services.values().get("management") != null) {
                if (!(services.values().get("management") instanceof JsonValue.ObjectValue m)
                        || m.values().containsKey("permission") || m.values().containsKey("app_ids"))
                    throw invalid("management");
                management = m;
            }
            if (services.values().get("protection") != null)
                protection = Protection.valueOf(((JsonValue.StringValue) services.values().get("protection")).value());
            if (services.values().get("keys") != null) {
                if (!(services.values().get("keys") instanceof JsonValue.ObjectValue keys))
                    throw invalid("keys");
                for (var e : keys.values().entrySet()) {
                    path(e.getKey());
                    if (!(e.getValue() instanceof JsonValue.ObjectValue attrs)
                            || !Set.of("sensitive", "schema").containsAll(attrs.values().keySet()))
                        throw invalid("rule");
                    JsonValue sensitive = attrs.values().get("sensitive"), constraint = attrs.values().get("schema");
                    if (sensitive != null && !(sensitive instanceof JsonValue.BooleanValue))
                        throw invalid("sensitive");
                    if (constraint != null)
                        JsonSchema.requireSchema(constraint, maximumDepth, maximumSteps);
                    rules.put(
                            e.getKey(),
                            new Rule(JsonSchema.tokens(e.getKey()), new JsonValue.BooleanValue(true).equals(sensitive),
                                    constraint));
                }
            }
        }
        Document result = new Document(properties, rules, whole, management, protection);
        result.values();
        result.defaults();
        return result;
    }

    /**
     * Converts an explicitly supplied plain object, never guesses definition members.
     *
     * @param values ordinary business object
     * @return definition
     */
    public Document importValues(JsonValue.ObjectValue values) {
        Map<String, Property> properties = new TreeMap<>();
        values.values().forEach((k, v) -> properties.put(k, new Property(v, null, null)));
        return new Document(properties, Map.of(), null, new JsonValue.ObjectValue(Map.of()), Protection.NONE);
    }

    /**
     * Encodes the canonical definition envelope.
     *
     * @param document parsed definition
     * @return stored JSON
     */
    public String content(Document document) {
        return encode(definition(document));
    }

    /**
     * Encodes current non-relationship metadata.
     *
     * @param document parsed definition
     * @return metadata JSON
     */
    public String extension(Document document) {
        Map<String, JsonValue> services = new TreeMap<>(), keys = new TreeMap<>();
        document.rules().forEach((path, rule) -> {
            Map<String, JsonValue> attrs = new TreeMap<>();
            attrs.put("sensitive", new JsonValue.BooleanValue(rule.sensitive()));
            if (rule.schema() != null)
                attrs.put("schema", rule.schema());
            keys.put(path, new JsonValue.ObjectValue(attrs));
        });
        if (!keys.isEmpty())
            services.put("keys", new JsonValue.ObjectValue(keys));
        if (!document.management().values().isEmpty())
            services.put("management", document.management());
        if (document.protection() != Protection.NONE)
            services.put("protection", new JsonValue.StringValue(document.protection().name()));
        return encode(
                new JsonValue.ObjectValue(Map.of(
                        "format",
                        new JsonValue.StringValue(FORMAT),
                        "services",
                        new JsonValue.ObjectValue(services))));
    }

    /**
     * Computes the protected definition digest, excluding editing and publication identity.
     *
     * @param item protected definition
     * @return SHA-256 checksum
     */
    public String checksum(Item item) {
        Map<String, JsonValue> fields = new TreeMap<>();
        fields.put("type", JsonKit.toValue(item.getType()));
        fields.put("code", JsonKit.toValue(item.getCode()));
        fields.put("format", JsonKit.toValue(item.getFormat()));
        fields.put("content", JsonKit.toValue(item.getContent()));
        fields.put("schema", JsonKit.toValue(item.getSchema()));
        fields.put("encrypted", JsonKit.toValue(item.getEncrypted()));
        fields.put("extension", JsonKit.toValue(item.getExtension()));
        fields.put("description", JsonKit.toValue(item.getDescription()));
        return Builder.sha256(encode(new JsonValue.ObjectValue(fields)));
    }

    /**
     * Validates structure and, when requested, effective and fallback constraints.
     *
     * @param document parsed input
     * @param level    requested validation
     */
    public void validate(Document document, ValidationLevel level) {
        JsonSchema.validate(new JsonValue.BooleanValue(true), definition(document), maximumDepth, maximumSteps);
        if (level == ValidationLevel.FULL) {
            check(document, document.effective());
            for (var e : document.properties().entrySet())
                if (e.getValue().defaultValue() != null)
                    check(
                            document,
                            put(
                                    document.effective(),
                                    JsonSchema.tokens(coordinate(e.getKey())),
                                    0,
                                    e.getValue().defaultValue()));
        }
    }

    /**
     * Validates one effective projection without values in diagnostics.
     *
     * @param document constraints
     * @param value    effective tree
     */
    private void check(Document document, JsonValue value) {
        if (!JsonSchema.validate(
                document.schema() == null ? new JsonValue.BooleanValue(true) : document.schema(),
                value,
                maximumDepth,
                maximumSteps).isEmpty())
            throw invalid("schema");
        for (var e : document.rules().entrySet()) {
            JsonValue selected = at(value, e.getKey());
            if (selected != null && e.getValue().schema() != null
                    && !JsonSchema.validate(e.getValue().schema(), selected, maximumDepth, maximumSteps).isEmpty())
                throw invalid("keySchema");
        }
    }

    /**
     * Renders final business values using supported delivery formats.
     *
     * @param value  effective tree
     * @param format target format
     * @return encoded business content
     */
    public String format(JsonValue value, Format format) {
        if (format == Format.JSON || format == Format.YAML)
            return encode(value);
        if (format == Format.TEXT)
            return value instanceof JsonValue.StringValue text ? text.value() : encode(value);
        if (!(value instanceof JsonValue.ObjectValue object))
            throw invalid("properties");
        StringBuilder out = new StringBuilder();
        object.values().forEach(
                (k, v) -> out.append(k).append('=')
                        .append(v instanceof JsonValue.StringValue text ? text.value() : encode(v)).append('\n'));
        return out.toString();
    }

    /**
     * Storage protection, not authorization or release state.
     *
     * @author Kimi Liu
     */
    public enum Protection {
        /**
         * Unprotected definition.
         */
        NONE,
        /**
         * Individually protected sensitive members.
         */
        KEYS,
        /**
         * Whole protected definition.
         */
        WHOLE

    }

    /**
     * Requested definition validation.
     *
     * @author Kimi Liu
     */
    public enum ValidationLevel {
        /**
         * Definition and rule syntax.
         */
        SYNTAX,
        /**
         * Effective value and Schema constraints as well.
         */
        FULL

    }

    /**
     * Exact value/default members; Java null denotes absence.
     *
     * @param value        explicit member
     * @param defaultValue fallback member
     * @param desc         display description
     * @author Kimi Liu
     */
    public record Property(JsonValue value, JsonValue defaultValue, String desc) {

    }

    /**
     * Predecoded sensitivity/Schema path.
     *
     * @param segments  decoded tokens
     * @param sensitive storage protection
     * @param schema    optional constraint
     * @author Kimi Liu
     */
    public record Rule(List<String> segments, boolean sensitive, JsonValue schema) {

        /**
         * Freezes exact path tokens.
         *
         * @param segments  decoded tokens
         * @param sensitive storage protection
         * @param schema    optional constraint
         */
        public Rule {
            segments = List.copyOf(segments);
        }

    }

    /**
     * Immutable, single parsed configuration representation.
     *
     * @param properties original keys and members
     * @param rules      canonical pointer rules
     * @param schema     whole Schema
     * @param management supported management constraints
     * @param protection storage mode
     * @author Kimi Liu
     */
    public record Document(Map<String, Property> properties, Map<String, Rule> rules, JsonValue schema,
            JsonValue.ObjectValue management, Protection protection) {

        /**
         * Freezes parsed collections.
         *
         * @param properties definition members
         * @param rules      exact rules
         * @param schema     whole Schema
         * @param management non-relationship metadata
         * @param protection storage mode
         */
        public Document {
            properties = Collections.unmodifiableMap(new TreeMap<>(properties));
            rules = Collections.unmodifiableMap(new TreeMap<>(rules));
            Objects.requireNonNull(management);
            Objects.requireNonNull(protection);
        }

        /**
         * Materializes explicit values without parsing.
         *
         * @return explicit value tree
         */
        public JsonValue values() {
            return layer(definition(this), "value");
        }

        /**
         * Materializes defaults without parsing.
         *
         * @return fallback tree
         */
        public JsonValue defaults() {
            return layer(definition(this), "default");
        }

        /**
         * Returns default layers excluding descendants hidden by explicit ancestors.
         *
         * @return applicable fallback tree
         */
        public JsonValue applicableDefaults() {
            Map<String, Property> applicable = new TreeMap<>(properties);
            properties.forEach((key, property) -> {
                String path = coordinate(key);
                if (properties.entrySet().stream().anyMatch(
                        e -> e.getValue().value() != null && !coordinate(e.getKey()).equals(path)
                                && ancestor(coordinate(e.getKey()), path)))
                    applicable.put(key, new Property(property.value(), null, property.desc()));
            });
            return layer(definition(new Document(applicable, rules, schema, management, protection)), "default");
        }

        /**
         * Applies explicit values over defaults.
         *
         * @return exact effective tree
         */
        public JsonValue effective() {
            return effectiveDefinition(definition(this));
        }

    }

}
