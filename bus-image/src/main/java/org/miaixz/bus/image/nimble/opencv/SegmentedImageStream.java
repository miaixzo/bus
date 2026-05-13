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
 * Represents the SegmentedImageStream type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SegmentedImageStream {

    /**
     * The file value.
     */
    private final File file;

    /**
     * The segment positions value.
     */
    private final long[] segmentPositions;

    /**
     * The segment lengths value.
     */
    private final long[] segmentLengths;

    /**
     * The image descriptor value.
     */
    private final ImageDescriptor imageDescriptor;

    /**
     * Creates a new instance.
     *
     * @param file             the file.
     * @param segmentPositions the segment positions.
     * @param segmentLengths   the segment lengths.
     * @param imageDescriptor  the image descriptor.
     */
    public SegmentedImageStream(File file, long[] segmentPositions, int[] segmentLengths,
            ImageDescriptor imageDescriptor) {
        this.file = file;
        this.segmentPositions = segmentPositions;
        this.segmentLengths = segmentLengths == null ? null : getDoubleArray(segmentLengths);
        this.imageDescriptor = imageDescriptor;
    }

    /**
     * Gets the double array.
     *
     * @param array the array.
     * @return the double array.
     */
    public static double[] getDoubleArray(long[] array) {
        double[] a = new double[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }

    /**
     * Gets the double array.
     *
     * @param array the array.
     * @return the double array.
     */
    public static long[] getDoubleArray(int[] array) {
        long[] a = new long[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }

    /**
     * Gets the segment positions.
     *
     * @return the segment positions.
     */
    public long[] getSegmentPositions() {
        return segmentPositions;
    }

    /**
     * Gets the segment lengths.
     *
     * @return the segment lengths.
     */
    public long[] getSegmentLengths() {
        return segmentLengths;
    }

    /**
     * Gets the file.
     *
     * @return the file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the image descriptor.
     *
     * @return the image descriptor.
     */
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

}
