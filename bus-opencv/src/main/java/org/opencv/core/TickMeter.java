/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org opencv.org and other contributors.         ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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

    public long getNativeObjAddr() {
        return nativeObj;
    }

    // internal usage only
    public static TickMeter __fromPtr__(long addr) {
        return new TickMeter(addr);
    }

    //
    // C++: cv::TickMeter::TickMeter()
    //

    public TickMeter() {
        nativeObj = TickMeter_0();
        long nativeObjCopy = nativeObj;
        org.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    //
    // C++: void cv::TickMeter::start()
    //

    public void start() {
        start_0(nativeObj);
    }

    //
    // C++: void cv::TickMeter::stop()
    //

    public void stop() {
        stop_0(nativeObj);
    }

    //
    // C++: int64 cv::TickMeter::getTimeTicks()
    //

    public long getTimeTicks() {
        return getTimeTicks_0(nativeObj);
    }

    //
    // C++: double cv::TickMeter::getTimeMicro()
    //

    public double getTimeMicro() {
        return getTimeMicro_0(nativeObj);
    }

    //
    // C++: double cv::TickMeter::getTimeMilli()
    //

    public double getTimeMilli() {
        return getTimeMilli_0(nativeObj);
    }

    //
    // C++: double cv::TickMeter::getTimeSec()
    //

    public double getTimeSec() {
        return getTimeSec_0(nativeObj);
    }

    //
    // C++: int64 cv::TickMeter::getCounter()
    //

    public long getCounter() {
        return getCounter_0(nativeObj);
    }

    //
    // C++: double cv::TickMeter::getFPS()
    //

    public double getFPS() {
        return getFPS_0(nativeObj);
    }

    //
    // C++: double cv::TickMeter::getAvgTimeSec()
    //

    public double getAvgTimeSec() {
        return getAvgTimeSec_0(nativeObj);
    }

    //
    // C++: double cv::TickMeter::getAvgTimeMilli()
    //

    public double getAvgTimeMilli() {
        return getAvgTimeMilli_0(nativeObj);
    }

    //
    // C++: void cv::TickMeter::reset()
    //

    public void reset() {
        reset_0(nativeObj);
    }

    // C++: cv::TickMeter::TickMeter()
    private static native long TickMeter_0();

    // C++: void cv::TickMeter::start()
    private static native void start_0(long nativeObj);

    // C++: void cv::TickMeter::stop()
    private static native void stop_0(long nativeObj);

    // C++: int64 cv::TickMeter::getTimeTicks()
    private static native long getTimeTicks_0(long nativeObj);

    // C++: double cv::TickMeter::getTimeMicro()
    private static native double getTimeMicro_0(long nativeObj);

    // C++: double cv::TickMeter::getTimeMilli()
    private static native double getTimeMilli_0(long nativeObj);

    // C++: double cv::TickMeter::getTimeSec()
    private static native double getTimeSec_0(long nativeObj);

    // C++: int64 cv::TickMeter::getCounter()
    private static native long getCounter_0(long nativeObj);

    // C++: double cv::TickMeter::getFPS()
    private static native double getFPS_0(long nativeObj);

    // C++: double cv::TickMeter::getAvgTimeSec()
    private static native double getAvgTimeSec_0(long nativeObj);

    // C++: double cv::TickMeter::getAvgTimeMilli()
    private static native double getAvgTimeMilli_0(long nativeObj);

    // C++: void cv::TickMeter::reset()
    private static native void reset_0(long nativeObj);

    // native support for deleting native object
    private static native void delete(long nativeObj);

}
