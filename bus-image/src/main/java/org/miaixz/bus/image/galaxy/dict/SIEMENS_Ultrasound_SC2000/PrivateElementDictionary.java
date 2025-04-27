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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_Ultrasound_SC2000;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag.RawDataObjectType:
            return VR.CS;
        case PrivateTag.VectorOfBROIPoints:
        case PrivateTag.StartEndTimestampsOfStripStream:
        case PrivateTag.TimestampsOfVisibleRWaves:
            return VR.FD;
        case PrivateTag.VolumeRate:
            return VR.IS;
        case PrivateTag._0019_xx89_:
        case PrivateTag.AcousticMetaInformationVersion:
            return VR.LO;
        case PrivateTag.CommonAcousticMetaInformation:
        case PrivateTag.PerTransactionAcousticControlInformation:
        case PrivateTag.VisualizationInformation:
        case PrivateTag.ApplicationStateInformation:
        case PrivateTag.CineParametersSchema:
        case PrivateTag.ValuesOfCineParameters:
        case PrivateTag._0129_xx29_:
        case PrivateTag.AcousticImageAndFooterData:
        case PrivateTag.VolumePayload:
        case PrivateTag.AfterPayload:
            return VR.OB;
        case PrivateTag.AcousticStreamType:
        case PrivateTag._0119_xx21_:
            return VR.SH;
        case PrivateTag.PhysioCaptureROI:
            return VR.SL;
        case PrivateTag.MultiStreamSequence:
        case PrivateTag.AcousticDataSequence:
        case PrivateTag.MPRViewSequence:
        case PrivateTag.VisualizationSequence:
        case PrivateTag.ApplicationStateSequence:
        case PrivateTag.ReferencedBookmarkSequence:
        case PrivateTag.CineParametersSequence:
            return VR.SQ;
        case PrivateTag.AcousticStreamNumber:
            return VR.SS;
        case PrivateTag.BookmarkUID:
        case PrivateTag.ReferencedBookmarkUID:
        case PrivateTag.VolumeVersionID:
            return VR.UI;
        case PrivateTag.AcousticDataOffset:
        case PrivateTag.AcousticDataLength:
        case PrivateTag.FooterOffset:
        case PrivateTag.FooterLength:
            return VR.UL;
        case PrivateTag.BModeTintIndex:
        case PrivateTag.DopplerTintIndex:
        case PrivateTag.MModeTintIndex:
            return VR.US;
        }
        return VR.UN;
    }
}
