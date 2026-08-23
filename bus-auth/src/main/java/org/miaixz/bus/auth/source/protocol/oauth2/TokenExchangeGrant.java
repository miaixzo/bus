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
package org.miaixz.bus.auth.source.protocol.oauth2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents RFC 8693 token-exchange grant parameters carried by the standard OAuth token request.
 * <p>
 * Subject and actor tokens are opaque sensitive values whose syntax is determined by their token type identifiers.
 * Resource and audience lists preserve every wire occurrence in order, including duplicates permitted by the RFC.
 * </p>
 *
 * @param resource           ordered absolute target resource URIs
 * @param audience           ordered logical target service names
 * @param subjectToken       opaque token representing the exchange subject
 * @param subjectTokenType   absolute URI identifying the subject token representation
 * @param requestedTokenType optional absolute URI identifying the requested token representation
 * @param actorToken         optional opaque token representing the acting party
 * @param actorTokenType     optional absolute URI identifying the actor token representation
 * @param scope              optional requested scope
 * @author Kimi Liu
 */
public record TokenExchangeGrant(List<String> resource, List<String> audience, String subjectToken,
        String subjectTokenType, Optional<String> requestedTokenType, Optional<String> actorToken,
        Optional<String> actorTokenType, Optional<Scope> scope) implements TokenRequest.Grant {

    /**
     * RFC 8693 identifier for an OAuth access token used or returned by token exchange.
     */
    public static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    /**
     * Creates and validates an immutable token-exchange grant parameter set.
     *
     * @throws IllegalArgumentException if a required component, list entry, or optional container is {@code null}
     * @throws ValidateException        if a resource or token type is not an allowed URI, a value is empty, or actor
     *                                  fields are not paired
     */
    public TokenExchangeGrant {
        Assert.notNull(resource, "OAuth 2.x token-exchange resource list must not be null");
        final List<String> resourceCopy = new ArrayList<>(resource.size());
        for (String value : resource) {
            Assert.notNull(value, "OAuth 2.x token-exchange resource must not be null");
            validateAbsoluteUri(value, false, "OAuth 2.x token-exchange resource");
            resourceCopy.add(value);
        }
        Assert.notNull(audience, "OAuth 2.x token-exchange audience list must not be null");
        final List<String> audienceCopy = new ArrayList<>(audience.size());
        for (String value : audience) {
            audienceCopy.add(Assert.notEmpty(value, "OAuth 2.x token-exchange audience must not be empty"));
        }
        Assert.notEmpty(subjectToken, "OAuth 2.x token-exchange subject token must not be empty");
        Assert.notEmpty(subjectTokenType, "OAuth 2.x token-exchange subject token type must not be empty");
        validateAbsoluteUri(subjectTokenType, true, "OAuth 2.x token-exchange subject token type");
        Assert.notNull(requestedTokenType, "OAuth 2.x requested token type container must not be null");
        Assert.notNull(actorToken, "OAuth 2.x actor token container must not be null");
        Assert.notNull(actorTokenType, "OAuth 2.x actor token type container must not be null");
        Assert.notNull(scope, "OAuth 2.x token-exchange scope container must not be null");

        final String requestedType = requestedTokenType.getOrNull();
        if (requestedType != null) {
            validateAbsoluteUri(requestedType, true, "OAuth 2.x requested token type");
        }
        final String actor = actorToken.getOrNull();
        final String actorType = actorTokenType.getOrNull();
        if ((actor == null) != (actorType == null)) {
            throw new ValidateException("OAuth 2.x actor token and actor token type must be present together");
        }
        if (actor != null) {
            Assert.notEmpty(actor, "OAuth 2.x actor token must not be empty when present");
            validateAbsoluteUri(actorType, true, "OAuth 2.x actor token type");
        }
        final Scope requestedScope = scope.getOrNull();

        resource = List.copyOf(resourceCopy);
        audience = List.copyOf(audienceCopy);
        requestedTokenType = Optional.ofNullable(requestedType);
        actorToken = Optional.ofNullable(actor);
        actorTokenType = Optional.ofNullable(actorType);
        scope = Optional.ofNullable(requestedScope);
    }

    /**
     * Validates one absolute URI and its fragment policy.
     *
     * @param value           URI wire value
     * @param fragmentAllowed whether an RFC token type identifier may carry a fragment
     * @param label           safe component label used in diagnostics
     * @throws ValidateException if the value is not an absolute URI or contains a prohibited fragment
     */
    private static void validateAbsoluteUri(final String value, final boolean fragmentAllowed, final String label) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || !fragmentAllowed && uri.getRawFragment() != null) {
                throw new ValidateException(
                        label + " must be an absolute URI" + (fragmentAllowed ? Normal.EMPTY : " without a fragment"));
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException(label + " must be a valid absolute URI", exception);
        }
    }

    /**
     * Returns a diagnostic representation without subject or actor token material.
     *
     * @return redacted token-exchange grant summary
     */
    @Override
    public String toString() {
        return "TokenExchangeGrant[resource=" + resource + ", audience=" + audience
                + ", subjectToken=[REDACTED], subjectTokenType=" + subjectTokenType + ", requestedTokenType="
                + requestedTokenType + ", actorToken=[REDACTED], actorTokenType=" + actorTokenType + ", scope=" + scope
                + Symbol.BRACKET_RIGHT;
    }

}
