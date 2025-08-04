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
package org.miaixz.bus.extra.mq.provider.kafka;

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.MQProvider;
import org.miaixz.bus.extra.mq.Producer;

/**
 * Kafka消息队列引擎实现类，用于提供Kafka的消息队列服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KafkaProvider implements MQProvider {

    /**
     * Kafka客户端配置属性
     */
    private Properties properties;

    /**
     * 默认构造方法，用于SPI方式加载时检查库是否引入
     */
    public KafkaProvider() {
        // SPI方式加载时检查库是否引入
        Assert.notNull(org.apache.kafka.clients.CommonClientConfigs.class);
    }

    /**
     * 构造方法，使用MQConfig初始化Kafka配置
     *
     * @param config Kafka配置对象，包含连接所需信息
     */
    public KafkaProvider(final MQConfig config) {
        init(config);
    }

    /**
     * 构造方法，使用Properties初始化Kafka配置
     *
     * @param properties Kafka配置属性
     */
    public KafkaProvider(final Properties properties) {
        init(properties);
    }

    /**
     * 使用MQConfig初始化Kafka配置
     *
     * @param config Kafka配置对象，包含连接所需信息
     * @return 当前KafkaProvider实例，支持链式调用
     */
    @Override
    public KafkaProvider init(final MQConfig config) {
        return init(buidProperties(config));
    }

    /**
     * 使用Properties初始化Kafka配置
     *
     * @param properties Kafka配置属性
     * @return 当前KafkaProvider实例，支持链式调用
     */
    public KafkaProvider init(final Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 增加配置项
     *
     * @param key   配置项名称
     * @param value 配置项值
     * @return 当前KafkaProvider实例，支持链式调用
     */
    public KafkaProvider addProperty(final String key, final String value) {
        this.properties.put(key, value);
        return this;
    }

    /**
     * 获取Kafka生产者实例
     *
     * @return Kafka生产者实例
     */
    @Override
    public Producer getProducer() {
        return new KafkaProducer(this.properties);
    }

    /**
     * 获取Kafka消费者实例
     *
     * @return Kafka消费者实例
     */
    @Override
    public Consumer getConsumer() {
        return new KafkaConsumer(this.properties);
    }

    /**
     * 根据MQConfig构建Kafka配置属性
     *
     * @param config Kafka配置对象，包含连接所需信息
     * @return 构建好的Kafka配置属性
     */
    private static Properties buidProperties(final MQConfig config) {
        final Properties properties = new Properties();
        // 设置Kafka服务器地址
        properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.getBrokerUrl());
        // 添加其他配置属性
        properties.putAll(config.getProperties());
        return properties;
    }

}