package org.aoju.bus.office.provider;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.office.metric.TemporaryFileMaker;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * 当转换过程不再需要目标文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class TargetFromOutputStreamProvider extends AbstractTargetProvider
        implements TargetDocumentProvider {

    private final OutputStream outputStream;
    private final boolean closeStream;
    private final TemporaryFileMaker fileMaker;

    public TargetFromOutputStreamProvider(
            final OutputStream outputStream,
            final TemporaryFileMaker fileMaker,
            final boolean closeStream) {
        super(fileMaker.makeTemporaryFile());

        Assert.notNull(outputStream, "The outputStream is null");
        this.outputStream = outputStream;
        this.closeStream = closeStream;
        this.fileMaker = fileMaker;
    }

    @Override
    public File getFile() {
        return Optional.ofNullable(getFormat())
                .map(format -> fileMaker.makeTemporaryFile(format.getExtension()))
                .orElse(super.getFile());
    }

    @Override
    public void onComplete(final File tempFile) {
        try {
            FileUtils.copyFile(tempFile, outputStream);
            if (closeStream) {
                outputStream.close();
            }

        } catch (IOException ex) {
            throw new InstrumentException("Could not write file '" + tempFile + "' to stream", ex);
        } finally {
            FileUtils.delete(tempFile);
        }
    }

    @Override
    public void onFailure(final File tempFile, final Exception exception) {
        FileUtils.delete(tempFile);
    }

}
