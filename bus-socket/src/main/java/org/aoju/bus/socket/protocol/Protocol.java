package org.aoju.bus.socket.protocol;

/**
 * 协议接口<br>
 * 通过实现此接口完成消息的编码和解码
 *
 * <p>
 * 所有Socket使用相同协议对象，类成员变量和对象成员变量易造成并发读写问题。
 * </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Protocol<T> extends MsgEncoder<T>, MsgDecoder<T> {

}
