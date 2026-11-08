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
package org.miaixz.bus.opencv.img_hash;

/**
 * A perceptual image hash algorithm based on a frequency-domain image representation.
 */
public class PHash extends ImgHashBase {

    /**
     * Wraps an existing native PHash handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected PHash(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static PHash __fromPtr__(long addr) {
        return new PHash(addr);
    }

    /**
     * Creates a configured PHash instance using the supplied options.
     *
     * @return a new {@code PHash} wrapper configured with the selected options
     */
    public static PHash create() {
        return PHash.__fromPtr__(create_0());
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.PHash#create()}.
     *
     * @return native binding handle for a new {@code PHash} wrapper configured with the selected options
     */
    private static native long create_0();

    /**
     * Deletes the native {@code PHash} binding handle. This compatibility declaration is retained in addition to
     * cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
