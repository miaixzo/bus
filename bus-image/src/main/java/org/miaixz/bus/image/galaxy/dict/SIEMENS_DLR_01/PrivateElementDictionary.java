/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DLR_01;

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

            case PrivateTag._0019_xxA0_:
            case PrivateTag._0019_xxA1_:
                return VR.DS;

            case PrivateTag.MeasurementMode:
            case PrivateTag.ImageType:
            case PrivateTag.SoftwareVersion:
            case PrivateTag.MPMCode:
            case PrivateTag.Latitude:
            case PrivateTag.Sensitivity:
            case PrivateTag.EDR:
            case PrivateTag.LFix:
            case PrivateTag.SFix:
            case PrivateTag.PresetMode:
            case PrivateTag.Region:
            case PrivateTag.Subregion:
            case PrivateTag.Orientation:
            case PrivateTag.MarkOnFilm:
            case PrivateTag.RotationOnDRC:
            case PrivateTag.ReaderType:
            case PrivateTag.SubModality:
            case PrivateTag.ReaderSerialNumber:
            case PrivateTag.CassetteScale:
            case PrivateTag.CassetteMatrix:
            case PrivateTag.CassetteSubmatrix:
            case PrivateTag.Barcode:
            case PrivateTag.ContrastType:
            case PrivateTag.RotationAmount:
            case PrivateTag.RotationCenter:
            case PrivateTag.DensityShift:
            case PrivateTag.FrequencyEnhancement:
            case PrivateTag.FrequencyType:
            case PrivateTag.KernelLength:
            case PrivateTag.PLASource:
            case PrivateTag.PLADestination:
            case PrivateTag.UIDOriginalImage:
            case PrivateTag._0019_xx76_:
            case PrivateTag.PLAOfSecondaryDestination:
            case PrivateTag.FilmFormat:
            case PrivateTag.FilmSize:
            case PrivateTag.FullFilmFormat:
                return VR.LO;

            case PrivateTag.ReaderHeader:
                return VR.LT;

            case PrivateTag.KernelMode:
            case PrivateTag.ConvolutionMode:
                return VR.UL;

            case PrivateTag.FrequencyRank:
            case PrivateTag.NumberOfHardcopies:
                return VR.US;
        }
        return VR.UN;
    }

}
