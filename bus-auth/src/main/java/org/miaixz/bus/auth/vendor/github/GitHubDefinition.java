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

import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
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
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Declares the frozen GitHub.com OAuth App browser Vendor definition.
 * <p>
 * Standard OAuth authorization remains public with mandatory S256 PKCE. GitHub's authorization-code token request,
 * comma-delimited scope response, optional expiring-token refresh pair, and REST user representation remain private
 * Source-authentication behavior.
 * </p>
 *
 * @author Kimi Liu
 */
public final class GitHubDefinition implements VendorDefinition<GitHubSourceSettings> {

    /**
     * Stable GitHub platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("github");

    /**
     * Stable identifier of the sole GitHub.com OAuth App variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Formal standards implemented by the public authorization operation.
     */
    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION));

    /**
     * External management fields required for one GitHub OAuth App.
     */
    /**
     * Exact GitHub wire differences retained only inside Source authentication.
     */
    private static final List<VendorDeviation> DEVIATIONS = List.of(
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.FORM,
                    OAuth2.Parameters.GRANT_TYPE,
                    OAuth2.Parameters.GRANT_TYPE,
                    Optional.of(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                    Http.Method.POST,
                    false),
            deviation(
                    "source_authentication.complete",
                    VendorDeviation.Location.RESPONSE,
                    OAuth2.Parameters.SCOPE,
                    OAuth2.Parameters.SCOPE,
                    Optional.of(MediaType.APPLICATION_JSON_TYPE),
                    Http.Method.POST,
                    false));

    /**
     * Complete immutable endpoint, client, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("read:user"),
            new VendorTargets(Optional.of(
                    fixed("https://github.com/login/oauth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
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
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless GitHub definition used by Vendor directory assembly.
     */
    public GitHubDefinition() {
        // No initialization required.
        // All definition state is held by immutable class constants.
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
        return new VendorTargets.Fixed(new Endpoint(UnoUrl.parse(value), Endpoint.Transport.HTTPS, Optional.of(method),
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
     * Creates one immutable management form field.
     *
     * @param key      stable field key
     * @param label    human-readable field label
     * @param type     management input type
     * @param required whether external management must provide a value
     * @return immutable form field
     */
    /**
     * Returns the stable GitHub routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id type() {
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
     * Returns the exact immutable settings record accepted by this definition.
     *
     * @return GitHub Source settings class
     */
    @Override
    public Class<GitHubSourceSettings> settingsType() {
        return GitHubSourceSettings.class;
    }

    /**
     * Returns the sole supported GitHub variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact default GitHub definition.
     *
     * @param variant requested variant
     * @return exact immutable definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("GitHub Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
