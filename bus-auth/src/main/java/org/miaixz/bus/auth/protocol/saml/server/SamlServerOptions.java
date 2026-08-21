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
package org.miaixz.bus.auth.protocol.saml.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.saml.SamlBinding;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;

/**
 * Holds immutable deployment and security options for one SAML 2.0 identity Provider.
 * <p>
 * Endpoints describe external routes implemented by the hosting project; bus-auth does not create Controllers. Signing
 * key material remains in external key storage and is resolved using the retained key identifier only when a response
 * or assertion is issued.
 * </p>
 *
 * @param entityId                    identity-provider SAML entityID
 * @param singleSignOnServiceEndpoint HTTP-Redirect SingleSignOnService endpoint
 * @param singleLogoutServiceEndpoint optional HTTP-Redirect SingleLogoutService endpoint
 * @param metadataEndpoint            HTTPS Metadata publication endpoint
 * @param requestBinding              binding accepted for service-provider initiated requests
 * @param responseBinding             binding used to return SAML protocol responses
 * @param signingKeyId                external identifier of the identity-provider signing key
 * @param signatureAlgorithm          XML Signature algorithm URI
 * @param wantAuthnRequestsSigned     whether Authentication Requests require valid signatures
 * @param wantLogoutRequestsSigned    whether Logout Requests require valid signatures
 * @param signAssertions              whether issued assertions carry XML signatures
 * @param signResponses               whether issued protocol responses carry XML signatures
 * @param assertionLifetime           positive issued assertion lifetime
 * @param clockSkew                   maximum accepted clock difference at request time boundaries
 * @author Kimi Liu
 */
