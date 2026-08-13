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
package org.miaixz.bus.auth.vendor.linkedin;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Gender;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party client for LinkedIn authorization, token, profile, and email operations.
 *
 * <p>
 * Every remote call uses the injected Fabric context. State uses the injected atomic store, and client secrets are
 * resolved only for token exchange and cleared without being retained.
 * </p>
 *
 * @author Kimi Liu
 */
public class LinkedinProvider extends AbstractProvider {

    /**
     * LinkedIn profile field projection.
     */
    private static final String PROFILE_PROJECTION = "(id,firstName,lastName,profilePicture(displayImage~:playableStreams))";

    /**
     * LinkedIn primary-email field projection.
     */
    private static final String EMAIL_PROJECTION = "(elements*(handle~))";

    /**
     * Creates a LinkedIn client from the complete immutable dependency aggregate.
     *
     * @param configuration non-null registration, Fabric, clock, state-store, cache, and secret dependencies
     */
    public LinkedinProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.LINKEDIN);
    }

    /**
     * Parses and validates one LinkedIn token response.
     *
     * @param json token response document
     * @return mapped token set
     */
    private static VendorTokenSet readToken(final String json) {
        final TokenResponse response = JsonKit.toPojo(json, TokenResponse.class);
        validate(response);
        if (response.access_token() == null) {
            throw new AuthorizedException("Missing access_token in LinkedIn response");
        }
        return VendorTokenSet.builder().token(response.access_token()).expireIn(response.expires_in())
                .refresh(response.refresh_token()).build();
    }

    /**
     * Validates the common LinkedIn response error fields.
     *
     * @param response typed LinkedIn response
     */
    private static void validate(final LinkedinResponse response) {
        if (response == null)
            throw new AuthorizedException("Failed to parse LinkedIn response: empty response");
        if (response.error() != null) {
            throw new AuthorizedException(
                    response.error_description() == null ? "Unknown error" : response.error_description());
        }
    }

    /**
     * Builds the user's display name from localized or locale-keyed profile fields.
     *
     * @param profile typed profile response
     * @return display name
     */
    private static String name(final ProfileResponse profile) {
        final String first = profile.localizedFirstName() != null ? profile.localizedFirstName()
                : localized(profile.firstName());
        final String last = profile.localizedLastName() != null ? profile.localizedLastName()
                : localized(profile.lastName());
        return first + Symbol.SPACE + last;
    }

    /**
     * Resolves one locale-keyed LinkedIn name component.
     *
     * @param name localized name object
     * @return matching localized value or an empty string
     */
    private static String localized(final LocalizedName name) {
        if (name == null || name.localized() == null || name.preferredLocale() == null)
            return Normal.EMPTY;
        final LocaleValue locale = name.preferredLocale();
        if (locale.language() == null || locale.country() == null)
            return Normal.EMPTY;
        return name.localized().getOrDefault(locale.language() + Symbol.UNDERLINE + locale.country(), Normal.EMPTY);
    }

    /**
     * Extracts the first identifier of the largest LinkedIn profile image element.
     *
     * @param profile typed profile response
     * @return image URL or null
     */
    private static String avatar(final ProfileResponse profile) {
        final Map<String, DisplayImage> picture = profile.profilePicture();
        final DisplayImage image = picture == null ? null : picture.get("displayImage~");
        if (image == null || image.elements() == null || image.elements().isEmpty())
            return null;
        final ImageElement largest = image.elements().get(image.elements().size() - 1);
        return largest.identifiers() == null || largest.identifiers().isEmpty() ? null
                : largest.identifiers().get(0).identifier();
    }

    /**
     * Extracts the primary email address from the first LinkedIn email element.
     *
     * @param response typed email response
     * @return primary email address or null
     */
    private static String email(final EmailResponse response) {
        if (response.elements() == null || response.elements().isEmpty())
            return null;
        final Map<String, EmailValue> element = response.elements().get(0);
        final EmailValue value = element.get("handle~");
        return value == null ? null : value.emailAddress();
    }

    /**
     * Builds the LinkedIn authorization URL and atomically registers state.
     *
     * @param context immutable root operation context used for state ownership
     * @param state   optional caller-supplied state
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE)).queryParam("response_type", "code")
                        .queryParam("client_id", registration.clientId())
                        .queryParam("redirect_uri", registration.redirectUri())
                        .queryParam("state", state(current, state))
                        .queryParam("scope", scopes(Symbol.SPACE, false, getScopes(LinkedinScope.values()))).build());
    }

    /**
     * Exchanges a LinkedIn authorization code using an empty-form POST with query credentials.
     *
     * @param context  immutable root operation context used to resolve the client secret
     * @param callback immutable inbound callback containing the authorization code
     * @return successful message containing mapped token fields
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final Map<String, String> headers = Map.of(
                Http.Header.HOST,
                "www.linkedin.com",
                Http.Header.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED);
        return Message.success(readToken(post(tokenUrl(current, inbound.value("code").orElse(null)), null, headers)));
    }

    /**
     * Retrieves the LinkedIn profile and primary email through two Bearer-authenticated GET requests.
     *
     * @param context immutable root operation context
     * @param token   non-null LinkedIn token set
     * @return successful message containing the combined LinkedIn identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> headers = Map.of(
                Http.Header.HOST,
                "api.linkedin.com",
                Http.Header.CONNECTION,
                "Keep-Alive",
                Http.Header.AUTHORIZATION,
                Http.Auth.BEARER_PREFIX + authorization.getToken());
        final String profileUrl = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("projection", PROFILE_PROJECTION).build();
        final ProfileResponse profile = JsonKit.toPojo(get(profileUrl, null, headers), ProfileResponse.class);
        validate(profile);
        if (profile.id() == null) {
            throw new AuthorizedException("Missing id in LinkedIn profile response");
        }
        final EmailResponse email = JsonKit.toPojo(get(emailUrl(), null, headers), EmailResponse.class);
        validate(email);
        final String userName = name(profile);
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(profile)).uuid(profile.id()).username(userName)
                        .nickname(userName).avatar(avatar(profile)).email(email(email)).token(authorization)
                        .gender(Gender.UNKNOWN).source(descriptor().id()).build());
    }

    /**
     * Builds the email endpoint as a sibling of the effective LinkedIn user-info endpoint.
     *
     * @return effective email endpoint with frozen query fields
     */
    private String emailUrl() {
        final URI profile = URI.create(endpoint(VendorEndpoint.USERINFO));
        final String path = profile.getPath();
        final String base = path.substring(0, path.lastIndexOf('/') + 1) + "emailAddress";
        return VendorRequestBuilder.fromUrl(profile.resolve(base).toString()).queryParam("q", "members")
                .queryParam("projection", EMAIL_PROJECTION).build();
    }

    /**
     * Common LinkedIn error fields.
     *
     * @author Kimi Liu
     */
    private interface LinkedinResponse {

        /**
         * Returns the vendor error code.
         *
         * @return error code or null
         */
        String error();

        /**
         * Returns the vendor diagnostic text.
         *
         * @return diagnostic text or null
         */
        String error_description();
    }

    /**
     * Typed token response.
     *
     * @param access_token      access token
     * @param expires_in        lifetime in seconds
     * @param refresh_token     refresh token
     * @param error             error code
     * @param error_description diagnostic text
     * @author Kimi Liu
     */
    private record TokenResponse(String access_token, int expires_in, String refresh_token, String error,
            String error_description) implements LinkedinResponse {
    }

    /**
     * Preferred locale fields.
     *
     * @param language language tag
     * @param country  country tag
     * @author Kimi Liu
     */
    private record LocaleValue(String language, String country) {
    }

    /**
     * Locale-keyed name values.
     *
     * @param localized       localized values
     * @param preferredLocale preferred locale
     * @author Kimi Liu
     */
    private record LocalizedName(Map<String, String> localized, LocaleValue preferredLocale) {
    }

    /**
     * Image identifier.
     *
     * @param identifier image URL
     * @author Kimi Liu
     */
    private record ImageIdentifier(String identifier) {
    }

    /**
     * One image variant.
     *
     * @param identifiers image identifiers
     * @author Kimi Liu
     */
    private record ImageElement(List<ImageIdentifier> identifiers) {
    }

    /**
     * Profile image variants.
     *
     * @param elements image variants
     * @author Kimi Liu
     */
    private record DisplayImage(List<ImageElement> elements) {
    }

    /**
     * Typed LinkedIn profile response.
     *
     * @param id                 identifier
     * @param localizedFirstName first name
     * @param localizedLastName  last name
     * @param firstName          localized first-name map
     * @param lastName           localized last-name map
     * @param profilePicture     image map
     * @param error              error code
     * @param error_description  diagnostic text
     * @author Kimi Liu
     */
    private record ProfileResponse(String id, String localizedFirstName, String localizedLastName,
            LocalizedName firstName, LocalizedName lastName, Map<String, DisplayImage> profilePicture, String error,
            String error_description) implements LinkedinResponse {
    }

    /**
     * Primary email value.
     *
     * @param emailAddress primary email
     * @author Kimi Liu
     */
    private record EmailValue(String emailAddress) {
    }

    /**
     * Typed LinkedIn email response.
     *
     * @param elements          email elements
     * @param error             error code
     * @param error_description diagnostic text
     * @author Kimi Liu
     */
    private record EmailResponse(List<Map<String, EmailValue>> elements, String error, String error_description)
            implements LinkedinResponse {
    }

}
