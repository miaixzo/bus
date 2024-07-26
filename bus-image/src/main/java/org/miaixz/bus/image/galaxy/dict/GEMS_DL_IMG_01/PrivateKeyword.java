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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_IMG_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.FOVDimensionDouble:
            return "FOVDimensionDouble";
        case PrivateTag.DistanceToTableTop:
            return "DistanceToTableTop";
        case PrivateTag.ImageFileName:
            return "ImageFileName";
        case PrivateTag.DefaultSpatialFilterFamily:
            return "DefaultSpatialFilterFamily";
        case PrivateTag.DefaultSpatialFilterStrength:
            return "DefaultSpatialFilterStrength";
        case PrivateTag.MinSaturationDose:
            return "MinSaturationDose";
        case PrivateTag.DetectorGain:
            return "DetectorGain";
        case PrivateTag.PatientDoseLimit:
            return "PatientDoseLimit";
        case PrivateTag.PreprocImageRateMax:
            return "PreprocImageRateMax";
        case PrivateTag.SensorRoiShape:
            return "SensorRoiShape";
        case PrivateTag.SensorRoixPosition:
            return "SensorRoixPosition";
        case PrivateTag.SensorRoiyPosition:
            return "SensorRoiyPosition";
        case PrivateTag.SensorRoixSize:
            return "SensorRoixSize";
        case PrivateTag.SensorRoiySize:
            return "SensorRoiySize";
        case PrivateTag.NoiseSensitivity:
            return "NoiseSensitivity";
        case PrivateTag.SharpSensitivity:
            return "SharpSensitivity";
        case PrivateTag.ContrastSensitivity:
            return "ContrastSensitivity";
        case PrivateTag.LagSensitivity:
            return "LagSensitivity";
        case PrivateTag.Tube:
            return "Tube";
        case PrivateTag.DetectorSizeRows:
            return "DetectorSizeRows";
        case PrivateTag.DetectorSizeColumns:
            return "DetectorSizeColumns";
        case PrivateTag.MinObjectSize:
            return "MinObjectSize";
        case PrivateTag.MaxObjectSize:
            return "MaxObjectSize";
        case PrivateTag.MaxObjectSpeed:
            return "MaxObjectSpeed";
        case PrivateTag.ObjectBackMotion:
            return "ObjectBackMotion";
        case PrivateTag.ExposureTrajectoryFamily:
            return "ExposureTrajectoryFamily";
        case PrivateTag.WindowTimeDuration:
            return "WindowTimeDuration";
        case PrivateTag.PositionerAngleDisplayMode:
            return "PositionerAngleDisplayMode";
        case PrivateTag.DetectorOrigin:
            return "DetectorOrigin";
        case PrivateTag._0019_xx4C_:
            return "_0019_xx4C_";
        case PrivateTag.DefaultBrightnessContrast:
            return "DefaultBrightnessContrast";
        case PrivateTag.UserBrightnessContrast:
            return "UserBrightnessContrast";
        case PrivateTag.SourceSeriesNumber:
            return "SourceSeriesNumber";
        case PrivateTag.SourceImageNumber:
            return "SourceImageNumber";
        case PrivateTag.SourceFrameNumber:
            return "SourceFrameNumber";
        case PrivateTag.SourceSeriesItemId:
            return "SourceSeriesItemId";
        case PrivateTag.SourceImageItemId:
            return "SourceImageItemId";
        case PrivateTag.SourceFrameItemId:
            return "SourceFrameItemId";
        case PrivateTag.NumberOfPointsBeforeAcquisition:
            return "NumberOfPointsBeforeAcquisition";
        case PrivateTag.CurveDataBeforeAcquisition:
            return "CurveDataBeforeAcquisition";
        case PrivateTag.NumberOfPointsTrigger:
            return "NumberOfPointsTrigger";
        case PrivateTag.CurveDataTrigger:
            return "CurveDataTrigger";
        case PrivateTag.ECGSynchronization:
            return "ECGSynchronization";
        case PrivateTag.ECGDelayMode:
            return "ECGDelayMode";
        case PrivateTag.ECGDelayVector:
            return "ECGDelayVector";
        case PrivateTag._0019_xx67_:
            return "_0019_xx67_";
        case PrivateTag._0019_xx68_:
            return "_0019_xx68_";
        case PrivateTag._0019_xx69_:
            return "_0019_xx69_";
        case PrivateTag._0019_xx7A_:
            return "_0019_xx7A_";
        case PrivateTag._0019_xx7B_:
            return "_0019_xx7B_";
        case PrivateTag._0019_xx7C_:
            return "_0019_xx7C_";
        case PrivateTag.ImageDose:
            return "ImageDose";
        case PrivateTag.CalibrationFrame:
            return "CalibrationFrame";
        case PrivateTag.CalibrationObject:
            return "CalibrationObject";
        case PrivateTag.CalibrationObjectSize:
            return "CalibrationObjectSize";
        case PrivateTag.CalibrationFactor:
            return "CalibrationFactor";
        case PrivateTag.CalibrationDate:
            return "CalibrationDate";
        case PrivateTag.CalibrationTime:
            return "CalibrationTime";
        case PrivateTag.CalibrationAccuracy:
            return "CalibrationAccuracy";
        case PrivateTag.CalibrationExtended:
            return "CalibrationExtended";
        case PrivateTag.CalibrationImageOriginal:
            return "CalibrationImageOriginal";
        case PrivateTag.CalibrationFrameOriginal:
            return "CalibrationFrameOriginal";
        case PrivateTag.CalibrationNbPointsUif:
            return "CalibrationNbPointsUif";
        case PrivateTag.CalibrationPointsRow:
            return "CalibrationPointsRow";
        case PrivateTag.CalibrationPointsColumn:
            return "CalibrationPointsColumn";
        case PrivateTag.CalibrationMagnificationRatio:
            return "CalibrationMagnificationRatio";
        case PrivateTag.CalibrationSoftwareVersion:
            return "CalibrationSoftwareVersion";
        case PrivateTag.ExtendedCalibrationSoftwareVersion:
            return "ExtendedCalibrationSoftwareVersion";
        case PrivateTag.CalibrationReturnCode:
            return "CalibrationReturnCode";
        case PrivateTag.DetectorRotationAngle:
            return "DetectorRotationAngle";
        case PrivateTag.SpatialChange:
            return "SpatialChange";
        case PrivateTag.InconsistentFlag:
            return "InconsistentFlag";
        case PrivateTag.HorizontalAndVerticalImageFlip:
            return "HorizontalAndVerticalImageFlip";
        case PrivateTag.InternalLabelImage:
            return "InternalLabelImage";
        case PrivateTag.Angle1Increment:
            return "Angle1Increment";
        case PrivateTag.Angle2Increment:
            return "Angle2Increment";
        case PrivateTag.Angle3Increment:
            return "Angle3Increment";
        case PrivateTag.SensorFeedback:
            return "SensorFeedback";
        case PrivateTag.Grid:
            return "Grid";
        case PrivateTag.DefaultMaskPixelShift:
            return "DefaultMaskPixelShift";
        case PrivateTag.ApplicableReviewMode:
            return "ApplicableReviewMode";
        case PrivateTag.LogLUTControlPoints:
            return "LogLUTControlPoints";
        case PrivateTag.ExpLUTSUBControlPoints:
            return "ExpLUTSUBControlPoints";
        case PrivateTag.ABDValue:
            return "ABDValue";
        case PrivateTag.SubtractionWindowCenter:
            return "SubtractionWindowCenter";
        case PrivateTag.SubtractionWindowWidth:
            return "SubtractionWindowWidth";
        case PrivateTag.ImageRotation:
            return "ImageRotation";
        case PrivateTag.AutoInjectionEnabled:
            return "AutoInjectionEnabled";
        case PrivateTag.InjectionPhase:
            return "InjectionPhase";
        case PrivateTag.InjectionDelay:
            return "InjectionDelay";
        case PrivateTag.ReferenceInjectionFrameNumber:
            return "ReferenceInjectionFrameNumber";
        case PrivateTag.InjectionDuration:
            return "InjectionDuration";
        case PrivateTag.EPT:
            return "EPT";
        case PrivateTag.CanDownscan512:
            return "CanDownscan512";
        case PrivateTag.CurrentSpatialFilterStrength:
            return "CurrentSpatialFilterStrength";
        case PrivateTag.BrightnessSensitivity:
            return "BrightnessSensitivity";
        case PrivateTag.ExpLUTNOSUBControlPoints:
            return "ExpLUTNOSUBControlPoints";
        case PrivateTag._0019_xxAF_:
            return "_0019_xxAF_";
        case PrivateTag._0019_xxB0_:
            return "_0019_xxB0_";
        case PrivateTag.AcquisitionModeDescription:
            return "AcquisitionModeDescription";
        case PrivateTag.AcquisitionModeDescriptionLabel:
            return "AcquisitionModeDescriptionLabel";
        case PrivateTag._0019_xxB3_:
            return "_0019_xxB3_";
        case PrivateTag._0019_xxB8_:
            return "_0019_xxB8_";
        case PrivateTag.AcquisitionRegion:
            return "AcquisitionRegion";
        case PrivateTag.AcquisitionSUBMode:
            return "AcquisitionSUBMode";
        case PrivateTag.TableCradleAngle:
            return "TableCradleAngle";
        case PrivateTag.TableRotationStatusVector:
            return "TableRotationStatusVector";
        case PrivateTag.SourceToImageDistancePerFrameVector:
            return "SourceToImageDistancePerFrameVector";
        case PrivateTag._0019_xxC2_:
            return "_0019_xxC2_";
        case PrivateTag.TableRotationAngleIncrement:
            return "TableRotationAngleIncrement";
        case PrivateTag._0019_xxC4_:
            return "_0019_xxC4_";
        case PrivateTag.PatientPositionPerImage:
            return "PatientPositionPerImage";
        case PrivateTag.TableXPositionToIsocenterIncrement:
            return "TableXPositionToIsocenterIncrement";
        case PrivateTag.TableYPositionToIsocenterIncrement:
            return "TableYPositionToIsocenterIncrement";
        case PrivateTag.TableZPositionToIsocenterIncrement:
            return "TableZPositionToIsocenterIncrement";
        case PrivateTag.TableHeadTiltAngleIncrement:
            return "TableHeadTiltAngleIncrement";
        case PrivateTag._0019_xxDC_:
            return "_0019_xxDC_";
        case PrivateTag.AcquisitionPlane:
            return "AcquisitionPlane";
        case PrivateTag._0019_xxDD_:
            return "_0019_xxDD_";
        case PrivateTag._0019_xxE0_:
            return "_0019_xxE0_";
        case PrivateTag.SourceToDetectorDistancePerFrameVector:
            return "SourceToDetectorDistancePerFrameVector";
        case PrivateTag.TableRotationAngle:
            return "TableRotationAngle";
        case PrivateTag.TableXPositionToIsocenter:
            return "TableXPositionToIsocenter";
        case PrivateTag.TableYPositionToIsocenter:
            return "TableYPositionToIsocenter";
        case PrivateTag.TableZPositionToIsocenter:
            return "TableZPositionToIsocenter";
        case PrivateTag.TableHeadTiltAngle:
            return "TableHeadTiltAngle";
        case PrivateTag._0019_xxEF_:
            return "_0019_xxEF_";
        }
        return "";
    }

}
