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

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.Producer;

/**
 * Kafka 生产者实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KafkaProducer implements Producer {

    /**
     * Kafka原生生产者实例
     */
    private final org.apache.kafka.clients.producer.Producer<String, byte[]> producer;

    /**
     * 构造方法
     *
     * @param properties Kafka配置属性
     */
    public KafkaProducer(final Properties properties) {
        this(new org.apache.kafka.clients.producer.KafkaProducer<>(properties));
    }

    /**
     * 构造方法
     *
     * @param producer Kafka原生生产者实例
     */
    public KafkaProducer(final org.apache.kafka.clients.producer.Producer<String, byte[]> producer) {
        this.producer = producer;
    }

    /**
     * 发送消息到指定主题
     *
     * @param message 要发送的消息对象，包含主题和内容
     */
    @Override
    public void send(final Message message) {
        this.producer.send(new ProducerRecord<>(message.topic(), message.content()));
    }

    /**
     * 关闭生产者，释放资源
     *
     * @throws IOException 关闭过程中发生IO异常时抛出
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.producer);
    }

}