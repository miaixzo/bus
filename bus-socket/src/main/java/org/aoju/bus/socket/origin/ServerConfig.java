/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.socket.origin;

import java.net.SocketOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Quickly服务端/客户端配置信息 T:解码后生成的对象类型
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
final class ServerConfig<T> {

    /**
     * 消息体缓存大小,字节
     */
    private int readBufferSize = 512;

    /**
     * Write缓存区容量
     */
    private int writeQueueCapacity = 512;
    /**
     * 远程服务器IP
     */
    private String host;
    /**
     * 服务器消息拦截器
     */
    private NetMonitor<T> monitor;
    /**
     * 服务器端口号
     */
    private int port = 8888;
    /**
     * 消息处理器
     */
    private Message<T> processor;
    /**
     * 协议编解码
     */
    private Protocol<T> protocol;
    /**
     * 是否启用控制台banner
     */
    private boolean bannerEnabled = true;

    /**
     * Socket 配置
     */
    private Map<SocketOption<Object>, Object> socketOptions;

    /**
     * 消息体缓存大小,字节
     */
    private int writeBufferSize = 512;
    /**
     * 线程数
     */
    private int threadNum = 1;


    private int readBacklog = getIntProperty(ServerConfig.Property.READ_BACKLOG, 4096);

    static int getIntProperty(String property, int defaultVal) {
        String valString = System.getProperty(property);
        if (valString != null) {
            try {
                return Integer.parseInt(valString);
            } catch (NumberFormatException e) {
            }
        }
        return defaultVal;
    }

    static boolean getBoolProperty(String property, boolean defaultVal) {
        String valString = System.getProperty(property);
        if (valString != null) {
            return Boolean.parseBoolean(valString);
        }
        return defaultVal;
    }

    public final String getHost() {
        return host;
    }

    public final void setHost(String host) {
        this.host = host;
    }

    public final int getPort() {
        return port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

    public NetMonitor<T> getMonitor() {
        return monitor;
    }

    public Protocol<T> getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol<T> protocol) {
        this.protocol = protocol;
    }

    public final Message<T> getProcessor() {
        return processor;
    }

    public final void setProcessor(Message<T> processor) {
        this.processor = processor;
        this.monitor = (processor instanceof NetMonitor) ? (NetMonitor<T>) processor : null;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public void setBannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
    }

    public Map<SocketOption<Object>, Object> getSocketOptions() {
        return socketOptions;
    }

    public void setOption(SocketOption socketOption, Object f) {
        if (socketOptions == null) {
            socketOptions = new HashMap<>();
        }
        socketOptions.put(socketOption, f);
    }

    public int getWriteQueueCapacity() {
        return writeQueueCapacity;
    }

    public void setWriteQueueCapacity(int writeQueueCapacity) {
        this.writeQueueCapacity = writeQueueCapacity;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public int getReadBacklog() {
        return readBacklog;
    }

    /**
     * 服务配置
     */
    interface Property {
        String PROJECT_NAME = "bus-socket";
        String SESSION_WRITE_CHUNK_SIZE = PROJECT_NAME + ".session.writeChunkSize";
        String BUFFER_PAGE_NUM = PROJECT_NAME + ".bufferPool.pageNum";
        String SERVER_PAGE_SIZE = PROJECT_NAME + ".server.pageSize";
        String CLIENT_PAGE_SIZE = PROJECT_NAME + ".client.pageSize";
        String SERVER_PAGE_IS_DIRECT = PROJECT_NAME + ".server.page.isDirect";
        String CLIENT_PAGE_IS_DIRECT = PROJECT_NAME + ".client.page.isDirect";
        String READ_BACKLOG = PROJECT_NAME + ".read.backlog";
    }

}
