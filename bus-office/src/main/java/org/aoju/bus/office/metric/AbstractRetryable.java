/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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

/**
 * 该对象将尝试执行任务，直到任务成功或达到特定超时为止.
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
public abstract class AbstractRetryable {

    /**
     * 初始化类的新实例.
     */
    protected AbstractRetryable() {
        super();
    }

    /**
     * 尝试执行任务一次.
     *
     * @throws Exception 如果发生错误.
     */
    protected abstract void attempt() throws Exception;

    /**
     * 在没有启动延迟的情况下执行任务.
     *
     * @param interval 每个任务执行尝试之间的间隔.
     * @param timeout  超时之后，我们将不再尝试再次执行任务.
     * @throws InstrumentException 如果超时了.
     * @throws Exception           对于所有其他错误条件.
     */
    public void execute(final long interval, final long timeout) throws Exception {
        final long start = System.currentTimeMillis();
        while (true) {
            try {
                attempt();
                return;
            } catch (InstrumentException temporaryEx) {
                if (System.currentTimeMillis() - start < timeout) {
                    Thread.sleep(interval);
                } else {
                    throw new InstrumentException(temporaryEx.getCause());
                }
            }
        }
    }

}
