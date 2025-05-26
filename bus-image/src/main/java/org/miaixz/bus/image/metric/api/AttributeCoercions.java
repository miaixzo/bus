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
 * @author Kimi Liu
 * @since Java 17+
 */
public class AttributeCoercions implements Iterable<AttributeCoercion>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852261781513L;

    private final List<AttributeCoercion> list = new ArrayList<>();

    public void add(AttributeCoercion ac) {
        if (findByCommonName(ac.getCommonName()) != null)
            throw new IllegalStateException("AttributeCoercion with cn: '" + ac.getCommonName() + "' already exists");
        int index = Collections.binarySearch(list, ac);
        if (index < 0)
            index = -(index + 1);
        list.add(index, ac);
    }

    public void add(AttributeCoercions acs) {
        for (AttributeCoercion ac : acs.list)
            add(ac);
    }

    public boolean remove(AttributeCoercion ac) {
        return list.remove(ac);
    }

    public void clear() {
        list.clear();
    }

    public AttributeCoercion findByCommonName(String commonName) {
        for (AttributeCoercion ac : list)
            if (commonName.equals(ac.getCommonName()))
                return ac;
        return null;
    }

    public AttributeCoercion findAttributeCoercion(String sopClass, Dimse dimse, TransferCapability.Role role,
            String aeTitle) {
        for (AttributeCoercion ac : list)
            if (ac.matchesCondition(sopClass, dimse, role, aeTitle))
                return ac;
        return null;
    }

    @Override
    public Iterator<AttributeCoercion> iterator() {
        return list.iterator();
    }

}
