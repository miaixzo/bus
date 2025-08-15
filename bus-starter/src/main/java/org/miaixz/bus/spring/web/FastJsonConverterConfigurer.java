/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyFilter;

import jakarta.persistence.Transient;

/**
 * Fastjson2 JSON 转换器配置器。 配置 Fastjson2 的 HttpMessageConverter，支持 autoType 自动类型识别。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnClass({ JSON.class })
public class FastJsonConverterConfigurer implements JsonConverterConfigurer {

    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON,
            new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8));

    private String autoType;

    @Override
    public String name() {
        return "Fastjson2";
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void configure(List<HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring FastJson2HttpMessageConverter for Fastjson2");
        converters.add(order(), new FastJson2HttpMessageConverter(this.autoType));
        Logger.debug("FastJson2HttpMessageConverter configured with media types: {}", DEFAULT_MEDIA_TYPES);
    }

    @Override
    public void autoType(String autoType) {
        this.autoType = autoType;
    }

    /**
     * 自定义 Fastjson2 的 HttpMessageConverter，使用 JSONWriter.Feature 配置序列化， JSONReader.Feature 配置反序列化，并根据构造函数参数配置 autoType。
     */
    static class FastJson2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {

        private static final JSONWriter.Feature[] WRITER_FEATURES = { JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.IgnoreNonFieldGetter };

        private static final JSONReader.Feature[] READER_FEATURES = { JSONReader.Feature.FieldBased };

        private final String[] autoTypes;

        public FastJson2HttpMessageConverter(String autoType) {
            super(StandardCharsets.UTF_8, DEFAULT_MEDIA_TYPES.toArray(new MediaType[0]));
            this.autoTypes = StringKit.isEmpty(autoType) ? null
                    : Arrays.stream(autoType.split(Symbol.COMMA)).map(String::trim).filter(StringKit::isNotEmpty)
                            .toArray(String[]::new);
            if (this.autoTypes == null) {
                Logger.info("Fastjson2 autoType is not configured, @type deserialization is disabled");
            } else {
                Logger.info("Fastjson2 autoType is enabled, whitelist types: {}", String.join(", ", autoTypes));
            }
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return true;
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
                throws HttpMessageNotReadableException {
            try (var inputStream = inputMessage.getBody()) {
                String jsonString = new String(IoKit.readBytes(inputStream), StandardCharsets.UTF_8);
                Logger.debug("Deserializing JSON for class {}", clazz.getName());

                if (autoTypes != null && !isSafeJson(jsonString)) {
                    Logger.error("JSON contains untrusted @type: {}", jsonString);
                    throw new HttpMessageNotReadableException("JSON contains untrusted @type", inputMessage);
                }

                // 根据是否配置 autoType 进行反序列化
                Object result = autoTypes == null ? JSON.parseObject(jsonString, clazz, READER_FEATURES)
                        : JSON.parseObject(jsonString, clazz, JSONReader.autoTypeFilter(autoTypes), READER_FEATURES);

                return result;
            } catch (IOException e) {
                Logger.error("IO error occurred during JSON deserialization, class {}: {}", clazz.getName(),
                        e.getMessage(), e);
                throw new HttpMessageNotReadableException(
                        "IO error occurred during JSON deserialization: " + e.getMessage(), e, inputMessage);
            } catch (Exception e) {
                Logger.error("JSON deserialization failed, class {}: {}", clazz.getName(), e.getMessage(), e);
                throw new HttpMessageNotReadableException("JSON deserialization failed: " + e.getMessage(), e,
                        inputMessage);
            }
        }

        @Override
        protected void writeInternal(Object object, HttpOutputMessage outputMessage)
                throws HttpMessageNotWritableException {
            try {
                Logger.debug("==>     Object: {}", object != null ? object.getClass().getName() : "null");
                PropertyFilter filter = (source, name, value) -> {
                    if (value == null || Normal.EMPTY.equals(value) || Symbol.SPACE.equals(value)) {
                        return false;
                    }
                    try {
                        Field field = FieldKit.getField(source.getClass(), name);
                        if (field == null) {
                            return true;
                        }
                        if (Arrays.stream(field.getAnnotations())
                                .anyMatch(annotation -> annotation.annotationType().equals(Transient.class))) {
                            return false;
                        }
                        return !Modifier.isTransient(field.getModifiers());
                    } catch (Exception e) {
                        Logger.warn("Failed to check @Transient annotation for field {}", name, e.getMessage());
                        return true;
                    }
                };

                String jsonString = JSON.toJSONString(object, filter, WRITER_FEATURES);
                outputMessage.getBody().write(jsonString.getBytes(StandardCharsets.UTF_8));
                Logger.info("<==     Length: {}", jsonString.length());
            } catch (IOException e) {
                Logger.error("IO error occurred during JSON serialization: {}", e.getMessage(), e);
                throw new HttpMessageNotWritableException(
                        "IO error occurred during JSON serialization: " + e.getMessage(), e);
            } catch (Exception e) {
                Logger.error("JSON serialization failed: {}", e.getMessage(), e);
                throw new HttpMessageNotWritableException("JSON serialization failed: " + e.getMessage(), e);
            }
        }

        /**
         * 验证 JSON 输入是否只包含白名单中的 @type
         *
         * @param jsonString JSON 字符串
         * @return 如果 JSON 安全返回 true，否则返回 false
         */
        private boolean isSafeJson(String jsonString) {
            return !jsonString.contains("@type") || Arrays.stream(autoTypes).anyMatch(jsonString::contains);
        }
    }

}