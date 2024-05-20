/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.extra.ssh.provider.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.JschKit;
import org.miaixz.bus.extra.ssh.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Jsch Session封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JschSession implements Session {

    private final com.jcraft.jsch.Session raw;
    private final long timeout;

    /**
     * 构造
     *
     * @param connector {@link Connector}，保存连接和验证信息等
     */
    public JschSession(final Connector connector) {
        this(JschKit.openSession(connector), connector.getTimeout());
    }

    /**
     * 构造
     *
     * @param raw     {@link com.jcraft.jsch.Session}
     * @param timeout 连接超时时常，0表示不限制
     */
    public JschSession(final com.jcraft.jsch.Session raw, final long timeout) {
        this.raw = raw;
        this.timeout = timeout;
    }

    @Override
    public com.jcraft.jsch.Session getRaw() {
        return this.raw;
    }

    @Override
    public boolean isConnected() {
        return null != this.raw && this.raw.isConnected();
    }

    @Override
    public void close() throws IOException {
        JschKit.close(this.raw);
    }

    @Override
    public void bindLocalPort(final InetSocketAddress localAddress, final InetSocketAddress remoteAddress) throws InternalException {
        try {
            this.raw.setPortForwardingL(localAddress.getHostName(), localAddress.getPort(), remoteAddress.getHostName(), remoteAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e, "From [{}] mapping to [{}] error！", localAddress, remoteAddress);
        }
    }

    @Override
    public void unBindLocalPort(final InetSocketAddress localAddress) {
        try {
            this.raw.delPortForwardingL(localAddress.getHostName(), localAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void bindRemotePort(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) throws InternalException {
        try {
            this.raw.setPortForwardingR(remoteAddress.getHostName(), remoteAddress.getPort(),
                    localAddress.getHostName(), localAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e, "From [{}] mapping to [{}] error！", remoteAddress, localAddress);
        }
    }

    @Override
    public void unBindRemotePort(final InetSocketAddress remoteAddress) {
        try {
            this.raw.delPortForwardingR(remoteAddress.getHostName(), remoteAddress.getPort());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建Channel连接
     *
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @return {@link Channel}
     */
    public Channel createChannel(final ChannelType channelType) {
        return JschKit.createChannel(this.raw, channelType, this.timeout);
    }

    /**
     * 打开Shell连接
     *
     * @return {@link ChannelShell}
     */
    public ChannelShell openShell() {
        return (ChannelShell) openChannel(ChannelType.SHELL);
    }

    /**
     * 打开Channel连接
     *
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @return {@link Channel}
     */
    public Channel openChannel(final ChannelType channelType) {
        return JschKit.openChannel(this.raw, channelType, this.timeout);
    }

    /**
     * 打开SFTP会话
     *
     * @param charset 编码
     * @return {@link JschSftp}
     */
    public JschSftp openSftp(final java.nio.charset.Charset charset) {
        return new JschSftp(this.raw, charset, this.timeout);
    }

    /**
     * 执行Shell命令
     *
     * @param cmd     命令
     * @param charset 发送和读取内容的编码
     * @return {@link ChannelExec}
     */
    public String exec(final String cmd, final java.nio.charset.Charset charset) {
        return exec(cmd, charset, System.err);
    }

    /**
     * 执行Shell命令（使用EXEC方式）
     * <p>
     * 此方法单次发送一个命令到服务端，不读取环境变量，执行结束后自动关闭channel，不会产生阻塞。
     * </p>
     *
     * @param cmd       命令
     * @param charset   发送和读取内容的编码
     * @param errStream 错误信息输出到的位置
     * @return 执行结果内容
     */
    public String exec(final String cmd, java.nio.charset.Charset charset, final OutputStream errStream) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }
        final ChannelExec channel = (ChannelExec) createChannel(ChannelType.EXEC);
        channel.setCommand(ByteKit.toBytes(cmd, charset));
        channel.setInputStream(null);

        channel.setErrStream(errStream);
        InputStream in = null;
        try {
            channel.connect();
            in = channel.getInputStream();
            return IoKit.read(in, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final JSchException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    /**
     * 执行Shell命令
     * <p>
     * 此方法单次发送一个命令到服务端，自动读取环境变量，执行结束后自动关闭channel，不会产生阻塞。
     * </p>
     *
     * @param cmd     命令
     * @param charset 发送和读取内容的编码
     * @return {@link ChannelExec}
     */
    public String execByShell(final String cmd, final java.nio.charset.Charset charset) {
        final ChannelShell shell = openShell();
        // 开始连接
        shell.setPty(true);
        OutputStream out = null;
        InputStream in = null;
        try {
            out = shell.getOutputStream();
            in = shell.getInputStream();

            out.write(ByteKit.toBytes(cmd, charset));
            out.flush();

            return IoKit.read(in, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(out);
            IoKit.closeQuietly(in);
            if (shell.isConnected()) {
                shell.disconnect();
            }
        }
    }
}
