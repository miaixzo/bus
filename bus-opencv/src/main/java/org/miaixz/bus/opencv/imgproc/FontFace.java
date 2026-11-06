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
package org.miaixz.bus.opencv.imgproc;

import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.MatOfInt;

/**
 * A native font face used by text measurement and rendering operations.
 */
public class FontFace {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native FontFace handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected FontFace(long addr) {
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
    public static FontFace __fromPtr__(long addr) {
        return new FontFace(addr);
    }

    /**
     * Creates a font face using the default embedded sans font.
     */
    public FontFace() {
        nativeObj = FontFace_0();
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a configured FontFace value using the supplied values.
     *
     * @param fontPathOrName either path to a custom font file (.ttf/.otf, optionally gzip-compressed) or the name of
     *                       one of the built-in font slots: "sans": the default font (Rubik, always compiled in);
     *                       "italic": Rubik Italic, compiled in only if OpenCV was built with WITH_ITALICFONT=ON;
     *                       otherwise it can be provided with setBuiltinFont(), and if it is not available, "italic"
     *                       silently resolves to "sans"; "uni": WenQuanYi Micro Hei (CJK), compiled in only with
     *                       WITH_UNIFONT=ON; otherwise provide it with setBuiltinFont(), or loading fails. The built-in
     *                       slots also serve as the fallback chain: characters missing from the user's font are taken
     *                       from "sans", then "italic", then "uni". Empty fontPathOrName means the default embedded
     *                       font ("sans")
     */
    public FontFace(String fontPathOrName) {
        nativeObj = FontFace_1(fontPathOrName);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Loads new font face.
     *
     * @param fontPathOrName font-file path or built-in font-slot name; an empty string selects the default font
     * @return true if the requested font face is loaded successfully
     */
    public boolean set(String fontPathOrName) {
        return set_0(nativeObj, fontPathOrName);
    }

    /**
     * Returns the name of the loaded font face.
     *
     * @return the name of the loaded font face
     */
    public String getName() {
        return getName_0(nativeObj);
    }

    /**
     * Sets the current variable font instance.
     *
     * @param params axis-tag and 16.16 fixed-point value pairs selecting a variable-font instance
     * @return true if the variable-font instance settings are applied successfully
     */
    public boolean setInstance(MatOfInt params) {
        Mat params_mat = params;
        return setInstance_0(nativeObj, params_mat.nativeObj);
    }

    /**
     * Copies the current variable-font axis-tag and fixed-point value pairs into params.
     *
     * @param params axis-tag and 16.16 fixed-point value pairs receiving the current variable-font settings
     * @return true if variable-font instance settings are returned in params
     */
    public boolean getInstance(MatOfInt params) {
        Mat params_mat = params;
        return getInstance_0(nativeObj, params_mat.nativeObj);
    }

    /**
     * Provides or replaces one of the built-in font slots with an external font file.
     *
     * @param fontName the slot: "sans", "italic" or "uni"
     * @param fontPath path to a .ttf/.otf font (optionally gzip-compressed, e.g. .ttf.gz). An empty path restores the
     *                 compiled-in font of that slot (if any), otherwise the slot becomes empty
     * @return true if the named built-in font slot was replaced successfully
     */
    public static boolean setBuiltinFont(String fontName, String fontPath) {
        return setBuiltinFont_0(fontName, fontPath);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#FontFace()}.
     *
     * @return a newly allocated native {@code FontFace} binding handle
     */
    private static native long FontFace_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#FontFace(String)}.
     *
     * @param fontPathOrName either path to a custom font file (.ttf/.otf, optionally gzip-compressed) or the name of
     *                       one of the built-in font slots: "sans": the default font (Rubik, always compiled in);
     *                       "italic": Rubik Italic, compiled in only if OpenCV was built with WITH_ITALICFONT=ON;
     *                       otherwise it can be provided with setBuiltinFont(), and if it is not available, "italic"
     *                       silently resolves to "sans"; "uni": WenQuanYi Micro Hei (CJK), compiled in only with
     *                       WITH_UNIFONT=ON; otherwise provide it with setBuiltinFont(), or loading fails. The built-in
     *                       slots also serve as the fallback chain: characters missing from the user's font are taken
     *                       from "sans", then "italic", then "uni". Empty fontPathOrName means the default embedded
     *                       font ("sans")
     * @return a newly allocated native {@code FontFace} binding handle
     */
    private static native long FontFace_1(String fontPathOrName);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#set(String)}.
     *
     * @param nativeObj      native binding handle of this {@code FontFace} instance
     * @param fontPathOrName font-file path or built-in font-slot name; an empty string selects the default font
     * @return true if the requested font face is loaded successfully
     */
    private static native boolean set_0(long nativeObj, String fontPathOrName);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#getName()}.
     *
     * @param nativeObj native binding handle of this {@code FontFace} instance
     * @return the name of the loaded font face
     */
    private static native String getName_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#setInstance(MatOfInt)}.
     *
     * @param nativeObj            native binding handle of this {@code FontFace} instance
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; axis-tag and 16.16 fixed-point
     *                             value pairs selecting a variable-font instance
     * @return true if the variable-font instance settings are applied successfully
     */
    private static native boolean setInstance_0(long nativeObj, long params_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#getInstance(MatOfInt)}.
     *
     * @param nativeObj            native binding handle of this {@code FontFace} instance
     * @param params_mat_nativeObj native matrix or object handle for {@code params_mat}; axis-tag and 16.16 fixed-point
     *                             value pairs receiving the current variable-font settings
     * @return true if variable-font instance settings are returned in params
     */
    private static native boolean getInstance_0(long nativeObj, long params_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.FontFace#setBuiltinFont(String,String)}.
     *
     * @param fontName the slot: "sans", "italic" or "uni"
     * @param fontPath path to a .ttf/.otf font (optionally gzip-compressed, e.g. .ttf.gz). An empty path restores the
     *                 compiled-in font of that slot (if any), otherwise the slot becomes empty
     * @return true if the named built-in font slot was replaced successfully
     */
    private static native boolean setBuiltinFont_0(String fontName, String fontPath);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code FontFace} instance
     */
    private static native void delete(long nativeObj);

}
