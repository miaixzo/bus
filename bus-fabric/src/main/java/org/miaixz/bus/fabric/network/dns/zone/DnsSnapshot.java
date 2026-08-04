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
package org.miaixz.bus.fabric.network.dns.zone;

import java.time.Instant;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.dnssec.DnsSigningKey;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.policy.DnsPolicyRule;
import org.miaixz.bus.fabric.network.dns.server.DnsTsigKey;

/**
 * Immutable DNS runtime snapshot supplied by an external control project.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsSnapshot {

    /**
     * Snapshot identifier.
     */
    private final String snapshotId;

    /**
     * Monotonic snapshot version.
     */
    private final long version;

    /**
     * Snapshot creation time.
     */
    private final Instant createdAt;

    /**
     * Snapshot views.
     */
    private final List<DnsView> views;

    /**
     * Global upstream DNS servers used when local zones do not answer a query.
     */
    private final List<DnsUpstream> upstreams;

    /**
     * Global policy rules evaluated before local resolution and forwarding.
     */
    private final List<DnsPolicyRule> policies;

    /**
     * DNSSEC trust anchors used by validation when DNSSEC is enabled.
     */
    private final List<DnsTrustAnchor> dnssecTrustAnchors;

    /**
     * TSIG shared-secret keys used by transfer and dynamic-update paths.
     */
    private final List<DnsTsigKey> tsigKeys;

    /**
     * Creates a DNS snapshot.
     *
     * @param snapshotId snapshot identifier
     * @param version    monotonic version
     * @param createdAt  creation time
     * @param views      DNS views
     */
    public DnsSnapshot(final String snapshotId, final long version, final Instant createdAt,
            final List<DnsView> views) {
        this(snapshotId, version, createdAt, views, List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Creates a DNS snapshot.
     *
     * @param snapshotId snapshot identifier
     * @param version    monotonic version
     * @param createdAt  creation time
     * @param views      DNS views
     * @param upstreams  global upstream DNS servers
     * @param policies   global policy rules
     */
    public DnsSnapshot(final String snapshotId, final long version, final Instant createdAt, final List<DnsView> views,
            final List<DnsUpstream> upstreams, final List<DnsPolicyRule> policies) {
        this(snapshotId, version, createdAt, views, upstreams, policies, List.of(), List.of());
    }

    /**
     * Creates a DNS snapshot.
     *
     * @param snapshotId         snapshot identifier
     * @param version            monotonic version
     * @param createdAt          creation time
     * @param views              DNS views
     * @param upstreams          global upstream DNS servers
     * @param policies           global policy rules
     * @param dnssecTrustAnchors DNSSEC trust anchors
     * @param tsigKeys           TSIG shared-secret keys
     */
    public DnsSnapshot(final String snapshotId, final long version, final Instant createdAt, final List<DnsView> views,
            final List<DnsUpstream> upstreams, final List<DnsPolicyRule> policies,
            final List<DnsTrustAnchor> dnssecTrustAnchors, final List<DnsTsigKey> tsigKeys) {
        if (snapshotId == null || snapshotId.isBlank()) {
            throw new ValidateException("DNS snapshot id must be non-blank");
        }
        if (version < 0L) {
            throw new ValidateException("DNS snapshot version must be non-negative");
        }
        if (createdAt == null) {
            throw new ValidateException("DNS snapshot creation time must not be null");
        }
        this.snapshotId = snapshotId.trim();
        this.version = version;
        this.createdAt = createdAt;
        this.views = immutableViews(views);
        this.upstreams = immutableUpstreams(upstreams);
        this.policies = immutablePolicies(policies);
        this.dnssecTrustAnchors = immutableTrustAnchors(dnssecTrustAnchors);
        this.tsigKeys = immutableTsigKeys(tsigKeys);
        validateSigningKeys(this.views, this.createdAt);
    }

    /**
     * Creates a snapshot containing one default view.
     *
     * @param snapshotId snapshot identifier
     * @param version    monotonic version
     * @param zones      zones visible in the default view
     * @return immutable DNS snapshot
     */
    public static DnsSnapshot defaults(final String snapshotId, final long version, final List<DnsZone> zones) {
        return new DnsSnapshot(snapshotId, version, Instant.now(), List.of(DnsView.defaults(zones)));
    }

    /**
     * Creates a snapshot containing one default view and global upstreams.
     *
     * @param snapshotId snapshot identifier
     * @param version    monotonic version
     * @param zones      zones visible in the default view
     * @param upstreams  global upstream DNS servers
     * @return immutable DNS snapshot
     */
    public static DnsSnapshot defaults(
            final String snapshotId,
            final long version,
            final List<DnsZone> zones,
            final List<DnsUpstream> upstreams) {
        return new DnsSnapshot(snapshotId, version, Instant.now(), List.of(DnsView.defaults(zones)), upstreams,
                List.of());
    }

    /**
     * Returns the snapshot identifier.
     *
     * @return non-blank snapshot identifier
     */
    public String snapshotId() {
        return snapshotId;
    }

    /**
     * Returns the snapshot version.
     *
     * @return non-negative snapshot version
     */
    public long version() {
        return version;
    }

    /**
     * Returns the creation time.
     *
     * @return snapshot creation time
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns the DNS views.
     *
     * @return immutable DNS views
     */
    public List<DnsView> views() {
        return views;
    }

    /**
     * Returns global upstream DNS servers.
     *
     * @return immutable upstream DNS servers
     */
    public List<DnsUpstream> upstreams() {
        return upstreams;
    }

    /**
     * Returns global policy rules.
     *
     * @return immutable policy rules
     */
    public List<DnsPolicyRule> policies() {
        return policies;
    }

    /**
     * Returns DNSSEC trust anchors.
     *
     * @return immutable trust anchors
     */
    public List<DnsTrustAnchor> dnssecTrustAnchors() {
        return dnssecTrustAnchors;
    }

    /**
     * Returns TSIG shared-secret keys.
     *
     * @return immutable TSIG keys
     */
    public List<DnsTsigKey> tsigKeys() {
        return tsigKeys;
    }

    /**
     * Validates and copies snapshot views.
     *
     * @param views source views
     * @return immutable views
     */
    private static List<DnsView> immutableViews(final List<DnsView> views) {
        if (views == null || views.isEmpty()) {
            throw new ValidateException("DNS snapshot must contain at least one view");
        }
        for (final DnsView view : views) {
            if (view == null) {
                throw new ValidateException("DNS snapshot views must not contain null");
            }
        }
        return List.copyOf(views);
    }

    /**
     * Validates and copies global upstreams.
     *
     * @param upstreams source upstreams
     * @return immutable upstreams
     */
    private static List<DnsUpstream> immutableUpstreams(final List<DnsUpstream> upstreams) {
        if (upstreams == null) {
            throw new ValidateException("DNS snapshot upstreams must not be null");
        }
        for (final DnsUpstream upstream : upstreams) {
            if (upstream == null) {
                throw new ValidateException("DNS snapshot upstreams must not contain null");
            }
        }
        return List.copyOf(upstreams);
    }

    /**
     * Validates and copies global policy rules.
     *
     * @param policies source policies
     * @return immutable policies
     */
    private static List<DnsPolicyRule> immutablePolicies(final List<DnsPolicyRule> policies) {
        if (policies == null) {
            throw new ValidateException("DNS snapshot policies must not be null");
        }
        for (final DnsPolicyRule policy : policies) {
            if (policy == null) {
                throw new ValidateException("DNS snapshot policies must not contain null");
            }
        }
        return List.copyOf(policies);
    }

    /**
     * Validates and copies DNSSEC trust anchors.
     *
     * @param dnssecTrustAnchors source trust anchors
     * @return immutable trust anchors
     */
    private static List<DnsTrustAnchor> immutableTrustAnchors(final List<DnsTrustAnchor> dnssecTrustAnchors) {
        if (dnssecTrustAnchors == null) {
            throw new ValidateException("DNS snapshot trust anchors must not be null");
        }
        for (final DnsTrustAnchor trustAnchor : dnssecTrustAnchors) {
            if (trustAnchor == null) {
                throw new ValidateException("DNS snapshot trust anchors must not contain null");
            }
        }
        return List.copyOf(dnssecTrustAnchors);
    }

    /**
     * Validates and copies TSIG keys.
     *
     * @param tsigKeys source TSIG keys
     * @return immutable TSIG keys
     */
    private static List<DnsTsigKey> immutableTsigKeys(final List<DnsTsigKey> tsigKeys) {
        if (tsigKeys == null) {
            throw new ValidateException("DNS snapshot TSIG keys must not be null");
        }
        for (final DnsTsigKey tsigKey : tsigKeys) {
            if (tsigKey == null) {
                throw new ValidateException("DNS snapshot TSIG keys must not contain null");
            }
        }
        return List.copyOf(tsigKeys);
    }

    /**
     * Validates all zone signing keys against the snapshot creation time.
     *
     * @param views     snapshot views
     * @param createdAt snapshot creation time
     */
    private static void validateSigningKeys(final List<DnsView> views, final Instant createdAt) {
        for (final DnsView view : views) {
            for (final DnsZone zone : view.zones()) {
                validateSigningKeys(zone, createdAt);
            }
        }
    }

    /**
     * Validates one zone's signing keys against the snapshot creation time.
     *
     * @param zone      zone to validate
     * @param createdAt snapshot creation time
     */
    private static void validateSigningKeys(final DnsZone zone, final Instant createdAt) {
        for (final DnsSigningKey signingKey : zone.signingKeys()) {
            if (!signingKey.activeAt(createdAt)) {
                throw new ValidateException("DNS snapshot contains inactive DNSSEC signing key");
            }
        }
    }

}
