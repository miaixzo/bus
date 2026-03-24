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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__ACQ_1_0;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag.CrispyXPIFilterValue:
            case PrivateTag.AcquisitionSceneTime:
            case PrivateTag.ThreeDPositionerPrimaryStartAngle:
            case PrivateTag.ThreeDPositionerSecondaryStartAngle:
            case PrivateTag.TableObjectDistance:
            case PrivateTag.DetectorRotation:
                return VR.DS;

            case PrivateTag.ThreeDRPeakReferenceTime:
            case PrivateTag.ThreeDFrameReferenceDateTime:
                return VR.DT;

            case PrivateTag.CarmCoordinateSystem:
            case PrivateTag.RobotAxes:
            case PrivateTag.TableCoordinateSystem:
            case PrivateTag.PatientCoordinateSystem:
                return VR.FL;

            case PrivateTag.ThreeDCardiacTriggerDelayTime:
            case PrivateTag.ThreeDRRIntervalTimeMeasured:
                return VR.FD;

            case PrivateTag.ThreeDCardiacPhaseCenter:
            case PrivateTag.ThreeDCardiacPhaseWidth:
            case PrivateTag.DDOKernelsize:
            case PrivateTag.mAsModulation:
                return VR.IS;

            case PrivateTag.GammaLUTType:
                return VR.LO;

            case PrivateTag.SHSTPAR:
            case PrivateTag.SHZOOM:
            case PrivateTag.SHCOLPAR:
            case PrivateTag.OrganProgramInfo:
            case PrivateTag.Crispy1Container:
                return VR.OB;

            case PrivateTag.CurrentTimeProduct:
            case PrivateTag.ImagerReceptorDose:
            case PrivateTag.SkinDosePercent:
            case PrivateTag.SkinDoseAccumulation:
            case PrivateTag.SkinDoseRate:
            case PrivateTag.DynaXRayInfo:
            case PrivateTag.SourcetoIsocenter:
            case PrivateTag.ECGIndexArray:
            case PrivateTag.TotalSceneTime:
            case PrivateTag.ECGFrameTimeVector:
            case PrivateTag.ECGStartTimeOfRun:
            case PrivateTag.ThreeDPlannedAngle:
            case PrivateTag.ThreeDRotationPlaneAlpha:
            case PrivateTag.ThreeDRotationPlaneBeta:
            case PrivateTag.ThreeDFirstImageAngle:
                return VR.SL;

            case PrivateTag.GammaLUTSequence:
            case PrivateTag.ThreeDCardiacTriggerSequence:
                return VR.SQ;

            case PrivateTag.PostBlankingCircle:
            case PrivateTag.DynaAngles:
            case PrivateTag.TotalSteps:
            case PrivateTag.DynaAngulationStep:
            case PrivateTag.StandPosition:
            case PrivateTag.RotationAngle:
            case PrivateTag.TableCoordinates:
            case PrivateTag.IsocenterTablePosition:
            case PrivateTag.Angulation:
            case PrivateTag.Orbital:
            case PrivateTag.LargeVolumeOverlap:
            case PrivateTag.ThreeDStartAngle:
            case PrivateTag.ThreeDTriggerAngle:
            case PrivateTag.PhysicalDetectorRotation:
            case PrivateTag.TableTilt:
            case PrivateTag.TableRotation:
            case PrivateTag.TableCradleTilt:
                return VR.SS;

            case PrivateTag.ImpacFilename:
            case PrivateTag.CopperFilter:
                return VR.UL;

            case PrivateTag.AcquisitionType:
            case PrivateTag.AcquisitionMode:
            case PrivateTag.FootswitchIndex:
            case PrivateTag.AcquisitionRoom:
            case PrivateTag.MeasuringField:
            case PrivateTag.ModalityLUTInputGamma:
            case PrivateTag.ModalityLUTOutputGamma:
            case PrivateTag.AcquisitionZoom:
            case PrivateTag.DDOValue:
            case PrivateTag.DRSingleFlag:
            case PrivateTag.PressureData:
            case PrivateTag.FDFlag:
            case PrivateTag.KFactor:
            case PrivateTag.EVE:
            case PrivateTag.RestoreFlag:
            case PrivateTag.StandMovementFlag:
            case PrivateTag.FDRows:
            case PrivateTag.FDColumns:
            case PrivateTag.TableMovementFlag:
            case PrivateTag.ICStentFlag:
            case PrivateTag.GammaLUTDescriptor:
            case PrivateTag.GammaLUTData:
            case PrivateTag.GlobalGain:
            case PrivateTag.GlobalOffset:
            case PrivateTag.DIPPMode:
            case PrivateTag.ArtisSystemType:
            case PrivateTag.ArtisTableType:
            case PrivateTag.ArtisTableTopType:
            case PrivateTag.WaterValue:
            case PrivateTag.ImageRotation:
            case PrivateTag.ReconstructionPreset:
                return VR.US;
        }
        return VR.UN;
    }

}
