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
package org.miaixz.bus.extra.json;

import java.math.BigDecimal;
import java.util.*;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;

/**
 * Validates the explicitly supported, local-only JSON Schema vocabulary using exact provider-neutral values. Unknown
 * validation keywords and reference cycles fail closed; annotations never mutate input or fetch resources.
 *
 * @author Kimi Liu
 */
public final class JsonSchema {

    /**
     * Explicit validation vocabulary shared by metadata validation and instance evaluation.
     */
    private static final Set<String> KEYWORDS = Set.of(
            "$schema",
            "$id",
            "$defs",
            "$ref",
            "title",
            "description",
            "default",
            "examples",
            "type",
            "properties",
            "required",
            "additionalProperties",
            "items",
            "minItems",
            "maxItems",
            "uniqueItems",
            "minProperties",
            "maxProperties",
            "enum",
            "const",
            "minimum",
            "maximum",
            "exclusiveMinimum",
            "exclusiveMaximum",
            "multipleOf",
            "minLength",
            "maxLength",
            "pattern",
            "allOf",
            "anyOf",
            "oneOf",
            "not",
            "if",
            "then",
            "else");
    /**
     * Supported JSON value types, with integers compared without floating-point coercion.
     */
    private static final Set<String> TYPES = Set
            .of("object", "array", "string", "number", "integer", "boolean", "null");
    /**
     * Nonnegative integral bounds in the supported vocabulary.
     */
    private static final Set<String> COUNTS = Set
            .of("minItems", "maxItems", "minProperties", "maxProperties", "minLength", "maxLength");
    /**
     * Exact decimal bounds in the supported vocabulary.
     */
    private static final Set<String> NUMBERS = Set
            .of("minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum", "multipleOf");
    /**
     * Fixed diagnostic output cap shared by Schema evaluation and configuration metadata consumers. Reaching this cap
     * limits returned findings without converting a failed evaluation into success. Parser and evaluation work budgets
     * remain independent caller-supplied limits.
     */
    public static final int MAX_FINDINGS = Normal._256;

    /**
     * Prevents instantiation of the stateless entry point.
     */
    private JsonSchema() {
        // No initialization required.
    }

    /**
     * Describes one failed constraint without including the rejected value or source content.
     *
     * @param path    canonical instance JSON Pointer; empty means the root
     * @param keyword fixed constraint name
     * @author Kimi Liu
     */
    public record Violation(String path, String keyword) {
    }

    /**
     * Validates an immutable schema before it is saved or evaluated.
     *
     * @param schema       boolean or object schema
     * @param maximumDepth positive structural and evaluation depth bound
     * @param maximumSteps positive node and evaluation-work bound
     */
    public static void requireSchema(JsonValue schema, int maximumDepth, long maximumSteps) {
        Evaluation evaluation = new Evaluation(schema, maximumDepth, maximumSteps);
        evaluation.bound(schema, 0);
        evaluation.steps = 0;
        evaluation.inspect(schema, new HashSet<>(), 0);
    }

    /**
     * Returns bounded, safe findings for an immutable instance without coercion or default insertion. Malformed schemas
     * and exhausted bounds throw a sanitized argument exception instead of silently passing.
     *
     * @param schema       boolean or object schema
     * @param instance     exact JSON instance
     * @param maximumDepth positive structural and evaluation depth bound
     * @param maximumSteps positive node and evaluation-work bound
     * @return immutable path-and-keyword findings; empty means all supported constraints passed
     */
    public static List<Violation> validate(JsonValue schema, JsonValue instance, int maximumDepth, long maximumSteps) {
        requireSchema(schema, maximumDepth, maximumSteps);
        Evaluation evaluation = new Evaluation(schema, maximumDepth, maximumSteps);
        evaluation.bound(instance, 0);
        evaluation.steps = 0;
        List<Violation> findings = new ArrayList<>();
        try {
            evaluation.matches(schema, instance, "", findings, 0);
        } catch (ArithmeticException failure) {
            throw invalid("numericBudget");
        }
        return List.copyOf(findings);
    }

