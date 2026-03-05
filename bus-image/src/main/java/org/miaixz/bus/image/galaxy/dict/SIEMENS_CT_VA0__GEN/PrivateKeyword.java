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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.SourceSideCollimatorAperture:
                return "SourceSideCollimatorAperture";

            case PrivateTag.DetectorSideCollimatorAperture:
                return "DetectorSideCollimatorAperture";

            case PrivateTag.ExposureTime:
                return "ExposureTime";

            case PrivateTag.ExposureCurrent:
                return "ExposureCurrent";

            case PrivateTag.KVPGeneratorPowerCurrent:
                return "KVPGeneratorPowerCurrent";

            case PrivateTag.GeneratorVoltage:
                return "GeneratorVoltage";

            case PrivateTag.MasterControlMask:
                return "MasterControlMask";

            case PrivateTag.ProcessingMask:
                return "ProcessingMask";

            case PrivateTag._0019_xx44_:
                return "_0019_xx44_";

            case PrivateTag._0019_xx45_:
                return "_0019_xx45_";

            case PrivateTag.NumberOfVirtuellChannels:
                return "NumberOfVirtuellChannels";

            case PrivateTag.NumberOfReadings:
                return "NumberOfReadings";

            case PrivateTag._0019_xx71_:
                return "_0019_xx71_";

            case PrivateTag.NumberOfProjections:
                return "NumberOfProjections";

            case PrivateTag.NumberOfBytes:
                return "NumberOfBytes";

            case PrivateTag.ReconstructionAlgorithmSet:
                return "ReconstructionAlgorithmSet";

            case PrivateTag.ReconstructionAlgorithmIndex:
                return "ReconstructionAlgorithmIndex";

            case PrivateTag.RegenerationSoftwareVersion:
                return "RegenerationSoftwareVersion";

            case PrivateTag._0019_xx88_:
                return "_0019_xx88_";

            case PrivateTag.RotationAngle:
                return "RotationAngle";

            case PrivateTag.StartAngle:
                return "StartAngle";

            case PrivateTag._0021_xx20_:
                return "_0021_xx20_";

            case PrivateTag.TopogramTubePosition:
                return "TopogramTubePosition";

            case PrivateTag.LengthOfTopogram:
                return "LengthOfTopogram";

            case PrivateTag.TopogramCorrectionFactor:
                return "TopogramCorrectionFactor";

            case PrivateTag.MaximumTablePosition:
                return "MaximumTablePosition";

            case PrivateTag.TableMoveDirectionCode:
                return "TableMoveDirectionCode";

            case PrivateTag.VOIStartRow:
                return "VOIStartRow";

            case PrivateTag.VOIStopRow:
                return "VOIStopRow";

            case PrivateTag.VOIStartColumn:
                return "VOIStartColumn";

            case PrivateTag.VOIStopColumn:
                return "VOIStopColumn";

            case PrivateTag.VOIStartSlice:
                return "VOIStartSlice";

            case PrivateTag.VOIStopSlice:
                return "VOIStopSlice";

            case PrivateTag.VectorStartRow:
                return "VectorStartRow";

            case PrivateTag.VectorRowStep:
                return "VectorRowStep";

            case PrivateTag.VectorStartColumn:
                return "VectorStartColumn";

            case PrivateTag.VectorColumnStep:
                return "VectorColumnStep";

            case PrivateTag.RangeTypeCode:
                return "RangeTypeCode";

            case PrivateTag.ReferenceTypeCode:
                return "ReferenceTypeCode";

            case PrivateTag.ObjectOrientation:
                return "ObjectOrientation";

            case PrivateTag.LightOrientation:
                return "LightOrientation";

            case PrivateTag.LightBrightness:
                return "LightBrightness";

            case PrivateTag.LightContrast:
                return "LightContrast";

            case PrivateTag.OverlayThreshold:
                return "OverlayThreshold";

            case PrivateTag.SurfaceThreshold:
                return "SurfaceThreshold";

            case PrivateTag.GreyScaleThreshold:
                return "GreyScaleThreshold";

            case PrivateTag._0021_xxA0_:
                return "_0021_xxA0_";

            case PrivateTag._0021_xxA2_:
                return "_0021_xxA2_";

            case PrivateTag._0021_xxA7_:
                return "_0021_xxA7_";
        }
        return "";
    }

}
