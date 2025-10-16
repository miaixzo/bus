/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.Serial;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.replacer.LookupReplacer;
import org.miaixz.bus.core.text.replacer.ReplacerChain;

/**
 * Escapes special characters in XML strings. This class provides functionality to convert characters that have special
 * meaning in XML into their corresponding XML entity references.
 *
 * <p>
 * The following characters are escaped:
 *
 * <ul>
 * <li>{@code &} (ampersand) is replaced with {@code &amp;}</li>
 * <li>{@code <} (less than) is replaced with {@code &lt;}</li>
 * <li>{@code >} (greater than) is replaced with {@code &gt;}</li>
 * <li>{@code "} (double quote) is replaced with {@code &quot;}</li>
 * <li>{@code '} (single quote) is replaced with {@code &apos;}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlEscape extends ReplacerChain {

    /**
     * Lookup table for XML escape characters. This array defines the mapping from special characters to their XML
     * entity references.
     */
    protected static final String[][] BASIC_ESCAPE = { { "'", "&apos;" }, // " - single-quote
            { "\"", "&quot;" }, // " - double-quote
            { Symbol.AND, "&amp;" }, // & - ampersand
            { "<", "&lt;" }, // < - less-than
            { ">", "&gt;" }, // > - greater-than
    };
    /**
     * The serial version UID for serialization.
     */
    @Serial
    private static final long serialVersionUID = 2852236260780L;

    /**
     * Constructs a new {@code XmlEscape} instance. Initializes the escape chain with the basic XML escape lookup
     * replacer.
     */
    public XmlEscape() {
        addChain(new LookupReplacer(BASIC_ESCAPE));
    }

}
