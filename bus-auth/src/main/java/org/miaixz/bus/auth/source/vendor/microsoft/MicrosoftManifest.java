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
package org.miaixz.bus.auth.source.vendor.microsoft;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.source.SourceWorkflow;
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
 * Declares Microsoft global-cloud and China-cloud login and enterprise Graph variants.
 * <p>
 * Both variants use tenant-scoped Microsoft identity platform endpoints for standard authorization and token
 * operations. Microsoft Graph current-user retrieval remains private Source-authentication behavior for the two login
 * variants. The independent enterprise variants use application permissions over fixed cloud-specific Graph hosts and
 * expose the implementation-neutral enterprise snapshot, change, and retrieval contracts.
 * </p>
 *
 * @author Kimi Liu
 */
public class MicrosoftManifest implements VendorManifest<MicrosoftOptions> {

    /**
     * Stable Microsoft platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("microsoft");

    /**
     * Global Microsoft identity and Graph service variant.
     */
    public static final Vendor.Variant GLOBAL = new Vendor.Variant("global");

    /**
     * Microsoft China operated by 21Vianet service variant.
     */
    public static final Vendor.Variant CHINA = new Vendor.Variant("china");

    /**
     * Global Microsoft Graph application-permission enterprise variant.
     */
    public static final Vendor.Variant ENTERPRISE_GLOBAL = new Vendor.Variant("enterprise-global");

    /**
     * Microsoft China Graph application-permission enterprise variant.
     */
    public static final Vendor.Variant ENTERPRISE_CHINA = new Vendor.Variant("enterprise-china");
    /**
     * Sole global Graph application-permission scope accepted by the enterprise Variant.
     */
    static final String GLOBAL_APPLICATION_SCOPE = "https://graph.microsoft.com/.default";
    /**
     * Sole China Graph application-permission scope accepted by the enterprise Variant.
     */
    static final String CHINA_APPLICATION_SCOPE = "https://microsoftgraph.chinacloudapi.cn/.default";
    /**
     * Exact Source authentication and public OAuth operations shared by both clouds.
     */
    private static final Capability.Manifest CAPABILITIES = new Capability.Manifest(List.of(
            SourceWorkflow.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceWorkflow.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2ClientScheme.AUTHORIZATION,
            OAuth2ClientScheme.TOKEN));
    /**
     * Ordered compatibility scopes plus the least-privileged Graph permission required by {@code /me}.
     */
    private static final List<String> DEFAULT_SCOPES = List
            .of("profile", "email", "openid", "offline_access", "User.Read");
    /**
     * Exact Realm coverage description shared by both isolated Graph clouds.
     */
    private static final Realm.Description REALM_DESCRIPTION = new Realm.Description(
            Set.of(
                    Realm.Kind.USER,
                    Realm.Kind.ORGANIZATION,
                    Realm.Kind.GROUP,
                    Realm.Kind.ROLE,
                    Realm.Kind.SERVICE_ACCOUNT),
            Set.of(
                    Realm.RelationKind.MEMBER,
                    Realm.RelationKind.MANAGER,
                    Realm.RelationKind.ROLE_MEMBER,
                    Realm.RelationKind.APPLICATION_ASSIGNMENT),
            Set.of(
                    Realm.Operation.DESCRIBE,
                    Realm.Operation.SNAPSHOT,
                    Realm.Operation.CHANGES,
                    Realm.Operation.RETRIEVE),
            Realm.Coverage.UNKNOWN, Builder.MAXIMUM_REALM_PAGE_SIZE,
            List.of(
                    "graph-application-permissions-control-visibility",
                    "changes-support-user-group-and-service-principal-kinds",
                    "expired-delta-token-requires-new-baseline"));

    /**
     * Exact implementation-neutral capabilities exposed only by Microsoft enterprise variants.
     */
    private static final Capability.Manifest REALM_CAPABILITIES = Realm.manifest(REALM_DESCRIPTION);

