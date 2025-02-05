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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.BModeTintIndex:
            return "BModeTintIndex";
        case PrivateTag.DopplerTintIndex:
            return "DopplerTintIndex";
        case PrivateTag.MModeTintIndex:
            return "MModeTintIndex";
        case PrivateTag._0019_xx89_:
            return "_0019_xx89_";
        case PrivateTag.AcousticMetaInformationVersion:
            return "AcousticMetaInformationVersion";
        case PrivateTag.CommonAcousticMetaInformation:
            return "CommonAcousticMetaInformation";
        case PrivateTag.MultiStreamSequence:
            return "MultiStreamSequence";
        case PrivateTag.AcousticDataSequence:
            return "AcousticDataSequence";
        case PrivateTag.PerTransactionAcousticControlInformation:
            return "PerTransactionAcousticControlInformation";
        case PrivateTag.AcousticDataOffset:
            return "AcousticDataOffset";
        case PrivateTag.AcousticDataLength:
            return "AcousticDataLength";
        case PrivateTag.FooterOffset:
            return "FooterOffset";
        case PrivateTag.FooterLength:
            return "FooterLength";
        case PrivateTag.AcousticStreamNumber:
            return "AcousticStreamNumber";
        case PrivateTag.AcousticStreamType:
            return "AcousticStreamType";
        case PrivateTag.StageTimerTime:
            return "StageTimerTime";
        case PrivateTag.StopWatchTime:
            return "StopWatchTime";
        case PrivateTag.VolumeRate:
            return "VolumeRate";
        case PrivateTag._0119_xx21_:
            return "_0119_xx21_";
        case PrivateTag.MPRViewSequence:
            return "MPRViewSequence";
        case PrivateTag.BookmarkUID:
            return "BookmarkUID";
        case PrivateTag.PlaneOriginVector:
            return "PlaneOriginVector";
        case PrivateTag.RowVector:
            return "RowVector";
        case PrivateTag.ColumnVector:
            return "ColumnVector";
        case PrivateTag.VisualizationSequence:
            return "VisualizationSequence";
        case PrivateTag.VisualizationInformation:
            return "VisualizationInformation";
        case PrivateTag.ApplicationStateSequence:
            return "ApplicationStateSequence";
        case PrivateTag.ApplicationStateInformation:
            return "ApplicationStateInformation";
        case PrivateTag.ReferencedBookmarkSequence:
            return "ReferencedBookmarkSequence";
        case PrivateTag.ReferencedBookmarkUID:
            return "ReferencedBookmarkUID";
        case PrivateTag.CineParametersSequence:
            return "CineParametersSequence";
        case PrivateTag.CineParametersSchema:
            return "CineParametersSchema";
        case PrivateTag.ValuesOfCineParameters:
            return "ValuesOfCineParameters";
        case PrivateTag._0129_xx29_:
            return "_0129_xx29_";
        case PrivateTag.RawDataObjectType:
            return "RawDataObjectType";
        case PrivateTag.PhysioCaptureROI:
            return "PhysioCaptureROI";
        case PrivateTag.VectorOfBROIPoints:
            return "VectorOfBROIPoints";
        case PrivateTag.StartEndTimestampsOfStripStream:
            return "StartEndTimestampsOfStripStream";
        case PrivateTag.TimestampsOfVisibleRWaves:
            return "TimestampsOfVisibleRWaves";
        case PrivateTag.AcousticImageAndFooterData:
            return "AcousticImageAndFooterData";
        case PrivateTag.VolumeVersionID:
            return "VolumeVersionID";
        case PrivateTag.VolumePayload:
            return "VolumePayload";
        case PrivateTag.AfterPayload:
            return "AfterPayload";
        }
        return "";
    }

}
