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

import java.time.Instant;
import java.util.List;

import org.miaixz.bus.auth.protocol.saml.*;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Maps safe identity-provider failures into standard SAML 2.0 status responses.
 * <p>
 * Callers may use this mapper only after establishing a registered response destination. Bus errors, exception text,
 * JSON detail, request values, and secret material never enter StatusMessage or StatusDetail.
 * </p>
 *
 * @author Kimi Liu
 */
public class SamlErrorMapper {

    /**
     * Validated identity-provider options used for Issuer construction.
     */
    private final SamlServerOptions options;

    /**
     * Creates a standard SAML error mapper for one Provider.
     *
     * @param options validated SAML Provider options
     * @throws IllegalArgumentException if options are {@code null}
     */
    public SamlErrorMapper(final SamlServerOptions options) {
        this.options = Assert.notNull(options, "SAML Provider options must not be null");
    }

    /**
     * Creates the standard nested error status without framework-specific detail.
     *
     * @param secondLevelStatus standard second-level status URI
     * @param safeMessage       safe status text
     * @return standard SAML Status
     */
    private static Status status(final String secondLevelStatus, final String safeMessage) {
        final String topLevel = requester(secondLevelStatus) ? Saml.Statuses.REQUESTER : Saml.Statuses.RESPONDER;
        return new Status(new StatusCode(topLevel, Optional.of(new StatusCode(secondLevelStatus, Optional.empty()))),
                Optional.of(new StatusMessage(safeMessage)), Optional.empty());
    }

    /**
     * Identifies standard second-level failures attributable to the requester.
     *
     * @param status exact standard status URI
     * @return {@code true} for a requester-side failure
     */
    private static boolean requester(final String status) {
        return switch (status) {
            case Saml.Statuses.INVALID_ATTRIBUTE, Saml.Statuses.INVALID_NAME_ID_POLICY, Saml.Statuses.NO_AUTHN_CONTEXT, Saml.Statuses.REQUEST_UNSUPPORTED, Saml.Statuses.UNKNOWN_PRINCIPAL -> true;
            default -> false;
        };
    }

    /**
     * Identifies standard error statuses published by this mapper.
     *
     * @param status exact status URI
     * @return {@code true} for one supported second-level StatusCode
     */
    private static boolean supported(final String status) {
        return requester(status) || Saml.Statuses.REQUEST_DENIED.equals(status);
    }

    /**
     * Validates safe response construction inputs.
     *
     * @param destination       already registered response destination
     * @param secondLevelStatus standard second-level status URI
     * @param safeMessage       non-sensitive message
     * @param now               shared-clock issue instant
     */
    private static void validate(
            final String destination,
            final String secondLevelStatus,
            final String safeMessage,
            final Instant now) {
        Assert.notBlank(destination, "SAML error response destination must not be blank");
        Assert.notBlank(secondLevelStatus, "SAML second-level status must not be blank");
        if (!supported(secondLevelStatus)) {
            throw new ValidateException("SAML error mapper requires a supported standard second-level StatusCode");
        }
        Assert.notBlank(safeMessage, "SAML safe StatusMessage must not be blank");
        Assert.notNull(now, "SAML error response issue instant must not be null");
    }

    /**
     * Generates an unpredictable XML NCName-compatible response identifier with bus-core UUID support.
     *
     * @return underscore-prefixed identifier
     */
    private static String identifier() {
        return Symbol.C_UNDERLINE + UUID.randomUUID().toString(true);
    }

    /**
     * Creates an error Response correlated to an Authentication Request.
     *
     * @param request           original validated Authentication Request
     * @param destination       registered Assertion Consumer Service destination
     * @param secondLevelStatus standard second-level SAML status URI
     * @param safeMessage       non-sensitive human-readable status message
     * @param now               response issue instant from the shared clock
     * @return unsigned semantic SAML error Response
     */
    public Response response(
            final AuthnRequest request,
            final String destination,
            final String secondLevelStatus,
            final String safeMessage,
            final Instant now) {
        Assert.notNull(request, "SAML Authentication Request must not be null");
        validate(destination, secondLevelStatus, safeMessage, now);
        return new Response(identifier(), Optional.of(request.id()), Saml.VERSION_2_0, now, Optional.of(destination),
                Optional.empty(), Optional.of(issuer()), Optional.empty(), List.of(),
                status(secondLevelStatus, safeMessage), List.of());
    }

    /**
     * Creates an error LogoutResponse correlated to a Logout Request.
     *
     * @param request           original validated Logout Request
     * @param destination       registered service-provider SingleLogoutService destination
     * @param secondLevelStatus standard second-level SAML status URI
     * @param safeMessage       non-sensitive human-readable status message
     * @param now               response issue instant from the shared clock
     * @return unsigned semantic SAML error LogoutResponse
     */
    public LogoutResponse logoutResponse(
            final LogoutRequest request,
            final String destination,
            final String secondLevelStatus,
            final String safeMessage,
            final Instant now) {
        Assert.notNull(request, "SAML Logout Request must not be null");
        validate(destination, secondLevelStatus, safeMessage, now);
        return new LogoutResponse(identifier(), Optional.of(request.id()), Saml.VERSION_2_0, now,
                Optional.of(destination), Optional.empty(), Optional.of(issuer()), Optional.empty(), List.of(),
                status(secondLevelStatus, safeMessage));
    }

    /**
     * Creates the standard identity-provider Issuer value.
     *
     * @return entity-format SAML Issuer
     */
    private Issuer issuer() {
        return new Issuer(new NameID(options.entityId(), Optional.empty(), Optional.empty(),
                Optional.of(Saml.NameIdFormats.ENTITY), Optional.empty()));
    }

}
