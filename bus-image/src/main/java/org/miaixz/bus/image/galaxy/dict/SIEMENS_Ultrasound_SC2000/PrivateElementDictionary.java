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
