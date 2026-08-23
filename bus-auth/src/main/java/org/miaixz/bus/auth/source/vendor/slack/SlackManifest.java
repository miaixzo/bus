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
package org.miaixz.bus.auth.source.vendor.slack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.vendor.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares the Slack OAuth 2.0 browser and administrator SCIM Vendor manifests.
 * <p>
 * Public operations retain standard authorization, token, and revocation request/result types. The comma-delimited
 * authorization scope, query-authenticated {@code oauth.v2.access} call, Slack response envelopes, {@code users.info},
 * and {@code auth.revoke} transport remain exact registered Vendor deviations. The independent SCIM Variant exposes
 * only implementation-neutral Realm description, snapshot, and stable-key retrieval through fixed official Users and
 * Groups resources.
 * </p>
 *
 * @author Kimi Liu
 */
public class SlackManifest implements VendorManifest<SlackOptions> {

    /**
     * Stable Slack platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("slack");

    /**
     * Stable identifier of the Slack browser-login Variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant(Normal.DEFAULT);

    /**
     * Stable identifier of the Slack administrator SCIM Variant.
     */
    public static final Vendor.Variant SCIM = new Vendor.Variant("scim");

    /**
     * Slack client-secret authentication carried in token query parameters.
     */
    private static final Endpoint.Authentication CLIENT_SECRET_QUERY = new Endpoint.Authentication(
            "client_secret_query");

    /**
     * Exact Source authentication and public OAuth operations exposed by Slack.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN,
            OAuth2ClientScheme.REVOCATION));

    /**
     * Frozen implementation-neutral coverage description for Slack administrator SCIM.
     */
    private static final Realm.Description REALM_DESCRIPTION = new Realm.Description(
            Set.of(Realm.Kind.USER, Realm.Kind.GROUP), Set.of(Realm.RelationKind.MEMBER),
            Set.of(Realm.Operation.DESCRIBE, Realm.Operation.SNAPSHOT, Realm.Operation.RETRIEVE),
            Realm.Coverage.UNKNOWN, Builder.MAXIMUM_REALM_PAGE_SIZE,
            List.of(
                    "scim-provisioned-identities-only",
                    "business-plus-or-enterprise-plan-required",
                    Builder.REALM_LIMITATION_SNAPSHOT_ONLY));

    /**
     * Exact implementation-neutral capabilities exposed by the administrator SCIM Variant.
     */
    private static final Capability.Manifest REALM_CAPABILITIES = Realm.manifest(REALM_DESCRIPTION);

    /**
     * Exact historical Slack wire deviations isolated from the OAuth protocol package.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "authorize",
                    VendorDeviation.Location.QUERY,
                    "comma-delimited scope",
                    OAuth2.Parameters.SCOPE,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.QUERY,
                    "client_id/client_secret/code/redirect_uri",
                    "token request fields",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "ok/error/response_metadata/authed_user",
                    "access token response extensions",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    OAuth2.Parameters.TOKEN,
                    VendorDeviation.Location.RESPONSE,
                    "comma-delimited scope",
                    OAuth2.Parameters.SCOPE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.QUERY,
                    "user",
                    null,
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    Builder.SOURCE_AUTHENTICATION_COMPLETE,
                    VendorDeviation.Location.RESPONSE,
                    "ok/error/response_metadata/user",
                    null,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true),
            deviation(
                    "revocation",
                    VendorDeviation.Location.HEADER,
                    "Bearer access token",
                    "token form member",
                    Optional.empty(),
                    Http.Method.GET,
                    false),
            deviation(
                    "revocation",
                    VendorDeviation.Location.RESPONSE,
                    "ok/error/response_metadata/revoked",
                    "empty success response",
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.GET,
                    true));

    /**
     * Complete immutable Slack endpoint, client, scope, capability, form, and deviation manifest.
     */
    private static final VendorManifest.Variant VARIANT = new VendorManifest.Variant(ID, DEFAULT, Protocol.OAUTH2,
            VendorManifest.Pkce.DISABLED, Credential.Type.CLIENT_SECRET,
            List.of("users.profile:read", "users:read", "users:read.email"),
            new VendorTargets(Optional
                    .of(fixed("https://slack.com/oauth/v2/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.of(fixed("https://slack.com/api/oauth.v2.access", Http.Method.GET, CLIENT_SECRET_QUERY)),
                    Optional.of(
                            fixed("https://slack.com/api/users.info", Http.Method.GET, Endpoint.Authentication.BEARER)),
                    Optional.empty(), Optional.empty(),
                    Optional.of(
                            fixed(
                                    "https://slack.com/api/auth.revoke",
                                    Http.Method.GET,
                                    Endpoint.Authentication.BEARER)),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
            CAPABILITIES, DEVIATIONS);

    /**
     * Complete immutable Slack administrator SCIM manifest.
     */
    private static final VendorManifest.Variant SCIM_VARIANT = new VendorManifest.Variant(ID, SCIM, Protocol.SCIM,
            VendorManifest.Pkce.DISABLED, Credential.Type.SHARED_SECRET, List.of(),
            new VendorTargets(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    managementTargets()),
            REALM_CAPABILITIES, List.of());

    /**
     * Creates the stateless Slack manifest used by Vendor locator assembly.
     */
    public SlackManifest() {
    }

    /**
     * Returns the frozen Slack administrator SCIM coverage description.
     *
     * @return immutable SCIM management description
     */
    static Realm.Description realmDescription() {
        return REALM_DESCRIPTION;
    }

    /**
     * Creates the exact ordered Slack SCIM resource-target declarations.
     *
     * @return immutable-by-construction SCIM management target map
     */
    private static Map<String, VendorTargets.Target> managementTargets() {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(
                Builder.REALM_USERS,
                fixed("https://api.slack.com/scim/v2/Users", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_USER,
                fixed("https://api.slack.com/scim/v2/Users", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUPS,
                fixed("https://api.slack.com/scim/v2/Groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUP,
                fixed("https://api.slack.com/scim/v2/Groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        return targets;
    }

    /**
     * Creates one fixed credential-free Slack HTTPS endpoint.
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
     * Creates one immutable registered Slack wire deviation.
     *
     * @param operation    affected operation
     * @param location     exact wire location
     * @param vendorName   exact Slack field or representation name
     * @param standardName corresponding standard field, or {@code null}
     * @param mediaType    exact representation when applicable
     * @param method       exact HTTP method
     * @param enveloped    whether the result uses a Slack response envelope
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
     * Returns the stable Slack routing identifier.
     *
     * @return Slack platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Slack presentation metadata.
     *
     * @return immutable Slack management metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Slack", "Slack workspace account authorization", "slack");
    }

    /**
     * Returns the login Variant followed by the administrator SCIM Variant.
     *
     * @return immutable two-Variant manifest list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(VARIANT, SCIM_VARIANT);
    }

    /**
     * Resolves one exact supported Slack manifest.
     *
     * @param variant requested Slack variant
     * @return immutable login or SCIM manifest
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (DEFAULT.equals(variant)) {
            return VARIANT;
        }
        if (SCIM.equals(variant)) {
            return SCIM_VARIANT;
        }
        throw new ValidateException("Slack Vendor variant is not supported");
    }

}
