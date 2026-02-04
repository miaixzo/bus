/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CM_VA0__CMS;

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

            case PrivateTag.StorageMode:
            case PrivateTag.MeasurementMode:
            case PrivateTag.CalculationMode:
            case PrivateTag.PatientOrientationSet1:
            case PrivateTag.PatientOrientationSet2:
            case PrivateTag.WindowStyle:
            case PrivateTag._0029_xx11_:
            case PrivateTag._0029_xx13_:
            case PrivateTag.PixelQualityCode:
            case PrivateTag.ArchiveCode:
            case PrivateTag.ExposureCode:
            case PrivateTag._0029_xx53_:
                return VR.CS;

            case PrivateTag.LastMoveDate:
            case PrivateTag.RegistrationDate:
            case PrivateTag.ModificationDate:
            case PrivateTag.PatientBirthdate:
                return VR.DA;

            case PrivateTag.NumberOfMeasurements:
            case PrivateTag.UsedPatientWeight:
            case PrivateTag.PatientWeight:
            case PrivateTag._0019_xx70_:
            case PrivateTag.FoV:
            case PrivateTag.ImageMagnificationFactor:
            case PrivateTag.ImageScrollOffset:
            case PrivateTag.ImagePosition:
            case PrivateTag.ImageNormal:
            case PrivateTag.ImageDistance:
            case PrivateTag.ImageRow:
            case PrivateTag.ImageColumn:
                return VR.DS;

            case PrivateTag.OrganCode:
            case PrivateTag.NetFrequency:
            case PrivateTag.NoiseLevel:
            case PrivateTag.NumberOfDataBytes:
            case PrivateTag.ImagePixelOffset:
            case PrivateTag.PixelQualityValue:
            case PrivateTag.SortCode:
                return VR.IS;

            case PrivateTag._0011_xx0A_:
            case PrivateTag._0011_xx22_:
            case PrivateTag.PatientId:
            case PrivateTag.PatientsMaidenName:
            case PrivateTag.ReferringPhysician:
            case PrivateTag.AdmittingDiagnosis:
            case PrivateTag.PatientSex:
            case PrivateTag.ProcedureDescription:
            case PrivateTag.PatientRestDirection:
            case PrivateTag.PatientPosition:
            case PrivateTag.ViewDirection:
            case PrivateTag._0013_xx50_:
            case PrivateTag._0013_xx51_:
            case PrivateTag._0013_xx52_:
            case PrivateTag._0013_xx53_:
            case PrivateTag._0013_xx54_:
            case PrivateTag._0013_xx55_:
            case PrivateTag._0013_xx56_:
            case PrivateTag._0019_xx80_:
            case PrivateTag.StudyName:
            case PrivateTag.Splash:
            case PrivateTag.ImageText:
            case PrivateTag.ImageGraphicsFormatCode:
            case PrivateTag.ImageGraphics:
                return VR.LO;

            case PrivateTag.BinaryData:
                return VR.OB;

            case PrivateTag.ModifyingPhysician:
            case PrivateTag.PatientName:
                return VR.PN;

            case PrivateTag.StudyType:
                return VR.SH;

            case PrivateTag.LastMoveTime:
            case PrivateTag.RegistrationTime:
            case PrivateTag.ModificationTime:
                return VR.TM;

            case PrivateTag.EvaluationMaskImage:
                return VR.UL;

            case PrivateTag.ImagePositioningHistoryMask:
                return VR.US;
        }
        return VR.UN;
    }

}
