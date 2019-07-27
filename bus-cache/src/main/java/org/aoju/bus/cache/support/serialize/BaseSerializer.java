package org.aoju.bus.cache.support.serialize;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BaseSerializer {

    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] bytes);
}