    /**
     * Compares exact values through the same canonical decimal and object-member semantics used by enum and const.
     * Values are bounded independently, without adding an artificial schema wrapper to their structural depth.
     *
     * @param left         first immutable JSON value
     * @param right        second immutable JSON value
     * @param maximumDepth positive structural and comparison depth bound
     * @param maximumSteps positive node and comparison-work bound
     * @return whether both values are semantically equal
     */
    public static boolean equivalent(JsonValue left, JsonValue right, int maximumDepth, long maximumSteps) {
        Evaluation evaluation = new Evaluation(new JsonValue.BooleanValue(true), maximumDepth, maximumSteps);
        evaluation.bound(left, 0);
        evaluation.steps = 0;
        evaluation.bound(right, 0);
        evaluation.steps = 0;
        try {
            return evaluation.canonical(left, 0).equals(evaluation.canonical(right, 0));
        } catch (ArithmeticException failure) {
            throw invalid("numericBudget");
        }
    }

    /**
     * Appends one exact member name to a canonical JSON Pointer without interpreting dots or slashes as hierarchy.
     *
     * @param pointer parent canonical pointer
     * @param member  exact object member or canonical array index
     * @return pointer with RFC 6901 escaping for the supplied member
     */
    public static String pointer(String pointer, String member) {
        if (pointer == null || member == null)
            throw invalid("pointer");
        tokens(pointer);
        return pointer + "/" + member.replace("~", "~0").replace("/", "~1");
    }

    /**
     * Validates every escape in a canonical JSON Pointer before any lookup can stop at a missing member.
     *
     * @param pointer empty root pointer or slash-prefixed canonical pointer
     * @return immutable exact decoded member names; empty for the root
     */
    public static List<String> tokens(String pointer) {
        if (pointer == null || !pointer.isEmpty() && !pointer.startsWith("/"))
            throw invalid("pointer");
        if (pointer.isEmpty())
            return List.of();
        List<String> names = new ArrayList<>();
        for (String token : pointer.substring(1).split("/", -1)) {
            String name = token.replace("~1", "/").replace("~0", "~");
            if (!name.replace("~", "~0").replace("/", "~1").equals(token))
                throw invalid("pointer");
            names.add(name);
        }
        return List.copyOf(names);
    }

    /**
     * Resolves a canonical JSON Pointer from an already bounded immutable tree. Missing members return null; malformed
     * escapes and array indices are rejected rather than guessed.
     *
     * @param root    exact JSON tree
     * @param pointer empty root pointer or slash-prefixed canonical pointer
     * @return selected value, or null when a valid pointer has no corresponding member
     */
    public static JsonValue at(JsonValue root, String pointer) {
        if (root == null)
            throw invalid("pointer");
        List<String> names = tokens(pointer);
        JsonValue current = root;
        for (String name : names) {
            if (current instanceof JsonValue.ObjectValue object)
                current = object.values().get(name);
            else if (current instanceof JsonValue.ArrayValue array) {
                if (name.isEmpty() || name.length() > 10 || name.length() > 1 && name.charAt(0) == '0'
                        || !name.chars().allMatch(c -> c >= '0' && c <= '9'))
                    throw invalid("pointer");
                long index;
                try {
                    index = Long.parseLong(name);
                } catch (NumberFormatException failure) {
                    throw invalid("pointer");
                }
                current = index < array.values().size() ? array.values().get((int) index) : null;
            } else
                current = null;
            if (current == null)
                return null;
        }
        return current;
    }

    /**
     * Creates an exception containing only a fixed vocabulary name and no schema or instance values.
     *
     * @param keyword fixed invalid vocabulary name
     * @return sanitized schema exception
     */
    private static IllegalArgumentException invalid(String keyword) {
        return new IllegalArgumentException("Invalid or unsupported JSON Schema: " + keyword);
    }

    /**
     * Holds invocation-local bounds and recursion state; no caller data or findings are cached between evaluations.
     *
     * @author Kimi Liu
     */
    private static final class Evaluation {

        /**
         * Immutable root used only to resolve local schema references.
         */
        private final JsonValue root;
        /**
         * Maximum permitted schema or instance evaluation depth.
         */
        private final int maximumDepth;
        /**
         * Maximum permitted tree nodes or evaluation work units.
         */
        private final long maximumSteps;
        /**
         * Work consumed by the current phase.
         */
        private long steps;

