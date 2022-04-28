/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.process;

import org.aoju.bus.core.lang.Charset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Objects;

/**
 * 从输入流中读取所有行.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamPumper extends Thread {

    private final InputStream stream;
    private final LineConsumer consumer;

    /**
     * 为指定的流创建一个新的pumper.
     *
     * @param stream   要从中读取的输入流.
     * @param consumer 从输入流读取行的使用者.
     */
    public StreamPumper(final InputStream stream, final LineConsumer consumer) {
        super();

        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(stream, "consumer must not be null");

        this.stream = stream;
        this.consumer = consumer;
        this.setDaemon(true);
    }

    /**
     * 获取从输入流读取的行的使用者.
     *
     * @return The consumer.
     */
    public LineConsumer getConsumer() {
        return consumer;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader =
                     new BufferedReader(Channels.newReader(Channels.newChannel(stream), Charset.DEFAULT_UTF_8))) {
            String line;
            while (null != (line = bufferedReader.readLine())) {
                consumer.consume(line);
            }
        } catch (IOException ex) {
            // ignore errors
        }
    }

    /**
     * 提供一个函数来使用从流中读取的行.
     */
    @FunctionalInterface
    public interface LineConsumer {

        /**
         * 使用从输入流读取的行.
         *
         * @param line 读取行信息.
         */
        void consume(String line);
    }

}
