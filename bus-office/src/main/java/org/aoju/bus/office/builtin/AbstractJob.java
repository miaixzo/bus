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
package org.aoju.bus.office.builtin;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.magic.family.DocumentFormat;
import org.aoju.bus.office.provider.AbstractSourceProvider;
import org.aoju.bus.office.provider.AbstractTargetProvider;

/**
 * 所有转换作业实现的基类.
 *
 * @author Kimi Liu
 * @version 5.6.8
 * @since JDK 1.8+
 */
public abstract class AbstractJob implements OptionalTarget {

    protected AbstractSourceProvider source;
    protected AbstractTargetProvider target;

    protected AbstractJob(
            final AbstractSourceProvider source, final AbstractTargetProvider target) {
        super();

        this.source = source;
        this.target = target;
    }

    @Override
    public ConvertJob as(final DocumentFormat format) {

        target.setDocumentFormat(format);
        return this;
    }

    @Override
    public final void execute() throws InstrumentException {
        Assert.notNull(target.getFormat(), "The target format is missing or not supported");
        doExecute();
    }

    /**
     * 执行转换并阻塞，直到转换终止.
     * 此时，源文档格式和目标文档格式都是已知且有效的.
     *
     * @throws InstrumentException 如果转换失败.
     */
    protected abstract void doExecute() throws InstrumentException;

}
