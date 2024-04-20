/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.galaxy.media;

import org.miaixz.bus.core.lang.Symbol;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum RecordType {
    PATIENT,
    STUDY,
    SERIES,
    IMAGE,
    OVERLAY,
    VOI_LUT,
    CURVE,
    STORED_PRINT,
    RT_DOSE,
    RT_STRUCTURE_SET,
    RT_PLAN,
    RT_TREAT_RECORD,
    PRESENTATION,
    WAVEFORM,
    SR_DOCUMENT,
    KEY_OBJECT_DOC,
    SPECTROSCOPY,
    RAW_DATA,
    REGISTRATION,
    FIDUCIAL,
    HANGING_PROTOCOL,
    ENCAP_DOC,
    HL7_STRUC_DOC,
    VALUE_MAP,
    STEREOMETRIC,
    PALETTE,
    IMPLANT,
    IMPLANT_ASSY,
    IMPLANT_GROUP,
    PLAN,
    MEASUREMENT,
    SURFACE,
    SURFACE_SCAN,
    TRACT,
    ASSESSMENT,
    PRIVATE;

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
