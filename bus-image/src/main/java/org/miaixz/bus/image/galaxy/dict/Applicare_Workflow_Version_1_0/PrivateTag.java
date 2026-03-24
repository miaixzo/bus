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
package org.miaixz.bus.image.galaxy.dict.Applicare_Workflow_Version_1_0;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Applicare/Workflow/Version 1.0";

    /** (3113,xx01) VR=CS VM=1 Order Control */
    public static final int OrderControl = 0x31130001;

    /** (3113,xx10) VR=SH VM=1 Scheduled Action Item Code Value */
    public static final int ScheduledActionItemCodeValue = 0x31130010;

    /**
     * (3113,xx11) VR=SH VM=1 Scheduled Action Item Coding Scheme Designator
     */
    public static final int ScheduledActionItemCodingSchemeDesignator = 0x31130011;

    /** (3113,xx12) VR=LO VM=1 Scheduled Action Item Code Meaning */
    public static final int ScheduledActionItemCodeMeaning = 0x31130012;

    /** (3113,xx15) VR=SH VM=1 Requested Action Item Code Value */
    public static final int RequestedActionItemCodeValue = 0x31130015;

    /**
     * (3113,xx16) VR=SH VM=1 Requested Action Item Coding Scheme Designator
     */
    public static final int RequestedActionItemCodingSchemeDesignator = 0x31130016;

    /** (3113,xx17) VR=LO VM=1 Requested Action Item Code Meaning */
    public static final int RequestedActionItemCodeMeaning = 0x31130017;

    /** (3113,xx20) VR=SH VM=1 Performed Action Item Code Value */
    public static final int PerformedActionItemCodeValue = 0x31130020;

    /**
     * (3113,xx21) VR=SH VM=1 Performed Action Item Coding Scheme Designator
     */
    public static final int PerformedActionItemCodingSchemeDesignator = 0x31130021;

    /** (3113,xx22) VR=LO VM=1 Performed Action Item Code Meaning */
    public static final int PerformedActionItemCodeMeaning = 0x31130022;

    /** (3113,xx25) VR=SH VM=1 Performed Procedure Code Value */
    public static final int PerformedProcedureCodeValue = 0x31130025;

    /**
     * (3113,xx26) VR=SH VM=1 Performed Procedure Coding Scheme Designator
     */
    public static final int PerformedProcedureCodingSchemeDesignator = 0x31130026;

    /** (3113,xx27) VR=LO VM=1 Performed Procedure Code Meaning */
    public static final int PerformedProcedureCodeMeaning = 0x31130027;

    /** (3113,xx30) VR=UI VM=1 Referenced Image SOP Class UID */
    public static final int ReferencedImageSOPClassUID = 0x31130030;

    /** (3113,xx31) VR=UI VM=1 Referenced Image SOP Instance UID */
    public static final int ReferencedImageSOPInstanceUID = 0x31130031;

    /** (3113,xxE0) VR=CS VM=1 Locked By Hostname */
    public static final int LockedByHostname = 0x311300E0;

    /** (3113,xxE1) VR=CS VM=1 Locked By User */
    public static final int LockedByUser = 0x311300E1;

    /** (3113,xxE2) VR=CS VM=1 KfEdit Lock User */
    public static final int KfEditLockUser = 0x311300E2;

}
