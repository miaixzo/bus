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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gson JSON 转换器配置器，与 Spring MVC 集成。 支持 autoType 配置，限制反序列化到指定包前缀的类。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@ConditionalOnClass({com.google.gson.Gson.class})
public class GsonConverterConfigurer implements JsonConverterConfigurer {

    private String autoType;

    @Override
    public String name() {
        return "Gson";
    }

    @Override
    public int order() {
        return 2; // 最低优先级
    }

    @Override
    public void configure(List<HttpMessageConverter<?>> converters) {
        Logger.debug("Configuring GsonHttpMessageConverter with autoType: {}", autoType);

        // 配置 Gson，添加 autoType 限制
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (autoType != null && !autoType.isEmpty()) {
            gsonBuilder.registerTypeAdapterFactory(new AutoTypeAdapterFactory(autoType));
            Logger.debug("Gson autoType enabled for package prefix: {}", autoType);
        }

        Gson gson = gsonBuilder.create();
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        converter
                .setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, new MediaType("application", "json+gson")));
        converters.add(order(), converter);
        Logger.debug("Gson converter configured with media types: {}", converter.getSupportedMediaTypes());
    }

    @Override
    public void autoType(String autoType) {
        this.autoType = autoType;
    }

    /**
     * 自定义 TypeAdapterFactory，限制反序列化类型到 autoType 包前缀。
     */
    private static class AutoTypeAdapterFactory implements com.google.gson.TypeAdapterFactory {
        private final String autoTypePrefix;

        public AutoTypeAdapterFactory(String autoTypePrefix) {
            this.autoTypePrefix = autoTypePrefix;
        }

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<?> rawType = type.getRawType();
            if (autoTypePrefix != null && !rawType.getName().startsWith(autoTypePrefix)) {
                throw new JsonParseException("Type not allowed: " + rawType.getName()
                        + ", must start with package prefix: " + autoTypePrefix);
            }
            return null; // Delegate to default adapter
        }
    }

}