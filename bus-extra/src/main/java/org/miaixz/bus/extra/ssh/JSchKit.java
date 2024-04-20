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
package org.miaixz.bus.extra.ssh;

import com.jcraft.jsch.*;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.NetKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSH2工具类
 * 它允许你连接到一个SSH服务器,并且可以使用端口转发,X11转发,文件传输等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JSchKit {

    /**
     * 不使用SSH的值
     */
    public static final String SSH_NONE = "none";

    /**
     * 本地端口生成器
     */
    private static final AtomicInteger port = new AtomicInteger(10000);

    /**
     * 生成一个本地端口,用于远程端口映射
     *
     * @return 未被使用的本地端口
     */
    public static int generateLocalPort() {
        int validPort = port.get();
        while (false == NetKit.isUsableLocalPort(validPort)) {
            validPort = port.incrementAndGet();
        }
        return validPort;
    }

    /**
     * 获得一个SSH会话,重用已经使用的会话
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @return SSH会话
     */
    public static Session getSession(String sshHost, int sshPort, String sshUser, String sshPass) {
        return JSchSessionPool.INSTANCE.getSession(sshHost, sshPort, sshUser, sshPass);
    }

    /**
     * 获得一个SSH会话，重用已经使用的会话
     *
     * @param sshHost        主机
     * @param sshPort        端口
     * @param sshUser        用户名
     * @param privateKeyPath 私钥路径
     * @param passphrase     私钥密码
     * @return SSH会话
     */
    public static Session getSession(String sshHost, int sshPort, String sshUser, String privateKeyPath, byte[] passphrase) {
        return JSchSessionPool.INSTANCE.getSession(sshHost, sshPort, sshUser, privateKeyPath, passphrase);
    }

    /**
     * 打开一个新的SSH会话
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @return SSH会话
     */
    public static Session openSession(String sshHost, int sshPort, String sshUser, String sshPass) {
        return openSession(sshHost, sshPort, sshUser, sshPass, 0);
    }

    /**
     * 打开一个新的SSH会话
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param timeout Socket连接超时时长，单位毫秒
     * @return SSH会话
     */
    public static Session openSession(String sshHost, int sshPort, String sshUser, String sshPass, int timeout) {
        final Session session = createSession(sshHost, sshPort, sshUser, sshPass);
        try {
            session.connect(timeout);
        } catch (JSchException e) {
            throw new InternalException(e);
        }
        return session;
    }

    /**
     * 打开一个新的SSH会话
     *
     * @param sshHost        主机
     * @param sshPort        端口
     * @param sshUser        用户名
     * @param privateKeyPath 私钥的路径
     * @param passphrase     私钥文件的密码，可以为null
     * @return SSH会话
     */
    public static Session openSession(String sshHost, int sshPort, String sshUser, String privateKeyPath, byte[] passphrase) {
        return openSession(sshHost, sshPort, sshUser, privateKeyPath, passphrase, 0);
    }

    /**
     * 打开一个新的SSH会话
     *
     * @param sshHost        主机
     * @param sshPort        端口
     * @param sshUser        用户名
     * @param privateKeyPath 私钥的路径
     * @param passphrase     私钥文件的密码，可以为null
     * @param timeOut        超时时间，单位毫秒
     * @return SSH会话
     */
    public static Session openSession(String sshHost, int sshPort, String sshUser, String privateKeyPath, byte[] passphrase, int timeOut) {
        final Session session = createSession(sshHost, sshPort, sshUser, privateKeyPath, passphrase);
        try {
            session.connect(timeOut);
        } catch (JSchException e) {
            throw new InternalException(e);
        }
        return session;
    }

    /**
     * 新建一个新的SSH会话，此方法并不打开会话(既不调用connect方法)
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名，如果为null，默认root
     * @param sshPass 密码
     * @return SSH会话
     */
    public static Session createSession(String sshHost, int sshPort, String sshUser, String sshPass) {
        final JSch jsch = new JSch();
        final Session session = createSession(jsch, sshHost, sshPort, sshUser);

        if (StringKit.isNotEmpty(sshPass)) {
            session.setPassword(sshPass);
        }

        return session;
    }

    /**
     * 新建一个新的SSH会话，此方法并不打开会话(既不调用connect方法)
     *
     * @param sshHost        主机
     * @param sshPort        端口
     * @param sshUser        用户名，如果为null，默认root
     * @param privateKeyPath 私钥的路径
     * @param passphrase     私钥文件的密码，可以为null
     * @return SSH会话
     */
    public static Session createSession(String sshHost, int sshPort, String sshUser, String privateKeyPath, byte[] passphrase) {
        Assert.notEmpty(privateKeyPath, "PrivateKey Path must be not empty!");

        final JSch jsch = new JSch();
        try {
            jsch.addIdentity(privateKeyPath, passphrase);
        } catch (JSchException e) {
            throw new InternalException(e);
        }

        return createSession(jsch, sshHost, sshPort, sshUser);
    }

    /**
     * 创建一个SSH会话，重用已经使用的会话
     *
     * @param jsch    {@link JSch}
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名，如果为null，默认root
     * @return {@link Session}
     */
    public static Session createSession(JSch jsch, String sshHost, int sshPort, String sshUser) {
        Assert.notEmpty(sshHost, "SSH Host must be not empty!");
        Assert.isTrue(sshPort > 0, "SSH port must be > 0");

        // 默认root用户
        if (StringKit.isEmpty(sshUser)) {
            sshUser = "root";
        }

        if (null == jsch) {
            jsch = new JSch();
        }

        Session session;
        try {
            session = jsch.getSession(sshUser, sshHost, sshPort);
        } catch (JSchException e) {
            throw new InternalException(e);
        }

        // 设置第一次登录的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");

        return session;
    }

    /**
     * 绑定端口到本地  一个会话可绑定多个端口
     *
     * @param session    需要绑定端口的SSH会话
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @param localPort  本地端口
     * @return 成功与否
     * @throws InternalException 端口绑定失败异常
     */
    public static boolean bindPort(Session session, String remoteHost, int remotePort, int localPort) throws InternalException {
        return bindPort(session, remoteHost, remotePort, "127.0.0.1", localPort);
    }

    /**
     * 绑定端口到本地。 一个会话可绑定多个端口
     *
     * @param session    需要绑定端口的SSH会话
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @param localHost  本地主机
     * @param localPort  本地端口
     * @return 成功与否
     * @throws InternalException 端口绑定失败异常
     */
    public static boolean bindPort(Session session, String remoteHost, int remotePort, String localHost, int localPort) throws InternalException {
        if (session != null && session.isConnected()) {
            try {
                session.setPortForwardingL(localHost, localPort, remoteHost, remotePort);
            } catch (JSchException e) {
                throw new InternalException("From [{}:{}] mapping to [{}:{}] error ！", remoteHost, remotePort, localHost, localPort);
            }
            return true;
        }
        return false;
    }

    /**
     * 绑定ssh服务端的serverPort端口, 到host主机的port端口上
     *
     * @param session  与ssh服务端建立的会话
     * @param bindPort ssh服务端上要被绑定的端口
     * @param host     转发到的host
     * @param port     host上的端口
     * @return 成功与否
     * @throws InternalException 端口绑定失败异常
     */
    public static boolean bindPort(Session session, int bindPort, String host, int port) throws InternalException {
        if (null != session && session.isConnected()) {
            try {
                session.setPortForwardingR(bindPort, host, port);
            } catch (JSchException e) {
                throw new InternalException("From [{}] mapping to [{}] error！", bindPort, port);
            }
            return true;
        }
        return false;
    }

    /**
     * 解除端口映射
     *
     * @param session   需要解除端口映射的SSH会话
     * @param localPort 需要解除的本地端口
     * @return 解除成功与否
     */
    public static boolean unBindPort(Session session, int localPort) {
        try {
            session.delPortForwardingL(localPort);
            return true;
        } catch (JSchException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 打开SSH会话,并绑定远程端口到本地的一个随机端口
     *
     * @param sshConn    SSH连接信息对象
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @return 映射后的本地端口
     * @throws InternalException 连接异常
     */
    public static int openAndBindPortToLocal(Connector sshConn, String remoteHost, int remotePort) throws InternalException {
        final Session session = openSession(sshConn.getHost(), sshConn.getPort(), sshConn.getUser(), sshConn.getPassword());
        if (null == session) {
            throw new InternalException("Error to create SSH Session！");
        }
        final int localPort = generateLocalPort();
        bindPort(session, remoteHost, remotePort, localPort);
        return localPort;
    }

    /**
     * 打开SFTP连接
     *
     * @param session Session会话
     * @return {@link ChannelSftp}
     */
    public static ChannelSftp openSftp(Session session) {
        return openSftp(session, 0);
    }

    /**
     * 打开SFTP连接
     *
     * @param session Session会话
     * @param timeout 连接超时时长，单位毫秒
     * @return {@link ChannelSftp}
     */
    public static ChannelSftp openSftp(Session session, int timeout) {
        return (ChannelSftp) openChannel(session, ChannelType.SFTP, timeout);
    }

    /**
     * 打开Shell连接
     *
     * @param session Session会话
     * @return {@link ChannelShell}
     */
    public static ChannelShell openShell(Session session) {
        return (ChannelShell) openChannel(session, ChannelType.SHELL);
    }

    /**
     * 打开Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型,可以是shell或sftp等,见{@link ChannelType}
     * @return {@link Channel}
     */
    public static Channel openChannel(Session session, ChannelType channelType) {
        final Channel channel = createChannel(session, channelType);
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * 打开Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @param timeout     连接超时时长，单位毫秒
     * @return {@link Channel}
     */
    public static Channel openChannel(Session session, ChannelType channelType, int timeout) {
        final Channel channel = createChannel(session, channelType);
        try {
            channel.connect(Math.max(timeout, 0));
        } catch (JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * 创建Sftp
     *
     * @param sshHost 远程主机
     * @param sshPort 远程主机端口
     * @param sshUser 远程主机用户名
     * @param sshPass 远程主机密码
     * @return {@link Sftp}
     */
    public static Sftp createSftp(String sshHost, int sshPort, String sshUser, String sshPass) {
        return new Sftp(sshHost, sshPort, sshUser, sshPass);
    }

    /**
     * 创建Sftp
     *
     * @param session SSH会话
     * @return {@link Sftp}
     */
    public static Sftp createSftp(Session session) {
        return new Sftp(session);
    }

    /**
     * 创建Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型,可以是shell或sftp等,见{@link ChannelType}
     * @return {@link Channel}
     */
    public static Channel createChannel(Session session, ChannelType channelType) {
        Channel channel;
        try {
            if (false == session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel(channelType.getValue());
        } catch (JSchException e) {
            throw new InternalException(e);
        }
        return channel;
    }

    /**
     * 执行Shell命令(使用EXEC方式)
     *
     * @param session Session会话
     * @param cmd     命令
     * @param charset 发送和读取内容的编码
     * @return {@link ChannelExec}
     */
    public static String exec(Session session, String cmd, java.nio.charset.Charset charset) {
        return exec(session, cmd, charset, System.err);
    }

    /**
     * 执行Shell命令
     * 此方法单次发送一个命令到服务端，不读取环境变量，执行结束后自动关闭channel，不会产生阻塞
     *
     * @param session   Session会话
     * @param cmd       命令
     * @param charset   发送和读取内容的编码
     * @param errStream 错误信息输出到的位置
     * @return 执行结果内容
     */
    public static String exec(Session session, String cmd, java.nio.charset.Charset charset, OutputStream errStream) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }
        ChannelExec channel = (ChannelExec) openChannel(session, ChannelType.EXEC);
        channel.setCommand(StringKit.bytes(cmd, charset));
        channel.setInputStream(null);
        channel.setErrStream(errStream);
        InputStream in = null;
        try {
            channel.start();
            in = channel.getInputStream();
            return IoKit.read(in, charset);
        } catch (IOException e) {
            throw new InternalException(e);
        } catch (JSchException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(in);
            close(channel);
        }
    }

    /**
     * 执行Shell命令
     * 此方法单次发送一个命令到服务端，自动读取环境变量，执行结束后自动关闭channel，不会产生阻塞
     *
     * @param session Session会话
     * @param cmd     命令
     * @param charset 发送和读取内容的编码
     * @return {@link ChannelExec}
     */
    public static String execByShell(Session session, String cmd, java.nio.charset.Charset charset) {
        final ChannelShell shell = openShell(session);
        shell.setPty(true);
        OutputStream out = null;
        InputStream in = null;
        try {
            out = shell.getOutputStream();
            in = shell.getInputStream();

            out.write(StringKit.bytes(cmd, charset));
            out.flush();

            return IoKit.read(in, charset);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(out);
            IoKit.close(in);
            close(shell);
        }
    }

    /**
     * 关闭SSH连接会话
     *
     * @param session SSH会话
     */
    public static void close(Session session) {
        if (null != session && session.isConnected()) {
            session.disconnect();
        }
        JSchSessionPool.INSTANCE.remove(session);
    }

    /**
     * 关闭会话通道
     *
     * @param channel 会话通道
     */
    public static void close(Channel channel) {
        if (null != channel && channel.isConnected()) {
            channel.disconnect();
        }
    }

    /**
     * 关闭SSH连接会话
     *
     * @param key 主机,格式为user@host:port
     */
    public static void close(String key) {
        JSchSessionPool.INSTANCE.close(key);
    }

    /**
     * 关闭所有SSH连接会话
     */
    public static void closeAll() {
        JSchSessionPool.INSTANCE.closeAll();
    }

}
