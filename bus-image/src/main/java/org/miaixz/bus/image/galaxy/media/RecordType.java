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
package org.miaixz.bus.image.galaxy.media;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Defines the RecordType values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum RecordType {

    /**
     * Constant for the patient value.
     */
    PATIENT,
    /**
     * Constant for the study value.
     */
    STUDY,
    /**
     * Constant for the series value.
     */
    SERIES,
    /**
     * Constant for the image value.
     */
    IMAGE,
    /**
     * Constant for the overlay value.
     */
    OVERLAY,
    /**
     * Constant for the voi lut value.
     */
    VOI_LUT,
    /**
     * Constant for the curve value.
     */
    CURVE,
    /**
     * Constant for the stored print value.
     */
    STORED_PRINT,
    /**
     * Constant for the rt dose value.
     */
    RT_DOSE,
    /**
     * Constant for the rt structure set value.
     */
    RT_STRUCTURE_SET,
    /**
     * Constant for the rt plan value.
     */
    RT_PLAN,
    /**
     * Constant for the rt treat record value.
     */
    RT_TREAT_RECORD,
    /**
     * Constant for the presentation value.
     */
    PRESENTATION,
    /**
     * Constant for the waveform value.
     */
    WAVEFORM,
    /**
     * Constant for the sr document value.
     */
    SR_DOCUMENT,
    /**
     * Constant for the key object doc value.
     */
    KEY_OBJECT_DOC,
    /**
     * Constant for the spectroscopy value.
     */
    SPECTROSCOPY,
    /**
     * Constant for the raw data value.
     */
    RAW_DATA,
    /**
     * Constant for the registration value.
     */
    REGISTRATION,
    /**
     * Constant for the fiducial value.
     */
    FIDUCIAL,
    /**
     * Constant for the hanging protocol value.
     */
    HANGING_PROTOCOL,
    /**
     * Constant for the encap doc value.
     */
    ENCAP_DOC,
    /**
     * Constant for the hl7 struc doc value.
     */
    HL7_STRUC_DOC,
    /**
     * Constant for the value map value.
     */
    VALUE_MAP,
    /**
     * Constant for the stereometric value.
     */
    STEREOMETRIC,
    /**
     * Constant for the palette value.
     */
    PALETTE,
    /**
     * Constant for the implant value.
     */
    IMPLANT,
    /**
     * Constant for the implant assy value.
     */
    IMPLANT_ASSY,
    /**
     * Constant for the implant group value.
     */
    IMPLANT_GROUP,
    /**
     * Constant for the plan value.
     */
    PLAN,
    /**
     * Constant for the measurement value.
     */
    MEASUREMENT,
    /**
     * Constant for the surface value.
     */
    SURFACE,
    /**
     * Constant for the surface scan value.
     */
    SURFACE_SCAN,
    /**
     * Constant for the tract value.
     */
    TRACT,
    /**
     * Constant for the assessment value.
     */
    ASSESSMENT,
    /**
     * Constant for the private value.
     */
    PRIVATE;

    /**
     * Executes the for code operation.
     *
     * @param code the code.
     * @return the operation result.
     */
    public static RecordType forCode(String code) {
        try {
            return RecordType.valueOf(code.replace(Symbol.C_SPACE, Symbol.C_UNDERLINE));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(code);
        }
    }

    /**
     * Executes the code operation.
     *
     * @return the operation result.
     */
    public String code() {
        return name().replace(Symbol.C_UNDERLINE, Symbol.C_SPACE);
    }

}
