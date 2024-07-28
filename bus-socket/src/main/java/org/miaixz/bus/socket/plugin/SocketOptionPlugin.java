/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于设置Socket Option的插件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SocketOptionPlugin<T> extends AbstractPlugin<T> {

    private Map<SocketOption<Object>, Object> optionMap = new HashMap<>();

    @Override
    public final AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        setOption(channel);
        return super.shouldAccept(channel);
    }

    /**
     * 往socket channel中设置option值。 默认将通过{@link #setOption(SocketOption, Object)}指定的配置值绑定到每一个Socket中。 如果有个性化的需求,可以重新实现本方法。
     *
     * @param channel 频道
     */
    public void setOption(AsynchronousSocketChannel channel) {
        try {
            if (!optionMap.containsKey(StandardSocketOptions.TCP_NODELAY)) {
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            }
            for (Map.Entry<SocketOption<Object>, Object> entry : optionMap.entrySet()) {
                channel.setOption(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            Logger.error("", e);
        }
    }

    /**
     * 设置Socket的TCP参数配置。
     * <p>
     * AIO客户端的有效可选范围为： 1. StandardSocketOptions.SO_SNDBUF 2. StandardSocketOptions.SO_RCVBUF 3.
     * StandardSocketOptions.SO_KEEPALIVE 4. StandardSocketOptions.SO_REUSEADDR 5. StandardSocketOptions.TCP_NODELAY
     * </p>
     *
     * @param <V>          泛型
     * @param socketOption 配置项
     * @param value        配置值
     * @return the object
     */
    public final <V> SocketOptionPlugin<T> setOption(SocketOption<V> socketOption, V value) {
        put0(socketOption, value);
        return this;
    }

    public final <V> V getOption(SocketOption<V> socketOption) {
        Object value = optionMap.get(socketOption);
        return value == null ? null : (V) value;
    }

    private void put0(SocketOption socketOption, Object value) {
        optionMap.put(socketOption, value);
    }

}
