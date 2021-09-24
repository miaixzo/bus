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
package org.aoju.bus.http.bodys;

import org.aoju.bus.http.Toable;
import org.aoju.bus.http.Wapper;
import org.aoju.bus.http.metric.Array;
import org.aoju.bus.http.metric.Convertor;
import org.aoju.bus.http.metric.TaskExecutor;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public abstract class AbstractBody implements Toable {

    protected TaskExecutor taskExecutor;
    protected Charset charset;

    public AbstractBody(TaskExecutor taskExecutor, Charset charset) {
        this.taskExecutor = taskExecutor;
        this.charset = charset;
    }

    @Override
    public Wapper toWapper() {
        if (null == taskExecutor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return taskExecutor.doMsgConvert((Convertor c) -> c.toMapper(toByteStream(), charset));
    }

    @Override
    public Array toArray() {
        if (null == taskExecutor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return taskExecutor.doMsgConvert((Convertor c) -> c.toArray(toByteStream(), charset));
    }

    @Override
    public <T> T toBean(Class<T> type) {
        if (null == taskExecutor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return taskExecutor.doMsgConvert((Convertor c) -> c.toBean(type, toByteStream(), charset));
    }

    @Override
    public <T> List<T> toList(Class<T> type) {
        if (null == taskExecutor) {
            throw new IllegalStateException("Task executor is null!");
        }
        return taskExecutor.doMsgConvert((Convertor c) -> c.toList(type, toByteStream(), charset));
    }

}
