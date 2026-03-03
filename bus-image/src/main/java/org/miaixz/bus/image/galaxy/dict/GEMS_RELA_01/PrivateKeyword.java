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
package org.miaixz.bus.image.galaxy.dict.GEMS_RELA_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.SeriesFromWhichPrescribed:
                return "SeriesFromWhichPrescribed";

            case PrivateTag.GenesisVersionNow:
                return "GenesisVersionNow";

            case PrivateTag.SeriesRecordChecksum:
                return "SeriesRecordChecksum";

            case PrivateTag._0021_xx15_:
                return "_0021_xx15_";

            case PrivateTag._0021_xx16_:
                return "_0021_xx16_";

            case PrivateTag.AcqReconRecordChecksum:
                return "AcqReconRecordChecksum";

            case PrivateTag.TableStartLocation:
                return "TableStartLocation";

            case PrivateTag.ImageFromWhichPrescribed:
                return "ImageFromWhichPrescribed";

            case PrivateTag.ScreenFormat:
                return "ScreenFormat";

            case PrivateTag.AnatomicalReferenceForScout:
                return "AnatomicalReferenceForScout";

            case PrivateTag._0021_xx4E_:
                return "_0021_xx4E_";

            case PrivateTag.LocationsInAcquisition:
                return "LocationsInAcquisition";

            case PrivateTag.GraphicallyPrescribed:
                return "GraphicallyPrescribed";

            case PrivateTag.RotationFromSourceXRot:
                return "RotationFromSourceXRot";

            case PrivateTag.RotationFromSourceYRot:
                return "RotationFromSourceYRot";

            case PrivateTag.RotationFromSourceZRot:
                return "RotationFromSourceZRot";

            case PrivateTag.ImagePosition:
                return "ImagePosition";

            case PrivateTag.ImageOrientation:
                return "ImageOrientation";

            case PrivateTag.Num3DSlabs:
                return "Num3DSlabs";

            case PrivateTag.LocsPer3DSlab:
                return "LocsPer3DSlab";

            case PrivateTag.Overlaps:
                return "Overlaps";

            case PrivateTag.ImageFiltering:
                return "ImageFiltering";

            case PrivateTag.DiffusionDirection:
                return "DiffusionDirection";

            case PrivateTag.TaggingFlipAngle:
                return "TaggingFlipAngle";

            case PrivateTag.TaggingOrientation:
                return "TaggingOrientation";

            case PrivateTag.TagSpacing:
                return "TagSpacing";

            case PrivateTag.RTIATimer:
                return "RTIATimer";

            case PrivateTag.Fps:
                return "Fps";

            case PrivateTag._0021_xx70_:
                return "_0021_xx70_";

            case PrivateTag._0021_xx71_:
                return "_0021_xx71_";

            case PrivateTag.AutoWindowLevelAlpha:
                return "AutoWindowLevelAlpha";

            case PrivateTag.AutoWindowLevelBeta:
                return "AutoWindowLevelBeta";

            case PrivateTag.AutoWindowLevelWindow:
                return "AutoWindowLevelWindow";

            case PrivateTag.AutoWindowLevelLevel:
                return "AutoWindowLevelLevel";

            case PrivateTag.TubeFocalSpotPosition:
                return "TubeFocalSpotPosition";

            case PrivateTag.BiopsyPosition:
                return "BiopsyPosition";

            case PrivateTag.BiopsyTLocation:
                return "BiopsyTLocation";

            case PrivateTag.BiopsyRefLocation:
                return "BiopsyRefLocation";
        }
        return "";
    }

}
