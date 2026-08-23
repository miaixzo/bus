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
package org.miaixz.bus.fabric.network.dns.resolve;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.dnssec.DnsSigningKey;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstreamHealth;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.policy.DnsPolicyIndex;
import org.miaixz.bus.fabric.network.dns.policy.DnsPolicyRule;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.server.DnsTsigKey;
import org.miaixz.bus.fabric.network.dns.zone.DnsSnapshot;
import org.miaixz.bus.fabric.network.dns.zone.DnsTrustAnchor;
import org.miaixz.bus.fabric.network.dns.zone.DnsView;
import org.miaixz.bus.fabric.network.dns.zone.DnsZone;

/**
 * Immutable runtime lookup index compiled from a DNS snapshot.
 *
 * @author Kimi Liu
 */
public class RuntimeIndex {

    /**
     * Source snapshot.
     */
    private final DnsSnapshot snapshot;

    /**
     * Compiled view indexes.
     */
    private final List<ViewIndex> views;

    /**
     * Default view index.
     */
    private final ViewIndex defaultView;

    /**
     * Compiled global policy index.
     */
    private final DnsPolicyIndex globalPolicyIndex;

    /**
     * DNSKEY records indexed by DNSSEC key tag.
     */
    private final Map<Integer, List<DnsRecord>> dnskeyByKeyTag;

    /**
     * DS records indexed by owner name.
     */
    private final Map<String, List<DnsRecord>> dsByOwner;

    /**
     * Trust anchors indexed by DNSSEC key tag.
     */
    private final Map<Integer, List<DnsTrustAnchor>> trustAnchorsByKeyTag;

    /**
     * Shared upstream health reference used by hot-path forwarding.
     */
    private final DnsUpstreamHealth upstreamHealth;

    /**
     * Upstream health references indexed by stable upstream health key.
     */
    private final Map<String, DnsUpstreamHealth> upstreamHealthRefs;

    /**
     * Creates a runtime index.
     *
     * @param snapshot source DNS snapshot
     */
    public RuntimeIndex(final DnsSnapshot snapshot) {
        if (snapshot == null) {
            throw new ValidateException("DNS snapshot must not be null");
        }
        this.snapshot = snapshot;
        this.views = compileViews(snapshot);
        this.defaultView = defaultView(this.views);
        this.globalPolicyIndex = DnsPolicyIndex.compile(snapshot.policies());
        this.dnskeyByKeyTag = compileDnskeyIndex(snapshot);
        this.dsByOwner = compileDsIndex(snapshot);
        this.trustAnchorsByKeyTag = compileTrustAnchorIndex(snapshot);
        this.upstreamHealth = new DnsUpstreamHealth();
        this.upstreamHealthRefs = compileUpstreamHealthRefs(snapshot, upstreamHealth);
    }

    /**
     * Compiles a snapshot into a runtime index.
     *
     * @param snapshot source DNS snapshot
     * @return immutable runtime index
     */
    public static RuntimeIndex compile(final DnsSnapshot snapshot) {
        return new RuntimeIndex(snapshot);
    }

    /**
     * Compiles and atomically installs a snapshot into an index reference.
     *
     * @param target   runtime index reference
     * @param snapshot source DNS snapshot
     * @return compiled runtime index
     */
    public static RuntimeIndex replace(final AtomicReference<RuntimeIndex> target, final DnsSnapshot snapshot) {
        if (target == null) {
            throw new ValidateException("DNS runtime index reference must not be null");
        }
        final RuntimeIndex compiled = compile(snapshot);
        target.set(compiled);
        return compiled;
    }

    /**
     * Returns the source snapshot.
     *
     * @return immutable snapshot
     */
    public DnsSnapshot snapshot() {
        return snapshot;
    }

    /**
     * Returns global policy rules from the active snapshot.
     *
     * @return immutable global policy rules
     */
    public List<DnsPolicyRule> globalPolicies() {
        return globalPolicyIndex.rules();
    }

    /**
     * Returns the compiled global policy index.
     *
     * @return compiled global policy index
     */
    public DnsPolicyIndex globalPolicyIndex() {
        return globalPolicyIndex;
    }

