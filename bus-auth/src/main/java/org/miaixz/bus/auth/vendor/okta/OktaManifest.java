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
package org.miaixz.bus.auth.vendor.okta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Realm;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the Okta custom authorization-server login and service-app management manifests.
 * <p>
 * The organization label and authorization-server identifier are deployment selectors, while every host suffix,
 * endpoint path, HTTP method, client authentication method, scope default, and signing algorithm remains frozen by this
 * immutable profile. The resolved issuer is used unchanged by Discovery, authorization-response, and ID Token
 * validation. The independent management Variant uses a private-key JWT and fixed organization-scoped Management API
 * templates to expose provider-neutral enterprise snapshot and retrieval operations.
 * </p>
 *
 * @author Kimi Liu
 */
public class OktaManifest implements VariantManifest<OktaOptions> {

    /**
     * Stable Okta platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("okta");

    /**
     * Stable identifier of the custom authorization-server variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the Okta service-app management Variant.
     */
    public static final Vendor.Variant MANAGEMENT = new Vendor.Variant("management");
    /**
     * Ordered Okta Management API scopes accepted by the service-app Variant.
     */
    static final List<String> MANAGEMENT_SCOPES = List.of("okta.users.read", "okta.groups.read", "okta.roles.read");
    /**
     * Manifest-owned issuer template resolved from the two external Okta selectors.
     */
    private static final String ISSUER = "https://{instance}.okta.com/oauth2/{authorizationServerId}";
    /**
     * Exact Source authentication and standard OIDC operations exposed by the Okta adapter.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OpenIdClientScheme.AUTHENTICATION,
            OpenIdClientScheme.TOKEN,
            OpenIdClientScheme.REVOCATION,
            OpenIdClientScheme.DISCOVERY,
            OpenIdClientScheme.JWK_SET,
            OpenIdClientScheme.USERINFO));
    /**
     * Exact provider-neutral capabilities exposed by the management Variant.
     */
    private static final Capability.Manifest ENTERPRISE_CAPABILITIES = new Capability.Manifest(
            List.of(Realm.describe(ID), Realm.snapshot(ID), Realm.retrieve(ID)));
    /**
     * Historical default Okta login scopes preserved in deterministic request order.
     */
    private static final List<String> DEFAULT_SCOPES = List
            .of("openid", "profile", "email", "address", "phone", "offline_access");
    /**
     * Frozen provider-neutral coverage description for Okta service-app management.
     */
    private static final Realm.Description ENTERPRISE_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.GROUP, Realm.Kind.ROLE),
            Set.of(Realm.RelationKind.MEMBER, Realm.RelationKind.ROLE_MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.UNKNOWN, Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE,
            List.of(
                    "admin-role-scope-controls-visible-resources",
                    "organization-and-service-apps-not-enumerated",
                    Builder.ENTERPRISE_LIMITATION_REPEATED_RESOURCES,
                    Builder.ENTERPRISE_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Complete immutable Okta endpoint, client, scope, capability, and form manifest.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VariantManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, DEFAULT_SCOPES,
            new VendorTargets(
                    Optional.of(template(ISSUER + "/v1/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            template(
                                    ISSUER + "/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.of(template(ISSUER + "/v1/userinfo", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.of(
                            template(
                                    ISSUER + "/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.empty(),
                    Optional.of(
                            template(
                                    ISSUER + "/v1/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_BASIC)),
                    Optional.empty(),
                    Optional.of(
                            template(
                                    ISSUER + "/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(template(ISSUER + "/v1/keys", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES, List.of());

    /**
     * Complete immutable Okta service-app management manifest.
     */
    private static final VariantManifest.Variant MANAGEMENT_VARIANT = new VariantManifest.Variant(ID, MANAGEMENT,
            Protocol.HTTPS, VariantManifest.Pkce.DISABLED, Credential.Type.PRIVATE_KEY, MANAGEMENT_SCOPES,
            new VendorTargets(Optional.empty(),
                    Optional.of(
                            template(
                                    "https://{instance}.okta.com/oauth2/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.PRIVATE_KEY_JWT)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), managementTargets()),
            ENTERPRISE_CAPABILITIES, List.of());

    /**
     * Creates the stateless Okta manifest used by Vendor directory assembly.
     */
    public OktaManifest() {
    }

    /**
     * Returns the frozen Okta management coverage description.
     *
     * @return immutable management description
     */
    static Realm.Description enterpriseDescription() {
        return ENTERPRISE_DESCRIPTION;
    }

    /**
     * Creates the exact ordered Okta Management API target declarations.
     *
     * @return immutable-by-construction management target map
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(
                Builder.ENTERPRISE_USERS,
                template("https://{instance}.okta.com/api/v1/users", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_USER,
                template("https://{instance}.okta.com/api/v1/users", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_GROUPS,
                template("https://{instance}.okta.com/api/v1/groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_GROUP,
                template("https://{instance}.okta.com/api/v1/groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_GROUP_MEMBERS,
                template("https://{instance}.okta.com/api/v1/groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_ROLES,
                template("https://{instance}.okta.com/api/v1", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_ROLE_MEMBERS,
                template("https://{instance}.okta.com/api/v1", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.ENTERPRISE_ROLE_ASSIGNMENTS,
                template("https://{instance}.okta.com/api/v1", Http.Method.GET, Endpoint.Authentication.BEARER));
        return targets;
    }

    /**
     * Creates one constrained Okta HTTPS endpoint template.
     *
     * @param value          manifest-owned endpoint template
     * @param method         standard operation HTTP method
     * @param authentication endpoint authentication declaration
     * @return immutable constrained endpoint target
     */
    private static VendorTargets.Template template(
            final String value,
            final Http.Method method,
            final Endpoint.Authentication authentication) {
        return new VendorTargets.Template(value, method, Set.of(authentication), Optional.empty(), TlsClientAuth.NONE);
    }

    /**
     * Returns the one-time Okta client form for organization and authorization-server selectors.
     *
     * @return immutable Okta client configuration form
     */
    @Override
    public Scheme.Form form() {
        return VariantManifest.Forms.extended(
                false,
                List.of(
                        VariantManifest.Forms.field("instance", "Okta organization", Scheme.Form.Type.TEXT, true),
                        VariantManifest.Forms.field(
                                "authorizationServerId",
                                "Authorization server identifier",
                                Scheme.Form.Type.TEXT,
                                false)));
    }

    /**
     * Returns the stable Okta routing identifier.
     *
     * @return Okta platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Okta management metadata.
     *
     * @return immutable Okta presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Okta", "Okta custom authorization-server sign-in", "okta");
    }

    /**
     * Returns the login Variant followed by the service-app management Variant.
     *
     * @return immutable two-Variant manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT, MANAGEMENT_VARIANT);
    }

    /**
     * Resolves the exact Okta login or management manifest.
     *
     * @param variant requested Okta variant
     * @return immutable matching manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        if (MANAGEMENT.equals(variant)) {
            return MANAGEMENT_VARIANT;
        }
        throw new ValidateException("Okta Vendor variant is not supported");
    }

}
