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
package org.miaixz.bus.auth.source.vendor.dingtalk;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorDeviation;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares DingTalk's frozen browser-login, account-login, and enterprise directory Source variants.
 * <p>
 * The {@code oauth2} variant exposes only its standard authorization request as OAuth 2.0; its camel-case JSON token
 * and proprietary access-token header remain inside Source authentication. The {@code account} variant is a separate
 * signed temporary-code flow and never fabricates OAuth token operations. The {@code enterprise} variant independently
 * exposes implementation-neutral directory capabilities over DingTalk's official HTTPS management endpoints.
 * </p>
 *
 * @author Kimi Liu
 */
public class DingTalkManifest implements VendorManifest<DingTalkOptions> {

    /**
     * Stable platform routing identifier shared by registration, Source lookup, and runtime compilation.
     */
    public static final Vendor.Id ID = new Vendor.Id("dingtalk");

    /**
     * Stable identifier of the browser delegated OAuth 2.0 variant.
     */
    public static final Vendor.Variant OAUTH2 = new Vendor.Variant("oauth2");

    /**
     * Stable identifier of the signed account-login variant.
     */
    public static final Vendor.Variant ACCOUNT = new Vendor.Variant("account");

    /**
     * Stable identifier of the DingTalk enterprise directory variant.
     */
    public static final Vendor.Variant ENTERPRISE = new Vendor.Variant("enterprise");

    /**
     * DingTalk's registered JSON-body client authentication method.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_JSON = new Endpoint.Authentication("client_secret_json");

    /**
     * DingTalk's proprietary delegated access-token header authentication method.
     */
    private static final Endpoint.Authentication DINGTALK_ACCESS_TOKEN = new Endpoint.Authentication(
            "x-acs-dingtalk-access-token");

