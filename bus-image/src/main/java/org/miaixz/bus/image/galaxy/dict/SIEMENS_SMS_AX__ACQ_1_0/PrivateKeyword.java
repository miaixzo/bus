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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__ACQ_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.AcquisitionType:
            return "AcquisitionType";
        case PrivateTag.AcquisitionMode:
            return "AcquisitionMode";
        case PrivateTag.FootswitchIndex:
            return "FootswitchIndex";
        case PrivateTag.AcquisitionRoom:
            return "AcquisitionRoom";
        case PrivateTag.CurrentTimeProduct:
            return "CurrentTimeProduct";
        case PrivateTag.ImagerReceptorDose:
            return "ImagerReceptorDose";
        case PrivateTag.SkinDosePercent:
            return "SkinDosePercent";
        case PrivateTag.SkinDoseAccumulation:
            return "SkinDoseAccumulation";
        case PrivateTag.SkinDoseRate:
            return "SkinDoseRate";
        case PrivateTag.ImpacFilename:
            return "ImpacFilename";
        case PrivateTag.CopperFilter:
            return "CopperFilter";
        case PrivateTag.MeasuringField:
            return "MeasuringField";
        case PrivateTag.PostBlankingCircle:
            return "PostBlankingCircle";
        case PrivateTag.DynaAngles:
            return "DynaAngles";
        case PrivateTag.TotalSteps:
            return "TotalSteps";
        case PrivateTag.DynaXRayInfo:
            return "DynaXRayInfo";
        case PrivateTag.ModalityLUTInputGamma:
            return "ModalityLUTInputGamma";
        case PrivateTag.ModalityLUTOutputGamma:
            return "ModalityLUTOutputGamma";
        case PrivateTag.SHSTPAR:
            return "SHSTPAR";
        case PrivateTag.AcquisitionZoom:
            return "AcquisitionZoom";
        case PrivateTag.DynaAngulationStep:
            return "DynaAngulationStep";
        case PrivateTag.DDOValue:
            return "DDOValue";
        case PrivateTag.DRSingleFlag:
            return "DRSingleFlag";
        case PrivateTag.SourcetoIsocenter:
            return "SourcetoIsocenter";
        case PrivateTag.PressureData:
            return "PressureData";
        case PrivateTag.ECGIndexArray:
            return "ECGIndexArray";
        case PrivateTag.FDFlag:
            return "FDFlag";
        case PrivateTag.SHZOOM:
            return "SHZOOM";
        case PrivateTag.SHCOLPAR:
            return "SHCOLPAR";
        case PrivateTag.KFactor:
            return "KFactor";
        case PrivateTag.EVE:
            return "EVE";
        case PrivateTag.TotalSceneTime:
            return "TotalSceneTime";
        case PrivateTag.RestoreFlag:
            return "RestoreFlag";
        case PrivateTag.StandMovementFlag:
            return "StandMovementFlag";
        case PrivateTag.FDRows:
            return "FDRows";
        case PrivateTag.FDColumns:
            return "FDColumns";
        case PrivateTag.TableMovementFlag:
            return "TableMovementFlag";
        case PrivateTag.CrispyXPIFilterValue:
            return "CrispyXPIFilterValue";
        case PrivateTag.ICStentFlag:
            return "ICStentFlag";
        case PrivateTag.GammaLUTSequence:
            return "GammaLUTSequence";
        case PrivateTag.AcquisitionSceneTime:
            return "AcquisitionSceneTime";
        case PrivateTag.ThreeDCardiacPhaseCenter:
            return "ThreeDCardiacPhaseCenter";
        case PrivateTag.ThreeDCardiacPhaseWidth:
            return "ThreeDCardiacPhaseWidth";
        case PrivateTag.OrganProgramInfo:
            return "OrganProgramInfo";
        case PrivateTag.DDOKernelsize:
            return "DDOKernelsize";
        case PrivateTag.mAsModulation:
            return "mAsModulation";
        case PrivateTag.ThreeDRPeakReferenceTime:
            return "ThreeDRPeakReferenceTime";
        case PrivateTag.ECGFrameTimeVector:
            return "ECGFrameTimeVector";
        case PrivateTag.ECGStartTimeOfRun:
            return "ECGStartTimeOfRun";
        case PrivateTag.GammaLUTDescriptor:
            return "GammaLUTDescriptor";
        case PrivateTag.GammaLUTType:
            return "GammaLUTType";
        case PrivateTag.GammaLUTData:
            return "GammaLUTData";
        case PrivateTag.GlobalGain:
            return "GlobalGain";
        case PrivateTag.GlobalOffset:
            return "GlobalOffset";
        case PrivateTag.DIPPMode:
            return "DIPPMode";
        case PrivateTag.ArtisSystemType:
            return "ArtisSystemType";
        case PrivateTag.ArtisTableType:
            return "ArtisTableType";
        case PrivateTag.ArtisTableTopType:
            return "ArtisTableTopType";
        case PrivateTag.WaterValue:
            return "WaterValue";
        case PrivateTag.ThreeDPositionerPrimaryStartAngle:
            return "ThreeDPositionerPrimaryStartAngle";
        case PrivateTag.ThreeDPositionerSecondaryStartAngle:
            return "ThreeDPositionerSecondaryStartAngle";
        case PrivateTag.StandPosition:
            return "StandPosition";
        case PrivateTag.RotationAngle:
            return "RotationAngle";
        case PrivateTag.ImageRotation:
            return "ImageRotation";
        case PrivateTag.TableCoordinates:
            return "TableCoordinates";
        case PrivateTag.IsocenterTablePosition:
            return "IsocenterTablePosition";
        case PrivateTag.TableObjectDistance:
            return "TableObjectDistance";
        case PrivateTag.CarmCoordinateSystem:
            return "CarmCoordinateSystem";
        case PrivateTag.RobotAxes:
            return "RobotAxes";
        case PrivateTag.TableCoordinateSystem:
            return "TableCoordinateSystem";
        case PrivateTag.PatientCoordinateSystem:
            return "PatientCoordinateSystem";
        case PrivateTag.Angulation:
            return "Angulation";
        case PrivateTag.Orbital:
            return "Orbital";
        case PrivateTag.LargeVolumeOverlap:
            return "LargeVolumeOverlap";
        case PrivateTag.ReconstructionPreset:
            return "ReconstructionPreset";
        case PrivateTag.ThreeDStartAngle:
            return "ThreeDStartAngle";
        case PrivateTag.ThreeDPlannedAngle:
            return "ThreeDPlannedAngle";
        case PrivateTag.ThreeDRotationPlaneAlpha:
            return "ThreeDRotationPlaneAlpha";
        case PrivateTag.ThreeDRotationPlaneBeta:
            return "ThreeDRotationPlaneBeta";
        case PrivateTag.ThreeDFirstImageAngle:
            return "ThreeDFirstImageAngle";
        case PrivateTag.ThreeDTriggerAngle:
            return "ThreeDTriggerAngle";
        case PrivateTag.DetectorRotation:
            return "DetectorRotation";
        case PrivateTag.PhysicalDetectorRotation:
            return "PhysicalDetectorRotation";
        case PrivateTag.TableTilt:
            return "TableTilt";
        case PrivateTag.TableRotation:
            return "TableRotation";
        case PrivateTag.TableCradleTilt:
            return "TableCradleTilt";
        case PrivateTag.Crispy1Container:
            return "Crispy1Container";
        case PrivateTag.ThreeDCardiacTriggerSequence:
            return "ThreeDCardiacTriggerSequence";
        case PrivateTag.ThreeDFrameReferenceDateTime:
            return "ThreeDFrameReferenceDateTime";
        case PrivateTag.ThreeDCardiacTriggerDelayTime:
            return "ThreeDCardiacTriggerDelayTime";
        case PrivateTag.ThreeDRRIntervalTimeMeasured:
            return "ThreeDRRIntervalTimeMeasured";
        }
        return "";
    }

}
