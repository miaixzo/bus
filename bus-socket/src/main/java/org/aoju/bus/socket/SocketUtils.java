package org.aoju.bus.socket;

import org.aoju.bus.core.lang.exception.CommonException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;

/**
 * Socket相关工具类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SocketUtils {

    /**
     * 获取远程端的地址信息，包括host和端口<br>
     * null表示channel为null或者远程主机未连接
     *
     * @param channel {@link AsynchronousSocketChannel}
     * @return 远程端的地址信息，包括host和端口，null表示channel为null或者远程主机未连接
     */
    public static SocketAddress getRemoteAddress(AsynchronousSocketChannel channel) {
        try {
            return (null == channel) ? null : channel.getRemoteAddress();
        } catch (ClosedChannelException e) {
            // Channel未打开或已关闭，返回null表示未连接
            return null;
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 远程主机是否处于连接状态<br>
     * 通过判断远程地址获取成功与否判断
     *
     * @param channel {@link AsynchronousSocketChannel}
     * @return 远程主机是否处于连接状态
     */
    public static boolean isConnected(AsynchronousSocketChannel channel) {
        return null != getRemoteAddress(channel);
    }

}
