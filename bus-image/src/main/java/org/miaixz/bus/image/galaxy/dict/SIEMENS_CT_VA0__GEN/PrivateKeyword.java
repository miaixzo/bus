/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
