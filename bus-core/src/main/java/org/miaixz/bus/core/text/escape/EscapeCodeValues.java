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
package org.miaixz.bus.core.text.escape;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.translate.AggregateTranslator;
import org.miaixz.bus.core.text.translate.CharSequenceTranslator;
import org.miaixz.bus.core.text.translate.LookupTranslator;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HTML/XML/JSON等
 * ESCAPE信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EscapeCodeValues {

    /**
     * 用于转义Shell命令语言的转换器
     */
    public static CharSequenceTranslator ESCAPE_XSI;
    /**
     * 用于取消转义已转义的XSI值项的转换器
     */
    public static CharSequenceTranslator UNESCAPE_XSI = new XsiUnescaper();
    /**
     * 转义对象字符 转换器对象
     */
    public static CharSequenceTranslator ESCAPE_JAVA;
    /**
     * 取消转义为Java字符 转换器对象
     */
    public static CharSequenceTranslator UNESCAPE_JAVA;
    /**
     * 用于转义EcmaScript/JavaScript的转换器对象
     */
    public static CharSequenceTranslator ESCAPE_ECMASCRIPT;
    /**
     * 取消转义EcmaScript的转换器对象
     */
    public static CharSequenceTranslator UNESCAPE_ECMASCRIPT;
    /**
     * 用于转义Json的转换器对象
     */
    public static CharSequenceTranslator ESCAPE_JSON;
    /**
     * 用于取消转义Json的转换器对象
     */
    public static CharSequenceTranslator UNESCAPE_JSON;
    /**
     * 用于转义XML 1.0的转换器对象
     */
    public static CharSequenceTranslator ESCAPE_XML10;
    /**
     * 用于转义XML 1.1的转换器对象
     */
    public static CharSequenceTranslator ESCAPE_XML11;
    /**
     * 取消转义XML的转换器对象
     */
    public static CharSequenceTranslator UNESCAPE_XML;
    /**
     * 用于转义,HTML 3.0版本对象
     */
    public static CharSequenceTranslator ESCAPE_HTML3;
    /**
     * 用于取消转义,HTML 3.0版本对象
     */
    public static CharSequenceTranslator UNESCAPE_HTML3;
    /**
     * 用于转义,HTML 4.0版本对象
     */
    public static CharSequenceTranslator ESCAPE_HTML4;
    /**
     * 用于取消转义,HTML 4.0版本对象
     */
    public static CharSequenceTranslator UNESCAPE_HTML4;

    static {
        final Map<CharSequence, CharSequence> ISO8859_1_ESCAPE_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> JAVA_ESCAPE = new HashMap<>();
        final Map<CharSequence, CharSequence> UNESCAPE_JAVA_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> HTML40_ESCAPE_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> BASIC_ESCAPE_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> APOS_ESCAPE_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> ESCAPE_JAVA_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> ESCAPE_ECMASCRIPT_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> escapeJsonMap = new HashMap<>();
        final Map<CharSequence, CharSequence> ESCAPE_XML10_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> ESCAPE_XML11_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> ESCAPE_XSI_MAP = new HashMap<>();
        final Map<CharSequence, CharSequence> ISO8859_1_ESCAPE;
        final Map<CharSequence, CharSequence> ISO8859_1_UNESCAPE;
        final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_ESCAPE;
        final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_UNESCAPE;
        final Map<CharSequence, CharSequence> HTML40_EXTENDED_ESCAPE;
        final Map<CharSequence, CharSequence> HTML40_EXTENDED_UNESCAPE;
        final Map<CharSequence, CharSequence> BASIC_ESCAPE;
        final Map<CharSequence, CharSequence> BASIC_UNESCAPE;
        final Map<CharSequence, CharSequence> APOS_ESCAPE;
        final Map<CharSequence, CharSequence> APOS_UNESCAPE;

        ISO8859_1_ESCAPE_MAP.put("\u00A0", Symbol.HTML_NBSP);
        ISO8859_1_ESCAPE_MAP.put("\u00A1", "&iexcl;"); // inverted exclamation mark
        ISO8859_1_ESCAPE_MAP.put("\u00A2", "&cent;"); // cent sign
        ISO8859_1_ESCAPE_MAP.put("\u00A3", "&pound;"); // pound sign
        ISO8859_1_ESCAPE_MAP.put("\u00A4", "&curren;"); // currency sign
        ISO8859_1_ESCAPE_MAP.put("\u00A5", "&yen;"); // yen sign = yuan sign
        ISO8859_1_ESCAPE_MAP.put("\u00A6", "&brvbar;"); // broken bar = broken vertical bar
        ISO8859_1_ESCAPE_MAP.put("\u00A7", "&sect;"); // section sign
        ISO8859_1_ESCAPE_MAP.put("\u00A8", "&uml;"); // diaeresis = spacing diaeresis
        ISO8859_1_ESCAPE_MAP.put("\u00A9", "&copy;"); // © - copyright sign
        ISO8859_1_ESCAPE_MAP.put("\u00AA", "&ordf;"); // feminine ordinal indicator
        ISO8859_1_ESCAPE_MAP.put("\u00AB", "&laquo;"); // left-pointing double angle quotation mark = left pointing guillemet
        ISO8859_1_ESCAPE_MAP.put("\u00AC", "&not;"); // not sign
        ISO8859_1_ESCAPE_MAP.put("\u00AD", "&shy;"); // soft hyphen = discretionary hyphen
        ISO8859_1_ESCAPE_MAP.put("\u00AE", "&reg;"); // ® - registered trademark sign
        ISO8859_1_ESCAPE_MAP.put("\u00AF", "&macr;"); // macron = spacing macron = overline = APL overbar
        ISO8859_1_ESCAPE_MAP.put("\u00B0", "&deg;"); // degree sign
        ISO8859_1_ESCAPE_MAP.put("\u00B1", "&plusmn;"); // plus-minus sign = plus-or-minus sign
        ISO8859_1_ESCAPE_MAP.put("\u00B2", "&sup2;"); // superscript two = superscript digit two = squared
        ISO8859_1_ESCAPE_MAP.put("\u00B3", "&sup3;"); // superscript three = superscript digit three = cubed
        ISO8859_1_ESCAPE_MAP.put("\u00B4", "&acute;"); // acute accent = spacing acute
        ISO8859_1_ESCAPE_MAP.put("\u00B5", "&micro;"); // micro sign
        ISO8859_1_ESCAPE_MAP.put("\u00B6", "&para;"); // pilcrow sign = paragraph sign
        ISO8859_1_ESCAPE_MAP.put("\u00B7", "&middot;"); // middle dot = Georgian comma = Greek middle dot
        ISO8859_1_ESCAPE_MAP.put("\u00B8", "&cedil;"); // cedilla = spacing cedilla
        ISO8859_1_ESCAPE_MAP.put("\u00B9", "&sup1;"); // superscript one = superscript digit one
        ISO8859_1_ESCAPE_MAP.put("\u00BA", "&ordm;"); // masculine ordinal indicator
        ISO8859_1_ESCAPE_MAP.put("\u00BB", "&raquo;"); // right-pointing double angle quotation mark = right pointing guillemet
        ISO8859_1_ESCAPE_MAP.put("\u00BC", "&frac14;"); // vulgar fraction one quarter = fraction one quarter
        ISO8859_1_ESCAPE_MAP.put("\u00BD", "&frac12;"); // vulgar fraction one half = fraction one half
        ISO8859_1_ESCAPE_MAP.put("\u00BE", "&frac34;"); // vulgar fraction three quarters = fraction three quarters
        ISO8859_1_ESCAPE_MAP.put("\u00BF", "&iquest;"); // inverted question mark = turned question mark
        ISO8859_1_ESCAPE_MAP.put("\u00C0", "&Agrave;"); // À - uppercase A, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00C1", "&Aacute;"); // Á - uppercase A, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00C2", "&Acirc;"); // Â - uppercase A, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00C3", "&Atilde;"); // Ã - uppercase A, tilde
        ISO8859_1_ESCAPE_MAP.put("\u00C4", "&Auml;"); // Ä - uppercase A, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00C5", "&Aring;"); // Å - uppercase A, ring
        ISO8859_1_ESCAPE_MAP.put("\u00C6", "&AElig;"); // Æ - uppercase AE
        ISO8859_1_ESCAPE_MAP.put("\u00C7", "&Ccedil;"); // Ç - uppercase C, cedilla
        ISO8859_1_ESCAPE_MAP.put("\u00C8", "&Egrave;"); // È - uppercase E, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00C9", "&Eacute;"); // É - uppercase E, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00CA", "&Ecirc;"); // Ê - uppercase E, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00CB", "&Euml;"); // Ë - uppercase E, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00CC", "&Igrave;"); // Ì - uppercase I, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00CD", "&Iacute;"); // Í - uppercase I, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00CE", "&Icirc;"); // Î - uppercase I, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00CF", "&Iuml;"); // Ï - uppercase I, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00D0", "&ETH;"); // Ð - uppercase Eth, Icelandic
        ISO8859_1_ESCAPE_MAP.put("\u00D1", "&Ntilde;"); // Ñ - uppercase N, tilde
        ISO8859_1_ESCAPE_MAP.put("\u00D2", "&Ograve;"); // Ò - uppercase O, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00D3", "&Oacute;"); // Ó - uppercase O, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00D4", "&Ocirc;"); // Ô - uppercase O, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00D5", "&Otilde;"); // Õ - uppercase O, tilde
        ISO8859_1_ESCAPE_MAP.put("\u00D6", "&Ouml;"); // Ö - uppercase O, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00D7", "&times;"); // multiplication sign
        ISO8859_1_ESCAPE_MAP.put("\u00D8", "&Oslash;"); // Ø - uppercase O, slash
        ISO8859_1_ESCAPE_MAP.put("\u00D9", "&Ugrave;"); // Ù - uppercase U, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00DA", "&Uacute;"); // Ú - uppercase U, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00DB", "&Ucirc;"); // Û - uppercase U, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00DC", "&Uuml;"); // Ü - uppercase U, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00DD", "&Yacute;"); // Ý - uppercase Y, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00DE", "&THORN;"); // Þ - uppercase THORN, Icelandic
        ISO8859_1_ESCAPE_MAP.put("\u00DF", "&szlig;"); // ß - lowercase sharps, German
        ISO8859_1_ESCAPE_MAP.put("\u00E0", "&agrave;"); // à - lowercase a, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00E1", "&aacute;"); // á - lowercase a, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00E2", "&acirc;"); // â - lowercase a, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00E3", "&atilde;"); // ã - lowercase a, tilde
        ISO8859_1_ESCAPE_MAP.put("\u00E4", "&auml;"); // ä - lowercase a, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00E5", "&aring;"); // å - lowercase a, ring
        ISO8859_1_ESCAPE_MAP.put("\u00E6", "&aelig;"); // æ - lowercase ae
        ISO8859_1_ESCAPE_MAP.put("\u00E7", "&ccedil;"); // ç - lowercase c, cedilla
        ISO8859_1_ESCAPE_MAP.put("\u00E8", "&egrave;"); // è - lowercase e, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00E9", "&eacute;"); // é - lowercase e, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00EA", "&ecirc;"); // ê - lowercase e, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00EB", "&euml;"); // ë - lowercase e, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00EC", "&igrave;"); // ì - lowercase i, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00ED", "&iacute;"); // í - lowercase i, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00EE", "&icirc;"); // î - lowercase i, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00EF", "&iuml;"); // ï - lowercase i, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00F0", "&eth;"); // ð - lowercase eth, Icelandic
        ISO8859_1_ESCAPE_MAP.put("\u00F1", "&ntilde;"); // ñ - lowercase n, tilde
        ISO8859_1_ESCAPE_MAP.put("\u00F2", "&ograve;"); // ò - lowercase o, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00F3", "&oacute;"); // ó - lowercase o, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00F4", "&ocirc;"); // ô - lowercase o, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00F5", "&otilde;"); // õ - lowercase o, tilde
        ISO8859_1_ESCAPE_MAP.put("\u00F6", "&ouml;"); // ö - lowercase o, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00F7", "&divide;"); // division sign
        ISO8859_1_ESCAPE_MAP.put("\u00F8", "&oslash;"); // ø - lowercase o, slash
        ISO8859_1_ESCAPE_MAP.put("\u00F9", "&ugrave;"); // ù - lowercase u, grave accent
        ISO8859_1_ESCAPE_MAP.put("\u00FA", "&uacute;"); // ú - lowercase u, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00FB", "&ucirc;"); // û - lowercase u, circumflex accent
        ISO8859_1_ESCAPE_MAP.put("\u00FC", "&uuml;"); // ü - lowercase u, umlaut
        ISO8859_1_ESCAPE_MAP.put("\u00FD", "&yacute;"); // ý - lowercase y, acute accent
        ISO8859_1_ESCAPE_MAP.put("\u00FE", "&thorn;"); // þ - lowercase thorn, Icelandic
        ISO8859_1_ESCAPE_MAP.put("\u00FF", "&yuml;"); // ÿ - lowercase y, umlaut

        JAVA_ESCAPE.put("\b", "\\b");
        JAVA_ESCAPE.put(Symbol.LF, "\\n");
        JAVA_ESCAPE.put(Symbol.HT, "\\t");
        JAVA_ESCAPE.put("\f", "\\f");
        JAVA_ESCAPE.put(Symbol.CR, "\\r");

        UNESCAPE_JAVA_MAP.put("\\\\", Symbol.BACKSLASH);
        UNESCAPE_JAVA_MAP.put("\\\"", Symbol.DOUBLE_QUOTES);
        UNESCAPE_JAVA_MAP.put("\\'", Symbol.SINGLE_QUOTE);
        UNESCAPE_JAVA_MAP.put(Symbol.BACKSLASH, Normal.EMPTY);

        HTML40_ESCAPE_MAP.put("\u0192", "&fnof;"); // latin small f with hook = function= florin, U+0192 ISOtech
        HTML40_ESCAPE_MAP.put("\u0391", "&Alpha;"); // greek capital letter alpha, U+0391
        HTML40_ESCAPE_MAP.put("\u0392", "&Beta;"); // greek capital letter beta, U+0392
        HTML40_ESCAPE_MAP.put("\u0393", "&Gamma;"); // greek capital letter gamma,U+0393 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u0394", "&Delta;"); // greek capital letter delta,U+0394 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u0395", "&Epsilon;"); // greek capital letter epsilon, U+0395
        HTML40_ESCAPE_MAP.put("\u0396", "&Zeta;"); // greek capital letter zeta, U+0396
        HTML40_ESCAPE_MAP.put("\u0397", "&Eta;"); // greek capital letter eta, U+0397
        HTML40_ESCAPE_MAP.put("\u0398", "&Theta;"); // greek capital letter theta,U+0398 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u0399", "&Iota;"); // greek capital letter iota, U+0399
        HTML40_ESCAPE_MAP.put("\u039A", "&Kappa;"); // greek capital letter kappa, U+039A
        HTML40_ESCAPE_MAP.put("\u039B", "&Lambda;"); // greek capital letter lambda,U+039B ISOgrk3
        HTML40_ESCAPE_MAP.put("\u039C", "&Mu;"); // greek capital letter mu, U+039C
        HTML40_ESCAPE_MAP.put("\u039D", "&Nu;"); // greek capital letter nu, U+039D
        HTML40_ESCAPE_MAP.put("\u039E", "&Xi;"); // greek capital letter xi, U+039E ISOgrk3
        HTML40_ESCAPE_MAP.put("\u039F", "&Omicron;"); // greek capital letter omicron, U+039F
        HTML40_ESCAPE_MAP.put("\u03A0", "&Pi;"); // greek capital letter pi, U+03A0 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03A1", "&Rho;"); // greek capital letter rho, U+03A1
        HTML40_ESCAPE_MAP.put("\u03A3", "&Sigma;"); // greek capital letter sigma,U+03A3 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03A4", "&Tau;"); // greek capital letter tau, U+03A4
        HTML40_ESCAPE_MAP.put("\u03A5", "&Upsilon;"); // greek capital letter upsilon,U+03A5 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03A6", "&Phi;"); // greek capital letter phi,U+03A6 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03A7", "&Chi;"); // greek capital letter chi, U+03A7
        HTML40_ESCAPE_MAP.put("\u03A8", "&Psi;"); // greek capital letter psi,U+03A8 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03A9", "&Omega;"); // greek capital letter omega,U+03A9 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B1", "&alpha;"); // greek small letter alpha,U+03B1 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B2", "&beta;"); // greek small letter beta, U+03B2 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B3", "&gamma;"); // greek small letter gamma,U+03B3 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B4", "&delta;"); // greek small letter delta,U+03B4 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B5", "&epsilon;"); // greek small letter epsilon,U+03B5 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B6", "&zeta;"); // greek small letter zeta, U+03B6 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B7", "&eta;"); // greek small letter eta, U+03B7 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B8", "&theta;"); // greek small letter theta,U+03B8 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03B9", "&iota;"); // greek small letter iota, U+03B9 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03BA", "&kappa;"); // greek small letter kappa,U+03BA ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03BB", "&lambda;"); // greek small letter lambda,U+03BB ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03BC", "&mu;"); // greek small letter mu, U+03BC ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03BD", "&nu;"); // greek small letter nu, U+03BD ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03BE", "&xi;"); // greek small letter xi, U+03BE ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03BF", "&omicron;"); // greek small letter omicron, U+03BF NEW
        HTML40_ESCAPE_MAP.put("\u03C0", "&pi;"); // greek small letter pi, U+03C0 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C1", "&rho;"); // greek small letter rho, U+03C1 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C2", "&sigmaf;"); // greek small letter final sigma,U+03C2 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C3", "&sigma;"); // greek small letter sigma,U+03C3 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C4", "&tau;"); // greek small letter tau, U+03C4 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C5", "&upsilon;"); // greek small letter upsilon,U+03C5 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C6", "&phi;"); // greek small letter phi, U+03C6 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C7", "&chi;"); // greek small letter chi, U+03C7 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C8", "&psi;"); // greek small letter psi, U+03C8 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03C9", "&omega;"); // greek small letter omega,U+03C9 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u03D1", "&thetasym;"); // greek small letter theta symbol,U+03D1 NEW
        HTML40_ESCAPE_MAP.put("\u03D2", "&upsih;"); // greek upsilon with hook symbol,U+03D2 NEW
        HTML40_ESCAPE_MAP.put("\u03D6", "&piv;"); // greek pi symbol, U+03D6 ISOgrk3
        HTML40_ESCAPE_MAP.put("\u2022", "&bull;"); // bullet = black small circle,U+2022 ISOpub
        HTML40_ESCAPE_MAP.put("\u2026", "&hellip;"); // horizontal ellipsis = three dot leader,U+2026 ISOpub
        HTML40_ESCAPE_MAP.put("\u2032", "&prime;"); // prime = minutes = feet, U+2032 ISOtech
        HTML40_ESCAPE_MAP.put("\u2033", "&Prime;"); // double prime = seconds = inches,U+2033 ISOtech
        HTML40_ESCAPE_MAP.put("\u203E", "&oline;"); // overline = spacing overscore,U+203E NEW
        HTML40_ESCAPE_MAP.put("\u2044", "&frasl;"); // fraction slash, U+2044 NEW
        HTML40_ESCAPE_MAP.put("\u2118", "&weierp;"); // script capital P = power set= Weierstrass p, U+2118 ISOamso
        HTML40_ESCAPE_MAP.put("\u2111", "&image;"); // blackletter capital I = imaginary part,U+2111 ISOamso
        HTML40_ESCAPE_MAP.put("\u211C", "&real;"); // blackletter capital R = real part symbol,U+211C ISOamso
        HTML40_ESCAPE_MAP.put("\u2122", "&trade;"); // trade mark sign, U+2122 ISOnum
        HTML40_ESCAPE_MAP.put("\u2135", "&alefsym;"); // alef symbol = first transfinite cardinal,U+2135 NEW
        HTML40_ESCAPE_MAP.put("\u2190", "&larr;"); // leftwards arrow, U+2190 ISOnum
        HTML40_ESCAPE_MAP.put("\u2191", "&uarr;"); // upwards arrow, U+2191 ISOnum-->
        HTML40_ESCAPE_MAP.put("\u2192", "&rarr;"); // rightwards arrow, U+2192 ISOnum
        HTML40_ESCAPE_MAP.put("\u2193", "&darr;"); // downwards arrow, U+2193 ISOnum
        HTML40_ESCAPE_MAP.put("\u2194", "&harr;"); // left right arrow, U+2194 ISOamsa
        HTML40_ESCAPE_MAP.put("\u21B5", "&crarr;"); // downwards arrow with corner leftwards= carriage return, U+21B5 NEW
        HTML40_ESCAPE_MAP.put("\u21D0", "&lArr;"); // leftwards double arrow, U+21D0 ISOtech
        HTML40_ESCAPE_MAP.put("\u21D1", "&uArr;"); // upwards double arrow, U+21D1 ISOamsa
        HTML40_ESCAPE_MAP.put("\u21D2", "&rArr;"); // rightwards double arrow,U+21D2 ISOtech
        HTML40_ESCAPE_MAP.put("\u21D3", "&dArr;"); // downwards double arrow, U+21D3 ISOamsa
        HTML40_ESCAPE_MAP.put("\u21D4", "&hArr;"); // left right double arrow,U+21D4 ISOamsa
        HTML40_ESCAPE_MAP.put("\u2200", "&forall;"); // for all, U+2200 ISOtech
        HTML40_ESCAPE_MAP.put("\u2202", "&part;"); // partial differential, U+2202 ISOtech
        HTML40_ESCAPE_MAP.put("\u2203", "&exist;"); // there exists, U+2203 ISOtech
        HTML40_ESCAPE_MAP.put("\u2205", "&empty;"); // empty set = null set = diameter,U+2205 ISOamso
        HTML40_ESCAPE_MAP.put("\u2207", "&nabla;"); // nabla = backward difference,U+2207 ISOtech
        HTML40_ESCAPE_MAP.put("\u2208", "&isin;"); // element of, U+2208 ISOtech
        HTML40_ESCAPE_MAP.put("\u2209", "&notin;"); // not an element of, U+2209 ISOtech
        HTML40_ESCAPE_MAP.put("\u220B", "&ni;"); // contains as member, U+220B ISOtech
        HTML40_ESCAPE_MAP.put("\u220F", "&prod;"); // n-ary product = product sign,U+220F ISOamsb
        HTML40_ESCAPE_MAP.put("\u2211", "&sum;"); // n-ary summation, U+2211 ISOamsb
        HTML40_ESCAPE_MAP.put("\u2212", "&minus;"); // minus sign, U+2212 ISOtech
        HTML40_ESCAPE_MAP.put("\u2217", "&lowast;"); // asterisk operator, U+2217 ISOtech
        HTML40_ESCAPE_MAP.put("\u221A", "&radic;"); // square root = radical sign,U+221A ISOtech
        HTML40_ESCAPE_MAP.put("\u221D", "&prop;"); // proportional to, U+221D ISOtech
        HTML40_ESCAPE_MAP.put("\u221E", "&infin;"); // infinity, U+221E ISOtech
        HTML40_ESCAPE_MAP.put("\u2220", "&ang;"); // angle, U+2220 ISOamso
        HTML40_ESCAPE_MAP.put("\u2227", "&and;"); // logical and = wedge, U+2227 ISOtech
        HTML40_ESCAPE_MAP.put("\u2228", "&or;"); // logical or = vee, U+2228 ISOtech
        HTML40_ESCAPE_MAP.put("\u2229", "&cap;"); // intersection = cap, U+2229 ISOtech
        HTML40_ESCAPE_MAP.put("\u222A", "&cup;"); // union = cup, U+222A ISOtech
        HTML40_ESCAPE_MAP.put("\u222B", "&int;"); // integral, U+222B ISOtech
        HTML40_ESCAPE_MAP.put("\u2234", "&there4;"); // therefore, U+2234 ISOtech
        HTML40_ESCAPE_MAP.put("\u223C", "&sim;"); // tilde operator = varies with = similar to,U+223C ISOtech
        HTML40_ESCAPE_MAP.put("\u2245", "&cong;"); // approximately equal to, U+2245 ISOtech
        HTML40_ESCAPE_MAP.put("\u2248", "&asymp;"); // almost equal to = asymptotic to,U+2248 ISOamsr
        HTML40_ESCAPE_MAP.put("\u2260", "&ne;"); // not equal to, U+2260 ISOtech
        HTML40_ESCAPE_MAP.put("\u2261", "&equiv;"); // identical to, U+2261 ISOtech
        HTML40_ESCAPE_MAP.put("\u2264", "&le;"); // less-than or equal to, U+2264 ISOtech
        HTML40_ESCAPE_MAP.put("\u2265", "&ge;"); // greater-than or equal to,U+2265 ISOtech
        HTML40_ESCAPE_MAP.put("\u2282", "&sub;"); // subset of, U+2282 ISOtech
        HTML40_ESCAPE_MAP.put("\u2283", "&sup;"); // superset of, U+2283 ISOtech
        HTML40_ESCAPE_MAP.put("\u2284", "&nsub;"); // not a subset of, U+2284 ISOamsn
        HTML40_ESCAPE_MAP.put("\u2286", "&sube;"); // subset of or equal to, U+2286 ISOtech
        HTML40_ESCAPE_MAP.put("\u2287", "&supe;"); // superset of or equal to,U+2287 ISOtech
        HTML40_ESCAPE_MAP.put("\u2295", "&oplus;"); // circled plus = direct sum,U+2295 ISOamsb
        HTML40_ESCAPE_MAP.put("\u2297", "&otimes;"); // circled times = vector product,U+2297 ISOamsb
        HTML40_ESCAPE_MAP.put("\u22A5", "&perp;"); // up tack = orthogonal to = perpendicular,U+22A5 ISOtech
        HTML40_ESCAPE_MAP.put("\u22C5", "&sdot;"); // dot operator, U+22C5 ISOamsb
        HTML40_ESCAPE_MAP.put("\u2308", "&lceil;"); // left ceiling = apl upstile,U+2308 ISOamsc
        HTML40_ESCAPE_MAP.put("\u2309", "&rceil;"); // right ceiling, U+2309 ISOamsc
        HTML40_ESCAPE_MAP.put("\u230A", "&lfloor;"); // left floor = apl downstile,U+230A ISOamsc
        HTML40_ESCAPE_MAP.put("\u230B", "&rfloor;"); // right floor, U+230B ISOamsc
        HTML40_ESCAPE_MAP.put("\u2329", "&lang;"); // left-pointing angle bracket = bra,U+2329 ISOtech
        HTML40_ESCAPE_MAP.put("\u232A", "&rang;"); // right-pointing angle bracket = ket,U+232A ISOtech
        HTML40_ESCAPE_MAP.put("\u25CA", "&loz;"); // lozenge, U+25CA ISOpub
        HTML40_ESCAPE_MAP.put("\u2660", "&spades;"); // black spade suit, U+2660 ISOpub
        HTML40_ESCAPE_MAP.put("\u2663", "&clubs;"); // black club suit = shamrock,U+2663 ISOpub
        HTML40_ESCAPE_MAP.put("\u2665", "&hearts;"); // black heart suit = valentine,U+2665 ISOpub
        HTML40_ESCAPE_MAP.put("\u2666", "&diams;"); // black diamond suit, U+2666 ISOpub
        HTML40_ESCAPE_MAP.put("\u0152", "&OElig;"); // latin capital ligature OE,U+0152 ISOlat2
        HTML40_ESCAPE_MAP.put("\u0153", "&oelig;"); // latin small ligature oe, U+0153 ISOlat2
        HTML40_ESCAPE_MAP.put("\u0160", "&Scaron;"); // latin capital letter S with caron,U+0160 ISOlat2
        HTML40_ESCAPE_MAP.put("\u0161", "&scaron;"); // latin small letter s with caron,U+0161 ISOlat2
        HTML40_ESCAPE_MAP.put("\u0178", "&Yuml;"); // latin capital letter Y with diaeresis,U+0178 ISOlat2
        HTML40_ESCAPE_MAP.put("\u02C6", "&circ;"); // modifier letter circumflex accent,U+02C6 ISOpub
        HTML40_ESCAPE_MAP.put("\u02DC", "&tilde;"); // small tilde, U+02DC ISOdia
        HTML40_ESCAPE_MAP.put("\u2002", "&ensp;"); // en space, U+2002 ISOpub
        HTML40_ESCAPE_MAP.put("\u2003", "&emsp;"); // em space, U+2003 ISOpub
        HTML40_ESCAPE_MAP.put("\u2009", "&thinsp;"); // thin space, U+2009 ISOpub
        HTML40_ESCAPE_MAP.put("\u200C", "&zwnj;"); // zero width non-joiner,U+200C NEW RFC 2070
        HTML40_ESCAPE_MAP.put("\u200D", "&zwj;"); // zero width joiner, U+200D NEW RFC 2070
        HTML40_ESCAPE_MAP.put("\u200E", "&lrm;"); // left-to-right mark, U+200E NEW RFC 2070
        HTML40_ESCAPE_MAP.put("\u200F", "&rlm;"); // right-to-left mark, U+200F NEW RFC 2070
        HTML40_ESCAPE_MAP.put("\u2013", "&ndash;"); // en dash, U+2013 ISOpub
        HTML40_ESCAPE_MAP.put("\u2014", "&mdash;"); // em dash, U+2014 ISOpub
        HTML40_ESCAPE_MAP.put("\u2018", "&lsquo;"); // left single quotation mark,U+2018 ISOnum
        HTML40_ESCAPE_MAP.put("\u2019", "&rsquo;"); // right single quotation mark,U+2019 ISOnum
        HTML40_ESCAPE_MAP.put("\u201A", "&sbquo;"); // single low-9 quotation mark, U+201A NEW
        HTML40_ESCAPE_MAP.put("\u201C", "&ldquo;"); // left double quotation mark,U+201C ISOnum
        HTML40_ESCAPE_MAP.put("\u201D", "&rdquo;"); // right double quotation mark,U+201D ISOnum
        HTML40_ESCAPE_MAP.put("\u201E", "&bdquo;"); // double low-9 quotation mark, U+201E NEW
        HTML40_ESCAPE_MAP.put("\u2020", "&dagger;"); // dagger, U+2020 ISOpub
        HTML40_ESCAPE_MAP.put("\u2021", "&Dagger;"); // double dagger, U+2021 ISOpub
        HTML40_ESCAPE_MAP.put("\u2030", "&permil;"); // per mille sign, U+2030 ISOtech
        HTML40_ESCAPE_MAP.put("\u2039", "&lsaquo;"); // single left-pointing angle quotation mark,U+2039 ISO proposed
        HTML40_ESCAPE_MAP.put("\u203A", "&rsaquo;"); // single right-pointing angle quotation mark,U+203A ISO proposed
        HTML40_ESCAPE_MAP.put("\u20AC", "&euro;"); // euro sign, U+20AC NEW
        BASIC_ESCAPE_MAP.put(Symbol.DOUBLE_QUOTES, Symbol.HTML_QUOTE); // " - double-quote
        BASIC_ESCAPE_MAP.put(Symbol.AND, Symbol.HTML_AMP);   // & - ampersand
        BASIC_ESCAPE_MAP.put(Symbol.LT, Symbol.HTML_LT);    // < - less-than
        BASIC_ESCAPE_MAP.put(Symbol.GT, Symbol.HTML_GT);    // > - greater-than
        APOS_ESCAPE_MAP.put(Symbol.SINGLE_QUOTE, Symbol.HTML_APOS); // XML apostrophe
        ESCAPE_JAVA_MAP.put(Symbol.DOUBLE_QUOTES, "\\\"");
        ESCAPE_JAVA_MAP.put(Symbol.BACKSLASH, "\\\\");
        ESCAPE_ECMASCRIPT_MAP.put(Symbol.SINGLE_QUOTE, "\\'");
        ESCAPE_ECMASCRIPT_MAP.put(Symbol.DOUBLE_QUOTES, "\\\"");
        ESCAPE_ECMASCRIPT_MAP.put(Symbol.BACKSLASH, "\\\\");
        ESCAPE_ECMASCRIPT_MAP.put(Symbol.SLASH, "\\/");
        escapeJsonMap.put(Symbol.DOUBLE_QUOTES, "\\\"");
        escapeJsonMap.put(Symbol.BACKSLASH, "\\\\");
        escapeJsonMap.put(Symbol.SLASH, "\\/");
        ESCAPE_XML10_MAP.put("\u0000", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0001", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0002", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0003", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0004", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0005", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0006", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0007", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0008", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u000b", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u000c", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u000e", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u000f", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0010", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0011", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0012", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0013", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0014", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0015", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0016", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0017", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0018", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u0019", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u001a", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u001b", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u001c", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u001d", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u001e", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\u001f", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\ufffe", Normal.EMPTY);
        ESCAPE_XML10_MAP.put("\uffff", Normal.EMPTY);
        ESCAPE_XML11_MAP.put("\u0000", Normal.EMPTY);
        ESCAPE_XML11_MAP.put("\u000b", "&#11;");
        ESCAPE_XML11_MAP.put("\u000c", "&#12;");
        ESCAPE_XML11_MAP.put("\ufffe", Normal.EMPTY);
        ESCAPE_XML11_MAP.put("\uffff", Normal.EMPTY);
        ESCAPE_XSI_MAP.put(Symbol.OR, "\\|");
        ESCAPE_XSI_MAP.put(Symbol.AND, "\\&");
        ESCAPE_XSI_MAP.put(Symbol.SEMICOLON, "\\;");
        ESCAPE_XSI_MAP.put(Symbol.LT, "\\<");
        ESCAPE_XSI_MAP.put(Symbol.GT, "\\>");
        ESCAPE_XSI_MAP.put(Symbol.PARENTHESE_LEFT, "\\(");
        ESCAPE_XSI_MAP.put(Symbol.PARENTHESE_RIGHT, "\\)");
        ESCAPE_XSI_MAP.put(Symbol.DOLLAR, "\\$");
        ESCAPE_XSI_MAP.put("`", "\\`");
        ESCAPE_XSI_MAP.put(Symbol.BACKSLASH, "\\\\");
        ESCAPE_XSI_MAP.put(Symbol.DOUBLE_QUOTES, "\\\"");
        ESCAPE_XSI_MAP.put(Symbol.SINGLE_QUOTE, "\\'");
        ESCAPE_XSI_MAP.put(Symbol.SPACE, "\\ ");
        ESCAPE_XSI_MAP.put(Symbol.HT, "\\\t");
        ESCAPE_XSI_MAP.put(Symbol.CRLF, Normal.EMPTY);
        ESCAPE_XSI_MAP.put(Symbol.LF, Normal.EMPTY);
        ESCAPE_XSI_MAP.put(Symbol.STAR, "\\*");
        ESCAPE_XSI_MAP.put(Symbol.QUESTION_MARK, "\\?");
        ESCAPE_XSI_MAP.put(Symbol.BRACKET_LEFT, "\\[");
        ESCAPE_XSI_MAP.put(Symbol.SHAPE, "\\#");
        ESCAPE_XSI_MAP.put(Symbol.TILDE, "\\~");
        ESCAPE_XSI_MAP.put(Symbol.EQUAL, "\\=");
        ESCAPE_XSI_MAP.put(Symbol.PERCENT, "\\%");

        ISO8859_1_ESCAPE = Collections.unmodifiableMap(ISO8859_1_ESCAPE_MAP);
        ISO8859_1_UNESCAPE = Collections.unmodifiableMap(invert(ISO8859_1_ESCAPE));
        JAVA_CTRL_CHARS_ESCAPE = Collections.unmodifiableMap(JAVA_ESCAPE);
        JAVA_CTRL_CHARS_UNESCAPE = Collections.unmodifiableMap(invert(JAVA_CTRL_CHARS_ESCAPE));
        HTML40_EXTENDED_ESCAPE = Collections.unmodifiableMap(HTML40_ESCAPE_MAP);
        HTML40_EXTENDED_UNESCAPE = Collections.unmodifiableMap(invert(HTML40_EXTENDED_ESCAPE));
        BASIC_ESCAPE = Collections.unmodifiableMap(BASIC_ESCAPE_MAP);
        BASIC_UNESCAPE = Collections.unmodifiableMap(invert(BASIC_ESCAPE));
        APOS_ESCAPE = Collections.unmodifiableMap(APOS_ESCAPE_MAP);
        APOS_UNESCAPE = Collections.unmodifiableMap(invert(APOS_ESCAPE));

        ESCAPE_JAVA = new AggregateTranslator(
                new LookupTranslator(Collections.unmodifiableMap(ESCAPE_JAVA_MAP)),
                new LookupTranslator(JAVA_CTRL_CHARS_ESCAPE),
                JavaUnicodeEscaper.outsideOf(Normal._32, 0x7f)
        );
        UNESCAPE_JAVA = new AggregateTranslator(
                new OctalUnescaper(),
                new UnicodeUnescaper(),
                new LookupTranslator(JAVA_CTRL_CHARS_UNESCAPE),
                new LookupTranslator(Collections.unmodifiableMap(UNESCAPE_JAVA_MAP))
        );
        ESCAPE_ECMASCRIPT = new AggregateTranslator(
                new LookupTranslator(Collections.unmodifiableMap(ESCAPE_ECMASCRIPT_MAP)),
                new LookupTranslator(JAVA_CTRL_CHARS_ESCAPE),
                JavaUnicodeEscaper.outsideOf(Normal._32, 0x7f)
        );

        ESCAPE_JSON = new AggregateTranslator(
                new LookupTranslator(Collections.unmodifiableMap(escapeJsonMap)),
                new LookupTranslator(JAVA_CTRL_CHARS_ESCAPE),
                JavaUnicodeEscaper.outsideOf(Normal._32, 0x7e)
        );

        ESCAPE_XML10 = new AggregateTranslator(
                new LookupTranslator(BASIC_ESCAPE),
                new LookupTranslator(APOS_ESCAPE),
                new LookupTranslator(Collections.unmodifiableMap(ESCAPE_XML10_MAP)),
                NumericEscaper.between(0x7f, 0x84),
                NumericEscaper.between(0x86, 0x9f),
                new UnicodeUnpaired()
        );

        ESCAPE_XML11 = new AggregateTranslator(
                new LookupTranslator(BASIC_ESCAPE),
                new LookupTranslator(APOS_ESCAPE),
                new LookupTranslator(Collections.unmodifiableMap(ESCAPE_XML11_MAP)),
                NumericEscaper.between(0x1, 0x8),
                NumericEscaper.between(0xe, 0x1f),
                NumericEscaper.between(0x7f, 0x84),
                NumericEscaper.between(0x86, 0x9f),
                new UnicodeUnpaired()
        );

        ESCAPE_XSI = new LookupTranslator(
                Collections.unmodifiableMap(ESCAPE_XSI_MAP)
        );

        UNESCAPE_JSON = UNESCAPE_JAVA;

        ESCAPE_HTML3 =
                new AggregateTranslator(
                        new LookupTranslator(BASIC_ESCAPE),
                        new LookupTranslator(ISO8859_1_ESCAPE)
                );

        UNESCAPE_HTML3 =
                new AggregateTranslator(
                        new LookupTranslator(BASIC_UNESCAPE),
                        new LookupTranslator(ISO8859_1_UNESCAPE),
                        new NumericUnescaper()
                );

        ESCAPE_HTML4 =
                new AggregateTranslator(
                        new LookupTranslator(BASIC_ESCAPE),
                        new LookupTranslator(ISO8859_1_ESCAPE),
                        new LookupTranslator(HTML40_EXTENDED_ESCAPE)
                );

        UNESCAPE_HTML4 =
                new AggregateTranslator(
                        new LookupTranslator(BASIC_UNESCAPE),
                        new LookupTranslator(ISO8859_1_UNESCAPE),
                        new LookupTranslator(HTML40_EXTENDED_UNESCAPE),
                        new NumericUnescaper()
                );

        UNESCAPE_ECMASCRIPT = UNESCAPE_JAVA;

        UNESCAPE_XML = new AggregateTranslator(
                new LookupTranslator(BASIC_UNESCAPE),
                new LookupTranslator(APOS_UNESCAPE),
                new NumericUnescaper()
        );

    }

    /**
     * 用于将转义映射转换为反转义映射
     *
     * @param map map数组
     * @return Map倒数组
     */
    public static Map<CharSequence, CharSequence> invert(final Map<CharSequence, CharSequence> map) {
        final Map<CharSequence, CharSequence> newMap = new HashMap<>();
        for (final Map.Entry<CharSequence, CharSequence> pair : map.entrySet()) {
            newMap.put(pair.getValue(), pair.getKey());
        }
        return newMap;
    }

    /**
     * 用于反斜杠转义项的反转义器对象
     */
    static class XsiUnescaper extends CharSequenceTranslator {

        @Override
        public int translate(final CharSequence input, final int index, final Writer out) throws IOException {

            if (index != 0) {
                throw new IllegalStateException("XSI Unescaper should never reach the [1] index");
            }

            final String s = input.toString();

            int segmentStart = 0;
            int searchOffset = 0;
            while (true) {
                final int pos = s.indexOf(Symbol.C_BACKSLASH, searchOffset);
                if (pos == -1) {
                    if (segmentStart < s.length()) {
                        out.write(s.substring(segmentStart));
                    }
                    break;
                }
                if (pos > segmentStart) {
                    out.write(s.substring(segmentStart, pos));
                }
                segmentStart = pos + 1;
                searchOffset = pos + 2;
            }

            return Character.codePointCount(input, 0, input.length());
        }
    }

}
