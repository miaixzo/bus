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

// C++: class MSTEdge

/**
 * Represents an edge in a graph for Minimum Spanning Tree (MST) computation.
 * <p>
 * Each edge connects two nodes (source and target) and has an associated weight.
 */
public class MSTEdge {

    /**
     * The {@code nativeObj} value.
     */
    protected final long nativeObj;

    /**
     * Creates a new {@code MSTEdge} instance.
     *
     * @param addr the {@code addr} value
     */
    protected MSTEdge(long addr) {
        nativeObj = addr;
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
    public static MSTEdge __fromPtr__(long addr) {
        return new MSTEdge(addr);
    }

    // C++: int MSTEdge::source
    private static native int get_source_0(long nativeObj);

    //
    // C++: int MSTEdge::source
    //

    // C++: void MSTEdge::source
    private static native void set_source_0(long nativeObj, int source);

    //
    // C++: void MSTEdge::source
    //

    // C++: int MSTEdge::target
    private static native int get_target_0(long nativeObj);

    //
    // C++: int MSTEdge::target
    //

    // C++: void MSTEdge::target
    private static native void set_target_0(long nativeObj, int target);

    //
    // C++: void MSTEdge::target
    //

    // C++: double MSTEdge::weight
    private static native double get_weight_0(long nativeObj);

    //
    // C++: double MSTEdge::weight
    //

    // C++: void MSTEdge::weight
    private static native void set_weight_0(long nativeObj, double weight);

    //
    // C++: void MSTEdge::weight
    //

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
     * Performs the {@code get_source} operation.
     *
     * @return the operation result
     */
    public int get_source() {
        return get_source_0(nativeObj);
    }

    /**
     * Performs the {@code set_source} operation.
     *
     * @param source the {@code source} value
     */
    public void set_source(int source) {
        set_source_0(nativeObj, source);
    }

    /**
     * Performs the {@code get_target} operation.
     *
     * @return the operation result
     */
    public int get_target() {
        return get_target_0(nativeObj);
    }

    /**
     * Performs the {@code set_target} operation.
     *
     * @param target the {@code target} value
     */
    public void set_target(int target) {
        set_target_0(nativeObj, target);
    }

    /**
     * Performs the {@code get_weight} operation.
     *
     * @return the operation result
     */
    public double get_weight() {
        return get_weight_0(nativeObj);
    }

    /**
     * Performs the {@code set_weight} operation.
     *
     * @param weight the {@code weight} value
     */
    public void set_weight(double weight) {
        set_weight_0(nativeObj, weight);
    }

}
