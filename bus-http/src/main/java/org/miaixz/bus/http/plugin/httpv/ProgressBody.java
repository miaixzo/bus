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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.AssignSink;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.bodys.RequestBody;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * A {@link RequestBody} decorator that monitors the progress of an upload. It wraps an existing RequestBody and invokes
 * a callback with progress updates as data is being written to the network.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProgressBody extends RequestBody {

    /**
     * The original, underlying request body.
     */
    private final RequestBody requestBody;
    /**
     * The callback to be invoked with progress updates.
     */
    private final Callback<Progress> onProcess;
    /**
     * The executor on which the progress callback will be executed.
     */
    private final Executor callbackExecutor;
    /**
     * The minimum number of bytes that must be transferred before a progress update is triggered.
     */
    private final long stepBytes;
    /**
     * An internal counter to track progress steps.
     */
    private long step = 0;
    /**
     * The object that holds the current progress state (total and transferred bytes).
     */
    private final Progress progress;
    /**
     * A flag to ensure the final "done" progress update is only sent once.
     */
    private boolean doneCalled = false;
    /**
     * The buffered sink that performs the progress tracking.
     */
    private BufferSink bufferedSink;

    /**
     * Constructs a new ProgressBody.
     *
     * @param requestBody      The actual request body to wrap.
     * @param onProcess        The callback to receive progress updates.
     * @param callbackExecutor The executor to run the callback on.
     * @param contentLength    The total length of the request body.
     * @param stepBytes        The interval in bytes for progress updates.
     */
    public ProgressBody(RequestBody requestBody, Callback<Progress> onProcess, Executor callbackExecutor,
            long contentLength, long stepBytes) {
        this.requestBody = requestBody;
        this.onProcess = onProcess;
        this.callbackExecutor = callbackExecutor;
        this.stepBytes = stepBytes;
        this.progress = new Progress(contentLength, 0);
    }

    @Override
    public long contentLength() {
        return progress.getTotalBytes();
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        if (null == bufferedSink) {
            bufferedSink = IoKit.buffer(new AssignSink(sink) {

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    // This method is called repeatedly; byteCount is the number of bytes uploaded in each call.
                    super.write(source, byteCount);
                    progress.addDoneBytes(byteCount);
                    if (progress.notDoneOrReached(step * stepBytes)) {
                        return;
                    }
                    if (progress.isDone()) {
                        if (doneCalled) {
                            return;
                        }
                        doneCalled = true;
                    }
                    step++;
                    callbackExecutor.execute(() -> onProcess.on(progress));
                }

            });
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

}
