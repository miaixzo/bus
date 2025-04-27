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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS CM VA0 CMS";

    /** (0009,xx00) VR=DS VM=1 Number of Measurements */
    public static final int NumberOfMeasurements = 0x00090000;

    /** (0009,xx10) VR=CS VM=1 Storage Mode */
    public static final int StorageMode = 0x00090010;

    /** (0009,xx12) VR=UL VM=1 Evaluation Mask - Image */
    public static final int EvaluationMaskImage = 0x00090012;

    /** (0009,xx26) VR=DA VM=1 Last Move Date */
    public static final int LastMoveDate = 0x00090026;

    /** (0009,xx27) VR=TM VM=1 Last Move Time */
    public static final int LastMoveTime = 0x00090027;

    /** (0011,xx0A) VR=LO VM=1 ? */
    public static final int _0011_xx0A_ = 0x0011000A;

    /** (0011,xx10) VR=DA VM=1 Registration Date */
    public static final int RegistrationDate = 0x00110010;

    /** (0011,xx11) VR=TM VM=1 Registration Time */
    public static final int RegistrationTime = 0x00110011;

    /** (0011,xx22) VR=LO VM=1 ? */
    public static final int _0011_xx22_ = 0x00110022;

    /** (0011,xx23) VR=DS VM=1 Used Patient Weight */
    public static final int UsedPatientWeight = 0x00110023;

    /** (0011,xx40) VR=IS VM=1 Organ Code */
    public static final int OrganCode = 0x00110040;

    /** (0013,xx00) VR=PN VM=1 Modifying Physician */
    public static final int ModifyingPhysician = 0x00130000;

    /** (0013,xx10) VR=DA VM=1 Modification Date */
    public static final int ModificationDate = 0x00130010;

    /** (0013,xx12) VR=TM VM=1 Modification Time */
    public static final int ModificationTime = 0x00130012;

    /** (0013,xx20) VR=PN VM=1 Patient Name */
    public static final int PatientName = 0x00130020;

    /** (0013,xx22) VR=LO VM=1 Patient Id */
    public static final int PatientId = 0x00130022;

    /** (0013,xx30) VR=DA VM=1 Patient Birthdate */
    public static final int PatientBirthdate = 0x00130030;

    /** (0013,xx31) VR=DS VM=1 Patient Weight */
    public static final int PatientWeight = 0x00130031;

    /** (0013,xx32) VR=LO VM=1 Patients Maiden Name */
    public static final int PatientsMaidenName = 0x00130032;

    /** (0013,xx33) VR=LO VM=1 Referring Physician */
    public static final int ReferringPhysician = 0x00130033;

    /** (0013,xx34) VR=LO VM=1 Admitting Diagnosis */
    public static final int AdmittingDiagnosis = 0x00130034;

    /** (0013,xx35) VR=LO VM=1 Patient Sex */
    public static final int PatientSex = 0x00130035;

    /** (0013,xx40) VR=LO VM=1 Procedure Description */
    public static final int ProcedureDescription = 0x00130040;

    /** (0013,xx42) VR=LO VM=1 Patient Rest Direction */
    public static final int PatientRestDirection = 0x00130042;

    /** (0013,xx44) VR=LO VM=1 Patient Position */
    public static final int PatientPosition = 0x00130044;

    /** (0013,xx46) VR=LO VM=1 View Direction */
    public static final int ViewDirection = 0x00130046;

    /** (0013,xx50) VR=LO VM=1 ? */
    public static final int _0013_xx50_ = 0x00130050;

    /** (0013,xx51) VR=LO VM=1 ? */
    public static final int _0013_xx51_ = 0x00130051;

    /** (0013,xx52) VR=LO VM=1 ? */
    public static final int _0013_xx52_ = 0x00130052;

    /** (0013,xx53) VR=LO VM=1 ? */
    public static final int _0013_xx53_ = 0x00130053;

    /** (0013,xx54) VR=LO VM=1 ? */
    public static final int _0013_xx54_ = 0x00130054;

    /** (0013,xx55) VR=LO VM=1 ? */
    public static final int _0013_xx55_ = 0x00130055;

    /** (0013,xx56) VR=LO VM=1 ? */
    public static final int _0013_xx56_ = 0x00130056;

    /** (0019,xx10) VR=IS VM=1 Net Frequency */
    public static final int NetFrequency = 0x00190010;

    /** (0019,xx20) VR=CS VM=1 Measurement Mode */
    public static final int MeasurementMode = 0x00190020;

    /** (0019,xx30) VR=CS VM=1 Calculation Mode */
    public static final int CalculationMode = 0x00190030;

    /** (0019,xx50) VR=IS VM=1 Noise Level */
    public static final int NoiseLevel = 0x00190050;

    /** (0019,xx60) VR=IS VM=1 Number of Data Bytes */
    public static final int NumberOfDataBytes = 0x00190060;

    /** (0019,xx70) VR=DS VM=1-n ? */
    public static final int _0019_xx70_ = 0x00190070;

    /** (0019,xx80) VR=LO VM=1 ? */
    public static final int _0019_xx80_ = 0x00190080;

    /** (0021,xx20) VR=DS VM=2 FoV */
    public static final int FoV = 0x00210020;

    /** (0021,xx22) VR=DS VM=1 Image Magnification Factor */
    public static final int ImageMagnificationFactor = 0x00210022;

    /** (0021,xx24) VR=DS VM=2 Image Scroll Offset */
    public static final int ImageScrollOffset = 0x00210024;

    /** (0021,xx26) VR=IS VM=1 Image Pixel Offset */
    public static final int ImagePixelOffset = 0x00210026;

    /** (0021,xx60) VR=DS VM=3 Image Position */
    public static final int ImagePosition = 0x00210060;

    /** (0021,xx61) VR=DS VM=3 Image Normal */
    public static final int ImageNormal = 0x00210061;

    /** (0021,xx63) VR=DS VM=1 Image Distance */
    public static final int ImageDistance = 0x00210063;

    /** (0021,xx65) VR=US VM=1 Image Positioning History Mask */
    public static final int ImagePositioningHistoryMask = 0x00210065;

    /** (0021,xx6A) VR=DS VM=3 Image Row */
    public static final int ImageRow = 0x0021006A;

    /** (0021,xx6B) VR=DS VM=3 Image Column */
    public static final int ImageColumn = 0x0021006B;

    /** (0021,xx70) VR=CS VM=3 Patient Orientation Set1 */
    public static final int PatientOrientationSet1 = 0x00210070;

    /** (0021,xx71) VR=CS VM=3 Patient Orientation Set2 */
    public static final int PatientOrientationSet2 = 0x00210071;

    /** (0021,xx80) VR=LO VM=1 Study Name */
    public static final int StudyName = 0x00210080;

    /** (0021,xx82) VR=SH VM=3 Study Type */
    public static final int StudyType = 0x00210082;

    /** (0029,xx10) VR=CS VM=1 Window Style */
    public static final int WindowStyle = 0x00290010;

    /** (0029,xx11) VR=CS VM=1 ? */
    public static final int _0029_xx11_ = 0x00290011;

    /** (0029,xx13) VR=CS VM=1 ? */
    public static final int _0029_xx13_ = 0x00290013;

    /** (0029,xx20) VR=CS VM=3 Pixel Quality Code */
    public static final int PixelQualityCode = 0x00290020;

    /** (0029,xx22) VR=IS VM=3 Pixel Quality Value */
    public static final int PixelQualityValue = 0x00290022;

    /** (0029,xx50) VR=CS VM=1 Archive Code */
    public static final int ArchiveCode = 0x00290050;

    /** (0029,xx51) VR=CS VM=1 Exposure Code */
    public static final int ExposureCode = 0x00290051;

    /** (0029,xx52) VR=IS VM=1 Sort Code */
    public static final int SortCode = 0x00290052;

    /** (0029,xx53) VR=CS VM=1 ? */
    public static final int _0029_xx53_ = 0x00290053;

    /** (0029,xx60) VR=LO VM=1 Splash */
    public static final int Splash = 0x00290060;

    /** (0051,xx10) VR=LO VM=1-n Image Text */
    public static final int ImageText = 0x00510010;

    /** (6021,xx00) VR=LO VM=1 Image Graphics Format Code */
    public static final int ImageGraphicsFormatCode = 0x60210000;

    /** (6021,xx10) VR=LO VM=1 Image Graphics */
    public static final int ImageGraphics = 0x60210010;

    /** (7FE1,xx00) VR=OB VM=1-n Binary Data */
    public static final int BinaryData = 0x7FE10000;

}
