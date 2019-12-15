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
 * @version 3.6.6
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
