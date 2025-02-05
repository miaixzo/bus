/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.AMI_ImageContext_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "AMI ImageContext_01";

    /** (3109,xx10) VR=CS VM=1 Window Invert */
    public static final int WindowInvert = 0x31090010;

    /** (3109,xx20) VR=IS VM=1 Window Center */
    public static final int WindowCenter = 0x31090020;

    /** (3109,xx30) VR=IS VM=1 Window Width */
    public static final int WindowWidth = 0x31090030;

    /** (3109,xx40) VR=CS VM=1 Pixel Aspect Ratio Swap */
    public static final int PixelAspectRatioSwap = 0x31090040;

    /** (3109,xx50) VR=CS VM=1 Enable Averaging */
    public static final int EnableAveraging = 0x31090050;

    /** (3109,xx60) VR=CS VM=1 Quality */
    public static final int Quality = 0x31090060;

    /** (3109,xx70) VR=CS VM=1 Viewport Annotation Level */
    public static final int ViewportAnnotationLevel = 0x31090070;

    /** (3109,xx80) VR=CS VM=1 Show Image Annotation */
    public static final int ShowImageAnnotation = 0x31090080;

    /** (3109,xx90) VR=CS VM=1 Show Image Overlay */
    public static final int ShowImageOverlay = 0x31090090;

}
