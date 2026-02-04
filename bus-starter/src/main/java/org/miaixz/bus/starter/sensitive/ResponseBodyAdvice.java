/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.sensitive;

import jakarta.annotation.Resource;
import org.miaixz.bus.base.advice.BaseAdvice;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.entity.Result;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.sensitive.Builder;
import org.miaixz.bus.sensitive.magic.annotation.Privacy;
import org.miaixz.bus.sensitive.magic.annotation.Sensitive;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice} that intercepts responses from
 * methods or classes annotated with {@link Sensitive} to perform data encryption and desensitization.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResponseBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {

    @Resource
    private SensitiveProperties properties;

    /**
     * Determines if this advice should be applied to the given method return type.
     *
     * @param returnType    The return type of the method.
     * @param converterType The selected converter type.
     * @return {@code true} if the advice should be applied, {@code false} otherwise.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Check for @Sensitive annotation on the class
        if (returnType.getDeclaringClass().isAnnotationPresent(Sensitive.class)) {
            return true;
        }
        // Check for @Sensitive annotation on the method
        return returnType.getMethod().isAnnotationPresent(Sensitive.class);
    }

    /**
     * Invoked before the response body is written by the selected {@link HttpMessageConverter}.
     * <p>
     * This method orchestrates the desensitization and encryption of the response body based on the {@link Sensitive}
     * annotation on the controller method.
     * </p>
     *
     * @param body                  The body to be written.
     * @param returnType            The return type of the controller method.
     * @param selectedContentType   The content type selected by the converter.
     * @param selectedConverterType The converter type selected to write the body.
     * @param request               The current request.
     * @param response              The current response.
     * @return The modified body to be written, or the original body.
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (ObjectKit.isNotEmpty(this.properties) && !this.properties.isDebug() && body instanceof Message) {
            try {
                final Sensitive sensitive = returnType.getMethodAnnotation(Sensitive.class);
                if (ObjectKit.isEmpty(sensitive)) {
                    return body;
                }

                Object data = ((Message) body).getData();
                if (data instanceof Result) {
                    List<Object> processedRows = new ArrayList<>();
                    for (Object row : ((Result) data).getRows()) {
                        processObject(sensitive, row);
                        processedRows.add(row);
                    }
                    ((Result) data).setRows(processedRows);
                } else if (data instanceof List) {
                    List<Object> processedList = new ArrayList<>();
                    for (Object item : (List<?>) data) {
                        processObject(sensitive, item);
                        processedList.add(item);
                    }
                    ((Message) body).setData(processedList);
                } else {
                    processObject(sensitive, data);
                    ((Message) body).setData(data);
                }
            } catch (Exception e) {
                Logger.error("Internal processing failure during response body modification", e);
            }
        }
        return body;
    }

    /**
     * Processes a single object for desensitization and encryption.
     *
     * @param sensitive The {@link Sensitive} annotation instance.
     * @param object    The object to process.
     */
    private void processObject(Sensitive sensitive, Object object) {
        if (ObjectKit.isEmpty(object)) {
            return;
        }
        // Perform data desensitization
        if ((Builder.ALL.equals(sensitive.value()) || Builder.SENS.equals(sensitive.value()))
                && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
            Logger.debug("Response data desensitization enabled...");
            Builder.on(object, sensitive);
        }
        // Perform data encryption
        if ((Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value()))
                && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
            Map<String, Privacy> privacyMap = getPrivacyMap(object.getClass());
            for (Map.Entry<String, Privacy> entry : privacyMap.entrySet()) {
                Privacy privacy = entry.getValue();
                if (ObjectKit.isNotEmpty(privacy) && StringKit.isNotEmpty(privacy.value())) {
                    if (Builder.ALL.equals(privacy.value()) || Builder.OUT.equals(privacy.value())) {
                        String property = entry.getKey();
                        Object value = getValue(object, property);
                        if (value instanceof String && StringKit.isNotEmpty((String) value)) {
                            if (ObjectKit.isEmpty(this.properties.getEncrypt())) {
                                throw new InternalException(
                                        "Encryption properties are not configured. Please check 'bus.sensitive.encrypt'.");
                            }
                            Logger.debug("Response data encryption enabled for property: {}", property);
                            String encryptedValue = org.miaixz.bus.crypto.Builder.encrypt(
                                    this.properties.getEncrypt().getType(),
                                    this.properties.getEncrypt().getKey(),
                                    (String) value,
                                    Charset.UTF_8);
                            setValue(object, property, encryptedValue);
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves a map of fields annotated with {@link Privacy} for a given class.
     *
     * @param clazz The class to inspect.
     * @return A map where the key is the field name and the value is the {@link Privacy} annotation.
     */
    private Map<String, Privacy> getPrivacyMap(Class<?> clazz) {
        Map<String, Privacy> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Privacy privacy = field.getAnnotation(Privacy.class);
            if (null != privacy) {
                map.put(field.getName(), privacy);
            }
        }
        return map;
    }

    /**
     * Sets a value on a bean property using its setter method.
     *
     * @param entity The bean instance.
     * @param field  The name of the property.
     * @param value  The value to set.
     * @param <T>    The type of the bean.
     */
    private static <T> void setValue(T entity, String field, Object value) {
        if (FieldKit.hasField(entity.getClass(), field)) {
            MethodKit.invokeSetter(entity, field, value);
        }
    }

    /**
     * Gets a value from a bean property using its getter method.
     *
     * @param entity The bean instance.
     * @param field  The name of the property.
     * @param <T>    The type of the bean.
     * @return The value of the property, or {@code null}.
     */
    private static <T> Object getValue(T entity, String field) {
        if (FieldKit.hasField(entity.getClass(), field)) {
            return MethodKit.invokeGetter(entity, field);
        }
        return null;
    }

}
