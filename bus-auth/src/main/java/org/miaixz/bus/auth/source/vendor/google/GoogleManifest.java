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
package org.miaixz.bus.auth.source.vendor.google;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorDeviation;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the frozen Google OpenID Connect login and Workspace Realm manifests.
 * <p>
 * The default Variant preserves Google browser sign-in. The independent Workspace Variant uses domain-wide delegation
 * and fixed Google Admin SDK endpoints to expose implementation-neutral Realm snapshots and stable-key retrieval.
 * </p>
 *
 * @author Kimi Liu
 */
public class GoogleManifest implements VendorManifest<GoogleOptions> {

    /**
     * Stable platform routing identifier shared by registration, Source lookup, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("google");

    /**
     * Internal identifier of the Google OpenID Connect login Variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Internal identifier of the Google Workspace Realm Variant.
     */
    public static final Vendor.Variant WORKSPACE = new Vendor.Variant("workspace");
    /**
     * Ordered read-only Google Admin SDK scopes delegated to the configured administrator.
     */
    static final List<String> WORKSPACE_SCOPES = List.of(
            "https://www.googleapis.com/auth/admin.directory.user.readonly",
            "https://www.googleapis.com/auth/admin.directory.group.readonly",
            "https://www.googleapis.com/auth/admin.directory.orgunit.readonly",
            "https://www.googleapis.com/auth/admin.directory.rolemanagement.readonly");
    /**
     * Exact public operations supported by the compiled Google Source.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.AUTHENTICATION,
            OAuth2ClientScheme.TOKEN,
            OAuth2ClientScheme.REVOCATION,
            OpenIdClientScheme.DISCOVERY,
            OpenIdClientScheme.JWK_SET,
            OpenIdClientScheme.USERINFO));
    /**
     * Frozen implementation-neutral coverage description for the Google Workspace Variant.
     */
    private static final Realm.Description REALM_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.GROUP, Realm.Kind.ROLE),
            Set.of(Realm.RelationKind.PARENT, Realm.RelationKind.MEMBER, Realm.RelationKind.ROLE_MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.UNKNOWN, Builder.MAXIMUM_REALM_PAGE_SIZE,
            List.of(
                    "domain-wide-delegation-required",
                    "admin-sdk-visibility-follows-delegated-admin",
                    Builder.REALM_LIMITATION_UNPAGED_REPLAY,
                    Builder.REALM_LIMITATION_REPLAY_CHANGE_FAILURE,
                    Builder.REALM_LIMITATION_SNAPSHOT_ONLY));
    /**
     * Exact implementation-neutral capabilities exposed by the Workspace Realm Variant.
     */
    private static final Capability.Manifest REALM_CAPABILITIES = Realm.manifest(REALM_DESCRIPTION);

    /**
     * Complete immutable endpoint, client-policy, scope, capability, and form manifest for the default variant.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VendorManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET, List.of("openid", "profile", "email"),
            new VendorTargets(
                    Optional.of(
                            fixed(
                                    "https://accounts.google.com/o/oauth2/v2/auth",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth2.googleapis.com/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://openidconnect.googleapis.com/v1/userinfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://oauth2.googleapis.com/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth2.googleapis.com/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://accounts.google.com/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://www.googleapis.com/oauth2/v3/certs",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES,
            List.of(
                    new VendorDeviation(OpenIdConnect.Parameters.ID_TOKEN, VendorDeviation.Location.RESPONSE,
                            "accounts.google.com", Optional.of(JwtClaims.ISSUER),
                            Optional.of(MediaType.APPLICATION_JSON_TYPE), Http.Method.POST, false),
                    new VendorDeviation(OAuth2.Parameters.TOKEN, VendorDeviation.Location.RESPONSE, "error_subtype",
                            Optional.empty(), Optional.of(MediaType.APPLICATION_JSON_TYPE), Http.Method.POST, false),
                    new VendorDeviation("revoke", VendorDeviation.Location.FORM, OAuth2.Parameters.TOKEN,
                            Optional.of(OAuth2.Parameters.TOKEN),
                            Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE), Http.Method.POST, false)));

    /**
     * Complete immutable Google Workspace Realm manifest.
     */
    private static final VendorManifest.Variant WORKSPACE_VARIANT = new VendorManifest.Variant(ID, WORKSPACE,
            Protocol.HTTPS, VendorManifest.Pkce.DISABLED, Credential.Type.PRIVATE_KEY, WORKSPACE_SCOPES,
            new VendorTargets(Optional.empty(), Optional
                    .of(fixed("https://oauth2.googleapis.com/token", Http.Method.POST, Endpoint.Authentication.NONE)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), managementTargets()),
            REALM_CAPABILITIES, List.of());

    /**
     * Creates the stateless Google manifest used by Vendor locator assembly.
     */
    public GoogleManifest() {
    }

    /**
     * Returns the frozen Google Workspace Realm coverage description.
     *
     * @return immutable Workspace Realm description
     */
    static Realm.Description realmDescription() {
        return REALM_DESCRIPTION;
    }

    /**
     * Creates the exact ordered Google Admin SDK management target declarations.
     *
     * @return immutable-by-construction management target map
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(
                Builder.REALM_USERS,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/users",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_USER,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/users",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ORGANIZATIONS,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/customer",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ORGANIZATION,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/customer",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUPS,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/groups",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUP,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/groups",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUP_MEMBERS,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/groups",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ROLES,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/customer",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ROLE_MEMBERS,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/customer",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ROLE_ASSIGNMENTS,
                fixed(
                        "https://admin.googleapis.com/admin/directory/v1/customer",
                        Http.Method.GET,
                        Endpoint.Authentication.BEARER));
        return targets;
    }

    /**
     * Creates one fixed HTTPS endpoint.
     *
     * @param value          exact credential-free endpoint URL
     * @param method         HTTP method used by the standard operation
     * @param authentication endpoint authentication
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
     * Returns the Google registration form including Workspace delegation selectors.
     *
     * @param variant exact Google variant whose form is requested
     * @return immutable Google registration form
     */
    @Override
    public Scheme.Form form(final Vendor.Variant variant) {
        final VendorManifest.Variant selected = variant(variant);
        return WORKSPACE.equals(variant) ? VendorManifest.Forms.extended(
                selected,
                List.of(
                        VendorManifest.Forms
                                .field("customer", "Google Workspace customer", Scheme.Form.Type.TEXT, true),
                        VendorManifest.Forms
                                .field("delegatedAdmin", "Delegated administrator", Scheme.Form.Type.TEXT, true)))
                : VendorManifest.Forms.common(selected);
    }

    /**
     * Returns the stable platform identifier used to select this manifest.
     *
     * @return stable Google routing identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Google presentation metadata for external management catalogs.
     *
     * @return immutable Google presentation metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Google", "Google OpenID Connect sign-in", "google");
    }

    /**
     * Returns the Google login Variant followed by the Workspace Realm Variant.
     *
     * @return immutable two-Variant manifest list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT, WORKSPACE_VARIANT);
    }

    /**
     * Returns the exact Google login or Workspace Realm manifest.
     *
     * @param variant requested variant
     * @return exact matching Google manifest
     * @throws ValidateException if the requested variant is not supported
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        if (WORKSPACE.equals(variant)) {
            return WORKSPACE_VARIANT;
        }
        throw new ValidateException("Google Vendor variant is not supported");
    }

}
