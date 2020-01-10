/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.builtin;

import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.office.magic.family.DocumentFormat;
import org.aoju.bus.office.magic.family.FormatRegistry;
import org.aoju.bus.office.metric.OfficeManager;
import org.aoju.bus.office.metric.TemporaryFileMaker;
import org.aoju.bus.office.provider.AbstractSourceProvider;
import org.aoju.bus.office.provider.AbstractTargetProvider;
import org.aoju.bus.office.provider.TargetFromFileProvider;
import org.aoju.bus.office.provider.TargetFromOutputStreamProvider;

import java.io.File;
import java.io.OutputStream;

/**
 * 使用尚未应用于转换器的源格式的所有转换作业实现的基类.
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public abstract class AbstractNorm implements OptionalSource {

    private static final boolean DEFAULT_CLOSE_STREAM = true;

    protected final AbstractSourceProvider source;
    protected final OfficeManager officeManager;
    protected final FormatRegistry formatRegistry;

    protected AbstractNorm(
            final AbstractSourceProvider source,
            final OfficeManager officeManager,
            final FormatRegistry formatRegistry) {
        super();

        this.source = source;
        this.officeManager = officeManager;
        this.formatRegistry = formatRegistry;
    }

    @Override
    public AbstractNorm as(final DocumentFormat format) {
        source.setDocumentFormat(format);
        return this;
    }

    @Override
    public AbstractJob to(final File target) {
        final TargetFromFileProvider specs = new TargetFromFileProvider(target);
        final DocumentFormat format =
                formatRegistry.getFormatByExtension(FileUtils.getExtension(target.getName()));
        if (format != null) {
            specs.setDocumentFormat(format);
        }

        return toInternal(specs);
    }

    @Override
    public AbstractJob to(final OutputStream target) {
        return to(target, DEFAULT_CLOSE_STREAM);
    }

    @Override
    public AbstractJob to(final OutputStream target, final boolean closeStream) {
        if (officeManager instanceof TemporaryFileMaker) {
            return toInternal(new TargetFromOutputStreamProvider(
                    target, (TemporaryFileMaker) officeManager, closeStream));
        }
        throw new IllegalStateException(
                "An office manager must implements the TemporaryFileMaker "
                        + "interface in order to be able to convert to OutputStream.");
    }

    /**
     * 配置当前转换以使用指定的规范写入结果.
     *
     * @param target 用于转换的目标规范.
     * @return 当前转换规范.
     */
    protected abstract AbstractJob to(AbstractTargetProvider target);

    private AbstractJob toInternal(final AbstractTargetProvider target) {
        return to(target);
    }

}
