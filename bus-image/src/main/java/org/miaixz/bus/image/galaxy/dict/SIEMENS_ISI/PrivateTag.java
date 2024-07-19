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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_ISI;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS ISI";

    /** (0003,xx08) VR=US VM=1 ISI Command Field */
    public static final int ISICommandField = 0x00030008;

    /** (0003,xx11) VR=US VM=1 Attach ID Application Code */
    public static final int AttachIDApplicationCode = 0x00030011;

    /** (0003,xx12) VR=UL VM=1 Attach ID Message Count */
    public static final int AttachIDMessageCount = 0x00030012;

    /** (0003,xx13) VR=DA VM=1 Attach ID Date */
    public static final int AttachIDDate = 0x00030013;

    /** (0003,xx14) VR=TM VM=1 Attach ID Time */
    public static final int AttachIDTime = 0x00030014;

    /** (0003,xx20) VR=US VM=1 Message Type */
    public static final int MessageType = 0x00030020;

    /** (0003,xx30) VR=DA VM=1 Max Waiting Date */
    public static final int MaxWaitingDate = 0x00030030;

    /** (0003,xx31) VR=TM VM=1 Max Waiting Time */
    public static final int MaxWaitingTime = 0x00030031;

    /** (0009,xx01) VR=UN VM=1 RIS Patient Info IMGEF */
    public static final int RISPatientInfoIMGEF = 0x00090001;

    /** (0011,xx03) VR=LO VM=1 Patient UID */
    public static final int PatientUID = 0x00110003;

    /** (0011,xx04) VR=LO VM=1 Patient ID */
    public static final int PatientID = 0x00110004;

    /** (0011,xx0A) VR=LO VM=1 Case ID */
    public static final int CaseID = 0x0011000A;

    /** (0011,xx22) VR=LO VM=1 Request ID */
    public static final int RequestID = 0x00110022;

    /** (0011,xx23) VR=LO VM=1 Examination UID */
    public static final int ExaminationUID = 0x00110023;

    /** (0011,xxA1) VR=DA VM=1 Patient Registration Date */
    public static final int PatientRegistrationDate = 0x001100A1;

    /** (0011,xxA2) VR=TM VM=1 Patient Registration Time */
    public static final int PatientRegistrationTime = 0x001100A2;

    /** (0011,xxB0) VR=LO VM=1 Patient Last Name */
    public static final int PatientLastName = 0x001100B0;

    /** (0011,xxB2) VR=LO VM=1 Patient First Name */
    public static final int PatientFirstName = 0x001100B2;

    /** (0011,xxB4) VR=LO VM=1 Patient Hospital Status */
    public static final int PatientHospitalStatus = 0x001100B4;

    /** (0011,xxBC) VR=TM VM=1 Current Location Time */
    public static final int CurrentLocationTime = 0x001100BC;

    /** (0011,xxC0) VR=LO VM=1 Patient Insurance Status */
    public static final int PatientInsuranceStatus = 0x001100C0;

    /** (0011,xxD0) VR=LO VM=1 Patient Billing Type */
    public static final int PatientBillingType = 0x001100D0;

    /** (0011,xxD2) VR=LO VM=1 Patient Billing Address */
    public static final int PatientBillingAddress = 0x001100D2;

    /** (0031,xx12) VR=LO VM=1 Examination Reason */
    public static final int ExaminationReason = 0x00310012;

    /** (0031,xx30) VR=DA VM=1 Requested Date */
    public static final int RequestedDate = 0x00310030;

    /** (0031,xx32) VR=TM VM=1 Worklist Request Start Time */
    public static final int WorklistRequestStartTime = 0x00310032;

    /** (0031,xx33) VR=TM VM=1 Worklist Request End Time */
    public static final int WorklistRequestEndTime = 0x00310033;

    /** (0031,xx4A) VR=TM VM=1 Requested Time */
    public static final int RequestedTime = 0x0031004A;

    /** (0031,xx80) VR=LO VM=1 Requested Location */
    public static final int RequestedLocation = 0x00310080;

    /** (0055,xx46) VR=LO VM=1 Current Ward */
    public static final int CurrentWard = 0x00550046;

    /** (0193,xx02) VR=DS VM=1 RIS Key */
    public static final int RISKey = 0x01930002;

    /** (0307,xx01) VR=UN VM=1 RIS Worklist IMGEF */
    public static final int RISWorklistIMGEF = 0x03070001;

    /** (0309,xx01) VR=UN VM=1 RIS Report IMGEF */
    public static final int RISReportIMGEF = 0x03090001;

    /** (4009,xx01) VR=LO VM=1 Report ID */
    public static final int ReportID = 0x40090001;

    /** (4009,xx20) VR=LO VM=1 Report Status */
    public static final int ReportStatus = 0x40090020;

    /** (4009,xx30) VR=DA VM=1 Report Creation Date */
    public static final int ReportCreationDate = 0x40090030;

    /** (4009,xx70) VR=LO VM=1 Report Approving Physician */
    public static final int ReportApprovingPhysician = 0x40090070;

    /** (4009,xxE0) VR=LO VM=1 Report Text */
    public static final int ReportText = 0x400900E0;

    /** (4009,xxE1) VR=LO VM=1 Report Author */
    public static final int ReportAuthor = 0x400900E1;

    /** (4009,xxE3) VR=LO VM=1 Reporting Radiologist */
    public static final int ReportingRadiologist = 0x400900E3;

}
