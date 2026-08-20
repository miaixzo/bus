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
 * Declares the frozen GitLab.com OAuth application Vendor definition.
 * <p>
 * Authorization, token, refresh-token grant, and revocation use the framework's standard OAuth models. The adapter
 * retains only GitLab's registered {@code created_at}, refresh {@code redirect_uri}, empty JSON revocation response,
 * and REST identity mapping as exact wire handling.
 * </p>
 *
 * @author Kimi Liu
 */
public final class GitLabDefinition implements VendorDefinition<GitLabSourceSettings> {

    /**
     * Stable GitLab platform routing identifier.
     */
    public static final Vendor.Id ID = new Vendor.Id("gitlab");

    /**
     * Stable identifier of the sole GitLab.com OAuth App variant.
     */
    public static final Vendor.Variant DEFAULT = new Vendor.Variant("default");

    /**
     * Formal standards implemented by the public OAuth operations.
     */
    /**
     * Exact Source and standard OAuth capabilities of the default variant.
     */
    private static final Capability.Manifest MANIFEST = new Capability.Manifest(List.of(
            SourceAuthentication.initiate(Set.of(Capability.Interaction.REDIRECT)),
            SourceAuthentication.complete(Set.of(Capability.Interaction.REDIRECT)),
            OAuth2SourceProfile.AUTHORIZATION,
            OAuth2SourceProfile.TOKEN,
            OAuth2SourceProfile.REVOCATION));

    /**
     * External management fields required for one GitLab OAuth App.
     */
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
     * Complete immutable endpoint, client, scope, capability, form, and deviation definition.
     */
    private static final VendorDefinition.Definition DEFINITION = new VendorDefinition.Definition(ID, DEFAULT,
            Protocol.OAUTH2, List.of("read_user"),
            new VendorTargets(
                    Optional.of(
                            fixed("https://gitlab.com/oauth/authorize", Http.Method.GET, Endpoint.Authentication.NONE)),
                    Optional.empty(),
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
            MANIFEST, DEVIATIONS);

    /**
     * Creates the stateless GitLab definition used by Vendor directory assembly.
     */
    public GitLabDefinition() {
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
     * Creates one immutable management form field.
     *
     * @param key      stable field key
     * @param label    human-readable field label
     * @param type     management input type
     * @param required whether external management must provide a value
     * @return immutable form field
     */
    /**
     * Returns the stable GitLab routing identifier.
     *
     * @return stable platform identifier
     */
    @Override
    public Vendor.Id type() {
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
     * Returns the exact immutable settings record accepted by this definition.
     *
     * @return GitLab Source settings class
     */
    @Override
    public Class<GitLabSourceSettings> settingsType() {
        return GitLabSourceSettings.class;
    }

    /**
     * Returns the sole supported GitLab variant.
     *
     * @return immutable single-element definition list
     */
    @Override
    public List<VendorDefinition.Definition> variants() {
        return List.of(DEFINITION);
    }

    /**
     * Returns the exact default GitLab definition.
     *
     * @param variant requested variant
     * @return exact immutable definition
     * @throws ValidateException if the requested variant is unsupported
     */
    @Override
    public VendorDefinition.Definition variant(final Vendor.Variant variant) {
        if (!DEFAULT.equals(variant)) {
            throw new ValidateException("GitLab Vendor variant is not supported");
        }
        return DEFINITION;
    }

}
