/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.nimble.codec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompressionRules implements Iterable<CompressionRule>, Serializable {

    private final List<CompressionRule> list = new ArrayList<>();

    public void add(CompressionRule rule) {
        if (null != findByCommonName(rule.getCommonName()))
            throw new IllegalStateException("CompressionRule with cn: '"
                    + rule.getCommonName() + "' already exists");
        int index = Collections.binarySearch(list, rule);
        if (index < 0)
            index = -(index + 1);
        list.add(index, rule);
    }

    public void add(CompressionRules rules) {
        for (CompressionRule rule : rules)
            add(rule);
    }

    public boolean remove(CompressionRule ac) {
        return list.remove(ac);
    }

    public void clear() {
        list.clear();
    }

    public CompressionRule findByCommonName(String commonName) {
        for (CompressionRule rule : list)
            if (commonName.equals(rule.getCommonName()))
                return rule;
        return null;
    }

    public CompressionRule findCompressionRule(String aeTitle, ImageDescriptor imageDescriptor) {
        for (CompressionRule ac : list)
            if (ac.matchesCondition(aeTitle, imageDescriptor))
                return ac;
        return null;
    }

    @Override
    public Iterator<CompressionRule> iterator() {
        return list.iterator();
    }
}
