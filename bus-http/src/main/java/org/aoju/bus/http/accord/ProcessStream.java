/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.accord;

import org.aoju.bus.http.OnBack;
import org.aoju.bus.http.Process;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public class ProcessStream extends InputStream {

    private InputStream input;
    private OnBack<Process> onProcess;
    private Executor callbackExecutor;
    private long stepBytes;
    private long step = 0;
    private Process process;
    private boolean doneCalled = false;

    public ProcessStream(InputStream input, OnBack<Process> onProcess, long totalBytes, long stepBytes,
                         long doneBytes, Executor callbackExecutor) {
        this.input = input;
        this.onProcess = onProcess;
        this.stepBytes = stepBytes;
        this.callbackExecutor = callbackExecutor;
        this.process = new Process(totalBytes, doneBytes);
        this.step = doneBytes / stepBytes;
    }


    @Override
    public int read() throws IOException {
        int data = input.read();
        if (data > -1) {
            process.increaseDoneBytes();
        }
        if (process.notDoneOrReached(step * stepBytes)) {
            return data;
        }
        if (process.isDone()) {
            if (doneCalled) {
                return data;
            }
            doneCalled = true;
        }
        step++;
        callbackExecutor.execute(() -> onProcess.on(process));
        return data;
    }

}
