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
package org.miaixz.bus.extra.ssh.provider.mina;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.MinaKit;
import org.miaixz.bus.extra.ssh.Session;

/**
 * Apache MINA SSHD（https://mina.apache.org/sshd-project/）会话封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MinaSession implements Session {

    private final SshClient sshClient;
    private final ClientSession raw;

    /**
     * 构造
     *
     * @param connector 连接信息
     */
    public MinaSession(final Connector connector) {
        this.sshClient = MinaKit.openClient();
        // https://github.com/apache/mina-sshd/blob/master/docs/port-forwarding.md#standard-port-forwarding
        this.sshClient.setForwardingFilter(new AcceptAllForwardingFilter());
        this.raw = MinaKit.openSession(this.sshClient, connector);
    }

    @Override
    public Object getRaw() {
        return this.raw;
    }

    @Override
    public boolean isConnected() {
        return null != this.raw && this.raw.isOpen();
    }

    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.raw);
        if (null != this.sshClient) {
            this.sshClient.stop();
        }
        IoKit.closeQuietly(this.sshClient);
    }

    @Override
    public void bindLocalPort(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress)
            throws InternalException {
        try {
            this.raw.startLocalPortForwarding(new SshdSocketAddress(localAddress),
                    new SshdSocketAddress(remoteAddress));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void unBindLocalPort(final InetSocketAddress localAddress) throws InternalException {
        try {
            this.raw.stopLocalPortForwarding(new SshdSocketAddress(localAddress));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void bindRemotePort(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress)
            throws InternalException {
        try {
            this.raw.startRemotePortForwarding(new SshdSocketAddress(remoteAddress),
                    new SshdSocketAddress(localAddress));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void unBindRemotePort(final InetSocketAddress remoteAddress) throws InternalException {
        try {
            this.raw.stopRemotePortForwarding(new SshdSocketAddress(remoteAddress));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 执行Shell命令
     *
     * @param cmd     命令
     * @param charset 发送和读取内容的编码
     * @return 结果
     */
    public String exec(final String cmd, final Charset charset) {
        return exec(cmd, charset, System.err);
    }

    /**
     * 执行Shell命令（使用EXEC方式）
     * <p>
     * 此方法单次发送一个命令到服务端，不读取环境变量，执行结束后自动关闭channel，不会产生阻塞。
     * </p>
     * 参考：https://github.com/apache/mina-sshd/blob/master/docs/client-setup.md#running-a-command-or-opening-a-shell
     *
     * @param cmd       命令
     * @param charset   发送和读取内容的编码
     * @param errStream 错误信息输出到的位置
     * @return 执行结果内容
     */
    public String exec(final String cmd, final Charset charset, final OutputStream errStream) {
        try {
            return this.raw.executeRemoteCommand(cmd, errStream, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 执行Shell命令
     * <p>
     * 此方法单次发送一个命令到服务端，自动读取环境变量，执行结束后自动关闭channel，不会产生阻塞。
     * </p>
     *
     * @param cmd       命令
     * @param charset   发送和读取内容的编码
     * @param errStream 异常输出位置
     * @return 结果
     */
    public String execByShell(final String cmd, final Charset charset, final OutputStream errStream) {
        final ChannelShell shellChannel;
        try {
            shellChannel = this.raw.createShellChannel();
            if (null != errStream) {
                shellChannel.setErr(errStream);
            }
            shellChannel.open().verify();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        IoKit.write(shellChannel.getInvertedIn(), charset, false, cmd);
        return IoKit.read(shellChannel.getInvertedOut(), charset);
    }
}
