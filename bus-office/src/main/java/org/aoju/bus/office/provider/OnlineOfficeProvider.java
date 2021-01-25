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

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.builtin.AbstractJob;
import org.aoju.bus.office.builtin.AbstractNorm;
import org.aoju.bus.office.builtin.OnlineMadeInOffice;
import org.aoju.bus.office.magic.family.FormatRegistry;
import org.aoju.bus.office.metric.InstalledOfficeHolder;
import org.aoju.bus.office.metric.OfficeManager;

/**
 * 在线转换器将向LibreOffice在线服务器发送转换请求.
 * 按预期工作,它必须与在线office管理器一起使用.
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
public class OnlineOfficeProvider extends AbstractProvider {

    private OnlineOfficeProvider(
            final OfficeManager officeManager, final FormatRegistry formatRegistry) {
        super(officeManager, formatRegistry);
    }

    /**
     * 创建一个新的生成器实例.
     *
     * @return 新的生成器实例.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 使用默认配置创建一个新的{@link OnlineOfficeProvider}.
     * 将要使用的{@link OfficeManager} 是 {@link InstalledOfficeHolder}类所包含的.
     *
     * @return 带有默认配置的 {@link OnlineOfficeProvider}.
     */
    public static OnlineOfficeProvider make() {
        return builder().build();
    }

    /**
     * 使用带有默认配置的指定的{@link OfficeManager}创建一个新的{@link OnlineOfficeProvider}.
     *
     * @param officeManager 转换器将使用{@link OfficeManager}转换文档.
     * @return 带有默认配置的 {@link OnlineOfficeProvider}.
     */
    public static OnlineOfficeProvider make(final OfficeManager officeManager) {
        return builder().officeManager(officeManager).build();
    }

    @Override
    protected AbstractNorm convert(
            final AbstractSourceProvider source) {
        return new Online(source);
    }

    /**
     * 用于构造{@link OnlineOfficeProvider}的生成器.
     *
     * @see OnlineOfficeProvider
     */
    public static final class Builder extends AbstractConverterBuilder<Builder> {

        private Builder() {
            super();
        }

        @Override
        public OnlineOfficeProvider build() {
            return new OnlineOfficeProvider(officeManager, formatRegistry);
        }

    }

    private class Online
            extends AbstractNorm {

        private Online(final AbstractSourceProvider source) {
            super(source, OnlineOfficeProvider.this.officeManager, OnlineOfficeProvider.this.formatRegistry);
        }

        @Override
        protected AbstractJob to(final AbstractTargetProvider target) {
            return new OnlineJob(source, target);
        }
    }

    private class OnlineJob extends AbstractJob {

        private OnlineJob(
                final AbstractSourceProvider source,
                final AbstractTargetProvider target) {
            super(source, target);
        }

        @Override
        public void doExecute() throws InstrumentException {
            final OnlineMadeInOffice task = new OnlineMadeInOffice(source, target);
            officeManager.execute(task);
        }

    }

}
