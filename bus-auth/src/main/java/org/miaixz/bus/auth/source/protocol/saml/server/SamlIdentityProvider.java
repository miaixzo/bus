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
package org.miaixz.bus.auth.source.protocol.saml.server;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.source.protocol.saml.*;
import org.miaixz.bus.core.lang.Assert;

/**
 * Provides the standards-based SAML 2.0 identity-provider facade for one compiled server-role Source.
 * <p>
 * The facade owns no protocol policy or transport behavior. It delegates each standard operation to its single strongly
 * typed service, retaining the exact SAML request and response models across the Roster boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public class SamlIdentityProvider {

    /**
     * Service that validates Authentication Requests and issues SAML Responses.
     */
    private final SingleSignOnService singleSignOnService;

    /**
     * Service that validates Logout Requests and issues Logout Responses.
     */
    private final SingleLogoutService singleLogoutService;

    /**
     * Service that publishes this identity provider's SAML Metadata.
     */
    private final MetadataService metadataService;

    /**
     * Creates an identity-provider facade from its exact operation owners.
     *
     * @param singleSignOnService SAML SingleSignOnService implementation
     * @param singleLogoutService SAML SingleLogoutService implementation
     * @param metadataService     SAML Metadata publication service
     * @throws IllegalArgumentException if a service is {@code null}
     */
    public SamlIdentityProvider(final SingleSignOnService singleSignOnService,
            final SingleLogoutService singleLogoutService, final MetadataService metadataService) {
        this.singleSignOnService = Assert.notNull(singleSignOnService, "SAML SingleSignOnService must not be null");
        this.singleLogoutService = Assert.notNull(singleLogoutService, "SAML SingleLogoutService must not be null");
        this.metadataService = Assert.notNull(metadataService, "SAML MetadataService must not be null");
    }

    /**
     * Processes one standard SAML Authentication Request.
     *
     * @param request standard Authentication Request
     * @param context immutable invocation context containing the authenticated subject
     * @param timeout shared end-to-end timeout
     * @return stage containing a standard SAML Response or a closed framework failure
     */
    public CompletionStage<Outcome<Response>> singleSignOn(
            final AuthnRequest request,
            final Context context,
            final Timeout timeout) {
        return singleSignOnService.singleSignOn(request, context, timeout);
    }

    /**
     * Processes one standard SAML Logout Request.
     *
     * @param request standard Logout Request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a standard SAML Logout Response or a closed framework failure
     */
    public CompletionStage<Outcome<LogoutResponse>> singleLogout(
            final LogoutRequest request,
            final Context context,
            final Timeout timeout) {
        return singleLogoutService.singleLogout(request, context, timeout);
    }

    /**
     * Publishes this identity provider's standard SAML Metadata entity descriptor.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return stage containing a standard EntityDescriptor or a closed framework failure
     */
    public CompletionStage<Outcome<EntityDescriptor>> metadata(final Context context, final Timeout timeout) {
        return metadataService.metadata(context, timeout);
    }

}
