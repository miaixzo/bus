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
package org.miaixz.bus.extra.json.provider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.InternalException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * A {@link org.miaixz.bus.extra.json.JsonProvider} implementation based on the Jackson library. This class provides
 * JSON serialization and deserialization functionalities using Jackson's {@link ObjectMapper}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JacksonProvider extends AbstractJsonProvider {

    /**
     * The underlying Jackson {@link ObjectMapper} used for JSON operations.
     */
    public static ObjectMapper objectMapper;

    /**
     * Constructs a new {@code JacksonProvider} instance. Initializes an {@link ObjectMapper} with module auto-detection
     * and disables writing dates as timestamps.
     */
    public JacksonProvider() {
        objectMapper = new ObjectMapper().findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String toJsonString(Object object, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return objectMapper.writer(sdf).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T toPojo(Map fromMap, Class<T> clazz) {
        return objectMapper.convertValue(fromMap, clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json) {
        try {
            return objectMapper.readValue(json, LinkedList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Class<T> clazz) {
        try {
            return (List<T>) objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> List<T> toList(String json, Type type) {
        TypeReference<List<T>> typeReference = new TypeReference<>() {

            /**
             * Description inherited from parent class or interface.
             */
            @Override
            public Type getType() {
                return type;
            }
        };
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <K, V> Map<K, V> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public <T> T getValue(String json, String field) {
        try {
            return (T) objectMapper.readTree(json).get(field);
        } catch (JsonProcessingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public boolean isJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
