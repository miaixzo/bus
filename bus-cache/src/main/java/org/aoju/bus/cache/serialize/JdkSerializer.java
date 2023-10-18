/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.cache.serialize;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.logger.Logger;

import java.io.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkSerializer extends AbstractSerializer {

    private static void serialize(Serializable object, OutputStream outputStream) {
        if (null == outputStream) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;

            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(object);
            } catch (IOException e) {
                throw new InternalException(e);
            } finally {
                try {
                    if (null != out) {
                        out.close();
                    }
                } catch (IOException var10) {

                }
            }
        }
    }

    private static Object deserialize(InputStream inputStream) {
        if (null == inputStream) {
            throw new IllegalArgumentException("The InputStream must not be null");
        } else {
            ObjectInputStream in = null;

            Object result;
            try {
                in = new ObjectInputStream(inputStream);
                result = in.readObject();
            } catch (ClassCastException | IOException | ClassNotFoundException ce) {
                throw new InternalException(ce);
            } finally {
                try {
                    if (null != in) {
                        in.close();
                    }
                } catch (IOException e) {
                    Logger.error(e, "close stream failed when deserialize error: ", e.getMessage());
                }
            }
            return result;
        }
    }

    @Override
    protected byte[] doSerialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Normal._512);
        serialize((Serializable) object, baos);
        return baos.toByteArray();
    }

    @Override
    protected Object doDeserialize(byte[] bytes) {
        if (null == bytes) {
            throw new IllegalArgumentException("The byte[] must not be null");
        } else {
            return deserialize(new ByteArrayInputStream(bytes));
        }
    }

}
