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

        for (PayloadAdapter adapter : candidateAdapters()) {
            if (adapter != null) {
                return adapter;
            }
        }

        throw new IllegalStateException(
                "No supported JSON framework found for Temporal payload conversion. Expected fastjson2, Jackson, or Gson.");
    }

    /**
     * Attempts to create a fastjson2 adapter.
     *
     * @return the fastjson2 adapter, or {@code null} if fastjson2 is not available
     */
    protected static PayloadAdapter tryCreateFastjsonAdapter() {
        try {
            Class<?> jsonClass = Class.forName("com.alibaba.fastjson2.JSON");
            Method toJsonBytes = jsonClass.getMethod("toJSONBytes", Object.class);
            Method parseObject = jsonClass.getMethod("parseObject", byte[].class, Type.class);
            return new PayloadAdapter() {

                @Override
                public String name() {
                    return "fastjson2";
                }

                @Override
                public byte[] toBytes(Object value) throws Exception {
                    return (byte[]) toJsonBytes.invoke(null, value);
                }

                @Override
                public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception {
                    return (T) parseObject.invoke(null, bytes, valueType == null ? valueClass : valueType);
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
            Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object objectMapper = objectMapperClass.getConstructor().newInstance();
            invokeNoArgIfPresent(objectMapperClass, objectMapper, "findAndRegisterModules");
            Method writeValueAsBytes = objectMapperClass.getMethod("writeValueAsBytes", Object.class);
            Method constructType = objectMapperClass.getMethod("constructType", Type.class);
            Method readValue = objectMapperClass
                    .getMethod("readValue", byte[].class, Class.forName("com.fasterxml.jackson.databind.JavaType"));
            return new PayloadAdapter() {

                @Override
                public String name() {
                    return "jackson";
                }

                @Override
                public byte[] toBytes(Object value) throws Exception {
                    return (byte[]) writeValueAsBytes.invoke(objectMapper, value);
                }

                @Override
                public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception {
                    Object javaType = constructType.invoke(objectMapper, valueType == null ? valueClass : valueType);
                    return (T) readValue.invoke(objectMapper, bytes, javaType);
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
            Class<?> gsonClass = Class.forName("com.google.gson.Gson");
            Object gson = gsonClass.getConstructor().newInstance();
            Method toJson = gsonClass.getMethod("toJson", Object.class);
            Method fromJson = gsonClass.getMethod("fromJson", String.class, Type.class);
            return new PayloadAdapter() {

                @Override
                public String name() {
                    return "gson";
                }

                @Override
                public byte[] toBytes(Object value) throws Exception {
                    return ((String) toJson.invoke(gson, value)).getBytes(Charset.UTF_8);
                }

                @Override
                public <T> T fromBytes(byte[] bytes, Class<T> valueClass, Type valueType) throws Exception {
                    return (T) fromJson
                            .invoke(gson, new String(bytes, Charset.UTF_8), valueType == null ? valueClass : valueType);
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
            Method method = type.getMethod(methodName);
            method.invoke(target);
        } catch (Throwable ignored) {
        }
    }

}
