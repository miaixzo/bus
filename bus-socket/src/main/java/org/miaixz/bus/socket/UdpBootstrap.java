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
package org.miaixz.bus.socket;

import org.miaixz.bus.socket.buffers.BufferFactory;
import org.miaixz.bus.socket.buffers.BufferPool;
import org.miaixz.bus.socket.process.MessageProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * UDP服务启动类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UdpBootstrap {

    /**
     * 服务配置
     */
    private final ServerConfig config = new ServerConfig();
    /**
     * 内存池
     */
    private BufferPool bufferPool;
    private BufferPool innerBufferPool = null;
    private WorkerRegister workerRegister;
    private boolean innerWorker = false;

    public <Request> UdpBootstrap(Protocol<Request> protocol, MessageProcessor<Request> messageProcessor, WorkerRegister workerRegister) {
        this(protocol, messageProcessor);
        this.workerRegister = workerRegister;
    }

    public <Request> UdpBootstrap(Protocol<Request> protocol, MessageProcessor<Request> messageProcessor) {
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
    }

    /**
     * 开启一个UDP通道，端口号随机
     *
     * @return UDP通道
     */
    public UdpChannel open() throws IOException {
        return open(0);
    }

    /**
     * 开启一个UDP通道
     *
     * @param port 指定绑定端口号,为0则随机指定
     */
    public UdpChannel open(int port) throws IOException {
        return open(null, port);
    }

    /**
     * 开启一个UDP通道
     *
     * @param host 绑定本机地址
     * @param port 指定绑定端口号,为0则随机指定
     */
    public UdpChannel open(String host, int port) throws IOException {
        // 初始化内存池
        if (bufferPool == null) {
            this.bufferPool = config.getBufferFactory().create();
            this.innerBufferPool = bufferPool;
        }
        // 初始化工作线程
        if (workerRegister == null) {
            innerWorker = true;
            workerRegister = new WorkerRegister(bufferPool, config.getThreadNum());
        }
        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        if (port > 0) {
            InetSocketAddress inetSocketAddress = host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
            channel.socket().bind(inetSocketAddress);
        }
        return new UdpChannel(channel, workerRegister, config, bufferPool.allocateBufferPage());
    }

    private synchronized void initWorker() {
        if (workerRegister != null) {
            return;
        }
    }

    public void shutdown() {
        if (innerWorker) {
            workerRegister.shutdown();
        }
        if (innerBufferPool != null) {
            innerBufferPool.release();
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     */
    public final UdpBootstrap setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置线程大小
     *
     * @param num 大小
     * @return the object
     */
    public final UdpBootstrap setThreadNum(int num) {
        this.config.setThreadNum(num);
        return this;
    }

    /**
     * 设置内存池
     * 通过该方法设置的内存池，在AioQuickServer执行shutdown时不会触发内存池的释放。
     * 该方法适用于多个AioQuickServer、AioQuickClient共享内存池的场景。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferPool 内存池对象
     * @return 当前AioQuickServer对象
     */
    public final UdpBootstrap setBufferPagePool(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
        this.config.setBufferFactory(BufferFactory.DISABLED_BUFFER_FACTORY);
        return this;
    }

    /**
     * 设置内存池的构造工厂。
     * 通过工厂形式生成的内存池会强绑定到当前UdpBootstrap对象，
     * 在UdpBootstrap执行shutdown时会释放内存池。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferFactory 内存池工厂
     * @return 当前AioQuickServer对象
     */
    public final UdpBootstrap setBufferFactory(BufferFactory bufferFactory) {
        this.config.setBufferFactory(bufferFactory);
        this.bufferPool = null;
        return this;
    }

}

