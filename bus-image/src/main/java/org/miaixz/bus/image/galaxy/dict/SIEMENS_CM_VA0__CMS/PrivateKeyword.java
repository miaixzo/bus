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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CM_VA0__CMS;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.NumberOfMeasurements:
                return "NumberOfMeasurements";

            case PrivateTag.StorageMode:
                return "StorageMode";

            case PrivateTag.EvaluationMaskImage:
                return "EvaluationMaskImage";

            case PrivateTag.LastMoveDate:
                return "LastMoveDate";

            case PrivateTag.LastMoveTime:
                return "LastMoveTime";

            case PrivateTag._0011_xx0A_:
                return "_0011_xx0A_";

            case PrivateTag.RegistrationDate:
                return "RegistrationDate";

            case PrivateTag.RegistrationTime:
                return "RegistrationTime";

            case PrivateTag._0011_xx22_:
                return "_0011_xx22_";

            case PrivateTag.UsedPatientWeight:
                return "UsedPatientWeight";

            case PrivateTag.OrganCode:
                return "OrganCode";

            case PrivateTag.ModifyingPhysician:
                return "ModifyingPhysician";

            case PrivateTag.ModificationDate:
                return "ModificationDate";

            case PrivateTag.ModificationTime:
                return "ModificationTime";

            case PrivateTag.PatientName:
                return "PatientName";

            case PrivateTag.PatientId:
                return "PatientId";

            case PrivateTag.PatientBirthdate:
                return "PatientBirthdate";

            case PrivateTag.PatientWeight:
                return "PatientWeight";

            case PrivateTag.PatientsMaidenName:
                return "PatientsMaidenName";

            case PrivateTag.ReferringPhysician:
                return "ReferringPhysician";

            case PrivateTag.AdmittingDiagnosis:
                return "AdmittingDiagnosis";

            case PrivateTag.PatientSex:
                return "PatientSex";

            case PrivateTag.ProcedureDescription:
                return "ProcedureDescription";

            case PrivateTag.PatientRestDirection:
                return "PatientRestDirection";

            case PrivateTag.PatientPosition:
                return "PatientPosition";

            case PrivateTag.ViewDirection:
                return "ViewDirection";

            case PrivateTag._0013_xx50_:
                return "_0013_xx50_";

            case PrivateTag._0013_xx51_:
                return "_0013_xx51_";

            case PrivateTag._0013_xx52_:
                return "_0013_xx52_";

            case PrivateTag._0013_xx53_:
                return "_0013_xx53_";

            case PrivateTag._0013_xx54_:
                return "_0013_xx54_";

            case PrivateTag._0013_xx55_:
                return "_0013_xx55_";

            case PrivateTag._0013_xx56_:
                return "_0013_xx56_";

            case PrivateTag.NetFrequency:
                return "NetFrequency";

            case PrivateTag.MeasurementMode:
                return "MeasurementMode";

            case PrivateTag.CalculationMode:
                return "CalculationMode";

            case PrivateTag.NoiseLevel:
                return "NoiseLevel";

            case PrivateTag.NumberOfDataBytes:
                return "NumberOfDataBytes";

            case PrivateTag._0019_xx70_:
                return "_0019_xx70_";

            case PrivateTag._0019_xx80_:
                return "_0019_xx80_";

            case PrivateTag.FoV:
                return "FoV";

            case PrivateTag.ImageMagnificationFactor:
                return "ImageMagnificationFactor";

            case PrivateTag.ImageScrollOffset:
                return "ImageScrollOffset";

            case PrivateTag.ImagePixelOffset:
                return "ImagePixelOffset";

            case PrivateTag.ImagePosition:
                return "ImagePosition";

            case PrivateTag.ImageNormal:
                return "ImageNormal";

            case PrivateTag.ImageDistance:
                return "ImageDistance";

            case PrivateTag.ImagePositioningHistoryMask:
                return "ImagePositioningHistoryMask";

            case PrivateTag.ImageRow:
                return "ImageRow";

            case PrivateTag.ImageColumn:
                return "ImageColumn";

            case PrivateTag.PatientOrientationSet1:
                return "PatientOrientationSet1";

            case PrivateTag.PatientOrientationSet2:
                return "PatientOrientationSet2";

            case PrivateTag.StudyName:
                return "StudyName";

            case PrivateTag.StudyType:
                return "StudyType";

            case PrivateTag.WindowStyle:
                return "WindowStyle";

            case PrivateTag._0029_xx11_:
                return "_0029_xx11_";

            case PrivateTag._0029_xx13_:
                return "_0029_xx13_";

            case PrivateTag.PixelQualityCode:
                return "PixelQualityCode";

            case PrivateTag.PixelQualityValue:
                return "PixelQualityValue";

            case PrivateTag.ArchiveCode:
                return "ArchiveCode";

            case PrivateTag.ExposureCode:
                return "ExposureCode";

            case PrivateTag.SortCode:
                return "SortCode";

            case PrivateTag._0029_xx53_:
                return "_0029_xx53_";

            case PrivateTag.Splash:
                return "Splash";

            case PrivateTag.ImageText:
                return "ImageText";

            case PrivateTag.ImageGraphicsFormatCode:
                return "ImageGraphicsFormatCode";

            case PrivateTag.ImageGraphics:
                return "ImageGraphics";

            case PrivateTag.BinaryData:
                return "BinaryData";
        }
        return "";
    }

}
