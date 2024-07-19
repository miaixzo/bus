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
     * <a href="http://www.hl7.eu/HL7v2x/v251/hl7v251tab0211.htm">HL7 table 0211 - Alternate character sets</a>..
     * For example, {@code HL7Charset.setCharsetNameMapping("Windows-1252", "windows-1252")} associate
     * proprietary field MSH-18-character value {@code Windows-1252} with Windows-1252 (CP-1252) charset,
     * containing characters Š/š and Ž/ž not included in ISO-8859-1 (Latin-1), but used in Estonian and
     * Finnish for transcribing foreign names.
     *
     * @param code        value field MSH-18-character
     * @param charsetName The name of the mapped charset
     * @throws IllegalCharsetNameException If the given {@code charsetName} is illegal
     * @throws IllegalArgumentException    If the given {@code charsetName} is null
     * @throws UnsupportedCharsetException If no support for the named charset is available
     *                                     in this instance of the Java virtual machine
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
        if (code == null) code = "";
        String value = CHARSET_NAMES_MAP.get(code);
        if (value != null) return value;
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
