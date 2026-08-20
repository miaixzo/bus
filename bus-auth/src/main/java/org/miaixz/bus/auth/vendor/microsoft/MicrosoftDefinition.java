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
package org.miaixz.bus.auth.vendor.microsoft;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.VendorDeviation;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.TlsClientAuth;

/**
 * Declares Microsoft global-cloud and China-cloud OAuth 2.0 browser Source variants.
 * <p>
 * Both variants use tenant-scoped Microsoft identity platform endpoints for standard authorization and token
 * operations. Microsoft Graph current-user retrieval remains private Source-authentication behavior and contributes
 * only a verified external identity keyed by the Graph {@code id} member.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MicrosoftDefinition implements VendorDefinition<MicrosoftSourceSettings> {

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
     * Exact Source authentication and public OAuth operations shared by both clouds.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.TOKEN));

    /**
     * Ordered compatibility scopes plus the least-privileged Graph permission required by {@code /me}.
     */
    private static final List<String> DEFAULT_SCOPES = List
            .of("profile", "email", "openid", "offline_access", "User.Read");

    /**
     * Private Microsoft Graph identity mapping retained from the historical providers.
     */
    private static final List<VendorDeviation> GRAPH_IDENTITY = List.of(
            new VendorDeviation("source_authentication.complete", VendorDeviation.Location.RESPONSE, "id",
                    Optional.empty(), Optional.of(MediaType.APPLICATION_JSON_TYPE), Http.Method.GET, false));

    /**
     * Complete immutable global-cloud definition.
     */
    private static final VendorDefinition.Definition GLOBAL_DEFINITION = definition(
            GLOBAL,
            "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize",
            "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token",
            "https://graph.microsoft.com/v1.0/me");

    /**
     * Complete immutable China-cloud definition.
     */
    private static final VendorDefinition.Definition CHINA_DEFINITION = definition(
            CHINA,
            "https://login.partner.microsoftonline.cn/{tenant}/oauth2/v2.0/authorize",
            "https://login.partner.microsoftonline.cn/{tenant}/oauth2/v2.0/token",
            "https://microsoftgraph.chinacloudapi.cn/v1.0/me");

    /**
     * Creates the stateless Microsoft definition used by Vendor directory assembly.
     */
    public MicrosoftDefinition() {
        // No initialization required.
        // All definition state is held by immutable class constants.
    }

    /**
     * Creates one immutable Microsoft cloud definition from its official endpoint family.
     *
     * @param variant       selected cloud variant
     * @param authorization tenant-scoped authorization endpoint template
     * @param token         tenant-scoped token endpoint template
     * @param graph         fixed Microsoft Graph current-user endpoint
     * @return complete immutable definition
     */
    private static VendorDefinition.Definition definition(
            final Vendor.Variant variant,
            final String authorization,
            final String token,
            final String graph) {
        return new VendorDefinition.Definition(ID, variant, Protocol.OAUTH2, DEFAULT_SCOPES,
                new VendorTargets(Optional.of(template(authorization, Http.Method.GET, Endpoint.Authentication.NONE)),
                        Optional.empty(),
                        Optional.of(template(token, Http.Method.POST, Endpoint.Authentication.CLIENT_SECRET_POST)),
                        Optional.of(fixed(graph, Http.Method.GET, Endpoint.Authentication.BEARER)),
                        Optional.of(template(token, Http.Method.POST, Endpoint.Authentication.CLIENT_SECRET_POST)),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty()),
                MANIFEST, GRAPH_IDENTITY);
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
        return new VendorTargets.Fixed(new Endpoint(org.miaixz.bus.fabric.UnoUrl.parse(value), Endpoint.Transport.HTTPS,
                Optional.of(method), Set.of(authentication), Optional.empty(), TlsClientAuth.NONE));
    }

    /**
     * Returns the stable Microsoft routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id type() {
        return ID;
    }

    /**
     * Returns non-sensitive Microsoft presentation metadata.
     *
     * @return immutable management metadata
     */
    @Override
    public Vendor.Metadata metadata() {
        return new Vendor.Metadata("Microsoft", "Microsoft identity platform and Graph sign-in", "microsoft");
    }

    /**
     * Returns the exact externally decoded settings record.
     *
     * @return Microsoft Source settings class
     */
    @Override
    public Class<MicrosoftSourceSettings> settingsType() {
        return MicrosoftSourceSettings.class;
    }

    /**
     * Returns both independently routable Microsoft cloud variants.
     *
     * @return immutable global and China definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(GLOBAL_DEFINITION, CHINA_DEFINITION);
    }

    /**
     * Resolves one exact Microsoft cloud definition.
     *
     * @param variant requested Microsoft cloud variant
     * @return immutable matching definition
     * @throws ValidateException if the variant is neither global nor China
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (GLOBAL.equals(variant)) {
            return GLOBAL_DEFINITION;
        }
        if (CHINA.equals(variant)) {
            return CHINA_DEFINITION;
        }
        throw new ValidateException("Microsoft Vendor variant is not supported");
    }

}
