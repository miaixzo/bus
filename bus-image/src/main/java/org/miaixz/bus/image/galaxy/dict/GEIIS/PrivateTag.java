/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.GEIIS;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEIIS";

    /** (004B,xx13) VR=IS VM=1 ? */
    public static final int _004B_xx13_ = 0x004B0013;

    /** (004B,xx15) VR=LT VM=1 ? */
    public static final int _004B_xx15_ = 0x004B0015;

    /** (0009,xx10) VR=SQ VM=1 GE Private Image Thumbnail Sequence */
    public static final int GEPrivateImageThumbnailSequence = 0x00090010;

    /** (0009,xx12) VR=IS VM=1 ? */
    public static final int _0009_xx12_ = 0x00090012;

    /** (0029,xx10) VR=UL VM=1 Shift Count */
    public static final int ShiftCount = 0x00290010;

    /** (0029,xx12) VR=UL VM=1 Offset */
    public static final int Offset = 0x00290012;

    /** (0029,xx14) VR=UL VM=1 Actual Frame Number */
    public static final int ActualFrameNumber = 0x00290014;

    /** (0905,xx30) VR=LO VM=1 Assigning Authority For Patient ID */
    public static final int AssigningAuthorityForPatientID = 0x09050030;

    /** (0907,xx10) VR=UI VM=1 Original Study Instance UID */
    public static final int OriginalStudyInstanceUID = 0x09070010;

    /** (0907,xx20) VR=UI VM=1 Original Series Instance UID */
    public static final int OriginalSeriesInstanceUID = 0x09070020;

    /** (0907,xx30) VR=UI VM=1 Original SOP Instance UID */
    public static final int OriginalSOPInstanceUID = 0x09070030;

    /** (7FD1,xx10) VR=UL VM=1 Compression Type */
    public static final int CompressionType = 0x7FD10010;

    /** (7FD1,xx20) VR=UL VM=1-n Multiframe Offsets */
    public static final int MultiframeOffsets = 0x7FD10020;

    /** (7FD1,xx30) VR=UL VM=1 Multi-Resolution Levels */
    public static final int MultiResolutionLevels = 0x7FD10030;

    /** (7FD1,xx40) VR=UL VM=1-n Subband Rows */
    public static final int SubbandRows = 0x7FD10040;

    /** (7FD1,xx50) VR=UL VM=1-n Subband Columns */
    public static final int SubbandColumns = 0x7FD10050;

    /** (7FD1,xx60) VR=UL VM=1-n Subband Bytecounts */
    public static final int SubbandBytecounts = 0x7FD10060;

}