        /**
         * Creates an isolated bounded evaluation.
         *
         * @param root         immutable root schema
         * @param maximumDepth positive depth bound
         * @param maximumSteps positive work bound
         */
        private Evaluation(JsonValue root, int maximumDepth, long maximumSteps) {
            if (root == null || maximumDepth < 1 || maximumDepth > 256 || maximumSteps < 1)
                throw invalid("bounds");
            this.root = root;
            this.maximumDepth = maximumDepth;
            this.maximumSteps = maximumSteps;
        }

        /**
         * Charges one work unit before recursion or comparison.
         *
         * @param depth current recursion depth
         */
        private void tick(int depth) {
            if (depth > maximumDepth || steps == maximumSteps)
                throw invalid("evaluationBudget");
            steps++;
        }

        /**
         * Bounds an immutable value tree independently of its later schema evaluation.
         *
         * @param value non-null tree node
         * @param depth current structural depth
         */
        private void bound(JsonValue value, int depth) {
            tick(depth);
            if (value == null)
                throw invalid("value");
            if (value instanceof JsonValue.NumberValue number && (Math.abs((long) number.value().scale()) > maximumSteps
                    || number.value().precision() > maximumSteps))
                throw invalid("numericBudget");
            if (value instanceof JsonValue.ObjectValue object) {
                for (JsonValue child : object.values().values())
                    bound(child, depth + 1);
            } else if (value instanceof JsonValue.ArrayValue array) {
                for (JsonValue child : array.values())
                    bound(child, depth + 1);
            }
        }

        /**
         * Validates every supported schema position, including unused alternatives and definitions.
         *
         * @param schema     current boolean or object schema
         * @param references active local references used for cycle detection
         * @param depth      current schema recursion depth
         */
        private void inspect(JsonValue schema, Set<String> references, int depth) {
            tick(depth);
            if (schema instanceof JsonValue.BooleanValue)
                return;
            if (!(schema instanceof JsonValue.ObjectValue object))
                throw invalid("schema");
            Map<String, JsonValue> fields = object.values();
            if (!KEYWORDS.containsAll(fields.keySet()))
                throw invalid("keyword");
            for (var entry : fields.entrySet()) {
                String key = entry.getKey();
                JsonValue value = entry.getValue();
                if (COUNTS.contains(key)) {
                    BigDecimal number = decimal(value, key);
                    if (number.signum() < 0 || number.stripTrailingZeros().scale() > 0)
                        throw invalid(key);
                } else if (NUMBERS.contains(key)) {
                    if ("multipleOf".equals(key) && decimal(value, key).signum() <= 0)
                        throw invalid(key);
                    decimal(value, key);
                } else
                    switch (key) {
                        case "$schema", "$id", "title", "description", "pattern" -> {
                            String text = text(value, key);
                            if ("pattern".equals(key)) {
                                try {
                                    Pattern.get(text, 0);
                                } catch (RuntimeException failure) {
                                    throw invalid("pattern");
                                }
                            }
                        }
                        case "$ref" -> {
                            String reference = text(value, key);
                            if (!references.add(reference))
                                throw invalid("referenceCycle");
                            inspect(resolve(reference), references, depth + 1);
                            references.remove(reference);
                        }
                        case "type" -> {
                            List<String> names = value instanceof JsonValue.StringValue string ? List.of(string.value())
                                    : names(value, key);
                            if (names.isEmpty() || !TYPES.containsAll(names))
                                throw invalid(key);
                        }
                        case "$defs", "properties" -> {
                            if (!(value instanceof JsonValue.ObjectValue map))
                                throw invalid(key);
                            for (JsonValue child : map.values().values())
                                inspect(child, references, depth + 1);
                        }
                        case "required" -> names(value, key);
                        case "allOf", "anyOf", "oneOf" -> {
                            if (!(value instanceof JsonValue.ArrayValue array) || array.values().isEmpty())
                                throw invalid(key);
                            for (JsonValue child : array.values())
                                inspect(child, references, depth + 1);
                        }
                        case "not", "if", "then", "else", "additionalProperties", "items" -> inspect(
                                value,
                                references,
                                depth + 1);
                        case "enum" -> {
                            if (!(value instanceof JsonValue.ArrayValue array) || array.values().isEmpty())
                                throw invalid(key);
                            Set<String> unique = new HashSet<>();
                            for (JsonValue child : array.values())
                                if (!unique.add(canonical(child, depth + 1)))
                                    throw invalid(key);
                        }
                        case "uniqueItems" -> {
                            if (!(value instanceof JsonValue.BooleanValue))
                                throw invalid(key);
                        }
                        case "examples" -> {
                            if (!(value instanceof JsonValue.ArrayValue))
                                throw invalid(key);
                        }
                        default -> {
                            /* const and default retain their exact annotation or comparison values. */
                        }
                    }
            }
        }

