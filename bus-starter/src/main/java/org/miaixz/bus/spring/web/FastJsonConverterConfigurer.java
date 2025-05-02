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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Fastjson2 JSON 转换器配置器。 配置 Fastjson2 的 HttpMessageConverter，支持 autoType 自动类型识别。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnClass({JSON.class})
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
        private static final JSONWriter.Feature[] WRITER_FEATURES = {JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteNulls};
        private static final JSONReader.Feature[] READER_FEATURES = {JSONReader.Feature.FieldBased};
        private static final Filter[] FILTERS = {(ValueFilter) (object, name,
                                                                value) -> value == null || Normal.EMPTY.equals(value) || Symbol.SPACE.equals(value) ? null : value};

        private final String[] autoTypes;

        public FastJson2HttpMessageConverter(String autoType) {
            super(StandardCharsets.UTF_8, DEFAULT_MEDIA_TYPES.toArray(new MediaType[0]));
            if (StringKit.isEmpty(autoType)) {
                this.autoTypes = null;
                Logger.info("Fastjson2 autoType is not configured, @type deserialization is disabled");
            } else {
                this.autoTypes = StringKit.splitToArray(autoType, Symbol.COMMA);
                for (int i = 0; i < autoTypes.length; i++) {
                    autoTypes[i] = autoTypes[i].trim();
                    if (StringKit.isEmpty(autoTypes[i])) {
                        throw new IllegalArgumentException("autoType contains empty or invalid types");
                    }
                }
                Logger.info("Fastjson2 autoType is enabled, whitelist types: {}", String.join(", ", autoTypes));
            }
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return true; // 支持所有类型
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
                throws HttpMessageNotReadableException {
            InputStream inputStream = null;
            try {
                inputStream = inputMessage.getBody();
                byte[] bytes = IoKit.readBytes(inputStream);
                String jsonString = new String(bytes, StandardCharsets.UTF_8);
                Logger.debug("Deserializing JSON for class {}", clazz.getName());

                // 验证 JSON 是否安全
                if (autoTypes != null && !isSafeJson(jsonString)) {
                    Logger.error("JSON contains untrusted @type: {}", jsonString);
                    throw new HttpMessageNotReadableException("JSON contains untrusted @type", inputMessage);
                }

                // 根据是否配置 autoType 进行反序列化
                Object result;
                if (autoTypes == null) {
                    result = JSON.parseObject(jsonString, clazz, READER_FEATURES);
                } else {
                    result = JSON.parseObject(jsonString, clazz, JSONReader.autoTypeFilter(autoTypes), READER_FEATURES);
                }
                Logger.debug("Deserialization successful for class {}", clazz.getName());
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
            } finally {
                IoKit.closeQuietly(inputStream);
            }
        }

        @Override
        protected void writeInternal(Object object, HttpOutputMessage outputMessage)
                throws HttpMessageNotWritableException {
            try {
                Logger.debug("Serializing object: {}", object != null ? object.getClass().getName() : "null");
                String jsonString = JSON.toJSONString(object, FILTERS, WRITER_FEATURES);
                byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                outputMessage.getBody().write(bytes);
                Logger.debug("Serialization successful, JSON length: {}", jsonString.length());
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
         * 验证 JSON 输入是否只包含白名单中的 @type。
         *
         * @param jsonString JSON 字符串
         * @return 如果 JSON 安全返回 true，否则返回 false
         */
        private boolean isSafeJson(String jsonString) {
            if (jsonString.contains("@type")) {
                for (String autoType : autoTypes) {
                    if (jsonString.contains(autoType)) {
                        Logger.debug("Found trusted @type: {}", autoType);
                        return true;
                    }
                }
                Logger.warn("Untrusted @type detected in JSON: {}", jsonString);
                return false;
            }
            return true;
        }
    }

}