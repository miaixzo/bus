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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * This class defines various DICOM codes related to the Scope of Accumulation as {@link Code} objects. These codes are
 * used to specify the scope over which certain accumulated values or measurements apply.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ScopeOfAccumulation {

    /**
     * Code for "Scope of Accumulation".
     */
    public static final Code CODE = new Code("113705", "DCM", null, "Scope of Accumulation");
    /**
     * Code for "Study" scope of accumulation.
     */
    public static final Code Study = new Code("113014", "DCM", null, "Study");
    /**
     * Code for "Series" scope of accumulation.
     */
    public static final Code Series = new Code("113015", "DCM", null, "Series");
    /**
     * Code for "Performed Procedure Step" scope of accumulation.
     */
    public static final Code PerformedProcedureStep = new Code("113016", "DCM", null, "Performed Procedure Step");
    /**
     * Code for "Procedure Step To This Point" scope of accumulation.
     */
    public static final Code ProcedureStepToThisPoint = new Code("113970", "DCM", null, "Procedure Step To This Point");
    /**
     * Code for "Irradiation Event" scope of accumulation.
     */
    public static final Code IrradiationEvent = new Code("113852", "DCM", null, "Irradiation Event");

}
