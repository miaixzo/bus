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
package org.miaixz.bus.auth.protocol.scim.codec;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.auth.protocol.scim.Group;
import org.miaixz.bus.auth.protocol.scim.Resource;
import org.miaixz.bus.auth.protocol.scim.Scim;
import org.miaixz.bus.auth.protocol.scim.User;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonRecordVerifier;
import org.miaixz.bus.extra.json.JsonRecordVerifier.Member;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Converts RFC 7643 User and Group resources between typed protocol objects and provider-neutral JSON.
 * <p>
 * Structural records own every standard JSON vocabulary. Declared extension schemas remain dynamic top-level objects,
 * while all core attributes are mapped through the typed {@link User}, {@link Group}, and {@link Resource} models.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ScimResourceCodec {

    /**
     * Verifies the exact core User document after declared extension objects are removed.
     */
    private static final JsonRecordVerifier<UserDocument> USER_VERIFIER = JsonRecordVerifier.of(UserDocument.class);

    /**
     * Verifies the exact core Group document after declared extension objects are removed.
     */
    private static final JsonRecordVerifier<GroupDocument> GROUP_VERIFIER = JsonRecordVerifier.of(GroupDocument.class);

    /**
     * Verifies standard resource metadata.
     */
    private static final JsonRecordVerifier<MetaDocument> META_VERIFIER = JsonRecordVerifier.of(MetaDocument.class);

    /**
     * Verifies a standard structured User name.
     */
    private static final JsonRecordVerifier<NameDocument> NAME_VERIFIER = JsonRecordVerifier.of(NameDocument.class);

    /**
     * Verifies a common multi-valued User item.
     */
    private static final JsonRecordVerifier<MultiValueDocument> MULTI_VALUE_VERIFIER = JsonRecordVerifier
            .of(MultiValueDocument.class);

    /**
     * Verifies a structured User address.
     */
    private static final JsonRecordVerifier<AddressDocument> ADDRESS_VERIFIER = JsonRecordVerifier
            .of(AddressDocument.class);

    /**
     * Verifies a User group membership value.
     */
    private static final JsonRecordVerifier<MembershipDocument> MEMBERSHIP_VERIFIER = JsonRecordVerifier
            .of(MembershipDocument.class);

    /**
     * Verifies a Group member value.
     */
    private static final JsonRecordVerifier<GroupMemberDocument> GROUP_MEMBER_VERIFIER = JsonRecordVerifier
            .of(GroupMemberDocument.class);

    /**
     * Runtime-selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Maximum accepted or emitted encoded body bytes.
     */
    private final long maximumBytes;

    /**
     * Maximum accepted JSON object or array nesting depth.
     */
    private final int maximumDepth;

    /**
     * Creates a resource codec with explicit provider and safety limits.
     *
     * @param jsonProvider externally selected JSON provider
     * @param maximumBytes positive body-size limit
     * @param maximumDepth positive JSON-depth limit
     * @throws IllegalArgumentException if the provider is {@code null}
     * @throws ValidateException        if a limit is not positive
     */
    public ScimResourceCodec(final JsonProvider jsonProvider, final long maximumBytes, final int maximumDepth) {
        this.jsonProvider = Assert.notNull(jsonProvider, "SCIM resource JSON provider must not be null");
        if (maximumBytes <= 0 || maximumDepth <= 0) {
            throw new ValidateException("SCIM resource JSON limits must be positive");
        }
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Parses one bounded SCIM JSON body into an object.
     *
     * @param body         owned request or response body
     * @param provider     selected JSON provider
     * @param maximumBytes positive size limit
     * @param maximumDepth positive depth limit
     * @return parsed JSON object
     */
    static JsonValue.ObjectValue object(
            final PayloadBody body,
            final JsonProvider provider,
            final long maximumBytes,
            final int maximumDepth) {
        Assert.notNull(body, "SCIM JSON body must not be null");
        media(body.media());
        if (body.length() <= 0 || body.length() > maximumBytes) {
            throw new ValidateException("SCIM JSON body is empty or exceeds the configured limit");
        }
        final JsonValue parsed = provider.readValue(body.bytes(maximumBytes), maximumDepth, true);
        if (!(parsed instanceof JsonValue.ObjectValue value)) {
            throw new ValidateException("SCIM JSON root must be an object");
        }
        return value;
    }

    /**
     * Serializes one SCIM JSON value under the shared size and depth limits.
     *
     * @param value        provider-neutral SCIM JSON value
     * @param provider     selected JSON provider
     * @param maximumBytes positive encoded size limit
     * @param maximumDepth positive JSON-depth limit
     * @return bounded UTF-8 JSON bytes
     */
    static byte[] bytes(
            final JsonValue value,
            final JsonProvider provider,
            final long maximumBytes,
            final int maximumDepth) {
        final byte[] encoded = Assert.notNull(provider, "SCIM JSON provider must not be null")
                .writeValue(Assert.notNull(value, "SCIM JSON value must not be null"));
        if (encoded.length == 0 || encoded.length > maximumBytes) {
            throw new ValidateException("SCIM JSON response is empty or exceeds the configured size limit");
        }
        provider.readValue(encoded, maximumDepth, true);
        return encoded;
    }

    /**
     * Requires the standard SCIM JSON media type and UTF-8 charset.
     *
     * @param value body media type
     */
    static void media(final MediaType value) {
        if (!MediaType.APPLICATION_SCIM_JSON_TYPE.isCompatible(value)) {
            throw new ValidateException("SCIM JSON body must use application/scim+json");
        }
        final String charset = value.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(value.charset())) {
            throw new ValidateException("SCIM JSON charset must be UTF-8");
        }
    }

    /**
     * Decodes one parsed User or Group resource.
     *
     * @param object       parsed resource object
     * @param resourceType exact User or Group class
     * @return typed resource
     */
    static Resource decodeResource(final JsonValue.ObjectValue object, final Class<? extends Resource> resourceType) {
        final Map<String, JsonValue> source = object.values();
        final List<String> schemas = strings(required(source, Scim.Attributes.SCHEMAS), Scim.Attributes.SCHEMAS);
        final String baseSchema = resourceType == User.class ? Scim.USER_SCHEMA
                : resourceType == Group.class ? Scim.GROUP_SCHEMA : null;
        if (baseSchema == null || !schemas.contains(baseSchema)) {
            throw new ValidateException("SCIM resource does not declare the requested core schema");
        }
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        final Map<String, JsonValue> core = new LinkedHashMap<>(source);
        for (String schema : schemas) {
            if (!baseSchema.equals(schema) && source.containsKey(schema)) {
                final JsonValue extension = source.get(schema);
                if (!(extension instanceof JsonValue.ObjectValue)) {
                    throw new ValidateException("SCIM extension member must be a JSON object");
                }
                extensions.put(schema, extension);
                core.remove(schema);
            }
        }
        final JsonValue.ObjectValue coreObject = new JsonValue.ObjectValue(core);
        if (resourceType == User.class) {
            USER_VERIFIER.validate(coreObject);
        } else {
            GROUP_VERIFIER.validate(coreObject);
        }
        final Resource.Common common = new Resource.Common(schemas,
                Optional.ofNullable(optionalString(source, Scim.Attributes.ID)),
                Optional.ofNullable(optionalString(source, Scim.Attributes.EXTERNAL_ID)),
                Optional.ofNullable(
                        source.containsKey(Scim.Attributes.META) ? meta(object(source, Scim.Attributes.META)) : null),
                new JsonValue.ObjectValue(extensions));
        return resourceType == User.class ? user(source, common) : group(source, common);
    }

    /**
     * Encodes one typed User or Group resource.
     *
     * @param resource typed resource
     * @return provider-neutral resource JSON object
     */
    static JsonValue.ObjectValue encodeResource(final Resource resource) {
        final Resource value = Assert.notNull(resource, "SCIM resource must not be null");
        if (!(value instanceof User) && !(value instanceof Group)) {
            throw new ValidateException("SCIM resource codec supports only User and Group");
        }
        final Resource.Common common = value instanceof User user ? user.common() : ((Group) value).common();
        final Map<String, JsonValue> members = common(common);
        if (value instanceof User user) {
            encodeUser(members, user);
        } else {
            encodeGroup(members, (Group) value);
        }
        members.putAll(common.extensions().values());
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Decodes all typed core User attributes.
     *
     * @param values parsed top-level members
     * @param common validated common attributes
     * @return typed User resource
     */
    private static User user(final Map<String, JsonValue> values, final Resource.Common common) {
        final JsonValue passwordValue = values.get(Scim.Attributes.PASSWORD);
        final Optional<SecretLease> password;
        if (passwordValue == null) {
            password = Optional.empty();
        } else if (passwordValue instanceof JsonValue.StringValue text) {
            password = Optional.of(new SecretLease(text.value().toCharArray()));
        } else {
            throw new ValidateException("SCIM User password must be a JSON string");
        }
        try {
            return new User(common, requiredString(values, Scim.Attributes.USER_NAME),
                    Optional.ofNullable(name(optionalObject(values, Scim.Attributes.NAME))),
                    optionalText(values, Scim.Attributes.DISPLAY_NAME), optionalText(values, Scim.Attributes.NICK_NAME),
                    optionalUri(values, Scim.Attributes.PROFILE_URL), optionalText(values, Scim.Attributes.TITLE),
                    optionalText(values, Scim.Attributes.USER_TYPE),
                    optionalText(values, Scim.Attributes.PREFERRED_LANGUAGE),
                    optionalText(values, Scim.Attributes.LOCALE), optionalText(values, Scim.Attributes.TIMEZONE),
                    optionalBoolean(values, Scim.Attributes.ACTIVE), password,
                    multiValues(values, Scim.Attributes.EMAILS), multiValues(values, Scim.Attributes.PHONE_NUMBERS),
                    multiValues(values, Scim.Attributes.IMS), multiValues(values, Scim.Attributes.PHOTOS),
                    addresses(values), memberships(values), multiValues(values, Scim.Attributes.ENTITLEMENTS),
                    multiValues(values, Scim.Attributes.ROLES), multiValues(values, Scim.Attributes.X509_CERTIFICATES));
        } catch (RuntimeException failure) {
            final SecretLease lease = password.getOrNull();
            if (lease != null) {
                lease.close();
            }
            throw failure;
        }
    }

    /**
     * Decodes one typed core Group resource.
     *
     * @param values parsed top-level members
     * @param common validated common attributes
     * @return typed Group resource
     */
    private static Group group(final Map<String, JsonValue> values, final Resource.Common common) {
        final List<Group.Member> members = new ArrayList<>();
        for (JsonValue.ObjectValue item : objects(values, Scim.Attributes.MEMBERS)) {
            GROUP_MEMBER_VERIFIER.validate(item);
            final String type = optionalString(item.values(), Scim.Attributes.TYPE);
            members.add(
                    new Group.Member(requiredString(item.values(), Scim.Attributes.VALUE),
                            optionalUri(item.values(), Scim.Attributes.REFERENCE),
                            Optional.ofNullable(type == null ? null : groupType(type)),
                            optionalText(item.values(), Scim.Attributes.DISPLAY)));
        }
        return new Group(common, requiredString(values, Scim.Attributes.DISPLAY_NAME), members);
    }

    /**
     * Encodes typed core User attributes without emitting the write-only password.
     *
     * @param members target JSON members
     * @param user    typed User resource
     */
    private static void encodeUser(final Map<String, JsonValue> members, final User user) {
        members.put(Scim.Attributes.USER_NAME, string(user.userName()));
        putObject(members, Scim.Attributes.NAME, user.name(), ScimResourceCodec::encodeName);
        put(members, Scim.Attributes.DISPLAY_NAME, user.displayName());
        put(members, Scim.Attributes.NICK_NAME, user.nickName());
        putUri(members, Scim.Attributes.PROFILE_URL, user.profileUrl());
        put(members, Scim.Attributes.TITLE, user.title());
        put(members, Scim.Attributes.USER_TYPE, user.userType());
        put(members, Scim.Attributes.PREFERRED_LANGUAGE, user.preferredLanguage());
        put(members, Scim.Attributes.LOCALE, user.locale());
        put(members, Scim.Attributes.TIMEZONE, user.timezone());
        putBoolean(members, Scim.Attributes.ACTIVE, user.active());
        putArray(members, Scim.Attributes.EMAILS, user.emails().stream().map(ScimResourceCodec::encodeMulti).toList());
        putArray(
                members,
                Scim.Attributes.PHONE_NUMBERS,
                user.phoneNumbers().stream().map(ScimResourceCodec::encodeMulti).toList());
        putArray(
                members,
                Scim.Attributes.IMS,
                user.instantMessages().stream().map(ScimResourceCodec::encodeMulti).toList());
        putArray(members, Scim.Attributes.PHOTOS, user.photos().stream().map(ScimResourceCodec::encodeMulti).toList());
        putArray(
                members,
                Scim.Attributes.ADDRESSES,
                user.addresses().stream().map(ScimResourceCodec::encodeAddress).toList());
        putArray(
                members,
                Scim.Attributes.GROUPS,
                user.groups().stream().map(ScimResourceCodec::encodeMembership).toList());
        putArray(
                members,
                Scim.Attributes.ENTITLEMENTS,
                user.entitlements().stream().map(ScimResourceCodec::encodeMulti).toList());
        putArray(members, Scim.Attributes.ROLES, user.roles().stream().map(ScimResourceCodec::encodeMulti).toList());
        putArray(
                members,
                Scim.Attributes.X509_CERTIFICATES,
                user.certificates().stream().map(ScimResourceCodec::encodeMulti).toList());
    }

    /**
     * Encodes typed core Group attributes.
     *
     * @param members target JSON members
     * @param group   typed Group resource
     */
    private static void encodeGroup(final Map<String, JsonValue> members, final Group group) {
        members.put(Scim.Attributes.DISPLAY_NAME, string(group.displayName()));
        final List<JsonValue> encoded = new ArrayList<>(group.members().size());
        for (Group.Member member : group.members()) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put(Scim.Attributes.VALUE, string(member.value()));
            putUri(item, Scim.Attributes.REFERENCE, member.reference());
            if (!member.type().isEmpty()) {
                item.put(Scim.Attributes.TYPE, string(member.type().getOrThrow().value()));
            }
            put(item, Scim.Attributes.DISPLAY, member.display());
            encoded.add(new JsonValue.ObjectValue(item));
        }
        members.put(Scim.Attributes.MEMBERS, new JsonValue.ArrayValue(encoded));
    }

    /**
     * Creates common resource JSON members.
     *
     * @param common typed common attributes
     * @return mutable ordered member map
     */
    private static Map<String, JsonValue> common(final Resource.Common common) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.SCHEMAS, array(common.schemas()));
        put(members, Scim.Attributes.ID, common.id());
        put(members, Scim.Attributes.EXTERNAL_ID, common.externalId());
        if (!common.meta().isEmpty()) {
            members.put(Scim.Attributes.META, encodeMeta(common.meta().getOrThrow()));
        }
        return members;
    }

    /**
     * Decodes standard resource metadata.
     *
     * @param value parsed metadata object
     * @return typed metadata
     */
    private static Resource.Meta meta(final JsonValue.ObjectValue value) {
        META_VERIFIER.validate(value);
        return new Resource.Meta(requiredString(value.values(), Scim.Attributes.RESOURCE_TYPE),
                optionalInstant(value.values(), Scim.Attributes.CREATED),
                optionalInstant(value.values(), Scim.Attributes.LAST_MODIFIED),
                optionalText(value.values(), Scim.Attributes.VERSION),
                optionalText(value.values(), Scim.Attributes.LOCATION));
    }

    /**
     * Encodes standard resource metadata.
     *
     * @param meta typed metadata
     * @return provider-neutral metadata object
     */
    static JsonValue.ObjectValue encodeMeta(final Resource.Meta meta) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put(Scim.Attributes.RESOURCE_TYPE, string(meta.resourceType()));
        putInstant(values, Scim.Attributes.CREATED, meta.created());
        putInstant(values, Scim.Attributes.LAST_MODIFIED, meta.lastModified());
        put(values, Scim.Attributes.VERSION, meta.version());
        put(values, Scim.Attributes.LOCATION, meta.location());
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Decodes an optional structured name.
     *
     * @param value optional object
     * @return typed name or {@code null}
     */
    private static User.Name name(final JsonValue.ObjectValue value) {
        if (value == null) {
            return null;
        }
        NAME_VERIFIER.validate(value);
        return new User.Name(optionalText(value.values(), Scim.Attributes.FORMATTED),
                optionalText(value.values(), Scim.Attributes.FAMILY_NAME),
                optionalText(value.values(), Scim.Attributes.GIVEN_NAME),
                optionalText(value.values(), Scim.Attributes.MIDDLE_NAME),
                optionalText(value.values(), Scim.Attributes.HONORIFIC_PREFIX),
                optionalText(value.values(), Scim.Attributes.HONORIFIC_SUFFIX));
    }

    /**
     * Encodes one structured name.
     *
     * @param value typed name
     * @return name JSON object
     */
    private static JsonValue.ObjectValue encodeName(final User.Name value) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        put(members, Scim.Attributes.FORMATTED, value.formatted());
        put(members, Scim.Attributes.FAMILY_NAME, value.familyName());
        put(members, Scim.Attributes.GIVEN_NAME, value.givenName());
        put(members, Scim.Attributes.MIDDLE_NAME, value.middleName());
        put(members, Scim.Attributes.HONORIFIC_PREFIX, value.honorificPrefix());
        put(members, Scim.Attributes.HONORIFIC_SUFFIX, value.honorificSuffix());
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Decodes one common multi-valued attribute array.
     *
     * @param values containing object members
     * @param name   standard array member name
     * @return immutable typed values
     */
    private static List<User.MultiValue> multiValues(final Map<String, JsonValue> values, final String name) {
        final List<User.MultiValue> result = new ArrayList<>();
        for (JsonValue.ObjectValue item : objects(values, name)) {
            MULTI_VALUE_VERIFIER.validate(item);
            result.add(
                    new User.MultiValue(requiredString(item.values(), Scim.Attributes.VALUE),
                            optionalText(item.values(), Scim.Attributes.DISPLAY),
                            optionalText(item.values(), Scim.Attributes.TYPE),
                            optionalBoolean(item.values(), Scim.Attributes.PRIMARY)));
        }
        return List.copyOf(result);
    }

    /**
     * Encodes one common multi-valued attribute item.
     *
     * @param value typed item
     * @return item JSON object
     */
    private static JsonValue.ObjectValue encodeMulti(final User.MultiValue value) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.VALUE, string(value.value()));
        put(members, Scim.Attributes.DISPLAY, value.display());
        put(members, Scim.Attributes.TYPE, value.type());
        putBoolean(members, Scim.Attributes.PRIMARY, value.primary());
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Decodes all structured addresses.
     *
     * @param values containing object members
     * @return immutable address list
     */
    private static List<User.Address> addresses(final Map<String, JsonValue> values) {
        final List<User.Address> result = new ArrayList<>();
        for (JsonValue.ObjectValue item : objects(values, Scim.Attributes.ADDRESSES)) {
            ADDRESS_VERIFIER.validate(item);
            result.add(
                    new User.Address(optionalText(item.values(), Scim.Attributes.FORMATTED),
                            optionalText(item.values(), Scim.Attributes.STREET_ADDRESS),
                            optionalText(item.values(), Scim.Attributes.LOCALITY),
                            optionalText(item.values(), Scim.Attributes.REGION),
                            optionalText(item.values(), Scim.Attributes.POSTAL_CODE),
                            optionalText(item.values(), Scim.Attributes.COUNTRY),
                            optionalText(item.values(), Scim.Attributes.TYPE),
                            optionalBoolean(item.values(), Scim.Attributes.PRIMARY)));
        }
        return List.copyOf(result);
    }

    /**
     * Encodes one structured address.
     *
     * @param value typed address
     * @return address JSON object
     */
    private static JsonValue.ObjectValue encodeAddress(final User.Address value) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        put(members, Scim.Attributes.FORMATTED, value.formatted());
        put(members, Scim.Attributes.STREET_ADDRESS, value.streetAddress());
        put(members, Scim.Attributes.LOCALITY, value.locality());
        put(members, Scim.Attributes.REGION, value.region());
        put(members, Scim.Attributes.POSTAL_CODE, value.postalCode());
        put(members, Scim.Attributes.COUNTRY, value.country());
        put(members, Scim.Attributes.TYPE, value.type());
        putBoolean(members, Scim.Attributes.PRIMARY, value.primary());
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Decodes all read-only User group memberships.
     *
     * @param values containing object members
     * @return immutable membership list
     */
    private static List<User.Membership> memberships(final Map<String, JsonValue> values) {
        final List<User.Membership> result = new ArrayList<>();
        for (JsonValue.ObjectValue item : objects(values, Scim.Attributes.GROUPS)) {
            MEMBERSHIP_VERIFIER.validate(item);
            final String type = optionalString(item.values(), Scim.Attributes.TYPE);
            result.add(
                    new User.Membership(requiredString(item.values(), Scim.Attributes.VALUE),
                            optionalUri(item.values(), Scim.Attributes.REFERENCE),
                            optionalText(item.values(), Scim.Attributes.DISPLAY),
                            Optional.ofNullable(type == null ? null : membershipType(type))));
        }
        return List.copyOf(result);
    }

    /**
     * Encodes one read-only User group membership.
     *
     * @param value typed membership
     * @return membership JSON object
     */
    private static JsonValue.ObjectValue encodeMembership(final User.Membership value) {
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(Scim.Attributes.VALUE, string(value.value()));
        putUri(members, Scim.Attributes.REFERENCE, value.reference());
        put(members, Scim.Attributes.DISPLAY, value.display());
        if (!value.type().isEmpty()) {
            members.put(Scim.Attributes.TYPE, string(value.type().getOrThrow().value()));
        }
        return new JsonValue.ObjectValue(members);
    }

    /**
     * Resolves a standard Group member resource type.
     *
     * @param value exact wire value
     * @return typed resource type
     */
    private static Group.Type groupType(final String value) {
        return switch (value) {
            case Scim.ResourceTypes.USER -> Group.Type.USER;
            case Scim.ResourceTypes.GROUP -> Group.Type.GROUP;
            default -> throw new ValidateException("SCIM Group member type must be User or Group");
        };
    }

    /**
     * Resolves a standard User membership type.
     *
     * @param value exact lowercase wire value
     * @return typed membership type
     */
    private static User.MembershipType membershipType(final String value) {
        return switch (value) {
            case "direct" -> User.MembershipType.DIRECT;
            case "indirect" -> User.MembershipType.INDIRECT;
            default -> throw new ValidateException("SCIM User group type must be direct or indirect");
        };
    }

    /**
     * Reads an optional array of JSON objects.
     *
     * @param values containing object members
     * @param name   array member name
     * @return immutable object list
     */
    private static List<JsonValue.ObjectValue> objects(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("SCIM member must be an array: " + name);
        }
        final List<JsonValue.ObjectValue> result = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("SCIM array member must contain objects: " + name);
            }
            result.add(object);
        }
        return List.copyOf(result);
    }

    /**
     * Reads an optional JSON object member.
     *
     * @param values containing object members
     * @param name   member name
     * @return object or {@code null}
     */
    private static JsonValue.ObjectValue optionalObject(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("SCIM member must be a JSON object: " + name);
        }
        return object;
    }

    /**
     * Reads a required JSON object member.
     *
     * @param values containing object members
     * @param name   member name
     * @return required object
     */
    static JsonValue.ObjectValue object(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = required(values, name);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("SCIM member must be a JSON object: " + name);
        }
        return object;
    }

    /**
     * Reads one required non-null JSON member.
     *
     * @param values containing object members
     * @param name   member name
     * @return required value
     */
    static JsonValue required(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            throw new ValidateException("SCIM member is required and must not be null: " + name);
        }
        return value;
    }

    /**
     * Reads a required non-blank JSON string.
     *
     * @param values containing object members
     * @param name   member name
     * @return decoded string
     */
    static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("SCIM member requires a non-blank string: " + name);
        }
        return value;
    }

    /**
     * Reads an optional JSON string without coercion.
     *
     * @param values containing object members
     * @param name   member name
     * @return decoded string or {@code null}
     */
    static String optionalString(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("SCIM member must be a JSON string: " + name);
        }
        return text.value();
    }

    /**
     * Reads optional text into the Bus optional type.
     *
     * @param values containing object members
     * @param name   member name
     * @return optional decoded text
     */
    private static Optional<String> optionalText(final Map<String, JsonValue> values, final String name) {
        return Optional.ofNullable(optionalString(values, name));
    }

    /**
     * Reads an optional strict JSON boolean.
     *
     * @param values containing object members
     * @param name   member name
     * @return optional boolean
     */
    private static Optional<Boolean> optionalBoolean(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.BooleanValue bool)) {
            throw new ValidateException("SCIM member must be a JSON boolean: " + name);
        }
        return Optional.of(bool.value());
    }

    /**
     * Reads an optional absolute URI string.
     *
     * @param values containing object members
     * @param name   member name
     * @return optional absolute URI
     */
    private static Optional<URI> optionalUri(final Map<String, JsonValue> values, final String name) {
        final String text = optionalString(values, name);
        if (text == null) {
            return Optional.empty();
        }
        try {
            final URI uri = URI.create(text);
            if (!uri.isAbsolute()) {
                throw new ValidateException("SCIM URI member must be absolute: " + name);
            }
            return Optional.of(uri);
        } catch (IllegalArgumentException failure) {
            throw new ValidateException("SCIM URI member is invalid: " + name, failure);
        }
    }

    /**
     * Reads an optional RFC 3339 instant.
     *
     * @param values containing object members
     * @param name   member name
     * @return optional instant
     */
    private static Optional<Instant> optionalInstant(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException failure) {
            throw new ValidateException("SCIM dateTime member is invalid: " + name, failure);
        }
    }

    /**
     * Decodes a non-empty JSON string array.
     *
     * @param value array JSON value
     * @param name  validation member name
     * @return immutable decoded strings
     */
    static List<String> strings(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
            throw new ValidateException("SCIM member must be a non-empty string array: " + name);
        }
        final List<String> values = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue text) || text.value().isBlank()) {
                throw new ValidateException("SCIM array member contains a non-string or blank value: " + name);
            }
            values.add(text.value());
        }
        return List.copyOf(values);
    }

    /**
     * Encodes a string list as a JSON array.
     *
     * @param values source strings
     * @return provider-neutral array
     */
    static JsonValue.ArrayValue array(final List<String> values) {
        return new JsonValue.ArrayValue(
                values.stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast).toList());
    }

    /**
     * Adds a present optional string.
     *
     * @param values target members
     * @param name   member name
     * @param value  optional string
     */
    static void put(final Map<String, JsonValue> values, final String name, final Optional<String> value) {
        if (!value.isEmpty()) {
            values.put(name, string(value.getOrThrow()));
        }
    }

    /**
     * Adds a present optional URI.
     *
     * @param values target members
     * @param name   member name
     * @param value  optional URI
     */
    private static void putUri(final Map<String, JsonValue> values, final String name, final Optional<URI> value) {
        if (!value.isEmpty()) {
            values.put(name, string(value.getOrThrow().toString()));
        }
    }

    /**
     * Adds a present optional boolean.
     *
     * @param values target members
     * @param name   member name
     * @param value  optional boolean
     */
    private static void putBoolean(
            final Map<String, JsonValue> values,
            final String name,
            final Optional<Boolean> value) {
        if (!value.isEmpty()) {
            values.put(name, new JsonValue.BooleanValue(value.getOrThrow()));
        }
    }

    /**
     * Adds a non-empty encoded array.
     *
     * @param values  target members
     * @param name    member name
     * @param encoded encoded items
     */
    private static void putArray(
            final Map<String, JsonValue> values,
            final String name,
            final List<? extends JsonValue> encoded) {
        if (!encoded.isEmpty()) {
            final List<JsonValue> items = new ArrayList<>(encoded.size());
            items.addAll(encoded);
            values.put(name, new JsonValue.ArrayValue(items));
        }
    }

    /**
     * Adds a present typed object through its encoder.
     *
     * @param values  target members
     * @param name    member name
     * @param value   optional typed value
     * @param encoder typed object encoder
     * @param <T>     typed value kind
     */
    private static <T> void putObject(
            final Map<String, JsonValue> values,
            final String name,
            final Optional<T> value,
            final Function<T, JsonValue.ObjectValue> encoder) {
        if (!value.isEmpty()) {
            values.put(name, encoder.apply(value.getOrThrow()));
        }
    }

    /**
     * Adds a present optional instant using RFC 3339 text.
     *
     * @param values target members
     * @param name   member name
     * @param value  optional instant
     */
    private static void putInstant(
            final Map<String, JsonValue> values,
            final String name,
            final Optional<Instant> value) {
        if (!value.isEmpty()) {
            values.put(name, string(value.getOrThrow().toString()));
        }
    }

    /**
     * Creates one JSON string value.
     *
     * @param value text value
     * @return JSON string
     */
    private static JsonValue.StringValue string(final String value) {
        return new JsonValue.StringValue(value);
    }

    /**
     * Decodes and closes one standard User or Group request body.
     *
     * @param request      owned Fabric request body
     * @param resourceType exact User or Group class
     * @param <R>          requested resource type
     * @return typed resource owning any inbound password lease
     */
    public <R extends Resource> R decode(final HttpRequest request, final Class<R> resourceType) {
        final HttpRequest encoded = Assert.notNull(request, "SCIM resource HTTP request must not be null");
        final Class<R> type = Assert.notNull(resourceType, "SCIM resource class must not be null");
        if (type != User.class && type != Group.class) {
            throw new ValidateException("SCIM resource decoder supports only User and Group");
        }
        if (encoded.method() != Http.Method.POST && encoded.method() != Http.Method.PUT) {
            throw new ValidateException("SCIM resource body requires HTTP POST or PUT");
        }
        final PayloadBody body = encoded.body();
        try (body) {
            return type.cast(decodeResource(object(body, jsonProvider, maximumBytes, maximumDepth), type));
        }
    }

    /**
     * Encodes one successful standard User or Group response.
     *
     * @param request  originating Fabric request
     * @param resource typed service-provider resource
     * @param status   HTTP 200 or 201 status
     * @return complete SCIM JSON response
     */
    public HttpResponse encode(final HttpRequest request, final Resource resource, final int status) {
        final HttpRequest origin = Assert.notNull(request, "SCIM resource origin request must not be null");
        final Resource value = Assert.notNull(resource, "SCIM response resource must not be null");
        if (status != Http.Status.OK && status != Http.Status.CREATED) {
            throw new ValidateException("SCIM resource success status must be 200 or 201");
        }
        if (!(value instanceof User) && !(value instanceof Group)) {
            throw new ValidateException("SCIM resource response supports only User and Group");
        }
        final Resource.Common common = value instanceof User user ? user.common() : ((Group) value).common();
        if (common.id().isEmpty() || value instanceof User user && !user.password().isEmpty()) {
            throw new ValidateException("SCIM response resource requires id and must not expose password");
        }
        final Resource.Meta meta = common.meta().getOrNull();
        if (status == Http.Status.CREATED && (meta == null || meta.location().isEmpty())) {
            throw new ValidateException("SCIM 201 response requires meta.location");
        }
        final Headers.Builder headers = Headers.builder().add(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE)
                .add(Http.Header.PRAGMA, Http.Cache.NO_CACHE);
        if (meta != null && !meta.location().isEmpty()) {
            headers.add(Http.Header.LOCATION, meta.location().getOrThrow());
        }
        if (meta != null && !meta.version().isEmpty()) {
            headers.add(Http.Header.ETAG, meta.version().getOrThrow());
        }
        final byte[] body = bytes(encodeResource(value), jsonProvider, maximumBytes, maximumDepth);
        return HttpResponse.builder().request(origin).code(status).headers(headers.build())
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_SCIM_JSON_TYPE)).build();
    }

    /**
     * Encodes the standard empty resource-deletion response.
     *
     * @param request originating Fabric request
     * @return HTTP 204 response without a body
     */
    public HttpResponse deleted(final HttpRequest request) {
        return HttpResponse.builder().request(Assert.notNull(request, "SCIM delete origin request must not be null"))
                .code(Http.Status.NO_CONTENT)
                .headers(
                        Headers.of(
                                Http.Header.CACHE_CONTROL,
                                Http.Cache.NO_STORE,
                                Http.Header.PRAGMA,
                                Http.Cache.NO_CACHE))
                .build();
    }

    /**
     * Owns the exact standard core User JSON vocabulary.
     *
     * @param schemas           required schema identifiers
     * @param id                optional resource id
     * @param externalId        optional external id
     * @param meta              optional resource metadata
     * @param userName          required user name
     * @param name              optional structured name
     * @param displayName       optional display name
     * @param nickName          optional nickname
     * @param profileUrl        optional profile URL
     * @param title             optional title
     * @param userType          optional user type
     * @param preferredLanguage optional language
     * @param locale            optional locale
     * @param timezone          optional timezone
     * @param active            optional active flag
     * @param password          optional write-only password
     * @param emails            optional email array
     * @param phoneNumbers      optional telephone array
     * @param ims               optional instant-message array
     * @param photos            optional photo array
     * @param addresses         optional address array
     * @param groups            optional membership array
     * @param entitlements      optional entitlement array
     * @param roles             optional role array
     * @param x509Certificates  optional certificate array
     */
    private record UserDocument(JsonValue schemas, Optional<JsonValue> id, Optional<JsonValue> externalId,
            Optional<JsonValue> meta, JsonValue userName, Optional<JsonValue> name, Optional<JsonValue> displayName,
            Optional<JsonValue> nickName, Optional<JsonValue> profileUrl, Optional<JsonValue> title,
            Optional<JsonValue> userType, Optional<JsonValue> preferredLanguage, Optional<JsonValue> locale,
            Optional<JsonValue> timezone, Optional<JsonValue> active, Optional<JsonValue> password,
            Optional<JsonValue> emails, Optional<JsonValue> phoneNumbers, Optional<JsonValue> ims,
            Optional<JsonValue> photos, Optional<JsonValue> addresses, Optional<JsonValue> groups,
            Optional<JsonValue> entitlements, Optional<JsonValue> roles, Optional<JsonValue> x509Certificates) {

    }

    /**
     * Owns the exact standard core Group JSON vocabulary.
     *
     * @param schemas     required schema identifiers
     * @param id          optional resource id
     * @param externalId  optional external id
     * @param meta        optional resource metadata
     * @param displayName required display name
     * @param members     optional membership array
     */
    private record GroupDocument(JsonValue schemas, Optional<JsonValue> id, Optional<JsonValue> externalId,
            Optional<JsonValue> meta, JsonValue displayName, Optional<JsonValue> members) {

    }

    /**
     * Owns the exact standard Meta vocabulary.
     *
     * @param resourceType required resource type
     * @param created      optional creation instant
     * @param lastModified optional modification instant
     * @param version      optional entity-tag
     * @param location     optional resource location
     */
    private record MetaDocument(JsonValue resourceType, Optional<JsonValue> created, Optional<JsonValue> lastModified,
            Optional<JsonValue> version, Optional<JsonValue> location) {

    }

    /**
     * Owns the exact structured-name vocabulary.
     *
     * @param formatted       optional formatted name
     * @param familyName      optional family name
     * @param givenName       optional given name
     * @param middleName      optional middle name
     * @param honorificPrefix optional prefix
     * @param honorificSuffix optional suffix
     */
    private record NameDocument(Optional<JsonValue> formatted, Optional<JsonValue> familyName,
            Optional<JsonValue> givenName, Optional<JsonValue> middleName, Optional<JsonValue> honorificPrefix,
            Optional<JsonValue> honorificSuffix) {

    }

    /**
     * Owns the exact common multi-valued attribute vocabulary.
     *
     * @param value   required primary value
     * @param display optional display value
     * @param type    optional type
     * @param primary optional primary flag
     */
    private record MultiValueDocument(JsonValue value, Optional<JsonValue> display, Optional<JsonValue> type,
            Optional<JsonValue> primary) {

    }

    /**
     * Owns the exact address vocabulary.
     *
     * @param formatted     optional formatted address
     * @param streetAddress optional street address
     * @param locality      optional locality
     * @param region        optional region
     * @param postalCode    optional postal code
     * @param country       optional country
     * @param type          optional address type
     * @param primary       optional primary flag
     */
    private record AddressDocument(Optional<JsonValue> formatted, Optional<JsonValue> streetAddress,
            Optional<JsonValue> locality, Optional<JsonValue> region, Optional<JsonValue> postalCode,
            Optional<JsonValue> country, Optional<JsonValue> type, Optional<JsonValue> primary) {

    }

    /**
     * Owns the exact User membership vocabulary.
     *
     * @param value     required group id
     * @param reference optional group reference
     * @param display   optional display value
     * @param type      optional membership type
     */
    private record MembershipDocument(JsonValue value, @Member(Scim.Attributes.REFERENCE) Optional<JsonValue> reference,
            Optional<JsonValue> display, Optional<JsonValue> type) {

    }

    /**
     * Owns the exact Group member vocabulary.
     *
     * @param value     required member id
     * @param reference optional member reference
     * @param type      optional resource type
     * @param display   optional display value
     */
    private record GroupMemberDocument(JsonValue value,
            @Member(Scim.Attributes.REFERENCE) Optional<JsonValue> reference, Optional<JsonValue> type,
            Optional<JsonValue> display) {

    }

}
