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
package org.miaixz.bus.auth.protocol.saml.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.Options;
import org.miaixz.bus.auth.protocol.saml.SamlBinding;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;

/**
 * Holds immutable deployment and security options for one SAML 2.0 service-provider Source.
 * <p>
 * Identity-provider metadata and service endpoints are deployment input loaded by an external project. Metadata
 * retrieval verifies that the explicitly configured SSO and SLO endpoints are actually declared by the trusted
 * identity-provider entity. Cryptographic material is never retained here; {@code signingKeyId} is resolved through the
 * external key loader for each signing operation.
 * </p>
 *
 * @param entityId                              service-provider SAML entityID
 * @param identityProviderEntityId              exact trusted identity-provider SAML entityID
 * @param identityProviderMetadataEndpoint      trusted identity-provider Metadata endpoint
 * @param singleSignOnServiceEndpoint           identity-provider HTTP-Redirect SingleSignOnService endpoint
 * @param singleLogoutServiceEndpoint           optional identity-provider HTTP-Redirect SingleLogoutService endpoint
 * @param assertionConsumerServiceUrl           service-provider HTTP-POST AssertionConsumerService location
 * @param serviceProviderSingleLogoutServiceUrl optional service-provider SingleLogoutService location
 * @param requestBinding                        binding used for service-provider initiated SAML requests
 * @param signingKeyId                          external identifier of the service-provider signing key
 * @param signatureAlgorithm                    XML Signature algorithm URI used for Redirect query signatures
 * @param signAuthnRequests                     whether Authentication Requests must be signed
 * @param signLogoutRequests                    whether Logout Requests must be signed
 * @param wantAssertionsSigned                  whether every accepted assertion must carry a valid signature
 * @param wantResponsesSigned                   whether every accepted SAML Response must carry a valid signature
 * @param clockSkew                             maximum accepted clock difference at SAML time boundaries
 * @param maximumAssertionAge                   maximum accepted age when a profile condition does not impose a shorter
 *                                              interval
 * @author Kimi Liu
 */
