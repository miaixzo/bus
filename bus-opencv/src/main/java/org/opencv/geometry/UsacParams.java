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
package org.opencv.geometry;

// C++: class UsacParams

/**
 * Provides the {@code UsacParams} API.
 */
public class UsacParams {

    /**
     * The {@code nativeObj} value.
     */
    protected final long nativeObj;

    /**
     * Creates a new {@code UsacParams} instance.
     *
     * @param addr the {@code addr} value
     */
    protected UsacParams(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.opencv.core.CleanableMat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a new {@code UsacParams} instance.
     */
    public UsacParams() {
        nativeObj = UsacParams_0();
        long nativeObjCopy = nativeObj;
        org.opencv.core.CleanableMat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    // internal usage only
    /**
     * Performs the {@code __fromPtr__} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static UsacParams __fromPtr__(long addr) {
        return new UsacParams(addr);
    }

    //
    // C++: cv::UsacParams::UsacParams()
    //

    // C++: cv::UsacParams::UsacParams()
    private static native long UsacParams_0();

    //
    // C++: double UsacParams::confidence
    //

    // C++: double UsacParams::confidence
    private static native double get_confidence_0(long nativeObj);

    //
    // C++: void UsacParams::confidence
    //

    // C++: void UsacParams::confidence
    private static native void set_confidence_0(long nativeObj, double confidence);

    //
    // C++: bool UsacParams::isParallel
    //

    // C++: bool UsacParams::isParallel
    private static native boolean get_isParallel_0(long nativeObj);

    //
    // C++: void UsacParams::isParallel
    //

    // C++: void UsacParams::isParallel
    private static native void set_isParallel_0(long nativeObj, boolean isParallel);

    //
    // C++: int UsacParams::loIterations
    //

    // C++: int UsacParams::loIterations
    private static native int get_loIterations_0(long nativeObj);

    //
    // C++: void UsacParams::loIterations
    //

    // C++: void UsacParams::loIterations
    private static native void set_loIterations_0(long nativeObj, int loIterations);

    //
    // C++: LocalOptimMethod UsacParams::loMethod
    //

    // C++: LocalOptimMethod UsacParams::loMethod
    private static native int get_loMethod_0(long nativeObj);

    //
    // C++: void UsacParams::loMethod
    //

    // C++: void UsacParams::loMethod
    private static native void set_loMethod_0(long nativeObj, int loMethod);

    //
    // C++: int UsacParams::loSampleSize
    //

    // C++: int UsacParams::loSampleSize
    private static native int get_loSampleSize_0(long nativeObj);

    //
    // C++: void UsacParams::loSampleSize
    //

    // C++: void UsacParams::loSampleSize
    private static native void set_loSampleSize_0(long nativeObj, int loSampleSize);

    //
    // C++: int UsacParams::maxIterations
    //

    // C++: int UsacParams::maxIterations
    private static native int get_maxIterations_0(long nativeObj);

    //
    // C++: void UsacParams::maxIterations
    //

    // C++: void UsacParams::maxIterations
    private static native void set_maxIterations_0(long nativeObj, int maxIterations);

    //
    // C++: NeighborSearchMethod UsacParams::neighborsSearch
    //

    // C++: NeighborSearchMethod UsacParams::neighborsSearch
    private static native int get_neighborsSearch_0(long nativeObj);

    //
    // C++: void UsacParams::neighborsSearch
    //

    // C++: void UsacParams::neighborsSearch
    private static native void set_neighborsSearch_0(long nativeObj, int neighborsSearch);

    //
    // C++: int UsacParams::randomGeneratorState
    //

    // C++: int UsacParams::randomGeneratorState
    private static native int get_randomGeneratorState_0(long nativeObj);

    //
    // C++: void UsacParams::randomGeneratorState
    //

    // C++: void UsacParams::randomGeneratorState
    private static native void set_randomGeneratorState_0(long nativeObj, int randomGeneratorState);

    //
    // C++: SamplingMethod UsacParams::sampler
    //

    // C++: SamplingMethod UsacParams::sampler
    private static native int get_sampler_0(long nativeObj);

    //
    // C++: void UsacParams::sampler
    //

    // C++: void UsacParams::sampler
    private static native void set_sampler_0(long nativeObj, int sampler);

    //
    // C++: ScoreMethod UsacParams::score
    //

    // C++: ScoreMethod UsacParams::score
    private static native int get_score_0(long nativeObj);

    //
    // C++: void UsacParams::score
    //

    // C++: void UsacParams::score
    private static native void set_score_0(long nativeObj, int score);

    //
    // C++: double UsacParams::threshold
    //

    // C++: double UsacParams::threshold
    private static native double get_threshold_0(long nativeObj);

    //
    // C++: void UsacParams::threshold
    //

    // C++: void UsacParams::threshold
    private static native void set_threshold_0(long nativeObj, double threshold);

    //
    // C++: PolishingMethod UsacParams::final_polisher
    //

    // C++: PolishingMethod UsacParams::final_polisher
    private static native int get_final_polisher_0(long nativeObj);

    //
    // C++: void UsacParams::final_polisher
    //

    // C++: void UsacParams::final_polisher
    private static native void set_final_polisher_0(long nativeObj, int final_polisher);

    //
    // C++: int UsacParams::final_polisher_iterations
    //

    // C++: int UsacParams::final_polisher_iterations
    private static native int get_final_polisher_iterations_0(long nativeObj);

    //
    // C++: void UsacParams::final_polisher_iterations
    //

    // C++: void UsacParams::final_polisher_iterations
    private static native void set_final_polisher_iterations_0(long nativeObj, int final_polisher_iterations);

    // native support for java finalize() or cleaner
    private static native void delete(long nativeObj);

    /**
     * Performs the {@code getNativeObjAddr} operation.
     *
     * @return the operation result
     */
    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Performs the {@code get_confidence} operation.
     *
     * @return the operation result
     */
    public double get_confidence() {
        return get_confidence_0(nativeObj);
    }

    /**
     * Performs the {@code set_confidence} operation.
     *
     * @param confidence the {@code confidence} value
     */
    public void set_confidence(double confidence) {
        set_confidence_0(nativeObj, confidence);
    }

    /**
     * Performs the {@code get_isParallel} operation.
     *
     * @return the operation result
     */
    public boolean get_isParallel() {
        return get_isParallel_0(nativeObj);
    }

    /**
     * Performs the {@code set_isParallel} operation.
     *
     * @param isParallel the {@code isParallel} value
     */
    public void set_isParallel(boolean isParallel) {
        set_isParallel_0(nativeObj, isParallel);
    }

    /**
     * Performs the {@code get_loIterations} operation.
     *
     * @return the operation result
     */
    public int get_loIterations() {
        return get_loIterations_0(nativeObj);
    }

    /**
     * Performs the {@code set_loIterations} operation.
     *
     * @param loIterations the {@code loIterations} value
     */
    public void set_loIterations(int loIterations) {
        set_loIterations_0(nativeObj, loIterations);
    }

    /**
     * Performs the {@code get_loMethod} operation.
     *
     * @return the operation result
     */
    public int get_loMethod() {
        return get_loMethod_0(nativeObj);
    }

    /**
     * Performs the {@code set_loMethod} operation.
     *
     * @param loMethod the {@code loMethod} value
     */
    public void set_loMethod(int loMethod) {
        set_loMethod_0(nativeObj, loMethod);
    }

    /**
     * Performs the {@code get_loSampleSize} operation.
     *
     * @return the operation result
     */
    public int get_loSampleSize() {
        return get_loSampleSize_0(nativeObj);
    }

    /**
     * Performs the {@code set_loSampleSize} operation.
     *
     * @param loSampleSize the {@code loSampleSize} value
     */
    public void set_loSampleSize(int loSampleSize) {
        set_loSampleSize_0(nativeObj, loSampleSize);
    }

    /**
     * Performs the {@code get_maxIterations} operation.
     *
     * @return the operation result
     */
    public int get_maxIterations() {
        return get_maxIterations_0(nativeObj);
    }

    /**
     * Performs the {@code set_maxIterations} operation.
     *
     * @param maxIterations the {@code maxIterations} value
     */
    public void set_maxIterations(int maxIterations) {
        set_maxIterations_0(nativeObj, maxIterations);
    }

    /**
     * Performs the {@code get_neighborsSearch} operation.
     *
     * @return the operation result
     */
    public int get_neighborsSearch() {
        return get_neighborsSearch_0(nativeObj);
    }

    /**
     * Performs the {@code set_neighborsSearch} operation.
     *
     * @param neighborsSearch the {@code neighborsSearch} value
     */
    public void set_neighborsSearch(int neighborsSearch) {
        set_neighborsSearch_0(nativeObj, neighborsSearch);
    }

    /**
     * Performs the {@code get_randomGeneratorState} operation.
     *
     * @return the operation result
     */
    public int get_randomGeneratorState() {
        return get_randomGeneratorState_0(nativeObj);
    }

    /**
     * Performs the {@code set_randomGeneratorState} operation.
     *
     * @param randomGeneratorState the {@code randomGeneratorState} value
     */
    public void set_randomGeneratorState(int randomGeneratorState) {
        set_randomGeneratorState_0(nativeObj, randomGeneratorState);
    }

    /**
     * Performs the {@code get_sampler} operation.
     *
     * @return the operation result
     */
    public int get_sampler() {
        return get_sampler_0(nativeObj);
    }

    /**
     * Performs the {@code set_sampler} operation.
     *
     * @param sampler the {@code sampler} value
     */
    public void set_sampler(int sampler) {
        set_sampler_0(nativeObj, sampler);
    }

    /**
     * Performs the {@code get_score} operation.
     *
     * @return the operation result
     */
    public int get_score() {
        return get_score_0(nativeObj);
    }

    /**
     * Performs the {@code set_score} operation.
     *
     * @param score the {@code score} value
     */
    public void set_score(int score) {
        set_score_0(nativeObj, score);
    }

    /**
     * Performs the {@code get_threshold} operation.
     *
     * @return the operation result
     */
    public double get_threshold() {
        return get_threshold_0(nativeObj);
    }

    /**
     * Performs the {@code set_threshold} operation.
     *
     * @param threshold the {@code threshold} value
     */
    public void set_threshold(double threshold) {
        set_threshold_0(nativeObj, threshold);
    }

    /**
     * Performs the {@code get_final_polisher} operation.
     *
     * @return the operation result
     */
    public int get_final_polisher() {
        return get_final_polisher_0(nativeObj);
    }

    /**
     * Performs the {@code set_final_polisher} operation.
     *
     * @param final_polisher the {@code final_polisher} value
     */
    public void set_final_polisher(int final_polisher) {
        set_final_polisher_0(nativeObj, final_polisher);
    }

    /**
     * Performs the {@code get_final_polisher_iterations} operation.
     *
     * @return the operation result
     */
    public int get_final_polisher_iterations() {
        return get_final_polisher_iterations_0(nativeObj);
    }

    /**
     * Performs the {@code set_final_polisher_iterations} operation.
     *
     * @param final_polisher_iterations the {@code final_polisher_iterations} value
     */
    public void set_final_polisher_iterations(int final_polisher_iterations) {
        set_final_polisher_iterations_0(nativeObj, final_polisher_iterations);
    }

}
