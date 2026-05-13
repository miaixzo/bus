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
package org.miaixz.bus.image.metric.api;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.image.Dimse;
import org.miaixz.bus.image.metric.TransferCapability;

/**
 * Represents the AttributeCoercions type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AttributeCoercions implements Iterable<AttributeCoercion>, Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852262865573L;

    /**
     * The list value.
     */
    private final List<AttributeCoercion> list = new ArrayList<>();

    /**
     * Executes the add operation.
     *
     * @param ac the ac.
     */
    public void add(AttributeCoercion ac) {
        if (findByCommonName(ac.getCommonName()) != null)
            throw new IllegalStateException("AttributeCoercion with cn: '" + ac.getCommonName() + "' already exists");
        int index = Collections.binarySearch(list, ac);
        if (index < 0)
            index = -(index + 1);
        list.add(index, ac);
    }

    /**
     * Executes the add operation.
     *
     * @param acs the acs.
     */
    public void add(AttributeCoercions acs) {
        for (AttributeCoercion ac : acs.list)
            add(ac);
    }

    /**
     * Executes the remove operation.
     *
     * @param ac the ac.
     * @return true if the condition is met; otherwise false.
     */
    public boolean remove(AttributeCoercion ac) {
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
    public AttributeCoercion findByCommonName(String commonName) {
        for (AttributeCoercion ac : list)
            if (commonName.equals(ac.getCommonName()))
                return ac;
        return null;
    }

    /**
     * Finds the attribute coercion.
     *
     * @param sopClass the sop class.
     * @param dimse    the dimse.
     * @param role     the role.
     * @param aeTitle  the ae title.
     * @return the operation result.
     */
    public AttributeCoercion findAttributeCoercion(
            String sopClass,
            Dimse dimse,
            TransferCapability.Role role,
            String aeTitle) {
        for (AttributeCoercion ac : list)
            if (ac.matchesCondition(sopClass, dimse, role, aeTitle))
                return ac;
        return null;
    }

    /**
     * Executes the iterator operation.
     *
     * @return the operation result.
     */
    @Override
    public Iterator<AttributeCoercion> iterator() {
        return list.iterator();
    }

}
