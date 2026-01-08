/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.Serial;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.miaixz.bus.core.lang.Chain;

/**
 * A chain of string replacers, allowing multiple replacement logics to be combined.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReplacerChain extends StringReplacer implements Chain<StringReplacer, ReplacerChain> {

    @Serial
    private static final long serialVersionUID = 2852239321085L;

    /**
     * The list of string replacers in this chain.
     */
    private final List<StringReplacer> replacers = new LinkedList<>();

    /**
     * Constructs a new {@code ReplacerChain} with the given string replacers.
     *
     * @param stringReplacers The string replacers to add to the chain.
     */
    public ReplacerChain(final StringReplacer... stringReplacers) {
        for (final StringReplacer stringReplacer : stringReplacers) {
            addChain(stringReplacer);
        }
    }

    /**
     * Returns an iterator over elements of type T.
     *
     * @return an Iterator
     */
    @Override
    public Iterator<StringReplacer> iterator() {
        return replacers.iterator();
    }

    /**
     * Addchain method.
     *
     * @return the ReplacerChain value
     */
    @Override
    public ReplacerChain addChain(final StringReplacer element) {
        replacers.add(element);
        return this;
    }

    /**
     * Replaces a portion of the text using the replacers in the chain. Iterates through the chain and applies the first
     * replacer that matches.
     *
     * @param text The text to be processed.
     * @param pos  The current position in the text.
     * @param out  The {@code StringBuilder} to which the replaced text is appended.
     * @return The number of characters consumed by the replacement, or 0 if no replacement occurred.
     */
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