    /**
     * Private Microsoft Graph identity mapping required by the supported Microsoft Source variants.
     */
    private static final List<VendorDeviation> GRAPH_IDENTITY = List.of(
            new VendorDeviation(Builder.SOURCE_AUTHENTICATION_COMPLETE, VendorDeviation.Location.RESPONSE, "id",
                    Optional.empty(), Optional.of(MediaType.APPLICATION_JSON_TYPE), Http.Method.GET, false));

    /**
     * Complete immutable global-cloud manifest.
     */
    private static final VendorManifest.Variant GLOBAL_VARIANT = variant(
            GLOBAL,
            "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize",
            "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token",
            "https://graph.microsoft.com/v1.0/me");

    /**
     * Complete immutable China-cloud manifest.
     */
    private static final VendorManifest.Variant CHINA_VARIANT = variant(
            CHINA,
            "https://login.partner.microsoftonline.cn/{tenant}/oauth2/v2.0/authorize",
            "https://login.partner.microsoftonline.cn/{tenant}/oauth2/v2.0/token",
            "https://microsoftgraph.chinacloudapi.cn/v1.0/me");

    /**
     * Complete immutable global-cloud enterprise Graph manifest.
     */
    private static final VendorManifest.Variant REALM_GLOBAL_VARIANT = realm(
            ENTERPRISE_GLOBAL,
            "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token",
            "https://graph.microsoft.com",
            GLOBAL_APPLICATION_SCOPE);

    /**
     * Complete immutable China-cloud enterprise Graph manifest.
     */
    private static final VendorManifest.Variant REALM_CHINA_VARIANT = realm(
            ENTERPRISE_CHINA,
            "https://login.partner.microsoftonline.cn/{tenant}/oauth2/v2.0/token",
            "https://microsoftgraph.chinacloudapi.cn",
            CHINA_APPLICATION_SCOPE);

    /**
     * Creates the stateless Microsoft manifest used by Vendor locator assembly.
     */
    public MicrosoftManifest() {
    }

    /**
     * Returns the frozen Microsoft Realm coverage description.
     *
     * @return immutable Realm description shared by both clouds
     */
    static Realm.Description realmDescription() {
        return REALM_DESCRIPTION;
    }

    /**
     * Creates one immutable Microsoft cloud manifest from its official endpoint family.
     *
     * @param variant       selected cloud variant
     * @param authorization tenant-scoped authorization endpoint template
     * @param token         tenant-scoped token endpoint template
     * @param graph         fixed Microsoft Graph current-user endpoint
     * @return complete immutable manifest
     */
    private static VendorManifest.Variant variant(
            final Vendor.Variant variant,
            final String authorization,
            final String token,
            final String graph) {
        return new VendorManifest.Variant(ID, variant, Protocol.OAUTH2, VendorManifest.Pkce.DISABLED,
                Credential.Type.CLIENT_SECRET, DEFAULT_SCOPES,
                new VendorTargets(Optional.of(template(authorization, Http.Method.GET, Endpoint.Authentication.NONE)),
                        Optional.of(template(token, Http.Method.POST, Endpoint.Authentication.CLIENT_SECRET_POST)),
                        Optional.of(fixed(graph, Http.Method.GET, Endpoint.Authentication.BEARER)),
                        Optional.of(template(token, Http.Method.POST, Endpoint.Authentication.CLIENT_SECRET_POST)),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty()),
                CAPABILITIES, GRAPH_IDENTITY);
    }

    /**
     * Creates one immutable Microsoft Realm Variant from its fixed authority and Graph host.
     *
     * @param variant selected enterprise cloud variant
     * @param token   tenant-scoped client-credentials token endpoint template
     * @param graph   fixed cloud-specific Microsoft Graph origin
     * @param scope   sole cloud-specific application-permission scope
     * @return complete immutable Realm manifest
     */
    private static VendorManifest.Variant realm(
            final Vendor.Variant variant,
            final String token,
            final String graph,
            final String scope) {
        return new VendorManifest.Variant(ID, variant, Protocol.HTTPS, VendorManifest.Pkce.DISABLED,
                Credential.Type.CLIENT_SECRET, List.of(scope),
                new VendorTargets(Optional.empty(),
                        Optional.of(template(token, Http.Method.POST, Endpoint.Authentication.CLIENT_SECRET_POST)),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty(), managementTargets(graph)),
                REALM_CAPABILITIES, List.of());
    }

