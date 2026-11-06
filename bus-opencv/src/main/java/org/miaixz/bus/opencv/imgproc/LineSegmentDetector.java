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
package org.miaixz.bus.opencv.imgproc;

import org.miaixz.bus.opencv.core.Algorithm;
import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.Size;

/**
 * Detects line segments and estimates their widths, precision, and false-alarm scores.
 */
public class LineSegmentDetector extends Algorithm {

    /**
     * Wraps an existing native LineSegmentDetector handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected LineSegmentDetector(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static LineSegmentDetector __fromPtr__(long addr) {
        return new LineSegmentDetector(addr);
    }

    /**
     * Finds oriented line segments in a single-channel image and optionally returns line-width, angular-precision, and
     * false-alarm estimates.
     *
     * @param image 8-bit single-channel input image; pass a matrix view to restrict detection to a region
     * @param lines output line endpoints encoded as four-component float vectors (x1, y1, x2, y2), oriented by the
     *              image gradient
     * @param width optional output estimates of the widths of the regions supporting each line
     * @param prec  optional output angular precision estimates for the detected lines
     * @param nfa   optional logarithmic false-alarm scores, computed in advanced refinement mode; larger scores
     *              indicate stronger detections
     * @throws NullPointerException if image, lines, width, prec, nfa is null
     */
    public void detect(Mat image, Mat lines, Mat width, Mat prec, Mat nfa) {
        detect_0(nativeObj, image.nativeObj, lines.nativeObj, width.nativeObj, prec.nativeObj, nfa.nativeObj);
    }

    /**
     * Finds oriented line segments in a single-channel image and optionally returns line-width, angular-precision, and
     * false-alarm estimates.
     *
     * @param image 8-bit single-channel input image; pass a matrix view to restrict detection to a region
     * @param lines output line endpoints encoded as four-component float vectors (x1, y1, x2, y2), oriented by the
     *              image gradient
     * @param width optional output estimates of the widths of the regions supporting each line
     * @param prec  optional output angular precision estimates for the detected lines
     * @throws NullPointerException if image, lines, width, prec is null
     */
    public void detect(Mat image, Mat lines, Mat width, Mat prec) {
        detect_1(nativeObj, image.nativeObj, lines.nativeObj, width.nativeObj, prec.nativeObj);
    }

    /**
     * Finds oriented line segments in a single-channel image and optionally returns line-width, angular-precision, and
     * false-alarm estimates.
     *
     * @param image 8-bit single-channel input image; pass a matrix view to restrict detection to a region
     * @param lines output line endpoints encoded as four-component float vectors (x1, y1, x2, y2), oriented by the
     *              image gradient
     * @param width optional output estimates of the widths of the regions supporting each line
     * @throws NullPointerException if image, lines, width is null
     */
    public void detect(Mat image, Mat lines, Mat width) {
        detect_2(nativeObj, image.nativeObj, lines.nativeObj, width.nativeObj);
    }

    /**
     * Finds oriented line segments in a single-channel image and optionally returns line-width, angular-precision, and
     * false-alarm estimates.
     *
     * @param image 8-bit single-channel input image; pass a matrix view to restrict detection to a region
     * @param lines output line endpoints encoded as four-component float vectors (x1, y1, x2, y2), oriented by the
     *              image gradient
     * @throws NullPointerException if image, lines is null
     */
    public void detect(Mat image, Mat lines) {
        detect_3(nativeObj, image.nativeObj, lines.nativeObj);
    }

    /**
     * Draws the line segments on a given image.
     *
     * @param image The image, where the lines will be drawn. Should be bigger or equal to the image, where the lines
     *              were found
     * @param lines A vector of the lines that needed to be drawn
     * @throws NullPointerException if image, lines is null
     */
    public void drawSegments(Mat image, Mat lines) {
        drawSegments_0(nativeObj, image.nativeObj, lines.nativeObj);
    }

    /**
     * Draws two groups of lines in blue and red, counting the non overlapping (mismatching) pixels.
     *
     * @param size   The size of the image, where lines1 and lines2 were found
     * @param lines1 The first group of lines that needs to be drawn. It is visualized in blue color
     * @param lines2 The second group of lines. They visualized in red color
     * @param image  Optional image, where the lines will be drawn. The image should be color(3-channel) in order for
     *               lines1 and lines2 to be drawn in the above mentioned colors
     * @return the number of pixels that do not match between the two line renderings
     * @throws NullPointerException if size, lines1, lines2, image is null
     */
    public int compareSegments(Size size, Mat lines1, Mat lines2, Mat image) {
        return compareSegments_0(
                nativeObj,
                size.width,
                size.height,
                lines1.nativeObj,
                lines2.nativeObj,
                image.nativeObj);
    }

