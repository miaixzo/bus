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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_VA0__GEN;

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

            case PrivateTag._0019_xx71_:
            case PrivateTag._0021_xxA2_:
                return VR.CS;

            case PrivateTag.SourceSideCollimatorAperture:
            case PrivateTag.DetectorSideCollimatorAperture:
            case PrivateTag.KVPGeneratorPowerCurrent:
            case PrivateTag.GeneratorVoltage:
            case PrivateTag.LengthOfTopogram:
            case PrivateTag.TopogramCorrectionFactor:
            case PrivateTag.MaximumTablePosition:
            case PrivateTag.ObjectOrientation:
            case PrivateTag.LightOrientation:
            case PrivateTag.LightBrightness:
            case PrivateTag.LightContrast:
                return VR.DS;

            case PrivateTag.ExposureTime:
            case PrivateTag.ExposureCurrent:
            case PrivateTag.NumberOfVirtuellChannels:
            case PrivateTag.NumberOfReadings:
            case PrivateTag.NumberOfProjections:
            case PrivateTag.NumberOfBytes:
            case PrivateTag._0019_xx88_:
            case PrivateTag.RotationAngle:
            case PrivateTag.StartAngle:
            case PrivateTag._0021_xx20_:
            case PrivateTag.TopogramTubePosition:
            case PrivateTag.TableMoveDirectionCode:
            case PrivateTag.VOIStartRow:
            case PrivateTag.VOIStopRow:
            case PrivateTag.VOIStartColumn:
            case PrivateTag.VOIStopColumn:
            case PrivateTag.VOIStartSlice:
            case PrivateTag.VOIStopSlice:
            case PrivateTag.VectorStartRow:
            case PrivateTag.VectorRowStep:
            case PrivateTag.VectorStartColumn:
            case PrivateTag.VectorColumnStep:
            case PrivateTag.RangeTypeCode:
            case PrivateTag.ReferenceTypeCode:
            case PrivateTag.OverlayThreshold:
            case PrivateTag.SurfaceThreshold:
            case PrivateTag.GreyScaleThreshold:
            case PrivateTag._0021_xxA0_:
                return VR.IS;

            case PrivateTag.ReconstructionAlgorithmSet:
            case PrivateTag.ReconstructionAlgorithmIndex:
            case PrivateTag.RegenerationSoftwareVersion:
            case PrivateTag._0021_xxA7_:
                return VR.LO;

            case PrivateTag.MasterControlMask:
            case PrivateTag._0019_xx44_:
            case PrivateTag._0019_xx45_:
                return VR.UL;

            case PrivateTag.ProcessingMask:
                return VR.US;
        }
        return VR.UN;
    }

}
