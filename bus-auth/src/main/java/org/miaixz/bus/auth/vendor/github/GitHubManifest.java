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
package org.miaixz.bus.auth.vendor.github;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen GitHub.com OAuth App and Enterprise management Vendor manifests.
 * <p>
 * Standard OAuth authorization remains public with mandatory S256 PKCE. GitHub's authorization-code token request,
 * comma-delimited scope response, optional expiring-token refresh pair, and REST user representation remain private
 * Source-authentication behavior. The independent Enterprise Variant exposes the official enterprise-team surfaces
 * through provider-neutral directory capabilities and a separately managed read-only administrator token.
 * </p>
 *
 * @author Kimi Liu
 */
public class GitHubManifest implements VariantManifest<GitHubOptions> {

    /**
     * Stable GitHub platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("github");

    /**
     * Stable identifier of the sole GitHub.com OAuth App variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the GitHub Enterprise management Variant.
     */
    public static final Vendor.Variant ENTERPRISE = new Vendor.Variant("enterprise");

    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Exact provider-neutral capabilities exposed by the Enterprise management Variant.
     */
    private static final Capability.Manifest ENTERPRISE_CAPABILITIES = new Capability.Manifest(
            List.of(Realm.describe(ID), Realm.snapshot(ID), Realm.retrieve(ID)));

    /**
     * Frozen provider-neutral coverage description for GitHub Enterprise Teams.
     */
    private static final Realm.Description ENTERPRISE_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.GROUP), Set.of(Realm.RelationKind.MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.PARTIAL, Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE,
            List.of(
                    "enterprise-team-visible-members-and-assignments-only",
                    "classic-pat-read-enterprise-required",
                    Builder.ENTERPRISE_LIMITATION_REPEATED_RESOURCES,
                    Builder.ENTERPRISE_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Exact GitHub wire differences retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.FORM,
                    OAuth2.Parameters.GRANT_TYPE,
                    OAuth2.Parameters.GRANT_TYPE,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    OAuth2.Parameters.SCOPE,
                    OAuth2.Parameters.SCOPE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VariantManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of("read:user"),
            new VendorTargets(Optional.of(
                    fixed("https://github.com/login/oauth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://github.com/login/oauth/access_token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(fixed("https://api.github.com/user", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://github.com/login/oauth/access_token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Complete immutable GitHub Enterprise management manifest.
     */
    private static final VariantManifest.Variant ENTERPRISE_VARIANT = new VariantManifest.Variant(ID, ENTERPRISE,
            Protocol.HTTPS, VariantManifest.Pkce.DISABLED, Credential.Type.SHARED_SECRET, List.of(),
            new VendorTargets(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    managementTargets()),
            ENTERPRISE_CAPABILITIES, List.of());

    /**
     * Creates the stateless GitHub manifest used by Vendor directory assembly.
     */
    public GitHubManifest() {
    }

    /**
     * Returns the frozen GitHub Enterprise management description.
     *
     * @return immutable coverage description
     */
    static Realm.Description enterpriseDescription() {
        return ENTERPRISE_DESCRIPTION;
    }

    /**
     * Creates the exact ordered GitHub Enterprise management targets.
     *
     * @return immutable-by-construction management target map
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        final VendorTargets.Target teams = template("https://api.github.com/enterprises/{tenant}/teams");
        targets.put(Builder.ENTERPRISE_USERS, teams);
        targets.put(
                Builder.ENTERPRISE_USER,
                fixed("https://api.github.com/user", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(Builder.ENTERPRISE_ORGANIZATIONS, teams);
        targets.put(
                Builder.ENTERPRISE_ORGANIZATION,
                fixed("https://api.github.com/organizations", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(Builder.ENTERPRISE_GROUPS, teams);
        targets.put(Builder.ENTERPRISE_GROUP, teams);
        targets.put(Builder.ENTERPRISE_GROUP_MEMBERS, teams);
        targets.put(Builder.ENTERPRISE_ORGANIZATION_ASSIGNMENTS, teams);
        return targets;
    }

    /**
     * Creates one constrained Enterprise-slug HTTPS target template.
     *
     * @param value manifest-owned endpoint template
     * @return immutable constrained target
     */
    private static VendorTargets.Template template(final String value) {
        return new VendorTargets.Template(value, Http.Method.GET, Set.of(Endpoint.Authentication.BEARER),
                Optional.empty(), TlsClientAuth.NONE);
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
     * @param authentication endpoint authentication declaration
     * @return immutable fixed endpoint target
     */
    private static VendorTargets.Fixed fixed(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Fixed(new Endpoint(Url.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
                Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Creates one immutable registered GitHub wire deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact GitHub field name
     * @param standardName corresponding standard field
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether GitHub wraps the response
     * @return immutable deviation declaration
     */
    private static VendorDeviation deviation(
            final String operation,
            final VendorDeviation.Location location,
            final String vendorName,
            final String standardName,
            final Optional<MediaType> mediaType,
            final Http.Method method,
            final boolean enveloped) {
        return new VendorDeviation(operation, location, vendorName, Optional.ofNullable(standardName), mediaType,
                method, enveloped);
    }

    /**
     * Returns the stable GitHub routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive GitHub presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("GitHub", "GitHub.com OAuth App login", "github");
    }

    /**
     * Returns the login Variant followed by the Enterprise management Variant.
     *
     * @return immutable two-Variant list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT, ENTERPRISE_VARIANT);
    }

    /**
     * Returns one exact supported GitHub manifest.
     *
     * @param variant requested variant
     * @return exact immutable manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        if (ENTERPRISE.equals(variant)) {
            return ENTERPRISE_VARIANT;
        }
        throw new ValidateException("GitHub Vendor variant is not supported");
    }

}
