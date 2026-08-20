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
package org.miaixz.bus.auth.protocol.scim;

import java.net.URI;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the RFC 7643 core Group resource and its ordered membership values.
 *
 * @param common      common SCIM resource attributes and extensions
 * @param displayName human-readable group name
 * @param members     ordered direct User or Group memberships
 * @author Kimi Liu
 */
public record Group(Resource.Common common, String displayName, List<Member> members) implements Resource {

    /**
     * Validates the Group schema, display name, and typed memberships.
     *
     * @throws IllegalArgumentException if a required value, container, or member is {@code null}
     * @throws ValidateException        if the Group schema is absent
     */
    public Group {
        common = Assert.notNull(common, "SCIM Group common attributes must not be null");
        if (!common.schemas().contains(Scim.GROUP_SCHEMA)) {
            throw new ValidateException("SCIM Group schemas must contain the core Group schema");
        }
        displayName = Assert.notBlank(displayName, "SCIM Group displayName must not be blank");
        Assert.notNull(members, "SCIM Group members must not be null");
        for (Member member : members) {
            Assert.notNull(member, "SCIM Group member must not be null");
        }
        members = List.copyOf(members);
    }

    /**
     * Normalizes a required Bus Optional containing non-blank text.
     *
     * @param value required optional container
     * @param label validation label
     * @return independent optional with the same text value
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        if (!value.isEmpty()) {
            Assert.notBlank(value.getOrThrow(), label + " must not be blank");
        }
        return Optional.ofNullable(value.getOrNull());
    }

    /**
     * Returns the schema identifiers declared by the common resource attributes.
     *
     * @return immutable Group and extension schema URI list
     */
    @Override
    public List<String> schemas() {
        return common.schemas();
    }

    /**
     * Returns the optional service-provider-maintained Group metadata.
     *
     * @return optional Group metadata
     */
    @Override
    public Optional<Resource.Meta> meta() {
        return common.meta();
    }

    /**
     * Defines the standard resource types referenced by Group members.
     *
     * @author Kimi Liu
     */
    public enum Type {

        /**
         * User resource member type.
         */
        USER("User"),

        /**
         * Group resource member type.
         */
        GROUP("Group");

        /**
         * Exact RFC 7643 member type value.
         */
        private final String value;

        /**
         * Creates one constrained member resource type.
         *
         * @param value exact wire value
         */
        Type(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact member type wire value.
         *
         * @return {@code User} or {@code Group}
         */
        public String value() {
            return value;
        }

    }

    /**
     * Models one RFC 7643 Group membership value.
     *
     * @param value     identifier of the referenced member resource
     * @param reference absolute URI of the referenced member resource when available
     * @param type      constrained referenced resource type when present
     * @param display   human-readable member display value when available
     * @author Kimi Liu
     */
    public record Member(String value, Optional<URI> reference, Optional<Type> type, Optional<String> display) {

        /**
         * Validates and normalizes one immutable membership value.
         *
         * @throws IllegalArgumentException if a required value or optional container is {@code null}
         * @throws ValidateException        if the reference is not absolute or type is not User or Group
         */
        public Member {
            value = Assert.notBlank(value, "SCIM Group member value must not be blank");
            Assert.notNull(reference, "SCIM Group member reference container must not be null");
            final URI memberReference = reference.getOrNull();
            if (memberReference != null && !memberReference.isAbsolute()) {
                throw new ValidateException("SCIM Group member reference must be absolute");
            }
            reference = Optional.ofNullable(memberReference);
            Assert.notNull(type, "SCIM Group member type container must not be null");
            type = Optional.ofNullable(type.getOrNull());
            display = optionalText(display, "SCIM Group member display");
        }

    }

}
