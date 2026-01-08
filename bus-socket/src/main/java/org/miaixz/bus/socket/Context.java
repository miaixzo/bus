/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.socket;

import org.miaixz.bus.core.lang.Normal;

import java.net.SocketOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the context for a server or client, holding configuration and operational parameters. The generic type
 * {@code T} represents the type of object produced after decoding.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Context {

    /**
     * The size of the read buffer for message bodies, in bytes.
     */
    private int readBufferSize = Normal._512;
    /**
     * The size limit for individual memory chunks in the write buffer.
     */
    private int writeBufferSize = Normal._128;
    /**
     * The capacity of the write buffer queue.
     */
    private int writeBufferCapacity = Normal._16;
    /**
     * The IP address of the remote server.
     */
    private String host;
    /**
     * The message interceptor for monitoring server events.
     */
    private Monitor monitor;
    /**
     * The port number for the server.
     */
    private int port = 7890;

    /**
     * The server socket backlog size.
     */
    private int backlog = 1000;

    /**
     * The message handler for processing incoming data.
     */
    private Handler processor;
    /**
     * The message codec for encoding and decoding data.
     */
    private Message message;

    /**
     * A map of socket options to be configured on the underlying socket.
     */
    private Map<SocketOption<Object>, Object> socketOptions;

    /**
     * The number of threads for the worker pool.
     */
    private int threadNum = 1;

    /**
     * Gets the default size of memory chunks for writing.
     *
     * @return the write buffer size
     */
    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    /**
     * Sets the size of memory chunks for writing.
     *
     * @param writeBufferSize the new write buffer size
     */
    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    /**
     * Gets the host address.
     *
     * @return the host address
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host address.
     *
     * @param host the new host address
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port number.
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number.
     *
     * @param port the new port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the monitor for this context.
     *
     * @return the current {@link Monitor}
     */
    public Monitor getMonitor() {
        return monitor;
    }

    /**
     * Gets the protocol message codec.
     *
     * @return the current {@link Message} codec
     */
    public Message getProtocol() {
        return message;
    }

    /**
     * Sets the protocol message codec.
     *
     * @param message the new {@link Message} codec
     */
    public void setProtocol(Message message) {
        this.message = message;
    }

    /**
     * Gets the message handler.
     *
     * @return the current {@link Handler}
     */
    public Handler getProcessor() {
        return processor;
    }

    /**
     * Sets the message handler. If the handler is also an instance of {@link Monitor}, it will be set as the monitor
     * for this context.
     *
     * @param processor the new message handler
     */
    public void setProcessor(Handler processor) {
        this.processor = processor;
        this.monitor = (processor instanceof Monitor) ? (Monitor) processor : null;
    }

    /**
     * Gets the read buffer size.
     *
     * @return the read buffer size in bytes
     */
    public int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * Sets the read buffer size.
     *
     * @param readBufferSize the new read buffer size in bytes
     */
    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    /**
     * Gets the map of configured socket options.
     *
     * @return the map of socket options
     */
    public Map<SocketOption<Object>, Object> getSocketOptions() {
        return socketOptions;
    }

    /**
     * Sets a socket option.
     *
     * @param socketOption the socket option to set
     * @param value        the value of the socket option
     */
    public void setOption(SocketOption socketOption, Object value) {
        if (socketOptions == null) {
            socketOptions = new HashMap<>(4);
        }
        socketOptions.put(socketOption, value);
    }

    /**
     * Gets the capacity of the write buffer queue.
     *
     * @return the write buffer capacity
     */
    public int getWriteBufferCapacity() {
        return writeBufferCapacity;
    }

    /**
     * Sets the capacity of the write buffer queue.
     *
     * @param writeBufferCapacity the new capacity
     */
    public void setWriteBufferCapacity(int writeBufferCapacity) {
        this.writeBufferCapacity = writeBufferCapacity;
    }

    /**
     * Gets the number of threads for the worker pool.
     *
     * @return the number of threads
     */
    public int getThreadNum() {
        return threadNum;
    }

    /**
     * Sets the number of threads for the worker pool.
     *
     * @param threadNum the number of threads
     */
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    /**
     * Gets the server socket backlog size.
     *
     * @return the backlog size
     */
    public int getBacklog() {
        return backlog;
    }

    /**
     * Sets the server socket backlog size.
     *
     * @param backlog the new backlog size
     */
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return a string representation of this context
     */
    @Override
    public String toString() {
        return "Context{" + "readBufferSize=" + readBufferSize + ", writeBufferSize=" + writeBufferSize
                + ", writeBufferCapacity=" + writeBufferCapacity + ", host='" + host + '\'' + ", monitor=" + monitor
                + ", port=" + port + ", backlog=" + backlog + ", processor=" + processor + ", protocol=" + message
                + ", socketOptions=" + socketOptions + ", threadNum=" + threadNum + '}';
    }

}
