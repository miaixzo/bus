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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ProcedureReasons {

    public static final Code EquipmentFailure = new Code("110501", "DCM", null, "Equipment failure");
    public static final Code DuplicateOrder = new Code("110510", "DCM", null, "Duplicate order");
    public static final Code DiscontinuedForUnspecifiedReason = new Code("110513", "DCM", null,
            "Discontinued for unspecified reason");
    public static final Code IncorrectWorklistEntrySelected = new Code("110514", "DCM", null,
            "Incorrect worklist entry selected");
    public static final Code ObjectsIncorrectlyFormatted = new Code("110521", "DCM", null,
            "Objects incorrectly formatted");
    public static final Code ObjectTypesNotSupported = new Code("110522", "DCM", null, "Object Types not supported");
    public static final Code ObjectSetIncomplete = new Code("110523", "DCM", null, "Object Set incomplete");
    public static final Code MediaFailure = new Code("110524", "DCM", null, "Media Failure");
    public static final Code ResourcePreEmpted = new Code("110526", "DCM", null, "Resource pre-empted");
    public static final Code ResourceInadequate = new Code("110527", "DCM", null, "Resource inadequate");
    public static final Code DiscontinuedProcedureStepRescheduled = new Code("110528", "DCM", null,
            "Discontinued Procedure Step rescheduled");
    public static final Code DiscontinuedProcedureStepReschedulingRecommended = new Code("110529", "DCM", null,
            "Discontinued Procedure Step rescheduling recommended");
    public static final Code WorkitemAssignmentRejectedByAssignedResource = new Code("110530", "DCM", null,
            "Workitem assignment rejected by assigned resource");
    public static final Code WorkitemExpired = new Code("110533", "DCM", null, "Workitem expired");
    // TODO Include CID 9301 Modality PPS Discontinuation Reasons
    // TODO Include CID 60 Imaging Agent Administration Adverse Events

}
