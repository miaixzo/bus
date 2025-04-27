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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_APPL_PRESENTATION;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CT APPL PRESENTATION";

    /** (0029,xx00) VR=US VM=1 Translucent Mode */
    public static final int TranslucentMode = 0x00290000;

    /** (0029,xx01) VR=FD VM=1 Translucent Window Size */
    public static final int TranslucentWindowSize = 0x00290001;

    /** (0029,xx02) VR=US VM=1 Panoramic Mode */
    public static final int PanoramicMode = 0x00290002;

    /** (0029,xx03) VR=FD VM=1 Panoramic Inner Width */
    public static final int PanoramicInnerWidth = 0x00290003;

    /** (0029,xx04) VR=US VM=1 Display Unseen Areas */
    public static final int DisplayUnseenAreas = 0x00290004;

    /** (0029,xx05) VR=US VM=4 Unseen Areas Color */
    public static final int UnseenAreasColor = 0x00290005;

    /** (0029,xx06) VR=US VM=1 Display Tagged Data */
    public static final int DisplayTaggedData = 0x00290006;

    /** (0029,xx07) VR=US VM=4 Tagged Color */
    public static final int TaggedColor = 0x00290007;

    /** (0029,xx08) VR=UL VM=1 Tagged Sample Thickness */
    public static final int TaggedSampleThickness = 0x00290008;

    /** (0029,xx09) VR=SL VM=1 Tagged Threshold */
    public static final int TaggedThreshold = 0x00290009;

    /** (0029,xx10) VR=US VM=1 Kernel Filter */
    public static final int KernelFilter = 0x00290010;

}
