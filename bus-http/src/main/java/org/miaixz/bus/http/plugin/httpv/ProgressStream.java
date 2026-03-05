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
