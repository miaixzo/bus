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
package org.miaixz.bus.image.galaxy.data;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum HL7Separator {
    FIELD("|", "\\F\\"),
    COMPONENT("^", "\\S\\"),
    SUBCOMPONENT("&", "\\T\\"),
    REPETITION("~", "\\R\\"),
    ESCAPE("\\", "\\E\\");

    public final String separator;
    public final String escapeSequence;

    HL7Separator(String separator, String escapeSequence) {
        this.separator = separator;
        this.escapeSequence = escapeSequence;
    }

    public static String escapeAll(String s) {
        return FIELD.escape(
                COMPONENT.escape(
                        SUBCOMPONENT.escape(
                                REPETITION.escape(
                                        ESCAPE.escape(s)))));
    }

    public static String unescapeAll(String s) {
        return ESCAPE.unescape(
                REPETITION.unescape(
                        SUBCOMPONENT.unescape(
                                COMPONENT.unescape(
                                        FIELD.unescape(s)))));
    }

    public String escape(String s) {
        return s.replace(separator, escapeSequence);
    }

    public String unescape(String s) {
        return s.replace(escapeSequence, separator);
    }

}
