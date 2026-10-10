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
package org.opencv.imgproc;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

// C++: class FontFace

/**
 * Wrapper on top of a truetype/opentype/etc font, i.e. Freetype's FT_Face.
 * <p>
 * The class is used to store the loaded fonts; the font can then be passed to the functions putText and getTextSize.
 */
public class FontFace {

    /**
     * The {@code nativeObj} value.
     */
    protected final long nativeObj;

    /**
     * Creates a new {@code FontFace} instance.
     *
     * @param addr the {@code addr} value
     */
    protected FontFace(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.opencv.core.CleanableMat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * loads default font
     */
    public FontFace() {
        nativeObj = FontFace_0();
        long nativeObjCopy = nativeObj;
        org.opencv.core.CleanableMat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * loads font at the specified path or with specified name.
     *
     * @param fontPathOrName either path to the custom font or the name of embedded font: "sans", "italic" or "uni".
     *                       Empty fontPathOrName means the default embedded font.
     */
    public FontFace(String fontPathOrName) {
        nativeObj = FontFace_1(fontPathOrName);
        long nativeObjCopy = nativeObj;
        org.opencv.core.CleanableMat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    //
    // C++: cv::FontFace::FontFace()
    //

    // internal usage only
    /**
     * Performs the {@code __fromPtr__} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static FontFace __fromPtr__(long addr) {
        return new FontFace(addr);
    }

    //
    // C++: cv::FontFace::FontFace(String fontPathOrName)
    //

    // C++: cv::FontFace::FontFace()
    private static native long FontFace_0();

    //
    // C++: bool cv::FontFace::set(String fontPathOrName)
    //

    // C++: cv::FontFace::FontFace(String fontPathOrName)
    private static native long FontFace_1(String fontPathOrName);

    //
    // C++: String cv::FontFace::getName()
    //

    // C++: bool cv::FontFace::set(String fontPathOrName)
    private static native boolean set_0(long nativeObj, String fontPathOrName);

    //
    // C++: bool cv::FontFace::setInstance(vector_int params)
    //

    // C++: String cv::FontFace::getName()
    private static native String getName_0(long nativeObj);

    //
    // C++: bool cv::FontFace::getInstance(vector_int& params)
    //

    // C++: bool cv::FontFace::setInstance(vector_int params)
    private static native boolean setInstance_0(long nativeObj, long params_mat_nativeObj);

    // C++: bool cv::FontFace::getInstance(vector_int& params)
    private static native boolean getInstance_0(long nativeObj, long params_mat_nativeObj);

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
     * loads new font face
     *
     * @param fontPathOrName automatically generated
     * @return automatically generated
     */
    public boolean set(String fontPathOrName) {
        return set_0(nativeObj, fontPathOrName);
    }

    /**
     * Performs the {@code getName} operation.
     *
     * @return the operation result
     */
    public String getName() {
        return getName_0(nativeObj);
    }

    /**
     * sets the current variable font instance.
     *
     * @param params The list of pairs key1, value1, key2, value2, ..., e.g.
     *               {@code myfont.setInstance({CV_FOURCC('w','g','h','t'), 400&lt;&lt;16, CV_FOURCC('s','l','n','t'), -(15&lt;&lt;16)});}
     *               Note that the parameter values are specified in 16.16 fixed-point format, that is, integer values
     *               need to be shifted by 16 (or multiplied by 65536).
     * @return automatically generated
     */
    public boolean setInstance(MatOfInt params) {
        Mat params_mat = params;
        return setInstance_0(nativeObj, params_mat.nativeObj);
    }

    /**
     * Performs the {@code getInstance} operation.
     *
     * @param params the {@code params} value
     * @return the operation result
     */
    public boolean getInstance(MatOfInt params) {
        Mat params_mat = params;
        return getInstance_0(nativeObj, params_mat.nativeObj);
    }

}
