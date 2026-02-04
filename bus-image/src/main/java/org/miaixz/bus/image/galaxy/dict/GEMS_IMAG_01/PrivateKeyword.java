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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMAG_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ImageArchiveFlag:
                return "ImageArchiveFlag";

            case PrivateTag.ScoutType:
                return "ScoutType";

            case PrivateTag.VmaMamp:
                return "VmaMamp";

            case PrivateTag.VmaPhase:
                return "VmaPhase";

            case PrivateTag.VmaMod:
                return "VmaMod";

            case PrivateTag.VmaClipOrNoiseIndexBy10:
                return "VmaClipOrNoiseIndexBy10";

            case PrivateTag.SmartScanOnOffFlag:
                return "SmartScanOnOffFlag";

            case PrivateTag.ForeignImageRevision:
                return "ForeignImageRevision";

            case PrivateTag.ImagingMode:
                return "ImagingMode";

            case PrivateTag.PulseSequence:
                return "PulseSequence";

            case PrivateTag.ImagingOptions:
                return "ImagingOptions";

            case PrivateTag.PlaneType:
                return "PlaneType";

            case PrivateTag.ObliquePlane:
                return "ObliquePlane";

            case PrivateTag.RASLetterOfImageLocation:
                return "RASLetterOfImageLocation";

            case PrivateTag.ImageLocation:
                return "ImageLocation";

            case PrivateTag.CenterRCoordOfPlaneImage:
                return "CenterRCoordOfPlaneImage";

            case PrivateTag.CenterACoordOfPlaneImage:
                return "CenterACoordOfPlaneImage";

            case PrivateTag.CenterSCoordOfPlaneImage:
                return "CenterSCoordOfPlaneImage";

            case PrivateTag.NormalRCoord:
                return "NormalRCoord";

            case PrivateTag.NormalACoord:
                return "NormalACoord";

            case PrivateTag.NormalSCoord:
                return "NormalSCoord";

            case PrivateTag.RCoordOfTopRightCorner:
                return "RCoordOfTopRightCorner";

            case PrivateTag.ACoordOfTopRightCorner:
                return "ACoordOfTopRightCorner";

            case PrivateTag.SCoordOfTopRightCorner:
                return "SCoordOfTopRightCorner";

            case PrivateTag.RCoordOfBottomRightCorner:
                return "RCoordOfBottomRightCorner";

            case PrivateTag.ACoordOfBottomRightCorner:
                return "ACoordOfBottomRightCorner";

            case PrivateTag.SCoordOfBottomRightCorner:
                return "SCoordOfBottomRightCorner";

            case PrivateTag.TableStartLocation:
                return "TableStartLocation";

            case PrivateTag.TableEndLocation:
                return "TableEndLocation";

            case PrivateTag.RASLetterForSideOfImage:
                return "RASLetterForSideOfImage";

            case PrivateTag.RASLetterForAnteriorPosterior:
                return "RASLetterForAnteriorPosterior";

            case PrivateTag.RASLetterForScoutStartLoc:
                return "RASLetterForScoutStartLoc";

            case PrivateTag.RASLetterForScoutEndLoc:
                return "RASLetterForScoutEndLoc";

            case PrivateTag.ImageDimensionX:
                return "ImageDimensionX";

            case PrivateTag.ImageDimensionY:
                return "ImageDimensionY";

            case PrivateTag.NumberOfExcitations:
                return "NumberOfExcitations";
        }
        return "";
    }

}
