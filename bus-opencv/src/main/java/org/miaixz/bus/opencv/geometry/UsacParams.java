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
package org.miaixz.bus.opencv.geometry;

/**
 * Configuration for robust model estimation with the USAC framework.
 */
public class UsacParams {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native UsacParams handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected UsacParams(long addr) {
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
    public static UsacParams __fromPtr__(long addr) {
        return new UsacParams(addr);
    }

    /**
     * Creates an empty UsacParams value.
     */
    public UsacParams() {
        nativeObj = UsacParams_0();
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Returns requested probability of obtaining a robust model from an all-inlier sample.
     *
     * @return requested probability of obtaining a robust model from an all-inlier sample
     */
    public double get_confidence() {
        return get_confidence_0(nativeObj);
    }

    /**
     * Sets requested probability of obtaining a robust model from an all-inlier sample.
     *
     * @param confidence requested probability of obtaining a robust model from an all-inlier sample
     */
    public void set_confidence(double confidence) {
        set_confidence_0(nativeObj, confidence);
    }

    /**
     * Returns true if parallel robust estimation is enabled.
     *
     * @return true if parallel robust estimation is enabled
     */
    public boolean get_isParallel() {
        return get_isParallel_0(nativeObj);
    }

    /**
     * Sets whether parallel robust estimation is enabled.
     *
     * @param isParallel whether parallel is enabled
     */
    public void set_isParallel(boolean isParallel) {
        set_isParallel_0(nativeObj, isParallel);
    }

    /**
     * Returns maximum number of local-optimization iterations.
     *
     * @return maximum number of local-optimization iterations
     */
    public int get_loIterations() {
        return get_loIterations_0(nativeObj);
    }

    /**
     * Sets maximum number of local-optimization iterations.
     *
     * @param loIterations maximum number of local-optimization iterations
     */
    public void set_loIterations(int loIterations) {
        set_loIterations_0(nativeObj, loIterations);
    }

    /**
     * Returns local-optimization method identifier.
     *
     * @return local-optimization method identifier
     */
    public int get_loMethod() {
        return get_loMethod_0(nativeObj);
    }

    /**
     * Sets local-optimization method identifier.
     *
     * @param loMethod local-optimization method identifier
     */
    public void set_loMethod(int loMethod) {
        set_loMethod_0(nativeObj, loMethod);
    }

    /**
     * Returns sample size used by local optimization.
     *
     * @return sample size used by local optimization
     */
    public int get_loSampleSize() {
        return get_loSampleSize_0(nativeObj);
    }

    /**
     * Sets sample size used by local optimization.
     *
     * @param loSampleSize sample size used by local optimization
     */
    public void set_loSampleSize(int loSampleSize) {
        set_loSampleSize_0(nativeObj, loSampleSize);
    }

    /**
     * Returns upper limit on robust-estimation iterations.
     *
     * @return upper limit on robust-estimation iterations
     */
    public int get_maxIterations() {
        return get_maxIterations_0(nativeObj);
    }

    /**
     * Sets upper limit on robust-estimation iterations.
     *
     * @param maxIterations upper limit on robust-estimation iterations
     */
    public void set_maxIterations(int maxIterations) {
        set_maxIterations_0(nativeObj, maxIterations);
    }

    /**
     * Returns neighborhood-search strategy used by robust estimation.
     *
     * @return neighborhood-search strategy used by robust estimation
     */
    public int get_neighborsSearch() {
        return get_neighborsSearch_0(nativeObj);
    }

    /**
     * Sets neighborhood-search strategy used by robust estimation.
     *
     * @param neighborsSearch neighborhood-search strategy used by robust estimation
     */
    public void set_neighborsSearch(int neighborsSearch) {
        set_neighborsSearch_0(nativeObj, neighborsSearch);
    }

    /**
     * Returns initial state of the estimator's random-number generator.
     *
     * @return initial state of the estimator's random-number generator
     */
    public int get_randomGeneratorState() {
        return get_randomGeneratorState_0(nativeObj);
    }

    /**
     * Sets initial state of the estimator's random-number generator.
     *
     * @param randomGeneratorState initial state of the estimator's random-number generator
     */
    public void set_randomGeneratorState(int randomGeneratorState) {
        set_randomGeneratorState_0(nativeObj, randomGeneratorState);
    }

    /**
     * Returns sample-selection strategy identifier.
     *
     * @return sample-selection strategy identifier
     */
    public int get_sampler() {
        return get_sampler_0(nativeObj);
    }

    /**
     * Sets sample-selection strategy identifier.
     *
     * @param sampler sample-selection strategy identifier
     */
    public void set_sampler(int sampler) {
        set_sampler_0(nativeObj, sampler);
    }

    /**
     * Returns robust-model scoring method identifier.
     *
     * @return robust-model scoring method identifier
     */
    public int get_score() {
        return get_score_0(nativeObj);
    }

    /**
     * Sets robust-model scoring method identifier.
     *
     * @param score robust-model scoring method identifier
     */
    public void set_score(int score) {
        set_score_0(nativeObj, score);
    }

    /**
     * Returns maximum residual for classifying a correspondence as an inlier.
     *
     * @return maximum residual for classifying a correspondence as an inlier
     */
    public double get_threshold() {
        return get_threshold_0(nativeObj);
    }

    /**
     * Sets maximum residual for classifying a correspondence as an inlier.
     *
     * @param threshold maximum residual used to classify a model correspondence as an inlier
     */
    public void set_threshold(double threshold) {
        set_threshold_0(nativeObj, threshold);
    }

    /**
     * Returns final model-polishing method identifier.
     *
     * @return final model-polishing method identifier
     */
    public int get_final_polisher() {
        return get_final_polisher_0(nativeObj);
    }

    /**
     * Sets final model-polishing method identifier.
     *
     * @param final_polisher final model-polishing method identifier
     */
    public void set_final_polisher(int final_polisher) {
        set_final_polisher_0(nativeObj, final_polisher);
    }

    /**
     * Returns number of final model-polishing iterations.
     *
     * @return number of final model-polishing iterations
     */
    public int get_final_polisher_iterations() {
        return get_final_polisher_iterations_0(nativeObj);
    }

    /**
     * Sets number of final model-polishing iterations.
     *
     * @param final_polisher_iterations number of final model-polishing iterations
     */
    public void set_final_polisher_iterations(int final_polisher_iterations) {
        set_final_polisher_iterations_0(nativeObj, final_polisher_iterations);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#UsacParams()}.
     *
     * @return a newly allocated native {@code UsacParams} binding handle
     */
    private static native long UsacParams_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_confidence()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return requested probability of obtaining a robust model from an all-inlier sample
     */
    private static native double get_confidence_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_confidence(double)}.
     *
     * @param nativeObj  native binding handle of this {@code UsacParams} instance
     * @param confidence requested probability of obtaining a robust model from an all-inlier sample
     */
    private static native void set_confidence_0(long nativeObj, double confidence);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_isParallel()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return true if parallel robust estimation is enabled
     */
    private static native boolean get_isParallel_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_isParallel(boolean)}.
     *
     * @param nativeObj  native binding handle of this {@code UsacParams} instance
     * @param isParallel whether parallel is enabled
     */
    private static native void set_isParallel_0(long nativeObj, boolean isParallel);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_loIterations()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return maximum number of local-optimization iterations
     */
    private static native int get_loIterations_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_loIterations(int)}.
     *
     * @param nativeObj    native binding handle of this {@code UsacParams} instance
     * @param loIterations maximum number of local-optimization iterations
     */
    private static native void set_loIterations_0(long nativeObj, int loIterations);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_loMethod()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return local-optimization method identifier
     */
    private static native int get_loMethod_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_loMethod(int)}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @param loMethod  local-optimization method identifier
     */
    private static native void set_loMethod_0(long nativeObj, int loMethod);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_loSampleSize()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return sample size used by local optimization
     */
    private static native int get_loSampleSize_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_loSampleSize(int)}.
     *
     * @param nativeObj    native binding handle of this {@code UsacParams} instance
     * @param loSampleSize sample size used by local optimization
     */
    private static native void set_loSampleSize_0(long nativeObj, int loSampleSize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_maxIterations()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return upper limit on robust-estimation iterations
     */
    private static native int get_maxIterations_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_maxIterations(int)}.
     *
     * @param nativeObj     native binding handle of this {@code UsacParams} instance
     * @param maxIterations upper limit on robust-estimation iterations
     */
    private static native void set_maxIterations_0(long nativeObj, int maxIterations);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_neighborsSearch()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return neighborhood-search strategy used by robust estimation
     */
    private static native int get_neighborsSearch_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_neighborsSearch(int)}.
     *
     * @param nativeObj       native binding handle of this {@code UsacParams} instance
     * @param neighborsSearch neighborhood-search strategy used by robust estimation
     */
    private static native void set_neighborsSearch_0(long nativeObj, int neighborsSearch);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_randomGeneratorState()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return initial state of the estimator's random-number generator
     */
    private static native int get_randomGeneratorState_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_randomGeneratorState(int)}.
     *
     * @param nativeObj            native binding handle of this {@code UsacParams} instance
     * @param randomGeneratorState initial state of the estimator's random-number generator
     */
    private static native void set_randomGeneratorState_0(long nativeObj, int randomGeneratorState);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_sampler()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return sample-selection strategy identifier
     */
    private static native int get_sampler_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_sampler(int)}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @param sampler   sample-selection strategy identifier
     */
    private static native void set_sampler_0(long nativeObj, int sampler);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_score()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return robust-model scoring method identifier
     */
    private static native int get_score_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_score(int)}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @param score     robust-model scoring method identifier
     */
    private static native void set_score_0(long nativeObj, int score);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_threshold()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return maximum residual for classifying a correspondence as an inlier
     */
    private static native double get_threshold_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_threshold(double)}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @param threshold maximum residual used to classify a model correspondence as an inlier
     */
    private static native void set_threshold_0(long nativeObj, double threshold);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_final_polisher()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return final model-polishing method identifier
     */
    private static native int get_final_polisher_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_final_polisher(int)}.
     *
     * @param nativeObj      native binding handle of this {@code UsacParams} instance
     * @param final_polisher final model-polishing method identifier
     */
    private static native void set_final_polisher_0(long nativeObj, int final_polisher);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#get_final_polisher_iterations()}.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     * @return number of final model-polishing iterations
     */
    private static native int get_final_polisher_iterations_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.UsacParams#set_final_polisher_iterations(int)}.
     *
     * @param nativeObj                 native binding handle of this {@code UsacParams} instance
     * @param final_polisher_iterations number of final model-polishing iterations
     */
    private static native void set_final_polisher_iterations_0(long nativeObj, int final_polisher_iterations);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code UsacParams} instance
     */
    private static native void delete(long nativeObj);

}