    /**
     * Draws two groups of lines in blue and red, counting the non overlapping (mismatching) pixels.
     *
     * @param size   The size of the image, where lines1 and lines2 were found
     * @param lines1 The first group of lines that needs to be drawn. It is visualized in blue color
     * @param lines2 The second group of lines. They visualized in red color
     * @return the number of pixels that do not match between the two line renderings
     * @throws NullPointerException if size, lines1, lines2 is null
     */
    public int compareSegments(Size size, Mat lines1, Mat lines2) {
        return compareSegments_1(nativeObj, size.width, size.height, lines1.nativeObj, lines2.nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#detect(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code LineSegmentDetector} instance
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit single-channel input image; pass a
     *                        matrix view to restrict detection to a region
     * @param lines_nativeObj native matrix or object handle for {@code lines}; output line endpoints encoded as
     *                        four-component float vectors (x1, y1, x2, y2), oriented by the image gradient
     * @param width_nativeObj native matrix or object handle for {@code width}; optional output estimates of the widths
     *                        of the regions supporting each line
     * @param prec_nativeObj  native matrix or object handle for {@code prec}; optional output angular precision
     *                        estimates for the detected lines
     * @param nfa_nativeObj   native matrix or object handle for {@code nfa}; optional logarithmic false-alarm scores,
     *                        computed in advanced refinement mode; larger scores indicate stronger detections
     */
    private static native void detect_0(
            long nativeObj,
            long image_nativeObj,
            long lines_nativeObj,
            long width_nativeObj,
            long prec_nativeObj,
            long nfa_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#detect(Mat,Mat,Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code LineSegmentDetector} instance
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit single-channel input image; pass a
     *                        matrix view to restrict detection to a region
     * @param lines_nativeObj native matrix or object handle for {@code lines}; output line endpoints encoded as
     *                        four-component float vectors (x1, y1, x2, y2), oriented by the image gradient
     * @param width_nativeObj native matrix or object handle for {@code width}; optional output estimates of the widths
     *                        of the regions supporting each line
     * @param prec_nativeObj  native matrix or object handle for {@code prec}; optional output angular precision
     *                        estimates for the detected lines
     */
    private static native void detect_1(
            long nativeObj,
            long image_nativeObj,
            long lines_nativeObj,
            long width_nativeObj,
            long prec_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#detect(Mat,Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code LineSegmentDetector} instance
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit single-channel input image; pass a
     *                        matrix view to restrict detection to a region
     * @param lines_nativeObj native matrix or object handle for {@code lines}; output line endpoints encoded as
     *                        four-component float vectors (x1, y1, x2, y2), oriented by the image gradient
     * @param width_nativeObj native matrix or object handle for {@code width}; optional output estimates of the widths
     *                        of the regions supporting each line
     */
    private static native void detect_2(
            long nativeObj,
            long image_nativeObj,
            long lines_nativeObj,
            long width_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#detect(Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code LineSegmentDetector} instance
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit single-channel input image; pass a
     *                        matrix view to restrict detection to a region
     * @param lines_nativeObj native matrix or object handle for {@code lines}; output line endpoints encoded as
     *                        four-component float vectors (x1, y1, x2, y2), oriented by the image gradient
     */
    private static native void detect_3(long nativeObj, long image_nativeObj, long lines_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#drawSegments(Mat,Mat)}.
     *
     * @param nativeObj       native binding handle of this {@code LineSegmentDetector} instance
     * @param image_nativeObj native matrix or object handle for {@code image}; The image, where the lines will be
     *                        drawn. Should be bigger or equal to the image, where the lines were found
     * @param lines_nativeObj native matrix or object handle for {@code lines}; A vector of the lines that needed to be
     *                        drawn
     */
    private static native void drawSegments_0(long nativeObj, long image_nativeObj, long lines_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#compareSegments(Size,Mat,Mat,Mat)}.
     *
     * @param nativeObj        native binding handle of this {@code LineSegmentDetector} instance
     * @param size_width       {@code size.width} component of The size of the image, where lines1 and lines2 were found
     * @param size_height      {@code size.height} component of The size of the image, where lines1 and lines2 were
     *                         found
     * @param lines1_nativeObj native matrix or object handle for {@code lines1}; The first group of lines that needs to
     *                         be drawn. It is visualized in blue color
     * @param lines2_nativeObj native matrix or object handle for {@code lines2}; The second group of lines. They
     *                         visualized in red color
     * @param image_nativeObj  native matrix or object handle for {@code image}; Optional image, where the lines will be
     *                         drawn. The image should be color(3-channel) in order for lines1 and lines2 to be drawn in
     *                         the above mentioned colors
     * @return the number of pixels that do not match between the two line renderings
     */
    private static native int compareSegments_0(
            long nativeObj,
            double size_width,
            double size_height,
            long lines1_nativeObj,
            long lines2_nativeObj,
            long image_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.LineSegmentDetector#compareSegments(Size,Mat,Mat)}.
     *
     * @param nativeObj        native binding handle of this {@code LineSegmentDetector} instance
     * @param size_width       {@code size.width} component of The size of the image, where lines1 and lines2 were found
     * @param size_height      {@code size.height} component of The size of the image, where lines1 and lines2 were
     *                         found
     * @param lines1_nativeObj native matrix or object handle for {@code lines1}; The first group of lines that needs to
     *                         be drawn. It is visualized in blue color
     * @param lines2_nativeObj native matrix or object handle for {@code lines2}; The second group of lines. They
     *                         visualized in red color
     * @return the number of pixels that do not match between the two line renderings
     */
    private static native int compareSegments_1(
            long nativeObj,
            double size_width,
            double size_height,
            long lines1_nativeObj,
            long lines2_nativeObj);

    /**
     * Deletes the native {@code LineSegmentDetector} binding handle. This compatibility declaration is retained in
     * addition to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
