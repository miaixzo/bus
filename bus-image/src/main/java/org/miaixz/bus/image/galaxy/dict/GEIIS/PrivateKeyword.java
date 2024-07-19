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
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag._004B_xx13_:
            return "_004B_xx13_";
        case PrivateTag._004B_xx15_:
            return "_004B_xx15_";
        case PrivateTag.GEPrivateImageThumbnailSequence:
            return "GEPrivateImageThumbnailSequence";
        case PrivateTag._0009_xx12_:
            return "_0009_xx12_";
        case PrivateTag.ShiftCount:
            return "ShiftCount";
        case PrivateTag.Offset:
            return "Offset";
        case PrivateTag.ActualFrameNumber:
            return "ActualFrameNumber";
        case PrivateTag.AssigningAuthorityForPatientID:
            return "AssigningAuthorityForPatientID";
        case PrivateTag.OriginalStudyInstanceUID:
            return "OriginalStudyInstanceUID";
        case PrivateTag.OriginalSeriesInstanceUID:
            return "OriginalSeriesInstanceUID";
        case PrivateTag.OriginalSOPInstanceUID:
            return "OriginalSOPInstanceUID";
        case PrivateTag.CompressionType:
            return "CompressionType";
        case PrivateTag.MultiframeOffsets:
            return "MultiframeOffsets";
        case PrivateTag.MultiResolutionLevels:
            return "MultiResolutionLevels";
        case PrivateTag.SubbandRows:
            return "SubbandRows";
        case PrivateTag.SubbandColumns:
            return "SubbandColumns";
        case PrivateTag.SubbandBytecounts:
            return "SubbandBytecounts";
        }
        return "";
    }

}