    /**
     * Creates the exact ordered Microsoft Graph management target map for one sovereign cloud.
     *
     * @param graph fixed cloud-specific Graph origin
     * @return immutable-by-construction management target declarations
     */
    private static Map<String, VendorTargets.Target> managementTargets(final String graph) {
        final Map<String, VendorTargets.Target> targets = new LinkedHashMap<>();
        targets.put(Builder.REALM_USERS, fixed(graph + "/v1.0/users", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(Builder.REALM_USER, fixed(graph + "/v1.0/users", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ORGANIZATION,
                fixed(graph + "/v1.0/organization", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUPS,
                fixed(graph + "/v1.0/groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUP,
                fixed(graph + "/v1.0/groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_GROUP_MEMBERS,
                fixed(graph + "/v1.0/groups", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ROLES,
                fixed(graph + "/v1.0/directoryRoles", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ROLE_MEMBERS,
                fixed(graph + "/v1.0/directoryRoles", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_ROLE_ASSIGNMENTS,
                fixed(graph + "/v1.0/servicePrincipals", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(
                Builder.REALM_SERVICE_ACCOUNTS,
                fixed(graph + "/v1.0/servicePrincipals", Http.Method.GET, Endpoint.Authentication.BEARER));
        targets.put(Builder.REALM_CHANGES, fixed(graph + "/v1.0", Http.Method.GET, Endpoint.Authentication.BEARER));
        return targets;
    }

    /**
     * Creates one constrained tenant-path endpoint template.
     *
     * @param value          official HTTPS endpoint template
     * @param method         operation HTTP method
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
     * Creates one fixed credential-free HTTPS resource endpoint.
     *
     * @param value          exact endpoint URL
     * @param method         operation HTTP method
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
     * Returns the one-time Microsoft client form with an optional tenant selector.
     *
     * @param variant exact Microsoft variant whose form is requested
     * @return immutable Microsoft client configuration form
     */
    @Override
    public Scheme.Form form(final Vendor.Variant variant) {
        final VendorManifest.Variant selected = variant(variant);
        return VendorManifest.Forms.extended(
                selected,
                List.of(VendorManifest.Forms.field("tenant", "Microsoft tenant", Scheme.Form.Type.TEXT, true)));
    }

    /**
     * Returns the stable Microsoft routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id vendor() {
        return ID;
    }

    /**
     * Returns non-sensitive Microsoft presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Scheme.Metadata metadata() {
        return new Scheme.Metadata("Microsoft", "Microsoft identity platform and Graph sign-in", "microsoft");
    }

    /**
     * Returns the two login variants followed by the two enterprise cloud variants.
     *
     * @return immutable four-Variant manifest list
     */
    @Override
    public List<VendorManifest.Variant> variants() {
        return List.of(GLOBAL_VARIANT, CHINA_VARIANT, REALM_GLOBAL_VARIANT, REALM_CHINA_VARIANT);
    }

    /**
     * Resolves one exact Microsoft cloud manifest.
     *
     * @param variant requested Microsoft cloud variant
     * @return immutable matching manifest
     * @throws ValidateException if the variant is outside the four frozen Microsoft variants
     */
    @Override
    public VendorManifest.Variant variant(final Vendor.Variant variant) {
        if (GLOBAL.equals(variant)) {
            return GLOBAL_VARIANT;
        }
        if (CHINA.equals(variant)) {
            return CHINA_VARIANT;
        }
        if (ENTERPRISE_GLOBAL.equals(variant)) {
            return REALM_GLOBAL_VARIANT;
        }
        if (ENTERPRISE_CHINA.equals(variant)) {
            return REALM_CHINA_VARIANT;
        }
        throw new ValidateException("Microsoft Vendor variant is not supported");
    }

}