    /**
     * Exact capabilities implemented by the delegated OAuth 2.0 variant.
     */
    private static final Capability.Manifest OAUTH2_CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION));

    /**
     * Exact capabilities implemented by the proprietary account variant.
     */
    private static final Capability.Manifest ACCOUNT_CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT))));

    /**
     * DingTalk-specific limitation documenting the platform's maximum supported department depth.
     */
    private static final String DEPARTMENT_DEPTH_LIMITED_BY_PLATFORM = "department-depth-limited-by-platform";

    /**
     * Exact Realm coverage description shared with the compiled Realm adapter.
     */
    private static final Realm.Description REALM_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.ROLE),
            Set.of(
                    Realm.RelationKind.PARENT,
                    Realm.RelationKind.MEMBER,
                    Realm.RelationKind.MANAGER,
                    Realm.RelationKind.ROLE_MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.PARTIAL, Builder.MAXIMUM_REALM_PAGE_SIZE,
            List.of(
                    Builder.REALM_LIMITATION_APPLICATION_VISIBLE_CONTACT_SCOPE,
                    Builder.REALM_LIMITATION_HIERARCHY_PAGES_REPLAY_FROM_ROOT,
                    Builder.REALM_LIMITATION_UNPAGED_REPLAY,
                    Builder.REALM_LIMITATION_REPLAY_CHANGE_FAILURE,
                    DEPARTMENT_DEPTH_LIMITED_BY_PLATFORM,
                    Builder.REALM_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Exact implementation-neutral capabilities implemented by the enterprise directory adapter.
     */
    private static final Capability.Manifest REALM_CAPABILITIES = Realm.manifest(REALM_DESCRIPTION);

    /**
     * Registered deviations confined to the delegated Source-authentication chain.
     */
    private static final List<VendorDeviation> OAUTH2_DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    "org_type",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    "corpId",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "authCode",
                    OAuth2.Parameters.CODE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.JSON,
                    "clientSecret",
                    OAuth2.Parameters.CLIENT_SECRET,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "accessToken",
                    OAuth2.Parameters.ACCESS_TOKEN,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.HEADER,
                    "x-acs-dingtalk-access-token",
                    Http.Header.AUTHORIZATION,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    false));

    /**
     * Registered deviations confined to the proprietary signed account chain.
     */
    private static final List<VendorDeviation> ACCOUNT_DEVIATIONS = List.of(
            deviation(
                    Builder.SOURCE_AUTHENTICATION_INITIATE,
                    VendorDeviation.Location.QUERY,
                    "appid",
                    OAuth2.Parameters.CLIENT_ID,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "signature",
                    null,
                    Optional.empty(),
                    Http.Method.POST,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.JSON,
                    "tmp_auth_code",
                    OAuth2.Parameters.CODE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    true));

    /**
     * Complete immutable delegated OAuth 2.0 variant manifest.
     */
    private static final VendorManifest.Variant OAUTH2_VARIANT = new VendorManifest.Variant(ID, OAUTH2, Protocol.OAUTH2,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of("openid"),
            targets(
                    fixed("https://login.dingtalk.com/oauth2/auth", Http.Method.GET, Endpoint.Authentication.NONE),
                    fixed("https://api.dingtalk.com/v1.0/oauth2/userAccessToken", Http.Method.POST, CLIENT_SECRET_JSON),
                    fixed("https://api.dingtalk.com/v1.0/contact/users/me", Http.Method.GET, DINGTALK_ACCESS_TOKEN),
                    fixed(
                            "https://api.dingtalk.com/v1.0/oauth2/userAccessToken",
                            Http.Method.POST,
                            CLIENT_SECRET_JSON)),
            OAUTH2_CAPABILITIES, OAUTH2_DEVIATIONS);

    /**
     * Complete immutable proprietary account-login variant manifest.
     */
    private static final VendorManifest.Variant ACCOUNT_VARIANT = new VendorManifest.Variant(ID, ACCOUNT,
            Protocol.HTTPS, VendorManifest.Pkce.DISABLED, Credential.Type.SHARED_SECRET, List.of("snsapi_login"),
            targets(
                    fixed(
                            "https://oapi.dingtalk.com/connect/oauth2/sns_authorize",
                            Http.Method.GET,
                            Endpoint.Authentication.NONE),
                    null,
                    fixed(
                            "https://oapi.dingtalk.com/sns/getuserinfo_bycode",
                            Http.Method.POST,
                            Endpoint.Authentication.NONE),
                    null),
            ACCOUNT_CAPABILITIES, ACCOUNT_DEVIATIONS);

    /**
     * Complete immutable DingTalk enterprise directory Realm Variant declaration.
     */
    private static final VendorManifest.Variant REALM_VARIANT = new VendorManifest.Variant(ID, ENTERPRISE,
            Protocol.HTTPS, VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET, List.of(),
            new VendorTargets(Optional.empty(), Optional.of(
                    fixed("https://api.dingtalk.com/v1.0/oauth2/accessToken", Http.Method.POST, CLIENT_SECRET_JSON)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), managementTargets()),
            REALM_CAPABILITIES, List.of());

    /**
     * Creates the stateless DingTalk manifest used by Vendor module assembly.
     */
    public DingTalkManifest() {
    }

    /**
     * Returns the frozen DingTalk Realm coverage description.
     *
     * @return immutable Realm description
     */
    static Realm.Description realmDescription() {
        return REALM_DESCRIPTION;
    }

    /**
     * Creates the ordered official DingTalk management target map.
     *
     * @return immutable-by-construction management target declarations
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        final VendorTargets.Target user = fixed(
                "https://oapi.dingtalk.com/topapi/v2/user/get",
                Http.Method.POST,
                Endpoint.Authentication.NONE);
        targets.put(Builder.REALM_USERS, user);
        targets.put(Builder.REALM_USER, user);
        targets.put(
                Builder.REALM_ORGANIZATIONS,
                fixed(
                        "https://oapi.dingtalk.com/topapi/v2/department/listsub",
                        Http.Method.POST,
                        Endpoint.Authentication.NONE));
        targets.put(
                Builder.REALM_ORGANIZATION_USERS,
                fixed("https://oapi.dingtalk.com/topapi/user/listid", Http.Method.POST, Endpoint.Authentication.NONE));
        targets.put(
                Builder.REALM_ROLES,
                fixed("https://oapi.dingtalk.com/topapi/role/list", Http.Method.POST, Endpoint.Authentication.NONE));
        targets.put(
                Builder.REALM_ROLE_MEMBERS,
                fixed(
                        "https://oapi.dingtalk.com/topapi/role/simplelist",
                        Http.Method.POST,
                        Endpoint.Authentication.NONE));
        return targets;
    }

    /**
     * Creates the complete endpoint set used by either DingTalk variant.
     *
     * @param authorization fixed authorization endpoint
     * @param token         fixed token endpoint, or {@code null}
     * @param userInfo      fixed current-user endpoint
     * @param refresh       fixed refresh endpoint, or {@code null}
     * @return immutable endpoint set
     */
    private static VendorTargets targets(
            final VendorTargets.Fixed authorization,
            final VendorTargets.Fixed token,
            final VendorTargets.Fixed userInfo,
            final VendorTargets.Fixed refresh) {
        return new VendorTargets(Optional.of(authorization), Optional.ofNullable(token), Optional.of(userInfo),
                Optional.ofNullable(refresh), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    /**
     * Creates one fixed credential-free HTTPS endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         exact HTTP method
     * @param authentication endpoint authentication method
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
     * Creates one immutable registered DingTalk wire deviation.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   exact DingTalk field name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether DingTalk wraps the result
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
     * Returns the one-time DingTalk form for optional organization selectors.
     *
     * @param variant exact DingTalk variant whose form is requested
     * @return immutable DingTalk client configuration form
     */
    @Override
    public Scheme.Form form(final Vendor.Variant variant) {
        final VendorManifest.Variant selected = variant(variant);
        if (OAUTH2.equals(variant)) {
            return VendorManifest.Forms.extended(
                    selected,
                    true,
                    List.of(
                            VendorManifest.Forms.field("orgType", "Organization type", Scheme.Form.Type.TEXT, false),
                            VendorManifest.Forms.field(
                                    "exclusiveLogin",
                                    "Exclusive organization login",
                                    Scheme.Form.Type.BOOLEAN,
                                    false),
                            VendorManifest.Forms.field(
                                    "exclusiveCorpId",
                                    "Exclusive organization identifier",
                                    Scheme.Form.Type.TEXT,
                                    false)));
        }
        return VendorManifest.Forms.extended(selected, ACCOUNT.equals(variant), List.of());
    }

    /**
     * Returns the stable DingTalk platform identifier.
     *
     * @return stable DingTalk routing identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive DingTalk presentation metadata.
     *
     * @return immutable DingTalk management metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("DingTalk", "DingTalk login and enterprise directory access", "dingtalk");
    }

    /**
     * Returns all DingTalk variants in stable management order.
     *
     * @return delegated OAuth 2.0, account-login, then enterprise manifests
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(OAUTH2_VARIANT, ACCOUNT_VARIANT, REALM_VARIANT);
    }

    /**
     * Returns the exact manifest for one supported DingTalk variant.
     *
     * @param variant requested variant
     * @return exact immutable variant manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (OAUTH2.equals(variant)) {
            return OAUTH2_VARIANT;
        }
        if (ACCOUNT.equals(variant)) {
            return ACCOUNT_VARIANT;
        }
        if (ENTERPRISE.equals(variant)) {
            return REALM_VARIANT;
        }
        throw new ValidateException("DingTalk Vendor variant is not supported");
    }

}
