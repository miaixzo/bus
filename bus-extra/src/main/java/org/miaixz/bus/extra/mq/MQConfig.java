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
package org.miaixz.bus.extra.mq;

import java.io.Serial;
import java.io.Serializable;
import java.util.Properties;

/**
 * MQ配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MQConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852266759152L;

    /**
     * 创建配置
     *
     * @param brokerUrl Broker地址
     * @return 配置
     */
    public static MQConfig of(final String brokerUrl) {
        return new MQConfig(brokerUrl);
    }

    private String brokerUrl;
    private Properties properties;
    /**
     * 自定义引擎，当多个jar包引入时，可以自定使用的默认引擎
     */
    private Class<? extends MQProvider> customEngine;

    /**
     * 构造
     *
     * @param brokerUrl Broker地址
     */
    public MQConfig(final String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    /**
     * 获取Broker地址
     *
     * @return Broker地址
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * 设置Broker地址
     *
     * @param brokerUrl Broker地址
     * @return this
     */
    public MQConfig setBrokerUrl(final String brokerUrl) {
        this.brokerUrl = brokerUrl;
        return this;
    }

    /**
     * 获取配置
     *
     * @return 配置
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 设置配置
     *
     * @param properties 配置
     * @return this
     */
    public MQConfig setProperties(final Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 添加配置项
     *
     * @param key   键
     * @param value 值
     * @return this
     */
    public Properties addProperty(final String key, final String value) {
        if (null == this.properties) {
            this.properties = new Properties();
        }
        this.properties.setProperty(key, value);
        return this.properties;
    }

    /**
     * 自定义引擎，当多个jar包引入时，可以自定使用的默认引擎
     *
     * @return 自定义引擎
     */
    public Class<? extends MQProvider> getCustomEngine() {
        return customEngine;
    }

    /**
     * 自定义引擎，当多个jar包引入时，可以自定使用的默认引擎
     *
     * @param customEngine 自定义引擎
     * @return this
     */
    public MQConfig setCustomEngine(final Class<? extends MQProvider> customEngine) {
        this.customEngine = customEngine;
        return this;
    }

}
