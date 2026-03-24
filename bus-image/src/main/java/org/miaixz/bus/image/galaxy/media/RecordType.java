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
 * @author Kimi Liu
 * @since Java 21+
 */
public enum RecordType {

    PATIENT, STUDY, SERIES, IMAGE, OVERLAY, VOI_LUT, CURVE, STORED_PRINT, RT_DOSE, RT_STRUCTURE_SET, RT_PLAN,
    RT_TREAT_RECORD, PRESENTATION, WAVEFORM, SR_DOCUMENT, KEY_OBJECT_DOC, SPECTROSCOPY, RAW_DATA, REGISTRATION,
    FIDUCIAL, HANGING_PROTOCOL, ENCAP_DOC, HL7_STRUC_DOC, VALUE_MAP, STEREOMETRIC, PALETTE, IMPLANT, IMPLANT_ASSY,
    IMPLANT_GROUP, PLAN, MEASUREMENT, SURFACE, SURFACE_SCAN, TRACT, ASSESSMENT, PRIVATE;

    public static RecordType forCode(String code) {
        try {
            return RecordType.valueOf(code.replace(Symbol.C_SPACE, Symbol.C_UNDERLINE));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(code);
        }
    }

    public String code() {
        return name().replace(Symbol.C_UNDERLINE, Symbol.C_SPACE);
    }

}
