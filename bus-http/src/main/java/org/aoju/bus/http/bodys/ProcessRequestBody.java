/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.DelegateSink;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.OnBack;
import org.aoju.bus.http.Process;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class ProcessRequestBody extends RequestBody {

    private RequestBody requestBody;
    private OnBack<Process> onProcess;
    private Executor callbackExecutor;
    private long stepBytes;
    private long step = 0;
    private Process process;
    private boolean doneCalled = false;
    private BufferSink bufferedSink;

    public ProcessRequestBody(RequestBody requestBody, OnBack<Process> onProcess, Executor callbackExecutor,
                              long contentLength, long stepBytes) {
        this.requestBody = requestBody;
        this.onProcess = onProcess;
        this.callbackExecutor = callbackExecutor;
        this.stepBytes = stepBytes;
        this.process = new Process(contentLength, 0);
    }

    @Override
    public long contentLength() {
        return process.getTotalBytes();
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = IoKit.buffer(new DelegateSink(sink) {

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    //这个方法会循环调用，byteCount 是每次调用上传的字节数。
                    super.write(source, byteCount);
                    process.addDoneBytes(byteCount);
                    if (process.notDoneOrReached(step * stepBytes)) {
                        return;
                    }
                    if (process.isDone()) {
                        if (doneCalled) {
                            return;
                        }
                        doneCalled = true;
                    }
                    step++;
                    callbackExecutor.execute(() -> {
                        onProcess.on(process);
                    });
                }

            });
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

}

