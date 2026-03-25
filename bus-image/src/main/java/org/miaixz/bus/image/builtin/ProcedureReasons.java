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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines various DICOM Procedure Reasons codes as {@link Code} objects. These codes specify reasons for the
 * discontinuation or failure of a procedure step.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProcedureReasons {

    /**
     * Code for Equipment failure as a reason for procedure discontinuation.
     */
    public static final Code EquipmentFailure = new Code("110501", "DCM", null, "Equipment failure");
    /**
     * Code for Duplicate order as a reason for procedure discontinuation.
     */
    public static final Code DuplicateOrder = new Code("110510", "DCM", null, "Duplicate order");
    /**
     * Code for Discontinued for unspecified reason as a reason for procedure discontinuation.
     */
    public static final Code DiscontinuedForUnspecifiedReason = new Code("110513", "DCM", null,
            "Discontinued for unspecified reason");
    /**
     * Code for Incorrect worklist entry selected as a reason for procedure discontinuation.
     */
    public static final Code IncorrectWorklistEntrySelected = new Code("110514", "DCM", null,
            "Incorrect worklist entry selected");
    /**
     * Code for Objects incorrectly formatted as a reason for procedure discontinuation.
     */
    public static final Code ObjectsIncorrectlyFormatted = new Code("110521", "DCM", null,
            "Objects incorrectly formatted");
    /**
     * Code for Object Types not supported as a reason for procedure discontinuation.
     */
    public static final Code ObjectTypesNotSupported = new Code("110522", "DCM", null, "Object Types not supported");
    /**
     * Code for Object Set incomplete as a reason for procedure discontinuation.
     */
    public static final Code ObjectSetIncomplete = new Code("110523", "DCM", null, "Object Set incomplete");
    /**
     * Code for Media Failure as a reason for procedure discontinuation.
     */
    public static final Code MediaFailure = new Code("110524", "DCM", null, "Media Failure");
    /**
     * Code for Resource pre-empted as a reason for procedure discontinuation.
     */
    public static final Code ResourcePreEmpted = new Code("110526", "DCM", null, "Resource pre-empted");
    /**
     * Code for Resource inadequate as a reason for procedure discontinuation.
     */
    public static final Code ResourceInadequate = new Code("110527", "DCM", null, "Resource inadequate");
    /**
     * Code for Discontinued Procedure Step rescheduled as a reason for procedure discontinuation.
     */
    public static final Code DiscontinuedProcedureStepRescheduled = new Code("110528", "DCM", null,
            "Discontinued Procedure Step rescheduled");
    /**
     * Code for Discontinued Procedure Step rescheduling recommended as a reason for procedure discontinuation.
     */
    public static final Code DiscontinuedProcedureStepReschedulingRecommended = new Code("110529", "DCM", null,
            "Discontinued Procedure Step rescheduling recommended");
    /**
     * Code for Workitem assignment rejected by assigned resource as a reason for procedure discontinuation.
     */
    public static final Code WorkitemAssignmentRejectedByAssignedResource = new Code("110530", "DCM", null,
            "Workitem assignment rejected by assigned resource");
    /**
     * Code for Workitem expired as a reason for procedure discontinuation.
     */
    public static final Code WorkitemExpired = new Code("110533", "DCM", null, "Workitem expired");
    // TODO Include CID 9301 Modality PPS Discontinuation Reasons
    // TODO Include CID 60 Imaging Agent Administration Adverse Events

}
