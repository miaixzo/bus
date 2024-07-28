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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_WORKFLOW;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO WORKFLOW";

    /** (0031,xx10) VR=UI VM=1 Internal Patient UID */
    public static final int InternalPatientUID = 0x00310010;

    /** (0031,xx11) VR=SH VM=1 Patients Death Indicator */
    public static final int PatientsDeathIndicator = 0x00310011;

    /** (0031,xx12) VR=DA VM=1 Patients Death Date */
    public static final int PatientsDeathDate = 0x00310012;

    /** (0031,xx13) VR=TM VM=1 Patients Death Time */
    public static final int PatientsDeathTime = 0x00310013;

    /** (0031,xx14) VR=SH VM=1 VIP Indicator */
    public static final int VIPIndicator = 0x00310014;

    /** (0031,xx15) VR=US VM=1 Emergency Flag */
    public static final int EmergencyFlag = 0x00310015;

    /** (0031,xx20) VR=SH VM=1 Internal Visit UID */
    public static final int InternalVisitUID = 0x00310020;

    /** (0031,xx25) VR=SH VM=1 Internal ISR UID */
    public static final int InternalISRUID = 0x00310025;

    /** (0031,xx32) VR=SH VM=1 Control State */
    public static final int ControlState = 0x00310032;

    /** (0031,xx34) VR=US VM=1 Local Flag */
    public static final int LocalFlag = 0x00310034;

    /** (0031,xx36) VR=UI VM=1-n Referenced Studies */
    public static final int ReferencedStudies = 0x00310036;

    /** (0031,xx40) VR=LO VM=1 Workflow ID */
    public static final int WorkflowID = 0x00310040;

    /** (0031,xx41) VR=LO VM=1 Workflow Description */
    public static final int WorkflowDescription = 0x00310041;

    /** (0031,xx42) VR=LO VM=1 Workflow Control State */
    public static final int WorkflowControlState = 0x00310042;

    /** (0031,xx43) VR=US VM=1 Workflow Ad Hoc Flag */
    public static final int WorkflowAdHocFlag = 0x00310043;

    /** (0031,xx44) VR=US VM=1 Hybrid Flag */
    public static final int HybridFlag = 0x00310044;

    /** (0031,xx50) VR=LO VM=1 Workitem ID */
    public static final int WorkitemID = 0x00310050;

    /** (0031,xx51) VR=LO VM=1 Workitem Name */
    public static final int WorkitemName = 0x00310051;

    /** (0031,xx52) VR=LO VM=1 Workitem Type */
    public static final int WorkitemType = 0x00310052;

    /** (0031,xx53) VR=LO VM=1-n Workitem Roles */
    public static final int WorkitemRoles = 0x00310053;

    /** (0031,xx54) VR=LO VM=1 Workitem Description */
    public static final int WorkitemDescription = 0x00310054;

    /** (0031,xx55) VR=LO VM=1 Workitem Control State */
    public static final int WorkitemControlState = 0x00310055;

    /** (0031,xx56) VR=LO VM=1 Claiming User */
    public static final int ClaimingUser = 0x00310056;

    /** (0031,xx57) VR=LO VM=1 Claiming Host */
    public static final int ClaimingHost = 0x00310057;

    /** (0031,xx58) VR=LO VM=1 Taskflow ID */
    public static final int TaskflowID = 0x00310058;

    /** (0031,xx59) VR=LO VM=1 Taskflow Name */
    public static final int TaskflowName = 0x00310059;

    /** (0031,xx5A) VR=US VM=1 Failed Flag */
    public static final int FailedFlag = 0x0031005A;

    /** (0031,xx5B) VR=DT VM=1 Scheduled Time */
    public static final int ScheduledTime = 0x0031005B;

    /** (0031,xx5C) VR=US VM=1 Workitem Ad Hoc Flag */
    public static final int WorkitemAdHocFlag = 0x0031005C;

    /** (0031,xx5D) VR=US VM=1 Patient Update Pending Flag */
    public static final int PatientUpdatePendingFlag = 0x0031005D;

    /** (0031,xx5E) VR=US VM=1 Patient Mixup Flag */
    public static final int PatientMixupFlag = 0x0031005E;

    /** (0031,xx60) VR=LO VM=1 Client ID */
    public static final int ClientID = 0x00310060;

    /** (0031,xx61) VR=LO VM=1 Template ID */
    public static final int TemplateID = 0x00310061;

    /** (0031,xx81) VR=LO VM=1 Institution Name */
    public static final int InstitutionName = 0x00310081;

    /** (0031,xx82) VR=ST VM=1 Institution Address */
    public static final int InstitutionAddress = 0x00310082;

    /** (0031,xx83) VR=SQ VM=1 Institution Code Sequence */
    public static final int InstitutionCodeSequence = 0x00310083;

}
