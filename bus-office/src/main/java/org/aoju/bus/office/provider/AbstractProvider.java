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
package org.aoju.bus.office.provider;

import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Provider;
import org.aoju.bus.office.builtin.AbstractNorm;
import org.aoju.bus.office.builtin.OptionalSource;
import org.aoju.bus.office.magic.family.DefaultFormatRegistry;
import org.aoju.bus.office.magic.family.DocumentFormat;
import org.aoju.bus.office.magic.family.FormatRegistry;
import org.aoju.bus.office.metric.InstalledOfficeHolder;
import org.aoju.bus.office.metric.OfficeManager;
import org.aoju.bus.office.metric.TemporaryFileMaker;

import java.io.File;
import java.io.InputStream;

/**
 * 所有文档转换器实现的基类.
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public abstract class AbstractProvider implements Provider {

    protected final OfficeManager officeManager;
    protected final FormatRegistry formatRegistry;

    protected AbstractProvider(final OfficeManager officeManager,
                               final FormatRegistry formatRegistry) {
        super();

        OfficeManager manager = officeManager;
        if (null == manager) {
            manager = InstalledOfficeHolder.getInstance();
            if (null == manager) {
                throw new IllegalStateException("An office manager is required in order to build a converter.");
            }
        }
        this.officeManager = manager;
        this.formatRegistry = null == formatRegistry ? DefaultFormatRegistry.getInstance() : formatRegistry;
    }

    @Override
    public OptionalSource convert(final File source) {
        final SourceFromFileProvider specs = new SourceFromFileProvider(source);
        final DocumentFormat format =
                formatRegistry.getFormatBySuffix(FileKit.getSuffix(source.getName()));
        if (null != format) {
            specs.setDocumentFormat(format);
        }
        return convert(specs);
    }

    @Override
    public OptionalSource convert(final InputStream source) {
        return convert(source, Builder.DEFAULT_CLOSE_STREAM);
    }

    @Override
    public OptionalSource convert(final InputStream source,
                                  final boolean closeStream) {

        if (officeManager instanceof TemporaryFileMaker) {
            return convert(new SourceFromInputStreamProvider(
                    source,
                    (TemporaryFileMaker) officeManager,
                    closeStream));
        }
        throw new IllegalStateException("An office manager must implements the " +
                "TemporaryFileMaker interface in order to be able to convert InputStream");
    }

    /**
     * 使用给定的规范转换源文档.
     *
     * @param source 转换输入作为文档规范.
     * @return 当前转换规范.
     */
    protected abstract AbstractNorm convert(AbstractSourceProvider source);

    @Override
    public FormatRegistry getFormatRegistry() {
        return formatRegistry;
    }

    /**
     * 构造{@link AbstractProvider}的构造器.
     *
     * @see AbstractProvider
     */
    public abstract static class AbstractConverterBuilder<B extends AbstractConverterBuilder<B>> {

        protected OfficeManager officeManager;
        protected FormatRegistry formatRegistry;

        protected AbstractConverterBuilder() {
            super();
        }

        /**
         * 指定转换器将用于执行office任务的{@link OfficeManager}.
         *
         * @param manager 此转换器将使用的office管理器.
         * @return 当前实例信息.
         */
        public B officeManager(final OfficeManager manager) {
            this.officeManager = manager;
            return (B) this;
        }

        /**
         * 指定{@link FormatRegistry}，其中包含此转换器将支持的文档格式.
         *
         * @param registry 包含支持格式的注册表.
         * @return 当前实例信息.
         */
        public B formatRegistry(final FormatRegistry registry) {
            this.formatRegistry = registry;
            return (B) this;
        }

        /**
         * 指定此生成器指定的转换器.
         *
         * @return 此生成器指定的转换器.
         */
        protected abstract AbstractProvider build();
    }

}