        /**
         * Evaluates one schema and its siblings using exact immutable values and a shared work budget.
         *
         * @param schema   already validated schema node
         * @param value    exact instance node
         * @param path     canonical instance pointer
         * @param findings bounded destination for safe findings
         * @param depth    current evaluation depth
         * @return whether this schema and all applicable constraints passed
         */
        private boolean matches(JsonValue schema, JsonValue value, String path, List<Violation> findings, int depth) {
            tick(depth);
            if (schema instanceof JsonValue.BooleanValue flag)
                return check(flag.value(), path, "falseSchema", findings);
            Map<String, JsonValue> fields = ((JsonValue.ObjectValue) schema).values();
            boolean valid = true;
            if (fields.containsKey("$ref"))
                valid &= matches(resolve(text(fields.get("$ref"), "$ref")), value, path, findings, depth + 1);
            if (fields.containsKey("type")) {
                JsonValue types = fields.get("type");
                List<String> allowed = types instanceof JsonValue.StringValue string ? List.of(string.value())
                        : names(types, "type");
                valid &= check(allowed.stream().anyMatch(type -> type(value, type)), path, "type", findings);
            }
            if (fields.containsKey("const"))
                valid &= check(
                        canonical(value, depth + 1).equals(canonical(fields.get("const"), depth + 1)),
                        path,
                        "const",
                        findings);
            if (fields.get("enum") instanceof JsonValue.ArrayValue choices) {
                String actual = canonical(value, depth + 1);
                boolean found = false;
                for (JsonValue choice : choices.values())
                    found |= actual.equals(canonical(choice, depth + 1));
                valid &= check(found, path, "enum", findings);
            }
            if (value instanceof JsonValue.ObjectValue object)
                valid &= object(fields, object, path, findings, depth);
            if (value instanceof JsonValue.ArrayValue array)
                valid &= array(fields, array, path, findings, depth);
            if (value instanceof JsonValue.StringValue string) {
                int length = string.value().codePointCount(0, string.value().length());
                valid &= limits(fields, BigDecimal.valueOf(length), "minLength", "maxLength", path, findings);
                if (fields.containsKey("pattern"))
                    valid &= check(
                            Pattern.get(text(fields.get("pattern"), "pattern"), 0).matcher(string.value()).find(),
                            path,
                            "pattern",
                            findings);
            }
            if (value instanceof JsonValue.NumberValue number) {
                valid &= limits(fields, number.value(), "minimum", "maximum", path, findings);
                if (fields.containsKey("exclusiveMinimum"))
                    valid &= check(
                            number.value().compareTo(decimal(fields.get("exclusiveMinimum"), "exclusiveMinimum")) > 0,
                            path,
                            "exclusiveMinimum",
                            findings);
                if (fields.containsKey("exclusiveMaximum"))
                    valid &= check(
                            number.value().compareTo(decimal(fields.get("exclusiveMaximum"), "exclusiveMaximum")) < 0,
                            path,
                            "exclusiveMaximum",
                            findings);
                if (fields.containsKey("multipleOf"))
                    valid &= check(
                            number.value().remainder(decimal(fields.get("multipleOf"), "multipleOf")).signum() == 0,
                            path,
                            "multipleOf",
                            findings);
            }
            for (String key : List.of("allOf", "anyOf", "oneOf")) {
                if (!(fields.get(key) instanceof JsonValue.ArrayValue alternatives))
                    continue;
                int matched = 0;
                for (JsonValue alternative : alternatives.values())
                    if (matches(alternative, value, path, new ArrayList<>(), depth + 1))
                        matched++;
                valid &= check(
                        "allOf".equals(key) ? matched == alternatives.values().size()
                                : "anyOf".equals(key) ? matched > 0 : matched == 1,
                        path,
                        key,
                        findings);
            }
            if (fields.containsKey("not"))
                valid &= check(
                        !matches(fields.get("not"), value, path, new ArrayList<>(), depth + 1),
                        path,
                        "not",
                        findings);
            if (fields.containsKey("if")) {
                String branch = matches(fields.get("if"), value, path, new ArrayList<>(), depth + 1) ? "then" : "else";
                if (fields.containsKey(branch))
                    valid &= matches(fields.get(branch), value, path, findings, depth + 1);
            }
            return valid;
        }

