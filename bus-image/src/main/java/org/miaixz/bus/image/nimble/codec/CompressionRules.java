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
 * Represents the CompressionRules type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompressionRules implements Iterable<CompressionRule>, Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852288199011L;

    /**
     * The list value.
     */
    private final List<CompressionRule> list = new ArrayList<>();

    /**
     * Executes the add operation.
     *
     * @param rule the rule.
     */
    public void add(CompressionRule rule) {
        if (findByCommonName(rule.getCommonName()) != null)
            throw new IllegalStateException("CompressionRule with cn: '" + rule.getCommonName() + "' already exists");
        int index = Collections.binarySearch(list, rule);
        if (index < 0)
            index = -(index + 1);
        list.add(index, rule);
    }

    /**
     * Executes the add operation.
     *
     * @param rules the rules.
     */
    public void add(CompressionRules rules) {
        for (CompressionRule rule : rules)
            add(rule);
    }

    /**
     * Executes the remove operation.
     *
     * @param ac the ac.
     * @return true if the condition is met; otherwise false.
     */
    public boolean remove(CompressionRule ac) {
        return list.remove(ac);
    }

    /**
     * Executes the clear operation.
     */
    public void clear() {
        list.clear();
    }

    /**
     * Finds the by common name.
     *
     * @param commonName the common name.
     * @return the operation result.
     */
    public CompressionRule findByCommonName(String commonName) {
        for (CompressionRule rule : list)
            if (commonName.equals(rule.getCommonName()))
                return rule;
        return null;
    }

    /**
     * Finds the compression rule.
     *
     * @param aeTitle         the ae title.
     * @param imageDescriptor the image descriptor.
     * @return the operation result.
     */
    public CompressionRule findCompressionRule(String aeTitle, ImageDescriptor imageDescriptor) {
        for (CompressionRule ac : list)
            if (ac.matchesCondition(aeTitle, imageDescriptor))
                return ac;
        return null;
    }

    /**
     * Executes the iterator operation.
     *
     * @return the operation result.
     */
    @Override
    public Iterator<CompressionRule> iterator() {
        return list.iterator();
    }

}
