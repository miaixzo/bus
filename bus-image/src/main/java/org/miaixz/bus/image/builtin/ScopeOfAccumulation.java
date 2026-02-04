/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
