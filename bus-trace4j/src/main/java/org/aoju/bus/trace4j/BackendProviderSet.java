package org.aoju.bus.trace4j;

import org.aoju.bus.trace4j.backend.TraceBackendProvider;

import java.lang.ref.SoftReference;
import java.util.*;

public class BackendProviderSet extends AbstractSet<TraceBackendProvider> {

    private Set<SoftReference<TraceBackendProvider>> values;
    private boolean valid = true;

    BackendProviderSet(Set<TraceBackendProvider> elements) {
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
            if (provider == null)
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
