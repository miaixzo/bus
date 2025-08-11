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
package org.miaixz.bus.vortex.support;

import reactor.core.publisher.Mono;

/**
 * 消息队列生产者接口，用于向指定主题发送消息。
 * <p>
 * 该接口定义了消息队列生产者的核心功能，允许实现类以异步方式向消息队列的指定主题发送消息。 通过返回 {@link Mono}<{@link Void}> 支持响应式编程，确保非阻塞操作。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MqProducer {

    /**
     * 向指定的消息队列主题发送消息。
     * <p>
     * 该方法以异步方式将消息发送到指定的主题。实现类需要处理消息的序列化、发送逻辑以及与消息队列系统的交互。 返回的 {@link Mono}<{@link Void}> 表示发送操作的完成状态，成功时完成，失败时抛出异常。
     * </p>
     *
     * @param topic   消息队列的主题名称，用于标识消息的目标队列
     * @param message 要发送的消息内容
     * @return {@link Mono}<{@link Void}> 表示异步发送操作的完成状态
     */
    Mono<Void> send(String topic, String message);

}