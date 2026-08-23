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
package org.miaixz.bus.auth.source.vendor.wechat;

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
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.source.vendor.VendorTargets;
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
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Payload;

/**
 * Implements the implementation-neutral WeCom realm surface under the stable WeChat Vendor.
 * <p>
 * Snapshot pagination advances through the finite {@link Phase} order {@code DEPARTMENTS}, {@code USERS},
 * {@code GROUPS}, and {@code GROUP_MEMBERS}. Parent-dependent phases replay the stable sorted department or tag list
 * before completely rereading the current official unpaged child response. Every phase sorts its frozen minimal
 * projection by stable identifier and uses only offset, relation offset, parent identifier, and a lowercase SHA-256
 * projection fingerprint in the opaque cursor. Snapshot limits split only normalized output; they never limit,
 * truncate, or cache an upstream response. The adapter never persists directory data or implements changes.
 * </p>
 *
 * @author Kimi Liu
 */
public class WeChatEeRealmAdapter implements VendorAdapter {

    /**
     * Synthetic parent identifier used by the root WeCom department.
     */
    private static final String ROOT_PARENT_ID = "0";

    /**
     * WeCom business code indicating an invalid application access token.
     */
    private static final long INVALID_ACCESS_TOKEN_CODE = 40014L;

    /**
     * WeCom business code indicating an expired application access token.
     */
    private static final long EXPIRED_ACCESS_TOKEN_CODE = 42001L;

    /**
     * WeCom business code indicating that the application lacks the requested API permission.
     */
    private static final long API_PERMISSION_DENIED_CODE = 48002L;

    /**
     * WeCom business code indicating that the application cannot view the requested contact resource.
     */
    private static final long CONTACT_PERMISSION_DENIED_CODE = 60011L;

    /**
     * WeCom business code indicating that an explicit user retrieval did not find the requested member.
     */
    private static final long USER_NOT_FOUND_CODE = 60111L;

    /**
     * WeCom business code indicating application-level request-frequency exhaustion.
     */
    private static final long RATE_LIMITED_CODE = 45009L;

    /**
     * Empty immutable JSON object used by projections without allow-listed attributes.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Resource categories exposed by the Realm Variant.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set
            .of(Realm.Kind.USER, Realm.Kind.ORGANIZATION, Realm.Kind.GROUP);

    /**
     * Exact ordered management-target key set required by this adapter.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.REALM_USERS,
            Builder.REALM_USER,
            Builder.REALM_ORGANIZATIONS,
            Builder.REALM_ORGANIZATION_USERS,
            Builder.REALM_GROUPS,
            Builder.REALM_GROUP_MEMBERS);

    /**
     * Selected immutable WeChat Realm Variant.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated WeChat Realm deployment options containing only a credential reference.
     */
    private final WeChatOptions options;

    /**
     * Caller-owned execution services used without taking lifecycle ownership.
     */
    private final DriverServices services;

    /**
     * Resolved manifest-owned token and WeCom management endpoints.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Source-private cache holding only the short-lived upstream application token.
     */
    private final CacheX<String, Access> accessCache;

    /**
     * Creates one Source-isolated WeChat realm adapter.
     *
     * @param spaceId  Source space used to isolate credential resolution
     * @param sourceId Source identifier used to validate Source ownership
     * @param manifest exact WeChat manifest
     * @param variant  exact selected Realm Variant
     * @param options  validated WeChat Realm options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if the manifest, Variant, protocol, targets, or options are inconsistent
     */
    public WeChatEeRealmAdapter(final String spaceId, final String sourceId, final WeChatManifest manifest,
            final VendorManifest.Variant variant, final WeChatOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "WeChat Realm space id must not be blank");
        Assert.notBlank(sourceId, "WeChat Realm Source id must not be blank");
        final WeChatManifest selectedManifest = Assert.notNull(manifest, "WeChat Realm manifest must not be null");
        this.variant = Assert.notNull(variant, "WeChat Realm Variant must not be null");
        this.options = Assert.notNull(options, "WeChat Realm options must not be null");
        this.services = Assert.notNull(services, "WeChat Realm services must not be null");
        if (!WeChatManifest.ID.equals(selectedManifest.vendor())
                || !selectedManifest.variant(WeChatManifest.EE_ENTERPRISE).equals(this.variant)
                || !WeChatManifest.ID.equals(this.variant.platform())
                || !WeChatManifest.EE_ENTERPRISE.equals(this.variant.variant())
                || this.variant.protocol() != Protocol.HTTPS || !WeChatManifest.ID.equals(this.options.vendor())
                || !WeChatManifest.EE_ENTERPRISE.equals(this.options.variant())
                || this.options.redirectUri().isPresent() || !this.options.scopes().isEmpty()
                || !this.options.loginType().isEmpty() || !this.options.agentId().isEmpty()
                || !this.options.language().isEmpty() || !this.options.userType().isEmpty()) {
            throw new ValidateException("WeChat realm adapter requires the wechat/ee-enterprise HTTPS manifest");
        }
        this.services.policies().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (this.targets.token().isEmpty()
                || !List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("WeChat Realm manifest has an invalid management target set");
        }
        if (!this.targets.management().get(Builder.REALM_USERS)
                .equals(this.targets.management().get(Builder.REALM_ORGANIZATION_USERS))) {
            throw new ValidateException("WeChat Realm user-list targets must resolve identically");
        }
        this.accessCache = new MemoryCache<>(FabricX.clock()::millis);
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
     * Resolves the current parent index from one stable sorted replay collection.
     *
     * @param values   complete stable sorted projection
     * @param parentId cursor parent identifier, empty for the first parent
     * @param identity stable identifier extractor
     * @param message  safe replay failure description
     * @param <T>      minimal projection type
     * @return current parent index, or {@code -1} when the collection is empty
     */
    private static <T> int parentIndex(
            final List<T> values,
            final Optional<String> parentId,
            final Function<T, String> identity,
            final String message) {
        if (values.isEmpty()) {
            if (parentId.isPresent()) {
                throw new ValidateException(message);
            }
            return -1;
        }
        if (parentId.isEmpty()) {
            return 0;
        }
        for (int index = 0; index < values.size(); index++) {
            if (parentId.getOrNull().equals(identity.apply(values.get(index)))) {
                return index;
            }
        }
        throw new ValidateException(message);
    }

    /**
     * Verifies one unpaged continuation against its complete current projection.
     *
     * @param position    validated cursor position
     * @param fingerprint complete current projection fingerprint
     * @param size        complete current projection size
     * @param parentId    expected parent identifier, or {@code null} for a global collection
     * @return successful verification or a replay failure
     */
    private static Outcome<Boolean> verify(
            final Position position,
            final String fingerprint,
            final int size,
            final String parentId) {
        if (position.offset() > size) {
            return failed(ErrorCode._502, "WeChat unpaged replay offset exceeds the current projection");
        }
        if ((parentId == null && position.parentId().isPresent()) || (parentId != null
                && position.parentId().isPresent() && !parentId.equals(position.parentId().getOrNull()))) {
            return failed(ErrorCode._502, "WeChat unpaged replay parent no longer matches the cursor");
        }
        if (position.fingerprint().isPresent()) {
            if (!fingerprint.equals(position.fingerprint().getOrNull())) {
                return failed(ErrorCode._502, "WeChat unpaged source projection changed between pages");
            }
        } else if (position.offset() != 0 || position.relationOffset() != 0) {
            return failed(ErrorCode._502, "WeChat unpaged replay omits its projection fingerprint");
        }
        return Outcome.succeeded(Boolean.TRUE);
    }

