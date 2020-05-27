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
 ********************************************************************************/
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Expense;

/**
 * 获取office进程的退出码值.
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
public class ExitCodeRetryable extends AbstractRetryable {

    private final Expense process;
    private int exitCode;

    /**
     * 为指定的进程创建类的新实例
     *
     * @param process 要检索其退出码的进程
     */
    public ExitCodeRetryable(final Expense process) {
        super();
        this.process = process;
    }

    @Override
    protected void attempt() throws InstrumentException {
        final Integer code = process.getExitCode();
        if (code == null) {
            throw new InstrumentException();
        }
        exitCode = code.intValue();
    }

    /**
     * 进程的退出码
     *
     * @return 进程的退出值。值0表示正常终止
     */
    public int getExitCode() {
        return exitCode;
    }

}
