/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.codec;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompressionRules implements Iterable<CompressionRule>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852288199011L;

    private final List<CompressionRule> list = new ArrayList<>();

    public void add(CompressionRule rule) {
        if (findByCommonName(rule.getCommonName()) != null)
            throw new IllegalStateException("CompressionRule with cn: '" + rule.getCommonName() + "' already exists");
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
