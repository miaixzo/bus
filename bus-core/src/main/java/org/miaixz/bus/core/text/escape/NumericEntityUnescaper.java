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
import org.miaixz.bus.core.text.replacer.StringReplacer;
import org.miaixz.bus.core.xyz.CharKit;

/**
 * 形如&#39;的反转义器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericEntityUnescaper extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852236101870L;

    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        final int len = text.length();
        // 检查以确保以&#开头
        if (text.charAt(pos) == Symbol.C_AND && pos < len - 2 && text.charAt(pos + 1) == Symbol.C_HASH) {
            int start = pos + 2;
            boolean isHex = false;
            final char firstChar = text.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
            }

            // 确保&#后还有数字
            if (start == len) {
                return 0;
            }

            int end = start;
            while (end < len && CharKit.isHexChar(text.charAt(end))) {
                end++;
            }
            final boolean isSemiNext = (end != len) && (text.charAt(end) == Symbol.C_SEMICOLON);
            if (isSemiNext) {
                final int entityValue;
                try {
                    if (isHex) {
                        entityValue = Integer.parseInt(text.subSequence(start, end).toString(), 16);
                    } else {
                        entityValue = Integer.parseInt(text.subSequence(start, end).toString(), 10);
                    }
                } catch (final NumberFormatException nfe) {
                    return 0;
                }
                out.append((char) entityValue);
                return 2 + end - start + (isHex ? 1 : 0) + 1;
            }
        }
        return 0;
    }

}
