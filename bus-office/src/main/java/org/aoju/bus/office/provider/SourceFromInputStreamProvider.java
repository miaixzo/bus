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
 * @version 5.8.6
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
