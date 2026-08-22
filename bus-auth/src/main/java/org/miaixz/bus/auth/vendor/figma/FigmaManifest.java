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
package org.miaixz.bus.auth.vendor.figma;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
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
 * Declares the frozen Figma OAuth browser and administrator SCIM Vendor manifests.
 * <p>
 * The authorization endpoint remains a standard OAuth operation with mandatory S256 PKCE. Figma token, refresh, and
 * REST user behavior remain private Source-authentication operations because their complete public wire contracts do
 * not satisfy the framework's standard token or UserInfo capability boundaries. The independent SCIM Variant exposes
 * tenant-scoped Users and Groups targets for provider-neutral snapshot and retrieval.
 * </p>
 *
 * @author Kimi Liu
 */
public class FigmaManifest implements VariantManifest<FigmaOptions> {

    /**
     * Stable Figma platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("figma");

    /**
     * Stable identifier of the sole Figma.com OAuth variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the Figma administrator SCIM Variant.
     */
    public static final Vendor.Variant SCIM = new Vendor.Variant("scim");

    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Exact provider-neutral capabilities exposed by the administrator SCIM Variant.
     */
    private static final Capability.Manifest ENTERPRISE_CAPABILITIES = new Capability.Manifest(
            List.of(Realm.describe(ID), Realm.snapshot(ID), Realm.retrieve(ID)));

    /**
     * Frozen provider-neutral coverage description for Figma administrator SCIM.
     */
    private static final Realm.Description ENTERPRISE_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.GROUP), Set.of(Realm.RelationKind.MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.UNKNOWN, Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE,
            List.of(
                    "scim-managed-identities-only",
                    "starter-and-professional-plans-not-supported",
                    "retrieve-scans-paginated-collection-by-stable-id",
                    Builder.ENTERPRISE_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Exact Figma wire differences retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "user_id_string",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "id",
                    JwtClaims.SUBJECT,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VariantManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of("current_user:read"),
            new VendorTargets(
                    Optional.of(fixed("https://www.figma.com/oauth", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://api.figma.com/v1/oauth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.of(fixed("https://api.figma.com/v1/me", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://api.figma.com/v1/oauth/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Complete immutable Figma administrator SCIM manifest.
     */
    private static final VariantManifest.Variant SCIM_VARIANT = new VariantManifest.Variant(ID, SCIM, Protocol.SCIM,
            VariantManifest.Pkce.DISABLED, Credential.Type.SHARED_SECRET, List.of(),
            new VendorTargets(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    managementTargets()),
            ENTERPRISE_CAPABILITIES, List.of());

    /**
     * Creates the stateless Figma manifest used by Vendor directory assembly.
     */
    public FigmaManifest() {
    }

    /**
     * Returns the frozen Figma administrator SCIM description.
     *
     * @return immutable SCIM description
     */
    static Realm.Description enterpriseDescription() {
        return ENTERPRISE_DESCRIPTION;
    }

    /**
     * Creates the exact ordered tenant-scoped Figma SCIM targets.
     *
     * @return immutable-by-construction management target map
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(
                Builder.ENTERPRISE_USERS,
                template(
                        "https://www.figma.com/scim/v2/{tenant}/Users",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_USER,
                template(
                        "https://www.figma.com/scim/v2/{tenant}/Users",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_GROUPS,
                template(
                        "https://www.figma.com/scim/v2/{tenant}/Groups",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_GROUP,
                template(
                        "https://www.figma.com/scim/v2/{tenant}/Groups",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        return targets;
    }

    /**
     * Creates one constrained tenant-scoped HTTPS target template.
     *
     * @param value          manifest-owned template
     * @param method         exact HTTP method
     * @param authentication endpoint authentication declaration
     * @return immutable constrained target
     */
    private static VendorTargets.Template template(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Template(value, method, Set.of(authentication), Optional.empty(), TlsClientAuth.NONE);
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
     * Creates one immutable registered Figma wire deviation.
     *
     * @param operation    affected Source operation
     * @param location     exact wire location
     * @param vendorName   exact Figma field name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether Figma wraps the response
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
     * Returns the stable Figma routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Figma presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Figma", "Figma OAuth app and current user login", "figma");
    }

    /**
     * Returns the login Variant followed by the administrator SCIM Variant.
     *
     * @return immutable two-Variant list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT, SCIM_VARIANT);
    }

    /**
     * Returns one exact supported Figma manifest.
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
        if (SCIM.equals(variant)) {
            return SCIM_VARIANT;
        }
        throw new ValidateException("Figma Vendor variant is not supported");
    }

}
