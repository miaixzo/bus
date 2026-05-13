/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.opencv;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;
import org.miaixz.bus.logger.Logger;

/**
 * Represents the FileStreamSegment type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FileStreamSegment extends StreamSegment {

    /**
     * The file path value.
     */
    private final String filePath;

    /**
     * Creates a new instance.
     *
     * @param file            the file.
     * @param startPos        the start pos.
     * @param length          the length.
     * @param imageDescriptor the image descriptor.
     */
    FileStreamSegment(File file, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = file.getAbsolutePath();
    }

    /**
     * Creates a new instance.
     *
     * @param fdes            the fdes.
     * @param startPos        the start pos.
     * @param length          the length.
     * @param imageDescriptor the image descriptor.
     */
    FileStreamSegment(RandomAccessFile fdes, long[] startPos, long[] length, ImageDescriptor imageDescriptor) {
        super(startPos, length, imageDescriptor);
        this.filePath = getFilePath(fdes);
    }

    /**
     * Creates a new instance.
     *
     * @param stream the stream.
     */
    FileStreamSegment(SegmentedImageStream stream) {
        super(stream.getSegmentPositions(), stream.getSegmentLengths(), stream.getImageDescriptor());
        this.filePath = stream.getFile().getAbsolutePath();
    }

    /**
     * Gets the file path.
     *
     * @param file the file.
     * @return the file path.
     */
    public static String getFilePath(RandomAccessFile file) {
        try {
            Field fpath = RandomAccessFile.class.getDeclaredField("path");
            if (fpath != null) {
                fpath.setAccessible(true);
                return (String) fpath.get(file);
            }
        } catch (Exception e) {
            Logger.error(false, "Image", "get path from RandomAccessFile", e);
        }
        return null;
    }

    /**
     * Gets the random access file.
     *
     * @param fstream the fstream.
     * @return the random access file.
     */
    public static RandomAccessFile getRandomAccessFile(FileImageInputStream fstream) {
        try {
            Field fRaf = FileImageInputStream.class.getDeclaredField("raf");
            if (fRaf != null) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            Logger.error(false, "Image", "getFileDescriptor from FileImageInputStream", e);
        }
        return null;
    }

    /**
     * Gets the random access file.
     *
     * @param fstream the fstream.
     * @return the random access file.
     */
    public static RandomAccessFile getRandomAccessFile(FileImageOutputStream fstream) {
        try {
            Field fRaf = FileImageOutputStream.class.getDeclaredField("raf");
            if (fRaf != null) {
                fRaf.setAccessible(true);
                return (RandomAccessFile) fRaf.get(fstream);
            }
        } catch (Exception e) {
            Logger.error(false, "Image", "getFileDescriptor from FileImageOutputStream", e);
        }
        return null;
    }

    /**
     * Gets the file path.
     *
     * @return the file path.
     */
    public String getFilePath() {
        return filePath;
    }

}
