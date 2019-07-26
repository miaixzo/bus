package org.aoju.bus.extra;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.NetUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.extra.ssh.ChannelType;
import org.aoju.bus.extra.ssh.Connector;
import org.aoju.bus.extra.ssh.JschSessionPool;
import org.aoju.bus.extra.ssh.Sftp;
import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSH2工具类<br>
 * 它允许你连接到一个SSH服务器，并且可以使用端口转发，X11转发，文件传输等。<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SSHUtils {

    /**
     * 不使用SSH的值
     */
    public final static String SSH_NONE = "none";

    /**
     * 本地端口生成器
     */
    private static final AtomicInteger port = new AtomicInteger(10000);

    /**
     * 生成一个本地端口，用于远程端口映射
     *
     * @return 未被使用的本地端口
     */
    public static int generateLocalPort() {
        int validPort = port.get();
        while (false == NetUtils.isUsableLocalPort(validPort)) {
            validPort = port.incrementAndGet();
        }
        return validPort;
    }

    /**
     * 获得一个SSH会话，重用已经使用的会话
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @return SSH会话
     */
    public static Session getSession(String sshHost, int sshPort, String sshUser, String sshPass) {
        return JschSessionPool.INSTANCE.getSession(sshHost, sshPort, sshUser, sshPass);
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
        final Session session = createSession(sshHost, sshPort, sshUser, sshPass);
        try {
            session.connect();
        } catch (JSchException e) {
            throw new InstrumentException(e);
        }
        return session;
    }

    /**
     * 新建一个新的SSH会话
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 机用户名
     * @param sshPass 密码
     * @return SSH会话
     * @since 4.5.2
     */
    public static Session createSession(String sshHost, int sshPort, String sshUser, String sshPass) {
        if (StringUtils.isEmpty(sshHost) || sshPort < 0 || StringUtils.isEmpty(sshUser) || StringUtils.isEmpty(sshPass)) {
            return null;
        }

        Session session;
        try {
            session = new JSch().getSession(sshUser, sshHost, sshPort);
            session.setPassword(sshPass);
            // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
            session.setConfig("StrictHostKeyChecking", "no");
        } catch (JSchException e) {
            throw new InstrumentException(e);
        }
        return session;
    }

    /**
     * 绑定端口到本地。 一个会话可绑定多个端口
     *
     * @param session    需要绑定端口的SSH会话
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @param localPort  本地端口
     * @return 成功与否
     * @throws InstrumentException 端口绑定失败异常
     */
    public static boolean bindPort(Session session, String remoteHost, int remotePort, int localPort) throws InstrumentException {
        if (session != null && session.isConnected()) {
            try {
                session.setPortForwardingL(localPort, remoteHost, remotePort);
            } catch (JSchException e) {
                throw new InstrumentException("From [{" + remoteHost + "}] mapping to [{" + localPort + "}] error！");
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
            throw new InstrumentException(e);
        }
    }

    /**
     * 打开SSH会话，并绑定远程端口到本地的一个随机端口
     *
     * @param sshConn    SSH连接信息对象
     * @param remoteHost 远程主机
     * @param remotePort 远程端口
     * @return 映射后的本地端口
     * @throws InstrumentException 连接异常
     */
    public static int openAndBindPortToLocal(Connector sshConn, String remoteHost, int remotePort) throws InstrumentException {
        final Session session = openSession(sshConn.getHost(), sshConn.getPort(), sshConn.getUser(), sshConn.getPassword());
        if (session == null) {
            throw new InstrumentException("Error to create SSH Session！");
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
     * @since 4.0.3
     */
    public static ChannelSftp openSftp(Session session) {
        return (ChannelSftp) openChannel(session, ChannelType.SFTP);
    }

    /**
     * 创建Sftp
     *
     * @param sshHost 远程主机
     * @param sshPort 远程主机端口
     * @param sshUser 远程主机用户名
     * @param sshPass 远程主机密码
     * @return {@link Sftp}
     * @since 4.0.3
     */
    public static Sftp createSftp(String sshHost, int sshPort, String sshUser, String sshPass) {
        return new Sftp(sshHost, sshPort, sshUser, sshPass);
    }

    /**
     * 创建Sftp
     *
     * @param session SSH会话
     * @return {@link Sftp}
     * @since 4.0.5
     */
    public static Sftp createSftp(Session session) {
        return new Sftp(session);
    }

    /**
     * 打开Shell连接
     *
     * @param session Session会话
     * @return {@link ChannelShell}
     * @since 4.0.3
     */
    public static ChannelShell openShell(Session session) {
        return (ChannelShell) openChannel(session, ChannelType.SHELL);
    }

    /**
     * 打开Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @return {@link Channel}
     * @since 4.5.2
     */
    public static Channel openChannel(Session session, ChannelType channelType) {
        final Channel channel = createChannel(session, channelType);
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new InstrumentException(e);
        }
        return channel;
    }

    /**
     * 创建Channel连接
     *
     * @param session     Session会话
     * @param channelType 通道类型，可以是shell或sftp等，见{@link ChannelType}
     * @return {@link Channel}
     * @since 4.5.2
     */
    public static Channel createChannel(Session session, ChannelType channelType) {
        Channel channel;
        try {
            if (false == session.isConnected()) {
                session.connect();
            }
            channel = session.openChannel(channelType.getValue());
        } catch (JSchException e) {
            throw new InstrumentException(e);
        }
        return channel;
    }

    /**
     * 执行Shell命令
     *
     * @param session Session会话
     * @param cmd     命令
     * @param charset 发送和读取内容的编码
     * @return {@link ChannelExec}
     * @since 4.0.3
     */
    public static String exec(Session session, String cmd, Charset charset) {
        return exec(session, cmd, charset, System.err);
    }

    /**
     * 执行Shell命令
     *
     * @param session   Session会话
     * @param cmd       命令
     * @param charset   发送和读取内容的编码
     * @param errStream 错误信息输出到的位置
     * @return {@link ChannelExec}
     * @since 4.3.1
     */
    public static String exec(Session session, String cmd, Charset charset, OutputStream errStream) {
        if (null == charset) {
            charset = org.aoju.bus.core.consts.Charset.UTF_8;
        }
        ChannelExec channel = (ChannelExec) openChannel(session, ChannelType.EXEC);
        channel.setCommand(StringUtils.bytes(cmd, charset));
        channel.setInputStream(null);
        channel.setErrStream(errStream);
        InputStream in = null;
        try {
            channel.start();
            in = channel.getInputStream();
            return IoUtils.read(in, org.aoju.bus.core.consts.Charset.UTF_8);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } catch (JSchException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(in);
            close(channel);
        }
    }

    /**
     * 关闭SSH连接会话
     *
     * @param session SSH会话
     */
    public static void close(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        JschSessionPool.INSTANCE.remove(session);
    }

    /**
     * 关闭会话通道
     *
     * @param channel 会话通道
     * @since 4.0.3
     */
    public static void close(Channel channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

    /**
     * 关闭SSH连接会话
     *
     * @param key 主机，格式为user@host:port
     */
    public static void close(String key) {
        JschSessionPool.INSTANCE.close(key);
    }

    /**
     * 关闭所有SSH连接会话
     */
    public static void closeAll() {
        JschSessionPool.INSTANCE.closeAll();
    }

}
