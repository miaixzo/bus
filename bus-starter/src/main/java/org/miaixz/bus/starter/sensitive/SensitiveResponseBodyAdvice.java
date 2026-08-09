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
package org.miaixz.bus.starter.sensitive;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

/**
 * A {@link org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice} that intercepts responses from
 * methods or classes annotated with {@link Sensitive} to perform data encryption and desensitization.
 *
 * @author Kimi Liu
 */
public class SensitiveResponseBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {

    /**
     * Sensitive-data rules applied before response serialization.
     */
    private final SensitiveProperties properties;

    /**
     * Identity-based guard preventing repeated processing of the same response object.
     */
    private final Map<ServerHttpResponse, Boolean> processedResponses = Collections
            .synchronizedMap(new WeakHashMap<>());

    /**
     * Creates response advice with the validated rules owned by the current application context.
     *
     * @param properties sensitive processing rules
     */
    public SensitiveResponseBodyAdvice(SensitiveProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /**
     * Determines if this advice should be applied to the given method return type.
     *
     * @param returnType    The return type of the method.
     * @param converterType The selected converter type.
     * @return {@code true} if the advice should be applied, {@code false} otherwise.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> responseType = returnType.getParameterType();
        return returnType.hasMethodAnnotation(Sensitive.class) || returnType.hasMethodAnnotation(Privacy.class)
                || returnType.getDeclaringClass().isAnnotationPresent(Sensitive.class)
                || returnType.getDeclaringClass().isAnnotationPresent(Privacy.class)
                || responseType.isAnnotationPresent(Sensitive.class) || responseType.isAnnotationPresent(Privacy.class);
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
        if (this.properties.isDebug() || body == null || shouldSkip(body, selectedContentType, response)
                || this.processedResponses.containsKey(response)) {
            return body;
        }
        try {
            Sensitive sensitive = returnType.getMethodAnnotation(Sensitive.class);
            if (sensitive == null) {
                sensitive = returnType.getDeclaringClass().getAnnotation(Sensitive.class);
            }
            if (sensitive == null) {
                sensitive = body.getClass().getAnnotation(Sensitive.class);
            }
            boolean privacyEnabled = returnType.hasMethodAnnotation(Privacy.class)
                    || returnType.getDeclaringClass().isAnnotationPresent(Privacy.class)
                    || returnType.getParameterType().isAnnotationPresent(Privacy.class)
                    || body.getClass().isAnnotationPresent(Privacy.class);
            Object data = body instanceof Message<?> message ? message.getData() : body;
            Logger.debug(
                    true,
                    "Starter",
                    "Sensitive response processing started: controller={}, method={}, mode={}, stage={}, dataType={}, contentType={}",
                    returnType.getDeclaringClass().getName(),
                    returnType.getExecutable().getName(),
                    sensitive == null ? null : sensitive.value(),
                    sensitive == null ? null : sensitive.stage(),
                    data == null ? null : data.getClass().getName(),
                    selectedContentType);
            if (data instanceof Result<?> result) {
                List<?> rows = result.getRows();
                for (Object row : rows) {
                    processObject(sensitive, privacyEnabled, row);
                }
                Logger.debug(
                        false,
                        "Starter",
                        "Sensitive response processing completed: controller={}, method={}, resultRowCount={}",
                        returnType.getDeclaringClass().getName(),
                        returnType.getExecutable().getName(),
                        rows.size());
            } else if (data instanceof List<?> list) {
                for (Object item : list) {
                    processObject(sensitive, privacyEnabled, item);
                }
                Logger.debug(
                        false,
                        "Starter",
                        "Sensitive response processing completed: controller={}, method={}, listSize={}",
                        returnType.getDeclaringClass().getName(),
                        returnType.getExecutable().getName(),
                        list.size());
            } else {
                processObject(sensitive, privacyEnabled, data);
                Logger.debug(
                        false,
                        "Starter",
                        "Sensitive response processing completed: controller={}, method={}, dataType={}",
                        returnType.getDeclaringClass().getName(),
                        returnType.getExecutable().getName(),
                        data == null ? null : data.getClass().getName());
            }
            this.processedResponses.put(response, Boolean.TRUE);
        } catch (Exception e) {
            throw new IllegalStateException("Sensitive response processing failed", e);
        }
        return body;
    }

    /**
     * Determines whether the response has already been desensitized.
     *
     * @param body        response body being evaluated
     * @param contentType content type
     * @param response    current HTTP response
     * @return {@code true} when the response must not be processed again
     */
    private static boolean shouldSkip(Object body, MediaType contentType, ServerHttpResponse response) {
        if (body instanceof byte[] || body instanceof InputStream || body instanceof Resource
                || body instanceof ResponseBodyEmitter || body instanceof StreamingResponseBody) {
            return true;
        }
        if (contentType != null && (MediaType.TEXT_EVENT_STREAM.isCompatibleWith(contentType)
                || MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(contentType))) {
            return true;
        }
        String disposition = response.getHeaders().getFirst("Content-Disposition");
        return StringKit.isNotEmpty(disposition);
    }

    /**
     * Processes a single object for desensitization and encryption.
     *
     * @param sensitive      The {@link Sensitive} annotation instance.
     * @param object         The object to process.
     * @param privacyEnabled privacy enabled
     */
    private void processObject(Sensitive sensitive, boolean privacyEnabled, Object object) {
        if (ObjectKit.isEmpty(object)) {
            return;
        }
        // Perform data desensitization
        if (sensitive != null && (Builder.ALL.equals(sensitive.value()) || Builder.SENS.equals(sensitive.value()))
                && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage()))) {
            Logger.debug(false, "Starter", "Sensitive response data desensitization enabled...");
            Builder.on(object, sensitive);
        }
        // Perform data encryption
        if (privacyEnabled || (sensitive != null
                && (Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value()))
                && (Builder.ALL.equals(sensitive.stage()) || Builder.OUT.equals(sensitive.stage())))) {
            Map<String, Privacy> privacyMap = getPrivacyMap(object.getClass());
            for (Map.Entry<String, Privacy> entry : privacyMap.entrySet()) {
                Privacy privacy = entry.getValue();
                if (ObjectKit.isNotEmpty(privacy) && StringKit.isNotEmpty(privacy.value())) {
                    if (Builder.ALL.equals(privacy.value()) || Builder.OUT.equals(privacy.value())) {
                        String property = entry.getKey();
                        Object value = getValue(object, property);
                        if (value instanceof String && StringKit.isNotEmpty((String) value)) {
                            SensitiveProperties.Encrypt encrypt = this.properties.getEncrypt();
                            if (encrypt == null || StringKit.isBlank(encrypt.getType())
                                    || StringKit.isBlank(encrypt.getKey())) {
                                throw new InternalException(
                                        "Encryption properties are not configured. Please check 'bus.sensitive.encrypt'.");
                            }
                            Logger.debug(
                                    false,
                                    "Starter",
                                    "Sensitive response data encryption enabled for property: {}",
                                    property);
                            String encryptedValue = org.miaixz.bus.crypto.Builder
                                    .encrypt(encrypt.getType(), encrypt.getKey(), (String) value, Charset.UTF_8);
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
