package org.aoju.bus.office.provider;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.builtin.AbstractJob;
import org.aoju.bus.office.builtin.AbstractSpecs;
import org.aoju.bus.office.builtin.OnlineConvert;
import org.aoju.bus.office.magic.family.FormatRegistry;
import org.aoju.bus.office.metric.InstalledOfficeHolder;
import org.aoju.bus.office.metric.OfficeManager;

/**
 * 在线转换器将向LibreOffice在线服务器发送转换请求.
 * 按预期工作,它必须与在线office管理器一起使用.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OnlineProvider extends AbstractProvider {

    private OnlineProvider(
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
     * 使用默认配置创建一个新的{@link OnlineProvider}.
     * 将要使用的{@link OfficeManager}是{@link InstalledOfficeHolder}类所包含的.
     *
     * @return 带有默认配置的{@link OnlineProvider}.
     */
    public static OnlineProvider make() {
        return builder().build();
    }

    /**
     * 使用带有默认配置的指定的{@link OfficeManager}创建一个新的{@link OnlineProvider}.
     *
     * @param officeManager 转换器将使用{@link OfficeManager}转换文档.
     * @return 带有默认配置的{@link OnlineProvider}.
     */
    public static OnlineProvider make(final OfficeManager officeManager) {
        return builder().officeManager(officeManager).build();
    }

    @Override
    protected AbstractSpecs convert(
            final AbstractSourceProvider source) {
        return new Online(source);
    }

    /**
     * 用于构造{@link OnlineProvider}的生成器.
     *
     * @see OnlineProvider
     */
    public static final class Builder extends AbstractConverterBuilder<Builder> {

        private Builder() {
            super();
        }

        @Override
        public OnlineProvider build() {
            return new OnlineProvider(officeManager, formatRegistry);
        }

    }

    private class Online
            extends AbstractSpecs {

        private Online(final AbstractSourceProvider source) {
            super(source, OnlineProvider.this.officeManager, OnlineProvider.this.formatRegistry);
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
            final OnlineConvert task = new OnlineConvert(source, target);
            officeManager.execute(task);
        }
    }

}
