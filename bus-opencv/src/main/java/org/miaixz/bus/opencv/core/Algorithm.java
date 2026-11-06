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
 * Base class for native algorithm implementations exposed through the Java API. State management and serialization
 * behavior depend on the concrete algorithm.
 */
public class Algorithm {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native Algorithm handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected Algorithm(long addr) {
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
    public static Algorithm __fromPtr__(long addr) {
        return new Algorithm(addr);
    }

    /**
     * Clears the state maintained by the underlying algorithm. The concrete implementation determines which state is
     * cleared.
     */
    public void clear() {
        clear_0(nativeObj);
    }

    /**
     * Returns true if the Algorithm is empty (e.g. in the very beginning or after unsuccessful read.
     *
     * @return true if no usable contents are present; false otherwise
     */
    public boolean empty() {
        return empty_0(nativeObj);
    }

    /**
     * Serializes the algorithm state to a file. The concrete native algorithm must support serialization.
     *
     * @param filename path of the input or output file
     */
    public void save(String filename) {
        save_0(nativeObj, filename);
    }

    /**
     * Returns the algorithm identifier used as the top-level node name during serialization.
     *
     * @return the serialization node name reported by the concrete algorithm
     */
    public String getDefaultName() {
        return getDefaultName_0(nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Algorithm#clear()}.
     *
     * @param nativeObj native binding handle of this {@code Algorithm} instance
     */
    private static native void clear_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Algorithm#empty()}.
     *
     * @param nativeObj native binding handle of this {@code Algorithm} instance
     * @return true if no usable contents are present; false otherwise
     */
    private static native boolean empty_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Algorithm#save(String)}.
     *
     * @param nativeObj native binding handle of this {@code Algorithm} instance
     * @param filename  path of the input or output file
     */
    private static native void save_0(long nativeObj, String filename);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.Algorithm#getDefaultName()}.
     *
     * @param nativeObj native binding handle of this {@code Algorithm} instance
     * @return the serialization node name reported by the concrete algorithm
     */
    private static native String getDefaultName_0(long nativeObj);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code Algorithm} instance
     */
    private static native void delete(long nativeObj);

}
