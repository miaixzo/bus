/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.sink;

import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;

/**
 * A {@code FaultHideSink} is an {@link AssignSink} that catches {@link IOException}s from the underlying delegate sink
 * and marks an internal error state, preventing further exceptions from being thrown for subsequent operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FaultHideSink extends AssignSink {

    /**
     * A flag indicating whether an error has occurred in the underlying sink. If true, subsequent write, flush, and
     * close operations will be skipped.
     */
    private boolean hasErrors;

    /**
     * Constructs a {@code FaultHideSink} with the specified delegate sink.
     *
     * @param delegate The delegate sink to which operations will be forwarded.
     */
    public FaultHideSink(Sink delegate) {
        super(delegate);
    }

    /**
     * Writes {@code byteCount} bytes from {@code source} to the delegate sink. If an {@link IOException} occurs during
     * the write, the error state is set, and the exception is handled by {@link #onException(IOException)}. If an error
     * has already occurred, the bytes are skipped from the source.
     *
     * @param source    The buffer containing the data to write.
     * @param byteCount The number of bytes to write.
     * @throws IOException If an I/O error occurs and no error has been previously recorded.
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (hasErrors) {
            source.skip(byteCount);
            return;
        }
        try {
            super.write(source, byteCount);
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    /**
     * Flushes any buffered data to the delegate sink. If an {@link IOException} occurs during the flush, the error
     * state is set, and the exception is handled by {@link #onException(IOException)}. If an error has already
     * occurred, this operation is skipped.
     *
     * @throws IOException If an I/O error occurs and no error has been previously recorded.
     */
    @Override
    public void flush() throws IOException {
        if (hasErrors)
            return;
        try {
            super.flush();
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    /**
     * Closes the delegate sink and releases any system resources associated with it. If an {@link IOException} occurs
     * during the close, the error state is set, and the exception is handled by {@link #onException(IOException)}. If
     * an error has already occurred, this operation is skipped.
     *
     * @throws IOException If an I/O error occurs and no error has been previously recorded.
     */
    @Override
    public void close() throws IOException {
        if (hasErrors)
            return;
        try {
            super.close();
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    /**
     * Hook method to handle exceptions that occur during write, flush, or close operations. Subclasses can override
     * this method to provide custom error handling logic.
     *
     * @param e The {@link IOException} that occurred.
     */
    protected void onException(IOException e) {

    }

}
