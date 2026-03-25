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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_Ultrasound_SC2000;

/**
 * @author Kimi Liu
 * @since Java 21+
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
