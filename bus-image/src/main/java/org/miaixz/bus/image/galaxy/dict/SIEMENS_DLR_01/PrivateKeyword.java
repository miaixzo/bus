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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DLR_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.MeasurementMode:
                return "MeasurementMode";

            case PrivateTag.ImageType:
                return "ImageType";

            case PrivateTag.SoftwareVersion:
                return "SoftwareVersion";

            case PrivateTag.MPMCode:
                return "MPMCode";

            case PrivateTag.Latitude:
                return "Latitude";

            case PrivateTag.Sensitivity:
                return "Sensitivity";

            case PrivateTag.EDR:
                return "EDR";

            case PrivateTag.LFix:
                return "LFix";

            case PrivateTag.SFix:
                return "SFix";

            case PrivateTag.PresetMode:
                return "PresetMode";

            case PrivateTag.Region:
                return "Region";

            case PrivateTag.Subregion:
                return "Subregion";

            case PrivateTag.Orientation:
                return "Orientation";

            case PrivateTag.MarkOnFilm:
                return "MarkOnFilm";

            case PrivateTag.RotationOnDRC:
                return "RotationOnDRC";

            case PrivateTag.ReaderType:
                return "ReaderType";

            case PrivateTag.SubModality:
                return "SubModality";

            case PrivateTag.ReaderSerialNumber:
                return "ReaderSerialNumber";

            case PrivateTag.CassetteScale:
                return "CassetteScale";

            case PrivateTag.CassetteMatrix:
                return "CassetteMatrix";

            case PrivateTag.CassetteSubmatrix:
                return "CassetteSubmatrix";

            case PrivateTag.Barcode:
                return "Barcode";

            case PrivateTag.ContrastType:
                return "ContrastType";

            case PrivateTag.RotationAmount:
                return "RotationAmount";

            case PrivateTag.RotationCenter:
                return "RotationCenter";

            case PrivateTag.DensityShift:
                return "DensityShift";

            case PrivateTag.FrequencyRank:
                return "FrequencyRank";

            case PrivateTag.FrequencyEnhancement:
                return "FrequencyEnhancement";

            case PrivateTag.FrequencyType:
                return "FrequencyType";

            case PrivateTag.KernelLength:
                return "KernelLength";

            case PrivateTag.KernelMode:
                return "KernelMode";

            case PrivateTag.ConvolutionMode:
                return "ConvolutionMode";

            case PrivateTag.PLASource:
                return "PLASource";

            case PrivateTag.PLADestination:
                return "PLADestination";

            case PrivateTag.UIDOriginalImage:
                return "UIDOriginalImage";

            case PrivateTag._0019_xx76_:
                return "_0019_xx76_";

            case PrivateTag.ReaderHeader:
                return "ReaderHeader";

            case PrivateTag.PLAOfSecondaryDestination:
                return "PLAOfSecondaryDestination";

            case PrivateTag._0019_xxA0_:
                return "_0019_xxA0_";

            case PrivateTag._0019_xxA1_:
                return "_0019_xxA1_";

            case PrivateTag.NumberOfHardcopies:
                return "NumberOfHardcopies";

            case PrivateTag.FilmFormat:
                return "FilmFormat";

            case PrivateTag.FilmSize:
                return "FilmSize";

            case PrivateTag.FullFilmFormat:
                return "FullFilmFormat";
        }
        return "";
    }

}
