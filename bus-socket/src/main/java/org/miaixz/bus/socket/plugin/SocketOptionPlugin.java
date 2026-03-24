/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * A plugin for setting {@link SocketOption}s on accepted {@link AsynchronousSocketChannel}s.
 * <p>
 * This plugin allows for the configuration of various TCP socket parameters, such as buffer sizes and TCP_NODELAY, on
 * newly accepted connections.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 21+
 */
public class SocketOptionPlugin<T> extends AbstractPlugin<T> {

    /**
     * A map storing the socket options and their corresponding values to be applied.
     */
    private Map<SocketOption<Object>, Object> optionMap = new HashMap<>();

    @Override
    public final AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        setOption(channel);
        return super.shouldAccept(channel);
    }

    /**
     * Sets the configured {@link SocketOption} values on the provided {@link AsynchronousSocketChannel}. By default,
     * this method applies all options specified via {@link #setOption(SocketOption, Object)}. Custom implementations
     * can override this method for more specific option setting logic.
     *
     * @param channel the {@link AsynchronousSocketChannel} on which to set the options
     */
    public void setOption(AsynchronousSocketChannel channel) {
        try {
            // Ensure TCP_NODELAY is set to true by default if not explicitly configured
            if (!optionMap.containsKey(StandardSocketOptions.TCP_NODELAY)) {
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            }
            for (Map.Entry<SocketOption<Object>, Object> entry : optionMap.entrySet()) {
                channel.setOption(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            Logger.error("Failed to set socket option: ", e);
        }
    }

    /**
     * Sets a {@link SocketOption} and its value to be applied to new channels.
     * <p>
     * Valid options for AIO clients typically include:
     * <ol>
     * <li>{@link StandardSocketOptions#SO_SNDBUF}</li>
     * <li>{@link StandardSocketOptions#SO_RCVBUF}</li>
     * <li>{@link StandardSocketOptions#SO_KEEPALIVE}</li>
     * <li>{@link StandardSocketOptions#SO_REUSEADDR}</li>
     * <li>{@link StandardSocketOptions#TCP_NODELAY}</li>
     * </ol>
     *
     * @param <V>          the type of the socket option value
     * @param socketOption the {@link SocketOption} to set
     * @param value        the value for the socket option
     * @return this {@code SocketOptionPlugin} instance for method chaining
     */
    public final <V> SocketOptionPlugin<T> setOption(SocketOption<V> socketOption, V value) {
        put0(socketOption, value);
        return this;
    }

    /**
     * Retrieves the value of a configured {@link SocketOption}.
     *
     * @param <V>          the type of the socket option value
     * @param socketOption the {@link SocketOption} to retrieve the value for
     * @return the configured value of the socket option, or {@code null} if not set
     */
    public final <V> V getOption(SocketOption<V> socketOption) {
        Object value = optionMap.get(socketOption);
        return value == null ? null : (V) value;
    }

    /**
     * Internal method to put a socket option and its value into the map.
     *
     * @param socketOption the socket option
     * @param value        the value of the socket option
     */
    private void put0(SocketOption socketOption, Object value) {
        optionMap.put(socketOption, value);
    }

}
