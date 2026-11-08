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
package org.miaixz.bus.opencv.core;

/**
 * A native timer that accumulates elapsed ticks across measured intervals.
 */
public class TickMeter {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native TickMeter handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected TickMeter(long addr) {
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
    public static TickMeter __fromPtr__(long addr) {
        return new TickMeter(addr);
    }

    /**
     * Creates an empty TickMeter value.
     */
    public TickMeter() {
        nativeObj = TickMeter_0();
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Starts timing an interval.
     */
    public void start() {
        start_0(nativeObj);
    }

    /**
     * Stops the current timing interval and adds its duration to the accumulated measurement.
     */
    public void stop() {
        stop_0(nativeObj);
    }

    /**
     * Returns accumulated elapsed timer ticks.
     *
     * @return accumulated elapsed timer ticks
     */
    public long getTimeTicks() {
        return getTimeTicks_0(nativeObj);
    }

    /**
     * Returns accumulated elapsed time in microseconds.
     *
     * @return accumulated elapsed time in microseconds
     */
    public double getTimeMicro() {
        return getTimeMicro_0(nativeObj);
    }

    /**
     * Returns accumulated elapsed time in milliseconds.
     *
     * @return accumulated elapsed time in milliseconds
     */
    public double getTimeMilli() {
        return getTimeMilli_0(nativeObj);
    }

    /**
     * Returns accumulated elapsed time in seconds.
     *
     * @return accumulated elapsed time in seconds
     */
    public double getTimeSec() {
        return getTimeSec_0(nativeObj);
    }

    /**
     * Returns elapsed timer ticks of the most recent completed interval.
     *
     * @return elapsed timer ticks of the most recent completed interval
     */
    public long getLastTimeTicks() {
        return getLastTimeTicks_0(nativeObj);
    }

    /**
     * Returns elapsed microseconds of the most recent completed interval.
     *
     * @return elapsed microseconds of the most recent completed interval
     */
    public double getLastTimeMicro() {
        return getLastTimeMicro_0(nativeObj);
    }

    /**
     * Returns elapsed milliseconds of the most recent completed interval.
     *
     * @return elapsed milliseconds of the most recent completed interval
     */
    public double getLastTimeMilli() {
        return getLastTimeMilli_0(nativeObj);
    }

    /**
     * Returns elapsed seconds of the most recent completed interval.
     *
     * @return elapsed seconds of the most recent completed interval
     */
    public double getLastTimeSec() {
        return getLastTimeSec_0(nativeObj);
    }

    /**
     * Returns number of completed timing intervals.
     *
     * @return number of completed timing intervals
     */
    public long getCounter() {
        return getCounter_0(nativeObj);
    }

    /**
     * Returns completed interval count divided by accumulated seconds; zero when no time is accumulated.
     *
     * @return completed interval count divided by accumulated seconds; zero when no time is accumulated
     */
    public double getFPS() {
        return getFPS_0(nativeObj);
    }

    /**
     * Returns average seconds per completed interval; zero when no interval is completed.
     *
     * @return average seconds per completed interval; zero when no interval is completed
     */
    public double getAvgTimeSec() {
        return getAvgTimeSec_0(nativeObj);
    }

    /**
     * Returns average milliseconds per completed interval; zero when no interval is completed.
     *
     * @return average milliseconds per completed interval; zero when no interval is completed
     */
    public double getAvgTimeMilli() {
        return getAvgTimeMilli_0(nativeObj);
    }

    /**
     * Resets the accumulated timer measurements.
     */
    public void reset() {
        reset_0(nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#TickMeter()}.
     *
     * @return a newly allocated native {@code TickMeter} binding handle
     */
    private static native long TickMeter_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#start()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     */
    private static native void start_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#stop()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     */
    private static native void stop_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getTimeTicks()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return accumulated elapsed timer ticks
     */
    private static native long getTimeTicks_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getTimeMicro()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return accumulated elapsed time in microseconds
     */
    private static native double getTimeMicro_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getTimeMilli()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return accumulated elapsed time in milliseconds
     */
    private static native double getTimeMilli_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getTimeSec()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return accumulated elapsed time in seconds
     */
    private static native double getTimeSec_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getLastTimeTicks()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return elapsed timer ticks of the most recent completed interval
     */
    private static native long getLastTimeTicks_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getLastTimeMicro()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return elapsed microseconds of the most recent completed interval
     */
    private static native double getLastTimeMicro_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getLastTimeMilli()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return elapsed milliseconds of the most recent completed interval
     */
    private static native double getLastTimeMilli_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getLastTimeSec()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return elapsed seconds of the most recent completed interval
     */
    private static native double getLastTimeSec_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getCounter()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return number of completed timing intervals
     */
    private static native long getCounter_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getFPS()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return completed interval count divided by accumulated seconds; zero when no time is accumulated
     */
    private static native double getFPS_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getAvgTimeSec()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return average seconds per completed interval; zero when no interval is completed
     */
    private static native double getAvgTimeSec_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#getAvgTimeMilli()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     * @return average milliseconds per completed interval; zero when no interval is completed
     */
    private static native double getAvgTimeMilli_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.TickMeter#reset()}.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     */
    private static native void reset_0(long nativeObj);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code TickMeter} instance
     */
    private static native void delete(long nativeObj);

}
