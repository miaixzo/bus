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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMAG_01;

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

            case PrivateTag.ImageLocation:
            case PrivateTag.CenterRCoordOfPlaneImage:
            case PrivateTag.CenterACoordOfPlaneImage:
            case PrivateTag.CenterSCoordOfPlaneImage:
            case PrivateTag.NormalRCoord:
            case PrivateTag.NormalACoord:
            case PrivateTag.NormalSCoord:
            case PrivateTag.RCoordOfTopRightCorner:
            case PrivateTag.ACoordOfTopRightCorner:
            case PrivateTag.SCoordOfTopRightCorner:
            case PrivateTag.RCoordOfBottomRightCorner:
            case PrivateTag.ACoordOfBottomRightCorner:
            case PrivateTag.SCoordOfBottomRightCorner:
            case PrivateTag.TableStartLocation:
            case PrivateTag.TableEndLocation:
            case PrivateTag.ImageDimensionX:
            case PrivateTag.ImageDimensionY:
            case PrivateTag.NumberOfExcitations:
                return VR.FL;

            case PrivateTag.ForeignImageRevision:
            case PrivateTag.RASLetterOfImageLocation:
            case PrivateTag.RASLetterForSideOfImage:
            case PrivateTag.RASLetterForAnteriorPosterior:
            case PrivateTag.RASLetterForScoutStartLoc:
            case PrivateTag.RASLetterForScoutEndLoc:
                return VR.SH;

            case PrivateTag.ImageArchiveFlag:
            case PrivateTag.VmaMamp:
            case PrivateTag.VmaMod:
            case PrivateTag.VmaClipOrNoiseIndexBy10:
            case PrivateTag.ImagingOptions:
            case PrivateTag.ObliquePlane:
                return VR.SL;

            case PrivateTag.ScoutType:
            case PrivateTag.VmaPhase:
            case PrivateTag.SmartScanOnOffFlag:
            case PrivateTag.ImagingMode:
            case PrivateTag.PulseSequence:
            case PrivateTag.PlaneType:
                return VR.SS;
        }
        return VR.UN;
    }

}
