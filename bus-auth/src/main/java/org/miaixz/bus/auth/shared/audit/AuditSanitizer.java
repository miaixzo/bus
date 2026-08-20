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
package org.miaixz.bus.auth.shared.audit;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Converts explicitly typed audit views to provider-neutral JSON without inspecting security-sensitive field names.
 * <p>
 * A caller exposes structured audit data through {@link View} and marks every secret-bearing value by implementing
 * {@link SensitiveValue}. Sensitive values, unknown complex objects, non-finite numbers, and excessive nesting are
 * replaced by the same immutable marker. The sanitizer never invokes an arbitrary object's {@code toString()} method.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuditSanitizer {

    /**
     * Maximum typed view or collection depth inspected before fail-closed redaction.
     */
    private static final int MAX_DEPTH = Normal._16;

    /**
     * Shared immutable marker used for every value that cannot enter audit output.
     */
    private static final JsonValue.StringValue REDACTED = new JsonValue.StringValue("[REDACTED]");

    /**
     * Creates a stateless typed audit sanitizer.
     */
    public AuditSanitizer() {
        // No initialization required.
    }

    /**
     * Converts one typed view to an ordered object at the current nesting depth.
     *
     * @param view  typed audit projection
     * @param depth current nesting depth
     * @return detached sanitized JSON object
     * @throws IllegalArgumentException if the member list or a member is {@code null}
     */
    private static JsonValue.ObjectValue object(final View view, final int depth) {
        final List<Member> members = Assert.notNull(view.members(), "Audit view members must not be null");
        final Map<String, JsonValue> values = new LinkedHashMap<>(members.size());
        for (Member member : members) {
            final Member checked = Assert.notNull(member, "Audit view member must not be null");
            Assert.isTrue(
                    values.putIfAbsent(checked.name(), value(checked.value(), depth + 1)) == null,
                    "Audit view contains duplicate member: {}",
                    checked.name());
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Converts one explicitly selected audit value without using field-name heuristics.
     *
     * @param candidate selected audit value
     * @param depth     current typed-view or collection depth
     * @return immutable provider-neutral value or the redaction marker
     */
    private static JsonValue value(final Object candidate, final int depth) {
        if (candidate instanceof SensitiveValue || depth > MAX_DEPTH) {
            return REDACTED;
        }
        if (candidate == null || candidate instanceof JsonValue.NullValue) {
            return JsonValue.NullValue.instance();
        }
        if (candidate instanceof View view) {
            return object(view, depth);
        }
        if (candidate instanceof Collection<?> collection) {
            final List<JsonValue> values = new ArrayList<>(collection.size());
            for (Object element : collection) {
                values.add(value(element, depth + 1));
            }
            return new JsonValue.ArrayValue(values);
        }
        if (candidate instanceof JsonValue.StringValue || candidate instanceof JsonValue.NumberValue
                || candidate instanceof JsonValue.BooleanValue) {
            return (JsonValue) candidate;
        }
        if (candidate instanceof JsonValue) {
            return REDACTED;
        }
        if (candidate instanceof String text) {
            return new JsonValue.StringValue(text);
        }
        if (candidate instanceof Character character) {
            return new JsonValue.StringValue(String.valueOf(character));
        }
        if (candidate instanceof Boolean bool) {
            return new JsonValue.BooleanValue(bool);
        }
        if (candidate instanceof BigDecimal decimal) {
            return new JsonValue.NumberValue(decimal);
        }
        if (candidate instanceof BigInteger integer) {
            return new JsonValue.NumberValue(new BigDecimal(integer));
        }
        if (candidate instanceof Byte || candidate instanceof Short || candidate instanceof Integer
                || candidate instanceof Long) {
            return new JsonValue.NumberValue(BigDecimal.valueOf(((Number) candidate).longValue()));
        }
        if (candidate instanceof Float floating) {
            return Float.isFinite(floating) ? new JsonValue.NumberValue(BigDecimal.valueOf(floating.doubleValue()))
                    : REDACTED;
        }
        if (candidate instanceof Double floating) {
            return Double.isFinite(floating) ? new JsonValue.NumberValue(BigDecimal.valueOf(floating)) : REDACTED;
        }
        if (candidate instanceof Enum<?> enumeration) {
            return new JsonValue.StringValue(enumeration.name());
        }
        return REDACTED;
    }

    /**
     * Converts one typed view to a detached provider-neutral JSON object.
     *
     * @param view deliberate non-sensitive audit projection
     * @return immutable sanitized audit object
     * @throws IllegalArgumentException if the view or its member list contains {@code null}
     */
    public JsonValue.ObjectValue sanitize(final View view) {
        return object(Assert.notNull(view, "Audit view must not be null"), 0);
    }

    /**
     * Marks a value as security-sensitive regardless of its Java type, member name, or nesting location.
     * <p>
     * Implementations expose no value through this contract. The sanitizer recognizes the marker before considering
     * scalar conversion and always emits the redaction marker.
     * </p>
     *
     * @author Kimi Liu
     */
    public interface SensitiveValue {
        // Marker used by typed audit views.

    }

    /**
     * Exposes a deliberate non-sensitive projection of one domain object for audit output.
     *
     * @author Kimi Liu
     */
    public interface View {

        /**
         * Returns ordered audit members whose values are scalars, nested views, collections, or sensitive markers.
         *
         * @return immutable or caller-owned ordered member list
         */
        List<Member> members();

    }

    /**
     * Associates one semantic audit member name with its explicitly selected value.
     *
     * @param name  non-blank output member name
     * @param value selected value; {@code null} becomes JSON null
     * @author Kimi Liu
     */
    public record Member(String name, Object value) {

        /**
         * Creates one typed audit member without classifying sensitivity from its name.
         *
         * @param name  non-blank output member name
         * @param value selected audit value
         */
        public Member {
            Assert.notBlank(name, "Audit view member name must not be blank");
        }

    }

}
