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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_IMG_01;

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

            case PrivateTag.SensorRoiShape:
            case PrivateTag.Tube:
            case PrivateTag.ObjectBackMotion:
            case PrivateTag.PositionerAngleDisplayMode:
            case PrivateTag._0019_xx4C_:
            case PrivateTag.CalibrationObject:
            case PrivateTag.CalibrationExtended:
            case PrivateTag.SpatialChange:
            case PrivateTag.InconsistentFlag:
            case PrivateTag.HorizontalAndVerticalImageFlip:
            case PrivateTag.InternalLabelImage:
            case PrivateTag.Grid:
            case PrivateTag.ApplicableReviewMode:
            case PrivateTag.AutoInjectionEnabled:
            case PrivateTag.InjectionPhase:
            case PrivateTag.CanDownscan512:
            case PrivateTag.AcquisitionRegion:
            case PrivateTag.AcquisitionSUBMode:
            case PrivateTag.TableRotationStatusVector:
            case PrivateTag.PatientPositionPerImage:
            case PrivateTag.AcquisitionPlane:
                return VR.CS;

            case PrivateTag.CalibrationDate:
                return VR.DA;

            case PrivateTag.FOVDimensionDouble:
            case PrivateTag.MinSaturationDose:
            case PrivateTag.DetectorGain:
            case PrivateTag.PatientDoseLimit:
            case PrivateTag.PreprocImageRateMax:
            case PrivateTag.SensorRoixPosition:
            case PrivateTag.SensorRoiyPosition:
            case PrivateTag.SensorRoixSize:
            case PrivateTag.SensorRoiySize:
            case PrivateTag.NoiseSensitivity:
            case PrivateTag.SharpSensitivity:
            case PrivateTag.ContrastSensitivity:
            case PrivateTag.LagSensitivity:
            case PrivateTag.MinObjectSize:
            case PrivateTag.MaxObjectSize:
            case PrivateTag.MaxObjectSpeed:
            case PrivateTag.WindowTimeDuration:
            case PrivateTag.DefaultBrightnessContrast:
            case PrivateTag.UserBrightnessContrast:
            case PrivateTag._0019_xx67_:
            case PrivateTag._0019_xx68_:
            case PrivateTag._0019_xx69_:
            case PrivateTag._0019_xx7A_:
            case PrivateTag._0019_xx7B_:
            case PrivateTag._0019_xx7C_:
            case PrivateTag.ImageDose:
            case PrivateTag.CalibrationObjectSize:
            case PrivateTag.DetectorRotationAngle:
            case PrivateTag.Angle1Increment:
            case PrivateTag.Angle2Increment:
            case PrivateTag.Angle3Increment:
            case PrivateTag.SensorFeedback:
            case PrivateTag.LogLUTControlPoints:
            case PrivateTag.ExpLUTSUBControlPoints:
            case PrivateTag.ABDValue:
            case PrivateTag.SubtractionWindowCenter:
            case PrivateTag.SubtractionWindowWidth:
            case PrivateTag.ImageRotation:
            case PrivateTag.InjectionDelay:
            case PrivateTag.InjectionDuration:
            case PrivateTag.EPT:
            case PrivateTag.BrightnessSensitivity:
            case PrivateTag.ExpLUTNOSUBControlPoints:
            case PrivateTag._0019_xxAF_:
            case PrivateTag._0019_xxB0_:
            case PrivateTag._0019_xxC2_:
            case PrivateTag._0019_xxDD_:
                return VR.DS;

            case PrivateTag.DistanceToTableTop:
            case PrivateTag.CalibrationFactor:
            case PrivateTag.CalibrationMagnificationRatio:
            case PrivateTag.DefaultMaskPixelShift:
            case PrivateTag._0019_xxB8_:
            case PrivateTag.TableCradleAngle:
            case PrivateTag.SourceToImageDistancePerFrameVector:
            case PrivateTag.TableRotationAngleIncrement:
            case PrivateTag.TableXPositionToIsocenterIncrement:
            case PrivateTag.TableYPositionToIsocenterIncrement:
            case PrivateTag.TableZPositionToIsocenterIncrement:
            case PrivateTag.TableHeadTiltAngleIncrement:
            case PrivateTag._0019_xxE0_:
            case PrivateTag.SourceToDetectorDistancePerFrameVector:
            case PrivateTag.TableRotationAngle:
            case PrivateTag.TableXPositionToIsocenter:
            case PrivateTag.TableYPositionToIsocenter:
            case PrivateTag.TableZPositionToIsocenter:
            case PrivateTag.TableHeadTiltAngle:
            case PrivateTag._0019_xxEF_:
                return VR.FL;

            case PrivateTag.DefaultSpatialFilterFamily:
            case PrivateTag.DefaultSpatialFilterStrength:
            case PrivateTag.DetectorOrigin:
            case PrivateTag.SourceSeriesNumber:
            case PrivateTag.SourceImageNumber:
            case PrivateTag.SourceFrameNumber:
            case PrivateTag.ECGDelayVector:
            case PrivateTag.CalibrationReturnCode:
            case PrivateTag.ReferenceInjectionFrameNumber:
            case PrivateTag.CurrentSpatialFilterStrength:
            case PrivateTag._0019_xxC4_:
                return VR.IS;

            case PrivateTag.ImageFileName:
            case PrivateTag.CalibrationSoftwareVersion:
            case PrivateTag.ExtendedCalibrationSoftwareVersion:
            case PrivateTag.AcquisitionModeDescription:
            case PrivateTag.AcquisitionModeDescriptionLabel:
            case PrivateTag._0019_xxB3_:
            case PrivateTag._0019_xxDC_:
                return VR.LO;

            case PrivateTag.CurveDataBeforeAcquisition:
            case PrivateTag.CurveDataTrigger:
                return VR.OW;

            case PrivateTag.ECGSynchronization:
            case PrivateTag.ECGDelayMode:
                return VR.SH;

            case PrivateTag.CalibrationTime:
                return VR.TM;

            case PrivateTag.SourceSeriesItemId:
            case PrivateTag.SourceImageItemId:
            case PrivateTag.SourceFrameItemId:
                return VR.UI;

            case PrivateTag.ExposureTrajectoryFamily:
                return VR.UL;

            case PrivateTag.DetectorSizeRows:
            case PrivateTag.DetectorSizeColumns:
            case PrivateTag.NumberOfPointsBeforeAcquisition:
            case PrivateTag.NumberOfPointsTrigger:
            case PrivateTag.CalibrationFrame:
            case PrivateTag.CalibrationAccuracy:
            case PrivateTag.CalibrationImageOriginal:
            case PrivateTag.CalibrationFrameOriginal:
            case PrivateTag.CalibrationNbPointsUif:
            case PrivateTag.CalibrationPointsRow:
            case PrivateTag.CalibrationPointsColumn:
                return VR.US;
        }
        return VR.UN;
    }

}
