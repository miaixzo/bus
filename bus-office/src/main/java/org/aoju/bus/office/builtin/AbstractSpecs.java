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
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractSpecs implements OptionalSource {

    private static final boolean DEFAULT_CLOSE_STREAM = true;

    protected final AbstractSourceProvider source;
    protected final OfficeManager officeManager;
    protected final FormatRegistry formatRegistry;

    protected AbstractSpecs(
            final AbstractSourceProvider source,
            final OfficeManager officeManager,
            final FormatRegistry formatRegistry) {
        super();

        this.source = source;
        this.officeManager = officeManager;
        this.formatRegistry = formatRegistry;
    }

    @Override
    public AbstractSpecs as(final DocumentFormat format) {
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
     * Configures the current conversion to write the result using the specified specifications.
     *
     * @param target The target specifications to use for the conversion.
     * @return The current conversion specification.
     */
    protected abstract AbstractJob to(AbstractTargetProvider target);

    private AbstractJob toInternal(final AbstractTargetProvider target) {
        return to(target);
    }

}
