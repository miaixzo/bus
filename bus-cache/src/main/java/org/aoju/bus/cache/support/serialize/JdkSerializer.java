package org.aoju.bus.cache.support.serialize;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.logger.Logger;

import java.io.*;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JdkSerializer extends AbstractSerializer {

    private static void serialize(Serializable obj, OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;

            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(obj);
            } catch (IOException e) {
                throw new CommonException(e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException var10) {

                }
            }
        }
    }

    private static Object deserialize(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        } else {
            ObjectInputStream in = null;

            Object result;
            try {
                in = new ObjectInputStream(inputStream);
                result = in.readObject();
            } catch (ClassCastException | IOException | ClassNotFoundException ce) {
                throw new CommonException(ce);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    // close stream error when deserialize
                    // ignore
                    Logger.error(e, "close stream failed when deserialize error: ", e.getMessage());
                }
            }
            return result;
        }
    }

    @Override
    protected byte[] doSerialize(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize((Serializable) obj, baos);
        return baos.toByteArray();
    }

    @Override
    protected Object doDeserialize(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        } else {
            return deserialize(new ByteArrayInputStream(bytes));
        }
    }

}