    /**
     * Encodes one validated user projection in the frozen fingerprint field order.
     *
     * @param user minimal validated user projection
     * @return deterministic JSON projection
     */
    private static JsonValue.ObjectValue userProjection(final User user) {
        final Map<String, JsonValue> item = new LinkedHashMap<>();
        item.put("userid", new JsonValue.StringValue(user.id()));
        item.put("name", new JsonValue.StringValue(user.name()));
        item.put("status", number(user.status()));
        final List<JsonValue> departments = new ArrayList<>(user.departmentIds().size());
        for (String departmentId : user.departmentIds()) {
            departments.add(number(numericIdentifier(departmentId, "WeChat user department identifier")));
        }
        item.put("department", new JsonValue.ArrayValue(departments));
        final List<JsonValue> managers = new ArrayList<>(user.managerIds().size());
        for (String managerId : user.managerIds()) {
            managers.add(new JsonValue.StringValue(managerId));
        }
        item.put("direct_leader", new JsonValue.ArrayValue(managers));
        return new JsonValue.ObjectValue(item);
    }

    /**
     * Converts one department projection into a implementation-neutral organization resource.
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
     * Converts one user projection into a implementation-neutral user resource.
     *
     * @param user       minimal validated user projection
     * @param observedAt shared invocation observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()), Map.of("userid", user.id()), user.name(),
                user.status() == 1L ? Realm.State.ACTIVE : Realm.State.INACTIVE, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one group projection into a implementation-neutral group resource.
     *
     * @param group      minimal validated group projection
     * @param observedAt shared invocation observation instant
     * @return immutable group resource
     */
    private static Realm.Resource groupResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.GROUP, group.id()), Map.of(), group.name(),
                Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
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
     * @param containerKind normalized organization or group category
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
     * Creates one directed managed-user-to-manager relation.
     *
     * @param userId     stable managed user identifier
     * @param leaderId   stable manager user identifier
     * @param observedAt shared invocation observation instant
     * @return immutable manager relation
     */
    private static Realm.Relation managerRelation(
            final String userId,
            final String leaderId,
            final Instant observedAt) {
        return relation(Realm.RelationKind.MANAGER, Realm.Kind.USER, userId, Realm.Kind.USER, leaderId, observedAt);
    }

    /**
     * Creates one attribute-free directed Realm relation.
     *
     * @param relationKind normalized relation semantic
     * @param fromKind     normalized source resource category
     * @param fromId       stable source identifier
     * @param toKind       normalized target resource category
     * @param toId         stable target identifier
     * @param observedAt   shared invocation observation instant
     * @return immutable implementation-neutral relation
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
                    : kinds.contains(Realm.Kind.GROUP) ? Phase.GROUPS : null;
            case USERS -> kinds.contains(Realm.Kind.GROUP) ? Phase.GROUPS : null;
            case GROUPS -> Phase.GROUP_MEMBERS;
            case GROUP_MEMBERS -> null;
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
        position.validate(phase);
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put(Builder.CURSOR_OFFSET_FIELD, number(position.offset()));
        values.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(position.relationOffset()));
        values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
        values.put(Builder.CURSOR_FINGERPRINT_FIELD, nullable(position.fingerprint()));
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
        exactMembers(
                value,
                Set.of(
                        Builder.CURSOR_OFFSET_FIELD,
                        Builder.CURSOR_RELATION_OFFSET_FIELD,
                        Builder.CURSOR_PARENT_ID_FIELD,
                        Builder.CURSOR_FINGERPRINT_FIELD),
                "WeChat unpaged cursor position");
        final Position position = Position.unpaged(
                nonNegativeInt(value, Builder.CURSOR_OFFSET_FIELD),
                nonNegativeInt(value, Builder.CURSOR_RELATION_OFFSET_FIELD),
                nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                nullableString(value, Builder.CURSOR_FINGERPRINT_FIELD).getOrNull());
        position.validate(phase);
        return position;
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
                throw new ValidateException("WeChat cursor kind must be an integer code");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("WeChat cursor kind code is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !kinds.add(kind)) {
                throw new ValidateException("WeChat cursor kinds are not in canonical code order");
            }
            previous = code;
        }
        if (kinds.isEmpty()) {
            throw new ValidateException("WeChat cursor kinds must not be empty");
        }
        return List.copyOf(kinds);
    }

    /**
     * Resolves one stable Realm kind code without depending on enum declaration order.
     *
     * @param code stable persisted kind code
     * @return exact Realm kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : Realm.Kind.values()) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("WeChat cursor contains an unknown kind code");
    }

    /**
     * Rejects unsupported snapshot kinds instead of silently ignoring them.
     *
     * @param kinds normalized non-empty requested kind set
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (!SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("WeChat Realm snapshot contains an unsupported kind");
        }
    }

    /**
     * Parses one minimal WeChat department projection.
     *
     * @param value decoded department object
     * @return validated minimal department projection
     */
    private static Department department(final JsonValue.ObjectValue value) {
        return new Department(requiredIdentifier(value, "id"), requiredString(value, "name"),
                requiredIdentifier(value, "parentid"), requiredLong(value, "order"));
    }

    /**
     * Parses one minimal WeChat user projection.
     *
     * @param value decoded user object
     * @return validated minimal user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        final List<String> departmentIds = new ArrayList<>();
        for (JsonValue department : requiredArray(value, "department")) {
            departmentIds.add(numericIdentifier(department, "WeChat user department identifier"));
        }
        final List<String> managerIds = new ArrayList<>();
        for (JsonValue manager : optionalArray(value, "direct_leader")) {
            managerIds.add(identifier(manager, "WeChat direct leader identifier"));
        }
        return new User(requiredString(value, "userid"), requiredString(value, "name"), requiredLong(value, "status"),
                departmentIds, managerIds);
    }

    /**
     * Parses one minimal WeChat group projection.
     *
     * @param value decoded group object
     * @return validated minimal group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredIdentifier(value, "tagid"), requiredString(value, "tagname"));
    }

    /**
     * Parses one minimal WeChat group-member projection.
     *
     * @param value decoded member object
     * @return validated minimal member projection
     */
    private static Member member(final JsonValue.ObjectValue value) {
        return new Member(requiredString(value, "userid"));
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
     * Reads one optional JSON array member as empty when absent or explicitly null.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return immutable decoded array elements or an empty list
     */
    private static List<JsonValue> optionalArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array or null");
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
     * Reads one required WeCom identifier represented as a JSON string or exact non-negative integer.
     *
     * @param object decoded parent object
     * @param name   exact member name
     * @return canonical stable identifier text
     */
    private static String requiredIdentifier(final JsonValue.ObjectValue object, final String name) {
        return numericIdentifier(object.values().get(name), name);
    }

    /**
     * Converts one WeCom numeric identifier represented as a canonical string or exact JSON integer.
     *
     * @param value decoded numeric identifier value
     * @param label safe semantic field label
     * @return canonical non-negative decimal identifier text
     */
    private static String numericIdentifier(final JsonValue value, final String label) {
        if (value instanceof JsonValue.StringValue text) {
            final String checked = requireText(text.value(), label);
            final long identifier = numericIdentifier(checked, label);
            if (!Long.toString(identifier).equals(checked)) {
                throw new ValidateException(label + " must be canonical decimal text");
            }
            return checked;
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                final long identifier = number.value().longValueExact();
                if (identifier < 0L) {
                    throw new ValidateException(label + " must not be negative");
                }
                return Long.toString(identifier);
            } catch (ArithmeticException cause) {
                throw new ValidateException(label + " must be an exact integer", cause);
            }
        }
        throw new ValidateException(label + " must be a canonical JSON string or exact integer");
    }

    /**
     * Converts one WeCom string or exact non-negative integer identifier to canonical text.
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
                if (identifier < 0L) {
                    throw new ValidateException(label + " must not be negative");
                }
                return Long.toString(identifier);
            } catch (ArithmeticException cause) {
                throw new ValidateException(label + " must be an exact integer", cause);
            }
        }
        throw new ValidateException(label + " must be a JSON string or exact integer");
    }

    /**
     * Converts one validated canonical numeric identifier to an exact non-negative long.
     *
     * @param value canonical identifier text
     * @param label safe semantic field label
     * @return exact non-negative identifier value
     */
    private static long numericIdentifier(final String value, final String label) {
        try {
            final long identifier = Long.parseLong(value);
            if (identifier < 0L) {
                throw new ValidateException(label + " must not be negative");
            }
            return identifier;
        } catch (NumberFormatException cause) {
            throw new ValidateException(label + " must be a canonical integer", cause);
        }
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
     * @return implementation-neutral JSON value
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
     * @param operation safe Realm operation label
     * @param status    upstream HTTP status
     * @param errorCode non-sensitive WeChat business code, or {@code null}
     * @param headers   upstream headers used only for Retry-After normalization
     * @return immutable allow-listed scalar detail map
     */
    private static Map<String, JsonValue> details(
            final String operation,
            final int status,
            final Long errorCode,
            final FabricX.Headers headers) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(WeChatManifest.ID.value()));
        details.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(WeChatManifest.EE_ENTERPRISE.value()));
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
            default -> failed(ErrorCode._500, "WeChat delegated operation returned an unsupported outcome");
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
            default -> failed(ErrorCode._500, "WeChat internal operation could not be propagated");
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
        return completed(rejected(ErrorCode._400, "WeChat Realm capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(
                rejected(ErrorCode._400, "WeChat Realm request does not match the selected capability contract"));
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
     * Returns the exact Realm capability manifest selected at Source compilation.
     *
     * @return immutable describe, snapshot, and retrieve capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes one exact WeChat Realm capability and request type.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific Realm request
     * @param context    immutable non-secret invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return asynchronous typed Realm outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "WeChat Realm capability must not be null");
        Assert.notNull(context, "WeChat Realm context must not be null");
        Assert.notNull(timeout, "WeChat Realm timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.equals(Realm.DESCRIBE) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(WeChatManifest.realmDescription())));
        }
        if (capability.equals(Realm.SNAPSHOT) && request instanceof Realm.Snapshot snapshot) {
            return narrow(snapshot(snapshot, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.RETRIEVE) && request instanceof Realm.Retrieve retrieve) {
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
     * @param request exact implementation-neutral snapshot request
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
            return completed(rejected(ErrorCode._400, "WeChat Realm snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "WeChat Realm snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
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
                case GROUPS -> groups(access, request, state, observedAt, timeout);
                case GROUP_MEMBERS -> groupMembers(access, request, state, observedAt, timeout);
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "WeChat WeCom management API returned an invalid snapshot projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "WeChat Realm snapshot processing failed locally");
        }
    }

    /**
     * Reads one bounded organization page from a completely replayed W-02 response.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      unpaged department replay state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded organization page
     */
    private Outcome<Realm.Page> departments(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Department>> fetched = departmentList(access, "snapshot", timeout);
        if (!(fetched instanceof Outcome.Succeeded<List<Department>> success)) {
            return propagate(fetched);
        }
        final List<Department> departments = success.value();
        final String fingerprint = departmentFingerprint(departments);
        final Outcome<Boolean> verified = verify(state.position(), fingerprint, departments.size(), null);
        if (!(verified instanceof Outcome.Succeeded<Boolean>)) {
            return propagate(verified);
        }
        final List<Realm.Resource> resources = new ArrayList<>();
        final List<Realm.Relation> relations = new ArrayList<>();
        int offset = state.position().offset();
        while (offset < departments.size()) {
            final Department department = departments.get(offset++);
            resources.add(departmentResource(department, observedAt));
            if (!ROOT_PARENT_ID.equals(department.parentId())) {
                relations.add(parentRelation(department, observedAt));
            }
            if (resources.size() >= request.limit() || relations.size() >= request.limit()) {
                if (offset >= departments.size()) {
                    return completedPhase(resources, relations, state.kinds(), Phase.DEPARTMENTS);
                }
                return page(
                        resources,
                        relations,
                        state.kinds(),
                        Phase.DEPARTMENTS,
                        Position.unpaged(offset, 0, null, fingerprint));
            }
        }
        return completedPhase(resources, relations, state.kinds(), Phase.DEPARTMENTS);
    }

    /**
     * Reads one bounded user page from a completely replayed W-03 department response.
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
        final Position position = state.position();
        final Outcome<UserReplay> replayed = userReplay(access, "snapshot", timeout);
        if (!(replayed instanceof Outcome.Succeeded<UserReplay> replaySuccess)) {
            return propagate(replayed);
        }
        final UserReplay replay = replaySuccess.value();
        final List<Department> departments = replay.departments();
        final int parentIndex = parentIndex(
                departments,
                position.parentId(),
                Department::id,
                "WeChat department replay parent no longer exists");
        if (parentIndex < 0) {
            return completedPhase(List.of(), List.of(), state.kinds(), Phase.USERS);
        }
        final Department department = departments.get(parentIndex);
        final List<User> users = replay.users().get(department.id());
        final String fingerprint = replay.fingerprint();
        final Outcome<Boolean> verified = verify(position, fingerprint, users.size(), department.id());
        if (!(verified instanceof Outcome.Succeeded<Boolean>)) {
            return propagate(verified);
        }
        final List<Realm.Resource> resources = new ArrayList<>();
        final List<Realm.Relation> relations = new ArrayList<>();
        int offset = position.offset();
        while (offset < users.size()) {
            final User user = users.get(offset);
            if (!user.departmentIds().contains(department.id())) {
                return failed(ErrorCode._502, "WeChat department user is not bound to the requested department");
            }
            final boolean canonical = department.id().equals(user.departmentIds().get(0));
            final int relationOffset = offset == position.offset() ? position.relationOffset() : 0;
            if (relationOffset == 0 && canonical) {
                resources.add(userResource(user, observedAt));
            }
            final List<Realm.Relation> userRelations = new ArrayList<>();
            userRelations.add(memberRelation(user.id(), Realm.Kind.ORGANIZATION, department.id(), observedAt));
            if (canonical) {
                for (String managerId : user.managerIds()) {
                    userRelations.add(managerRelation(user.id(), managerId, observedAt));
                }
            }
            if (relationOffset > userRelations.size()) {
                return failed(ErrorCode._502, "WeChat user relation offset exceeds the replayed projection");
            }
            int relationIndex = relationOffset;
            while (relationIndex < userRelations.size() && relations.size() < request.limit()) {
                relations.add(userRelations.get(relationIndex++));
            }
            if (relationIndex < userRelations.size()) {
                return page(
                        resources,
                        relations,
                        state.kinds(),
                        Phase.USERS,
                        Position.unpaged(offset, relationIndex, department.id(), fingerprint));
            }
            offset++;
            if ((resources.size() >= request.limit() || relations.size() >= request.limit()) && offset < users.size()) {
                return page(
                        resources,
                        relations,
                        state.kinds(),
                        Phase.USERS,
                        Position.unpaged(offset, 0, department.id(), fingerprint));
            }
        }
        if (parentIndex + 1 < departments.size()) {
            return page(
                    resources,
                    relations,
                    state.kinds(),
                    Phase.USERS,
                    Position.unpaged(0, 0, departments.get(parentIndex + 1).id(), fingerprint));
        }
        return completedPhase(resources, relations, state.kinds(), Phase.USERS);
    }

    /**
     * Reads one bounded group page from a completely replayed W-05 response.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      group-list cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded group page
     */
    private Outcome<Realm.Page> groups(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Group>> fetched = groupList(access, "snapshot", timeout);
        if (!(fetched instanceof Outcome.Succeeded<List<Group>> success)) {
            return propagate(fetched);
        }
        final List<Group> groups = success.value();
        final String fingerprint = groupFingerprint(groups);
        final Outcome<Boolean> verified = verify(state.position(), fingerprint, groups.size(), null);
        if (!(verified instanceof Outcome.Succeeded<Boolean>)) {
            return propagate(verified);
        }
        final List<Realm.Resource> resources = new ArrayList<>();
        int offset = state.position().offset();
        while (offset < groups.size() && resources.size() < request.limit()) {
            resources.add(groupResource(groups.get(offset++), observedAt));
        }
        if (offset < groups.size()) {
            return page(
                    resources,
                    List.of(),
                    state.kinds(),
                    Phase.GROUPS,
                    Position.unpaged(offset, 0, null, fingerprint));
        }
        return completedPhase(resources, List.of(), state.kinds(), Phase.GROUPS);
    }

    /**
     * Reads one bounded group-member page from a completely replayed W-06 response.
     *
     * @param access     valid upstream application access
     * @param request    validated snapshot request
     * @param state      group-member cursor state
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded group-member relation page
     */
    private Outcome<Realm.Page> groupMembers(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Position position = state.position();
        final Outcome<GroupMemberReplay> replayed = groupMemberReplay(access, "snapshot", timeout);
        if (!(replayed instanceof Outcome.Succeeded<GroupMemberReplay> replaySuccess)) {
            return propagate(replayed);
        }
        final GroupMemberReplay replay = replaySuccess.value();
        final List<Group> groups = replay.groups();
        final int parentIndex = parentIndex(
                groups,
                position.parentId(),
                Group::id,
                "WeChat group replay parent no longer exists");
        if (parentIndex < 0) {
            return completedPhase(List.of(), List.of(), state.kinds(), Phase.GROUP_MEMBERS);
        }
        final Group group = groups.get(parentIndex);
        final List<Member> members = replay.members().get(group.id());
        final String fingerprint = replay.fingerprint();
        final Outcome<Boolean> verified = verify(position, fingerprint, members.size(), group.id());
        if (!(verified instanceof Outcome.Succeeded<Boolean>)) {
            return propagate(verified);
        }
        final List<Realm.Relation> relations = new ArrayList<>();
        int offset = position.offset();
        while (offset < members.size() && relations.size() < request.limit()) {
            relations.add(memberRelation(members.get(offset++).id(), Realm.Kind.GROUP, group.id(), observedAt));
        }
        if (offset < members.size()) {
            return page(
                    List.of(),
                    relations,
                    state.kinds(),
                    Phase.GROUP_MEMBERS,
                    Position.unpaged(offset, 0, group.id(), fingerprint));
        }
        if (parentIndex + 1 < groups.size()) {
            return page(
                    List.of(),
                    relations,
                    state.kinds(),
                    Phase.GROUP_MEMBERS,
                    Position.unpaged(0, 0, groups.get(parentIndex + 1).id(), fingerprint));
        }
        return Outcome.succeeded(new Realm.Page(List.of(), relations, Optional.empty()));
    }

    /**
     * Validates and starts one direct retrieval with a single observation instant.
     *
     * @param request exact implementation-neutral retrieval request
     * @param context immutable context used only for credential loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "WeChat Realm retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "WeChat Realm retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock().now();
        return authenticated(context, timeout, access -> retrieve(access, request.key(), observedAt, timeout));
    }

    /**
     * Dispatches one retrieval to the fixed WeCom management API resource path.
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
                case GROUP -> retrieveGroup(access, key.externalId(), observedAt, timeout);
                default -> rejected(ErrorCode._400, "WeChat Realm retrieve kind is unsupported");
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "WeChat WeCom management API returned an invalid retrieval projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "WeChat Realm retrieval processing failed locally");
        }
    }

    /**
     * Retrieves one user through the official stable-ID endpoint.
     *
     * @param access     valid upstream application access
     * @param userId     stable WeChat user identifier
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved user or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveUser(
            final Access access,
            final String userId,
            final Instant observedAt,
            final Timeout timeout) {
        final Endpoint endpoint = target(Builder.REALM_USER);
        final Outcome<JsonValue.ObjectValue> fetched = get(
                endpoint,
                access,
                timeout,
                "retrieve",
                true,
                Map.of("userid", requireText(userId, "WeChat user identifier")));
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final User user = user(success.value());
        if (!userId.equals(user.id())) {
            return failed(ErrorCode._502, "WeChat retrieve returned a different user identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(userResource(user, observedAt))));
    }

    /**
     * Scans the completely replayed official department list for one stable identifier.
     *
     * @param access       valid upstream application access
     * @param departmentId stable WeChat department identifier
     * @param observedAt   shared invocation observation instant
     * @param timeout      shared end-to-end timeout
     * @return retrieved department or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveDepartment(
            final Access access,
            final String departmentId,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Department>> fetched = departmentList(access, "retrieve", timeout);
        if (!(fetched instanceof Outcome.Succeeded<List<Department>> success)) {
            return propagate(fetched);
        }
        for (Department department : success.value()) {
            if (departmentId.equals(department.id())) {
                return Outcome.succeeded(new Realm.Retrieved(Optional.of(departmentResource(department, observedAt))));
            }
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
    }

    /**
     * Scans the completely replayed official tag list for one stable group identifier.
     *
     * @param access     valid upstream application access
     * @param groupId    stable WeChat group identifier
     * @param observedAt shared invocation observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved group or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveGroup(
            final Access access,
            final String groupId,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<List<Group>> fetched = groupList(access, "retrieve", timeout);
        if (!(fetched instanceof Outcome.Succeeded<List<Group>> success)) {
            return propagate(fetched);
        }
        for (Group group : success.value()) {
            if (groupId.equals(group.id())) {
                return Outcome.succeeded(new Realm.Retrieved(Optional.of(groupResource(group, observedAt))));
            }
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
    }

    /**
     * Acquires a cached or freshly exchanged application token and retries one 401 result exactly once.
     *
     * @param context   immutable context used by the project Secret Loader
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous WeCom management API operation
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
                                        (ignored, cause) -> cause == null ? Outcome.<Boolean>succeeded(Boolean.TRUE)
                                                : WeChatEeRealmAdapter.<Boolean>failed(
                                                        ErrorCode._500,
                                                        "WeChat upstream-token cache deletion failed"))
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
                                                        "WeChat token refresh returned an unsupported outcome"));
                                    });
                                });
                    });
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "WeChat token lookup returned an unsupported outcome"));
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
                return failed(ErrorCode._408, "WeChat Realm operation has no remaining timeout");
            }
            try {
                return Assert.notNull(operation.apply(access), "WeChat Realm operation returned no outcome");
            } catch (TimeoutException ignored) {
                return failed(ErrorCode._408, "WeChat Realm operation timed out");
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._503, "WeChat WeCom management API transport is unavailable");
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
                        return completed(failed(ErrorCode._500, "WeChat upstream-token cache lookup failed"));
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
                    .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout);
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "WeChat Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "WeChat Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "WeChat Secret Loader stage failed"))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> exchange(success.value(), timeout);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(ErrorCode._500, "WeChat Secret Loader returned an unsupported outcome"));
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
            secret = services.secretParser().parse(services.entry(), options.credential(), loaded);
        } catch (RuntimeException ignored) {
            if (raw != null) {
                raw.close();
            }
            return completed(failed(ErrorCode._500, "WeChat loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<Access>>supplyAsync(() -> {
                try {
                    return token(secret, timeout);
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "WeChat application-token exchange timed out");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "WeChat application-token transport is unavailable");
                }
            }, services.executor()).whenComplete((ignored, cause) -> secret.close()).thenCompose(this::cache);
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "WeChat application-token task could not be scheduled"));
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
        final long now = FabricX.clock().millis();
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
                        : failed(ErrorCode._500, "WeChat upstream-token cache creation failed"));
    }

    /**
     * Exchanges one Corp ID and leased application Corp Secret for a WeCom access token.
     *
     * @param secret  still-open App Secret lease
     * @param timeout shared end-to-end timeout
     * @return upstream access result or safely classified failure
     */
    private Outcome<Access> token(final SecretLease secret, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeChat application-token exchange has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(Protocol.HTTPS, timeout, services.policies())
                    .url(targets.token().getOrNull().url().toString()).method(Http.Method.GET)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).query("corpid", options.clientId())
                    .query("corpsecret", new String(secret.material())).execute();
        } catch (TimeoutException cause) {
            return failed(ErrorCode._408, "WeChat application-token exchange timed out");
        } catch (RuntimeException cause) {
            return failed(ErrorCode._503, "WeChat application-token endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return tokenHttpFailure(response);
            }
            final JsonValue.ObjectValue envelope = object(response);
            final long code = requiredLong(envelope, "errcode");
            if (code != Normal._0) {
                return rejected(
                        ErrorCode._401,
                        "WeChat rejected the configured Corp credentials",
                        details("token", response.code(), code, response.headers()));
            }
            final String token = requiredString(envelope, "access_token");
            final long seconds = requiredPositiveLong(envelope, "expires_in");
            final long now = FabricX.clock().millis();
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
            return failed(ErrorCode._502, "WeChat application-token endpoint returned an invalid response");
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
            return rejected(ErrorCode._401, "WeChat rejected the configured App credentials", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "WeChat application-token endpoint is rate limited", details);
        }
        return failed(ErrorCode._502, "WeChat application-token endpoint returned an upstream error", details);
    }

    /**
     * Executes one bounded WeCom management GET used only by direct retrieval.
     *
     * @param endpoint      exact manifest-derived endpoint
     * @param access        valid upstream application access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe Realm operation label
     * @param allowNotFound whether the explicit member-not-found code represents absence
     * @param query         exact non-secret query parameters
     * @return decoded success envelope, {@code null} for explicit absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Endpoint endpoint,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound,
            final Map<String, ?> query) {
        return get(endpoint, access, timeout, operation, allowNotFound, query, this::object);
    }

    /**
     * Executes one completely streamed WeCom unpaged management GET.
     *
     * @param endpoint  exact manifest-derived unpaged endpoint
     * @param access    valid upstream application access
     * @param timeout   shared end-to-end timeout
     * @param operation safe Realm operation label
     * @param query     exact non-secret query parameters
     * @return completely decoded success envelope or classified failure
     */
    private Outcome<JsonValue.ObjectValue> getUnpaged(
            final Endpoint endpoint,
            final Access access,
            final Timeout timeout,
            final String operation,
            final Map<String, ?> query) {
        return get(endpoint, access, timeout, operation, false, query, this::unpagedObject);
    }

    /**
     * Executes one WeCom management GET and validates its common business envelope.
     *
     * @param endpoint      exact manifest-derived endpoint
     * @param access        valid upstream application access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe Realm operation label
     * @param allowNotFound whether the explicit member-not-found code represents absence
     * @param query         exact non-secret query parameters
     * @param reader        bounded or explicit unpaged response reader
     * @return decoded success envelope, {@code null} for explicit absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Endpoint endpoint,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound,
            final Map<String, ?> query,
            final Function<Response, JsonValue.ObjectValue> reader) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeCom management request has no remaining timeout");
        }
        final Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.putAll(query);
        parameters.put("access_token", access.token());
        final Response response;
        try {
            response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(endpoint.url().toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).query(parameters)
                    .execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "WeCom management request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "WeCom management endpoint is unavailable");
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
                        "WeCom rejected the application token",
                        details(operation, response.code(), code, response.headers()));
            }
            if (code == API_PERMISSION_DENIED_CODE || code == CONTACT_PERMISSION_DENIED_CODE) {
                return rejected(
                        ErrorCode._403,
                        "WeCom permission is insufficient",
                        details(operation, response.code(), code, response.headers()));
            }
            if (code == RATE_LIMITED_CODE) {
                return failed(
                        ErrorCode._429,
                        "WeCom management API is rate limited",
                        details(operation, response.code(), code, response.headers()));
            }
            return failed(
                    ErrorCode._502,
                    "WeCom returned an unknown business error",
                    details(operation, response.code(), code, response.headers()));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "WeCom returned an invalid management response");
        }
    }

    /**
     * Maps one non-successful WeCom management API HTTP response using only allow-listed scalar details.
     *
     * @param response  owned non-successful HTTP response
     * @param operation safe Realm operation label
     * @param <T>       expected success type
     * @return classified rejection or operational failure
     */
    private <T> Outcome<T> httpFailure(final Response response, final String operation) {
        final int status = response.code();
        final Map<String, JsonValue> details = details(operation, status, null, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "WeChat WeCom management API rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "WeChat WeCom management API rejected the application token", details);
        }
        if (status == Http.Status.FORBIDDEN || status == Http.Status.NOT_FOUND) {
            return rejected(ErrorCode._403, "WeCom management permission is insufficient", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "WeChat WeCom management API is rate limited", details);
        }
        return failed(ErrorCode._502, "WeChat WeCom management API returned an upstream error", details);
    }

    /**
     * Completely reads, validates, de-duplicates, and stable-sorts the W-02 department response.
     *
     * @param access    valid upstream application access
     * @param operation safe Realm operation label
     * @param timeout   shared end-to-end timeout
     * @return complete minimal department projection
     */
    private Outcome<List<Department>> departmentList(
            final Access access,
            final String operation,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = getUnpaged(
                target(Builder.REALM_ORGANIZATIONS),
                access,
                timeout,
                operation,
                Map.of());
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final Map<String, Department> departments = new LinkedHashMap<>();
        for (JsonValue value : requiredArray(success.value(), "department")) {
            final Department department = department(requiredObject(value, "department item"));
            final Department previous = departments.putIfAbsent(department.id(), department);
            if (previous != null && !previous.equals(department)) {
                return failed(ErrorCode._502, "WeChat department response contains a conflicting identifier");
            }
        }
        final List<Department> result = new ArrayList<>(departments.values());
        result.sort(Comparator.comparing(Department::id));
        return Outcome.succeeded(List.copyOf(result));
    }

    /**
     * Completely reads, validates, de-duplicates, and stable-sorts one W-03 department user response.
     *
     * @param access       valid upstream application access
     * @param departmentId stable department identifier
     * @param operation    safe Realm operation label
     * @param timeout      shared end-to-end timeout
     * @return complete minimal user projection for the department
     */
    private Outcome<List<User>> userList(
            final Access access,
            final String departmentId,
            final String operation,
            final Timeout timeout) {
        final Map<String, Object> query = new LinkedHashMap<>();
        query.put("department_id", departmentId);
        query.put("fetch_child", Normal._0);
        final Outcome<JsonValue.ObjectValue> fetched = getUnpaged(
                target(Builder.REALM_USERS),
                access,
                timeout,
                operation,
                query);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final Map<String, User> users = new LinkedHashMap<>();
        for (JsonValue value : requiredArray(success.value(), "userlist")) {
            final User user = user(requiredObject(value, "user item"));
            final User previous = users.putIfAbsent(user.id(), user);
            if (previous != null && !previous.equals(user)) {
                return failed(ErrorCode._502, "WeChat user response contains a conflicting identifier");
            }
        }
        final List<User> result = new ArrayList<>(users.values());
        result.sort(Comparator.comparing(User::id));
        return Outcome.succeeded(List.copyOf(result));
    }

    /**
     * Completely reads, validates, de-duplicates, and stable-sorts the W-05 tag response.
     *
     * @param access    valid upstream application access
     * @param operation safe Realm operation label
     * @param timeout   shared end-to-end timeout
     * @return complete minimal group projection
     */
    private Outcome<List<Group>> groupList(final Access access, final String operation, final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = getUnpaged(
                target(Builder.REALM_GROUPS),
                access,
                timeout,
                operation,
                Map.of());
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        final Map<String, Group> groups = new LinkedHashMap<>();
        for (JsonValue value : requiredArray(success.value(), "taglist")) {
            final Group group = group(requiredObject(value, "tag item"));
            final Group previous = groups.putIfAbsent(group.id(), group);
            if (previous != null && !previous.equals(group)) {
                return failed(ErrorCode._502, "WeChat tag response contains a conflicting identifier");
            }
        }
        final List<Group> result = new ArrayList<>(groups.values());
        result.sort(Comparator.comparing(Group::id));
        return Outcome.succeeded(List.copyOf(result));
    }

    /**
     * Completely reads, validates, de-duplicates, and stable-sorts one W-06 tag-member response.
     *
     * @param access    valid upstream application access
     * @param group     stable parent tag projection
     * @param operation safe Realm operation label
     * @param timeout   shared end-to-end timeout
     * @return complete minimal tag-member projection
     */
    private Outcome<List<Member>> memberList(
            final Access access,
            final Group group,
            final String operation,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = getUnpaged(
                target(Builder.REALM_GROUP_MEMBERS),
                access,
                timeout,
                operation,
                Map.of("tagid", group.id()));
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (!group.name().equals(requiredString(success.value(), "tagname"))) {
            return failed(ErrorCode._502, "WeChat tag-member response returned a different tag name");
        }
        final Map<String, Member> members = new LinkedHashMap<>();
        for (JsonValue value : requiredArray(success.value(), "userlist")) {
            final Member member = member(requiredObject(value, "tag member item"));
            final Member previous = members.putIfAbsent(member.id(), member);
            if (previous != null && !previous.equals(member)) {
                return failed(ErrorCode._502, "WeChat tag-member response contains a conflicting identifier");
            }
        }
        final List<Member> result = new ArrayList<>(members.values());
        result.sort(Comparator.comparing(Member::id));
        return Outcome.succeeded(List.copyOf(result));
    }

    /**
     * Completely replays every visible department user list before one user page can be emitted.
     *
     * @param access    valid upstream application access
     * @param operation safe Realm operation label
     * @param timeout   shared end-to-end timeout
     * @return globally validated user replay with a phase-level fingerprint
     */
    private Outcome<UserReplay> userReplay(final Access access, final String operation, final Timeout timeout) {
        final Outcome<List<Department>> departmentOutcome = departmentList(access, operation, timeout);
        if (!(departmentOutcome instanceof Outcome.Succeeded<List<Department>> departmentSuccess)) {
            return propagate(departmentOutcome);
        }
        final List<Department> departments = departmentSuccess.value();
        final Set<String> departmentIds = new LinkedHashSet<>();
        for (Department department : departments) {
            departmentIds.add(department.id());
        }
        final Map<String, List<User>> usersByDepartment = new LinkedHashMap<>();
        final Map<String, User> usersById = new LinkedHashMap<>();
        final Map<String, Set<String>> memberships = new LinkedHashMap<>();
        for (Department department : departments) {
            final Outcome<List<User>> fetched = userList(access, department.id(), operation, timeout);
            if (!(fetched instanceof Outcome.Succeeded<List<User>> success)) {
                return propagate(fetched);
            }
            final List<User> users = success.value();
            usersByDepartment.put(department.id(), users);
            for (User user : users) {
                if (!user.departmentIds().contains(department.id())
                        || !departmentIds.containsAll(user.departmentIds())) {
                    return failed(
                            ErrorCode._502,
                            "WeChat department user contains an inconsistent visible department projection");
                }
                final User previous = usersById.putIfAbsent(user.id(), user);
                if (previous != null && !previous.equals(user)) {
                    return failed(
                            ErrorCode._502,
                            "WeChat department responses contain conflicting projections for one user key");
                }
                memberships.computeIfAbsent(user.id(), ignored -> new LinkedHashSet<>()).add(department.id());
            }
        }
        for (User user : usersById.values()) {
            if (!memberships.get(user.id()).equals(new LinkedHashSet<>(user.departmentIds()))) {
                return failed(
                        ErrorCode._502,
                        "WeChat user department fields do not match the replayed department memberships");
            }
        }
        return Outcome.succeeded(
                new UserReplay(departments, usersByDepartment, userReplayFingerprint(departments, usersByDepartment)));
    }

    /**
     * Completely replays every visible tag member list before one group-member page can be emitted.
     *
     * @param access    valid upstream application access
     * @param operation safe Realm operation label
     * @param timeout   shared end-to-end timeout
     * @return globally validated member replay with a phase-level fingerprint
     */
    private Outcome<GroupMemberReplay> groupMemberReplay(
            final Access access,
            final String operation,
            final Timeout timeout) {
        final Outcome<List<Group>> groupOutcome = groupList(access, operation, timeout);
        if (!(groupOutcome instanceof Outcome.Succeeded<List<Group>> groupSuccess)) {
            return propagate(groupOutcome);
        }
        final List<Group> groups = groupSuccess.value();
        final Map<String, List<Member>> membersByGroup = new LinkedHashMap<>();
        for (Group group : groups) {
            final Outcome<List<Member>> fetched = memberList(access, group, operation, timeout);
            if (!(fetched instanceof Outcome.Succeeded<List<Member>> success)) {
                return propagate(fetched);
            }
            membersByGroup.put(group.id(), success.value());
        }
        return Outcome.succeeded(
                new GroupMemberReplay(groups, membersByGroup, groupMemberReplayFingerprint(groups, membersByGroup)));
    }

    /**
     * Computes the complete deterministic W-02 department projection fingerprint.
     *
     * @param departments stable sorted department projection
     * @return 64-character lowercase SHA-256 fingerprint
     */
    private String departmentFingerprint(final List<Department> departments) {
        final List<JsonValue> values = new ArrayList<>(departments.size());
        for (Department department : departments) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put("id", number(numericIdentifier(department.id(), "WeChat department identifier")));
            item.put("name", new JsonValue.StringValue(department.name()));
            item.put(
                    "parentid",
                    number(numericIdentifier(department.parentId(), "WeChat department parent identifier")));
            item.put("order", number(department.order()));
            values.add(new JsonValue.ObjectValue(item));
        }
        return fingerprint(new JsonValue.ArrayValue(values));
    }

    /**
     * Computes the complete deterministic W-03 projection fingerprint for every visible department.
     *
     * @param departments       complete stable department projection
     * @param usersByDepartment stable sorted user projection indexed by department identifier
     * @return 64-character lowercase SHA-256 fingerprint
     */
    private String userReplayFingerprint(
            final List<Department> departments,
            final Map<String, List<User>> usersByDepartment) {
        final List<JsonValue> values = new ArrayList<>(departments.size());
        for (Department department : departments) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put("department_id", number(numericIdentifier(department.id(), "WeChat department identifier")));
            final List<JsonValue> users = new ArrayList<>(usersByDepartment.get(department.id()).size());
            for (User user : usersByDepartment.get(department.id())) {
                users.add(userProjection(user));
            }
            item.put("users", new JsonValue.ArrayValue(users));
            values.add(new JsonValue.ObjectValue(item));
        }
        return Builder.sha256Hex(departmentFingerprint(departments) + fingerprint(new JsonValue.ArrayValue(values)));
    }

    /**
     * Computes the complete deterministic W-05 tag projection fingerprint.
     *
     * @param groups stable sorted group projection
     * @return 64-character lowercase SHA-256 fingerprint
     */
    private String groupFingerprint(final List<Group> groups) {
        final List<JsonValue> values = new ArrayList<>(groups.size());
        for (Group group : groups) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put("tagid", number(numericIdentifier(group.id(), "WeChat tag identifier")));
            item.put("tagname", new JsonValue.StringValue(group.name()));
            values.add(new JsonValue.ObjectValue(item));
        }
        return fingerprint(new JsonValue.ArrayValue(values));
    }

    /**
     * Computes the complete deterministic W-06 projection fingerprint for every visible tag.
     *
     * @param groups         complete stable group projection
     * @param membersByGroup stable sorted member projection indexed by group identifier
     * @return 64-character lowercase SHA-256 fingerprint
     */
    private String groupMemberReplayFingerprint(
            final List<Group> groups,
            final Map<String, List<Member>> membersByGroup) {
        final List<JsonValue> values = new ArrayList<>(groups.size());
        for (Group group : groups) {
            final Map<String, JsonValue> item = new LinkedHashMap<>();
            item.put("tagid", number(numericIdentifier(group.id(), "WeChat tag identifier")));
            final List<JsonValue> members = new ArrayList<>(membersByGroup.get(group.id()).size());
            for (Member member : membersByGroup.get(group.id())) {
                members.add(new JsonValue.StringValue(member.id()));
            }
            item.put("members", new JsonValue.ArrayValue(members));
            values.add(new JsonValue.ObjectValue(item));
        }
        return Builder.sha256Hex(groupFingerprint(groups) + fingerprint(new JsonValue.ArrayValue(values)));
    }

    /**
     * Computes a lowercase SHA-256 digest of one canonical minimal JSON projection.
     *
     * @param value canonical JSON projection
     * @return 64-character lowercase SHA-256 fingerprint
     */
    private String fingerprint(final JsonValue value) {
        final byte[] encoded = JsonKit.writeValue(value);
        return Builder.sha256Hex(new String(encoded, Charset.UTF_8));
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
            throw new ValidateException("WeChat Realm manifest omits a required management target");
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
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(WeChatManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(WeChatManifest.EE_ENTERPRISE.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code()));
        final List<JsonValue> kinds = new ArrayList<>(state.kinds().size());
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        envelope.put(Builder.CURSOR_POSITION_FIELD, position(state.phase(), state.position()));
        final byte[] json = JsonKit.writeValue(new JsonValue.ObjectValue(envelope));
        return new Realm.Cursor(Base64.encodeUrlSafe(json));
    }

    /**
     * Decodes, validates, and canonicalizes one WeChat snapshot cursor.
     *
     * @param cursor opaque caller-supplied cursor
     * @param kinds  exact kinds requested by the current snapshot call
     * @return validated finite pagination state
     * @throws ValidateException if encoding, fields, types, context, position, or canonical form is invalid
     */
    private CursorState decode(final Realm.Cursor cursor, final Set<Realm.Kind> kinds) {
        try {
            final byte[] decoded = Base64.decode(cursor.value());
            final JsonValue value = JsonKit.readValue(decoded, Builder.MAXIMUM_REALM_JSON_DEPTH, true);
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
            if (!WeChatManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !WeChatManifest.EE_ENTERPRISE.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredLong(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("WeChat cursor does not belong to this snapshot operation");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("WeChat cursor kinds do not match the snapshot request");
            }
            final Position position = position(phase, requiredObject(envelope, Builder.CURSOR_POSITION_FIELD));
            final CursorState state = new CursorState(phase, decodedKinds, position);
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("WeChat cursor is not in canonical form");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("WeChat cursor is invalid", cause);
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
                JsonKit.readValue(
                        response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES),
                        Builder.MAXIMUM_REALM_JSON_DEPTH,
                        true),
                "WeChat response");
    }

    /**
     * Decodes one complete official unpaged response without a local materialization threshold.
     *
     * @param response owned successful W-02, W-03, W-05, or W-06 response
     * @return decoded top-level JSON object
     */
    private JsonValue.ObjectValue unpagedObject(final Response response) {
        final Buffer buffer = new Buffer();
        Payload.copyTo(response.body().payload(), buffer);
        return requiredObject(
                JsonKit.readValue(buffer.readByteArray(), Builder.MAXIMUM_REALM_JSON_DEPTH, true),
                "WeChat unpaged response");
    }

    /**
     * Defines the complete finite WeChat snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Replays the complete department list and emits organization resources and parent relations.
         */
        DEPARTMENTS(1),

        /**
         * Replays departments and emits canonical users, department membership, and manager relations.
         */
        USERS(2),

        /**
         * Replays the complete tag list and emits group resources.
         */
        GROUPS(3),

        /**
         * Replays tags and their complete member lists to emit group-member relations.
         */
        GROUP_MEMBERS(4);

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
            throw new ValidateException("WeChat cursor contains an unknown phase code");
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
            phase = Assert.notNull(phase, "WeChat cursor phase must not be null");
            Assert.notNull(kinds, "WeChat cursor kinds must not be null");
            final List<Realm.Kind> copy = new ArrayList<>(kinds.size());
            int previous = 0;
            for (Realm.Kind kind : kinds) {
                final Realm.Kind checked = Assert.notNull(kind, "WeChat cursor kind must not be null");
                if (!SUPPORTED_KINDS.contains(checked) || checked.code() <= previous) {
                    throw new ValidateException("WeChat cursor kinds must be supported and in stable code order");
                }
                previous = checked.code();
                copy.add(checked);
            }
            if (copy.isEmpty()) {
                throw new ValidateException("WeChat cursor kinds must not be empty");
            }
            kinds = List.copyOf(copy);
            if ((phase == Phase.DEPARTMENTS && !kinds.contains(Realm.Kind.ORGANIZATION))
                    || (phase == Phase.USERS && !kinds.contains(Realm.Kind.USER))
                    || ((phase == Phase.GROUPS || phase == Phase.GROUP_MEMBERS) && !kinds.contains(Realm.Kind.GROUP))) {
                throw new ValidateException("WeChat cursor phase is not enabled by its requested kinds");
            }
            position = Assert.notNull(position, "WeChat cursor position must not be null");
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
                    : ordered.contains(Realm.Kind.USER) ? Phase.USERS : Phase.GROUPS;
            return new CursorState(phase, ordered, Position.initial(phase));
        }
    }

    /**
     * Carries one recoverable unpaged WeCom phase position.
     *
     * @param offset         next stable projection index
     * @param relationOffset next relation index within the current user projection
     * @param parentId       current stable department or group identifier
     * @param fingerprint    complete lowercase SHA-256 projection fingerprint
     * @author Kimi Liu
     */
    private record Position(int offset, int relationOffset, Optional<String> parentId, Optional<String> fingerprint) {

        /**
         * Validates and normalizes one unpaged replay position.
         *
         * @param offset         next stable projection index
         * @param relationOffset next relation index within the current user projection
         * @param parentId       current stable parent identifier
         * @param fingerprint    complete projection fingerprint
         */
        private Position {
            if (offset < 0 || relationOffset < 0) {
                throw new ValidateException("WeChat cursor offsets must not be negative");
            }
            parentId = optionalText(parentId, "WeChat cursor parent identifier");
            fingerprint = optionalText(fingerprint, "WeChat cursor fingerprint");
            if (fingerprint.isPresent() && !fingerprint(fingerprint.getOrNull())) {
                throw new ValidateException("WeChat cursor fingerprint must be lowercase SHA-256 hexadecimal");
            }
        }

        /**
         * Creates the empty position required by one initial finite phase.
         *
         * @param phase initial finite phase
         * @return validated empty phase position
         */
        private static Position initial(final Phase phase) {
            Assert.notNull(phase, "WeChat initial cursor phase must not be null");
            return unpaged(0, 0, null, null);
        }

        /**
         * Creates one canonical unpaged replay position.
         *
         * @param offset         next stable projection index
         * @param relationOffset next relation index within the current user
         * @param parentId       current stable parent identifier, or {@code null}
         * @param fingerprint    complete projection fingerprint, or {@code null} before the first output
         * @return validated unpaged position
         */
        private static Position unpaged(
                final int offset,
                final int relationOffset,
                final String parentId,
                final String fingerprint) {
            return new Position(offset, relationOffset, Optional.ofNullable(parentId),
                    Optional.ofNullable(fingerprint));
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
         * Verifies phase-specific ownership of the four fixed unpaged fields.
         *
         * @param phase exact finite snapshot phase
         */
        private void validate(final Phase phase) {
            switch (phase) {
                case DEPARTMENTS, GROUPS -> {
                    if (parentId.isPresent() || relationOffset != 0 || (offset == 0) == fingerprint.isPresent()) {
                        throw new ValidateException("WeChat global unpaged cursor position is inconsistent");
                    }
                }
                case USERS, GROUP_MEMBERS -> {
                    if (parentId.isEmpty()) {
                        if (offset != 0 || relationOffset != 0 || fingerprint.isPresent()) {
                            throw new ValidateException("WeChat initial parent replay position is inconsistent");
                        }
                    } else if (fingerprint.isEmpty() && (offset != 0 || relationOffset != 0)) {
                        throw new ValidateException("WeChat parent replay cursor omits its fingerprint");
                    }
                    if (phase == Phase.GROUP_MEMBERS && relationOffset != 0) {
                        throw new ValidateException("WeChat group-member cursor must not contain a relation offset");
                    }
                }
            }
        }
    }

    /**
     * Carries one fully validated W-03 replay without retaining an upstream response object.
     *
     * @param departments complete stable sorted department projection
     * @param users       complete stable sorted users indexed by department identifier
     * @param fingerprint complete lowercase SHA-256 phase fingerprint
     * @author Kimi Liu
     */
    private record UserReplay(List<Department> departments, Map<String, List<User>> users, String fingerprint) {

        /**
         * Freezes one complete user replay and verifies its department index closure.
         *
         * @param departments complete stable sorted department projection
         * @param users       complete stable sorted users indexed by department identifier
         * @param fingerprint complete lowercase SHA-256 phase fingerprint
         */
        private UserReplay {
            departments = List.copyOf(Assert.notNull(departments, "WeChat user replay departments must not be null"));
            Assert.notNull(users, "WeChat user replay index must not be null");
            final Map<String, List<User>> copy = new LinkedHashMap<>();
            for (Department department : departments) {
                final List<User> values = users.get(department.id());
                if (values == null) {
                    throw new ValidateException("WeChat user replay omits a department projection");
                }
                copy.put(department.id(), List.copyOf(values));
            }
            if (copy.size() != users.size()) {
                throw new ValidateException("WeChat user replay contains an unknown department projection");
            }
            users = Collections.unmodifiableMap(copy);
            fingerprint = requireText(fingerprint, "WeChat user replay fingerprint");
            if (!Position.fingerprint(fingerprint)) {
                throw new ValidateException("WeChat user replay fingerprint must be lowercase SHA-256 hexadecimal");
            }
        }
    }

    /**
     * Carries one fully validated W-06 replay without retaining an upstream response object.
     *
     * @param groups      complete stable sorted group projection
     * @param members     complete stable sorted members indexed by group identifier
     * @param fingerprint complete lowercase SHA-256 phase fingerprint
     * @author Kimi Liu
     */
    private record GroupMemberReplay(List<Group> groups, Map<String, List<Member>> members, String fingerprint) {

        /**
         * Freezes one complete group-member replay and verifies its group index closure.
         *
         * @param groups      complete stable sorted group projection
         * @param members     complete stable sorted members indexed by group identifier
         * @param fingerprint complete lowercase SHA-256 phase fingerprint
         */
        private GroupMemberReplay {
            groups = List.copyOf(Assert.notNull(groups, "WeChat member replay groups must not be null"));
            Assert.notNull(members, "WeChat member replay index must not be null");
            final Map<String, List<Member>> copy = new LinkedHashMap<>();
            for (Group group : groups) {
                final List<Member> values = members.get(group.id());
                if (values == null) {
                    throw new ValidateException("WeChat member replay omits a group projection");
                }
                copy.put(group.id(), List.copyOf(values));
            }
            if (copy.size() != members.size()) {
                throw new ValidateException("WeChat member replay contains an unknown group projection");
            }
            members = Collections.unmodifiableMap(copy);
            fingerprint = requireText(fingerprint, "WeChat member replay fingerprint");
            if (!Position.fingerprint(fingerprint)) {
                throw new ValidateException("WeChat member replay fingerprint must be lowercase SHA-256 hexadecimal");
            }
        }
    }

    /**
     * Minimal non-sensitive WeChat department projection.
     *
     * @param id       stable department identifier
     * @param name     exact department display name
     * @param parentId stable parent department identifier
     * @param order    official non-negative department display order
     * @author Kimi Liu
     */
    private record Department(String id, String name, String parentId, long order) {

        /**
         * Validates one minimal department projection.
         *
         * @param id       stable department identifier
         * @param name     exact display name
         * @param parentId stable parent identifier
         * @param order    official non-negative display order
         */
        private Department {
            id = requireText(id, "WeChat department identifier");
            name = requireText(name, "WeChat department name");
            parentId = requireText(parentId, "WeChat department parent identifier");
            if (id.equals(parentId)) {
                throw new ValidateException("WeChat department must not be its own parent");
            }
            if (order < 0L) {
                throw new ValidateException("WeChat department order must not be negative");
            }
        }
    }

    /**
     * Minimal non-sensitive WeChat user projection.
     *
     * @param id            stable user identifier
     * @param name          exact user display name
     * @param status        official positive user status code
     * @param departmentIds stable department identifiers in canonical order
     * @param managerIds    stable manager user identifiers in canonical order
     * @author Kimi Liu
     */
    private record User(String id, String name, long status, List<String> departmentIds, List<String> managerIds) {

        /**
         * Validates and normalizes one minimal user projection.
         *
         * @param id            stable user identifier
         * @param name          exact display name
         * @param status        official positive user status code
         * @param departmentIds stable department identifiers
         * @param managerIds    stable manager identifiers
         */
        private User {
            id = requireText(id, "WeChat user identifier");
            name = requireText(name, "WeChat user name");
            if (status <= 0L) {
                throw new ValidateException("WeChat user status must be positive");
            }
            Assert.notNull(departmentIds, "WeChat user department identifiers must not be null");
            final LinkedHashSet<String> uniqueDepartments = new LinkedHashSet<>();
            for (String departmentId : departmentIds) {
                uniqueDepartments.add(requireText(departmentId, "WeChat user department identifier"));
            }
            if (uniqueDepartments.isEmpty()) {
                throw new ValidateException("WeChat user must belong to at least one visible department");
            }
            final List<String> orderedDepartments = new ArrayList<>(uniqueDepartments);
            orderedDepartments.sort(String::compareTo);
            departmentIds = List.copyOf(orderedDepartments);
            Assert.notNull(managerIds, "WeChat user manager identifiers must not be null");
            final LinkedHashSet<String> uniqueManagers = new LinkedHashSet<>();
            for (String managerId : managerIds) {
                final String checked = requireText(managerId, "WeChat user manager identifier");
                if (id.equals(checked)) {
                    throw new ValidateException("WeChat user must not manage itself");
                }
                uniqueManagers.add(checked);
            }
            final List<String> orderedManagers = new ArrayList<>(uniqueManagers);
            orderedManagers.sort(String::compareTo);
            managerIds = List.copyOf(orderedManagers);
        }
    }

    /**
     * Minimal non-sensitive WeChat group projection.
     *
     * @param id   stable group identifier
     * @param name exact group display name
     * @author Kimi Liu
     */
    private record Group(String id, String name) {

        /**
         * Validates one minimal group projection.
         *
         * @param id   stable group identifier
         * @param name exact display name
         */
        private Group {
            id = requireText(id, "WeChat group identifier");
            name = requireText(name, "WeChat group name");
        }
    }

    /**
     * Minimal non-sensitive WeChat group-member projection.
     *
     * @param id stable user identifier returned as the member identifier
     * @author Kimi Liu
     */
    private record Member(String id) {

        /**
         * Validates one minimal group-member projection.
         *
         * @param id stable member user identifier
         */
        private Member {
            id = requireText(id, "WeChat group member identifier");
        }
    }

    /**
     * Holds one short-lived WeChat application token inside the Source-private cache.
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
            token = requireText(token, "WeChat application access token");
            if (expiresAtMillis < 0L) {
                throw new ValidateException("WeChat application access expiration must not be negative");
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
