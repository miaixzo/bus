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
package org.miaixz.bus.auth.source.protocol.scim;

import java.net.URI;
import java.util.List;

import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models every RFC 7643 core User attribute with typed complex and multi-valued components.
 *
 * @param common            common resource attributes and declared schema extensions
 * @param userName          required service-provider-unique user identifier
 * @param name              optional structured person name
 * @param displayName       optional display name
 * @param nickName          optional casual name
 * @param profileUrl        optional profile URL
 * @param title             optional title
 * @param userType          optional relationship-to-service type
 * @param preferredLanguage optional preferred language tag
 * @param locale            optional locale tag
 * @param timezone          optional time-zone name
 * @param active            optional administrative active flag
 * @param password          inbound write-only password lease
 * @param emails            email address values
 * @param phoneNumbers      telephone values
 * @param instantMessages   instant-messaging values
 * @param photos            photo URI values
 * @param addresses         postal address values
 * @param groups            read-only group memberships
 * @param entitlements      entitlement values
 * @param roles             role values
 * @param certificates      X.509 certificate values
 * @author Kimi Liu
 */
public record User(Resource.Common common, String userName, Optional<Name> name, Optional<String> displayName,
        Optional<String> nickName, Optional<URI> profileUrl, Optional<String> title, Optional<String> userType,
        Optional<String> preferredLanguage, Optional<String> locale, Optional<String> timezone,
        Optional<Boolean> active, Optional<SecretLease> password, List<MultiValue> emails,
        List<MultiValue> phoneNumbers, List<MultiValue> instantMessages, List<MultiValue> photos,
        List<Address> addresses, List<Membership> groups, List<MultiValue> entitlements, List<MultiValue> roles,
        List<MultiValue> certificates) implements Resource, AutoCloseable {

    /**
     * Validates and freezes every core User component.
     *
     * @throws IllegalArgumentException if a component, container, or list item is {@code null}
     * @throws ValidateException        if the core User schema is absent
     */
    public User {
        common = Assert.notNull(common, "SCIM User common attributes must not be null");
        if (!common.schemas().contains(Scim.USER_SCHEMA)) {
            throw new ValidateException("SCIM User schemas must contain the core User schema");
        }
        userName = Assert.notBlank(userName, "SCIM User userName must not be blank");
        name = optional(name, "SCIM User name");
        displayName = text(displayName, "SCIM User displayName");
        nickName = text(nickName, "SCIM User nickName");
        profileUrl = optional(profileUrl, "SCIM User profileUrl");
        title = text(title, "SCIM User title");
        userType = text(userType, "SCIM User userType");
        preferredLanguage = text(preferredLanguage, "SCIM User preferredLanguage");
        locale = text(locale, "SCIM User locale");
        timezone = text(timezone, "SCIM User timezone");
        active = optional(active, "SCIM User active");
        password = optional(password, "SCIM User password");
        emails = values(emails, "SCIM User email");
        phoneNumbers = values(phoneNumbers, "SCIM User phone number");
        instantMessages = values(instantMessages, "SCIM User instant message");
        photos = values(photos, "SCIM User photo");
        addresses = values(addresses, "SCIM User address");
        groups = values(groups, "SCIM User group membership");
        entitlements = values(entitlements, "SCIM User entitlement");
        roles = values(roles, "SCIM User role");
        certificates = values(certificates, "SCIM User certificate");
    }

    /**
     * Normalizes one required Bus optional container.
     *
     * @param <T>   value type
     * @param value source container
     * @param label safe diagnostic label
     * @return normalized container
     */
    private static <T> Optional<T> optional(final Optional<T> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Normalizes optional non-blank text.
     *
     * @param value source container
     * @param label safe diagnostic label
     * @return normalized text container
     */
    private static Optional<String> text(final Optional<String> value, final String label) {
        final Optional<String> result = optional(value, label);
        if (result.isPresent()) {
            Assert.notBlank(result.getOrThrow(), label + " must not be blank");
        }
        return result;
    }

    /**
     * Freezes one typed multi-valued attribute list.
     *
     * @param <T>   item type
     * @param items caller-supplied attribute entries to validate and freeze
     * @param label safe diagnostic label
     * @return immutable value list
     */
    private static <T> List<T> values(final List<T> items, final String label) {
        Assert.notNull(items, label + " list must not be null");
        items.forEach(value -> Assert.notNull(value, label + " must not be null"));
        return List.copyOf(items);
    }

    /**
     * Returns the declared User and extension schemas.
     *
     * @return immutable schema list
     */
    @Override
    public List<String> schemas() {
        return common.schemas();
    }

    /**
     * Returns service-provider-maintained metadata when assigned.
     *
     * @return optional resource metadata
     */
    @Override
    public Optional<Resource.Meta> meta() {
        return common.meta();
    }

    /**
     * Erases the owned inbound write-only password lease when present.
     */
    @Override
    public void close() {
        final SecretLease lease = password.getOrNull();
        if (lease != null) {
            lease.close();
        }
    }

    /**
     * Returns a diagnostic representation without personal or credential values.
     *
     * @return fixed redacted User representation
     */
    @Override
    public String toString() {
        return "User[redacted]";
    }

    /**
     * Defines standard User group membership types.
     *
     * @author Kimi Liu
     */
    public enum MembershipType {

        /**
         * Direct group membership.
         */
        DIRECT("direct"),

        /**
         * Membership inherited through another group.
         */
        INDIRECT("indirect");

        /**
         * Exact lowercase wire value.
         */
        private final String value;

        /**
         * Creates one constrained membership type.
         *
         * @param value canonical wire value
         */
        MembershipType(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical wire value.
         *
         * @return direct or indirect
         */
        public String value() {
            return value;
        }

    }

    /**
     * Represents the standard structured User name.
     *
     * @param formatted       complete formatted name
     * @param familyName      family name
     * @param givenName       given name
     * @param middleName      middle name
     * @param honorificPrefix honorific prefix
     * @param honorificSuffix honorific suffix
     * @author Kimi Liu
     */
    public record Name(Optional<String> formatted, Optional<String> familyName, Optional<String> givenName,
            Optional<String> middleName, Optional<String> honorificPrefix, Optional<String> honorificSuffix) {

        /**
         * Normalizes every optional name component.
         */
        public Name {
            formatted = text(formatted, "SCIM formatted name");
            familyName = text(familyName, "SCIM family name");
            givenName = text(givenName, "SCIM given name");
            middleName = text(middleName, "SCIM middle name");
            honorificPrefix = text(honorificPrefix, "SCIM honorific prefix");
            honorificSuffix = text(honorificSuffix, "SCIM honorific suffix");
        }

    }

    /**
     * Represents common sub-attributes of a multi-valued User attribute.
     *
     * @param value   required primary value
     * @param display optional display value
     * @param type    optional client-defined type token
     * @param primary optional primary marker
     * @author Kimi Liu
     */
    public record MultiValue(String value, Optional<String> display, Optional<String> type, Optional<Boolean> primary) {

        /**
         * Validates and normalizes one multi-valued item.
         */
        public MultiValue {
            value = Assert.notBlank(value, "SCIM multi-valued item value must not be blank");
            display = text(display, "SCIM multi-valued item display");
            type = text(type, "SCIM multi-valued item type");
            primary = optional(primary, "SCIM multi-valued item primary");
        }

    }

    /**
     * Represents one structured postal address.
     *
     * @param formatted     complete formatted address
     * @param streetAddress street address
     * @param locality      locality or city
     * @param region        region or state
     * @param postalCode    postal code
     * @param country       country name
     * @param type          optional address type
     * @param primary       optional primary marker
     * @author Kimi Liu
     */
    public record Address(Optional<String> formatted, Optional<String> streetAddress, Optional<String> locality,
            Optional<String> region, Optional<String> postalCode, Optional<String> country, Optional<String> type,
            Optional<Boolean> primary) {

        /**
         * Normalizes every optional address component.
         */
        public Address {
            formatted = text(formatted, "SCIM formatted address");
            streetAddress = text(streetAddress, "SCIM street address");
            locality = text(locality, "SCIM address locality");
            region = text(region, "SCIM address region");
            postalCode = text(postalCode, "SCIM postal code");
            country = text(country, "SCIM address country");
            type = text(type, "SCIM address type");
            primary = optional(primary, "SCIM address primary");
        }

    }

    /**
     * Represents one read-only User group membership.
     *
     * @param value     referenced Group id
     * @param reference optional absolute Group URI
     * @param display   optional display value
     * @param type      optional direct or indirect membership type
     * @author Kimi Liu
     */
    public record Membership(String value, Optional<URI> reference, Optional<String> display,
            Optional<MembershipType> type) {

        /**
         * Validates and normalizes one membership.
         */
        public Membership {
            value = Assert.notBlank(value, "SCIM User group id must not be blank");
            reference = optional(reference, "SCIM User group reference");
            if (reference.isPresent() && !reference.getOrThrow().isAbsolute()) {
                throw new ValidateException("SCIM User group reference must be absolute");
            }
            display = text(display, "SCIM User group display");
            type = optional(type, "SCIM User group type");
        }

    }

}