        /**
         * Applies object member, required-member and additional-member constraints.
         *
         * @param fields   validated schema fields
         * @param object   exact object instance
         * @param path     instance pointer
         * @param findings safe finding destination
         * @param depth    current depth
         * @return whether all applicable object constraints passed
         */
        private boolean object(
                Map<String, JsonValue> fields,
                JsonValue.ObjectValue object,
                String path,
                List<Violation> findings,
                int depth) {
            boolean valid = limits(
                    fields,
                    BigDecimal.valueOf(object.values().size()),
                    "minProperties",
                    "maxProperties",
                    path,
                    findings);
            if (fields.containsKey("required"))
                for (String name : names(fields.get("required"), "required")) {
                    tick(depth);
                    valid &= check(object.values().containsKey(name), pointer(path, name), "required", findings);
                }
            Map<String, JsonValue> properties = fields.get("properties") instanceof JsonValue.ObjectValue map
                    ? map.values()
                    : Map.of();
            for (var entry : object.values().entrySet()) {
                tick(depth);
                JsonValue child = properties.get(entry.getKey());
                if (child == null)
                    child = fields.get("additionalProperties");
                if (child != null)
                    valid &= matches(child, entry.getValue(), pointer(path, entry.getKey()), findings, depth + 1);
            }
            return valid;
        }

        /**
         * Applies item, count and semantic uniqueness constraints without floating-point conversion.
         *
         * @param fields   validated schema fields
         * @param array    exact array instance
         * @param path     instance pointer
         * @param findings safe finding destination
         * @param depth    current depth
         * @return whether all applicable array constraints passed
         */
        private boolean array(
                Map<String, JsonValue> fields,
                JsonValue.ArrayValue array,
                String path,
                List<Violation> findings,
                int depth) {
            boolean valid = limits(
                    fields,
                    BigDecimal.valueOf(array.values().size()),
                    "minItems",
                    "maxItems",
                    path,
                    findings);
            boolean unique = fields.get("uniqueItems") instanceof JsonValue.BooleanValue flag && flag.value();
            Set<String> seen = new HashSet<>();
            for (int index = 0; index < array.values().size(); index++) {
                tick(depth);
                JsonValue child = array.values().get(index);
                String childPath = pointer(path, Integer.toString(index));
                if (fields.containsKey("items"))
                    valid &= matches(fields.get("items"), child, childPath, findings, depth + 1);
                if (unique)
                    valid &= check(seen.add(canonical(child, depth + 1)), childPath, "uniqueItems", findings);
            }
            return valid;
        }

        /**
         * Applies optional inclusive decimal or integral lower and upper bounds.
         *
         * @param fields   validated schema fields
         * @param actual   exact value or count
         * @param minimum  lower-bound vocabulary name
         * @param maximum  upper-bound vocabulary name
         * @param path     instance pointer
         * @param findings safe finding destination
         * @return whether both supplied bounds passed
         */
        private boolean limits(
                Map<String, JsonValue> fields,
                BigDecimal actual,
                String minimum,
                String maximum,
                String path,
                List<Violation> findings) {
            boolean valid = true;
            if (fields.containsKey(minimum))
                valid &= check(actual.compareTo(decimal(fields.get(minimum), minimum)) >= 0, path, minimum, findings);
            if (fields.containsKey(maximum))
                valid &= check(actual.compareTo(decimal(fields.get(maximum), maximum)) <= 0, path, maximum, findings);
            return valid;
        }

