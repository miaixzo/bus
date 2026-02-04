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

    @Serial
    private static final long serialVersionUID = 2852236260780L;

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
     * Constructs a new {@code XmlEscape} instance. Initializes the escape chain with the basic XML escape lookup
     * replacer.
     */
    public XmlEscape() {
        addChain(new LookupReplacer(BASIC_ESCAPE));
    }

}
