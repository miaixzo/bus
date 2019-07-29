package org.aoju.bus.socket.aio;

import org.aoju.bus.core.lang.exception.CommonException;

import java.nio.channels.CompletionHandler;

/**
 * 数据读取完成回调，调用Session中相应方法处理消息，单例使用
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ReadHandler implements CompletionHandler<Integer, AioSession> {

    @Override
    public void completed(Integer result, AioSession session) {
        session.callbackRead();
    }

    @Override
    public void failed(Throwable exc, AioSession session) {
        throw new CommonException(exc);
    }

}
