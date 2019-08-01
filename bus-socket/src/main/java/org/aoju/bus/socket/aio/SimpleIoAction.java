package org.aoju.bus.socket.aio;

import java.nio.ByteBuffer;

/**
 * 简易IO信息处理类<br>
 * 简单实现了accept和failed事件
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class SimpleIoAction implements IoAction<ByteBuffer> {

    @Override
    public void accept(AioSession session) {
    }

    @Override
    public void failed(Throwable exc, AioSession session) {

    }

}
