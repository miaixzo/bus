/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.consts;

/**
 * 标点符号常量
 *
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
public final class Symbol {

    /**
     * 字符:小数点
     */
    public static final char C_DOT = '.';
    public static final String DOT = Normal.EMPTY + C_DOT;
    public static final String DOUBLE_DOT = Normal.EMPTY + C_DOT + C_DOT;
    /**
     * 逗号字符
     */
    public static final char C_COMMA = ',';
    public static final String COMMA = Normal.EMPTY + C_COMMA;

    /**
     * 冒号
     */
    public static final char C_COLON = ':';
    public static final String COLON = Normal.EMPTY + C_COLON;

    /**
     * 空格
     */
    public static final char C_SPACE = ' ';
    public static final String SPACE = Normal.EMPTY + C_SPACE;

    /**
     * TAB
     */
    public static final char C_TAB = '	';
    public static final String TAB = Normal.EMPTY + C_TAB;


    /**
     * 下划线
     */
    public static final char C_UNDERLINE = '_';
    public static final String UNDERLINE = Normal.EMPTY + C_UNDERLINE;

    /**
     * 符号: @
     */
    public static final char C_AT = '@';
    public static final String AT = Normal.EMPTY + C_AT;

    /**
     * 斜杠
     */
    public static final char C_SLASH = '/';
    public static final String SLASH = Normal.EMPTY + C_SLASH;

    /**
     * 符号: *
     */
    public static final char C_STAR = '*';
    public static final String STAR = Normal.EMPTY + C_STAR;

    /**
     * 符号: 单引号
     */
    public static final char C_SINGLE_QUOTE = '\'';
    public static final String SINGLE_QUOTE = Normal.EMPTY + C_SINGLE_QUOTE;
    /**
     * 符号: 双引号
     */
    public static final char C_DOUBLE_QUOTES = '"';
    public static final String DOUBLE_QUOTES = Normal.EMPTY + C_DOUBLE_QUOTES;

    /**
     * 符号: 非
     */
    public static final char C_NOT = '!';
    public static final String NOT = Normal.EMPTY + C_NOT;

    /**
     * 符号: 与
     */
    public static final char C_AND = '&';
    public static final String AND = Normal.EMPTY + C_AND;

    /**
     * 符号: 或
     */
    public static final char C_OR = '|';
    public static final String OR = Normal.EMPTY + C_OR;

    /**
     * 符号: #
     */
    public static final char C_SHAPE = '#';
    public static final String SHAPE = Normal.EMPTY + C_SHAPE;

    /**
     * 符号: 美元
     */
    public static final char C_DOLLAR = '$';
    public static final String DOLLAR = Normal.EMPTY + C_DOLLAR;

    /**
     * 符号: 百分比
     */
    public static final char C_PERCENT = '%';
    public static final String PERCENT = Normal.EMPTY + C_PERCENT;

    /**
     * 符号: 幂
     */
    public static final char C_CARET = '^';
    public static final String CARET = Normal.EMPTY + C_CARET;

    /**
     * 符号: -
     */
    public static final char C_DASHED = '-';
    public static final String DASHED = Normal.EMPTY + C_DASHED;

    /**
     * 符号: +
     */
    public static final char C_PLUS = '+';
    public static final String PLUS = Normal.EMPTY + C_PLUS;

    /**
     * 符号: 大括号-左
     */
    public static final char C_DELIM_LEFT = '{';
    public static final String DELIM_LEFT = Normal.EMPTY + C_DELIM_LEFT;

    /**
     * 大括号-右
     */
    public static final char C_DELIM_END = '}';
    public static final String DELIM_END = Normal.EMPTY + C_DELIM_END;

    /**
     * 符号: 方括号-左
     */
    public static final char C_BRACKET_LEFT = '[';
    public static final String BRACKET_LEFT = Normal.EMPTY + C_BRACKET_LEFT;

    /**
     * 符号: 方括号-右
     */
    public static final char C_BRACKET_RIGHT = ']';
    public static final String BRACKET_RIGHT = Normal.EMPTY + C_BRACKET_RIGHT;

    /**
     * 符号: 等于号
     */
    public static final char C_EQUAL = '=';
    public static final String EQUAL = Normal.EMPTY + C_EQUAL;

    /**
     * 符号: 问号
     */
    public static final char C_QUESTION_MARK = '?';
    public static final String QUESTION_MARK = Normal.EMPTY + C_QUESTION_MARK;

    /**
     * 符号: 反斜杠
     */
    public static final char C_BACKSLASH = '\\';
    public static final String BACKSLASH = Normal.EMPTY + C_BACKSLASH;
    /**
     * 符号: 回车
     */
    public static final char C_CR = '\r';
    public static final String CR = Normal.EMPTY + C_CR;
    /**
     * 符号: 换行
     */
    public static final char C_LF = '\n';
    public static final String LF = Normal.EMPTY + C_LF;

    /**
     * 符号: 水平制表
     */
    public static final char C_HT = '\t';
    public static final String HT = Normal.EMPTY + C_HT;

    /**
     * 符号: 回车换行
     */
    public static final String CRLF = "\r\n";
    /**
     * 符号: 回车换行
     */
    public static final String NEWLINE = ",\n";
    /**
     * 符号: {}
     */
    public static final String DELIM = "{}";
    /**
     * 符号: []
     */
    public static final String BRACKET = "[]";
    /**
     * 符号: [L
     */
    public static final String NON_PREFIX = "[L";

    /**
     * HTML: 空格
     */
    public static final String HTML_NBSP = "&nbsp;";
    /**
     * HTML: 与
     */
    public static final String HTML_AMP = "&amp;";
    /**
     * HTML: ＂
     */
    public static final String HTML_QUOTE = "&quot;";
    /**
     * HTML: '
     */
    public static final String HTML_APOS = "&apos;";
    /**
     * HTML: 小于号
     */
    public static final String HTML_LT = "&lt;";
    /**
     * HTML: 大于号
     */
    public static final String HTML_GT = "&gt;";

    public static final String USERNAME_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    public static final String PASSWORD_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#";
    public static final String PATH_SEGMENT_ENCODE_SET = " \"<>^`{}|/\\?#";
    public static final String PATH_SEGMENT_ENCODE_SET_URI = "[]";
    public static final String QUERY_ENCODE_SET = " \"'<>#";
    public static final String QUERY_COMPONENT_REENCODE_SET = " \"'<>#&=";
    public static final String QUERY_COMPONENT_ENCODE_SET = " !\"#$&'(),/:;<=>?@[]\\^`{|}~";
    public static final String QUERY_COMPONENT_ENCODE_SET_URI = "\\^`{|}";
    public static final String FORM_ENCODE_SET = " \"':;<=>@[]^`{}|/\\?#&!$(),~";
    public static final String FRAGMENT_ENCODE_SET = "";
    public static final String FRAGMENT_ENCODE_SET_URI = " \"#<>\\^`{|}";

}
