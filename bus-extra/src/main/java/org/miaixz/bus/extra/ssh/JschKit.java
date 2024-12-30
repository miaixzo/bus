/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.ssh;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.extra.ssh.provider.jsch.ChannelType;
import org.miaixz.bus.extra.ssh.provider.jsch.JschSession;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Jsch工具类 Jsch是Java Secure Channel的缩写。 JSch是一个SSH2的纯Java实现。 它允许你连接到一个SSH服务器，并且可以使用端口转发，X11转发，文件传输等。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JschKit {

    /**
     * 打开Session会话
     *
     * @param connector 连接信息
     * @return {@link JschSession}
     */
    public static Session openSession(final Connector connector) {
        final JSch jsch = new JSch();
        final com.jcraft.jsch.Session session;
        try {
            session = jsch.getSession(connector.getUser(), connector.getHost(), connector.getPort());
            session.setTimeout((int) connector.getTimeout());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }

        session.setPassword(connector.getPassword());
        // 设置第一次登录的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");

        // 设置登录认证方式，跳过Kerberos身份验证
        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

        return session;
    }

    /**
     * 打开Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @param timeout     连接超时时长，单位毫秒
     * @return {@link Channel}
     */
    public static Channel openChannel(final Session session, final ChannelType channelType, final long timeout) {
        final Channel channel = createChannel(session, channelType, timeout);
        try {
            channel.connect((int) Math.max(timeout, 0));
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * 创建Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @param timeout     session超时时常，单位：毫秒
     * @return {@link Channel}
     */
    public static Channel createChannel(final Session session, final ChannelType channelType, final long timeout) {
        final Channel channel;
        try {
            if (!session.isConnected()) {
                session.connect((int) timeout);
            }
            channel = session.openChannel(channelType.getValue());
        } catch (final JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * 关闭SSH连接会话
     *
     * @param session SSH会话
     */
    public static void close(final Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * 关闭会话通道
     *
     * @param channel 会话通道
     */
    public static void close(final Channel channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

}
