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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_NM__Private;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "PHILIPS NM -Private";

    /** (7051,xx00) VR=US VM=1 Current Segment */
    public static final int CurrentSegment = 0x70510000;

    /** (7051,xx01) VR=US VM=1 Number of Segments */
    public static final int NumberOfSegments = 0x70510001;

    /** (7051,xx02) VR=FL VM=1 Segment Start Position */
    public static final int SegmentStartPosition = 0x70510002;

    /** (7051,xx03) VR=FL VM=1 Segment Stop Position */
    public static final int SegmentStopPosition = 0x70510003;

    /** (7051,xx04) VR=FL VM=1 Relative COR offset - X direction */
    public static final int RelativeCOROffsetXDirection = 0x70510004;

    /** (7051,xx05) VR=FL VM=1 Relative COR offset - Z direction */
    public static final int RelativeCOROffsetZDirection = 0x70510005;

    /** (7051,xx06) VR=US VM=1 Current Rotation Number */
    public static final int CurrentRotationNumber = 0x70510006;

    /** (7051,xx07) VR=US VM=1 Number of Rotations */
    public static final int NumberOfRotations = 0x70510007;

    /** (7051,xx10) VR=DS VM=1-n Alignment Translations */
    public static final int AlignmentTranslations = 0x70510010;

    /** (7051,xx11) VR=DS VM=1-n Alignment Rotations */
    public static final int AlignmentRotations = 0x70510011;

    /** (7051,xx12) VR=DS VM=1 Alignment Timestamp */
    public static final int AlignmentTimestamp = 0x70510012;

    /** (7051,xx15) VR=UI VM=1 Related Xray Series Instance UID */
    public static final int RelatedXraySeriesInstanceUID = 0x70510015;

    /** (7051,xx25) VR=LO VM=1 ? */
    public static final int _7051_xx25_ = 0x70510025;

    /** (7051,xx26) VR=DS VM=1 ? */
    public static final int _7051_xx26_ = 0x70510026;

    /** (7051,xx27) VR=IS VM=1 ? */
    public static final int _7051_xx27_ = 0x70510027;

    /** (7051,xx28) VR=IS VM=1 ? */
    public static final int _7051_xx28_ = 0x70510028;

    /** (7051,xx29) VR=IS VM=1 ? */
    public static final int _7051_xx29_ = 0x70510029;

}
