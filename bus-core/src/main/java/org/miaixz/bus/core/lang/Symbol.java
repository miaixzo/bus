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
package org.miaixz.bus.core.lang;

/**
 * Defines common punctuation and special character constants.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Symbol {

    /**
     * Character: '0'
     */
    public static final char C_ZERO = '0';
    /**
     * String: "0"
     */
    public static final String ZERO = Normal.EMPTY + C_ZERO;
    /**
     * Character: Chinese numeral '零' (zero)
     */
    public static final char C_UL_ZERO = '零';
    /**
     * String: Chinese numeral "零" (zero)
     */
    public static final String UL_ZERO = Normal.EMPTY + C_UL_ZERO;
    /**
     * Character: '1'
     */
    public static final char C_ONE = '1';
    /**
     * String: "1"
     */
    public static final String ONE = Normal.EMPTY + C_ONE;
    /**
     * Character: Chinese numeral '一' (one, simplified)
     */
    public static final char C_L_ONE = '一';
    /**
     * String: Chinese numeral "一" (one, simplified)
     */
    public static final String L_ONE = Normal.EMPTY + C_L_ONE;
    /**
     * Character: Chinese numeral '壹' (one, traditional/financial)
     */
    public static final char C_U_ONE = '壹';
    /**
     * String: Chinese numeral "壹" (one, traditional/financial)
     */
    public static final String U_ONE = Normal.EMPTY + C_U_ONE;

    /**
     * Character: '2'
     */
    public static final char C_TWO = '2';
    /**
     * String: "2"
     */
    public static final String TWO = Normal.EMPTY + C_TWO;
    /**
     * Character: Chinese numeral '二' (two, simplified)
     */
    public static final char C_L_TWO = '二';
    /**
     * String: Chinese numeral "二" (two, simplified)
     */
    public static final String S_TWO = Normal.EMPTY + C_L_TWO;
    /**
     * Character: Chinese numeral '贰' (two, traditional/financial)
     */
    public static final char C_U_TWO = '贰';
    /**
     * String: Chinese numeral "贰" (two, traditional/financial)
     */
    public static final String T_TWO = Normal.EMPTY + C_U_TWO;
    /**
     * Character: '3'
     */
    public static final char C_THREE = '3';
    /**
     * String: "3"
     */
    public static final String THREE = Normal.EMPTY + C_THREE;
    /**
     * Character: Chinese numeral '三' (three, simplified)
     */
    public static final char C_L_THREE = '三';
    /**
     * String: Chinese numeral "三" (three, simplified)
     */
    public static final String S_THREE = Normal.EMPTY + C_L_THREE;
    /**
     * Character: Chinese numeral '叁' (three, traditional/financial)
     */
    public static final char C_U_THREE = '叁';
    /**
     * String: Chinese numeral "叁" (three, traditional/financial)
     */
    public static final String T_THREE = Normal.EMPTY + C_U_THREE;

    /**
     * Character: '4'
     */
    public static final char C_FOUR = '4';
    /**
     * String: "4"
     */
    public static final String FOUR = Normal.EMPTY + C_FOUR;
    /**
     * Character: Chinese numeral '四' (four, simplified)
     */
    public static final char C_L_FOUR = '四';
    /**
     * String: Chinese numeral "四" (four, simplified)
     */
    public static final String S_FOUR = Normal.EMPTY + C_L_FOUR;
    /**
     * Character: Chinese numeral '肆' (four, traditional/financial)
     */
    public static final char C_U_FOUR = '肆';
    /**
     * String: Chinese numeral "肆" (four, traditional/financial)
     */
    public static final String T_FOUR = Normal.EMPTY + C_U_FOUR;

    /**
     * Character: '5'
     */
    public static final char C_FIVE = '5';
    /**
     * String: "5"
     */
    public static final String FIVE = Normal.EMPTY + C_FIVE;
    /**
     * Character: Chinese numeral '五' (five, simplified)
     */
    public static final char C_L_FIVE = '五';
    /**
     * String: Chinese numeral "五" (five, simplified)
     */
    public static final String S_FIVE = Normal.EMPTY + C_L_FIVE;
    /**
     * Character: Chinese numeral '伍' (five, traditional/financial)
     */
    public static final char C_U_FIVE = '伍';
    /**
     * String: Chinese numeral "伍" (five, traditional/financial)
     */
    public static final String T_FIVE = Normal.EMPTY + C_U_FIVE;
    /**
     * Character: '6'
     */
    public static final char C_SIX = '6';
    /**
     * String: "6"
     */
    public static final String SIX = Normal.EMPTY + C_SIX;
    /**
     * Character: Chinese numeral '六' (six, simplified)
     */
    public static final char C_L_SIX = '六';
    /**
     * String: Chinese numeral "六" (six, simplified)
     */
    public static final String L_SIX = Normal.EMPTY + C_L_SIX;
    /**
     * Character: Chinese numeral '陆' (six, traditional/financial)
     */
    public static final char C_U_SIX = '陆';
    /**
     * String: Chinese numeral "陆" (six, traditional/financial)
     */
    public static final String U_SIX = Normal.EMPTY + C_U_SIX;

    /**
     * Character: '7'
     */
    public static final char C_SEVEN = '7';
    /**
     * String: "7"
     */
    public static final String SEVEN = Normal.EMPTY + C_SEVEN;
    /**
     * Character: Chinese numeral '七' (seven, simplified)
     */
    public static final char C_L_SEVEN = '七';
    /**
     * String: Chinese numeral "七" (seven, simplified)
     */
    public static final String L_SEVEN = Normal.EMPTY + C_L_SEVEN;
    /**
     * Character: Chinese numeral '柒' (seven, traditional/financial)
     */
    public static final char C_U_SEVEN = '柒';
    /**
     * String: Chinese numeral "柒" (seven, traditional/financial)
     */
    public static final String U_SEVEN = Normal.EMPTY + C_U_SEVEN;

    /**
     * Character: '8'
     */
    public static final char C_EIGHT = '8';
    /**
     * String: "8"
     */
    public static final String EIGHT = Normal.EMPTY + C_EIGHT;
    /**
     * Character: Chinese numeral '八' (eight, simplified)
     */
    public static final char C_L_EIGHT = '八';
    /**
     * String: Chinese numeral "八" (eight, simplified)
     */
    public static final String L_EIGHT = Normal.EMPTY + C_L_EIGHT;
    /**
     * Character: Chinese numeral '捌' (eight, traditional/financial)
     */
    public static final char C_U_EIGHT = '捌';
    /**
     * String: Chinese numeral "捌" (eight, traditional/financial)
     */
    public static final String U_EIGHT = Normal.EMPTY + C_U_EIGHT;

    /**
     * Character: '9'
     */
    public static final char C_NINE = '9';
    /**
     * String: "9"
     */
    public static final String NINE = Normal.EMPTY + C_NINE;
    /**
     * Character: Chinese numeral '九' (nine, simplified)
     */
    public static final char C_L_NINE = '九';
    /**
     * String: Chinese numeral "九" (nine, simplified)
     */
    public static final String L_NINE = Normal.EMPTY + C_L_NINE;
    /**
     * Character: Chinese numeral '玖' (nine, traditional/financial)
     */
    public static final char C_U_NINE = '玖';
    /**
     * String: Chinese numeral "玖" (nine, traditional/financial)
     */
    public static final String U_NINE = Normal.EMPTY + C_U_NINE;

    /**
     * Integer: 10
     */
    public static final int C_TEN = 10;
    /**
     * String: "10"
     */
    public static final String TEN = Normal.EMPTY + C_TEN;
    /**
     * Character: Chinese numeral '十' (ten, simplified)
     */
    public static final char C_L_TEN = '十';
    /**
     * String: Chinese numeral "十" (ten, simplified)
     */
    public static final String L_TEN = Normal.EMPTY + C_L_TEN;
    /**
     * Character: Chinese numeral '拾' (ten, traditional/financial)
     */
    public static final char C_U_TEN = '拾';
    /**
     * String: Chinese numeral "拾" (ten, traditional/financial)
     */
    public static final String U_TEN = Normal.EMPTY + C_U_TEN;

    /**
     * Integer: 100
     */
    public static final int C_ONE_HUNDRED = 100;
    /**
     * String: "100"
     */
    public static final String ONE_HUNDRED = Normal.EMPTY + C_ONE_HUNDRED;
    /**
     * Character: Chinese numeral '百' (hundred, simplified)
     */
    public static final char C_L_ONE_HUNDRED = '百';
    /**
     * String: Chinese numeral "百" (hundred, simplified)
     */
    public static final String L_ONE_HUNDRED = Normal.EMPTY + C_L_ONE_HUNDRED;
    /**
     * Character: Chinese numeral '佰' (hundred, traditional/financial)
     */
    public static final char C_U_ONE_HUNDRED = '佰';
    /**
     * String: Chinese numeral "佰" (hundred, traditional/financial)
     */
    public static final String U_ONE_HUNDRED = Normal.EMPTY + C_U_ONE_HUNDRED;

    /**
     * Integer: 1000
     */
    public static final int C_ONE_THOUSAND = 1000;
    /**
     * String: "1000"
     */
    public static final String ONE_THOUSAND = Normal.EMPTY + C_ONE_THOUSAND;
    /**
     * Character: Chinese numeral '千' (thousand, simplified)
     */
    public static final char C_L_ONE_THOUSAND = '千';
    /**
     * String: Chinese numeral "千" (thousand, simplified)
     */
    public static final String L_ONE_THOUSAND = Normal.EMPTY + C_L_ONE_THOUSAND;
    /**
     * Character: Chinese numeral '仟' (thousand, traditional/financial)
     */
    public static final char C_U_ONE_THOUSAND = '仟';
    /**
     * String: Chinese numeral "仟" (thousand, traditional/financial)
     */
    public static final String U_ONE_THOUSAND = Normal.EMPTY + C_U_ONE_THOUSAND;

    /**
     * Character: '万' (ten thousand)
     */
    public static final char C_TEN_THOUSAND = 10000;
    /**
     * String: "10000"
     */
    public static final String TEN_THOUSAND = Normal.EMPTY + C_TEN_THOUSAND;
    /**
     * Character: Chinese numeral '万' (ten thousand, simplified)
     */
    public static final char C_L_TEN_THOUSAND = '万';
    /**
     * String: Chinese numeral "万" (ten thousand, simplified)
     */
    public static final String L_TEN_THOUSAND = Normal.EMPTY + C_L_TEN_THOUSAND;
    /**
     * Character: Chinese numeral '萬' (ten thousand, traditional)
     */
    public static final char C_U_TEN_THOUSAND = '萬';
    /**
     * String: Chinese numeral "萬" (ten thousand, traditional)
     */
    public static final String U_TEN_THOUSAND = Normal.EMPTY + C_U_TEN_THOUSAND;

    /**
     * Integer: 100,000,000
     */
    public static final int C_ONE_HUNDRED_MILLION = 100000000;
    /**
     * String: "100000000"
     */
    public static final String ONE_HUNDRED_MILLION = Normal.EMPTY + C_ONE_HUNDRED_MILLION;
    /**
     * Character: Chinese numeral '亿' (hundred million, simplified)
     */
    public static final char C_L_ONE_HUNDRED_MILLION = '亿';
    /**
     * String: Chinese numeral "亿" (hundred million, simplified)
     */
    public static final String L_ONE_HUNDRED_MILLION = Normal.EMPTY + C_L_ONE_HUNDRED_MILLION;
    /**
     * Character: Chinese numeral '億' (hundred million, traditional)
     */
    public static final char C_U_ONE_HUNDRED_MILLION = '億';
    /**
     * String: Chinese numeral "億" (hundred million, traditional)
     */
    public static final String U_ONE_HUNDRED_MILLION = Normal.EMPTY + C_U_ONE_HUNDRED_MILLION;

    /**
     * Character: 'X'
     */
    public static final char C_X = 'X';
    /**
     * String: "X"
     */
    public static final String X = Normal.EMPTY + C_X;

    /**
     * Character: comma ','
     */
    public static final char C_COMMA = ',';
    /**
     * String: ","
     */
    public static final String COMMA = Normal.EMPTY + C_COMMA;

    /**
     * Character: colon ':'
     */
    public static final char C_COLON = ':';
    /**
     * String: ":"
     */
    public static final String COLON = Normal.EMPTY + C_COLON;

    /**
     * Character: tilde '~'
     */
    public static final char C_TILDE = '~';
    /**
     * String: "~"
     */
    public static final String TILDE = Normal.EMPTY + C_TILDE;

    /**
     * Character: space ' '
     */
    public static final char C_SPACE = ' ';
    /**
     * String: " "
     */
    public static final String SPACE = Normal.EMPTY + C_SPACE;

    /**
     * Character: tab '\t'
     */
    public static final char C_TAB = '	';
    /**
     * String: "\t"
     */
    public static final String TAB = Normal.EMPTY + C_TAB;

    /**
     * Character: dot '.'
     */
    public static final char C_DOT = '.';
    /**
     * String: "."
     */
    public static final String DOT = Normal.EMPTY + C_DOT;
    /**
     * String: ".."
     */
    public static final String DOUBLE_DOT = Normal.EMPTY + C_DOT + C_DOT;

    /**
     * Character: semicolon ';'
     */
    public static final char C_SEMICOLON = ';';
    /**
     * String: ";"
     */
    public static final String SEMICOLON = Normal.EMPTY + C_SEMICOLON;

    /**
     * Character: underscore '_'
     */
    public static final char C_UNDERLINE = '_';
    /**
     * String: "_"
     */
    public static final String UNDERLINE = Normal.EMPTY + C_UNDERLINE;

    /**
     * Character: single quote '\''
     */
    public static final char C_SINGLE_QUOTE = '\'';
    /**
     * String: "'"
     */
    public static final String SINGLE_QUOTE = Normal.EMPTY + C_SINGLE_QUOTE;

    /**
     * Character: double quotes '"'
     */
    public static final char C_DOUBLE_QUOTES = '"';
    /**
     * String: '"'
     */
    public static final String DOUBLE_QUOTES = Normal.EMPTY + C_DOUBLE_QUOTES;

    /**
     * Character: exclamation mark '!'
     */
    public static final char C_NOT = '!';
    /**
     * String: "!"
     */
    public static final String NOT = Normal.EMPTY + C_NOT;

    /**
     * Character: ampersand 'amp;'
     */
    public static final char C_AND = '&';
    /**
     * String: "amp;"
     */
    public static final String AND = Normal.EMPTY + C_AND;

    /**
     * Character: pipe '|'
     */
    public static final char C_OR = '|';
    /**
     * String: "|"
     */
    public static final String OR = Normal.EMPTY + C_OR;

    /**
     * Character: at symbol '@'
     */
    public static final char C_AT = '@';
    /**
     * String: "@"
     */
    public static final String AT = Normal.EMPTY + C_AT;

    /**
     * Character: asterisk '*'
     */
    public static final char C_STAR = '*';
    /**
     * String: "*"
     */
    public static final String STAR = Normal.EMPTY + C_STAR;

    /**
     * Character: Chinese Yuan symbol '¥'
     */
    public static final char C_CNY = '¥';
    /**
     * String: "¥"
     */
    public static final String CNY = Normal.EMPTY + C_CNY;

    /**
     * Character: Chinese character for Yuan '元'
     */
    public static final char C_CNY_YUAN = '元';
    /**
     * String: "元"
     */
    public static final String CNY_YUAN = Normal.EMPTY + C_CNY_YUAN;

    /**
     * Character: Chinese character for Jiao (ten cents) '角'
     */
    public static final char C_CNY_JIAO = '角';
    /**
     * String: "角"
     */
    public static final String CNY_JIAO = Normal.EMPTY + C_CNY_JIAO;

    /**
     * Character: Chinese character for Fen (cent) '分'
     */
    public static final char C_CNY_FEN = '分';
    /**
     * String: "分"
     */
    public static final String CNY_FEN = Normal.EMPTY + C_CNY_FEN;

    /**
     * Character: Chinese character for "exact" or "integer" '整'
     */
    public static final char C_CNY_ZHENG = '整';
    /**
     * String: "整"
     */
    public static final String CNY_ZHENG = Normal.EMPTY + C_CNY_ZHENG;

    /**
     * Character: dollar sign '$'
     */
    public static final char C_DOLLAR = '$';
    /**
     * String: "$"
     */
    public static final String DOLLAR = Normal.EMPTY + C_DOLLAR;

    /**
     * Character: hash sign '#'
     */
    public static final char C_HASH = '#';
    /**
     * String: "#"
     */
    public static final String HASH = Normal.EMPTY + C_HASH;

    /**
     * Character: percent sign '%'
     */
    public static final char C_PERCENT = '%';
    /**
     * String: "%"
     */
    public static final String PERCENT = Normal.EMPTY + C_PERCENT;

    /**
     * Character: caret '^'
     */
    public static final char C_CARET = '^';
    /**
     * String: "^"
     */
    public static final String CARET = Normal.EMPTY + C_CARET;

    /**
     * Character: minus sign '-'
     */
    public static final char C_MINUS = '-';
    /**
     * String: "-"
     */
    public static final String MINUS = Normal.EMPTY + C_MINUS;

    /**
     * Character: plus sign '+'
     */
    public static final char C_PLUS = '+';
    /**
     * String: "+"
     */
    public static final String PLUS = Normal.EMPTY + C_PLUS;

    /**
     * Character: equals sign '='
     */
    public static final char C_EQUAL = '=';
    /**
     * String: "="
     */
    public static final String EQUAL = Normal.EMPTY + C_EQUAL;

    /**
     * Character: greater than sign '&gt;'
     */
    public static final char C_GT = '>';
    /**
     * String: "&gt;"
     */
    public static final String GT = Normal.EMPTY + C_GT;

    /**
     * Character: less than sign '&lt;'
     */
    public static final char C_LT = '<';
    /**
     * String: "&lt;"
     */
    public static final String LT = Normal.EMPTY + C_LT;

    /**
     * String: greater than or equal to "&gt;="
     */
    public static final String GE = ">=";
    /**
     * String: less than or equal to "&lt;="
     */
    public static final String LE = "<=";

    /**
     * Character: left parenthesis '('
     */
    public static final char C_PARENTHESE_LEFT = '(';
    /**
     * String: "("
     */
    public static final String PARENTHESE_LEFT = Normal.EMPTY + C_PARENTHESE_LEFT;

    /**
     * Character: right parenthesis ')'
     */
    public static final char C_PARENTHESE_RIGHT = ')';
    /**
     * String: ")"
     */
    public static final String PARENTHESE_RIGHT = Normal.EMPTY + C_PARENTHESE_RIGHT;

    /**
     * Character: left curly brace '{'
     */
    public static final char C_BRACE_LEFT = '{';
    /**
     * String: "{"
     */
    public static final String BRACE_LEFT = Normal.EMPTY + C_BRACE_LEFT;

    /**
     * Character: right curly brace '}'
     */
    public static final char C_BRACE_RIGHT = '}';
    /**
     * String: "}"
     */
    public static final String BRACE_RIGHT = Normal.EMPTY + C_BRACE_RIGHT;

    /**
     * Character: left square bracket '['
     */
    public static final char C_BRACKET_LEFT = '[';
    /**
     * String: "["
     */
    public static final String BRACKET_LEFT = Normal.EMPTY + C_BRACKET_LEFT;

    /**
     * Character: right square bracket ']'
     */
    public static final char C_BRACKET_RIGHT = ']';
    /**
     * String: "]"
     */
    public static final String BRACKET_RIGHT = Normal.EMPTY + C_BRACKET_RIGHT;

    /**
     * Character: question mark '?'
     */
    public static final char C_QUESTION_MARK = '?';
    /**
     * String: "?"
     */
    public static final String QUESTION_MARK = Normal.EMPTY + C_QUESTION_MARK;

    /**
     * Character: slash '/'
     */
    public static final char C_SLASH = '/';
    /**
     * String: "/"
     */
    public static final String SLASH = Normal.EMPTY + C_SLASH;
    /**
     * String: double slash "//"
     */
    public static final String FORWARDSLASH = SLASH + SLASH;

    /**
     * Character: backslash '\'
     */
    public static final char C_BACKSLASH = '\\';
    /**
     * String: "\" (backslash)
     */
    public static final String BACKSLASH = Normal.EMPTY + C_BACKSLASH;

    /**
     * Character: carriage return '\r'
     */
    public static final char C_CR = '\r';
    /**
     * String: "\r"
     */
    public static final String CR = Normal.EMPTY + C_CR;

    /**
     * Character: line feed '\n'
     */
    public static final char C_LF = '\n';
    /**
     * String: "\n"
     */
    public static final String LF = Normal.EMPTY + C_LF;

    /**
     * Character: horizontal tab '\t'
     */
    public static final char C_HT = '	';
    /**
     * String: "\t"
     */
    public static final String HT = Normal.EMPTY + C_HT;

    /**
     * String: carriage return and line feed "\r\n"
     */
    public static final String CRLF = "\r\n";

    /**
     * String: newline ",\n"
     */
    public static final String NEWLINE = ",\n";

    /**
     * String: empty curly braces "{}"
     */
    public static final String DELIM = "{}";

    /**
     * String: empty square brackets "[]"
     */
    public static final String BRACKET = "[]";

    /**
     * String: "[L"
     */
    public static final String NON_PREFIX = "[L";

    /**
     * String: "${"
     */
    public static final String DOLLAR_LEFT_BRACE = "${";

    /**
     * String: "#{"
     */
    public static final String HASH_LEFT_BRACE = "#{";

    /**
     * HTML entity: non-breaking space "&nbsp;"
     */
    public static final String HTML_NBSP = "&nbsp;";

    /**
     * HTML entity: ampersand "&amp;"
     */
    public static final String HTML_AMP = "&amp;";

    /**
     * HTML entity: double quote "&quot;"
     */
    public static final String HTML_QUOTE = "&quot;";

    /**
     * HTML entity: apostrophe "&apos;"
     */
    public static final String HTML_APOS = "&apos;";

    /**
     * HTML entity: less than sign "&lt;"
     */
    public static final String HTML_LT = "&lt;";

    /**
     * HTML entity: greater than sign "&gt;"
     */
    public static final String HTML_GT = "&gt;";

    /**
     * Unicode escape sequence start string: "\\u"
     */
    public static final String UNICODE_START_CHAR = "\\u";

}
