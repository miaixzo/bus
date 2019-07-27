package org.aoju.bus.socket.protocol;

import org.aoju.bus.socket.aio.AioSession;

import java.nio.ByteBuffer;

/**
 * 消息解码器
 *
 * @param <T> 解码后的目标类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface MsgDecoder<T> {
    /**
     * 对于从Socket流中获取到的数据采用当前MsgDecoder的实现类协议进行解析。
     *
     * @param session    本次需要解码的session
     * @param readBuffer 待处理的读buffer
     * @return 本次解码成功后封装的业务消息对象, 返回null则表示解码未完成
     */
    T decode(AioSession session, ByteBuffer readBuffer);
}
