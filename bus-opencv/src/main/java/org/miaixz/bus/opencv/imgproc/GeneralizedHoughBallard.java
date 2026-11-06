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

/**
 * Generalized Hough detector using the Ballard template representation.
 */
public class GeneralizedHoughBallard extends GeneralizedHough {

    /**
     * Wraps an existing native GeneralizedHoughBallard handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected GeneralizedHoughBallard(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static GeneralizedHoughBallard __fromPtr__(long addr) {
        return new GeneralizedHoughBallard(addr);
    }

    /**
     * Sets number of orientation quantization levels.
     *
     * @param levels number of orientation quantization levels
     */
    public void setLevels(int levels) {
        setLevels_0(nativeObj, levels);
    }

    /**
     * Returns number of orientation quantization levels.
     *
     * @return number of orientation quantization levels
     */
    public int getLevels() {
        return getLevels_0(nativeObj);
    }

    /**
     * Sets minimum accumulator vote count for accepting a detection.
     *
     * @param votesThreshold minimum accumulator vote count for accepting a detection
     */
    public void setVotesThreshold(int votesThreshold) {
        setVotesThreshold_0(nativeObj, votesThreshold);
    }

    /**
     * Returns minimum accumulator vote count for accepting a detection.
     *
     * @return minimum accumulator vote count for accepting a detection
     */
    public int getVotesThreshold() {
        return getVotesThreshold_0(nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughBallard#setLevels(int)}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughBallard} instance
     * @param levels    number of orientation quantization levels
     */
    private static native void setLevels_0(long nativeObj, int levels);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughBallard#getLevels()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughBallard} instance
     * @return number of orientation quantization levels
     */
    private static native int getLevels_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughBallard#setVotesThreshold(int)}.
     *
     * @param nativeObj      native binding handle of this {@code GeneralizedHoughBallard} instance
     * @param votesThreshold minimum accumulator vote count for accepting a detection
     */
    private static native void setVotesThreshold_0(long nativeObj, int votesThreshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.GeneralizedHoughBallard#getVotesThreshold()}.
     *
     * @param nativeObj native binding handle of this {@code GeneralizedHoughBallard} instance
     * @return minimum accumulator vote count for accepting a detection
     */
    private static native int getVotesThreshold_0(long nativeObj);

    /**
     * Deletes the native {@code GeneralizedHoughBallard} binding handle. This compatibility declaration is retained in
     * addition to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