    /**
     * Returns global upstream DNS servers from the active snapshot.
     *
     * @return immutable upstream DNS servers
     */
    public List<DnsUpstream> upstreams() {
        return snapshot.upstreams();
    }

    /**
     * Returns DNSSEC trust anchors from the active snapshot.
     *
     * @return immutable DNSSEC trust anchors
     */
    public List<DnsTrustAnchor> dnssecTrustAnchors() {
        return snapshot.dnssecTrustAnchors();
    }

    /**
     * Returns DNSKEY records for a key tag.
     *
     * @param keyTag unsigned 16-bit key tag
     * @return immutable DNSKEY records
     */
    public List<DnsRecord> dnskeysByKeyTag(final int keyTag) {
        return dnskeyByKeyTag.getOrDefault(DnsCodec.validateUnsignedShort(keyTag, "DNSSEC key tag"), List.of());
    }

    /**
     * Returns DS records for an owner name.
     *
     * @param owner owner name
     * @return immutable DS records
     */
    public List<DnsRecord> dsRecords(final String owner) {
        return dsByOwner.getOrDefault(DnsName.normalize(owner), List.of());
    }

    /**
     * Returns trust anchors for a key tag.
     *
     * @param keyTag unsigned 16-bit key tag
     * @return immutable trust anchors
     */
    public List<DnsTrustAnchor> trustAnchorsByKeyTag(final int keyTag) {
        return trustAnchorsByKeyTag
                .getOrDefault(DnsCodec.validateUnsignedShort(keyTag, "DNSSEC trust anchor key tag"), List.of());
    }

    /**
     * Returns the shared upstream health reference.
     *
     * @return upstream health reference
     */
    public DnsUpstreamHealth upstreamHealth() {
        return upstreamHealth;
    }

    /**
     * Returns upstream health references by upstream health key.
     *
     * @return immutable upstream health references
     */
    public Map<String, DnsUpstreamHealth> upstreamHealthRefs() {
        return upstreamHealthRefs;
    }

    /**
     * Returns TSIG keys from the active snapshot.
     *
     * @return immutable TSIG keys
     */
    public List<DnsTsigKey> tsigKeys() {
        return snapshot.tsigKeys();
    }

    /**
     * Finds the best matching zone for a query name.
     *
     * @param name query name
     * @return matching zone, or {@code null}
     */
    public DnsZone findZone(final String name) {
        return findZone(name, null);
    }

    /**
     * Finds the best matching zone for a query name and client address.
     *
     * @param name          query name
     * @param clientAddress client address, or {@code null} when unavailable
     * @return matching zone, or {@code null}
     */
    public DnsZone findZone(final String name, final InetAddress clientAddress) {
        return selectView(clientAddress).findZone(name);
    }

    /**
     * Returns view-scoped policies selected for a client address.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return immutable selected view policies
     */
    public List<DnsPolicyRule> viewPolicies(final InetAddress clientAddress) {
        return viewPolicyIndex(clientAddress).rules();
    }

    /**
     * Returns the compiled policy index selected for a client address.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return compiled selected-view policy index
     */
    public DnsPolicyIndex viewPolicyIndex(final InetAddress clientAddress) {
        return selectView(clientAddress).policyIndex;
    }

    /**
     * Returns the effective compiled policy index selected for a client address.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return compiled effective policy index
     */
    public DnsPolicyIndex policyIndex(final InetAddress clientAddress) {
        return selectView(clientAddress).effectivePolicyIndex;
    }

    /**
     * Returns the selected view name for a client address.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return selected view name
     */
    public String viewName(final InetAddress clientAddress) {
        return selectView(clientAddress).view.name();
    }

    /**
     * Selects the best view for a client address.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return selected view index
     */
    private ViewIndex selectView(final InetAddress clientAddress) {
        if (clientAddress == null) {
            return defaultView;
        }
        ViewIndex selected = null;
        int selectedPrefix = -1;
        for (final ViewIndex view : views) {
            final int prefix = view.view.matchPrefixLength(clientAddress);
            if (prefix < 0) {
                continue;
            }
            if (selected == null || prefix > selectedPrefix
                    || (prefix == selectedPrefix && view.view.name().compareTo(selected.view.name()) < 0)) {
                selected = view;
                selectedPrefix = prefix;
            }
        }
        return selected == null ? defaultView : selected;
    }

