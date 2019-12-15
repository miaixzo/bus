package org.aoju.bus.office.provider;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.office.metric.TemporaryFileMaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * 当转换过程不再需要源文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class SourceFromInputStreamProvider extends AbstractSourceProvider
        implements SourceDocumentProvider {

    private final InputStream inputStream;
    private final boolean closeStream;
    private final TemporaryFileMaker fileMaker;

    SourceFromInputStreamProvider(
            final InputStream inputStream,
            final TemporaryFileMaker fileMaker,
            final boolean closeStream) {
        super(fileMaker.makeTemporaryFile());

        this.inputStream = inputStream;
        this.closeStream = closeStream;
        this.fileMaker = fileMaker;
    }

    @Override
    public File getFile() {
        final File tempFile =
                Optional.ofNullable(getFormat())
                        .map(format -> fileMaker.makeTemporaryFile(format.getExtension()))
                        .orElse(super.getFile());
        try {
            final FileOutputStream outputStream = new FileOutputStream(tempFile);
            outputStream.getChannel().lock();
            try {
                IoUtils.copy(inputStream, outputStream);
                return tempFile;
            } finally {
                outputStream.close();
            }
        } catch (IOException ex) {
            throw new InstrumentException("Could not write stream to file " + tempFile, ex);
        }
    }

    @Override
    public void onConsumed(final File tempFile) {
        FileUtils.delete(tempFile);
        if (closeStream) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new InstrumentException("Could not close input stream", ex);
            }
        }
    }

}
