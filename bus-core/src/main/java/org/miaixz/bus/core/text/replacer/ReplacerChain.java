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
package org.miaixz.bus.core.text.replacer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.miaixz.bus.core.lang.Chain;

/**
 * 字符串替换链，用于组合多个字符串替换逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReplacerChain extends StringReplacer implements Chain<StringReplacer, ReplacerChain> {

    private static final long serialVersionUID = -1L;

    private final List<StringReplacer> replacers = new LinkedList<>();

    /**
     * 构造
     *
     * @param stringReplacers 字符串替换器
     */
    public ReplacerChain(final StringReplacer... stringReplacers) {
        for (final StringReplacer stringReplacer : stringReplacers) {
            addChain(stringReplacer);
        }
    }

    @Override
    public Iterator<StringReplacer> iterator() {
        return replacers.iterator();
    }

    @Override
    public ReplacerChain addChain(final StringReplacer element) {
        replacers.add(element);
        return this;
    }

    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        int consumed = 0;
        for (final StringReplacer stringReplacer : replacers) {
            consumed = stringReplacer.replace(text, pos, out);
            if (0 != consumed) {
                return consumed;
            }
        }
        return consumed;
    }

}
