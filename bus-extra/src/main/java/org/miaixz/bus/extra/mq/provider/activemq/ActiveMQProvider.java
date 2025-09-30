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
package org.miaixz.bus.extra.mq.provider.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.mq.MQConfig;
import org.miaixz.bus.extra.mq.provider.jms.JmsProvider;

import jakarta.jms.ConnectionFactory;

/**
 * ActiveMQ引擎实现类 提供ActiveMQ的消息队列服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ActiveMQProvider extends JmsProvider {

    /**
     * 默认构造方法 用于SPI方式加载时检查库是否引入
     */
    public ActiveMQProvider() {
        // SPI方式加载时检查库是否引入
        Assert.notNull(org.apache.activemq.ActiveMQConnectionFactory.class);
    }

    /**
     * 创建ActiveMQ连接工厂
     *
     * @param config ActiveMQ配置对象，包含连接所需信息
     * @return ActiveMQ连接工厂
     */
    @Override
    protected ConnectionFactory createConnectionFactory(final MQConfig config) {
        return new ActiveMQConnectionFactory(config.getBrokerUrl());
    }

}
