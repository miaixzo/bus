/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.image.nimble.opencv;

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileStreamSegment extends StreamSegment {

    private final String filePath;

    FileStreamSegment(File file, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = file.getAbsolutePath();
    }

    FileStreamSegment(RandomAccessFile fdes, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = getFilePath(fdes);
    }

    FileStreamSegment(ExtendInputImageStream stream) {
        super(stream.getSegmentPositions(), stream.getSegmentLengths(), stream.getImageDescriptor());
        this.filePath = stream.getFile().getAbsolutePath();
    }

    public static String getFilePath(RandomAccessFile file) {
        try {
            Field fpath = RandomAccessFile.class.getDeclaredField("path");
            if (null != fpath) {
                fpath.setAccessible(true);
                return (String) fpath.get(file);
            }
        } catch (Exception e) {
            Logger.error("get path from RandomAccessFile", e);
        }
        return null;
    }

    public static RandomAccessFile getRandomAccessFile(FileImageInputStream fstream) {
        try {
            Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
            if (null != fRaf) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            Logger.error("getFileDescriptor from FileImageInputStream", e);
        }
        return null;
    }

    public static RandomAccessFile getRandomAccessFile(FileImageOutputStream fstream) {
        try {
            Field fRaf = FileImageOutputStream.class.getDeclaredField("raf");
            if (null != fRaf) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            Logger.error("getFileDescriptor from FileImageOutputStream", e);
        }
        return null;
    }

    public String getFilePath() {
        return filePath;
    }

}
