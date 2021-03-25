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

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.office.metric.TemporaryFileMaker;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * 当转换过程不再需要目标文件时，提供应用行为的接口.
 *
 * @author Kimi Liu
 * @version 6.2.2
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
            FileKit.copyFile(tempFile, outputStream);
            if (closeStream) {
                outputStream.close();
            }

        } catch (IOException ex) {
            throw new InstrumentException("Could not write file '" + tempFile + "' to stream", ex);
        } finally {
            FileKit.delete(tempFile);
        }
    }

    @Override
    public void onFailure(final File tempFile, final Exception exception) {
        FileKit.delete(tempFile);
    }

}
