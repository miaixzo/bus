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
package org.miaixz.bus.core.text.escape;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.replacer.LookupReplacer;
import org.miaixz.bus.core.text.replacer.ReplacerChain;

/**
 * XML特殊字符转义
 * 见：<a href="https://stackoverflow.com/questions/1091945/what-characters-do-i-need-to-escape-in-xml-documents">
 * https://stackoverflow.com/questions/1091945/what-characters-do-i-need-to-escape-in-xml-documents</a>
 *
 * <pre>
 * 	 &amp; (ampersand) 替换为 &amp;amp;
 * 	 &lt; (less than) 替换为 &amp;lt;
 * 	 &gt; (greater than) 替换为 &amp;gt;
 * 	 &quot; (double quote) 替换为 &amp;quot;
 * 	 ' (single quote / apostrophe) 替换为 &amp;apos;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlEscape extends ReplacerChain {

    /**
     * XML转义字符
     */
    protected static final String[][] BASIC_ESCAPE = {
            // {"'", "&apos;"}, // " - single-quote
            {"\"", "&quot;"}, // " - double-quote
            {Symbol.AND, "&amp;"}, // & - ampersand
            {"<", "&lt;"}, // < - less-than
            {">", "&gt;"}, // > - greater-than
    };
    private static final long serialVersionUID = -1L;

    /**
     * 构造
     */
    public XmlEscape() {
        addChain(new LookupReplacer(BASIC_ESCAPE));
    }

}
