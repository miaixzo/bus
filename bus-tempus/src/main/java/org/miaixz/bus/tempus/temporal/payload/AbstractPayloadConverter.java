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
package org.miaixz.bus.tempus.temporal.payload;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.MethodKit;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Base implementation for Temporal payload converters with automatic JSON framework detection.
 * <p>
 * This abstract class provides utility methods to detect and create adapters for popular JSON frameworks (fastjson2,
 * Jackson, Gson) in priority order. Subclasses can override adapter resolution hooks to force a specific adapter.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractPayloadConverter implements PayloadConverter {

    /**
     * Returns a preferred adapter to use.
     * <p>
     * Default is {@code null}, meaning auto-detection is used.
     *
     * @return the preferred adapter, or {@code null} to enable auto-detection
     */
    protected PayloadAdapter preferredAdapter() {
        return null;
    }

    /**
     * Returns the candidate adapters to consider when auto-detecting.
     *
     * @return the candidate adapters (may contain {@code null} entries for unavailable frameworks)
     */
    protected List<PayloadAdapter> candidateAdapters() {
        return List.of(tryCreateFastjsonAdapter(), tryCreateJacksonAdapter(), tryCreateGsonAdapter());
    }

    /**
     * Resolves the adapter to use.
     *
     * @return the resolved adapter
     * @throws IllegalStateException if no supported JSON framework is available
     */
    protected PayloadAdapter resolveAdapter() {
        PayloadAdapter preferred = preferredAdapter();
        if (preferred != null) {
            return preferred;
        }
        return DefaultPayloadAdapterHolder.ADAPTER;
    }

    /**
     * Attempts to create a fastjson2 adapter.
     *
     * @return the fastjson2 adapter, or {@code null} if fastjson2 is not available
     */
    protected static PayloadAdapter tryCreateFastjsonAdapter() {
        try {
            Class<?> jsonClass = ClassKit.loadClass("com.alibaba.fastjson2.JSON");
            Method toJsonBytes = MethodKit.getPublicMethod(jsonClass, false, "toJSONBytes", Object.class);
            Method parseObject = MethodKit.getPublicMethod(jsonClass, false, "parseObject", byte[].class, Type.class);
            return new PayloadAdapter() {

                /**
                 * Returns the adapter identifier used in diagnostics.
                 *
                 * @return the adapter name
                 */
                @Override
                public String name() {
                    return "fastjson2";
                }

                /**
                 * Serializes the given value with fastjson2.
                 *
                 * @param value the value to serialize
                 * @return serialized bytes
                 * @throws Exception if serialization fails
                 */
                @Override
                public byte[] toBytes(Object value) throws Exception {
                    return MethodKit.invokeStatic(toJsonBytes, value);
                }

                /**
                 * Deserializes the given bytes with fastjson2.
                 *
                 * @param bytes      the serialized bytes
                 * @param valueClass the raw value type
                 * @param valueType  the generic value type when available
                 * @param <T>        the target value type
                 * @return the deserialized value
                 * @throws Exception if deserialization fails
                 */
                @Override
                public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception {
                    return MethodKit.invokeStatic(parseObject, bytes, valueType == null ? valueClass : valueType);
                }
            };
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Attempts to create a Jackson adapter.
     *
     * @return the Jackson adapter, or {@code null} if Jackson is not available
     */
    protected static PayloadAdapter tryCreateJacksonAdapter() {
        try {
            Class<?> objectMapperClass = ClassKit.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            Object objectMapper = objectMapperClass.getConstructor().newInstance();
            invokeNoArgIfPresent(objectMapperClass, objectMapper, "findAndRegisterModules");
            Method writeValueAsBytes = MethodKit
                    .getPublicMethod(objectMapperClass, false, "writeValueAsBytes", Object.class);
            Method constructType = MethodKit.getPublicMethod(objectMapperClass, false, "constructType", Type.class);
            Method readValue = MethodKit.getPublicMethod(
                    objectMapperClass,
                    false,
                    "readValue",
                    byte[].class,
                    ClassKit.loadClass("com.fasterxml.jackson.databind.JavaType"));
            return new PayloadAdapter() {

                /**
                 * Returns the adapter identifier used in diagnostics.
                 *
                 * @return the adapter name
                 */
                @Override
                public String name() {
                    return "jackson";
                }

                /**
                 * Serializes the given value with Jackson.
                 *
                 * @param value the value to serialize
                 * @return serialized bytes
                 * @throws Exception if serialization fails
                 */
                @Override
                public byte[] toBytes(Object value) throws Exception {
                    return MethodKit.invoke(objectMapper, writeValueAsBytes, value);
                }

                /**
                 * Deserializes the given bytes with Jackson.
                 *
                 * @param bytes      the serialized bytes
                 * @param valueClass the raw value type
                 * @param valueType  the generic value type when available
                 * @param <T>        the target value type
                 * @return the deserialized value
                 * @throws Exception if deserialization fails
                 */
                @Override
                public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception {
                    Object javaType = MethodKit
                            .invoke(objectMapper, constructType, valueType == null ? valueClass : valueType);
                    return MethodKit.invoke(objectMapper, readValue, bytes, javaType);
                }
            };
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Attempts to create a Gson adapter.
     *
     * @return the Gson adapter, or {@code null} if Gson is not available
     */
    protected static PayloadAdapter tryCreateGsonAdapter() {
        try {
            Class<?> gsonClass = ClassKit.loadClass("com.google.gson.Gson");
            Object gson = gsonClass.getConstructor().newInstance();
            Method toJson = MethodKit.getPublicMethod(gsonClass, false, "toJson", Object.class);
            Method fromJson = MethodKit.getPublicMethod(gsonClass, false, "fromJson", String.class, Type.class);
            return new PayloadAdapter() {

                /**
                 * Returns the adapter identifier used in diagnostics.
                 *
                 * @return the adapter name
                 */
                @Override
                public String name() {
                    return "gson";
                }

                /**
                 * Serializes the given value with Gson.
                 *
                 * @param value the value to serialize
                 * @return serialized bytes
                 * @throws Exception if serialization fails
                 */
                @Override
                public byte[] toBytes(Object value) throws Exception {
                    return MethodKit.<String>invoke(gson, toJson, value).getBytes(Charset.UTF_8);
                }

                /**
                 * Deserializes the given bytes with Gson.
                 *
                 * @param bytes      the serialized bytes
                 * @param valueClass the raw value type
                 * @param valueType  the generic value type when available
                 * @param <T>        the target value type
                 * @return the deserialized value
                 * @throws Exception if deserialization fails
                 */
                @Override
                public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception {
                    return MethodKit.invoke(
                            gson,
                            fromJson,
                            new String(bytes, Charset.UTF_8),
                            valueType == null ? valueClass : valueType);
                }
            };
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Invokes a no-argument method on the target object if it exists; ignores if the method is not found.
     *
     * @param type       the target class type
     * @param target     the target object
     * @param methodName the method name to invoke
     */
    protected static void invokeNoArgIfPresent(Class<?> type, Object target, String methodName) {
        try {
            Method method = MethodKit.getPublicMethod(type, false, methodName);
            MethodKit.invoke(target, method);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Resolves the default adapter using the framework priority order: fastjson2, Jackson, then Gson.
     *
     * @return the default JSON adapter
     * @throws IllegalStateException if no supported JSON framework is available at runtime
     */
    private static PayloadAdapter resolveDefaultAdapter() {
        for (PayloadAdapter adapter : List
                .of(tryCreateFastjsonAdapter(), tryCreateJacksonAdapter(), tryCreateGsonAdapter())) {
            if (adapter != null) {
                return adapter;
            }
        }
        throw new IllegalStateException(
                "No supported JSON framework found for Temporal payload conversion. Expected fastjson2, Jackson, or Gson.");
    }

    /**
     * Holds the lazily initialized default adapter to avoid eager framework detection during class loading.
     */
    private static final class DefaultPayloadAdapterHolder {

        /**
         * Lazily resolved default JSON adapter.
         */
        private static final PayloadAdapter ADAPTER = resolveDefaultAdapter();

        /**
         * Creates the holder type.
         */
        private DefaultPayloadAdapterHolder() {
        }
    }

}
