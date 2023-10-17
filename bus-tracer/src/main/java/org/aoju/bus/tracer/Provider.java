/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.tracer;

import org.aoju.bus.tracer.backend.TraceBackendProvider;

import java.lang.ref.SoftReference;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Provider extends AbstractSet<TraceBackendProvider> {

    private Set<SoftReference<TraceBackendProvider>> values;
    private boolean valid = true;

    Provider(Set<TraceBackendProvider> elements) {
        this.values = new HashSet<>();
        addAllInternal(elements);
    }

    @Override
    public Iterator<TraceBackendProvider> iterator() {
        final Collection<TraceBackendProvider> strongRefList = createStrongView(values);
        determineValidity(strongRefList);
        if (valid) {
            return strongRefList.iterator();
        }
        return Collections.emptyIterator();
    }

    @Override
    public int size() {
        final Collection<TraceBackendProvider> strongRefList = createStrongView(values);
        determineValidity(strongRefList);
        if (valid) {
            return strongRefList.size();
        }
        return 0;
    }

    private void addAllInternal(final Collection<TraceBackendProvider> elements) {
        for (TraceBackendProvider element : elements) {
            values.add(new SoftReference<>(element));
        }
    }

    private void determineValidity(final Collection<TraceBackendProvider> providers) {
        if (!valid) {
            return;
        }
        for (TraceBackendProvider provider : providers) {
            if (null == provider)
                valid = false;
        }
    }

    private Collection<TraceBackendProvider> createStrongView(Collection<SoftReference<TraceBackendProvider>> providerReferences) {
        final List<TraceBackendProvider> strongRefs = new ArrayList<>(providerReferences.size());
        for (SoftReference<TraceBackendProvider> providerSoftReference : providerReferences) {
            strongRefs.add(providerSoftReference.get());
        }
        return strongRefs;
    }

}
