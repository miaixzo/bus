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

import org.miaixz.bus.image.nimble.codec.ImageDescriptor;

/**
 * Represents the FileStreamSegment type.
 *
 * @author Kimi Liu
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
     * @param stream the stream.
     */
    FileStreamSegment(SegmentedImageStream stream) {
        super(stream.getSegmentPositions(), stream.getSegmentLengths(), stream.getImageDescriptor());
        this.filePath = stream.getFile().getAbsolutePath();
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
