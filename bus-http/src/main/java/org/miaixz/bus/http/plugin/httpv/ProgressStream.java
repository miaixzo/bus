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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.http.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * An {@link InputStream} decorator that monitors the progress of data being read. It wraps an existing InputStream and
 * invokes a callback with progress updates as data is consumed from the stream.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProgressStream extends InputStream {

    /**
     * The original, underlying input stream.
     */
    private final InputStream input;
    /**
     * The callback to be invoked with progress updates.
     */
    private final Callback<Progress> onProcess;
    /**
     * The executor on which the progress callback will be executed.
     */
    private final Executor callbackExecutor;
    /**
     * The minimum number of bytes that must be read before a progress update is triggered.
     */
    private final long stepBytes;
    /**
     * An internal counter to track progress steps.
     */
    private long step;
    /**
     * The object that holds the current progress state (total and transferred bytes).
     */
    private final Progress progress;
    /**
     * A flag to ensure the final "done" progress update is only sent once.
     */
    private boolean doneCalled = false;

    /**
     * Constructs a new ProgressStream.
     *
     * @param input            The actual input stream to wrap.
     * @param onProcess        The callback to receive progress updates.
     * @param totalBytes       The total number of bytes expected from the stream.
     * @param stepBytes        The interval in bytes for progress updates.
     * @param doneBytes        The initial number of bytes already considered read (for resuming downloads).
     * @param callbackExecutor The executor to run the callback on.
     */
    public ProgressStream(InputStream input, Callback<Progress> onProcess, long totalBytes, long stepBytes,
            long doneBytes, Executor callbackExecutor) {
        this.input = input;
        this.onProcess = onProcess;
        this.stepBytes = stepBytes;
        this.callbackExecutor = callbackExecutor;
        this.progress = new Progress(totalBytes, doneBytes);
        this.step = doneBytes / stepBytes;
    }

    /**
     * Reads the next byte of data from the input stream and updates the progress.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        int data = input.read();
        if (data > -1) {
            progress.increaseDoneBytes();
        }
        if (progress.notDoneOrReached(step * stepBytes)) {
            return data;
        }
        if (progress.isDone()) {
            if (doneCalled) {
                return data;
            }
            doneCalled = true;
        }
        step++;
        callbackExecutor.execute(() -> onProcess.on(progress));
        return data;
    }

}
