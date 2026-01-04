/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.logger.metric.slf4j;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A factory for creating {@link org.slf4j.Logger} instances. This factory detects the presence of an SLF4J binding and
 * creates loggers accordingly.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Slf4jLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code Slf4jLoggingFactory}. This constructor will throw a {@link NoClassDefFoundError} if no
     * SLF4J binding is found.
     */
    public Slf4jLoggingFactory() {
        this(true);
    }

    /**
     * Constructs a new {@code Slf4jLoggingFactory}.
     *
     * @param fail whether to throw an error if no SLF4J binding is found.
     */
    public Slf4jLoggingFactory(final boolean fail) {
        super("org.slf4j.Logger");
        exists(LoggerFactory.class);
        if (!fail) {
            return;
        }

        // Redirect System.err to capture the "no binding" message from SLF4J.
        final StringBuilder buf = new StringBuilder();
        final PrintStream err = System.err;
        System.setErr(new PrintStream(new OutputStream() {

            /**
             * Writes a single byte to the buffer. This implementation captures each character written to System.err by
             * appending it to the buffer, which allows us to check if SLF4J reported a "no binding" error.
             *
             * @param b the byte to write
             */
            @Override
            public void write(final int b) {
                buf.append((char) b);
            }
        }, true, Charset.US_ASCII));

        try {
            // Check if the underlying logger factory is a no-operation factory.
            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
                // If it is, throw an error with the captured message.
                throw new NoClassDefFoundError(buf.toString());
            } else {
                // Otherwise, print any captured output to the original System.err.
                err.print(buf);
                err.flush();
            }
        } finally {
            // Restore the original System.err.
            System.setErr(err);
        }
    }

    /**
     * Creates a logger provider for the specified name.
     *
     * @param name the name of the logger
     * @return a new {@link Provider} instance
     */
    @Override
    public Provider create(final String name) {
        return new Slf4jLoggingProvider(name);
    }

    /**
     * Creates a logger provider for the specified class.
     *
     * @param clazz the class for which to create the logger
     * @return a new {@link Provider} instance
     */
    @Override
    public Provider create(final Class<?> clazz) {
        return new Slf4jLoggingProvider(clazz);
    }

}
