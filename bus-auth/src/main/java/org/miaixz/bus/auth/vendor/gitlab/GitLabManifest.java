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
package org.miaixz.bus.auth.vendor.gitlab;

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
 * Declares the frozen GitLab.com OAuth application and Enterprise REST management Vendor manifests.
 * <p>
 * Authorization, token, refresh-token grant, and revocation use the framework's standard OAuth models. The adapter
 * retains only GitLab's registered {@code created_at}, refresh {@code redirect_uri}, empty JSON revocation response,
 * and REST identity mapping as exact wire handling. The independent Enterprise Variant resolves a constrained GitLab
 * deployment and top-level group into the official Groups, Members, and Users REST surfaces without using SCIM.
 * </p>
 *
 * @author Kimi Liu
 */
public class GitLabManifest implements VariantManifest<GitLabOptions> {

    /**
     * Stable GitLab platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("gitlab");

    /**
     * Stable identifier of the sole GitLab.com OAuth App variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the GitLab Enterprise REST management Variant.
     */
    public static final Vendor.Variant ENTERPRISE = new Vendor.Variant("enterprise");

    /**
     * GitLab administrator Token authentication carried by the official PRIVATE-TOKEN header.
     */
    private static final Endpoint.Authentication PRIVATE_TOKEN = new Endpoint.Authentication("private_token_header");

    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN,
            OAuth2ClientScheme.REVOCATION));

    /**
     * Exact provider-neutral capabilities exposed by the Enterprise management Variant.
     */
    private static final Capability.Manifest ENTERPRISE_CAPABILITIES = new Capability.Manifest(
            List.of(Realm.describe(ID), Realm.snapshot(ID), Realm.retrieve(ID)));

    /**
     * Frozen provider-neutral GitLab group hierarchy and membership coverage.
     */
    private static final Realm.Description ENTERPRISE_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.ORGANIZATION),
            Set.of(Realm.RelationKind.PARENT, Realm.RelationKind.MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.PARTIAL, Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE,
            List.of(
                    "top-level-group-hierarchy-and-members-only",
                    "visibility-follows-token-membership",
                    Builder.ENTERPRISE_LIMITATION_REPEATED_RESOURCES,
                    "scim-identity-api-not-used",
                    Builder.ENTERPRISE_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Exact GitLab wire differences handled without changing public standard models.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "created_at",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.FORM,
                    OAuth2.Parameters.REDIRECT_URI,
                    OAuth2.Parameters.REDIRECT_URI,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "revoke",
                    VendorDeviation.Location.RESPONSE,
                    "{}",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VariantManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of("read_user"),
            new VendorTargets(
                    Optional.of(
                            fixed("https://gitlab.com/oauth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://gitlab.com/oauth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed("https://gitlab.com/api/v4/user", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://gitlab.com/oauth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://gitlab.com/oauth/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Complete immutable GitLab Enterprise REST management manifest.
     */
    private static final VariantManifest.Variant ENTERPRISE_VARIANT = new VariantManifest.Variant(ID, ENTERPRISE,
            Protocol.HTTPS, VariantManifest.Pkce.DISABLED, Credential.Type.SHARED_SECRET, List.of(),
            new VendorTargets(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    managementTargets()),
            ENTERPRISE_CAPABILITIES, List.of());

    /**
     * Creates the stateless GitLab manifest used by Vendor directory assembly.
     */
    public GitLabManifest() {
    }

    /**
     * Returns the frozen GitLab Enterprise REST coverage description.
     *
     * @return immutable coverage description
     */
    static Realm.Description enterpriseDescription() {
        return ENTERPRISE_DESCRIPTION;
    }

    /**
     * Creates the exact ordered constrained GitLab REST management targets.
     *
     * @return immutable-by-construction management target map
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(Builder.ENTERPRISE_USERS, template("https://{instance}/api/v4/users"));
        targets.put(Builder.ENTERPRISE_USER, template("https://{instance}/api/v4/users"));
        targets.put(Builder.ENTERPRISE_ORGANIZATIONS, template("https://{instance}/api/v4/groups/{tenant}"));
        targets.put(Builder.ENTERPRISE_ORGANIZATION, template("https://{instance}/api/v4/groups"));
        targets.put(Builder.ENTERPRISE_ORGANIZATION_USERS, template("https://{instance}/api/v4/groups"));
        return targets;
    }

    /**
     * Creates one constrained GitLab deployment target template.
     *
     * @param value manifest-owned endpoint template
     * @return immutable constrained target
     */
    private static VendorTargets.Template template(final String value) {
        return new VendorTargets.Template(value, Http.Method.GET, Set.of(PRIVATE_TOKEN), Optional.empty(),
                TlsClientAuth.NONE);
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
     * Creates one immutable registered GitLab wire deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact GitLab field name
     * @param standardName corresponding standard field
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether GitLab wraps the response
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
     * Returns the stable GitLab routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive GitLab presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("GitLab", "GitLab.com OAuth App login", "gitlab");
    }

    /**
     * Returns the login Variant followed by the Enterprise REST management Variant.
     *
     * @return immutable two-Variant list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT, ENTERPRISE_VARIANT);
    }

    /**
     * Returns one exact supported GitLab manifest.
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
        throw new ValidateException("GitLab Vendor variant is not supported");
    }

}
