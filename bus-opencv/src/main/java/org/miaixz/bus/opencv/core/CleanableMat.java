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

import java.lang.ref.Cleaner;

/**
 * Base class that associates a native matrix handle with automatic cleanup.
 */
public abstract class CleanableMat {

    /**
     * Shared cleanup service that releases native handles after their Java wrappers become unreachable.
     */
    public static Cleaner cleaner = Cleaner.create();

    /**
     * Takes ownership of a nonzero native matrix header and registers its deletion with the shared cleaner. The cleanup
     * action captures a copy of the handle rather than this wrapper, so it cannot keep the wrapper reachable.
     *
     * @param obj native binding handle to retain; must be nonzero and must not be owned by another cleanup wrapper
     * @throws UnsupportedOperationException if the native binding handle is zero
     */
    protected CleanableMat(long obj) {
        if (obj == 0)
            throw new UnsupportedOperationException("Native object address is NULL");

        nativeObj = obj;

        long nativeObjCopy = nativeObj;
        cleaner.register(this, () -> n_delete(nativeObjCopy));
    }

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code CleanableMat} instance
     */
    private static native void n_delete(long nativeObj);

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    public final long nativeObj;
}
