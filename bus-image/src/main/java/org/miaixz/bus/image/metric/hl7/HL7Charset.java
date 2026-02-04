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
package org.miaixz.bus.image.metric.hl7;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Charset {

    private static final Map<String, String> CHARSET_NAMES_MAP = new HashMap<>();

    /**
     * Extend/override mapping of field MSH-18-character to named charset specified by
     * <a href="http://www.hl7.eu/HL7v2x/v251/hl7v251tab0211.htm">HL7 table 0211 - Alternate character sets</a>.. For
     * example, {@code HL7Charset.setCharsetNameMapping("Windows-1252", "windows-1252")} associate proprietary field
     * MSH-18-character value {@code Windows-1252} with Windows-1252 (CP-1252) charset, containing characters Š/š and
     * Ž/ž not included in ISO-8859-1 (Latin-1), but used in Estonian and Finnish for transcribing foreign names.
     *
     * @param code        value field MSH-18-character
     * @param charsetName The name of the mapped charset
     * @throws IllegalCharsetNameException If the given {@code charsetName} is illegal
     * @throws IllegalArgumentException    If the given {@code charsetName} is null
     * @throws UnsupportedCharsetException If no support for the named charset is available in this instance of the Java
     *                                     virtual machine
     */
    public static void setCharsetNameMapping(String code, String charsetName) {
        if (!Charset.isSupported(charsetName))
            throw new UnsupportedCharsetException(charsetName);
        CHARSET_NAMES_MAP.put(code, charsetName);
    }

    /**
     * Reset mapping of field MSH-18-character to named charsets as specified by
     * <a href="http://www.hl7.eu/HL7v2x/v251/hl7v251tab0211.htm">HL7 table 0211 - Alternate character sets</a>.
     */
    public static void resetCharsetNameMappings() {
        CHARSET_NAMES_MAP.clear();
    }

    public static String toCharsetName(String code) {
        if (code == null)
            code = "";
        String value = CHARSET_NAMES_MAP.get(code);
        if (value != null)
            return value;
        switch (code) {
            case "8859/1":
                return "ISO-8859-1";

            case "8859/2":
                return "ISO-8859-2";

            case "8859/3":
                return "ISO-8859-3";

            case "8859/4":
                return "ISO-8859-4";

            case "8859/5":
                return "ISO-8859-5";

            case "8859/6":
                return "ISO-8859-6";

            case "8859/7":
                return "ISO-8859-7";

            case "8859/8":
                return "ISO-8859-8";

            case "8859/9":
                return "ISO-8859-9";

            case "ISO IR14":
                return "JIS_X0201";

            case "ISO IR87":
                return "x-JIS0208";

            case "ISO IR159":
                return "JIS_X0212-1990";

            case "GB 18030-2000":
                return "GB18030";

            case "KS X 1001":
                return "EUC-KR";

            case "CNS 11643-1992":
                return "TIS-620";

            case "UNICODE":
            case "UNICODE UTF-8":
                return "UTF-8";
        }
        return "US-ASCII";
    }

    public static String toDicomCharacterSetCode(String code) {
        if (code != null && !code.isEmpty())
            switch (code) {
                case "8859/1":
                    return "ISO_IR 100";

                case "8859/2":
                    return "ISO_IR 101";

                case "8859/3":
                    return "ISO_IR 109";

                case "8859/4":
                    return "ISO_IR 110";

                case "8859/5":
                    return "ISO_IR 144";

                case "8859/6":
                    return "ISO_IR 127";

                case "8859/7":
                    return "ISO_IR 126";

                case "8859/8":
                    return "ISO_IR 138";

                case "8859/9":
                    return "ISO_IR 148";

                case "ISO IR14":
                    return "ISO_IR 13";

                case "ISO IR87":
                    return "ISO 2022 IR 87";

                case "ISO IR159":
                    return "ISO 2022 IR 159";

                case "GB 18030-2000":
                    return "GB18030";

                case "KS X 1001":
                    return "ISO 2022 IR 149";

                case "CNS 11643-1992":
                    return "ISO_IR 166";

                case "UNICODE":
                case "UNICODE UTF-8":
                    return "ISO_IR 192";
            }
        return null;
    }

}