    /**
     * Compiles all views.
     *
     * @param snapshot source snapshot
     * @return immutable compiled views
     */
    private static List<ViewIndex> compileViews(final DnsSnapshot snapshot) {
        final ArrayList<ViewIndex> result = new ArrayList<>();
        for (final DnsView view : snapshot.views()) {
            final ArrayList<DnsPolicyRule> effectivePolicies = new ArrayList<>(snapshot.policies());
            effectivePolicies.addAll(view.policies());
            result.add(
                    new ViewIndex(view, compileZones(view.zones()), DnsPolicyIndex.compile(view.policies()),
                            DnsPolicyIndex.compile(effectivePolicies)));
        }
        return List.copyOf(result);
    }

    /**
     * Selects the default view index.
     *
     * @param views compiled views
     * @return default view index
     */
    private static ViewIndex defaultView(final List<ViewIndex> views) {
        for (final ViewIndex view : views) {
            if (DnsView.DEFAULT.equals(view.view.name())) {
                return view;
            }
        }
        return views.getFirst();
    }

    /**
     * Compiles zones into longest-origin order.
     *
     * @param zones source zones
     * @return immutable sorted zones
     */
    private static List<DnsZone> compileZones(final List<DnsZone> zones) {
        final ArrayList<DnsZone> result = new ArrayList<>(zones);
        result.sort(Comparator.comparingInt((DnsZone zone) -> zone.origin().length()).reversed());
        return List.copyOf(result);
    }

    /**
     * Compiles DNSKEY records by key tag.
     *
     * @param snapshot source snapshot
     * @return immutable DNSKEY key-tag index
     */
    private static Map<Integer, List<DnsRecord>> compileDnskeyIndex(final DnsSnapshot snapshot) {
        final HashMap<Integer, List<DnsRecord>> mutable = new HashMap<>();
        for (final DnsView view : snapshot.views()) {
            for (final DnsZone zone : view.zones()) {
                indexDnskeys(mutable, zone.records());
                indexSigningKeys(mutable, zone.signingKeys());
            }
        }
        return immutableRecordIndex(mutable);
    }

    /**
     * Indexes DNSKEY records.
     *
     * @param target  target index
     * @param records source records
     */
    private static void indexDnskeys(final Map<Integer, List<DnsRecord>> target, final List<DnsRecord> records) {
        for (final DnsRecord record : records) {
            if (record.typeCode() == DnsRecordType.DNSKEY.code()) {
                target.computeIfAbsent(DnsSigningKey.keyTag(record.wireData()), ignored -> new ArrayList<>())
                        .add(record);
            }
        }
    }

    /**
     * Indexes signing keys as DNSKEY records.
     *
     * @param target      target index
     * @param signingKeys source signing keys
     */
    private static void indexSigningKeys(
            final Map<Integer, List<DnsRecord>> target,
            final List<DnsSigningKey> signingKeys) {
        for (final DnsSigningKey signingKey : signingKeys) {
            target.computeIfAbsent(signingKey.keyTag(), ignored -> new ArrayList<>()).add(signingKey.dnskeyRecord(0L));
        }
    }

    /**
     * Compiles DS records by owner name.
     *
     * @param snapshot source snapshot
     * @return immutable DS owner index
     */
    private static Map<String, List<DnsRecord>> compileDsIndex(final DnsSnapshot snapshot) {
        final HashMap<String, List<DnsRecord>> mutable = new HashMap<>();
        for (final DnsView view : snapshot.views()) {
            for (final DnsZone zone : view.zones()) {
                for (final DnsRecord record : zone.records()) {
                    if (record.typeCode() == DnsRecordType.DS.code()) {
                        mutable.computeIfAbsent(record.name(), ignored -> new ArrayList<>()).add(record);
                    }
                }
            }
        }
        return immutableStringRecordIndex(mutable);
    }

