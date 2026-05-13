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

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the PrivateElementDictionary type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    /**
     * The private creator value.
     */
    public static final String PrivateCreator = "";

    /**
     * Creates a new instance.
     */
    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    /**
     * Executes the keyword of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    /**
     * Executes the vr of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.TableStartLocation:
            case PrivateTag.RotationFromSourceXRot:
            case PrivateTag.RotationFromSourceYRot:
            case PrivateTag.RotationFromSourceZRot:
            case PrivateTag.TaggingFlipAngle:
            case PrivateTag.TaggingOrientation:
            case PrivateTag.TagSpacing:
            case PrivateTag.RTIATimer:
            case PrivateTag.Fps:
            case PrivateTag.AutoWindowLevelAlpha:
            case PrivateTag.AutoWindowLevelBeta:
            case PrivateTag.AutoWindowLevelWindow:
            case PrivateTag.AutoWindowLevelLevel:
                return VR.DS;

            case PrivateTag.BiopsyTLocation:
            case PrivateTag.BiopsyRefLocation:
                return VR.FL;

            case PrivateTag.AnatomicalReferenceForScout:
                return VR.LO;

            case PrivateTag._0021_xx70_:
            case PrivateTag._0021_xx71_:
                return VR.LT;

            case PrivateTag.GenesisVersionNow:
            case PrivateTag.ImagePosition:
            case PrivateTag.ImageOrientation:
                return VR.SH;

            case PrivateTag.Num3DSlabs:
            case PrivateTag.LocsPer3DSlab:
            case PrivateTag.Overlaps:
            case PrivateTag.ImageFiltering:
            case PrivateTag.DiffusionDirection:
                return VR.SL;

            case PrivateTag.SeriesFromWhichPrescribed:
            case PrivateTag._0021_xx16_:
            case PrivateTag.ImageFromWhichPrescribed:
            case PrivateTag.ScreenFormat:
            case PrivateTag.LocationsInAcquisition:
            case PrivateTag.GraphicallyPrescribed:
            case PrivateTag.TubeFocalSpotPosition:
            case PrivateTag.BiopsyPosition:
                return VR.SS;

            case PrivateTag.SeriesRecordChecksum:
            case PrivateTag.AcqReconRecordChecksum:
                return VR.UL;

            case PrivateTag._0021_xx15_:
            case PrivateTag._0021_xx4E_:
                return VR.US;
        }
        return VR.UN;
    }

}
