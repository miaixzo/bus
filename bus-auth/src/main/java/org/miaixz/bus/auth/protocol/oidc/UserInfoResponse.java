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
package org.miaixz.bus.auth.protocol.oidc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an OpenID Connect UserInfo response with standard claims grouped by their domain meaning.
 *
 * @param subject    required subject identifier
 * @param name       optional person-name claims
 * @param address    optional structured postal address
 * @param phone      optional telephone claims
 * @param email      optional email claims
 * @param profile    optional profile page URL
 * @param picture    optional profile picture URL
 * @param website    optional website URL
 * @param gender     optional gender claim
 * @param birthdate  optional birthdate claim
 * @param zoneInfo   optional time-zone database name
 * @param locale     optional BCP 47 locale tag
 * @param updatedAt  optional profile update NumericDate
 * @param extensions additional claims not represented by standard components
 * @author Kimi Liu
 */
public record UserInfoResponse(Subject subject, Optional<Name> name, Optional<Address> address, Optional<Phone> phone,
        Optional<Email> email, Optional<String> profile, Optional<String> picture, Optional<String> website,
        Optional<String> gender, Optional<String> birthdate, Optional<String> zoneInfo, Optional<String> locale,
        Optional<Long> updatedAt, JsonValue.ObjectValue extensions) {

    /**
     * Validates and freezes the standard UserInfo claim groups.
     *
     * @throws IllegalArgumentException if a component container is {@code null}
     * @throws ValidateException        if a URL or NumericDate component is invalid
     */
    public UserInfoResponse {
        Assert.notNull(subject, "OpenID Connect UserInfo subject must not be null");
        name = object(name, "OpenID Connect UserInfo name");
        address = object(address, "OpenID Connect UserInfo address");
        phone = object(phone, "OpenID Connect UserInfo phone");
        email = object(email, "OpenID Connect UserInfo email");
        profile = optionalUri(profile, "OpenID Connect UserInfo profile URL");
        picture = optionalUri(picture, "OpenID Connect UserInfo picture URL");
        website = optionalUri(website, "OpenID Connect UserInfo website URL");
        gender = text(gender, "OpenID Connect UserInfo gender");
        birthdate = text(birthdate, "OpenID Connect UserInfo birthdate");
        zoneInfo = text(zoneInfo, "OpenID Connect UserInfo zone information");
        locale = text(locale, "OpenID Connect UserInfo locale");
        Assert.notNull(updatedAt, "OpenID Connect UserInfo updated-at container must not be null");
        final Long timestamp = updatedAt.getOrNull();
        if (timestamp != null && timestamp < 0L) {
            throw new ValidateException("OpenID Connect UserInfo updated-at value must not be negative");
        }
        updatedAt = Optional.ofNullable(timestamp);
        Assert.notNull(extensions, "OpenID Connect UserInfo extensions must not be null");
        for (String claimName : extensions.values().keySet()) {
            Assert.notBlank(claimName, "OpenID Connect UserInfo extension claim name must not be blank");
            if (standard(claimName)) {
                throw new ValidateException("OpenID Connect UserInfo extension replaces a standard claim");
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies UserInfo claims represented by typed components.
     *
     * @param name exact case-sensitive claim name
     * @return {@code true} when the claim belongs to the typed UserInfo vocabulary
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case JwtClaims.SUBJECT, OpenIdConnect.Claims.NAME, OpenIdConnect.Claims.GIVEN_NAME, OpenIdConnect.Claims.FAMILY_NAME, OpenIdConnect.Claims.MIDDLE_NAME, OpenIdConnect.Claims.NICKNAME, OpenIdConnect.Claims.PREFERRED_USERNAME, OpenIdConnect.Claims.PROFILE, OpenIdConnect.Claims.PICTURE, OpenIdConnect.Claims.WEBSITE, OpenIdConnect.Claims.EMAIL, OpenIdConnect.Claims.EMAIL_VERIFIED, OpenIdConnect.Claims.GENDER, OpenIdConnect.Claims.BIRTHDATE, OpenIdConnect.Claims.ZONE_INFO, OpenIdConnect.Claims.LOCALE, OpenIdConnect.Claims.PHONE_NUMBER, OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED, OpenIdConnect.Claims.ADDRESS, OpenIdConnect.Claims.UPDATED_AT -> true;
            default -> false;
        };
    }

    /**
     * Adds one present string claim to a wire projection.
     *
     * @param target destination claim map
     * @param name   standard claim name
     * @param value  optional claim value
     */
    private static void put(final Map<String, JsonValue> target, final String name, final Optional<String> value) {
        final String present = value.getOrNull();
        if (present != null) {
            target.put(name, new JsonValue.StringValue(present));
        }
    }

    /**
     * Adds one present Boolean claim to a wire projection.
     *
     * @param target destination claim map
     * @param name   standard claim name
     * @param value  optional claim value
     */
    private static void putBoolean(
            final Map<String, JsonValue> target,
            final String name,
            final Optional<Boolean> value) {
        final Boolean present = value.getOrNull();
        if (present != null) {
            target.put(name, new JsonValue.BooleanValue(present));
        }
    }

    /**
     * Normalizes an optional object component.
     *
     * @param <T>   component type
     * @param value optional component container
     * @param label safe validation label
     * @return normalized optional component
     */
    private static <T> Optional<T> object(final Optional<T> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Normalizes optional non-blank text.
     *
     * @param value optional text container
     * @param label safe validation label
     * @return normalized optional text
     */
    private static Optional<String> text(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String present = value.getOrNull();
        if (present != null) {
            Assert.notBlank(present, label + " must not be blank");
        }
        return Optional.ofNullable(present);
    }

    /**
     * Normalizes an optional absolute URI claim.
     *
     * @param value optional URI container
     * @param label safe validation label
     * @return normalized optional URI text
     */
    private static Optional<String> optionalUri(final Optional<String> value, final String label) {
        final Optional<String> normalized = text(value, label);
        final String present = normalized.getOrNull();
        if (present != null) {
            try {
                if (!new URI(present).isAbsolute()) {
                    throw new ValidateException(label + " must be absolute");
                }
            } catch (URISyntaxException cause) {
                throw new ValidateException(label + " must be a valid absolute URI", cause);
            }
        }
        return normalized;
    }

    /**
     * Returns the complete standard UserInfo claim object used on the JSON wire.
     * <p>
     * Typed components remain the authoritative in-memory representation. This method performs a deterministic wire
     * projection for protocol codecs and Vendor identity mapping without storing a second claim map.
     * </p>
     *
     * @return immutable JSON object containing standard and extension claims
     */
    public JsonValue.ObjectValue claims() {
        final Map<String, JsonValue> values = new LinkedHashMap<>(extensions.values());
        values.put("sub", new JsonValue.StringValue(subject.value()));
        final Name person = name.getOrNull();
        if (person != null) {
            put(values, OpenIdConnect.Claims.NAME, person.formatted());
            put(values, OpenIdConnect.Claims.GIVEN_NAME, person.givenName());
            put(values, OpenIdConnect.Claims.FAMILY_NAME, person.familyName());
            put(values, OpenIdConnect.Claims.MIDDLE_NAME, person.middleName());
            put(values, OpenIdConnect.Claims.NICKNAME, person.nickname());
            put(values, OpenIdConnect.Claims.PREFERRED_USERNAME, person.preferredUsername());
        }
        put(values, OpenIdConnect.Claims.PROFILE, profile);
        put(values, OpenIdConnect.Claims.PICTURE, picture);
        put(values, OpenIdConnect.Claims.WEBSITE, website);
        put(values, OpenIdConnect.Claims.GENDER, gender);
        put(values, OpenIdConnect.Claims.BIRTHDATE, birthdate);
        put(values, OpenIdConnect.Claims.ZONE_INFO, zoneInfo);
        put(values, OpenIdConnect.Claims.LOCALE, locale);
        final Email mailbox = email.getOrNull();
        if (mailbox != null) {
            put(values, OpenIdConnect.Claims.EMAIL, mailbox.address());
            putBoolean(values, OpenIdConnect.Claims.EMAIL_VERIFIED, mailbox.verified());
        }
        final Phone telephone = phone.getOrNull();
        if (telephone != null) {
            put(values, OpenIdConnect.Claims.PHONE_NUMBER, telephone.number());
            putBoolean(values, OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED, telephone.verified());
        }
        final Address postal = address.getOrNull();
        if (postal != null) {
            final Map<String, JsonValue> addressClaims = new LinkedHashMap<>();
            put(addressClaims, OpenIdConnect.Claims.FORMATTED, postal.formatted());
            put(addressClaims, OpenIdConnect.Claims.STREET_ADDRESS, postal.streetAddress());
            put(addressClaims, OpenIdConnect.Claims.LOCALITY, postal.locality());
            put(addressClaims, OpenIdConnect.Claims.REGION, postal.region());
            put(addressClaims, OpenIdConnect.Claims.POSTAL_CODE, postal.postalCode());
            put(addressClaims, OpenIdConnect.Claims.COUNTRY, postal.country());
            values.put(OpenIdConnect.Claims.ADDRESS, new JsonValue.ObjectValue(addressClaims));
        }
        final Long updateTime = updatedAt.getOrNull();
        if (updateTime != null) {
            values.put(
                    OpenIdConnect.Claims.UPDATED_AT,
                    new JsonValue.NumberValue(java.math.BigDecimal.valueOf(updateTime)));
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Returns a diagnostic summary without personally identifiable values.
     *
     * @return redacted UserInfo response summary
     */
    @Override
    public String toString() {
        return "UserInfoResponse[subject=[REDACTED],name=[REDACTED],address=[REDACTED],phone=[REDACTED],"
                + "email=[REDACTED],extensions=[REDACTED]]";
    }

    /**
     * Represents the required UserInfo subject claim.
     *
     * @param value subject StringOrURI
     * @author Kimi Liu
     */
    public record Subject(String value) {

        /**
         * Validates JWT StringOrURI syntax.
         *
         * @throws IllegalArgumentException if value is {@code null} or blank
         * @throws ValidateException        if a colon-containing value is not a valid URI
         */
        public Subject {
            Assert.notBlank(value, "OpenID Connect UserInfo subject must not be blank");
            if (value.indexOf(Symbol.C_COLON) >= 0) {
                try {
                    new URI(value);
                } catch (URISyntaxException cause) {
                    throw new ValidateException("OpenID Connect UserInfo subject must satisfy StringOrURI syntax",
                            cause);
                }
            }
        }

    }

    /**
     * Groups the OpenID Connect person-name claims.
     *
     * @param formatted         full display name
     * @param givenName         given name
     * @param familyName        family name
     * @param middleName        middle name
     * @param nickname          casual name
     * @param preferredUsername preferred username
     * @author Kimi Liu
     */
    public record Name(Optional<String> formatted, Optional<String> givenName, Optional<String> familyName,
            Optional<String> middleName, Optional<String> nickname, Optional<String> preferredUsername) {

        /**
         * Validates and normalizes person-name claims.
         */
        public Name {
            formatted = text(formatted, "OpenID Connect UserInfo formatted name");
            givenName = text(givenName, "OpenID Connect UserInfo given name");
            familyName = text(familyName, "OpenID Connect UserInfo family name");
            middleName = text(middleName, "OpenID Connect UserInfo middle name");
            nickname = text(nickname, "OpenID Connect UserInfo nickname");
            preferredUsername = text(preferredUsername, "OpenID Connect UserInfo preferred username");
        }

    }

    /**
     * Represents the structured OpenID Connect address claim.
     *
     * @param formatted     complete mailing address
     * @param streetAddress street address
     * @param locality      city or locality
     * @param region        state, province, or region
     * @param postalCode    postal code
     * @param country       country name
     * @author Kimi Liu
     */
    public record Address(Optional<String> formatted, Optional<String> streetAddress, Optional<String> locality,
            Optional<String> region, Optional<String> postalCode, Optional<String> country) {

        /**
         * Validates and normalizes structured address claims.
         */
        public Address {
            formatted = text(formatted, "OpenID Connect UserInfo formatted address");
            streetAddress = text(streetAddress, "OpenID Connect UserInfo street address");
            locality = text(locality, "OpenID Connect UserInfo locality");
            region = text(region, "OpenID Connect UserInfo region");
            postalCode = text(postalCode, "OpenID Connect UserInfo postal code");
            country = text(country, "OpenID Connect UserInfo country");
        }

    }

    /**
     * Groups telephone number and verification claims.
     *
     * @param number   telephone number
     * @param verified whether control of the number was verified
     * @author Kimi Liu
     */
    public record Phone(Optional<String> number, Optional<Boolean> verified) {

        /**
         * Validates and normalizes telephone claims.
         */
        public Phone {
            number = text(number, "OpenID Connect UserInfo telephone number");
            Assert.notNull(verified, "OpenID Connect UserInfo telephone verification container must not be null");
            verified = Optional.ofNullable(verified.getOrNull());
        }

    }

    /**
     * Groups email address and verification claims.
     *
     * @param address  email address
     * @param verified whether control of the address was verified
     * @author Kimi Liu
     */
    public record Email(Optional<String> address, Optional<Boolean> verified) {

        /**
         * Validates and normalizes email claims.
         */
        public Email {
            address = text(address, "OpenID Connect UserInfo email address");
            Assert.notNull(verified, "OpenID Connect UserInfo email verification container must not be null");
            verified = Optional.ofNullable(verified.getOrNull());
        }

    }

}
