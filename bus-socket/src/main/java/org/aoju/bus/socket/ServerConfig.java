/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org sandao and other contributors.               *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket;

import org.aoju.bus.socket.process.MessageProcessor;

import java.net.SocketOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Quickly服务端/客户端配置信息 T:解码后生成的对象类型
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
public class ServerConfig<T> {

    /**
     * 消息体缓存大小,字节
     */
    private int readBufferSize = 512;
    /**
     * 内存块大小限制
     */
    private int writeBufferSize = 128;
    /**
     * Write缓存区容量
     */
    private int writeBufferCapacity = 16;
    /**
     * 远程服务器IP
     */
    private String host;
    /**
     * 服务器消息拦截器
     */
    private NetMonitor monitor;
    /**
     * 服务器端口号
     */
    private int port = 8888;

    /**
     * 服务端backlog
     */
    private int backlog = 1000;

    /**
     * 消息处理器
     */
    private MessageProcessor<T> processor;
    /**
     * 协议编解码
     */
    private Protocol<T> protocol;
    /**
     * Socket 配置
     */
    private Map<SocketOption<Object>, Object> socketOptions;
    /**
     * 线程数
     */
    private int threadNum = 1;
    /**
     * 内存池工厂
     */
    private BufferFactory bufferFactory = BufferFactory.DISABLED_BUFFER_FACTORY;

    /**
     * 获取默认内存块大小
     *
     * @return 内存块大小
     */
    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    /**
     * @param writeBufferSize 内存块大小
     */
    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    /**
     * @return 主机地址
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host 主机地址
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port 端口号
     */
    public void setPort(int port) {
        this.port = port;
    }

    public NetMonitor getMonitor() {
        return monitor;
    }

    public Protocol<T> getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol<T> protocol) {
        this.protocol = protocol;
    }

    public MessageProcessor<T> getProcessor() {
        return processor;
    }

    /**
     * @param processor 消息处理器
     */
    public void setProcessor(MessageProcessor<T> processor) {
        this.processor = processor;
        this.monitor = (processor instanceof NetMonitor) ? (NetMonitor) processor : null;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * @param readBufferSize 读缓冲大小
     */
    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public Map<SocketOption<Object>, Object> getSocketOptions() {
        return socketOptions;
    }

    /**
     * @param socketOption socketOption名称
     * @param f            socketOption值
     */
    public void setOption(SocketOption socketOption, Object f) {
        if (null == socketOptions) {
            socketOptions = new HashMap<>(4);
        }
        socketOptions.put(socketOption, f);
    }

    public int getWriteBufferCapacity() {
        return writeBufferCapacity;
    }

    public void setWriteBufferCapacity(int writeBufferCapacity) {
        this.writeBufferCapacity = writeBufferCapacity;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public BufferFactory getBufferFactory() {
        return bufferFactory;
    }

    public void setBufferFactory(BufferFactory bufferFactory) {
        this.bufferFactory = bufferFactory;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "readBufferSize=" + readBufferSize +
                ", writeQueueCapacity=" + writeBufferCapacity +
                ", host='" + host + '\'' +
                ", monitor=" + monitor +
                ", port=" + port +
                ", processor=" + processor +
                ", protocol=" + protocol +
                ", socketOptions=" + socketOptions +
                ", threadNum=" + threadNum +
                ", writeBufferSize=" + writeBufferSize +
                '}';
    }

}
