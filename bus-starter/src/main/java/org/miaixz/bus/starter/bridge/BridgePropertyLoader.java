/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.bridge;

import jakarta.annotation.Resource;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.Builder;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.*;

/**
 * 客户端-配置中心
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BridgePropertyLoader implements PropertySourceLoader, Ordered {

    @Resource
    private BridgeProperties properties;
    private String profiles = null;

    @Override
    public String[] getFileExtensions() {
        return new String[]{"properties", "yml", "yaml"};
    }

    @Override
    public List<PropertySource<?>> load(String name, org.springframework.core.io.Resource resource) throws IOException {
        Properties property = new Properties();
        if (name.contains("properties")) {
            property.load(resource.getInputStream());
        } else {
            YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
            bean.setResources(resource);
            property = bean.getObject();
        }

        if (profiles == null) {
            profiles = property.getProperty("spring.profiles.active");
        } else {
            Logger.info("spring.profiles.active = " + profiles + ",ignore load remote config");
        }
        // 如果指定了profile，则不加载远程配置
        if (profiles == null) {
            this.mergeProperties(property);
            PropertySource<?> props = new OriginTrackedMapPropertySource(name, property);
            return Collections.singletonList(props);
        }

        return Collections.singletonList(new OriginTrackedMapPropertySource(name, property));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 11;
    }

    public void mergeProperties(Properties properties) {
        String url = String.format("%s?method=%s&v=%s&format=%s&appKey=%s&profile=%s",
                this.properties.getUrl(),
                this.properties.getMethod(),
                this.properties.getVersion(),
                this.properties.getFormat(),
                this.properties.getAppKey(),
                this.properties.getProfile());
        Logger.debug("fetch configs url:" + url);
        String response = Httpx.get(url);
        Map<String, Object> map = (Map<String, Object>) JsonKit.toMap(response).get("data");
        if (map == null) {
            throw new RuntimeException("fetch remote config error!");
        }

        // 合并属性
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue().toString());
        }

        // 替换本地变量占位符
        Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entrySet) {
            String value = entry.getValue().toString();
            if (value.contains(Symbol.DOLLAR + Symbol.C_BRACE_LEFT)) {
                value = Builder.replaceYamlValue(properties, value);
                properties.setProperty(entry.getKey().toString(), value);
            }
        }
    }

}
