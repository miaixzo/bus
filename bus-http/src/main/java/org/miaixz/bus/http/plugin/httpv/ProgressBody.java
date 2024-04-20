/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.AssignSink;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.bodys.RequestBody;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProgressBody extends RequestBody {

    private RequestBody requestBody;
    private Callback<Progress> onProcess;
    private Executor callbackExecutor;
    private long stepBytes;
    private long step = 0;
    private Progress progress;
    private boolean doneCalled = false;
    private BufferSink bufferedSink;

    public ProgressBody(RequestBody requestBody, Callback<Progress> onProcess, Executor callbackExecutor,
                        long contentLength, long stepBytes) {
        this.requestBody = requestBody;
        this.onProcess = onProcess;
        this.callbackExecutor = callbackExecutor;
        this.stepBytes = stepBytes;
        this.progress = new Progress(contentLength, 0);
    }

    @Override
    public long length() {
        return progress.getTotalBytes();
    }

    @Override
    public MediaType mediaType() {
        return requestBody.mediaType();
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        if (null == bufferedSink) {
            bufferedSink = IoKit.buffer(new AssignSink(sink) {

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    //这个方法会循环调用，byteCount 是每次调用上传的字节数。
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
                    callbackExecutor.execute(() -> {
                        onProcess.on(progress);
                    });
                }

            });
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

}

