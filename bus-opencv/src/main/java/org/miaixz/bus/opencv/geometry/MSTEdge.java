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
 * A native edge record used by minimum-spanning-tree operations.
 */
public class MSTEdge {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native MSTEdge handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected MSTEdge(long addr) {
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
    public static MSTEdge __fromPtr__(long addr) {
        return new MSTEdge(addr);
    }

    /**
     * Returns source vertex index.
     *
     * @return source vertex index
     */
    public int get_source() {
        return get_source_0(nativeObj);
    }

    /**
     * Sets source vertex index.
     *
     * @param source index of the source vertex of the graph edge
     */
    public void set_source(int source) {
        set_source_0(nativeObj, source);
    }

    /**
     * Returns destination vertex index.
     *
     * @return destination vertex index
     */
    public int get_target() {
        return get_target_0(nativeObj);
    }

    /**
     * Sets destination vertex index.
     *
     * @param target index of the destination vertex of the graph edge
     */
    public void set_target(int target) {
        set_target_0(nativeObj, target);
    }

    /**
     * Returns graph edge weight.
     *
     * @return graph edge weight
     */
    public double get_weight() {
        return get_weight_0(nativeObj);
    }

    /**
     * Sets graph edge weight.
     *
     * @param weight weight assigned to the graph edge
     */
    public void set_weight(double weight) {
        set_weight_0(nativeObj, weight);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.MSTEdge#get_source()}.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     * @return source vertex index
     */
    private static native int get_source_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.MSTEdge#set_source(int)}.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     * @param source    index of the source vertex of the graph edge
     */
    private static native void set_source_0(long nativeObj, int source);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.MSTEdge#get_target()}.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     * @return destination vertex index
     */
    private static native int get_target_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.MSTEdge#set_target(int)}.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     * @param target    index of the destination vertex of the graph edge
     */
    private static native void set_target_0(long nativeObj, int target);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.MSTEdge#get_weight()}.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     * @return graph edge weight
     */
    private static native double get_weight_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.geometry.MSTEdge#set_weight(double)}.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     * @param weight    weight assigned to the graph edge
     */
    private static native void set_weight_0(long nativeObj, double weight);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code MSTEdge} instance
     */
    private static native void delete(long nativeObj);

}
