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
package org.miaixz.bus.extra.mq.provider.jms;

import java.io.IOException;
import org.miaixz.bus.core.lang.exception.MQueueException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.mq.Consumer;
import org.miaixz.bus.extra.mq.Message;
import org.miaixz.bus.extra.mq.MessageHandler;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;

/**
 * JMS消息消费者实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JmsConsumer implements Consumer {

    /**
     * 消费者组名称
     */
    private String consumerGroup;

    /**
     * JMS消息消费者对象
     */
    private final MessageConsumer consumer;

    /**
     * 构造方法
     *
     * @param consumerGroup 消费者组名称
     * @param consumer      JMS消息消费者对象
     */
    public JmsConsumer(final String consumerGroup, final MessageConsumer consumer) {
        this.consumerGroup = consumerGroup;
        this.consumer = consumer;
    }

    /**
     * 设置消费者组名称
     *
     * @param consumerGroup 消费者组名称
     * @return 当前JmsConsumer实例，支持链式调用
     */
    public JmsConsumer setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * 订阅消息并注册消息处理器
     *
     * @param messageHandler 消息处理器，用于处理接收到的消息
     */
    @Override
    public void subscribe(final MessageHandler messageHandler) {
        try {
            // 设置消息监听器
            this.consumer.setMessageListener(message -> {
                // 创建消息对象并交给处理器处理
                messageHandler.handle(new Message() {

                    /**
                     * 获取消息主题（消费者组名称）
                     *
                     * @return 消费者组名称作为主题
                     */
                    @Override
                    public String topic() {
                        return consumerGroup;
                    }

                    /**
                     * 获取消息内容
                     *
                     * @return 消息内容的字节数组
                     * @throws MQueueException 处理消息内容时发生异常
                     */
                    @Override
                    public byte[] content() {
                        try {
                            // 处理文本消息
                            if (message instanceof TextMessage) {
                                return ByteKit.toBytes(((TextMessage) message).getText());
                            }
                            // 处理字节消息
                            else if (message instanceof BytesMessage) {
                                // 创建与消息长度相同的字节数组
                                final byte[] bytes = new byte[(int) ((BytesMessage) message).getBodyLength()];
                                // 读取字节消息内容
                                ((BytesMessage) message).readBytes(bytes);
                                return bytes;
                            }
                            // 不支持的消息类型
                            else {
                                throw new IllegalArgumentException(
                                        "Unsupported message type: " + message.getClass().getName());
                            }
                        } catch (final JMSException e) {
                            throw new MQueueException(e);
                        }
                    }
                });
            });
        } catch (final JMSException e) {
            throw new MQueueException(e);
        }
    }

    /**
     * 关闭消费者，释放资源
     *
     * @throws IOException 关闭过程中发生IO异常时抛出
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.consumer);
    }

}
