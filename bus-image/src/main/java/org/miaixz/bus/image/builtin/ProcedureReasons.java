/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines various DICOM Procedure Reasons codes as {@link Code} objects. These codes specify reasons for the
 * discontinuation or failure of a procedure step.
 *
 * @author Kimi Liu
 * @since Java 17+
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