public record SamlClientOptions(String entityId, String identityProviderEntityId,
        Endpoint identityProviderMetadataEndpoint, Endpoint singleSignOnServiceEndpoint,
        Optional<Endpoint> singleLogoutServiceEndpoint, String assertionConsumerServiceUrl,
        Optional<String> serviceProviderSingleLogoutServiceUrl, SamlBinding requestBinding, String signingKeyId,
        String signatureAlgorithm, boolean signAuthnRequests, boolean signLogoutRequests, boolean wantAssertionsSigned,
        boolean wantResponsesSigned, Duration clockSkew, Duration maximumAssertionAge)
        implements Options<SamlClientOptions> {

    /**
     * Returns this immutable configuration implementation type.
     *
     * @return exact Options implementation class
     */
    @Override
    public Class<SamlClientOptions> type() {
        return SamlClientOptions.class;
    }

    /**
     * Maximum clock difference permitted by the SAML Source validation policy.
     */
    private static final Duration MAXIMUM_CLOCK_SKEW = Duration.ofMinutes(5);

    /**
     * Maximum assertion age permitted by the SAML Source validation policy.
     */
    private static final Duration MAXIMUM_ASSERTION_AGE = Duration.ofHours(24);

    /**
     * Normalizes optional values and enforces the fixed Redirect-request/Post-response security profile.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if a URI, endpoint, binding, or time limit violates the SAML Source scheme
     */
    public SamlClientOptions {
        entityId = absoluteUri(entityId, "SAML service-provider entityID", false);
        identityProviderEntityId = absoluteUri(identityProviderEntityId, "SAML identity-provider entityID", false);
        requireEndpoint(identityProviderMetadataEndpoint, Http.Method.GET, "SAML identity-provider Metadata endpoint");
        requireEndpoint(
                singleSignOnServiceEndpoint,
                Http.Method.GET,
                "SAML identity-provider SingleSignOnService endpoint");
        Assert.notNull(
                singleLogoutServiceEndpoint,
                "SAML identity-provider SingleLogoutService endpoint container must not be null");
        final Endpoint logoutEndpoint = singleLogoutServiceEndpoint.getOrNull();
        if (logoutEndpoint != null) {
            requireEndpoint(logoutEndpoint, Http.Method.GET, "SAML identity-provider SingleLogoutService endpoint");
        }
        singleLogoutServiceEndpoint = Optional.ofNullable(logoutEndpoint);

        assertionConsumerServiceUrl = absoluteUri(
                assertionConsumerServiceUrl,
                "SAML AssertionConsumerService URL",
                true);
        Assert.notNull(
                serviceProviderSingleLogoutServiceUrl,
                "SAML service-provider SingleLogoutService URL container must not be null");
        final String localLogout = serviceProviderSingleLogoutServiceUrl.getOrNull();
        serviceProviderSingleLogoutServiceUrl = Optional.ofNullable(
                localLogout == null ? null
                        : absoluteUri(localLogout, "SAML service-provider SingleLogoutService URL", true));

        Assert.notNull(requestBinding, "SAML request Binding must not be null");
        if (!SamlBinding.HTTP_REDIRECT.equals(requestBinding)) {
            throw new ValidateException("SAML service-provider initiated requests require HTTP-Redirect Binding");
        }
        signingKeyId = Assert.notBlank(signingKeyId, "SAML signing key identifier must not be blank");
        signatureAlgorithm = signatureUri(signatureAlgorithm);
        clockSkew = boundedDuration(clockSkew, Duration.ZERO, MAXIMUM_CLOCK_SKEW, "SAML clock skew", true);
        maximumAssertionAge = boundedDuration(
                maximumAssertionAge,
                Duration.ZERO,
                MAXIMUM_ASSERTION_AGE,
                "SAML maximum assertion age",
                false);
    }

    /**
     * Validates a remote SAML endpoint against the SAML Source transport policy.
     *
     * @param endpoint endpoint to validate
     * @param method   exact HTTP method required by its Binding
     * @param label    safe endpoint label
     * @throws IllegalArgumentException if the endpoint is {@code null}
     * @throws ValidateException        if transport, method, URL fragment, or authentication declaration is invalid
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
            throw new ValidateException(label + " must declare unauthenticated browser access");
        }
    }

    /**
     * Validates one absolute URI and optionally requires the HTTPS scheme.
     *
     * @param value URI lexical value
     * @param label safe semantic label
     * @param https whether the URI must use HTTPS
     * @return unchanged URI lexical value
     * @throws ValidateException if the value is not an allowed absolute fragment-free URI
     */
    private static String absoluteUri(final String value, final String label, final boolean https) {
        Assert.notBlank(value, label + " must not be blank");
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null
                    || (https && (!Protocol.HTTPS.name.equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null))) {
                throw new ValidateException(
                        label + " must be an absolute fragment-free" + (https ? " HTTPS" : Normal.EMPTY) + " URI");
            }
            return value;
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " is not a valid URI", exception);
        }
    }

    /**
     * Validates an XML Signature algorithm identifier as an absolute URI.
     * <p>
     * Unlike deployment endpoints and SAML entity identifiers, registered XML Signature algorithm identifiers may
     * legitimately use a fragment, such as the standard RSA-SHA256 URI ending in {@code #rsa-sha256}.
     * </p>
     *
     * @param value XML Signature algorithm identifier
     * @return unchanged absolute algorithm URI
     * @throws ValidateException if the value is not a syntactically valid absolute URI
     */
    private static String signatureUri(final String value) {
        Assert.notBlank(value, "SAML signature algorithm must not be blank");
        try {
            if (!new URI(value).isAbsolute()) {
                throw new ValidateException("SAML signature algorithm must be an absolute URI");
            }
            return value;
        } catch (URISyntaxException exception) {
            throw new ValidateException("SAML signature algorithm is not a valid URI", exception);
        }
    }

    /**
     * Validates one duration against an inclusive upper bound and configurable lower-bound equality.
     *
     * @param value            candidate duration
     * @param minimum          minimum duration
     * @param maximum          maximum duration
     * @param label            safe duration label
     * @param inclusiveMinimum whether the minimum value itself is allowed
     * @return unchanged validated duration
     * @throws ValidateException if the duration lies outside the allowed interval
     */
    private static Duration boundedDuration(
            final Duration value,
            final Duration minimum,
            final Duration maximum,
            final String label,
            final boolean inclusiveMinimum) {
        Assert.notNull(value, label + " must not be null");
        final int lower = value.compareTo(minimum);
        if ((inclusiveMinimum ? lower < 0 : lower <= 0) || value.compareTo(maximum) > 0) {
            throw new ValidateException(label + " is outside the permitted SAML security interval");
        }
        return value;
    }

    /**
     * Returns a stable presence marker for an optional endpoint.
     *
     * @param endpoint endpoint container
     * @return configured or absent marker
     */
    private static String present(final Optional<Endpoint> endpoint) {
        return endpoint.isPresent() ? "[CONFIGURED]" : "[ABSENT]";
    }

    /**
     * Returns a diagnostic summary without endpoint query values or cryptographic key identifiers.
     *
     * @return redacted SAML Source options summary
     */
    @Override
    public String toString() {
        return "SamlClientOptions[entityId=" + entityId + ", identityProviderEntityId=" + identityProviderEntityId
                + ", identityProviderMetadataEndpoint=[CONFIGURED]" + ", singleSignOnServiceEndpoint=[CONFIGURED]"
                + ", singleLogoutServiceEndpoint=" + present(singleLogoutServiceEndpoint)
                + ", assertionConsumerServiceUrl=" + assertionConsumerServiceUrl
                + ", serviceProviderSingleLogoutServiceUrl="
                + (serviceProviderSingleLogoutServiceUrl.isPresent() ? "[CONFIGURED]" : "[ABSENT]")
                + ", requestBinding=" + requestBinding.value() + ", signingKeyId=[REDACTED], signatureAlgorithm="
                + signatureAlgorithm + ", signAuthnRequests=" + signAuthnRequests + ", signLogoutRequests="
                + signLogoutRequests + ", wantAssertionsSigned=" + wantAssertionsSigned + ", wantResponsesSigned="
                + wantResponsesSigned + ", clockSkew=" + clockSkew + ", maximumAssertionAge=" + maximumAssertionAge
                + Symbol.BRACKET_RIGHT;
    }

}
