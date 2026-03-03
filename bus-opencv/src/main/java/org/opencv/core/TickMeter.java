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
package org.opencv.core;

// C++: class TickMeter
/**
 * a Class to measure passing time.
 *
 * The class computes passing time by counting the number of ticks per second. That is, the following code computes the
 * execution time in seconds: SNIPPET: snippets/core_various.cpp TickMeter_total
 *
 * It is also possible to compute the average time over multiple runs: SNIPPET: snippets/core_various.cpp
 * TickMeter_average
 *
 * SEE: getTickCount, getTickFrequency
 */
public class TickMeter {

    protected final long nativeObj;

    protected TickMeter(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    public TickMeter() {
        nativeObj = TickMeter_0();
        long nativeObjCopy = nativeObj;
        org.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    // internal usage only
    public static TickMeter __fromPtr__(long addr) {
        return new TickMeter(addr);
    }

    //
    // C++: cv::TickMeter::TickMeter()
    //

    // C++: cv::TickMeter::TickMeter()
    private static native long TickMeter_0();

    //
    // C++: void cv::TickMeter::start()
    //

    // C++: void cv::TickMeter::start()
    private static native void start_0(long nativeObj);

    //
    // C++: void cv::TickMeter::stop()
    //

    // C++: void cv::TickMeter::stop()
    private static native void stop_0(long nativeObj);

    //
    // C++: int64 cv::TickMeter::getTimeTicks()
    //

    // C++: int64 cv::TickMeter::getTimeTicks()
    private static native long getTimeTicks_0(long nativeObj);

    //
    // C++: double cv::TickMeter::getTimeMicro()
    //

    // C++: double cv::TickMeter::getTimeMicro()
    private static native double getTimeMicro_0(long nativeObj);

    //
    // C++: double cv::TickMeter::getTimeMilli()
    //

    // C++: double cv::TickMeter::getTimeMilli()
    private static native double getTimeMilli_0(long nativeObj);

    //
    // C++: double cv::TickMeter::getTimeSec()
    //

    // C++: double cv::TickMeter::getTimeSec()
    private static native double getTimeSec_0(long nativeObj);

    //
    // C++: int64 cv::TickMeter::getCounter()
    //

    // C++: int64 cv::TickMeter::getCounter()
    private static native long getCounter_0(long nativeObj);

    //
    // C++: double cv::TickMeter::getFPS()
    //

    // C++: double cv::TickMeter::getFPS()
    private static native double getFPS_0(long nativeObj);

    //
    // C++: double cv::TickMeter::getAvgTimeSec()
    //

    // C++: double cv::TickMeter::getAvgTimeSec()
    private static native double getAvgTimeSec_0(long nativeObj);

    //
    // C++: double cv::TickMeter::getAvgTimeMilli()
    //

    // C++: double cv::TickMeter::getAvgTimeMilli()
    private static native double getAvgTimeMilli_0(long nativeObj);

    //
    // C++: void cv::TickMeter::reset()
    //

    // C++: void cv::TickMeter::reset()
    private static native void reset_0(long nativeObj);

    // native support for deleting native object
    private static native void delete(long nativeObj);

    public long getNativeObjAddr() {
        return nativeObj;
    }

    public void start() {
        start_0(nativeObj);
    }

    public void stop() {
        stop_0(nativeObj);
    }

    public long getTimeTicks() {
        return getTimeTicks_0(nativeObj);
    }

    public double getTimeMicro() {
        return getTimeMicro_0(nativeObj);
    }

    public double getTimeMilli() {
        return getTimeMilli_0(nativeObj);
    }

    public double getTimeSec() {
        return getTimeSec_0(nativeObj);
    }

    public long getCounter() {
        return getCounter_0(nativeObj);
    }

    public double getFPS() {
        return getFPS_0(nativeObj);
    }

    public double getAvgTimeSec() {
        return getAvgTimeSec_0(nativeObj);
    }

    public double getAvgTimeMilli() {
        return getAvgTimeMilli_0(nativeObj);
    }

    public void reset() {
        reset_0(nativeObj);
    }

}
