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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Associates an exact JSON object member vocabulary with a Java record instead of a parallel string declaration.
 * <p>
 * Each record component name is the exact JSON member name. Verifier construction derives and freezes the vocabulary
 * from the record, so adding, removing, or renaming a component automatically changes validation. This utility does not
 * instantiate the record, coerce values, select a JSON provider, or discover arbitrary application classes.
 * </p>
 *
 * @param <T> record type that owns the JSON object vocabulary
 * @author Kimi Liu
 */
public final class JsonRecordVerifier<T extends Record> {

    /**
     * Associates a record component with an exact JSON member that is not a legal or suitable Java identifier.
     *
     * @author Kimi Liu
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.RECORD_COMPONENT)
    public @interface Member {

        /**
         * Returns the exact case-sensitive JSON member name.
         *
         * @return exact JSON member name
         */
        String value();

    }

    /**
     * Record class that owns the accepted member names.
     */
    private final Class<T> type;

    /**
     * Component-derived exact JSON members associated with their declared Java types.
     */
    private final Map<String, RecordComponent> members;

    /**
     * Creates an immutable verifier from one explicit record class.
     *
     * @param type record class whose components use exact JSON member names
     */
    private JsonRecordVerifier(final Class<T> type) {
        this.type = Assert.notNull(type, "JSON record verifier type must not be null");
        if (!type.isRecord()) {
            throw new ValidateException("JSON record verifier type must be a record: " + type.getName());
        }
        final Map<String, RecordComponent> derived = new LinkedHashMap<>();
        for (RecordComponent component : type.getRecordComponents()) {
            final Member annotation = component.getAnnotation(Member.class);
            final String member = annotation == null ? component.getName()
                    : Assert.notBlank(annotation.value(), "JSON record verifier member name must not be blank");
            Assert.isTrue(
                    derived.putIfAbsent(member, component) == null,
                    "JSON record verifier contains a duplicate member: {}",
                    member);
        }
        this.members = Map.copyOf(derived);
    }

    /**
     * Creates one immutable component-derived verifier.
     *
     * @param type record class whose components use exact JSON member names
     * @param <T>  record type
     * @return immutable record verifier
     */
    public static <T extends Record> JsonRecordVerifier<T> of(final Class<T> type) {
        return new JsonRecordVerifier<>(type);
    }

    /**
     * Requires the supplied JSON object to match the names, presence rules, and value types declared by the associated
     * record.
     *
     * @param value JSON object to validate
     * @return unchanged validated object
     * @throws IllegalArgumentException if {@code value} is {@code null}
     * @throws ValidateException        if the object contains an unknown member, omits a required member, or supplies a
     *                                  value whose JSON shape conflicts with the declared component type
     */
    public JsonValue.ObjectValue validate(final JsonValue.ObjectValue value) {
        final JsonValue.ObjectValue object = Assert.notNull(value, "JSON record verifier value must not be null");
        for (String member : object.values().keySet()) {
            if (!members.containsKey(member)) {
                throw new ValidateException(
                        "JSON object contains a member not declared by " + type.getName() + ": " + member);
            }
        }
        members.forEach((member, component) -> {
            final JsonValue memberValue = object.values().get(member);
            if (memberValue == null) {
                if (!isOptional(component.getGenericType())) {
                    throw new ValidateException(
                            "JSON object omits required member declared by " + type.getName() + ": " + member);
                }
                return;
            }
            validateValue(type.getName() + "." + member, memberValue, component.getGenericType());
        });
        return object;
    }

    /**
     * Validates one JSON value against a Java type without performing provider-specific coercion.
     *
     * @param path  member path used in validation failures
     * @param value provider-neutral JSON value
     * @param type  declared record component or generic element type
     * @throws ValidateException if the JSON value does not have the shape required by the declared Java type
     */
    private static void validateValue(final String path, final JsonValue value, final Type type) {
        if (isOptional(type)) {
            if (value instanceof JsonValue.NullValue) {
                return;
            }
            validateValue(path, value, requiredTypeArgument(path, type, 0));
            return;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            validateParameterized(path, value, parameterizedType);
            return;
        }
        if (type instanceof GenericArrayType arrayType) {
            validateArray(path, value, arrayType.getGenericComponentType());
            return;
        }
        if (type instanceof WildcardType wildcardType) {
            final Type[] upperBounds = wildcardType.getUpperBounds();
            Assert.isTrue(
                    upperBounds.length == 1,
                    "JSON record verifier wildcard must declare one upper bound: {}",
                    path);
            validateValue(path, value, upperBounds[0]);
            return;
        }
        if (!(type instanceof Class<?> declaredType)) {
            throw new ValidateException(
                    "JSON record verifier contains an unsupported generic type at " + path + ": " + type);
        }
        validateClass(path, value, declaredType);
    }

    /**
     * Validates a JSON value against a parameterized collection, map, or declared raw type.
     *
     * @param path              member path used in validation failures
     * @param value             provider-neutral JSON value
     * @param parameterizedType declared parameterized type
     * @throws ValidateException if the parameterized declaration or JSON value is unsupported
     */
    private static void validateParameterized(
            final String path,
            final JsonValue value,
            final ParameterizedType parameterizedType) {
        final Type rawType = parameterizedType.getRawType();
        if (!(rawType instanceof Class<?> rawClass)) {
            throw new ValidateException(
                    "JSON record verifier contains an unsupported raw type at " + path + ": " + rawType);
        }
        if (Collection.class.isAssignableFrom(rawClass)) {
            validateArray(path, value, requiredTypeArgument(path, parameterizedType, 0));
            return;
        }
        if (Map.class.isAssignableFrom(rawClass)) {
            final Type keyType = requiredTypeArgument(path, parameterizedType, 0);
            if (keyType != String.class) {
                throw new ValidateException("JSON object map key must be java.lang.String at " + path);
            }
            require(path, value, JsonValue.ObjectValue.class);
            final Type elementType = requiredTypeArgument(path, parameterizedType, 1);
            ((JsonValue.ObjectValue) value).values()
                    .forEach((member, element) -> validateValue(path + "." + member, element, elementType));
            return;
        }
        validateClass(path, value, rawClass);
    }

    /**
     * Validates every member of a JSON array against one declared element type.
     *
     * @param path        member path used in validation failures
     * @param value       provider-neutral JSON value
     * @param elementType declared collection or array element type
     * @throws ValidateException if the value is not an array or an element has an incompatible JSON shape
     */
    private static void validateArray(final String path, final JsonValue value, final Type elementType) {
        require(path, value, JsonValue.ArrayValue.class);
        final var elements = ((JsonValue.ArrayValue) value).values();
        for (int index = 0; index < elements.size(); index++) {
            validateValue(path + "[" + index + "]", elements.get(index), elementType);
        }
    }

    /**
     * Validates a JSON value against one non-parameterized Java class.
     *
     * @param path         member path used in validation failures
     * @param value        provider-neutral JSON value
     * @param declaredType declared Java class
     * @throws ValidateException if the declared class is unsupported or the JSON value has an incompatible shape
     */
    private static void validateClass(final String path, final JsonValue value, final Class<?> declaredType) {
        if (value instanceof JsonValue.NullValue) {
            if (declaredType.isPrimitive()) {
                throw new ValidateException("JSON null cannot bind to primitive member at " + path);
            }
            return;
        }
        if (JsonValue.class.isAssignableFrom(declaredType)) {
            require(path, value, declaredType);
        } else if (declaredType.isRecord()) {
            require(path, value, JsonValue.ObjectValue.class);
            JsonRecordVerifier.of((Class<? extends Record>) declaredType).validate((JsonValue.ObjectValue) value);
        } else if (declaredType.isArray()) {
            validateArray(path, value, declaredType.getComponentType());
        } else if (Collection.class.isAssignableFrom(declaredType)) {
            throw new ValidateException("JSON record verifier collection must declare its element type at " + path);
        } else if (Map.class.isAssignableFrom(declaredType)) {
            throw new ValidateException(
                    "JSON record verifier map must declare String keys and its value type at " + path);
        } else if (declaredType == boolean.class || declaredType == Boolean.class) {
            require(path, value, JsonValue.BooleanValue.class);
        } else if (isNumber(declaredType)) {
            require(path, value, JsonValue.NumberValue.class);
        } else if (declaredType == char.class || declaredType == Character.class) {
            require(path, value, JsonValue.StringValue.class);
            Assert.isTrue(
                    ((JsonValue.StringValue) value).value().length() == 1,
                    "JSON character member must contain exactly one character: {}",
                    path);
        } else if (declaredType.isEnum()) {
            require(path, value, JsonValue.StringValue.class);
            final String enumName = ((JsonValue.StringValue) value).value();
            boolean declared = false;
            for (Object constant : declaredType.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(enumName)) {
                    declared = true;
                    break;
                }
            }
            Assert.isTrue(declared, "JSON enum member contains an undeclared constant at {}: {}", path, enumName);
        } else if (declaredType == String.class || CharSequence.class.isAssignableFrom(declaredType)) {
            require(path, value, JsonValue.StringValue.class);
        } else if (declaredType == Object.class) {
            throw new ValidateException("JSON record verifier member must not use java.lang.Object at " + path);
        } else {
            require(path, value, JsonValue.StringValue.class);
        }
    }

    /**
     * Determines whether a declared Java type is either the Bus or JDK Optional container.
     *
     * @param type declared Java type
     * @return {@code true} when the type is a supported Optional container
     */
    private static boolean isOptional(final Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return isOptional(parameterizedType.getRawType());
        }
        return type == Optional.class || type == java.util.Optional.class;
    }

    /**
     * Returns one mandatory generic type argument from a parameterized declaration.
     *
     * @param path  member path used in validation failures
     * @param type  parameterized declaration
     * @param index zero-based argument index
     * @return declared generic type argument
     * @throws ValidateException if the declaration is raw or does not contain the requested argument
     */
    private static Type requiredTypeArgument(final String path, final Type type, final int index) {
        if (!(type instanceof ParameterizedType parameterizedType)
                || parameterizedType.getActualTypeArguments().length <= index) {
            throw new ValidateException(
                    "JSON record verifier type must declare generic argument " + index + " at " + path);
        }
        return parameterizedType.getActualTypeArguments()[index];
    }

    /**
     * Determines whether a Java class consumes an RFC 8259 number.
     *
     * @param type declared Java class
     * @return {@code true} for primitive and boxed numeric classes
     */
    private static boolean isNumber(final Class<?> type) {
        return type == byte.class || type == short.class || type == int.class || type == long.class
                || type == float.class || type == double.class || Number.class.isAssignableFrom(type)
                || type == BigInteger.class || type == BigDecimal.class;
    }

    /**
     * Requires one provider-neutral JSON value implementation at a member path.
     *
     * @param path         member path used in validation failures
     * @param value        provider-neutral JSON value
     * @param expectedType required JSON value implementation
     * @throws ValidateException if the value is not an instance of the required implementation
     */
    private static void require(final String path, final JsonValue value, final Class<?> expectedType) {
        if (!expectedType.isInstance(value)) {
            throw new ValidateException("JSON member " + path + " requires " + expectedType.getSimpleName()
                    + " but received " + value.getClass().getSimpleName());
        }
    }

}
