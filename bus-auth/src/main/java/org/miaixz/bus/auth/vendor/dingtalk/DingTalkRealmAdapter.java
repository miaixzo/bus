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
package org.miaixz.bus.auth.vendor.dingtalk;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Realm;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Payload;

/**
 * Implements the provider-neutral DingTalk realm surface over the official management API.
 * <p>
 * Snapshot pagination advances through the finite {@link Phase} order {@code DEPARTMENTS}, {@code USERS},
 * {@code ROLES}, and {@code ROLE_MEMBERS}. Department-dependent phases replay a stable depth-first department walk from
 * the root and retain only the global offset, prefix fingerprint, current parent, official numeric offset, and relation
 * offset in the opaque cursor. Role pagination and role-member pagination retain only official numeric offsets and the
 * current stable role identifier. The adapter never persists directory data or implements changes.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DingTalkRealmAdapter implements VendorAdapter {

    /**
     * Root department identifier accepted by DingTalk's direct-child department endpoint.
     */
    private static final String ROOT_DEPARTMENT_ID = "1";

    /**
     * DingTalk business code indicating an invalid application access token.
     */
    private static final long INVALID_ACCESS_TOKEN_CODE = 40014L;

    /**
     * DingTalk business code indicating an expired application access token.
     */
    private static final long EXPIRED_ACCESS_TOKEN_CODE = 42001L;

    /**
     * DingTalk business code indicating that the application lacks visible contact permission.
     */
    private static final long PERMISSION_DENIED_CODE = 60011L;

    /**
     * DingTalk business code explicitly indicating that a requested user does not exist.
     */
    private static final long USER_NOT_FOUND_CODE = 60121L;

    /**
     * Empty immutable JSON object used by projections without allow-listed attributes.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Resource categories exposed by the enterprise Variant.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set
            .of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.ROLE);

    /**
     * Exact ordered management-target key set required by this adapter.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.ENTERPRISE_USERS,
            Builder.ENTERPRISE_USER,
            Builder.ENTERPRISE_ORGANIZATIONS,
            Builder.ENTERPRISE_ORGANIZATION_USERS,
            Builder.ENTERPRISE_ROLES,
            Builder.ENTERPRISE_ROLE_MEMBERS);

    /**
     * Selected immutable DingTalk enterprise Variant.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated DingTalk enterprise deployment options containing only a credential reference.
     */
    private final DingTalkOptions options;

    /**
     * Caller-owned execution services used without taking lifecycle ownership.
     */
    private final DriverServices services;

    /**
     * Resolved manifest-owned token and management endpoints.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Source-private cache holding only the short-lived upstream application token.
     */
    private final CacheX<String, Access> accessCache;

    /**
     * Creates one Source-isolated DingTalk realm adapter.
     *
     * @param spaceId  registration space used to isolate credential resolution
     * @param sourceId registered Source identifier used to validate Source ownership
     * @param manifest exact DingTalk manifest
     * @param variant  exact selected enterprise Variant
     * @param options  validated DingTalk enterprise options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if the manifest, Variant, protocol, targets, or options are inconsistent
     */
    public DingTalkRealmAdapter(final String spaceId, final String sourceId, final DingTalkManifest manifest,
            final VariantManifest.Variant variant, final DingTalkOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "DingTalk enterprise space id must not be blank");
        Assert.notBlank(sourceId, "DingTalk enterprise Source id must not be blank");
        final DingTalkManifest selectedManifest = Assert
                .notNull(manifest, "DingTalk enterprise manifest must not be null");
        this.variant = Assert.notNull(variant, "DingTalk enterprise Variant must not be null");
        this.options = Assert.notNull(options, "DingTalk enterprise options must not be null");
        this.services = Assert.notNull(services, "DingTalk enterprise services must not be null");
        if (!DingTalkManifest.ID.equals(selectedManifest.vendor())
                || !selectedManifest.variant(DingTalkManifest.ENTERPRISE).equals(this.variant)
                || !DingTalkManifest.ID.equals(this.variant.platform())
                || !DingTalkManifest.ENTERPRISE.equals(this.variant.variant())
                || this.variant.protocol() != Protocol.HTTPS || !DingTalkManifest.ID.equals(this.options.vendor())
                || !DingTalkManifest.ENTERPRISE.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || !this.options.scopes().isEmpty() || this.options.orgType().isPresent()
                || this.options.corpId().isPresent() || this.options.exclusiveLogin()
                || this.options.exclusiveCorpId().isPresent()) {
            throw new ValidateException("DingTalk realm adapter requires the dingtalk/enterprise HTTPS manifest");
        }
        this.services.securityBaseline().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (this.targets.token().isEmpty()
                || !List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("DingTalk enterprise manifest has an invalid management target set");
        }
        if (!this.targets.management().get(Builder.ENTERPRISE_USERS)
                .equals(this.targets.management().get(Builder.ENTERPRISE_USER))) {
            throw new ValidateException("DingTalk enterprise user-detail targets must resolve identically");
        }
        this.accessCache = new MemoryCache<>(FabricX.clock(this.services.fabric())::millis);
    }

    /**
     * Reports whether one authenticated operation was rejected for an invalid upstream token.
     *
     * @param outcome completed authenticated operation outcome
     * @return whether the outcome carries the shared 401 error
     */
    private static boolean unauthorized(final Outcome<?> outcome) {
        return outcome instanceof Outcome.Rejected<?> rejected && ErrorCode._401.equals(rejected.failure().error());
    }

    /**
     * Converts one department projection into a provider-neutral organization resource.
     *
     * @param department minimal validated department projection
     * @param observedAt shared invocation observation instant
     * @return immutable organization resource
     */
    private static Realm.Resource departmentResource(final Department department, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ORGANIZATION, department.id()), Map.of(), department.name(),
                Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one user projection into a provider-neutral user resource.
     *
     * @param user       minimal validated user projection
     * @param observedAt shared invocation observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()), Map.of("userid", user.id()), user.name(),
                user.active() ? Realm.State.ACTIVE : Realm.State.INACTIVE, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one role projection into a provider-neutral role resource.
     *
     * @param role       minimal validated role projection
     * @param observedAt shared invocation observation instant
     * @return immutable role resource
     */
    private static Realm.Resource roleResource(final Role role, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ROLE, role.id()), Map.of(), role.name(), Realm.State.UNKNOWN,
                EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one department parent field into a directed child-to-parent relation.
     *
     * @param department minimal validated child department projection
     * @param observedAt shared invocation observation instant
     * @return immutable parent relation
     */
    private static Realm.Relation parentRelation(final Department department, final Instant observedAt) {
        return relation(
                Realm.RelationKind.PARENT,
                Realm.Kind.ORGANIZATION,
                department.id(),
                Realm.Kind.ORGANIZATION,
                department.parentId(),
                observedAt);
    }

    /**
     * Creates one directed user-to-container membership relation.
     *
     * @param userId        stable user identifier
     * @param containerKind normalized organization or role category
     * @param containerId   stable container identifier
     * @param observedAt    shared invocation observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(
            final String userId,
            final Realm.Kind containerKind,
            final String containerId,
            final Instant observedAt) {
        return relation(Realm.RelationKind.MEMBER, Realm.Kind.USER, userId, containerKind, containerId, observedAt);
    }

    /**
     * Creates one directed user-to-role assignment relation.
     *
     * @param userId     stable assigned user identifier
     * @param roleId     stable assigned role identifier
     * @param observedAt shared invocation observation instant
     * @return immutable role-member relation
     */
    private static Realm.Relation roleMemberRelation(
            final String userId,
            final String roleId,
            final Instant observedAt) {
        return relation(Realm.RelationKind.ROLE_MEMBER, Realm.Kind.USER, userId, Realm.Kind.ROLE, roleId, observedAt);
    }

    /**
     * Creates one directed managed-user-to-manager relation.
     *
     * @param userId     stable managed user identifier
     * @param managerId  stable manager user identifier
     * @param observedAt shared invocation observation instant
     * @return immutable manager relation
     */
    private static Realm.Relation managerRelation(
            final String userId,
            final String managerId,
            final Instant observedAt) {
        return relation(Realm.RelationKind.MANAGER, Realm.Kind.USER, userId, Realm.Kind.USER, managerId, observedAt);
    }

    /**
     * Creates one attribute-free directed enterprise relation.
     *
     * @param relationKind normalized relation semantic
     * @param fromKind     normalized source resource category
     * @param fromId       stable source identifier
     * @param toKind       normalized target resource category
     * @param toId         stable target identifier
     * @param observedAt   shared invocation observation instant
     * @return immutable provider-neutral relation
     */
    private static Realm.Relation relation(
            final Realm.RelationKind relationKind,
            final Realm.Kind fromKind,
            final String fromId,
            final Realm.Kind toKind,
            final String toId,
            final Instant observedAt) {
        return new Realm.Relation(
                new Realm.RelationKey(relationKind, new Realm.Key(fromKind, fromId), new Realm.Key(toKind, toId)),
                EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Selects the next finite snapshot phase needed by the requested resource kinds.
     *
     * @param phase current completed phase
     * @param kinds exact requested kind list
     * @return next required phase, or {@code null} when the snapshot is complete
     */
    private static Phase nextPhase(final Phase phase, final List<Realm.Kind> kinds) {
        return switch (phase) {
            case DEPARTMENTS -> kinds.contains(Realm.Kind.USER) ? Phase.USERS
                    : kinds.contains(Realm.Kind.ROLE) ? Phase.ROLES : null;
            case USERS -> kinds.contains(Realm.Kind.ROLE) ? Phase.ROLES : null;
            case ROLES -> Phase.ROLE_MEMBERS;
            case ROLE_MEMBERS -> null;
        };
    }

    /**
     * Encodes the exact phase-specific position member set.
     *
     * @param phase    finite snapshot phase
     * @param position validated phase position
     * @return immutable ordered JSON position object
     */
    private static JsonValue.ObjectValue position(final Phase phase, final Position position) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        switch (phase) {
            case DEPARTMENTS -> {
                values.put(Builder.CURSOR_OFFSET_FIELD, number(position.offset()));
                values.put(Builder.CURSOR_FINGERPRINT_FIELD, nullable(position.fingerprint()));
            }
            case USERS -> {
                values.put(Builder.CURSOR_OFFSET_FIELD, number(position.offset()));
                values.put(Builder.CURSOR_FINGERPRINT_FIELD, nullable(position.fingerprint()));
                values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
                values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
                values.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(position.relationOffset()));
            }
            case ROLES -> values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
            case ROLE_MEMBERS -> {
                values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
                values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
                values.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(position.relationOffset()));
            }
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Decodes and validates the exact position shape for one finite phase.
     *
     * @param phase finite snapshot phase
     * @param value decoded JSON position object
     * @return validated internal position
     */
    private static Position position(final Phase phase, final JsonValue.ObjectValue value) {
        return switch (phase) {
            case DEPARTMENTS -> {
                exactMembers(
                        value,
                        Set.of(Builder.CURSOR_OFFSET_FIELD, Builder.CURSOR_FINGERPRINT_FIELD),
                        "department cursor position");
                yield Position.tree(
                        nonNegativeInt(value, Builder.CURSOR_OFFSET_FIELD),
                        nullableString(value, Builder.CURSOR_FINGERPRINT_FIELD).getOrNull());
            }
            case USERS -> {
                exactMembers(
                        value,
                        Set.of(
                                Builder.CURSOR_OFFSET_FIELD,
                                Builder.CURSOR_FINGERPRINT_FIELD,
                                Builder.CURSOR_PARENT_ID_FIELD,
                                Builder.CURSOR_NEXT_FIELD,
                                Builder.CURSOR_RELATION_OFFSET_FIELD),
                        "user cursor position");
                yield Position.users(
                        nonNegativeInt(value, Builder.CURSOR_OFFSET_FIELD),
                        nullableString(value, Builder.CURSOR_FINGERPRINT_FIELD),
                        nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                        nullableString(value, Builder.CURSOR_NEXT_FIELD),
                        nonNegativeInt(value, Builder.CURSOR_RELATION_OFFSET_FIELD));
            }
            case ROLES -> {
                exactMembers(value, Set.of(Builder.CURSOR_NEXT_FIELD), "role cursor position");
                yield Position.page(nullableString(value, Builder.CURSOR_NEXT_FIELD));
            }
            case ROLE_MEMBERS -> {
                exactMembers(
                        value,
                        Set.of(
                                Builder.CURSOR_PARENT_ID_FIELD,
                                Builder.CURSOR_NEXT_FIELD,
                                Builder.CURSOR_RELATION_OFFSET_FIELD),
                        "role-member cursor position");
                yield Position.members(
                        nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                        nullableString(value, Builder.CURSOR_NEXT_FIELD),
                        nonNegativeInt(value, Builder.CURSOR_RELATION_OFFSET_FIELD));
            }
        };
    }

    /**
     * Decodes the stable requested-kind code array without using enum ordinal values.
     *
     * @param values decoded JSON kind array
     * @return immutable requested kinds in stable code order
     */
    private static List<Realm.Kind> kinds(final List<JsonValue> values) {
        final LinkedHashSet<Realm.Kind> kinds = new LinkedHashSet<>();
        int previous = 0;
        for (JsonValue value : values) {
            if (!(value instanceof JsonValue.NumberValue number)) {
                throw new ValidateException("DingTalk cursor kind must be an integer code");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("DingTalk cursor kind code is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !kinds.add(kind)) {
                throw new ValidateException("DingTalk cursor kinds are not in canonical code order");
            }
            previous = code;
        }
        if (kinds.isEmpty()) {
            throw new ValidateException("DingTalk cursor kinds must not be empty");
        }
        return List.copyOf(kinds);
    }

    /**
     * Resolves one stable enterprise kind code without depending on enum declaration order.
     *
     * @param code stable persisted kind code
     * @return exact enterprise kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : Realm.Kind.values()) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("DingTalk cursor contains an unknown kind code");
    }

    /**
     * Rejects unsupported snapshot kinds instead of silently ignoring them.
     *
     * @param kinds normalized non-empty requested kind set
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (!SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("DingTalk enterprise snapshot contains an unsupported kind");
        }
    }

    /**
     * Parses one minimal DingTalk department projection.
     *
     * @param value decoded department object
     * @return validated minimal department projection
     */
    private static Department department(final JsonValue.ObjectValue value) {
        return new Department(requiredIdentifier(value, "dept_id"), requiredString(value, "name"),
                requiredIdentifier(value, "parent_id"));
    }

    /**
     * Parses one minimal DingTalk user projection.
     *
     * @param value decoded user object
     * @return validated minimal user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        final List<String> departmentIds = new ArrayList<>();
        for (JsonValue department : requiredArray(value, "dept_id_list")) {
            departmentIds.add(identifier(department, "DingTalk user department identifier"));
        }
        return new User(requiredString(value, "userid"), requiredString(value, "name"),
                requiredBoolean(value, "active"), departmentIds, optionalText(value, "manager_userid"));
    }

    /**
     * Parses one minimal DingTalk role projection.
     *
     * @param value decoded role object
     * @return validated minimal role projection
     */
    private static Role role(final JsonValue.ObjectValue value) {
        return new Role(requiredIdentifier(value, "role_id"), requiredString(value, "name"));
    }

    /**
     * Parses one minimal DingTalk role-member projection.
     *
     * @param value decoded member object
     * @return validated minimal member projection
     */
    private static RoleMember member(final JsonValue.ObjectValue value) {
        return new RoleMember(requiredString(value, "userid"));
    }

    /**
     * Resolves the next official numeric offset from one paginated DingTalk result.
     *
     * @param data        decoded paginated result object
     * @param current     official input offset
     * @param resultCount number of stable projections read from the current page
     * @return canonical decimal continuation when {@code has_more} is true
     */
    private static Optional<String> offset(
            final JsonValue.ObjectValue data,
            final long current,
            final int resultCount) {
        final boolean hasMore = requiredBoolean(data, "has_more");
        if (!hasMore) {
            return Optional.empty();
        }
        final long next;
        if (data.values().containsKey("next_cursor")) {
            next = requiredLong(data, "next_cursor");
        } else {
            if (resultCount <= 0) {
                throw new ValidateException("DingTalk paginated response cannot advance an empty page");
            }
            try {
                next = Math.addExact(current, resultCount);
            } catch (ArithmeticException cause) {
                throw new ValidateException("DingTalk pagination offset overflowed", cause);
            }
        }
        if (next <= current) {
            throw new ValidateException("DingTalk pagination offset did not advance");
        }
        return Optional.of(Long.toString(next));
    }

    /**
     * Reads one required JSON object member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return required object member
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        return requiredObject(object.values().get(name), name);
    }

    /**
     * Narrows one decoded JSON value to a required object.
     *
     * @param value decoded JSON value
     * @param label safe semantic label
     * @return required object value
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue value, final String label) {
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException(label + " must be a JSON object");
        }
        return object;
    }

    /**
     * Reads one required JSON array member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return immutable decoded array elements
     */
    private static List<JsonValue> requiredArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array");
        }
        return array.values();
    }

    /**
     * Reads one required non-blank JSON string without trimming it.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return original validated string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string");
        }
        return requireText(text.value(), name);
    }

    /**
     * Reads one required DingTalk identifier represented as either a JSON string or exact positive integer.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return canonical non-blank identifier text
     */
    private static String requiredIdentifier(final JsonValue.ObjectValue object, final String name) {
        return identifier(object.values().get(name), name);
    }

    /**
     * Converts one DingTalk string or exact positive integer identifier to canonical text.
     *
     * @param value decoded identifier value
     * @param label safe semantic field label
     * @return canonical stable identifier text
     */
    private static String identifier(final JsonValue value, final String label) {
        if (value instanceof JsonValue.StringValue text) {
            return requireText(text.value(), label);
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                final long identifier = number.value().longValueExact();
                if (identifier <= 0L) {
                    throw new ValidateException(label + " must be positive");
                }
                return Long.toString(identifier);
            } catch (ArithmeticException cause) {
                throw new ValidateException(label + " must be an exact integer", cause);
            }
        }
        throw new ValidateException(label + " must be a JSON string or exact integer");
    }

    /**
     * Encodes one canonical positive numeric identifier for a DingTalk request body.
     *
     * @param value canonical identifier text
     * @param label safe semantic field label
     * @return exact JSON number value
     */
    private static JsonValue.NumberValue identifierNumber(final String value, final String label) {
        try {
            final long identifier = Long.parseLong(requireText(value, label));
            if (identifier <= 0L) {
                throw new ValidateException(label + " must be positive");
            }
            return number(identifier);
        } catch (NumberFormatException cause) {
            throw new ValidateException(label + " must be a canonical integer", cause);
        }
    }

    /**
     * Decodes one canonical non-negative numeric continuation from an opaque cursor position.
     *
     * @param value optional canonical decimal continuation
     * @param label safe semantic field label
     * @return non-negative official input offset
     */
    private static long continuation(final Optional<String> value, final String label) {
        if (value.isEmpty()) {
            return 0L;
        }
        final String text = requireText(value.getOrNull(), label);
        try {
            final long offset = Long.parseLong(text);
            if (offset < 0L || !Long.toString(offset).equals(text)) {
                throw new ValidateException(label + " must be a canonical non-negative integer");
            }
            return offset;
        } catch (NumberFormatException cause) {
            throw new ValidateException(label + " must be a canonical non-negative integer", cause);
        }
    }

    /**
     * Reads one optional non-blank JSON string or JSON null.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return normalized optional original string
     */
    private static Optional<String> optionalText(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string or null");
        }
        if (text.value().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(requireText(text.value(), name));
    }

    /**
     * Reads one required JSON boolean member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return decoded boolean value
     */
    private static boolean requiredBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.BooleanValue flag)) {
            throw new ValidateException(name + " must be a JSON boolean");
        }
        return flag.value();
    }

    /**
     * Reads one required exact JSON long member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return exact long value
     */
    private static long requiredLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException(name + " must be a JSON number");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException(name + " must be an exact integer", cause);
        }
    }

    /**
     * Reads one required positive JSON long member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return positive exact long value
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = requiredLong(object, name);
        if (value <= 0L) {
            throw new ValidateException(name + " must be positive");
        }
        return value;
    }

    /**
     * Reads one required exact JSON int member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return exact int value
     */
    private static int requiredInt(final JsonValue.ObjectValue object, final String name) {
        final long value = requiredLong(object, name);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new ValidateException(name + " exceeds the integer range");
        }
        return (int) value;
    }

    /**
     * Reads one non-negative exact JSON int member.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return non-negative int value
     */
    private static int nonNegativeInt(final JsonValue.ObjectValue object, final String name) {
        final int value = requiredInt(object, name);
        if (value < 0) {
            throw new ValidateException(name + " must not be negative");
        }
        return value;
    }

    /**
     * Reads one canonical optional JSON string member whose explicit absence is JSON null.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return optional validated string
     */
    private static Optional<String> nullableString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string or null");
        }
        return Optional.of(requireText(text.value(), name));
    }

    /**
     * Verifies an object's exact member-name closure.
     *
     * @param object   decoded object
     * @param expected exact allowed and required member set
     * @param label    safe semantic label
     */
    private static void exactMembers(
            final JsonValue.ObjectValue object,
            final Set<String> expected,
            final String label) {
        if (!object.values().keySet().equals(expected)) {
            throw new ValidateException(label + " contains an invalid member set");
        }
    }

    /**
     * Converts one optional string into a JSON string or explicit JSON null.
     *
     * @param value optional original string
     * @return provider-neutral JSON value
     */
    private static JsonValue nullable(final Optional<String> value) {
        return value.isPresent() ? new JsonValue.StringValue(value.getOrNull()) : JsonValue.NullValue.instance();
    }

    /**
     * Creates one exact JSON integer value.
     *
     * @param value integral value
     * @return immutable JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Validates required text without silently normalizing surrounding whitespace.
     *
     * @param value caller or upstream supplied text
     * @param label safe semantic field label
     * @return original validated text
     */
    private static String requireText(final String value, final String label) {
        final String text;
        try {
            text = Assert.notBlank(value, label + " must not be blank");
        } catch (IllegalArgumentException cause) {
            throw new ValidateException(label + " must not be blank", cause);
        }
        if (!text.equals(text.trim())) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return text;
    }

    /**
     * Builds safe allow-listed outcome details for one upstream result.
     *
     * @param operation safe enterprise operation label
     * @param status    upstream HTTP status
     * @param errorCode non-sensitive DingTalk business code, or {@code null}
     * @param headers   upstream headers used only for Retry-After normalization
     * @return immutable allow-listed scalar detail map
     */
    private static Map<String, JsonValue> details(
            final String operation,
            final int status,
            final Long errorCode,
            final FabricX.Headers headers) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(DingTalkManifest.ID.value()));
        details.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(DingTalkManifest.ENTERPRISE.value()));
        details.put(Builder.OPERATION_FIELD, new JsonValue.StringValue(operation));
        details.put(Builder.HTTP_STATUS_FIELD, number(status));
        if (errorCode != null) {
            details.put(Builder.ERROR_CODE_FIELD, number(errorCode));
        }
        final String retryAfter = headers.get(Http.Header.RETRY_AFTER);
        if (retryAfter != null) {
            try {
                final long seconds = Long.parseLong(retryAfter);
                if (seconds >= 0L) {
                    details.put(Builder.RETRY_AFTER_SECONDS_FIELD, number(seconds));
                }
            } catch (NumberFormatException ignored) {
                details.remove(Builder.RETRY_AFTER_SECONDS_FIELD);
            }
        }
        return Collections.unmodifiableMap(details);
    }

    /**
     * Erases one optional sensitive mutable byte array.
     *
     * @param value mutable bytes, or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) Normal._0);
        }
    }

    /**
     * Narrows one delegated outcome through the declared capability response type.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared response class
     * @param <S>          expected successful response type
     * @return type-safe delegated outcome
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "DingTalk delegated operation returned an unsupported outcome");
        });
    }

    /**
     * Propagates one failure outcome across an internal success type boundary.
     *
     * @param outcome internal outcome that must not contain a success value
     * @param <T>     target success type
     * @return rejection or failure with the original safe Failure value
     */
    private static <T> Outcome<T> propagate(final Outcome<?> outcome) {
        return switch (outcome) {
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "DingTalk internal operation could not be propagated");
        };
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe missing-capability rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> missing() {
        return completed(
                rejected(ErrorCode._400, "DingTalk enterprise capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(
                rejected(
                        ErrorCode._400,
                        "DingTalk enterprise request does not match the selected capability contract"));
    }

    /**
     * Creates a safe expected rejection without structured details.
     *
     * @param code        shared Bus error code
     * @param description safe non-sensitive description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final Errors code, final String description) {
        return rejected(code, description, Map.of());
    }

    /**
     * Creates a safe expected rejection with allow-listed details.
     *
     * @param code        shared Bus error code
     * @param description safe non-sensitive description
     * @param details     allow-listed non-sensitive scalar details
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.rejected(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Creates a safe operational failure without structured details.
     *
     * @param code        shared Bus error code
     * @param description safe non-sensitive description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates a safe operational failure with allow-listed details.
     *
     * @param code        shared Bus error code
     * @param description safe non-sensitive description
     * @param details     allow-listed non-sensitive scalar details
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the exact enterprise capability manifest selected at Source compilation.
     *
     * @return immutable describe, snapshot, and retrieve capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes one exact DingTalk enterprise capability and request type.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific enterprise request
     * @param context    immutable non-secret invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return asynchronous typed enterprise outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "DingTalk enterprise capability must not be null");
        Assert.notNull(context, "DingTalk enterprise context must not be null");
        Assert.notNull(timeout, "DingTalk enterprise timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.equals(Realm.describe(DingTalkManifest.ID)) && request instanceof Realm.Describe) {
            return completed(
                    Outcome.succeeded(capability.responseType().cast(DingTalkManifest.enterpriseDescription())));
        }
        if (capability.equals(Realm.snapshot(DingTalkManifest.ID)) && request instanceof Realm.Snapshot snapshot) {
            return narrow(snapshot(snapshot, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.retrieve(DingTalkManifest.ID)) && request instanceof Realm.Retrieve retrieve) {
            return narrow(retrieve(retrieve, context, timeout), capability.responseType());
        }
        return mismatch();
    }

    /**
     * Clears and closes the Source-private upstream-token cache without closing shared services.
     */
    @Override
    public void close() {
        accessCache.clear();
        accessCache.close();
    }

    /**
     * Validates and starts one snapshot invocation with a single observation instant.
     *
     * @param request exact provider-neutral snapshot request
     * @param context immutable context used only for credential loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous normalized page outcome
     */
    private CompletionStage<Outcome<Realm.Page>> snapshot(
            final Realm.Snapshot request,
            final Context context,
            final Timeout timeout) {
        final CursorState state;
        try {
            requireKinds(request.kinds());
            state = request.cursor().isPresent() ? decode(request.cursor().getOrNull(), request.kinds())
                    : CursorState.initial(request.kinds());
        } catch (RuntimeException ignored) {
            return completed(rejected(ErrorCode._400, "DingTalk enterprise snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "DingTalk enterprise snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> snapshot(access, request, state, observedAt, timeout));
    }

    /**
     * Executes the finite snapshot phase selected by one validated cursor state.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      canonical recoverable pagination state
     * @param observedAt single observation instant shared by the page
     * @param timeout    shared end-to-end timeout
     * @return normalized page or safely classified failure
     */
    private Outcome<Realm.Page> snapshot(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        try {
            return switch (state.phase()) {
                case DEPARTMENTS -> departments(access, request, state, observedAt, timeout);
                case USERS -> users(access, request, state, observedAt, timeout);
                case ROLES -> roles(access, request, state, observedAt, timeout);
                case ROLE_MEMBERS -> roleMembers(access, request, state, observedAt, timeout);
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "DingTalk management API returned an invalid snapshot projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "DingTalk enterprise snapshot processing failed locally");
        }
    }

    /**
     * Reads one department snapshot page using deterministic root replay.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      department replay state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded department page
     */
    private Outcome<Realm.Page> departments(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<TreePosition> replayed = replay(access, state.position(), timeout);
        if (!(replayed instanceof Outcome.Succeeded<TreePosition> success)) {
            return propagate(replayed);
        }
        final TreePosition tree = success.value();
        final List<Realm.Resource> resources = new ArrayList<>();
        final List<Realm.Relation> relations = new ArrayList<>();
        int offset = tree.offset();
        String fingerprint = tree.fingerprint().getOrNull();
        while (true) {
            final Outcome<Optional<Department>> advanced = tree.walk().next();
            if (!(advanced instanceof Outcome.Succeeded<Optional<Department>> item)) {
                return propagate(advanced);
            }
            if (item.value().isEmpty()) {
                return completedPhase(resources, relations, state.kinds(), Phase.DEPARTMENTS);
            }
            final Department department = item.value().getOrNull();
            if (request.kinds().contains(Realm.Kind.ORGANIZATION)) {
                resources.add(departmentResource(department, observedAt));
                if (!ROOT_DEPARTMENT_ID.equals(department.parentId())) {
                    relations.add(parentRelation(department, observedAt));
                }
            }
            fingerprint = fingerprint(fingerprint, department);
            offset++;
            if (resources.size() >= request.limit() || relations.size() >= request.limit()) {
                return page(resources, relations, state.kinds(), Phase.DEPARTMENTS, Position.tree(offset, fingerprint));
            }
        }
    }

    /**
     * Reads one user page for the current replayed department.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      user replay state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded user and relation page
     */
    private Outcome<Realm.Page> users(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<TreePosition> replayed = replay(access, state.position(), timeout);
        if (!(replayed instanceof Outcome.Succeeded<TreePosition> success)) {
            return propagate(replayed);
        }
        final TreePosition tree = success.value();
        final Outcome<Optional<Department>> advanced = tree.walk().next();
        if (!(advanced instanceof Outcome.Succeeded<Optional<Department>> item)) {
            return propagate(advanced);
        }
        if (item.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state.kinds(), Phase.USERS);
        }
        final Department department = item.value().getOrNull();
        final Position position = state.position();
        if (position.parentId().isPresent() && !department.id().equals(position.parentId().getOrNull())) {
            return failed(ErrorCode._502, "DingTalk department replay parent no longer matches the snapshot cursor");
        }
        final Outcome<WirePage<User>> fetched = userPage(
                access,
                department.id(),
                request.limit(),
                position.next(),
                timeout);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> page)) {
            return propagate(fetched);
        }
        final List<Realm.Resource> resources = new ArrayList<>();
        final List<Realm.Relation> allRelations = new ArrayList<>();
        for (User user : page.value().items()) {
            if (!user.departmentIds().contains(department.id())) {
                return failed(ErrorCode._502, "DingTalk department user is not bound to the requested department");
            }
            final boolean canonical = department.id().equals(user.departmentIds().get(0));
            if (position.relationOffset() == 0 && canonical) {
                resources.add(userResource(user, observedAt));
            }
            allRelations.add(memberRelation(user.id(), Realm.Kind.ORGANIZATION, department.id(), observedAt));
            if (canonical && user.managerId().isPresent()) {
                allRelations.add(managerRelation(user.id(), user.managerId().getOrNull(), observedAt));
            }
        }
        if (position.relationOffset() > allRelations.size()) {
            return failed(ErrorCode._502, "DingTalk user relation offset exceeds the replayed page");
        }
        final int relationEnd = Math.min(allRelations.size(), position.relationOffset() + request.limit());
        final List<Realm.Relation> relations = new ArrayList<>(
                allRelations.subList(position.relationOffset(), relationEnd));
        if (relationEnd < allRelations.size()) {
            return page(
                    resources,
                    relations,
                    state.kinds(),
                    Phase.USERS,
                    Position.users(tree.offset(), tree.fingerprint(), department.id(), position.next(), relationEnd));
        }
        if (page.value().next().isPresent()) {
            return page(
                    resources,
                    relations,
                    state.kinds(),
                    Phase.USERS,
                    Position.users(tree.offset(), tree.fingerprint(), department.id(), page.value().next(), 0));
        }
        final String fingerprint = fingerprint(tree.fingerprint().getOrNull(), department);
        return page(
                resources,
                relations,
                state.kinds(),
                Phase.USERS,
                Position.users(tree.offset() + 1, Optional.of(fingerprint), null, Optional.empty(), 0));
    }

    /**
     * Reads one official DingTalk role-list page.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      role-list cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded role page
     */
    private Outcome<Realm.Page> roles(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<WirePage<Role>> fetched = rolePage(
                access,
                request.limit(),
                state.position().next(),
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> page)) {
            return propagate(fetched);
        }
        final List<Realm.Resource> resources = new ArrayList<>(page.value().items().size());
        for (Role role : page.value().items()) {
            resources.add(roleResource(role, observedAt));
        }
        if (page.value().next().isPresent()) {
            return page(resources, List.of(), state.kinds(), Phase.ROLES, Position.page(page.value().next()));
        }
        return completedPhase(resources, List.of(), state.kinds(), Phase.ROLES);
    }

    /**
     * Reads one role-member page while replaying role enumeration by stable identifier.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      role-member cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded role-member relation page
     */
    private Outcome<Realm.Page> roleMembers(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Position position = state.position();
        final String parent;
        if (position.parentId().isPresent()) {
            parent = position.parentId().getOrNull();
        } else {
            final Outcome<Optional<Role>> first = nextRole(access, null, timeout);
            if (!(first instanceof Outcome.Succeeded<Optional<Role>> success)) {
                return propagate(first);
            }
            if (success.value().isEmpty()) {
                return completedPhase(List.of(), List.of(), state.kinds(), Phase.ROLE_MEMBERS);
            }
            parent = success.value().getOrNull().id();
        }
        final Outcome<WirePage<RoleMember>> fetched = memberPage(
                access,
                parent,
                request.limit(),
                position.next(),
                timeout);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<RoleMember>> page)) {
            return propagate(fetched);
        }
        final List<Realm.Relation> allRelations = new ArrayList<>(page.value().items().size());
        for (RoleMember member : page.value().items()) {
            allRelations.add(roleMemberRelation(member.id(), parent, observedAt));
        }
        if (position.relationOffset() > allRelations.size()) {
            return failed(ErrorCode._502, "DingTalk role-member relation offset exceeds the replayed page");
        }
        final int relationEnd = Math.min(allRelations.size(), position.relationOffset() + request.limit());
        final List<Realm.Relation> relations = new ArrayList<>(
                allRelations.subList(position.relationOffset(), relationEnd));
        if (relationEnd < allRelations.size()) {
            return page(
                    List.of(),
                    relations,
                    state.kinds(),
                    Phase.ROLE_MEMBERS,
                    Position.members(parent, position.next(), relationEnd));
        }
        if (page.value().next().isPresent()) {
            return page(
                    List.of(),
                    relations,
                    state.kinds(),
                    Phase.ROLE_MEMBERS,
                    Position.members(parent, page.value().next(), 0));
        }
        final Outcome<Optional<Role>> next = nextRole(access, parent, timeout);
        if (!(next instanceof Outcome.Succeeded<Optional<Role>> success)) {
            return propagate(next);
        }
        if (success.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Page(List.of(), relations, Optional.empty()));
        }
        return page(
                List.of(),
                relations,
                state.kinds(),
                Phase.ROLE_MEMBERS,
                Position.members(success.value().getOrNull().id(), Optional.empty(), 0));
    }

    /**
     * Validates and starts one direct retrieval with a single observation instant.
     *
     * @param request exact provider-neutral retrieval request
     * @param context immutable context used only for credential loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "DingTalk enterprise retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "DingTalk enterprise retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> retrieve(access, request.key(), observedAt, timeout));
    }

    /**
     * Dispatches one retrieval to the fixed DingTalk management resource path.
     *
     * @param access     valid upstream application access
     * @param key        requested stable resource key
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved resource, explicit absence, or safely classified failure
     */
    private Outcome<Realm.Retrieved> retrieve(
            final Access access,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        try {
            return switch (key.kind()) {
                case USER -> retrieveUser(access, key.externalId(), observedAt, timeout);
                case ORGANIZATION -> retrieveDepartment(access, key.externalId(), observedAt, timeout);
                case ROLE -> retrieveRole(access, key.externalId(), observedAt, timeout);
                default -> rejected(ErrorCode._400, "DingTalk enterprise retrieve kind is unsupported");
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "DingTalk management API returned an invalid retrieval projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "DingTalk enterprise retrieval processing failed locally");
        }
    }

    /**
     * Retrieves one user through the official stable-ID endpoint.
     *
     * @param access     valid upstream application access
     * @param userId     stable DingTalk user identifier
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved user or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveUser(
            final Access access,
            final String userId,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<User>> fetched = userDetail(
                access,
                target(Builder.ENTERPRISE_USER),
                userId,
                "retrieve",
                true,
                timeout);
        if (!(fetched instanceof Outcome.Succeeded<Optional<User>> success)) {
            return propagate(fetched);
        }
        if (success.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final User user = success.value().getOrNull();
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(userResource(user, observedAt))));
    }

    /**
     * Scans the deterministic department tree for one stable department identifier.
     *
     * @param access       valid upstream application access
     * @param departmentId stable DingTalk department identifier
     * @param observedAt   shared invocation observation instant
     * @param timeout      shared end-to-end timeout
     * @return retrieved department or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveDepartment(
            final Access access,
            final String departmentId,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<DepartmentWalk> started = DepartmentWalk.start(this, access, timeout, "retrieve");
        if (!(started instanceof Outcome.Succeeded<DepartmentWalk> success)) {
            return propagate(started);
        }
        while (true) {
            final Outcome<Optional<Department>> advanced = success.value().next();
            if (!(advanced instanceof Outcome.Succeeded<Optional<Department>> item)) {
                return propagate(advanced);
            }
            if (item.value().isEmpty()) {
                return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
            }
            final Department department = item.value().getOrNull();
            if (departmentId.equals(department.id())) {
                return Outcome.succeeded(new Realm.Retrieved(Optional.of(departmentResource(department, observedAt))));
            }
        }
    }

    /**
     * Scans official role-list pages for one stable role identifier.
     *
     * @param access     valid upstream application access
     * @param roleId     stable DingTalk role identifier
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved role or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveRole(
            final Access access,
            final String roleId,
            final Instant observedAt,
            final Timeout timeout) {
        Optional<String> next = Optional.empty();
        do {
            final Outcome<WirePage<Role>> fetched = rolePage(access, Normal._50, next, timeout, "retrieve");
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> page)) {
                return propagate(fetched);
            }
            for (Role role : page.value().items()) {
                if (roleId.equals(role.id())) {
                    return Outcome.succeeded(new Realm.Retrieved(Optional.of(roleResource(role, observedAt))));
                }
            }
            next = page.value().next();
        } while (next.isPresent());
        return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
    }

    /**
     * Acquires a cached or freshly exchanged application token and retries one 401 result exactly once.
     *
     * @param context   immutable context used by the project Secret Loader
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous management API operation
     * @param <T>       successful operation value type
     * @return asynchronous authenticated outcome
     */
    private <T> CompletionStage<Outcome<T>> authenticated(
            final Context context,
            final Timeout timeout,
            final Function<Access, Outcome<T>> operation) {
        return access(context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<Access> success -> execute(success.value(), timeout, operation)
                    .thenCompose(first -> {
                        if (!unauthorized(first)) {
                            return completed(first);
                        }
                        return accessCache.delete(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY)
                                .handle(
                                        (ignored, cause) -> cause == null ? Outcome.succeeded(Boolean.TRUE)
                                                : DingTalkRealmAdapter.<Boolean>failed(
                                                        ErrorCode._500,
                                                        "DingTalk upstream-token cache deletion failed"))
                                .thenCompose(deleted -> {
                                    if (!(deleted instanceof Outcome.Succeeded<Boolean>)) {
                                        return completed(propagate(deleted));
                                    }
                                    return exchange(context, timeout).thenCompose(refreshed -> switch (refreshed) {
                                        case Outcome.Succeeded<Access> retry -> execute(
                                                retry.value(),
                                                timeout,
                                                operation);
                                        case Outcome.Rejected<Access> rejected -> completed(
                                                Outcome.rejected(rejected.failure()));
                                        case Outcome.Failed<Access> failed -> completed(
                                                Outcome.failed(failed.failure()));
                                        default -> completed(
                                                failed(
                                                        ErrorCode._500,
                                                        "DingTalk token refresh returned an unsupported outcome"));
                                    });
                                });
                    });
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "DingTalk token lookup returned an unsupported outcome"));
        });
    }

    /**
     * Executes one authenticated operation on the Source executor and closes ordinary exceptions into Outcomes.
     *
     * @param access    valid upstream application access
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous operation
     * @param <T>       successful value type
     * @return asynchronous safely closed operation outcome
     */
    private <T> CompletionStage<Outcome<T>> execute(
            final Access access,
            final Timeout timeout,
            final Function<Access, Outcome<T>> operation) {
        return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "DingTalk enterprise operation has no remaining timeout");
            }
            try {
                return Assert.notNull(operation.apply(access), "DingTalk enterprise operation returned no outcome");
            } catch (TimeoutException ignored) {
                return failed(ErrorCode._408, "DingTalk enterprise operation timed out");
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._503, "DingTalk management API transport is unavailable");
            }
        }, services.executor());
    }

    /**
     * Reads the Source-private token cache before exchanging a new token.
     *
     * @param context immutable context used by the project Secret Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous cached or newly exchanged access outcome
     */
    private CompletionStage<Outcome<Access>> access(final Context context, final Timeout timeout) {
        return accessCache.get(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY)
                .<CompletionStage<Outcome<Access>>>handle((cached, cause) -> {
                    if (cause != null) {
                        return completed(failed(ErrorCode._500, "DingTalk upstream-token cache lookup failed"));
                    }
                    return cached == null ? exchange(context, timeout) : completed(Outcome.succeeded(cached));
                }).thenCompose(Function.identity());
    }

    /**
     * Loads the external App Secret, exchanges it for a application token, and conditionally caches the token.
     *
     * @param context immutable context supplied to the project Secret Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous application-token outcome
     */
    private CompletionStage<Outcome<Access>> exchange(final Context context, final Timeout timeout) {
        final CompletionStage<Outcome<SecretLoader.Record>> loaded;
        try {
            loaded = services.secretLoader()
                    .load(new SecretLoader.Request(services.registration(), options.credential()), context, timeout);
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "DingTalk Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "DingTalk Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "DingTalk Secret Loader stage failed"))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> exchange(success.value(), timeout);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(ErrorCode._500, "DingTalk Secret Loader returned an unsupported outcome"));
                });
    }

    /**
     * Parses one loaded Secret record and binds its lease to exactly one asynchronous terminal close point.
     *
     * @param loaded  project-loaded Secret record
     * @param timeout shared end-to-end timeout
     * @return asynchronous application-token outcome with deterministic Secret erasure
     */
    private CompletionStage<Outcome<Access>> exchange(final SecretLoader.Record loaded, final Timeout timeout) {
        final SecretLease raw = loaded == null ? null : loaded.lease();
        final SecretLease secret;
        try {
            secret = services.secretParser().parse(services.registration(), options.credential(), loaded);
        } catch (RuntimeException ignored) {
            if (raw != null) {
                raw.close();
            }
            return completed(failed(ErrorCode._500, "DingTalk loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<Access>>supplyAsync(() -> {
                try {
                    return token(secret, timeout);
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "DingTalk application-token exchange timed out");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "DingTalk application-token transport is unavailable");
                }
            }, services.executor()).whenComplete((ignored, cause) -> secret.close()).thenCompose(this::cache);
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "DingTalk application-token task could not be scheduled"));
        }
    }

    /**
     * Stores one cacheable upstream token with the mandatory early-expiration skew.
     *
     * @param outcome freshly exchanged access outcome
     * @return original successful access after cache creation, or a safe cache failure
     */
    private CompletionStage<Outcome<Access>> cache(final Outcome<Access> outcome) {
        if (!(outcome instanceof Outcome.Succeeded<Access> success) || success.value().expiresAtMillis() == 0L) {
            return completed(outcome);
        }
        final long now = FabricX.clock(services.fabric()).millis();
        final long lifetime = success.value().expiresAtMillis() - now;
        if (lifetime <= 0L) {
            return completed(Outcome.succeeded(new Access(success.value().token(), 0L)));
        }
        final long skew = Math.min(
                Builder.UPSTREAM_ACCESS_TOKEN_MAXIMUM_SKEW.toMillis(),
                lifetime / Builder.UPSTREAM_ACCESS_TOKEN_SKEW_DIVISOR);
        final long ttl = lifetime - skew;
        if (ttl <= 0L) {
            return completed(Outcome.succeeded(new Access(success.value().token(), 0L)));
        }
        return accessCache.create(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY, success.value(), ttl).handle(
                (ignored, cause) -> cause == null ? outcome
                        : failed(ErrorCode._500, "DingTalk upstream-token cache creation failed"));
    }

    /**
     * Exchanges one AppKey and leased AppSecret for a DingTalk application access token.
     *
     * @param secret  still-open App Secret lease
     * @param timeout shared end-to-end timeout
     * @return upstream access result or safely classified failure
     */
    private Outcome<Access> token(final SecretLease secret, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "DingTalk application-token exchange has no remaining timeout");
        }
        byte[] body = null;
        try {
            final Map<String, JsonValue> fields = new LinkedHashMap<>();
            fields.put("appKey", new JsonValue.StringValue(options.clientId()));
            fields.put("appSecret", new JsonValue.StringValue(new String(secret.material())));
            try {
                body = services.jsonProvider().writeValue(new JsonValue.ObjectValue(fields));
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._500, "DingTalk application-token request could not be encoded");
            }
            final Response response;
            try {
                response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout)
                        .url(targets.token().getOrNull().url().toString()).method(Http.Method.POST)
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_JSON_TYPE).execute();
            } catch (TimeoutException cause) {
                return failed(ErrorCode._408, "DingTalk application-token exchange timed out");
            } catch (RuntimeException cause) {
                return failed(ErrorCode._503, "DingTalk application-token endpoint is unavailable");
            }
            try (response) {
                if (!response.successful()) {
                    return tokenHttpFailure(response);
                }
                final JsonValue.ObjectValue envelope = object(response);
                final String token = requiredString(envelope, "accessToken");
                final long seconds = requiredPositiveLong(envelope, "expireIn");
                final long now = FabricX.clock(services.fabric()).millis();
                long expiresAt = 0L;
                try {
                    expiresAt = Math.addExact(now, Duration.ofSeconds(seconds).toMillis());
                } catch (ArithmeticException ignored) {
                    expiresAt = 0L;
                }
                if (expiresAt <= now) {
                    expiresAt = 0L;
                }
                return Outcome.succeeded(new Access(token, expiresAt));
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._502, "DingTalk application-token endpoint returned an invalid response");
            }
        } finally {
            clear(body);
        }
    }

    /**
     * Maps one non-successful token HTTP response without retaining its body or headers.
     *
     * @param response owned non-successful token response
     * @return credential rejection, limit failure, or upstream failure
     */
    private Outcome<Access> tokenHttpFailure(final Response response) {
        final int status = response.code();
        final Map<String, JsonValue> details = details("token", status, null, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.UNAUTHORIZED
                || status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._401, "DingTalk rejected the configured App credentials", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "DingTalk application-token endpoint is rate limited", details);
        }
        return failed(ErrorCode._502, "DingTalk application-token endpoint returned an upstream error", details);
    }

    /**
     * Executes one bounded DingTalk JSON POST for a paginated, token-independent, or retrieval response.
     *
     * @param endpoint      exact manifest-derived endpoint
     * @param access        valid upstream application access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether the explicit DingTalk user-not-found business code represents absence
     * @param fields        exact non-secret JSON request fields
     * @return decoded success envelope, {@code null} for explicit absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> post(
            final Endpoint endpoint,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound,
            final Map<String, JsonValue> fields) {
        return post(endpoint, access, timeout, operation, allowNotFound, fields, this::object);
    }

    /**
     * Executes the frozen D-02 unpaged DingTalk JSON POST without a local byte or record limit.
     *
     * @param endpoint  exact manifest-derived direct-child department endpoint
     * @param access    valid upstream application access
     * @param timeout   shared end-to-end timeout
     * @param operation safe enterprise operation label
     * @param fields    exact non-secret JSON request fields
     * @return completely decoded success envelope or classified failure
     */
    private Outcome<JsonValue.ObjectValue> postUnpaged(
            final Endpoint endpoint,
            final Access access,
            final Timeout timeout,
            final String operation,
            final Map<String, JsonValue> fields) {
        return post(endpoint, access, timeout, operation, false, fields, this::unpagedObject);
    }

    /**
     * Executes one DingTalk JSON POST and validates its legacy management envelope with the selected response reader.
     *
     * @param endpoint      exact manifest-derived endpoint
     * @param access        valid upstream application access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether the explicit DingTalk user-not-found business code represents absence
     * @param fields        exact non-secret JSON request fields
     * @param reader        bounded or explicit unpaged response reader
     * @return decoded success envelope, {@code null} for explicit absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> post(
            final Endpoint endpoint,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound,
            final Map<String, JsonValue> fields,
            final Function<Response, JsonValue.ObjectValue> reader) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "DingTalk management API request has no remaining timeout");
        }
        byte[] body = null;
        try {
            body = services.jsonProvider().writeValue(new JsonValue.ObjectValue(fields));
            final Response response;
            try {
                response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout).url(endpoint.url().toString())
                        .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .query("access_token", access.token()).body(body, MediaType.APPLICATION_JSON_TYPE).execute();
            } catch (TimeoutException ignored) {
                return failed(ErrorCode._408, "DingTalk management API request timed out");
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._503, "DingTalk management API endpoint is unavailable");
            }
            try (response) {
                if (!response.successful()) {
                    return httpFailure(response, operation);
                }
                final JsonValue.ObjectValue envelope = reader.apply(response);
                final long code = requiredLong(envelope, "errcode");
                if (code == Normal._0) {
                    return Outcome.succeeded(envelope);
                }
                if (allowNotFound && code == USER_NOT_FOUND_CODE) {
                    return Outcome.succeeded(null);
                }
                if (code == INVALID_ACCESS_TOKEN_CODE || code == EXPIRED_ACCESS_TOKEN_CODE) {
                    return rejected(
                            ErrorCode._401,
                            "DingTalk management API rejected the application token",
                            details(operation, response.code(), code, response.headers()));
                }
                if (code == PERMISSION_DENIED_CODE) {
                    return rejected(
                            ErrorCode._403,
                            "DingTalk management API permission is insufficient",
                            details(operation, response.code(), code, response.headers()));
                }
                return failed(
                        ErrorCode._502,
                        "DingTalk management API returned an unknown business error",
                        details(operation, response.code(), code, response.headers()));
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._502, "DingTalk management API returned an invalid response");
            }
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "DingTalk management request could not be encoded");
        } finally {
            clear(body);
        }
    }

    /**
     * Maps one non-successful management API HTTP response using only allow-listed scalar details.
     *
     * @param response  owned non-successful HTTP response
     * @param operation safe enterprise operation label
     * @param <T>       expected success type
     * @return classified rejection or operational failure
     */
    private <T> Outcome<T> httpFailure(final Response response, final String operation) {
        final int status = response.code();
        final Map<String, JsonValue> details = details(operation, status, null, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "DingTalk management API rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "DingTalk management API rejected the application token", details);
        }
        if (status == Http.Status.FORBIDDEN || status == Http.Status.NOT_FOUND) {
            return rejected(ErrorCode._403, "DingTalk management API permission is insufficient", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "DingTalk management API is rate limited", details);
        }
        return failed(ErrorCode._502, "DingTalk management API returned an upstream error", details);
    }

    /**
     * Reads and validates one complete unpaged direct-child department collection.
     *
     * @param access    valid upstream application access
     * @param parentId  stable parent department identifier
     * @param timeout   shared end-to-end timeout
     * @param operation safe enterprise operation label
     * @return stable-ID-sorted direct children or classified failure
     */
    private Outcome<List<Department>> departmentChildren(
            final Access access,
            final String parentId,
            final Timeout timeout,
            final String operation) {
        final Endpoint endpoint = target(Builder.ENTERPRISE_ORGANIZATIONS);
        final Map<String, JsonValue> fields = new LinkedHashMap<>();
        fields.put("dept_id", identifierNumber(parentId, "DingTalk parent department identifier"));
        fields.put("language", new JsonValue.StringValue("zh_CN"));
        final Outcome<JsonValue.ObjectValue> fetched = postUnpaged(endpoint, access, timeout, operation, fields);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final Map<String, Department> departments = new LinkedHashMap<>();
        for (JsonValue value : requiredArray(success.value(), "result")) {
            final Department department = department(requiredObject(value, "department item"));
            if (!parentId.equals(department.parentId())) {
                return failed(ErrorCode._502, "DingTalk department child has a different parent identifier");
            }
            final Department previous = departments.putIfAbsent(department.id(), department);
            if (previous != null && !previous.equals(department)) {
                return failed(ErrorCode._502, "DingTalk department response contains a conflicting identifier");
            }
        }
        final List<Department> result = new ArrayList<>(departments.values());
        result.sort(Comparator.comparing(Department::id));
        return Outcome.succeeded(List.copyOf(result));
    }

    /**
     * Reads one official user page for a direct department.
     *
     * @param access       valid upstream application access
     * @param departmentId stable direct department identifier
     * @param limit        requested framework page limit
     * @param next         official input numeric offset
     * @param timeout      shared end-to-end timeout
     * @return normalized stable-ID-sorted user page
     */
    private Outcome<WirePage<User>> userPage(
            final Access access,
            final String departmentId,
            final int limit,
            final Optional<String> next,
            final Timeout timeout) {
        final Endpoint endpoint = target(Builder.ENTERPRISE_ORGANIZATION_USERS);
        final long offset = continuation(next, "DingTalk department-user cursor");
        final Map<String, JsonValue> fields = new LinkedHashMap<>();
        fields.put("dept_id", identifierNumber(departmentId, "DingTalk department identifier"));
        fields.put("cursor", number(offset));
        fields.put("size", number(Math.min(limit, Normal._50)));
        final Outcome<JsonValue.ObjectValue> fetched = post(endpoint, access, timeout, "snapshot", false, fields);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final JsonValue.ObjectValue data = requiredObject(success.value(), "result");
        final LinkedHashSet<String> identifiers = new LinkedHashSet<>();
        for (JsonValue value : requiredArray(data, "userid_list")) {
            if (!(value instanceof JsonValue.StringValue text)) {
                return failed(ErrorCode._502, "DingTalk department-user identifier has an invalid JSON type");
            }
            identifiers.add(requireText(text.value(), "DingTalk department-user identifier"));
        }
        final List<String> ordered = new ArrayList<>(identifiers);
        ordered.sort(String::compareTo);
        final Map<String, User> users = new LinkedHashMap<>();
        for (String identifier : ordered) {
            final Outcome<Optional<User>> detail = userDetail(
                    access,
                    target(Builder.ENTERPRISE_USERS),
                    identifier,
                    "snapshot",
                    false,
                    timeout);
            if (!(detail instanceof Outcome.Succeeded<Optional<User>> userOutcome)) {
                return propagate(detail);
            }
            if (userOutcome.value().isEmpty()) {
                return failed(ErrorCode._502, "DingTalk department user disappeared during snapshot");
            }
            final User user = userOutcome.value().getOrNull();
            final User previous = users.putIfAbsent(user.id(), user);
            if (previous != null && !previous.equals(user)) {
                return failed(ErrorCode._502, "DingTalk user page contains a conflicting identifier");
            }
        }
        final List<User> items = new ArrayList<>(users.values());
        items.sort(Comparator.comparing(User::id));
        final Optional<String> pageToken = offset(data, offset, items.size());
        if (pageToken.isPresent() && pageToken.equals(next)) {
            return failed(ErrorCode._502, "DingTalk user pagination did not advance");
        }
        return Outcome.succeeded(new WirePage<>(items, pageToken));
    }

    /**
     * Reads one complete user projection from DingTalk's stable user endpoint.
     *
     * @param access        valid upstream application access
     * @param endpoint      manifest-derived user endpoint selected for the current operation
     * @param userId        stable DingTalk user identifier
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether the explicit user-not-found code represents absence
     * @param timeout       shared end-to-end timeout
     * @return normalized user, explicit absence, or classified failure
     */
    private Outcome<Optional<User>> userDetail(
            final Access access,
            final Endpoint endpoint,
            final String userId,
            final String operation,
            final boolean allowNotFound,
            final Timeout timeout) {
        final Map<String, JsonValue> fields = new LinkedHashMap<>();
        fields.put("userid", new JsonValue.StringValue(requireText(userId, "DingTalk user identifier")));
        fields.put("language", new JsonValue.StringValue("zh_CN"));
        final Outcome<JsonValue.ObjectValue> fetched = post(
                endpoint,
                access,
                timeout,
                operation,
                allowNotFound,
                fields);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(Optional.empty());
        }
        final User user = user(requiredObject(success.value(), "result"));
        if (!userId.equals(user.id())) {
            return failed(ErrorCode._502, "DingTalk user endpoint returned a different identifier");
        }
        return Outcome.succeeded(Optional.of(user));
    }

    /**
     * Reads one official DingTalk role-list page.
     *
     * @param access    valid upstream application access
     * @param limit     requested framework page limit
     * @param next      official input numeric offset
     * @param timeout   shared end-to-end timeout
     * @param operation safe enterprise operation label
     * @return normalized stable-ID-sorted role page
     */
    private Outcome<WirePage<Role>> rolePage(
            final Access access,
            final int limit,
            final Optional<String> next,
            final Timeout timeout,
            final String operation) {
        final Endpoint endpoint = target(Builder.ENTERPRISE_ROLES);
        final long current = continuation(next, "DingTalk role cursor");
        final Map<String, JsonValue> fields = new LinkedHashMap<>();
        fields.put("offset", number(current));
        fields.put("size", number(Math.min(limit, Normal._50)));
        final Outcome<JsonValue.ObjectValue> fetched = post(endpoint, access, timeout, operation, false, fields);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final JsonValue.ObjectValue data = requiredObject(success.value(), "result");
        final Map<String, Role> roles = new LinkedHashMap<>();
        final List<JsonValue> values = requiredArray(data, "list");
        for (JsonValue value : values) {
            final Role role = role(requiredObject(value, "role item"));
            final Role previous = roles.putIfAbsent(role.id(), role);
            if (previous != null && !previous.equals(role)) {
                return failed(ErrorCode._502, "DingTalk role page contains a conflicting identifier");
            }
        }
        final List<Role> items = new ArrayList<>(roles.values());
        items.sort(Comparator.comparing(Role::id));
        final Optional<String> pageToken = offset(data, current, values.size());
        if (pageToken.isPresent() && pageToken.equals(next)) {
            return failed(ErrorCode._502, "DingTalk role pagination did not advance");
        }
        return Outcome.succeeded(new WirePage<>(items, pageToken));
    }

    /**
     * Reads one official DingTalk role-member page.
     *
     * @param access  valid upstream application access
     * @param roleId  stable parent role identifier
     * @param limit   requested framework page limit
     * @param next    official input numeric offset
     * @param timeout shared end-to-end timeout
     * @return normalized stable-ID-sorted member page
     */
    private Outcome<WirePage<RoleMember>> memberPage(
            final Access access,
            final String roleId,
            final int limit,
            final Optional<String> next,
            final Timeout timeout) {
        final Endpoint endpoint = target(Builder.ENTERPRISE_ROLE_MEMBERS);
        final long current = continuation(next, "DingTalk role-member cursor");
        final Map<String, JsonValue> fields = new LinkedHashMap<>();
        fields.put("role_id", identifierNumber(roleId, "DingTalk role identifier"));
        fields.put("offset", number(current));
        fields.put("size", number(Math.min(limit, Normal._50)));
        final Outcome<JsonValue.ObjectValue> fetched = post(endpoint, access, timeout, "snapshot", false, fields);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final JsonValue.ObjectValue data = requiredObject(success.value(), "result");
        final Map<String, RoleMember> members = new LinkedHashMap<>();
        final List<JsonValue> values = requiredArray(data, "list");
        for (JsonValue value : values) {
            final RoleMember member = member(requiredObject(value, "member item"));
            final RoleMember previous = members.putIfAbsent(member.id(), member);
            if (previous != null && !previous.equals(member)) {
                return failed(ErrorCode._502, "DingTalk role-member page contains a conflicting identifier");
            }
        }
        final List<RoleMember> items = new ArrayList<>(members.values());
        items.sort(Comparator.comparing(RoleMember::id));
        final Optional<String> pageToken = offset(data, current, values.size());
        if (pageToken.isPresent() && pageToken.equals(next)) {
            return failed(ErrorCode._502, "DingTalk role-member pagination did not advance");
        }
        return Outcome.succeeded(new WirePage<>(items, pageToken));
    }

    /**
     * Finds the lexicographically next stable role without retaining the full application-visible role set.
     *
     * @param access  valid upstream application access
     * @param after   current role identifier, or {@code null} for the first role
     * @param timeout shared end-to-end timeout
     * @return next stable role, natural completion, or replay failure when the current role disappeared
     */
    private Outcome<Optional<Role>> nextRole(final Access access, final String after, final Timeout timeout) {
        Optional<String> next = Optional.empty();
        Role candidate = null;
        boolean found = after == null;
        do {
            final Outcome<WirePage<Role>> fetched = rolePage(access, Normal._50, next, timeout, "snapshot");
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> page)) {
                return propagate(fetched);
            }
            for (Role role : page.value().items()) {
                if (after != null && after.equals(role.id())) {
                    found = true;
                }
                if ((after == null || role.id().compareTo(after) > 0)
                        && (candidate == null || role.id().compareTo(candidate.id()) < 0)) {
                    candidate = role;
                }
            }
            next = page.value().next();
        } while (next.isPresent());
        if (!found) {
            return failed(ErrorCode._502, "DingTalk role replay parent no longer exists");
        }
        return Outcome.succeeded(Optional.ofNullable(candidate));
    }

    /**
     * Replays the department prefix recorded by one tree-dependent cursor.
     *
     * @param access   valid upstream application access
     * @param position validated tree cursor position
     * @param timeout  shared end-to-end timeout
     * @return department walk positioned at the next parent and verified prefix fingerprint
     */
    private Outcome<TreePosition> replay(final Access access, final Position position, final Timeout timeout) {
        final Outcome<DepartmentWalk> started = DepartmentWalk.start(this, access, timeout, "snapshot");
        if (!(started instanceof Outcome.Succeeded<DepartmentWalk> success)) {
            return propagate(started);
        }
        String fingerprint = null;
        for (int index = 0; index < position.offset(); index++) {
            final Outcome<Optional<Department>> advanced = success.value().next();
            if (!(advanced instanceof Outcome.Succeeded<Optional<Department>> item)) {
                return propagate(advanced);
            }
            if (item.value().isEmpty()) {
                return failed(ErrorCode._502, "DingTalk department replay offset exceeds the current tree");
            }
            fingerprint = fingerprint(fingerprint, item.value().getOrNull());
        }
        if (position.offset() == 0) {
            if (position.fingerprint().isPresent()) {
                return failed(ErrorCode._502, "DingTalk department replay has an unexpected initial fingerprint");
            }
        } else if (!position.fingerprint().isPresent() || !fingerprint.equals(position.fingerprint().getOrNull())) {
            return failed(ErrorCode._502, "DingTalk department replay projection changed between pages");
        }
        return Outcome
                .succeeded(new TreePosition(success.value(), position.offset(), Optional.ofNullable(fingerprint)));
    }

    /**
     * Computes the chained SHA-256 fingerprint of one deterministic department prefix.
     *
     * @param previous   previous prefix fingerprint, or {@code null} before the first department
     * @param department next minimal department projection
     * @return 64-character lowercase prefix fingerprint
     */
    private String fingerprint(final String previous, final Department department) {
        final Map<String, JsonValue> projection = new LinkedHashMap<>();
        projection.put("dept_id", new JsonValue.StringValue(department.id()));
        projection.put("name", new JsonValue.StringValue(department.name()));
        projection.put("parent_id", new JsonValue.StringValue(department.parentId()));
        final byte[] encoded = services.jsonProvider().writeValue(new JsonValue.ObjectValue(projection));
        return Builder.sha256Hex((previous == null ? Normal.EMPTY : previous) + new String(encoded, Charset.UTF_8));
    }

    /**
     * Creates one page with an encoded continuation cursor.
     *
     * @param resources normalized resources in output order
     * @param relations normalized relations in output order
     * @param kinds     exact requested kind set
     * @param phase     finite phase retained by the continuation
     * @param position  phase-specific recoverable position
     * @return successful normalized page outcome
     */
    private Outcome<Realm.Page> page(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final List<Realm.Kind> kinds,
            final Phase phase,
            final Position position) {
        final Realm.Cursor cursor = encode(new CursorState(phase, kinds, position));
        return Outcome.succeeded(new Realm.Page(resources, relations, Optional.of(cursor)));
    }

    /**
     * Completes one finite phase and selects the next phase required by the request.
     *
     * @param resources resources produced by the completed phase
     * @param relations relations produced by the completed phase
     * @param kinds     exact requested kind set
     * @param phase     completed finite phase
     * @return successful page with the next phase cursor or natural completion
     */
    private Outcome<Realm.Page> completedPhase(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final List<Realm.Kind> kinds,
            final Phase phase) {
        final Phase next = nextPhase(phase, kinds);
        final Optional<Realm.Cursor> cursor = next == null ? Optional.empty()
                : Optional.of(encode(new CursorState(next, kinds, Position.initial(next))));
        return Outcome.succeeded(new Realm.Page(resources, relations, cursor));
    }

    /**
     * Resolves one required manifest-owned management target.
     *
     * @param name fixed root Builder management key
     * @return exact resolved HTTPS endpoint
     * @throws ValidateException if the selected manifest omits the required target
     */
    private Endpoint target(final String name) {
        final Endpoint endpoint = targets.management().get(name);
        if (endpoint == null) {
            throw new ValidateException("DingTalk enterprise manifest omits a required management target");
        }
        return endpoint;
    }

    /**
     * Encodes one canonical six-field snapshot cursor envelope.
     *
     * @param state validated finite pagination state
     * @return opaque unpadded Base64 URL-safe cursor
     */
    private Realm.Cursor encode(final CursorState state) {
        final Map<String, JsonValue> envelope = new LinkedHashMap<>();
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(DingTalkManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(DingTalkManifest.ENTERPRISE.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code()));
        final List<JsonValue> kinds = new ArrayList<>(state.kinds().size());
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        envelope.put(Builder.CURSOR_POSITION_FIELD, position(state.phase(), state.position()));
        final byte[] json = services.jsonProvider().writeValue(new JsonValue.ObjectValue(envelope));
        return new Realm.Cursor(Base64.encodeUrlSafe(json));
    }

    /**
     * Decodes, validates, and canonicalizes one DingTalk snapshot cursor.
     *
     * @param cursor opaque caller-supplied cursor
     * @param kinds  exact kinds requested by the current snapshot call
     * @return validated finite pagination state
     * @throws ValidateException if encoding, fields, types, context, position, or canonical form is invalid
     */
    private CursorState decode(final Realm.Cursor cursor, final Set<Realm.Kind> kinds) {
        try {
            final byte[] decoded = Base64.decode(cursor.value());
            final JsonValue value = services.jsonProvider()
                    .readValue(decoded, Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true);
            final JsonValue.ObjectValue envelope = requiredObject(value, "cursor envelope");
            exactMembers(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD),
                    "cursor envelope");
            if (!DingTalkManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !DingTalkManifest.ENTERPRISE.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredLong(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("DingTalk cursor does not belong to this snapshot operation");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("DingTalk cursor kinds do not match the snapshot request");
            }
            final Position position = position(phase, requiredObject(envelope, Builder.CURSOR_POSITION_FIELD));
            final CursorState state = new CursorState(phase, decodedKinds, position);
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("DingTalk cursor is not in canonical form");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("DingTalk cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded JSON response object with duplicate-field rejection.
     *
     * @param response owned successful HTTP response
     * @return decoded top-level JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(
                services.jsonProvider().readValue(
                        response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES),
                        Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH,
                        true),
                "DingTalk response");
    }

    /**
     * Decodes one complete official unpaged response without applying a local materialization threshold.
     *
     * @param response owned successful D-02 response
     * @return decoded top-level JSON object
     */
    private JsonValue.ObjectValue unpagedObject(final Response response) {
        final Buffer buffer = new Buffer();
        Payload.copyTo(response.body().payload(), buffer);
        return requiredObject(
                services.jsonProvider().readValue(buffer.readByteArray(), Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true),
                "DingTalk unpaged response");
    }

    /**
     * Defines the complete finite DingTalk snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Replays the direct-child department tree and emits organization resources and parent relations.
         */
        DEPARTMENTS(1),

        /**
         * Replays departments and emits canonical users, department membership, and manager relations.
         */
        USERS(2),

        /**
         * Reads paginated role resources.
         */
        ROLES(3),

        /**
         * Replays roles and reads paginated role-member relations.
         */
        ROLE_MEMBERS(4);

        /**
         * Stable external phase code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates one finite snapshot phase.
         *
         * @param code stable external phase code
         */
        Phase(final int code) {
            this.code = code;
        }

        /**
         * Resolves one stable phase code without using ordinal values.
         *
         * @param code stable decoded phase code
         * @return exact finite snapshot phase
         */
        private static Phase from(final int code) {
            for (Phase phase : values()) {
                if (phase.code == code) {
                    return phase;
                }
            }
            throw new ValidateException("DingTalk cursor contains an unknown phase code");
        }

        /**
         * Returns the stable external phase code.
         *
         * @return stable phase code
         */
        private int code() {
            return code;
        }
    }

    /**
     * Carries one canonical finite snapshot cursor state.
     *
     * @param phase    exact finite phase
     * @param kinds    exact requested kinds in stable code order
     * @param position phase-specific recoverable position
     * @author Kimi Liu
     */
    private record CursorState(Phase phase, List<Realm.Kind> kinds, Position position) {

        /**
         * Validates and freezes one canonical cursor state.
         *
         * @param phase    exact finite phase
         * @param kinds    exact requested kinds
         * @param position phase-specific recoverable position
         */
        private CursorState {
            phase = Assert.notNull(phase, "DingTalk cursor phase must not be null");
            Assert.notNull(kinds, "DingTalk cursor kinds must not be null");
            final List<Realm.Kind> copy = new ArrayList<>(kinds.size());
            int previous = 0;
            for (Realm.Kind kind : kinds) {
                final Realm.Kind checked = Assert.notNull(kind, "DingTalk cursor kind must not be null");
                if (!SUPPORTED_KINDS.contains(checked) || checked.code() <= previous) {
                    throw new ValidateException("DingTalk cursor kinds must be supported and in stable code order");
                }
                previous = checked.code();
                copy.add(checked);
            }
            if (copy.isEmpty()) {
                throw new ValidateException("DingTalk cursor kinds must not be empty");
            }
            kinds = List.copyOf(copy);
            if ((phase == Phase.DEPARTMENTS && !kinds.contains(Realm.Kind.ORGANIZATION))
                    || (phase == Phase.USERS && !kinds.contains(Realm.Kind.USER))
                    || ((phase == Phase.ROLES || phase == Phase.ROLE_MEMBERS) && !kinds.contains(Realm.Kind.ROLE))) {
                throw new ValidateException("DingTalk cursor phase is not enabled by its requested kinds");
            }
            position = Assert.notNull(position, "DingTalk cursor position must not be null");
            position.validate(phase);
        }

        /**
         * Creates the first finite phase for one validated snapshot kind set.
         *
         * @param kinds exact requested kinds in stable code order
         * @return canonical initial cursor state
         */
        private static CursorState initial(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            final Phase phase = ordered.contains(Realm.Kind.ORGANIZATION) ? Phase.DEPARTMENTS
                    : ordered.contains(Realm.Kind.USER) ? Phase.USERS : Phase.ROLES;
            return new CursorState(phase, ordered, Position.initial(phase));
        }
    }

    /**
     * Carries the closed union of recoverable DingTalk phase-position values.
     *
     * @param next           official numeric offset when the current upstream page is not the first page
     * @param offset         global department-tree offset
     * @param relationOffset next relation offset within the replayed upstream page
     * @param parentId       current stable department or role identifier
     * @param fingerprint    verified lowercase SHA-256 department-prefix fingerprint
     * @author Kimi Liu
     */
    private record Position(Optional<String> next, int offset, int relationOffset, Optional<String> parentId,
            Optional<String> fingerprint) {

        /**
         * Validates one bounded internal phase position.
         *
         * @param next           official numeric offset
         * @param offset         global department offset
         * @param relationOffset next relation offset
         * @param parentId       stable parent identifier
         * @param fingerprint    tree-prefix fingerprint
         */
        private Position {
            next = optionalText(next, "DingTalk cursor numeric offset");
            parentId = optionalText(parentId, "DingTalk cursor parent identifier");
            fingerprint = optionalText(fingerprint, "DingTalk cursor fingerprint");
            continuation(next, "DingTalk cursor numeric continuation");
            if (offset < 0 || relationOffset < 0) {
                throw new ValidateException("DingTalk cursor offsets must not be negative");
            }
            if (fingerprint.isPresent() && !fingerprint(fingerprint.getOrNull())) {
                throw new ValidateException("DingTalk cursor fingerprint must be lowercase SHA-256 hexadecimal");
            }
        }

        /**
         * Creates the empty position required by one initial finite phase.
         *
         * @param phase initial finite phase
         * @return validated empty phase position
         */
        private static Position initial(final Phase phase) {
            return switch (phase) {
                case DEPARTMENTS -> tree(0, null);
                case USERS -> users(0, Optional.empty(), null, Optional.empty(), 0);
                case ROLES -> page(Optional.empty());
                case ROLE_MEMBERS -> members(null, Optional.empty(), 0);
            };
        }

        /**
         * Creates one department-tree replay position.
         *
         * @param offset      next global department offset
         * @param fingerprint verified prefix fingerprint, or {@code null} at offset zero
         * @return validated tree position
         */
        private static Position tree(final int offset, final String fingerprint) {
            return new Position(Optional.empty(), offset, 0, Optional.empty(), Optional.ofNullable(fingerprint));
        }

        /**
         * Creates one department-dependent user pagination position.
         *
         * @param offset         current global department offset
         * @param fingerprint    verified department-prefix fingerprint
         * @param parentId       current department identifier, or {@code null} before selecting it
         * @param next           official input user numeric offset
         * @param relationOffset next relation offset in the replayed user page
         * @return validated user position
         */
        private static Position users(
                final int offset,
                final Optional<String> fingerprint,
                final String parentId,
                final Optional<String> next,
                final int relationOffset) {
            return new Position(next, offset, relationOffset, Optional.ofNullable(parentId), fingerprint);
        }

        /**
         * Creates one simple official page-token position.
         *
         * @param next official input numeric offset
         * @return validated page position
         */
        private static Position page(final Optional<String> next) {
            return new Position(next, 0, 0, Optional.empty(), Optional.empty());
        }

        /**
         * Creates one parent-bound role-member pagination position.
         *
         * @param parentId       current role identifier, or {@code null} before selecting it
         * @param next           official input member numeric offset
         * @param relationOffset next relation offset in the replayed member page
         * @return validated role-member position
         */
        private static Position members(final String parentId, final Optional<String> next, final int relationOffset) {
            return new Position(next, 0, relationOffset, Optional.ofNullable(parentId), Optional.empty());
        }

        /**
         * Reports whether one string is exactly 64 lowercase hexadecimal characters.
         *
         * @param value candidate fingerprint
         * @return whether the candidate is canonical SHA-256 hexadecimal
         */
        private static boolean fingerprint(final String value) {
            if (value.length() != Normal._64) {
                return false;
            }
            for (int index = 0; index < value.length(); index++) {
                final char character = value.charAt(index);
                if (!((character >= '0' && character <= '9') || (character >= 'a' && character <= 'f'))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Validates and normalizes one Bus optional text container.
         *
         * @param value optional text container
         * @param label safe semantic field label
         * @return detached optional original text
         */
        private static Optional<String> optionalText(final Optional<String> value, final String label) {
            Assert.notNull(value, label + " container must not be null");
            return value.isPresent() ? Optional.of(requireText(value.getOrNull(), label)) : Optional.empty();
        }

        /**
         * Verifies that unused fields are empty for the selected finite phase.
         *
         * @param phase exact finite phase
         */
        private void validate(final Phase phase) {
            switch (phase) {
                case DEPARTMENTS -> {
                    if (next.isPresent() || relationOffset != 0 || parentId.isPresent()
                            || (offset == 0) == fingerprint.isPresent()) {
                        throw new ValidateException("DingTalk department cursor position is inconsistent");
                    }
                }
                case USERS -> {
                    if ((offset == 0) == fingerprint.isPresent()
                            || (parentId.isEmpty() && (next.isPresent() || relationOffset != 0))) {
                        throw new ValidateException("DingTalk user cursor position is inconsistent");
                    }
                }
                case ROLES -> {
                    if (offset != 0 || relationOffset != 0 || parentId.isPresent() || fingerprint.isPresent()) {
                        throw new ValidateException("DingTalk role cursor position is inconsistent");
                    }
                }
                case ROLE_MEMBERS -> {
                    if (offset != 0 || fingerprint.isPresent()
                            || (parentId.isEmpty() && (next.isPresent() || relationOffset != 0))) {
                        throw new ValidateException("DingTalk role-member cursor position is inconsistent");
                    }
                }
            }
        }
    }

    /**
     * Carries one upstream page whose continuation is always an official opaque token.
     *
     * @param items normalized stable-ID-sorted page items
     * @param next  official next numeric offset, or empty at natural completion
     * @param <T>   minimal wire projection type
     * @author Kimi Liu
     */
    private record WirePage<T>(List<T> items, Optional<String> next) {

        /**
         * Validates and freezes one normalized upstream page.
         *
         * @param items normalized page items
         * @param next  official continuation token
         */
        private WirePage {
            Assert.notNull(items, "DingTalk wire page items must not be null");
            final List<T> copy = new ArrayList<>(items.size());
            for (T item : items) {
                copy.add(Assert.notNull(item, "DingTalk wire page item must not be null"));
            }
            items = List.copyOf(copy);
            next = Position.optionalText(next, "DingTalk wire numeric offset");
        }
    }

    /**
     * Carries a DepartmentWalk positioned after a verified replay prefix.
     *
     * @param walk        request-local depth-first department walk
     * @param offset      next global department offset
     * @param fingerprint verified prefix fingerprint
     * @author Kimi Liu
     */
    private record TreePosition(DepartmentWalk walk, int offset, Optional<String> fingerprint) {

        /**
         * Validates one replayed tree position.
         *
         * @param walk        request-local depth-first department walk
         * @param offset      next global department offset
         * @param fingerprint verified prefix fingerprint
         */
        private TreePosition {
            walk = Assert.notNull(walk, "DingTalk department walk must not be null");
            if (offset < 0) {
                throw new ValidateException("DingTalk replay offset must not be negative");
            }
            fingerprint = Position.optionalText(fingerprint, "DingTalk replay fingerprint");
        }
    }

    /**
     * Performs one request-local iterative depth-first walk over direct-child department pages.
     * <p>
     * The stack retains only the actual hierarchy depth and each active parent's sorted direct-child page. It is never
     * placed in a cursor or retained across invocations.
     * </p>
     *
     * @author Kimi Liu
     */
    private static final class DepartmentWalk {

        /**
         * Owning adapter used to execute bounded child-page requests.
         */
        private final DingTalkRealmAdapter owner;

        /**
         * Valid upstream application access retained only for this synchronous operation.
         */
        private final Access access;

        /**
         * Shared end-to-end timeout retained by every child-page request.
         */
        private final Timeout timeout;

        /**
         * Safe operation label propagated into upstream failure details.
         */
        private final String operation;

        /**
         * Request-local depth-first stack of active direct-child pages.
         */
        private final Deque<DepartmentFrame> stack;

        /**
         * Creates one initialized request-local department walk.
         *
         * @param owner     owning adapter
         * @param access    valid upstream application access
         * @param timeout   shared end-to-end timeout
         * @param operation safe enterprise operation label
         * @param children  stable-ID-sorted root children
         */
        private DepartmentWalk(final DingTalkRealmAdapter owner, final Access access, final Timeout timeout,
                final String operation, final List<Department> children) {
            this.owner = Assert.notNull(owner, "DingTalk department walk owner must not be null");
            this.access = Assert.notNull(access, "DingTalk department walk access must not be null");
            this.timeout = Assert.notNull(timeout, "DingTalk department walk timeout must not be null");
            this.operation = requireText(operation, "DingTalk department walk operation");
            this.stack = new ArrayDeque<>();
            if (!children.isEmpty()) {
                this.stack.push(new DepartmentFrame(ROOT_DEPARTMENT_ID, children));
            }
        }

        /**
         * Loads the root direct-child page and creates one request-local walk.
         *
         * @param owner     owning adapter
         * @param access    valid upstream application access
         * @param timeout   shared end-to-end timeout
         * @param operation safe enterprise operation label
         * @return initialized department walk or classified upstream failure
         */
        private static Outcome<DepartmentWalk> start(
                final DingTalkRealmAdapter owner,
                final Access access,
                final Timeout timeout,
                final String operation) {
            final Outcome<List<Department>> root = owner
                    .departmentChildren(access, ROOT_DEPARTMENT_ID, timeout, operation);
            if (root instanceof Outcome.Succeeded<List<Department>> success) {
                return Outcome.succeeded(new DepartmentWalk(owner, access, timeout, operation, success.value()));
            }
            return propagate(root);
        }

        /**
         * Advances one stable depth-first preorder department.
         *
         * @return next department, natural completion, or classified child-page failure
         */
        private Outcome<Optional<Department>> next() {
            while (!stack.isEmpty()) {
                final DepartmentFrame frame = stack.peek();
                if (!frame.hasNext()) {
                    stack.pop();
                    continue;
                }
                final Department department = frame.next();
                for (DepartmentFrame ancestor : stack) {
                    if (ancestor.parentId().equals(department.id())) {
                        return failed(ErrorCode._502, "DingTalk department hierarchy contains a cycle");
                    }
                }
                final Outcome<List<Department>> children = owner
                        .departmentChildren(access, department.id(), timeout, operation);
                if (!(children instanceof Outcome.Succeeded<List<Department>> success)) {
                    return propagate(children);
                }
                if (!success.value().isEmpty()) {
                    stack.push(new DepartmentFrame(department.id(), success.value()));
                }
                return Outcome.succeeded(Optional.of(department));
            }
            return Outcome.succeeded(Optional.empty());
        }
    }

    /**
     * Retains one active parent's stable-ID-sorted direct children and current index.
     *
     * @author Kimi Liu
     */
    private static final class DepartmentFrame {

        /**
         * Stable parent identifier represented by this frame.
         */
        private final String parentId;

        /**
         * Immutable stable-ID-sorted direct children for this parent.
         */
        private final List<Department> children;

        /**
         * Index of the next direct child returned by this frame.
         */
        private int index;

        /**
         * Creates one active depth-first frame.
         *
         * @param parentId stable parent identifier
         * @param children stable-ID-sorted direct children
         */
        private DepartmentFrame(final String parentId, final List<Department> children) {
            this.parentId = requireText(parentId, "DingTalk department frame parent identifier");
            this.children = List
                    .copyOf(Assert.notNull(children, "DingTalk department frame children must not be null"));
        }

        /**
         * Returns whether another direct child remains.
         *
         * @return whether this frame can advance
         */
        private boolean hasNext() {
            return index < children.size();
        }

        /**
         * Returns and advances the next direct child.
         *
         * @return next direct child
         */
        private Department next() {
            return children.get(index++);
        }

        /**
         * Returns the stable parent identifier represented by this frame.
         *
         * @return stable parent identifier
         */
        private String parentId() {
            return parentId;
        }
    }

    /**
     * Minimal non-sensitive DingTalk department projection.
     *
     * @param id       stable department identifier
     * @param name     exact department display name
     * @param parentId stable parent department identifier
     * @author Kimi Liu
     */
    private record Department(String id, String name, String parentId) {

        /**
         * Validates one minimal department projection.
         *
         * @param id       stable department identifier
         * @param name     exact display name
         * @param parentId stable parent identifier
         */
        private Department {
            id = requireText(id, "DingTalk department identifier");
            name = requireText(name, "DingTalk department name");
            parentId = requireText(parentId, "DingTalk department parent identifier");
            if (id.equals(parentId)) {
                throw new ValidateException("DingTalk department must not be its own parent");
            }
        }
    }

    /**
     * Minimal non-sensitive DingTalk user projection.
     *
     * @param id            stable user identifier
     * @param name          exact user display name
     * @param active        official activation state
     * @param departmentIds stable department identifiers in canonical order
     * @param managerId     stable manager user identifier, or empty
     * @author Kimi Liu
     */
    private record User(String id, String name, boolean active, List<String> departmentIds,
            Optional<String> managerId) {

        /**
         * Validates and normalizes one minimal user projection.
         *
         * @param id            stable user identifier
         * @param name          exact display name
         * @param active        official activation state
         * @param departmentIds stable department identifiers
         * @param managerId     stable manager identifier
         */
        private User {
            id = requireText(id, "DingTalk user identifier");
            name = requireText(name, "DingTalk user name");
            Assert.notNull(departmentIds, "DingTalk user department identifiers must not be null");
            final LinkedHashSet<String> unique = new LinkedHashSet<>();
            for (String departmentId : departmentIds) {
                unique.add(requireText(departmentId, "DingTalk user department identifier"));
            }
            if (unique.isEmpty()) {
                throw new ValidateException("DingTalk user must belong to at least one visible department");
            }
            final List<String> ordered = new ArrayList<>(unique);
            ordered.sort(String::compareTo);
            departmentIds = List.copyOf(ordered);
            managerId = Position.optionalText(managerId, "DingTalk user manager identifier");
            if (managerId.isPresent() && id.equals(managerId.getOrNull())) {
                throw new ValidateException("DingTalk user must not manage itself");
            }
        }
    }

    /**
     * Minimal non-sensitive DingTalk role projection.
     *
     * @param id   stable role identifier
     * @param name exact role display name
     * @author Kimi Liu
     */
    private record Role(String id, String name) {

        /**
         * Validates one minimal role projection.
         *
         * @param id   stable role identifier
         * @param name exact display name
         */
        private Role {
            id = requireText(id, "DingTalk role identifier");
            name = requireText(name, "DingTalk role name");
        }
    }

    /**
     * Minimal non-sensitive DingTalk role-member projection.
     *
     * @param id stable user identifier returned as the member identifier
     * @author Kimi Liu
     */
    private record RoleMember(String id) {

        /**
         * Validates one minimal role-member projection.
         *
         * @param id stable member user identifier
         */
        private RoleMember {
            id = requireText(id, "DingTalk role member identifier");
        }
    }

    /**
     * Holds one short-lived DingTalk application token inside the Source-private cache.
     *
     * @param token           non-blank upstream application token
     * @param expiresAtMillis upstream absolute expiration in Fabric clock milliseconds, or zero when uncacheable
     * @author Kimi Liu
     */
    private record Access(String token, long expiresAtMillis) {

        /**
         * Validates one upstream application access value without changing token text.
         *
         * @param token           non-blank upstream application token
         * @param expiresAtMillis positive absolute expiration or zero
         */
        private Access {
            token = requireText(token, "DingTalk application access token");
            if (expiresAtMillis < 0L) {
                throw new ValidateException("DingTalk application access expiration must not be negative");
            }
        }

        /**
         * Returns a fixed representation that cannot disclose the application token or lifetime.
         *
         * @return root redacted marker
         */
        @Override
        public String toString() {
            return Builder.REDACTED_VALUE;
        }
    }

}
