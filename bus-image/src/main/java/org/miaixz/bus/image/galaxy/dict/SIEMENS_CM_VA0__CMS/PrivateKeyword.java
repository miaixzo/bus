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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CM_VA0__CMS;

/**
 * @author Kimi Liu
 * @since Java 17+
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
