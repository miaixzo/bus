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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_VA0__RAW;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CT VA0 RAW";

    /** (0021,xx10) VR=UL VM=2 Creation Mask */
    public static final int CreationMask = 0x00210010;

    /** (0021,xx20) VR=UL VM=2 Evaluation Mask */
    public static final int EvaluationMask = 0x00210020;

    /** (0021,xx30) VR=US VM=7 Extended Processing Mask */
    public static final int ExtendedProcessingMask = 0x00210030;

    /** (0021,xx40) VR=UL VM=1-n ? */
    public static final int _0021_xx40_ = 0x00210040;

    /** (0021,xx41) VR=UL VM=1-n ? */
    public static final int _0021_xx41_ = 0x00210041;

    /** (0021,xx42) VR=UL VM=1-n ? */
    public static final int _0021_xx42_ = 0x00210042;

    /** (0021,xx43) VR=UL VM=1-n ? */
    public static final int _0021_xx43_ = 0x00210043;

    /** (0021,xx44) VR=UL VM=1-n ? */
    public static final int _0021_xx44_ = 0x00210044;

    /** (0021,xx50) VR=CS VM=1 ? */
    public static final int _0021_xx50_ = 0x00210050;

}
