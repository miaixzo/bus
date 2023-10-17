/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.metric.internal.hl7;

import org.aoju.bus.core.lang.Charset;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class HL7Charset {

    public static String toCharsetName(String code) {
        if (null != code && !code.isEmpty())
            switch (code.charAt(code.length() - 1)) {
                case '0':
                    if (code.equals("GB 18030-2000"))
                        return "GB18030";
                    break;
                case '1':
                    if (code.equals("8859/1"))
                        return Charset.DEFAULT_ISO_8859_1;
                    if (code.equals("KS X 1001"))
                        return "EUC-KR";
                    break;
                case '2':
                    if (code.equals("8859/2"))
                        return "ISO-8859-2";
                    if (code.equals("CNS 11643-1992"))
                        return "TIS-620";
                    break;
                case '3':
                    if (code.equals("8859/3"))
                        return "ISO-8859-3";
                    break;
                case '4':
                    if (code.equals("8859/4"))
                        return "ISO-8859-4";
                    if (code.equals("ISO IR14"))
                        return "JIS_X0201";
                    break;
                case '5':
                    if (code.equals("8859/5"))
                        return "ISO-8859-5";
                    break;
                case '6':
                    if (code.equals("8859/6"))
                        return "ISO-8859-6";
                    break;
                case '7':
                    if (code.equals("8859/7"))
                        return "ISO-8859-7";
                    if (code.equals("ISO IR87"))
                        return "x-JIS0208";
                    break;
                case '8':
                    if (code.equals("8859/8"))
                        return "ISO-8859-8";
                    if (code.equals("UNICODE UTF-8"))
                        return Charset.DEFAULT_UTF_8;
                    break;
                case '9':
                    if (code.equals("8859/9"))
                        return "ISO-8859-9";
                    if (code.equals("ISO IR159"))
                        return "JIS_X0212-1990";
                    break;
                case 'E':
                    if (code.equals("UNICODE"))
                        return Charset.DEFAULT_UTF_8;
                    break;
            }
        return Charset.DEFAULT_US_ASCII;
    }

    public static String toCharacterSetCode(String code) {
        if (null != code && !code.isEmpty())
            switch (code.charAt(code.length() - 1)) {
                case '0':
                    if (code.equals("GB 18030-2000"))
                        return "GB18030";
                    break;
                case '1':
                    if (code.equals("8859/1"))
                        return "ISO_IR 100";
                    if (code.equals("KS X 1001"))
                        return "ISO 2022 IR 149";
                    break;
                case '2':
                    if (code.equals("8859/2"))
                        return "ISO_IR 101";
                    if (code.equals("CNS 11643-1992"))
                        return "ISO_IR 166";
                    break;
                case '3':
                    if (code.equals("8859/3"))
                        return "ISO_IR 109";
                    break;
                case '4':
                    if (code.equals("8859/4"))
                        return "ISO_IR 110";
                    if (code.equals("ISO IR14"))
                        return "ISO_IR 13";
                    break;
                case '5':
                    if (code.equals("8859/5"))
                        return "ISO_IR 144";
                    break;
                case '6':
                    if (code.equals("8859/6"))
                        return "ISO_IR 127";
                    break;
                case '7':
                    if (code.equals("8859/7"))
                        return "ISO_IR 126";
                    if (code.equals("ISO IR87"))
                        return "ISO 2022 IR 87";
                    break;
                case '8':
                    if (code.equals("8859/8"))
                        return "ISO_IR 138";
                    if (code.equals("UNICODE UTF-8"))
                        return "ISO_IR 192";
                    break;
                case '9':
                    if (code.equals("8859/9"))
                        return "ISO_IR 148";
                    if (code.equals("ISO IR159"))
                        return "ISO 2022 IR 159";
                    break;
                case 'E':
                    if (code.equals("UNICODE"))
                        return "ISO_IR 192";
                    break;
            }
        return null;
    }

}
