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
package org.aoju.bus.office;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.process.PumpStreamHandler;
import org.aoju.bus.office.process.StreamPumper;

import java.util.Objects;

/**
 * 重定向输出和错误流的进程的包装器类.
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public class Expense {

    private final Process process;
    private final PumpStreamHandler streamHandler;

    /**
     * 为给定的流程创建一个新的包装器.
     *
     * @param process 为其创建包装器的过程.
     */
    public Expense(final Process process) {
        super();
        Objects.requireNonNull(process, "process must not be null");
        this.process = process;
        this.streamHandler = new PumpStreamHandler(
                new StreamPumper(process.getInputStream(), (line) -> Logger.info(line)),
                new StreamPumper(process.getErrorStream(), (line) -> Logger.error(line)));
        this.streamHandler.start();
    }

    /**
     * 获取此包装器的进程.
     *
     * @return 当前这个进程.
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * 获取进程的退出代码.
     *
     * @return 进程的退出码，如果尚未终止则为空.
     */
    public Integer getExitCode() {
        try {
            final int exitValue = this.process.exitValue();
            this.streamHandler.stop();
            return exitValue;

        } catch (IllegalThreadStateException ex) {
            Logger.trace("The Office process has not yet terminated.");
            return null;
        }
    }

}
