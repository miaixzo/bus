/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
