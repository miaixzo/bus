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