    /**
     * Compiles trust anchors by key tag.
     *
     * @param snapshot source snapshot
     * @return immutable trust-anchor key-tag index
     */
    private static Map<Integer, List<DnsTrustAnchor>> compileTrustAnchorIndex(final DnsSnapshot snapshot) {
        final HashMap<Integer, List<DnsTrustAnchor>> mutable = new HashMap<>();
        for (final DnsTrustAnchor trustAnchor : snapshot.dnssecTrustAnchors()) {
            mutable.computeIfAbsent(trustAnchor.keyTag(), ignored -> new ArrayList<>()).add(trustAnchor);
        }
        final HashMap<Integer, List<DnsTrustAnchor>> immutable = new HashMap<>();
        for (final Map.Entry<Integer, List<DnsTrustAnchor>> entry : mutable.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(immutable);
    }

    /**
     * Compiles upstream health references by stable health key.
     *
     * @param snapshot source snapshot
     * @param health   shared upstream health reference
     * @return immutable upstream health reference map
     */
    private static Map<String, DnsUpstreamHealth> compileUpstreamHealthRefs(
            final DnsSnapshot snapshot,
            final DnsUpstreamHealth health) {
        final HashMap<String, DnsUpstreamHealth> refs = new HashMap<>();
        for (final DnsUpstream upstream : snapshot.upstreams()) {
            refs.put(health.healthKey(upstream), health);
        }
        for (final DnsView view : snapshot.views()) {
            for (final DnsZone zone : view.zones()) {
                for (final DnsUpstream upstream : zone.upstreams()) {
                    refs.put(health.healthKey(upstream), health);
                }
            }
        }
        return Map.copyOf(refs);
    }

    /**
     * Creates an immutable integer-key record index.
     *
     * @param source mutable source index
     * @return immutable index
     */
    private static Map<Integer, List<DnsRecord>> immutableRecordIndex(final Map<Integer, List<DnsRecord>> source) {
        final HashMap<Integer, List<DnsRecord>> immutable = new HashMap<>();
        for (final Map.Entry<Integer, List<DnsRecord>> entry : source.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(immutable);
    }

    /**
     * Creates an immutable string-key record index.
     *
     * @param source mutable source index
     * @return immutable index
     */
    private static Map<String, List<DnsRecord>> immutableStringRecordIndex(final Map<String, List<DnsRecord>> source) {
        final HashMap<String, List<DnsRecord>> immutable = new HashMap<>();
        for (final Map.Entry<String, List<DnsRecord>> entry : source.entrySet()) {
            immutable.put(DnsName.normalize(entry.getKey()), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(immutable);
    }

    /**
     * Compiled DNS view index.
     *
     * @author Kimi Liu
     */
    private static final class ViewIndex {

        /**
         * Source DNS view.
         */
        private final DnsView view;

        /**
         * Zones sorted by longest origin first.
         */
        private final List<DnsZone> zones;

        /**
         * Compiled view policy index.
         */
        private final DnsPolicyIndex policyIndex;

        /**
         * Compiled global plus view policy index.
         */
        private final DnsPolicyIndex effectivePolicyIndex;

        /**
         * Creates a compiled view index.
         *
         * @param view                 source DNS view
         * @param zones                zones sorted by longest origin first
         * @param policyIndex          compiled view policy index
         * @param effectivePolicyIndex compiled global plus view policy index
         */
        private ViewIndex(final DnsView view, final List<DnsZone> zones, final DnsPolicyIndex policyIndex,
                final DnsPolicyIndex effectivePolicyIndex) {
            this.view = view;
            this.zones = zones;
            this.policyIndex = policyIndex;
            this.effectivePolicyIndex = effectivePolicyIndex;
        }

        /**
         * Finds the best matching zone for a query name inside this view.
         *
         * @param name query name
         * @return matching zone, or {@code null}
         */
        private DnsZone findZone(final String name) {
            for (final DnsZone zone : zones) {
                if (zone.contains(name)) {
                    return zone;
                }
            }
            return null;
        }

    }

}
