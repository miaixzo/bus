/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.support.serialize;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.logger.Logger;

import java.io.*;

/**
 * @author Kimi Liu
 * @version 3.5.2
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
