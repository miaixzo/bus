package org.aoju.bus.cache.support.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public abstract class AbstractSerializer implements BaseSerializer {

    private static final Logger logger = LoggerFactory.getLogger("com.github.jbox.serialize.BaseSerializer");

    protected abstract byte[] doSerialize(Object obj) throws Throwable;

    protected abstract Object doDeserialize(byte[] bytes) throws Throwable;

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return doSerialize(obj);
        } catch (Throwable t) {
            logger.error("{} serialize error.", this.getClass().getName(), t);
            return null;
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            return (T) doDeserialize(bytes);
        } catch (Throwable t) {
            logger.error("{} deserialize error.", this.getClass().getName(), t);
            return null;
        }
    }
}
