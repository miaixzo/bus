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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_NM__Private;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.CurrentSegment:
            return "CurrentSegment";
        case PrivateTag.NumberOfSegments:
            return "NumberOfSegments";
        case PrivateTag.SegmentStartPosition:
            return "SegmentStartPosition";
        case PrivateTag.SegmentStopPosition:
            return "SegmentStopPosition";
        case PrivateTag.RelativeCOROffsetXDirection:
            return "RelativeCOROffsetXDirection";
        case PrivateTag.RelativeCOROffsetZDirection:
            return "RelativeCOROffsetZDirection";
        case PrivateTag.CurrentRotationNumber:
            return "CurrentRotationNumber";
        case PrivateTag.NumberOfRotations:
            return "NumberOfRotations";
        case PrivateTag.AlignmentTranslations:
            return "AlignmentTranslations";
        case PrivateTag.AlignmentRotations:
            return "AlignmentRotations";
        case PrivateTag.AlignmentTimestamp:
            return "AlignmentTimestamp";
        case PrivateTag.RelatedXraySeriesInstanceUID:
            return "RelatedXraySeriesInstanceUID";
        case PrivateTag._7051_xx25_:
            return "_7051_xx25_";
        case PrivateTag._7051_xx26_:
            return "_7051_xx26_";
        case PrivateTag._7051_xx27_:
            return "_7051_xx27_";
        case PrivateTag._7051_xx28_:
            return "_7051_xx28_";
        case PrivateTag._7051_xx29_:
            return "_7051_xx29_";
        }
        return "";
    }

}
