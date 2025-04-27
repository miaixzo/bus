/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
