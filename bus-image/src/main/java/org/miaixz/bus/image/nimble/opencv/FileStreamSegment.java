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

    FileStreamSegment(SegmentedImageStream stream) {
        super(stream.getSegmentPositions(), stream.getSegmentLengths(), stream.getImageDescriptor());
        this.filePath = stream.getFile().getAbsolutePath();
    }

    public static String getFilePath(RandomAccessFile file) {
        try {
            Field fpath = RandomAccessFile.class.getDeclaredField("path");
            if (fpath != null) {
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
            if (fRaf != null) {
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
            if (fRaf != null) {
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