public record SamlServerOptions(String entityId, Endpoint singleSignOnServiceEndpoint,
        Optional<Endpoint> singleLogoutServiceEndpoint, Endpoint metadataEndpoint, SamlBinding requestBinding,
        SamlBinding responseBinding, String signingKeyId, String signatureAlgorithm, boolean wantAuthnRequestsSigned,
        boolean wantLogoutRequestsSigned, boolean signAssertions, boolean signResponses, Duration assertionLifetime,
        Duration clockSkew) implements Options<SamlServerOptions> {

    /**
     * Maximum assertion lifetime permitted by the SAML Provider issuance policy.
     */
    private static final Duration MAXIMUM_ASSERTION_LIFETIME = Duration.ofHours(24);
    /**
     * Maximum clock difference permitted by the SAML Provider validation policy.
     */
    private static final Duration MAXIMUM_CLOCK_SKEW = Duration.ofMinutes(5);

    /**
     * Normalizes optional values and enforces the fixed Redirect-request/Post-response identity-provider profile.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if a URI, endpoint, binding, signing policy, or duration violates the profile
     */
    public SamlServerOptions {
        entityId = absoluteUri(entityId, "SAML identity-provider entityID");
        requireEndpoint(singleSignOnServiceEndpoint, Http.Method.GET, "SAML SingleSignOnService endpoint");
        Assert.notNull(singleLogoutServiceEndpoint, "SAML SingleLogoutService endpoint container must not be null");
        final Endpoint logout = singleLogoutServiceEndpoint.getOrNull();
        if (logout != null) {
            requireEndpoint(logout, Http.Method.GET, "SAML SingleLogoutService endpoint");
        }
        singleLogoutServiceEndpoint = Optional.ofNullable(logout);
        requireEndpoint(metadataEndpoint, Http.Method.GET, "SAML Metadata endpoint");

        Assert.notNull(requestBinding, "SAML request Binding must not be null");
        if (!SamlBinding.HTTP_REDIRECT.equals(requestBinding)) {
            throw new ValidateException("SAML identity Provider accepts requests through HTTP-Redirect Binding only");
        }
        Assert.notNull(responseBinding, "SAML response Binding must not be null");
        if (!SamlBinding.HTTP_POST.equals(responseBinding)) {
            throw new ValidateException("SAML identity Provider returns responses through HTTP-POST Binding only");
        }
        signingKeyId = Assert.notBlank(signingKeyId, "SAML signing key identifier must not be blank");
        signatureAlgorithm = absoluteUri(signatureAlgorithm, "SAML signature algorithm");
        if (!signAssertions && !signResponses) {
            throw new ValidateException("SAML Provider must sign assertions, responses, or both");
        }
        assertionLifetime = duration(
                assertionLifetime,
                Duration.ZERO,
                MAXIMUM_ASSERTION_LIFETIME,
                false,
                "SAML assertion lifetime");
        clockSkew = duration(clockSkew, Duration.ZERO, MAXIMUM_CLOCK_SKEW, true, "SAML clock skew");
    }

    /**
     * Validates one externally routed endpoint against the SAML Provider transport policy.
     *
     * @param endpoint endpoint to validate
     * @param method   exact HTTP method required by its Binding
     * @param label    safe endpoint label
     * @throws IllegalArgumentException if the endpoint is {@code null}
     * @throws ValidateException        if transport, method, fragment, or authentication declaration is invalid
     */
    private static void requireEndpoint(final Endpoint endpoint, final Http.Method method, final String label) {
        Assert.notNull(endpoint, label + " must not be null");
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != method || endpoint.url().toUri().getRawQuery() != null
                || endpoint.url().toUri().getRawFragment() != null) {
            throw new ValidateException(
                    label + " must be a query-free, fragment-free HTTPS " + method.name() + " endpoint");
        }
        if (!endpoint.authentication().contains(Endpoint.Authentication.NONE)) {
            throw new ValidateException(label + " must declare unauthenticated protocol access");
        }
    }

    /**
     * Validates one non-blank absolute fragment-free URI.
     *
     * @param value URI lexical value
     * @param label safe semantic label
     * @return unchanged URI lexical value
     * @throws ValidateException if the value is not an allowed absolute URI
     */
    private static String absoluteUri(final String value, final String label) {
        Assert.notBlank(value, label + " must not be blank");
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException(label + " must be an absolute fragment-free URI");
            }
            return value;
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Validates one duration against inclusive maximum and configurable lower-bound equality.
     *
     * @param value            candidate duration
     * @param minimum          minimum duration
     * @param maximum          maximum duration
     * @param inclusiveMinimum whether the minimum itself is permitted
     * @param label            safe duration label
     * @return unchanged validated duration
     * @throws ValidateException if the duration lies outside the permitted interval
     */
    private static Duration duration(
            final Duration value,
            final Duration minimum,
            final Duration maximum,
            final boolean inclusiveMinimum,
            final String label) {
        Assert.notNull(value, label + " must not be null");
        final int lower = value.compareTo(minimum);
        if ((inclusiveMinimum ? lower < 0 : lower <= 0) || value.compareTo(maximum) > 0) {
            throw new ValidateException(label + " is outside the permitted SAML security interval");
        }
        return value;
    }

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<SamlServerOptions> type() {
        return SamlServerOptions.class;
    }

    @Override
    public SamlServerOptions snapshot() {
        return this;
    }

    /**
     * Returns a diagnostic summary without endpoint query values or the signing key identifier.
     *
     * @return redacted SAML Provider options summary
     */
    @Override
    public String toString() {
        return "SamlServerOptions[entityId=" + entityId + ", singleSignOnServiceEndpoint=[CONFIGURED]"
                + ", singleLogoutServiceEndpoint="
                + (singleLogoutServiceEndpoint.isPresent() ? Builder.CONFIGURED_VALUE : Builder.ABSENT_VALUE)
                + ", metadataEndpoint=[CONFIGURED]" + ", requestBinding=" + requestBinding.value()
                + ", responseBinding=" + responseBinding.value() + ", signingKeyId=[REDACTED], signatureAlgorithm="
                + signatureAlgorithm + ", wantAuthnRequestsSigned=" + wantAuthnRequestsSigned
                + ", wantLogoutRequestsSigned=" + wantLogoutRequestsSigned + ", signAssertions=" + signAssertions
                + ", signResponses=" + signResponses + ", assertionLifetime=" + assertionLifetime + ", clockSkew="
                + clockSkew + Symbol.BRACKET_RIGHT;
    }

}
