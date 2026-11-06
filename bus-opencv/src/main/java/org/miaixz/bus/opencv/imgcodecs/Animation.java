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
package org.miaixz.bus.opencv.imgcodecs;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.MatOfInt;
import org.miaixz.bus.opencv.core.Scalar;
import org.miaixz.bus.opencv.utils.Converters;

/**
 * Native animation data containing image frames and their timing information.
 */
public class Animation {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native Animation handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected Animation(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Returns the opaque native handle associated with this Java wrapper.
     *
     * @return the opaque native handle
     */
    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static Animation __fromPtr__(long addr) {
        return new Animation(addr);
    }

    /**
     * Creates a configured Animation value using the supplied values.
     *
     * @param loopCount An integer representing the number of times the animation should loop: 0 (default) indicates
     *                  infinite looping, meaning the animation will replay continuously. Positive values denote finite
     *                  repeat counts, allowing the animation to play a limited number of times. If a negative value or
     *                  a value beyond the maximum of 0xffff (65535) is provided, it is reset to 0 (infinite looping) to
     *                  maintain valid bounds
     * @param bgColor   A Scalar object representing the background color in BGR format: Defaults to Scalar(),
     *                  indicating an empty color (usually transparent if supported). This background color provides a
     *                  solid fill behind frames that have transparency, ensuring a consistent display appearance
     * @throws NullPointerException if bgColor is null
     */
    public Animation(int loopCount, Scalar bgColor) {
        nativeObj = Animation_0(loopCount, bgColor.val[0], bgColor.val[1], bgColor.val[2], bgColor.val[3]);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a configured Animation value using the supplied values.
     *
     * @param loopCount An integer representing the number of times the animation should loop: 0 (default) indicates
     *                  infinite looping, meaning the animation will replay continuously. Positive values denote finite
     *                  repeat counts, allowing the animation to play a limited number of times. If a negative value or
     *                  a value beyond the maximum of 0xffff (65535) is provided, it is reset to 0 (infinite looping) to
     *                  maintain valid bounds
     */
    public Animation(int loopCount) {
        nativeObj = Animation_1(loopCount);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates an empty Animation value.
     */
    public Animation() {
        nativeObj = Animation_2();
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Returns animation repeat count as interpreted by the container codec.
     *
     * @return animation repeat count as interpreted by the container codec
     */
    public int get_loop_count() {
        return get_loop_count_0(nativeObj);
    }

    /**
     * Sets animation repeat count as interpreted by the container codec.
     *
     * @param loop_count animation repeat count; the container codec determines how it is stored
     */
    public void set_loop_count(int loop_count) {
        set_loop_count_0(nativeObj, loop_count);
    }

    /**
     * Returns animation background color in BGRA component order.
     *
     * @return animation background color in BGRA component order
     */
    public Scalar get_bgcolor() {
        return new Scalar(get_bgcolor_0(nativeObj));
    }

    /**
     * Sets animation background color in BGRA component order.
     *
     * @param bgcolor bgcolor channel values
     * @throws NullPointerException if bgcolor is null
     */
    public void set_bgcolor(Scalar bgcolor) {
        set_bgcolor_0(nativeObj, bgcolor.val[0], bgcolor.val[1], bgcolor.val[2], bgcolor.val[3]);
    }

    /**
     * Returns frame display durations in milliseconds.
     *
     * @return frame display durations in milliseconds
     */
    public MatOfInt get_durations() {
        return MatOfInt.fromNativeAddr(get_durations_0(nativeObj));
    }

    /**
     * Sets frame display durations in milliseconds.
     *
     * @param durations frame display durations in milliseconds, in frame order
     */
    public void set_durations(MatOfInt durations) {
        Mat durations_mat = durations;
        set_durations_0(nativeObj, durations_mat.nativeObj);
    }

    /**
     * Returns frame matrices in display order.
     *
     * @return frame matrices in display order
     */
    public List<Mat> get_frames() {
        List<Mat> retVal = new ArrayList<Mat>();
        Mat retValMat = new Mat(get_frames_0(nativeObj));
        Converters.Mat_to_vector_Mat(retValMat, retVal);
        return retVal;
    }

    /**
     * Sets frame matrices in display order.
     *
     * @param frames animation frame matrices in display order
     */
    public void set_frames(List<Mat> frames) {
        Mat frames_mat = Converters.vector_Mat_to_Mat(frames);
        set_frames_0(nativeObj, frames_mat.nativeObj);
    }

    /**
     * Returns standalone still-image representation associated with the animation.
     *
     * @return standalone still-image representation associated with the animation
     */
    public Mat get_still_image() {
        return new Mat(get_still_image_0(nativeObj));
    }

    /**
     * Sets standalone still-image representation associated with the animation.
     *
     * @param still_image standalone image representation associated with the animation
     * @throws NullPointerException if still_image is null
     */
    public void set_still_image(Mat still_image) {
        set_still_image_0(nativeObj, still_image.nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#Animation(int,Scalar)}.
     *
     * @param loopCount    An integer representing the number of times the animation should loop: 0 (default) indicates
     *                     infinite looping, meaning the animation will replay continuously. Positive values denote
     *                     finite repeat counts, allowing the animation to play a limited number of times. If a negative
     *                     value or a value beyond the maximum of 0xffff (65535) is provided, it is reset to 0 (infinite
     *                     looping) to maintain valid bounds
     * @param bgColor_val0 {@code bgColor.val[0]} component of A Scalar object representing the background color in BGR
     *                     format: Defaults to Scalar(), indicating an empty color (usually transparent if supported).
     *                     This background color provides a solid fill behind frames that have transparency, ensuring a
     *                     consistent display appearance
     * @param bgColor_val1 {@code bgColor.val[1]} component of A Scalar object representing the background color in BGR
     *                     format: Defaults to Scalar(), indicating an empty color (usually transparent if supported).
     *                     This background color provides a solid fill behind frames that have transparency, ensuring a
     *                     consistent display appearance
     * @param bgColor_val2 {@code bgColor.val[2]} component of A Scalar object representing the background color in BGR
     *                     format: Defaults to Scalar(), indicating an empty color (usually transparent if supported).
     *                     This background color provides a solid fill behind frames that have transparency, ensuring a
     *                     consistent display appearance
     * @param bgColor_val3 {@code bgColor.val[3]} component of A Scalar object representing the background color in BGR
     *                     format: Defaults to Scalar(), indicating an empty color (usually transparent if supported).
     *                     This background color provides a solid fill behind frames that have transparency, ensuring a
     *                     consistent display appearance
     * @return a newly allocated native {@code Animation} binding handle
     */
    private static native long Animation_0(
            int loopCount,
            double bgColor_val0,
            double bgColor_val1,
            double bgColor_val2,
            double bgColor_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#Animation(int)}.
     *
     * @param loopCount An integer representing the number of times the animation should loop: 0 (default) indicates
     *                  infinite looping, meaning the animation will replay continuously. Positive values denote finite
     *                  repeat counts, allowing the animation to play a limited number of times. If a negative value or
     *                  a value beyond the maximum of 0xffff (65535) is provided, it is reset to 0 (infinite looping) to
     *                  maintain valid bounds
     * @return a newly allocated native {@code Animation} binding handle
     */
    private static native long Animation_1(int loopCount);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#Animation()}.
     *
     * @return a newly allocated native {@code Animation} binding handle
     */
    private static native long Animation_2();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#get_loop_count()}.
     *
     * @param nativeObj native binding handle of this {@code Animation} instance
     * @return animation repeat count as interpreted by the container codec
     */
    private static native int get_loop_count_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#set_loop_count(int)}.
     *
     * @param nativeObj  native binding handle of this {@code Animation} instance
     * @param loop_count animation repeat count; the container codec determines how it is stored
     */
    private static native void set_loop_count_0(long nativeObj, int loop_count);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#get_bgcolor()}.
     *
     * @param nativeObj native binding handle of this {@code Animation} instance
     * @return packed numeric fields used by the Java wrapper to construct {@code Scalar}
     */
    private static native double[] get_bgcolor_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#set_bgcolor(Scalar)}.
     *
     * @param nativeObj    native binding handle of this {@code Animation} instance
     * @param bgcolor_val0 {@code bgcolor.val[0]} component of bgcolor channel values
     * @param bgcolor_val1 {@code bgcolor.val[1]} component of bgcolor channel values
     * @param bgcolor_val2 {@code bgcolor.val[2]} component of bgcolor channel values
     * @param bgcolor_val3 {@code bgcolor.val[3]} component of bgcolor channel values
     */
    private static native void set_bgcolor_0(
            long nativeObj,
            double bgcolor_val0,
            double bgcolor_val1,
            double bgcolor_val2,
            double bgcolor_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#get_durations()}.
     *
     * @param nativeObj native binding handle of this {@code Animation} instance
     * @return native binding handle for frame display durations in milliseconds
     */
    private static native long get_durations_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#set_durations(MatOfInt)}.
     *
     * @param nativeObj               native binding handle of this {@code Animation} instance
     * @param durations_mat_nativeObj native matrix or object handle for {@code durations_mat}; frame display durations
     *                                in milliseconds, in frame order
     */
    private static native void set_durations_0(long nativeObj, long durations_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#get_frames()}.
     *
     * @param nativeObj native binding handle of this {@code Animation} instance
     * @return native binding handle for frame matrices in display order
     */
    private static native long get_frames_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#set_frames(List<Mat>)}.
     *
     * @param nativeObj            native binding handle of this {@code Animation} instance
     * @param frames_mat_nativeObj native matrix or object handle for {@code frames_mat}; animation frame matrices in
     *                             display order
     */
    private static native void set_frames_0(long nativeObj, long frames_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#get_still_image()}.
     *
     * @param nativeObj native binding handle of this {@code Animation} instance
     * @return native binding handle for standalone still-image representation associated with the animation
     */
    private static native long get_still_image_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgcodecs.Animation#set_still_image(Mat)}.
     *
     * @param nativeObj             native binding handle of this {@code Animation} instance
     * @param still_image_nativeObj native matrix or object handle for {@code still_image}; standalone image
     *                              representation associated with the animation
     */
    private static native void set_still_image_0(long nativeObj, long still_image_nativeObj);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code Animation} instance
     */
    private static native void delete(long nativeObj);

}
