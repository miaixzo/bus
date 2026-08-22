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
package org.miaixz.bus.auth.vendor.aliyun;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Realm;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
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
 * Declares the frozen Alibaba Cloud OpenID Connect and RAM management Vendor manifests.
 *
 * @author Kimi Liu
 */
public class AliyunManifest implements VariantManifest<AliyunOptions> {

    /**
     * Stable platform routing identifier shared by registration, catalog, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("aliyun");

    /**
     * Internal identifier of the single Alibaba Cloud OpenID Connect variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the Alibaba Cloud RAM management Variant.
     */
    public static final Vendor.Variant RAM = new Vendor.Variant("ram");

    /**
     * Alibaba Cloud RAM request authentication using the official signature V3 template.
     */
    private static final Endpoint.Authentication ACS3 = new Endpoint.Authentication("acs3_hmac_sha256");

    /**
     * Exact public operations supported by the compiled Alibaba Cloud Source.
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
     * Exact provider-neutral capabilities exposed by the RAM management Variant.
     */
    private static final Capability.Manifest RAM_CAPABILITIES = new Capability.Manifest(
            List.of(Realm.describe(ID), Realm.snapshot(ID), Realm.retrieve(ID)));

    /**
     * Frozen provider-neutral coverage of identities visible through RAM APIs.
     */
    private static final Realm.Description RAM_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.GROUP, Realm.Kind.ROLE),
            Set.of(Realm.RelationKind.MEMBER, Realm.RelationKind.ROLE_MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.PARTIAL, Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE,
            List.of(
                    "ram-api-visible-identities-only",
                    "role-membership-requires-resolvable-ram-user-principal",
                    Builder.ENTERPRISE_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Complete immutable endpoint, client-policy, scope, capability, and form manifest for the default variant.
     */
    private static final VariantManifest.Variant VARIANT = new VariantManifest.Variant(ID, DEFAULT, Protocol.OIDC,
            VariantManifest.Pkce.REQUIRED, Credential.Type.CLIENT_SECRET,
            List.of(OpenIdConnect.Scopes.OPENID, OpenIdConnect.Scopes.PROFILE),
            new VendorTargets(Optional.of(
                    fixed("https://signin.aliyun.com/oauth2/v1/auth", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/userinfo",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/token",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/v1/revoke",
                                    Http.Method.POST,
                                    Endpoint.Authentication.CLIENT_SECRET_POST)),
                    Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://oauth.aliyun.com/.well-known/openid-configuration",
                                    Http.Method.GET,
                                    Endpoint.Authentication.NONE)),
                    Optional.of(
                            fixed("https://oauth.aliyun.com/v1/keys", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty()),
            CAPABILITIES, List.of());

    /**
     * Complete immutable Alibaba Cloud RAM management manifest.
     */
    private static final VariantManifest.Variant RAM_VARIANT = new VariantManifest.Variant(ID, RAM, Protocol.HTTPS,
            VariantManifest.Pkce.DISABLED, Credential.Type.SHARED_SECRET, List.of(),
            new VendorTargets(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    managementTargets()),
            RAM_CAPABILITIES, List.of());

    /**
     * Creates the stateless Alibaba Cloud manifest used by Vendor directory assembly.
     */
    public AliyunManifest() {
    }

    /**
     * Returns the frozen Alibaba Cloud RAM coverage description.
     *
     * @return immutable coverage description
     */
    static Realm.Description enterpriseDescription() {
        return RAM_DESCRIPTION;
    }

    /**
     * Creates the ordered RAM RPC management target closure.
     *
     * @return fresh ordered management target map consumed by the immutable manifest
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final VendorTargets.Target ram = fixed("https://ram.aliyuncs.com/", Http.Method.POST, ACS3);
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(Builder.ENTERPRISE_USERS, ram);
        targets.put(Builder.ENTERPRISE_USER, ram);
        targets.put(Builder.ENTERPRISE_GROUPS, ram);
        targets.put(Builder.ENTERPRISE_GROUP, ram);
        targets.put(Builder.ENTERPRISE_GROUP_MEMBERS, ram);
        targets.put(Builder.ENTERPRISE_ROLES, ram);
        targets.put(Builder.ENTERPRISE_ROLE, ram);
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
     * Returns the stable platform identifier used to select this manifest.
     *
     * @return stable Alibaba Cloud routing identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Alibaba Cloud presentation metadata for external management catalogs.
     *
     * @return immutable Alibaba Cloud presentation metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Alibaba Cloud", "Alibaba Cloud OpenID Connect sign-in", "aliyun");
    }

    /**
     * Returns the login Variant followed by the RAM management Variant.
     *
     * @return immutable two-Variant manifest list
     */
    @Override
    public List<VariantManifest.Variant> variants() {
        return List.of(VARIANT, RAM_VARIANT);
    }

    /**
     * Returns one exact supported manifest.
     *
     * @param variant requested variant
     * @return exact default Alibaba Cloud manifest
     * @throws ValidateException if the requested variant is not supported
     */
    @Override
    public VariantManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        if (RAM.equals(variant)) {
            return RAM_VARIANT;
        }
        throw new ValidateException("Alibaba Cloud Vendor variant is not supported");
    }

}