        /**
         * Checks one declared type without coercing scalar values.
         *
         * @param value exact JSON instance
         * @param type  validated vocabulary type
         * @return whether the instance has that type
         */
        private boolean type(JsonValue value, String type) {
            return switch (type) {
                case "object" -> value instanceof JsonValue.ObjectValue;
                case "array" -> value instanceof JsonValue.ArrayValue;
                case "string" -> value instanceof JsonValue.StringValue;
                case "number" -> value instanceof JsonValue.NumberValue;
                case "integer" -> value instanceof JsonValue.NumberValue number
                        && number.value().stripTrailingZeros().scale() <= 0;
                case "boolean" -> value instanceof JsonValue.BooleanValue;
                case "null" -> value instanceof JsonValue.NullValue;
                default -> false;
            };
        }

        /**
         * Resolves a local, non-percent-encoded JSON Pointer fragment without any network or file access.
         *
         * @param reference exact local reference string
         * @return referenced schema node
         */
        private JsonValue resolve(String reference) {
            if (!reference.startsWith("#") || reference.indexOf('%') >= 0)
                throw invalid("$ref");
            JsonValue selected = at(root, reference.substring(1));
            if (selected == null)
                throw invalid("$ref");
            return selected;
        }

        /**
         * Canonicalizes values for enum, const and uniqueness using exact decimal equality and unordered object
         * members.
         *
         * @param value exact input value
         * @param depth current evaluation depth
         * @return stable JSON comparison representation
         */
        private String canonical(JsonValue value, int depth) {
            return new String(JsonKit.writeValue(normalized(value, depth)), Charset.UTF_8);
        }

        /**
         * Normalizes comparison-only member order and numeric scale without modifying the original immutable input.
         *
         * @param value exact input value
         * @param depth current comparison depth
         * @return comparison-only normalized tree
         */
        private JsonValue normalized(JsonValue value, int depth) {
            tick(depth);
            if (value instanceof JsonValue.NumberValue number)
                return new JsonValue.NumberValue(number.value().stripTrailingZeros());
            if (value instanceof JsonValue.ObjectValue object) {
                Map<String, JsonValue> sorted = new TreeMap<>();
                object.values().forEach((key, child) -> sorted.put(key, normalized(child, depth + 1)));
                return new JsonValue.ObjectValue(sorted);
            }
            if (value instanceof JsonValue.ArrayValue array) {
                List<JsonValue> children = new ArrayList<>();
                for (JsonValue child : array.values())
                    children.add(normalized(child, depth + 1));
                return new JsonValue.ArrayValue(children);
            }
            return value;
        }

        /**
         * Reads a strict string without converting numbers or booleans.
         *
         * @param value   candidate value
         * @param keyword fixed vocabulary name for errors
         * @return exact string
         */
        private String text(JsonValue value, String keyword) {
            if (!(value instanceof JsonValue.StringValue string))
                throw invalid(keyword);
            return string.value();
        }

        /**
         * Reads an exact decimal without accepting numeric strings.
         *
         * @param value   candidate value
         * @param keyword fixed vocabulary name for errors
         * @return exact decimal
         */
        private BigDecimal decimal(JsonValue value, String keyword) {
            if (!(value instanceof JsonValue.NumberValue number))
                throw invalid(keyword);
            return number.value();
        }

        /**
         * Reads a unique string array used by required members and type alternatives.
         *
         * @param value   candidate array
         * @param keyword fixed vocabulary name for errors
         * @return exact names in their original order
         */
        private List<String> names(JsonValue value, String keyword) {
            if (!(value instanceof JsonValue.ArrayValue array))
                throw invalid(keyword);
            List<String> names = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (JsonValue child : array.values()) {
                tick(0);
                String name = text(child, keyword);
                if (!seen.add(name))
                    throw invalid(keyword);
                names.add(name);
            }
            return names;
        }

        /**
         * Appends one safe finding while retaining failure status after the finding cap is reached.
         *
         * @param passed   constraint outcome
         * @param path     canonical instance pointer
         * @param keyword  fixed constraint name
         * @param findings bounded destination
         * @return the original constraint outcome
         */
        private boolean check(boolean passed, String path, String keyword, List<Violation> findings) {
            if (!passed && findings.size() < MAX_FINDINGS)
                findings.add(new Violation(path, keyword));
            return passed;
        }
    }

}
