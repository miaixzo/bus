package org.miaixz.bus.spring.web;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Fastjson2 框架的配置器，与 Spring MVC 集成。 使用自定义 FastJson2HttpMessageConverter，支持 UTF-8 编码、自定义日期格式和空值过滤。
 */
public record FastJsonConverterConfigurer() implements JsonConverterConfigurer {

    private static final String FASTJSON2_CLASS = "com.alibaba.fastjson2.JSON";
    private static final List<MediaType> DEFAULT_MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON,
            MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));

    @Override
    public String converterName() {
        return "Fastjson2";
    }

    @Override
    public boolean isAvailable() {
        boolean available = ClassKit.isPresent(FASTJSON2_CLASS, FastJsonConverterConfigurer.class.getClassLoader());
        if (!available) {
            Logger.warn(
                    "Fastjson2 dependency {} is not present in the classpath. Ensure com.alibaba.fastjson2:fastjson2:2.0.57 is included.",
                    FASTJSON2_CLASS);
        } else {
            Logger.debug("Fastjson2 dependency {} is present.", FASTJSON2_CLASS);
        }
        return available;
    }

    @Override
    public int order() {
        return 1; // 第二优先级
    }

    @Override
    public void configure(List<HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring FastJson2HttpMessageConverter for Fastjson2");
        converters.add(order(), new FastJson2HttpMessageConverter());
        Logger.debug("FastJson2HttpMessageConverter configured with media types: {}", DEFAULT_MEDIA_TYPES);
    }

    /**
     * 自定义 Fastjson2 的 HttpMessageConverter，使用 JSONWriter.Feature 配置序列化，JSONReader.Feature 配置反序列化。
     */
    static class FastJson2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {
        private static final JSONWriter.Feature[] WRITER_FEATURES = {
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNulls
        };
        private static final JSONReader.Feature[] READER_FEATURES = {JSONReader.Feature.FieldBased};
        private static final Filter[] FILTERS = {
                (ValueFilter) (object, name, value) -> value == null || Normal.EMPTY.equals(value) || Symbol.SPACE.equals(value) ? null : value
        };

        public FastJson2HttpMessageConverter() {
            super(StandardCharsets.UTF_8, DEFAULT_MEDIA_TYPES.toArray(new MediaType[0]));
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return true; // 支持所有类型
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
                throws HttpMessageNotReadableException {
            try (InputStream inputStream = inputMessage.getBody()) {
                byte[] bytes = inputStream.readAllBytes();
                Logger.debug("Deserializing JSON with Fastjson2 for class: {}", clazz.getName());
                Object result = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz, READER_FEATURES);
                Logger.debug("Deserialization successful for class: {}", clazz.getName());
                return result;
            } catch (Exception e) {
                Logger.error("Failed to deserialize JSON with Fastjson2 for class {}: {}", clazz.getName(),
                        e.getMessage(), e);
                throw new HttpMessageNotReadableException(
                        "Failed to deserialize JSON with Fastjson2: " + e.getMessage(), e, inputMessage);
            }
        }

        @Override
        protected void writeInternal(Object object, HttpOutputMessage outputMessage)
                throws HttpMessageNotWritableException {
            try {
                Logger.debug("Serializing object with Fastjson2: {}",
                        object != null ? object.getClass().getName() : "null");
                String jsonString = JSON.toJSONString(object, FILTERS, WRITER_FEATURES);
                byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                outputMessage.getBody().write(bytes);
                Logger.debug("Serialization successful, JSON: {}", jsonString);
            } catch (Exception e) {
                Logger.error("Failed to serialize JSON with Fastjson2: {}", e.getMessage(), e);
                throw new HttpMessageNotWritableException("Failed to serialize JSON with Fastjson2: " + e.getMessage(),
                        e);
            }
        }
    }

}