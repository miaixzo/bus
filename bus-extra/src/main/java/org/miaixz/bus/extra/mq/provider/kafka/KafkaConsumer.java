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
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;

/**
 * Kafka消费端实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class KafkaConsumer implements Consumer {

    /**
     * Kafka原生消费者实例
     */
    private final org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer;

    /**
     * 构造方法
     *
     * @param properties Kafka配置属性
     */
    public KafkaConsumer(final Properties properties) {
        this.consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(properties);
    }

    /**
     * 构造方法
     *
     * @param consumer Kafka原生消费者实例
     */
    public KafkaConsumer(final org.apache.kafka.clients.consumer.Consumer<String, byte[]> consumer) {
        this.consumer = consumer;
    }

    /**
     * 设置消费的topic
     *
     * @param topics 要订阅的主题列表
     * @return 当前KafkaConsumer实例，支持链式调用
     */
    public KafkaConsumer setTopics(final String... topics) {
        this.consumer.subscribe(ListKit.of(topics));
        return this;
    }

    /**
     * 设置消费的topic正则
     *
     * @param topicPattern 主题匹配模式，使用正则表达式匹配多个主题
     * @return 当前KafkaConsumer实例，支持链式调用
     */
    public KafkaConsumer setTopicPattern(final Pattern topicPattern) {
        this.consumer.subscribe(topicPattern);
        return this;
    }

    /**
     * 订阅消息并注册消息处理器
     *
     * @param messageHandler 消息处理器，用于处理接收到的消息
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        for (final ConsumerRecord<String, byte[]> record : this.consumer.poll(Duration.ofMillis(3000))) {
            messageHandler.handle(new ConsumerRecordMessage(record));
        }
    }

    /**
     * 关闭消费者，释放资源
     *
     * @throws IOException 关闭过程中发生IO异常时抛出
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.consumer);
    }

    /**
     * 消费者记录包装为消息 将Kafka的ConsumerRecord包装为统一的Message接口实现
     */
    private static class ConsumerRecordMessage implements Message {

        /**
         * Kafka消费者记录
         */
        private final ConsumerRecord<String, byte[]> record;

        /**
         * 构造方法
         *
         * @param record Kafka消费者记录
         */
        private ConsumerRecordMessage(final ConsumerRecord<String, byte[]> record) {
            this.record = record;
        }

        /**
         * 获取消息主题
         *
         * @return 消息主题名称
         */
        @Override
        public String topic() {
            return record.topic();
        }

        /**
         * 获取消息内容
         *
         * @return 消息内容的字节数组
         */
        @Override
        public byte[] content() {
            return record.value();
        }
    }

}
