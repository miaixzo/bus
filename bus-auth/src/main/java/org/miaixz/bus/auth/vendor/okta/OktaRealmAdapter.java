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
package org.miaixz.bus.auth.vendor.okta;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.FabricX.UrlBuilder;
import org.miaixz.bus.auth.Realm;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.auth.worker.loader.KeyLoader;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.nimble.MemoryCache;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the provider-neutral Okta service-app realm surface.
 * <p>
 * Snapshot pagination advances through five finite phases in this order: users, user role assignments, groups, group
 * members, and group role assignments. Official validated Link URLs drive top-level and member pagination. Role phases
 * replay their user or group parent collections from the fixed beginning, persist only the stable current parent and
 * relation offset, and may repeat an equal role resource on later pages as declared by the Variant limitation. The
 * service-app client assertion is signed through the shared Bus JOSE service, while externally loaded private-key
 * material remains local to the synchronous signing method.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OktaRealmAdapter implements VendorAdapter {

    /**
     * Empty immutable JSON object used where the frozen mapping exposes no attributes.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by Okta management.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set.of(Realm.Kind.USER, Realm.Kind.GROUP, Realm.Kind.ROLE);

    /**
     * Ordered management-target key closure required from the management manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.ENTERPRISE_USERS,
            Builder.ENTERPRISE_USER,
            Builder.ENTERPRISE_GROUPS,
            Builder.ENTERPRISE_GROUP,
            Builder.ENTERPRISE_GROUP_MEMBERS,
            Builder.ENTERPRISE_ROLES,
            Builder.ENTERPRISE_ROLE_MEMBERS,
            Builder.ENTERPRISE_ROLE_ASSIGNMENTS);

    /**
     * Protected JOSE type value required for the service-app client assertion.
     */
    private static final String JWT_TYPE = "JWT";

    /**
     * Selected immutable Okta management Variant.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated Okta management deployment options.
     */
    private final OktaOptions options;

    /**
     * Caller-owned execution services used without taking lifecycle ownership.
     */
    private final DriverServices services;

    /**
     * Resolved token and Management API endpoints declared by the selected manifest.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Source-private cache containing only a short-lived service-app access token.
     */
    private final CacheX<String, Access> accessCache;

    /**
     * Shared Bus JWS implementation constrained to the sole RS256 algorithm.
     */
    private final JwsService jwsService;

    /**
     * Creates one Source-isolated Okta management realm adapter.
     *
     * @param spaceId  registration space used for key isolation
     * @param sourceId registered Source identifier used for ownership validation
     * @param manifest exact Okta manifest
     * @param variant  exact selected management Variant
     * @param options  validated management options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if manifest, Variant, protocol, targets, or options are inconsistent
     */
    public OktaRealmAdapter(final String spaceId, final String sourceId, final OktaManifest manifest,
            final VariantManifest.Variant variant, final OktaOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "Okta management space id must not be blank");
        Assert.notBlank(sourceId, "Okta management Source id must not be blank");
        final OktaManifest selectedManifest = Assert.notNull(manifest, "Okta manifest must not be null");
        this.variant = Assert.notNull(variant, "Okta management Variant must not be null");
        this.options = Assert.notNull(options, "Okta management options must not be null");
        this.services = Assert.notNull(services, "Okta management services must not be null");
        if (!OktaManifest.ID.equals(selectedManifest.vendor())
                || !OktaManifest.MANAGEMENT.equals(this.variant.variant())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || !OktaManifest.ID.equals(this.variant.platform()) || this.variant.protocol() != Protocol.HTTPS
                || !OktaManifest.ID.equals(this.options.vendor())
                || !OktaManifest.MANAGEMENT.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || this.options.credential().type() != org.miaixz.bus.auth.Credential.Type.PRIVATE_KEY
                || !this.variant.defaultScopes().equals(this.options.scopes())) {
            throw new ValidateException("Okta realm adapter requires the frozen management HTTPS Variant");
        }
        this.services.securityBaseline().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (this.targets.token().isEmpty()
                || !List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("Okta management manifest has an invalid management target set");
        }
        this.accessCache = new MemoryCache<>(FabricX.clock(this.services.fabric())::millis);
        this.jwsService = new JwsService(this.services.jsonProvider(),
                this.services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RS256.name()));
    }

    /**
     * Reports whether one authenticated operation was rejected for an invalid service-app token.
     *
     * @param outcome completed authenticated operation outcome
     * @return whether the outcome carries the shared 401 error
     */
    private static boolean unauthorized(final Outcome<?> outcome) {
        return outcome instanceof Outcome.Rejected<?> rejected && ErrorCode._401.equals(rejected.failure().error());
    }

    /**
     * Reports whether one Link parameter section contains the exact {@code next} relation token.
     *
     * @param parameters Link parameters following an angle-bracket target
     * @return whether the parameter closure declares {@code rel=next}
     */
    private static boolean nextRelation(final String parameters) {
        for (String parameter : parameters.split(";")) {
            final String normalized = StringKit.trim(parameter).toLowerCase(Locale.ROOT);
            if ("rel=next".equals(normalized) || "rel=\"next\"".equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies one continuation path against its exact top-level or dependent collection shape.
     *
     * @param candidate candidate normalized path
     * @param base      manifest-owned collection base path
     * @param phase     owning collection phase
     * @return whether the path belongs to the phase
     */
    private static boolean paginationPath(final String candidate, final String base, final Phase phase) {
        if (phase == Phase.USERS || phase == Phase.GROUPS) {
            return base.equals(candidate);
        }
        if (phase != Phase.GROUP_MEMBERS || !candidate.startsWith(base + "/") || !candidate.endsWith("/users")) {
            return false;
        }
        final String parent = candidate.substring(base.length() + 1, candidate.length() - "/users".length());
        return !parent.isEmpty() && parent.indexOf('/') < 0;
    }

    /**
     * Converts one minimal Okta user projection to a provider-neutral resource.
     *
     * @param user       validated user projection
     * @param observedAt shared observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()), orderedIdentifiers("login", user.login()),
                user.displayName(), user.state(), EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one minimal Okta group projection to a provider-neutral resource.
     *
     * @param group      validated group projection
     * @param observedAt shared observation instant
     * @return immutable group resource
     */
    private static Realm.Resource groupResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.GROUP, group.id()), Map.of(), group.name(),
                Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one minimal Okta administrator role to a provider-neutral resource.
     *
     * @param role       validated role projection
     * @param observedAt shared observation instant
     * @return immutable role resource
     */
    private static Realm.Resource roleResource(final Role role, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ROLE, role.id()), Map.of(), role.label(),
                Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one user membership relation to an Okta group.
     *
     * @param userId     stable user identifier
     * @param targetKind normalized group category
     * @param targetId   stable target identifier
     * @param observedAt shared observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(
            final String userId,
            final Realm.Kind targetKind,
            final String targetId,
            final Instant observedAt) {
        return relation(Realm.RelationKind.MEMBER, Realm.Kind.USER, userId, targetKind, targetId, observedAt);
    }

    /**
     * Creates one user- or group-to-role membership relation from an official role assignment.
     *
     * @param parentKind normalized assigned-resource kind
     * @param parentId   stable assigned-resource identifier
     * @param assignment validated role assignment projection
     * @param observedAt shared observation instant
     * @return immutable role-member relation
     */
    private static Realm.Relation roleMemberRelation(
            final Realm.Kind parentKind,
            final String parentId,
            final RoleAssignment assignment,
            final Instant observedAt) {
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        attributes.put("assignment_id", new JsonValue.StringValue(assignment.assignmentId()));
        return relation(
                Realm.RelationKind.ROLE_MEMBER,
                parentKind,
                parentId,
                Realm.Kind.ROLE,
                assignment.role().id(),
                new JsonValue.ObjectValue(attributes),
                observedAt);
    }

    /**
     * Creates one provider-neutral relation without allow-listed attributes.
     *
     * @param relationKind normalized relation category
     * @param fromKind     normalized source resource category
     * @param fromId       stable source identifier
     * @param toKind       normalized target resource category
     * @param toId         stable target identifier
     * @param observedAt   shared observation instant
     * @return immutable provider-neutral relation
     */
    private static Realm.Relation relation(
            final Realm.RelationKind relationKind,
            final Realm.Kind fromKind,
            final String fromId,
            final Realm.Kind toKind,
            final String toId,
            final Instant observedAt) {
        return relation(relationKind, fromKind, fromId, toKind, toId, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one provider-neutral relation with exact allow-listed attributes.
     *
     * @param relationKind normalized relation category
     * @param fromKind     normalized source resource category
     * @param fromId       stable source identifier
     * @param toKind       normalized target resource category
     * @param toId         stable target identifier
     * @param attributes   immutable allow-listed relation attributes
     * @param observedAt   shared observation instant
     * @return immutable provider-neutral relation
     */
    private static Realm.Relation relation(
            final Realm.RelationKind relationKind,
            final Realm.Kind fromKind,
            final String fromId,
            final Realm.Kind toKind,
            final String toId,
            final JsonValue.ObjectValue attributes,
            final Instant observedAt) {
        return new Realm.Relation(
                new Realm.RelationKey(relationKind, new Realm.Key(fromKind, fromId), new Realm.Key(toKind, toId)),
                attributes, observedAt);
    }

    /**
     * Creates one single-entry ordered identifier map.
     *
     * @param name  normalized identifier name
     * @param value exact identifier value
     * @return immutable ordered identifier map
     */
    private static Map<String, String> orderedIdentifiers(final String name, final String value) {
        final Map<String, String> identifiers = new LinkedHashMap<>();
        identifiers.put(name, value);
        return Collections.unmodifiableMap(identifiers);
    }

    /**
     * Adds one page resource, de-duplicating equal values and rejecting conflicting stable keys.
     *
     * @param resources page-local resource map
     * @param resource  normalized resource
     * @param label     safe projection label
     */
    private static void putResource(
            final Map<Realm.Key, Realm.Resource> resources,
            final Realm.Resource resource,
            final String label) {
        final Realm.Resource previous = resources.putIfAbsent(resource.key(), resource);
        if (previous != null && !previous.equals(resource)) {
            throw new ValidateException(label + " contains a conflicting resource key");
        }
    }

    /**
     * Adds one page relation, de-duplicating equal values and rejecting conflicting stable keys.
     *
     * @param relations page-local relation map
     * @param relation  normalized relation
     * @param label     safe projection label
     */
    private static void putRelation(
            final Map<Realm.RelationKey, Realm.Relation> relations,
            final Realm.Relation relation,
            final String label) {
        final Realm.Relation previous = relations.putIfAbsent(relation.key(), relation);
        if (previous != null && !previous.equals(relation)) {
            throw new ValidateException(label + " contains a conflicting relation key");
        }
    }

    /**
     * Parses one minimal Okta user projection.
     *
     * @param value decoded user object
     * @return validated minimal user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        final JsonValue.ObjectValue profile = requiredObject(value, "profile");
        final String login = requiredString(profile, "login");
        final String displayName = fallbackString(profile, "displayName", login);
        return new User(requiredIdentifier(value, "id"), login, displayName,
                userState(requiredString(value, "status")));
    }

    /**
     * Parses one minimal Okta group projection.
     *
     * @param value decoded group object
     * @return validated minimal group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredIdentifier(value, "id"), requiredString(requiredObject(value, "profile"), "name"));
    }

    /**
     * Parses one minimal Okta group-member projection.
     *
     * @param value decoded member object
     * @return validated minimal member projection
     */
    private static Member member(final JsonValue.ObjectValue value) {
        return new Member(requiredIdentifier(value, "id"));
    }

    /**
     * Parses one minimal Okta administrator-role projection.
     *
     * @param value decoded role object
     * @return validated minimal role projection
     */
    private static Role role(final JsonValue.ObjectValue value) {
        final String assignmentId = requiredIdentifier(value, "id");
        final Optional<String> type = optionalString(value, "type");
        final String id = type.isPresent() && !"CUSTOM".equals(type.getOrNull()) ? type.getOrNull() : assignmentId;
        return new Role(id, requiredString(value, "label"));
    }

    /**
     * Parses one minimal Okta role-assignment projection.
     *
     * @param value decoded role-assignment object
     * @return validated minimal role-assignment projection
     */
    private static RoleAssignment roleAssignment(final JsonValue.ObjectValue value) {
        return new RoleAssignment(requiredIdentifier(value, "id"), role(value));
    }

    /**
     * Maps one exact Okta user status to the normalized enterprise state closure.
     *
     * @param status exact upstream status
     * @return normalized active, inactive, or unknown state
     */
    private static Realm.State userState(final String status) {
        return switch (status) {
            case "ACTIVE" -> Realm.State.ACTIVE;
            case "STAGED", "PROVISIONED", "RECOVERY", "PASSWORD_EXPIRED", "LOCKED_OUT", "DEPROVISIONED", "SUSPENDED" -> Realm.State.INACTIVE;
            default -> Realm.State.UNKNOWN;
        };
    }

    /**
     * Selects the next declared finite phase enabled by the requested kinds.
     *
     * @param current completed phase
     * @param kinds   exact requested kinds in stable code order
     * @return next enabled phase or {@code null}
     */
    private static Phase nextPhase(final Phase current, final List<Realm.Kind> kinds) {
        Phase candidate = current.next();
        while (candidate != null && !kinds.contains(candidate.kind())) {
            candidate = candidate.next();
        }
        return candidate;
    }

    /**
     * Encodes the exact position member closure owned by one finite phase.
     *
     * @param phase    finite snapshot phase
     * @param position validated phase position
     * @return immutable ordered JSON position object
     */
    private static JsonValue.ObjectValue position(final Phase phase, final Position position) {
        position.validate(phase);
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        if (phase == Phase.USER_ROLES || phase == Phase.GROUP_ROLES) {
            values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
            values.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(position.relationOffset()));
        } else if (phase == Phase.GROUP_MEMBERS) {
            values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
            values.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(position.parentId()));
        } else {
            values.put(Builder.CURSOR_NEXT_FIELD, nullable(position.next()));
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Decodes the exact position shape owned by one finite phase.
     *
     * @param phase finite snapshot phase
     * @param value decoded JSON position object
     * @return validated internal position
     */
    private static Position position(final Phase phase, final JsonValue.ObjectValue value) {
        final Position position;
        if (phase == Phase.USER_ROLES || phase == Phase.GROUP_ROLES) {
            exactMembers(
                    value,
                    Set.of(Builder.CURSOR_PARENT_ID_FIELD, Builder.CURSOR_RELATION_OFFSET_FIELD),
                    "Okta role-assignment cursor position");
            position = Position.parent(
                    nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                    null,
                    nonNegativeInt(value, Builder.CURSOR_RELATION_OFFSET_FIELD));
        } else if (phase == Phase.GROUP_MEMBERS) {
            exactMembers(
                    value,
                    Set.of(Builder.CURSOR_NEXT_FIELD, Builder.CURSOR_PARENT_ID_FIELD),
                    "Okta group-member cursor position");
            position = Position.parent(
                    nullableString(value, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                    nullableString(value, Builder.CURSOR_NEXT_FIELD).getOrNull(),
                    0);
        } else {
            exactMembers(value, Set.of(Builder.CURSOR_NEXT_FIELD), "Okta Link cursor position");
            position = new Position(nullableString(value, Builder.CURSOR_NEXT_FIELD), Optional.empty(), 0);
        }
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
                throw new ValidateException("Okta cursor kind must be an integer code");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Okta cursor kind code is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !kinds.add(kind)) {
                throw new ValidateException("Okta cursor kinds are not in canonical code order");
            }
            previous = code;
        }
        if (kinds.isEmpty()) {
            throw new ValidateException("Okta cursor kinds must not be empty");
        }
        return List.copyOf(kinds);
    }

    /**
     * Resolves one stable enterprise kind code.
     *
     * @param code persisted kind code
     * @return exact enterprise kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : Realm.Kind.values()) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("Okta cursor contains an unknown kind code");
    }

    /**
     * Validates one requested kind set against the fixed Okta management closure.
     *
     * @param kinds caller-requested resource kinds
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (!SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("Okta snapshot contains an unsupported resource kind");
        }
    }

    /**
     * Reads one required JSON object member.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return required object member
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        return requiredObject(object.values().get(name), name);
    }

    /**
     * Narrows one decoded JSON value to a required object.
     *
     * @param value decoded JSON value
     * @param label safe field label
     * @return required object
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
     * @param name   member name
     * @return immutable array values
     */
    private static List<JsonValue> requiredArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array");
        }
        return array.values();
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded parent object
     * @param name   member name
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
     * Reads one required stable identifier represented as a string or exact integral JSON number.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return canonical lexical identifier
     */
    private static String requiredIdentifier(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.StringValue text) {
            return requireText(text.value(), name);
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                final long identifier = number.value().longValueExact();
                if (identifier < 0L) {
                    throw new ValidateException(name + " must not be a negative identifier");
                }
                return Long.toString(identifier);
            } catch (ArithmeticException cause) {
                throw new ValidateException(name + " must be an exact integral identifier", cause);
            }
        }
        throw new ValidateException(name + " must be a string or integral identifier");
    }

    /**
     * Reads one optional non-blank JSON string as empty when absent, null, or empty.
     *
     * @param object decoded parent object
     * @param name   member name
     * @return optional original string
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue object, final String name) {
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
     * Reads one optional display string and uses its declared fallback for null, empty, or whitespace-only values.
     *
     * @param object   decoded parent object
     * @param name     display member name
     * @param fallback validated fallback value
     * @return original valid display string or the declared fallback
     */
    private static String fallbackString(final JsonValue.ObjectValue object, final String name, final String fallback) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return fallback;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string or null");
        }
        return StringKit.hasText(text.value()) ? requireText(text.value(), name) : fallback;
    }

    /**
     * Reads one canonical optional cursor string whose absence is explicit JSON null.
     *
     * @param object decoded cursor object
     * @param name   member name
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
     * Reads one required exact JSON long member.
     *
     * @param object decoded parent object
     * @param name   member name
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
     * @param name   member name
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
     * Reads one exact JSON int member.
     *
     * @param object decoded parent object
     * @param name   member name
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
     * Reads one non-negative JSON int member.
     *
     * @param object decoded parent object
     * @param name   member name
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
     * Verifies an object's exact member-name closure.
     *
     * @param object   decoded object
     * @param expected exact member set
     * @param label    safe object label
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
     * Converts one optional string into a JSON string or explicit null.
     *
     * @param value optional string
     * @return JSON string or null
     */
    private static JsonValue nullable(final Optional<String> value) {
        return value.isPresent() ? new JsonValue.StringValue(value.getOrNull()) : JsonValue.NullValue.instance();
    }

    /**
     * Creates one exact JSON integer.
     *
     * @param value integral value
     * @return immutable JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Validates required text without silently trimming it.
     *
     * @param value caller or upstream supplied text
     * @param label safe semantic label
     * @return original validated text
     */
    private static String requireText(final String value, final String label) {
        final String text;
        try {
            text = Assert.notBlank(value, label + " must not be blank");
        } catch (IllegalArgumentException cause) {
            throw new ValidateException(label + " must not be blank", cause);
        }
        if (!text.equals(StringKit.trim(text))) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return text;
    }

    /**
     * Narrows one delegated outcome through the declared capability response type.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared response class
     * @param <S>          expected success type
     * @return type-safe delegated outcome
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "Okta delegated operation returned an unsupported outcome");
        });
    }

    /**
     * Propagates one non-success outcome across an internal type boundary.
     *
     * @param outcome outcome that must not contain a success value
     * @param <T>     target success type
     * @return original rejection or failure
     */
    private static <T> Outcome<T> propagate(final Outcome<?> outcome) {
        return switch (outcome) {
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "Okta internal outcome could not be propagated");
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
     * Creates a missing-capability rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> missing() {
        return completed(
                rejected(ErrorCode._400, "Okta enterprise capability is not declared by the selected manifest"));
    }

    /**
     * Creates a request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(
                rejected(ErrorCode._400, "Okta enterprise request does not match the selected capability contract"));
    }

    /**
     * Creates one expected rejection without structured details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final Errors code, final String description) {
        return rejected(code, description, Map.of());
    }

    /**
     * Creates one expected rejection with safe details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param details     allow-listed scalar details
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
     * Creates one operational failure without structured details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates one operational failure with safe details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param details     allow-listed scalar details
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
     * Routes one exact Okta management capability and request type.
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
        Assert.notNull(capability, "Okta enterprise capability must not be null");
        Assert.notNull(context, "Okta enterprise context must not be null");
        Assert.notNull(timeout, "Okta enterprise timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.equals(Realm.describe(OktaManifest.ID)) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(OktaManifest.enterpriseDescription())));
        }
        if (capability.equals(Realm.snapshot(OktaManifest.ID)) && request instanceof Realm.Snapshot snapshot) {
            return narrow(snapshot(snapshot, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.retrieve(OktaManifest.ID)) && request instanceof Realm.Retrieve retrieve) {
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
     * @param context immutable context used only for key loading
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
            return completed(rejected(ErrorCode._400, "Okta management snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Okta management snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> snapshot(access, request, state, observedAt, timeout));
    }

    /**
     * Dispatches one validated snapshot state to its finite Management API phase.
     *
     * @param access     valid service-app Okta access
     * @param request    validated snapshot request
     * @param state      canonical snapshot cursor state
     * @param observedAt shared invocation observation instant
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
                case USERS -> users(access, request, state, observedAt, timeout);
                case USER_ROLES, GROUP_ROLES -> roleAssignments(access, request, state, observedAt, timeout);
                case GROUPS -> groups(access, request, state, observedAt, timeout);
                case GROUP_MEMBERS -> groupMembers(access, request, state, observedAt, timeout);
            };
        } catch (ValidateException ignored) {
            return failed(ErrorCode._502, "Okta Management API returned an invalid snapshot projection");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Okta management snapshot processing failed locally");
        }
    }

    /**
     * Reads one official Okta user page from the fixed beginning or a validated Link continuation.
     *
     * @param access     valid service-app access
     * @param request    validated snapshot request
     * @param state      user page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded user resource page
     */
    private Outcome<Realm.Page> users(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final int limit = Math.min(request.limit(), Normal._200);
        final Url requestUrl = state.position().next().isPresent()
                ? pagination(state.position().next().getOrNull(), Phase.USERS)
                : url(Builder.ENTERPRISE_USERS, null, Map.of("limit", Integer.toString(limit)));
        final Outcome<WirePage<User>> fetched = wirePage(
                requestUrl,
                access,
                timeout,
                "snapshot",
                Phase.USERS,
                OktaRealmAdapter::user);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > limit) {
            return failed(ErrorCode._502, "Okta users response exceeds the requested page size");
        }
        final Map<Realm.Key, Realm.Resource> unique = new LinkedHashMap<>();
        for (User user : success.value().items()) {
            putResource(unique, userResource(user, observedAt), "Okta user page");
        }
        return continuedOrCompleted(List.copyOf(unique.values()), List.of(), state, success.value().next());
    }

    /**
     * Reads one official group collection page.
     *
     * @param access     valid service-app Okta access
     * @param request    validated snapshot request
     * @param state      group page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded group resource page
     */
    private Outcome<Realm.Page> groups(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final int limit = Math.min(request.limit(), Normal._200);
        final Url requestUrl = state.position().next().isPresent()
                ? pagination(state.position().next().getOrNull(), Phase.GROUPS)
                : url(Builder.ENTERPRISE_GROUPS, null, Map.of("limit", Integer.toString(limit)));
        final Outcome<WirePage<Group>> fetched = wirePage(
                requestUrl,
                access,
                timeout,
                "snapshot",
                Phase.GROUPS,
                OktaRealmAdapter::group);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > limit) {
            return failed(ErrorCode._502, "Okta groups response exceeds the requested page size");
        }
        final Map<Realm.Key, Realm.Resource> unique = new LinkedHashMap<>();
        for (Group group : success.value().items()) {
            putResource(unique, groupResource(group, observedAt), "Okta group page");
        }
        return continuedOrCompleted(List.copyOf(unique.values()), List.of(), state, success.value().next());
    }

    /**
     * Reads one official member page for the replayed current group.
     *
     * @param access     valid service-app Okta access
     * @param request    validated snapshot request
     * @param state      group-member page state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded group-member relation page
     */
    private Outcome<Realm.Page> groupMembers(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<String>> parent = parent(
                access,
                Phase.GROUP_MEMBERS,
                state.position().parentId(),
                false,
                "snapshot",
                timeout);
        if (!(parent instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
            return propagate(parent);
        }
        if (parentSuccess.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state);
        }
        final String groupId = parentSuccess.value().getOrNull();
        final int limit = Math.min(request.limit(), Normal._200);
        final Url initialUrl = url(
                Builder.ENTERPRISE_GROUP_MEMBERS,
                List.of(groupId, "users"),
                Map.of("limit", Integer.toString(limit)));
        final Url requestUrl = state.position().next().isPresent()
                ? pagination(state.position().next().getOrNull(), Phase.GROUP_MEMBERS)
                : initialUrl;
        if (!requestUrl.path().equals(initialUrl.path())) {
            return failed(ErrorCode._502, "Okta group-member continuation does not belong to its replay parent");
        }
        final Outcome<WirePage<Member>> fetched = wirePage(
                requestUrl,
                access,
                timeout,
                "snapshot",
                Phase.GROUP_MEMBERS,
                OktaRealmAdapter::member);
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Member>> success)) {
            return propagate(fetched);
        }
        if (success.value().items().size() > limit) {
            return failed(ErrorCode._502, "Okta group members response exceeds the requested page size");
        }
        final Map<Realm.RelationKey, Realm.Relation> unique = new LinkedHashMap<>();
        for (Member member : success.value().items()) {
            putRelation(
                    unique,
                    memberRelation(member.id(), Realm.Kind.GROUP, groupId, observedAt),
                    "Okta group-member page");
        }
        if (success.value().next().isPresent()) {
            return page(
                    List.of(),
                    List.copyOf(unique.values()),
                    state,
                    Position.parent(groupId, success.value().next().getOrNull(), 0));
        }
        final Outcome<Optional<String>> following = parent(
                access,
                Phase.GROUP_MEMBERS,
                Optional.of(groupId),
                true,
                "snapshot",
                timeout);
        if (!(following instanceof Outcome.Succeeded<Optional<String>> followingSuccess)) {
            return propagate(following);
        }
        if (followingSuccess.value().isPresent()) {
            return page(
                    List.of(),
                    List.copyOf(unique.values()),
                    state,
                    Position.parent(followingSuccess.value().getOrNull(), null, 0));
        }
        return completedPhase(List.of(), List.copyOf(unique.values()), state);
    }

    /**
     * Replays the current user or group parent and reads its bounded role-assignment array.
     *
     * @param access     valid service-app access
     * @param request    validated snapshot request
     * @param state      user-role or group-role phase state
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return one bounded role-resource and role-member relation page
     */
    private Outcome<Realm.Page> roleAssignments(
            final Access access,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<String>> parent = parent(
                access,
                state.phase(),
                state.position().parentId(),
                false,
                "snapshot",
                timeout);
        if (!(parent instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
            return propagate(parent);
        }
        if (parentSuccess.value().isEmpty()) {
            return completedPhase(List.of(), List.of(), state);
        }
        final String parentId = parentSuccess.value().getOrNull();
        final String collection = state.phase() == Phase.USER_ROLES ? "users" : "groups";
        final Outcome<List<RoleAssignment>> fetched = assignmentList(access, collection, parentId, "snapshot", timeout);
        if (!(fetched instanceof Outcome.Succeeded<List<RoleAssignment>> success)) {
            return propagate(fetched);
        }
        final List<RoleAssignment> assignments = success.value();
        if (state.position().relationOffset() > assignments.size()) {
            return failed(ErrorCode._502, "Okta role-assignment replay offset exceeds the current projection");
        }
        final Realm.Kind parentKind = state.phase() == Phase.USER_ROLES ? Realm.Kind.USER : Realm.Kind.GROUP;
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> unique = new LinkedHashMap<>();
        int offset = state.position().relationOffset();
        while (offset < assignments.size() && resources.size() < request.limit() && unique.size() < request.limit()) {
            final RoleAssignment assignment = assignments.get(offset++);
            putResource(resources, roleResource(assignment.role(), observedAt), "Okta role-assignment page");
            putRelation(
                    unique,
                    roleMemberRelation(parentKind, parentId, assignment, observedAt),
                    "Okta role-assignment page");
        }
        if (offset < assignments.size()) {
            return page(
                    List.copyOf(resources.values()),
                    List.copyOf(unique.values()),
                    state,
                    Position.parent(parentId, null, offset));
        }
        final Outcome<Optional<String>> following = parent(
                access,
                state.phase(),
                Optional.of(parentId),
                true,
                "snapshot",
                timeout);
        if (!(following instanceof Outcome.Succeeded<Optional<String>> followingSuccess)) {
            return propagate(following);
        }
        if (followingSuccess.value().isPresent()) {
            return page(
                    List.copyOf(resources.values()),
                    List.copyOf(unique.values()),
                    state,
                    Position.parent(followingSuccess.value().getOrNull(), null, 0));
        }
        return completedPhase(List.copyOf(resources.values()), List.copyOf(unique.values()), state);
    }

    /**
     * Validates and starts one direct stable-key retrieval with a single observation instant.
     *
     * @param request exact provider-neutral retrieval request
     * @param context immutable context used only for key loading
     * @param timeout shared end-to-end timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "Okta management retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Okta management retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return authenticated(context, timeout, access -> retrieve(access, request.key(), observedAt, timeout));
    }

    /**
     * Dispatches one retrieval to the fixed Management API resource path.
     *
     * @param access     valid service-app Okta access
     * @param key        stable provider-neutral resource key
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved resource, explicit absence, or classified failure
     */
    private Outcome<Realm.Retrieved> retrieve(
            final Access access,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        return switch (key.kind()) {
            case USER -> retrieveUser(access, key.externalId(), observedAt, timeout);
            case GROUP -> retrieveGroup(access, key.externalId(), observedAt, timeout);
            case ROLE -> retrieveRole(access, key.externalId(), observedAt, timeout);
            default -> rejected(ErrorCode._400, "Okta management retrieve kind is unsupported");
        };
    }

    /**
     * Retrieves one user by its stable Okta identifier.
     *
     * @param access     valid service-app Okta access
     * @param id         stable user identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved user or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveUser(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> fetched = get(
                url(Builder.ENTERPRISE_USER, List.of(id), Map.of()),
                access,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        if (success.value() == null) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final User user = user(success.value());
        if (!id.equals(user.id())) {
            return failed(ErrorCode._502, "Okta user retrieval returned a different stable identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(userResource(user, observedAt))));
    }

    /**
     * Locates one group by replaying the official paged group collection from its fixed beginning.
     *
     * @param access     valid service-app Okta access
     * @param id         stable group identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved group or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveGroup(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        Url requestUrl = url(Builder.ENTERPRISE_GROUPS, null, Map.of("limit", Integer.toString(Normal._200)));
        while (true) {
            final Outcome<WirePage<Group>> fetched = wirePage(
                    requestUrl,
                    access,
                    timeout,
                    "retrieve",
                    Phase.GROUPS,
                    OktaRealmAdapter::group);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
                return propagate(fetched);
            }
            for (Group group : success.value().items()) {
                if (id.equals(group.id())) {
                    return Outcome.succeeded(new Realm.Retrieved(Optional.of(groupResource(group, observedAt))));
                }
            }
            if (success.value().next().isEmpty()) {
                return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
            }
            requestUrl = pagination(success.value().next().getOrNull(), Phase.GROUPS);
        }
    }

    /**
     * Locates one administrator role by replaying user and group role-assignment projections.
     *
     * @param access     valid service-app Okta access
     * @param id         stable role identifier
     * @param observedAt shared observation instant
     * @param timeout    shared end-to-end timeout
     * @return retrieved role or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveRole(
            final Access access,
            final String id,
            final Instant observedAt,
            final Timeout timeout) {
        for (Phase phase : List.of(Phase.USER_ROLES, Phase.GROUP_ROLES)) {
            Optional<String> parentId = Optional.empty();
            while (true) {
                final Outcome<Optional<String>> parent = parent(
                        access,
                        phase,
                        parentId,
                        parentId.isPresent(),
                        "retrieve",
                        timeout);
                if (!(parent instanceof Outcome.Succeeded<Optional<String>> parentSuccess)) {
                    return propagate(parent);
                }
                if (parentSuccess.value().isEmpty()) {
                    break;
                }
                parentId = parentSuccess.value();
                final String collection = phase == Phase.USER_ROLES ? "users" : "groups";
                final Outcome<List<RoleAssignment>> fetched = assignmentList(
                        access,
                        collection,
                        parentId.getOrNull(),
                        "retrieve",
                        timeout);
                if (!(fetched instanceof Outcome.Succeeded<List<RoleAssignment>> success)) {
                    return propagate(fetched);
                }
                for (RoleAssignment assignment : success.value()) {
                    if (id.equals(assignment.role().id())) {
                        return Outcome.succeeded(
                                new Realm.Retrieved(Optional.of(roleResource(assignment.role(), observedAt))));
                    }
                }
            }
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
    }

    /**
     * Acquires a cached or freshly exchanged service-app token and retries one 401 result exactly once.
     *
     * @param context   immutable context used by the project Key Loader
     * @param timeout   shared end-to-end timeout
     * @param operation authenticated synchronous Management API operation
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
                                                : OktaRealmAdapter.<Boolean>failed(
                                                        ErrorCode._500,
                                                        "Okta upstream-token cache deletion failed"))
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
                                                        "Okta token refresh returned an unsupported outcome"));
                                    });
                                });
                    });
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "Okta token lookup returned an unsupported outcome"));
        });
    }

    /**
     * Executes one authenticated operation on the Source executor and closes transport exceptions into Outcomes.
     *
     * @param access    valid service-app Okta access
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
                return failed(ErrorCode._408, "Okta management operation has no remaining timeout");
            }
            try {
                return Assert.notNull(operation.apply(access), "Okta management operation returned no outcome");
            } catch (TimeoutException ignored) {
                return failed(ErrorCode._408, "Okta management operation timed out");
            } catch (RuntimeException ignored) {
                return failed(ErrorCode._503, "Okta Management API transport is unavailable");
            }
        }, services.executor());
    }

    /**
     * Reads the Source-private token cache before exchanging a new service-app token.
     *
     * @param context immutable context used by the project Key Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous cached or newly exchanged access outcome
     */
    private CompletionStage<Outcome<Access>> access(final Context context, final Timeout timeout) {
        return accessCache.get(Builder.UPSTREAM_ACCESS_TOKEN_CACHE_KEY)
                .<CompletionStage<Outcome<Access>>>handle((cached, cause) -> {
                    if (cause != null) {
                        return completed(failed(ErrorCode._500, "Okta upstream-token cache lookup failed"));
                    }
                    return cached == null ? exchange(context, timeout) : completed(Outcome.succeeded(cached));
                }).thenCompose(Function.identity());
    }

    /**
     * Loads and validates the external RSA private key before signing the service-app assertion synchronously.
     *
     * @param context immutable context supplied to the project Key Loader
     * @param timeout shared end-to-end timeout
     * @return asynchronous service-app access-token outcome
     */
    private CompletionStage<Outcome<Access>> exchange(final Context context, final Timeout timeout) {
        final Instant now = timeout.clock().now();
        final KeyLoader.Request query = new KeyLoader.Request(services.registration(), options.clientId(),
                Optional.of(options.credential().id()), Builder.SIGNATURE, JwaAlgorithm.RS256.name(), now);
        final CompletionStage<Outcome<KeyMaterial>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.keyLoader().load(query, context, timeout),
                    loaded -> services.keyParser().parse(services.registration(), query, loaded));
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "Okta signing-key resolution failed before returning a stage"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._500, "Okta signing-key resolution returned no stage"));
        }
        return resolution.<Outcome<String>>handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return OktaRealmAdapter.<String>failed(ErrorCode._500, "Okta signing-key resolution failed");
            }
            return switch (outcome) {
                case Outcome.Succeeded<KeyMaterial> success -> assertion(success.value(), now);
                case Outcome.Rejected<KeyMaterial> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyMaterial> failed -> Outcome.failed(failed.failure());
                default -> OktaRealmAdapter
                        .<String>failed(ErrorCode._500, "Okta signing-key resolution returned an unsupported outcome");
            };
        }).thenCompose(assertion -> switch (assertion) {
            case Outcome.Succeeded<String> success -> token(success.value(), timeout).thenCompose(this::cache);
            case Outcome.Rejected<String> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<String> failed -> completed(Outcome.failed(failed.failure()));
            default -> completed(failed(ErrorCode._500, "Okta assertion signing returned an unsupported outcome"));
        });
    }

    /**
     * Signs the exact Okta service-app assertion while retaining KeyMaterial only in this synchronous scope.
     *
     * @param resolved externally loaded and parsed signing-key material
     * @param now      shared-clock assertion issue instant
     * @return compact RS256 assertion or a safe rejection/failure
     */
    private Outcome<String> assertion(final KeyMaterial resolved, final Instant now) {
        if (!options.credential().id().equals(resolved.keyId())
                || !JwaAlgorithm.RS256.name().equals(resolved.algorithm())
                || !(resolved.key() instanceof RSAPrivateKey privateKey) || now.isBefore(resolved.notBefore())
                || !now.isBefore(resolved.notAfter())
                || resolved.notAfter().isBefore(now.plus(Builder.UPSTREAM_ASSERTION_LIFETIME))) {
            return rejected(ErrorCode._401, "Okta signing key does not match the configured management key");
        }
        byte[] payload = null;
        try {
            final Map<String, JsonValue> headers = new LinkedHashMap<>();
            headers.put(JoseHeader.ALGORITHM, new JsonValue.StringValue(JwaAlgorithm.RS256.name()));
            headers.put(JoseHeader.TYPE, new JsonValue.StringValue(JWT_TYPE));
            headers.put(JoseHeader.KEY_ID, new JsonValue.StringValue(resolved.keyId()));
            final Map<String, JsonValue> claims = new LinkedHashMap<>();
            claims.put(JwtClaims.ISSUER, new JsonValue.StringValue(options.clientId()));
            claims.put(JwtClaims.SUBJECT, new JsonValue.StringValue(options.clientId()));
            claims.put(JwtClaims.AUDIENCE, new JsonValue.StringValue(targets.token().getOrNull().url().toString()));
            claims.put(JwtClaims.ISSUED_AT, number(now.getEpochSecond()));
            claims.put(JwtClaims.EXPIRATION, number(now.plus(Builder.UPSTREAM_ASSERTION_LIFETIME).getEpochSecond()));
            claims.put(JwtClaims.JWT_ID, new JsonValue.StringValue(RandomKit.randomString(Normal._32)));
            payload = services.jsonProvider().writeValue(new JsonValue.ObjectValue(claims));
            final JwsService.Signature signature = jwsService
                    .sign(JoseHeader.protectedOnly(new JsonValue.ObjectValue(headers)), payload, privateKey);
            return Outcome.succeeded(jwsService.compact(new JwsService.Jws(payload, List.of(signature))));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._500, "Okta service-app assertion signing failed");
        } finally {
            if (payload != null) {
                Arrays.fill(payload, (byte) 0);
            }
        }
    }

    /**
     * Schedules the bounded Okta OAuth assertion exchange on the shared Source executor.
     *
     * @param assertion compact signed JWT bearer assertion
     * @param timeout   shared end-to-end timeout
     * @return asynchronous fresh access outcome
     */
    private CompletionStage<Outcome<Access>> token(final String assertion, final Timeout timeout) {
        try {
            return CompletableFuture
                    .<Outcome<Access>>supplyAsync(() -> tokenRequest(assertion, timeout), services.executor()).handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : OktaRealmAdapter
                                            .<Access>failed(ErrorCode._503, "Okta token exchange task failed"));
        } catch (RuntimeException ignored) {
            return completed(failed(ErrorCode._500, "Okta token exchange task could not be scheduled"));
        }
    }

    /**
     * Exchanges one signed assertion through the fixed Okta OAuth token endpoint.
     *
     * @param assertion compact signed JWT bearer assertion
     * @param timeout   shared end-to-end timeout
     * @return service-app access or safely classified failure
     */
    private Outcome<Access> tokenRequest(final String assertion, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Okta token exchange has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout)
                    .url(targets.token().getOrNull().url().toString()).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .form(OAuth2.Parameters.GRANT_TYPE, GrantType.CLIENT_CREDENTIALS.value())
                    .form(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, options.scopes()))
                    .form(OAuth2.Parameters.CLIENT_ASSERTION_TYPE, OAuth2.Parameters.JWT_BEARER_ASSERTION_TYPE)
                    .form(OAuth2.Parameters.CLIENT_ASSERTION, assertion).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Okta token exchange timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Okta token endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return tokenHttpFailure(response);
            }
            final JsonValue.ObjectValue envelope = object(response);
            final String token = requiredString(envelope, "access_token");
            final long seconds = requiredPositiveLong(envelope, "expires_in");
            final String tokenType = requiredString(envelope, "token_type");
            if (!"Bearer".equalsIgnoreCase(tokenType)) {
                return failed(ErrorCode._502, "Okta token endpoint returned an unsupported token type");
            }
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
            return failed(ErrorCode._502, "Okta token endpoint returned an invalid response");
        }
    }

    /**
     * Stores one cacheable service-app token with the mandatory early-expiration skew.
     *
     * @param outcome freshly exchanged access outcome
     * @return original access after cache creation or a safe cache failure
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
                        : failed(ErrorCode._500, "Okta upstream-token cache creation failed"));
    }

    /**
     * Maps one non-successful Okta OAuth token response.
     *
     * @param response owned non-successful token response
     * @return credential rejection, rate-limit failure, or upstream failure
     */
    private Outcome<Access> tokenHttpFailure(final Response response) {
        final int status = response.code();
        final Map<String, JsonValue> details = details("token", status, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.UNAUTHORIZED
                || status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._401, "Okta rejected the service-app assertion", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Okta token endpoint is rate limited", details);
        }
        return failed(ErrorCode._502, "Okta token endpoint returned an upstream error", details);
    }

    /**
     * Executes one Okta Management API GET and decodes a successful JSON object.
     *
     * @param url           manifest-derived immutable request URL
     * @param access        valid service-app Okta access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @return decoded object, {@code null} for absence, or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound) {
        return request(url, access, timeout, operation, allowNotFound, this::object);
    }

    /**
     * Executes one Okta Management API GET through an operation-specific response reader.
     *
     * @param url           manifest-derived immutable request URL
     * @param access        valid service-app Okta access
     * @param timeout       shared end-to-end timeout
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @param reader        bounded response reader
     * @param <T>           decoded response type
     * @return decoded value, {@code null} for absence, or classified failure
     */
    private <T> Outcome<T> request(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final boolean allowNotFound,
            final Function<Response, T> reader) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Okta Management API request has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout).url(url.toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(Http.Header.AUTHORIZATION, "Bearer " + access.token()).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Okta Management API request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Okta Management API endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return adminHttpFailure(response, operation, allowNotFound);
            }
            return Outcome.succeeded(reader.apply(response));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Okta Management API returned an invalid response");
        }
    }

    /**
     * Maps one non-successful Management API response without retaining its body or headers.
     *
     * @param response      owned non-successful response
     * @param operation     safe enterprise operation label
     * @param allowNotFound whether HTTP 404 represents explicit absence
     * @param <T>           expected success type
     * @return classified rejection, absence, or failure
     */
    private <T> Outcome<T> adminHttpFailure(
            final Response response,
            final String operation,
            final boolean allowNotFound) {
        final int status = response.code();
        final Map<String, JsonValue> details = details(operation, status, response.headers());
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "Okta Management API rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "Okta Management API rejected the service-app access token", details);
        }
        if (status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._403, "Okta Management API visibility or permission is insufficient", details);
        }
        if (status == Http.Status.NOT_FOUND && allowNotFound) {
            return Outcome.succeeded(null);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Okta Management API is rate limited", details);
        }
        return failed(ErrorCode._502, "Okta Management API returned an upstream error", details);
    }

    /**
     * Reads one official Okta array page and extracts its validated Link continuation.
     *
     * @param url       manifest-derived request URL
     * @param access    valid service-app Okta access
     * @param timeout   shared end-to-end timeout
     * @param operation safe enterprise operation label
     * @param phase     phase owning the exact path and query closure
     * @param parser    minimal projection parser
     * @param <T>       minimal item type
     * @return decoded page and optional validated official next URL
     */
    private <T> Outcome<WirePage<T>> wirePage(
            final Url url,
            final Access access,
            final Timeout timeout,
            final String operation,
            final Phase phase,
            final Function<JsonValue.ObjectValue, T> parser) {
        return request(url, access, timeout, operation, false, response -> {
            final List<T> items = new ArrayList<>();
            for (JsonValue value : array(response)) {
                items.add(parser.apply(requiredObject(value, "Okta collection item")));
            }
            return new WirePage<>(items, nextLink(response.headers(), phase));
        });
    }

    /**
     * Reads and canonicalizes one user's or group's bounded role-assignment array.
     *
     * @param access     valid service-app access
     * @param collection exact {@code users} or {@code groups} path segment
     * @param parentId   stable assigned user or group identifier
     * @param operation  safe enterprise operation label
     * @param timeout    shared end-to-end timeout
     * @return stable assignment-ID-sorted, de-duplicated role assignments
     */
    private Outcome<List<RoleAssignment>> assignmentList(
            final Access access,
            final String collection,
            final String parentId,
            final String operation,
            final Timeout timeout) {
        if (!("users".equals(collection) || "groups".equals(collection))) {
            throw new ValidateException("Okta role-assignment collection is invalid");
        }
        final String targetName = "users".equals(collection) ? Builder.ENTERPRISE_ROLE_ASSIGNMENTS
                : Builder.ENTERPRISE_ROLE_MEMBERS;
        final Outcome<List<JsonValue>> fetched = request(
                url(targetName, List.of(collection, parentId, "roles"), Map.of()),
                access,
                timeout,
                operation,
                false,
                this::array);
        if (!(fetched instanceof Outcome.Succeeded<List<JsonValue>> success)) {
            return propagate(fetched);
        }
        final Map<String, RoleAssignment> unique = new LinkedHashMap<>();
        for (JsonValue value : success.value()) {
            final RoleAssignment assignment = roleAssignment(requiredObject(value, "Okta role-assignment item"));
            final RoleAssignment previous = unique.putIfAbsent(assignment.assignmentId(), assignment);
            if (previous != null && !previous.equals(assignment)) {
                return failed(ErrorCode._502, "Okta role assignments contain a conflicting assignment identifier");
            }
        }
        final List<RoleAssignment> assignments = new ArrayList<>(unique.values());
        assignments.sort(
                Comparator.comparing(RoleAssignment::assignmentId).thenComparing(assignment -> assignment.role().id()));
        return Outcome.succeeded(List.copyOf(assignments));
    }

    /**
     * Replays the owning user or group collection and selects the current or following stable parent identifier.
     *
     * @param access    valid service-app access
     * @param phase     dependent role or group-member phase
     * @param current   empty for the first parent or the current stable parent identifier
     * @param following whether to return the parent following the supplied identifier
     * @param operation safe enterprise operation label
     * @param timeout   shared end-to-end timeout
     * @return selected parent identifier or natural exhaustion
     */
    private Outcome<Optional<String>> parent(
            final Access access,
            final Phase phase,
            final Optional<String> current,
            final boolean following,
            final String operation,
            final Timeout timeout) {
        final boolean users = phase == Phase.USER_ROLES;
        if (!users && phase != Phase.GROUP_MEMBERS && phase != Phase.GROUP_ROLES) {
            throw new ValidateException("Okta snapshot phase has no replay parent");
        }
        final Phase collectionPhase = users ? Phase.USERS : Phase.GROUPS;
        final String targetName = users ? Builder.ENTERPRISE_USERS : Builder.ENTERPRISE_GROUPS;
        Url requestUrl = url(targetName, null, Map.of("limit", Integer.toString(Normal._200)));
        boolean found = current.isEmpty();
        while (true) {
            final Outcome<WirePage<String>> fetched = wirePage(
                    requestUrl,
                    access,
                    timeout,
                    operation,
                    collectionPhase,
                    value -> requiredIdentifier(value, "id"));
            if (!(fetched instanceof Outcome.Succeeded<WirePage<String>> success)) {
                return propagate(fetched);
            }
            for (String parentId : success.value().items()) {
                if (found) {
                    return Outcome.succeeded(Optional.of(parentId));
                }
                if (current.getOrNull().equals(parentId)) {
                    if (!following) {
                        return Outcome.succeeded(Optional.of(parentId));
                    }
                    found = true;
                }
            }
            if (success.value().next().isEmpty()) {
                if (!found) {
                    return failed(ErrorCode._502, "Okta replay parent no longer exists");
                }
                return Outcome.succeeded(Optional.empty());
            }
            requestUrl = pagination(success.value().next().getOrNull(), collectionPhase);
        }
    }

    /**
     * Extracts the single official {@code rel=next} URL from Okta Link headers.
     *
     * @param headers response headers
     * @param phase   phase owning the continuation path
     * @return empty at natural exhaustion or the validated normalized next URL
     */
    private Optional<String> nextLink(final FabricX.Headers headers, final Phase phase) {
        String selected = null;
        for (String header : headers.values(Http.Header.LINK)) {
            for (String element : header.split(",")) {
                final int opening = element.indexOf('<');
                final int closing = element.indexOf('>', opening + 1);
                final String parameters = closing < 0 ? element : element.substring(closing + 1);
                if (!nextRelation(parameters)) {
                    continue;
                }
                if (opening < 0 || closing <= opening) {
                    throw new ValidateException("Okta response contains a malformed next Link value");
                }
                final String candidate = pagination(element.substring(opening + 1, closing), phase).toString();
                if (selected != null && !selected.equals(candidate)) {
                    throw new ValidateException("Okta response contains conflicting next Link values");
                }
                selected = candidate;
            }
        }
        return Optional.ofNullable(selected);
    }

    /**
     * Validates an official absolute Okta pagination URL against the selected host and phase closure.
     *
     * @param value absolute continuation URL from a cursor or Link header
     * @param phase phase owning the exact collection path
     * @return normalized validated HTTPS URL
     */
    private Url pagination(final String value, final Phase phase) {
        final Url candidate;
        try {
            candidate = Url.parse(requireText(value, "Okta continuation URL"));
        } catch (RuntimeException cause) {
            throw new ValidateException("Okta continuation URL is invalid", cause);
        }
        final Url base = target(
                phase == Phase.USERS ? Builder.ENTERPRISE_USERS
                        : phase == Phase.GROUPS ? Builder.ENTERPRISE_GROUPS : Builder.ENTERPRISE_GROUP_MEMBERS).url();
        if (!Protocol.HTTPS.getName().equalsIgnoreCase(candidate.scheme()) || candidate.port() != Port._443.getPort()
                || !base.host().equals(candidate.host())
                || candidate.username() != null && !candidate.username().isEmpty()
                || candidate.password() != null && !candidate.password().isEmpty()
                || candidate.fragment() != null && !candidate.fragment().isEmpty()
                || !paginationPath(candidate.path(), base.path(), phase)) {
            throw new ValidateException("Okta continuation URL violates the selected endpoint boundary");
        }
        if (!candidate.queryParameterNames().equals(Set.of("limit", "after"))
                || candidate.queryParameterValues("limit").size() != 1
                || candidate.queryParameterValues("after").size() != 1) {
            throw new ValidateException("Okta continuation URL has an invalid query closure");
        }
        final int limit;
        try {
            limit = Integer.parseInt(candidate.queryParameter("limit"));
        } catch (NumberFormatException cause) {
            throw new ValidateException("Okta continuation limit is invalid", cause);
        }
        if (limit <= 0 || limit > Normal._200) {
            throw new ValidateException("Okta continuation limit exceeds the supported range");
        }
        requireText(candidate.queryParameter("after"), "Okta continuation after value");
        return candidate;
    }

    /**
     * Builds one URL from a manifest-owned endpoint, safely encoded path segments, and fixed query values.
     *
     * @param targetName exact root Builder management target key
     * @param segments   optional stable path segments appended to the target base
     * @param query      exact query members
     * @return immutable Okta HTTPS URL
     */
    private Url url(final String targetName, final List<String> segments, final Map<String, String> query) {
        final Url base = target(targetName).url();
        final StringBuilder path = new StringBuilder(base.path());
        if (segments != null) {
            for (String segment : segments) {
                path.append('/')
                        .append(RFC3986.SEGMENT.encode(requireText(segment, "Okta path segment"), Charset.UTF_8));
            }
        }
        final UrlBuilder builder = Url.builder().scheme(base.scheme()).host(base.host()).path(path.toString());
        query.forEach(builder::query);
        return builder.build();
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
            throw new ValidateException("Okta management manifest omits a required management target");
        }
        return endpoint;
    }

    /**
     * Creates one page with an encoded continuation cursor.
     *
     * @param resources normalized resources in output order
     * @param relations normalized relations in output order
     * @param state     current finite cursor state
     * @param position  recoverable position for the same phase
     * @return successful normalized page outcome
     */
    private Outcome<Realm.Page> page(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Position position) {
        final Realm.Cursor cursor = encode(new CursorState(state.phase(), state.kinds(), position));
        return Outcome.succeeded(new Realm.Page(resources, relations, Optional.of(cursor)));
    }

    /**
     * Continues one official Link-paged phase or advances after natural upstream exhaustion.
     *
     * @param resources normalized resources
     * @param relations normalized relations
     * @param state     current finite cursor state
     * @param next      optional validated official next URL
     * @return continued or phase-completed page outcome
     */
    private Outcome<Realm.Page> continuedOrCompleted(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Optional<String> next) {
        return next.isPresent() ? page(resources, relations, state, Position.page(next.getOrNull()))
                : completedPhase(resources, relations, state);
    }

    /**
     * Completes one finite phase and selects the next phase enabled by the original kind set.
     *
     * @param resources resources produced by the completed phase
     * @param relations relations produced by the completed phase
     * @param state     completed finite cursor state
     * @return successful page with the next phase cursor or natural completion
     */
    private Outcome<Realm.Page> completedPhase(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state) {
        final Phase next = nextPhase(state.phase(), state.kinds());
        final Optional<Realm.Cursor> cursor = next == null ? Optional.empty()
                : Optional.of(encode(new CursorState(next, state.kinds(), Position.initial())));
        return Outcome.succeeded(new Realm.Page(resources, relations, cursor));
    }

    /**
     * Encodes one canonical six-field Okta management snapshot cursor.
     *
     * @param state validated finite pagination state
     * @return opaque unpadded Base64 URL-safe cursor
     */
    private Realm.Cursor encode(final CursorState state) {
        final Map<String, JsonValue> envelope = new LinkedHashMap<>();
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(OktaManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(OktaManifest.MANAGEMENT.value()));
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
     * Decodes, validates, and canonicalizes one Okta management snapshot cursor.
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
                    "Okta cursor envelope");
            if (!OktaManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !OktaManifest.MANAGEMENT.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredLong(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("Okta cursor does not belong to this management snapshot");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("Okta cursor kinds do not match the snapshot request");
            }
            final CursorState state = new CursorState(phase, decodedKinds,
                    position(phase, requiredObject(envelope, Builder.CURSOR_POSITION_FIELD)));
            if (state.position().next().isPresent()) {
                final String next = state.position().next().getOrNull();
                final Url validated = pagination(next, phase);
                if (!validated.toString().equals(next)) {
                    throw new ValidateException("Okta cursor continuation URL is not normalized");
                }
                if (phase == Phase.GROUP_MEMBERS) {
                    final String parentId = state.position().parentId().getOrNull();
                    final Url parentUrl = url(Builder.ENTERPRISE_GROUP_MEMBERS, List.of(parentId, "users"), Map.of());
                    if (!validated.path().equals(parentUrl.path())) {
                        throw new ValidateException("Okta cursor continuation does not belong to its replay parent");
                    }
                }
            }
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("Okta cursor is not in canonical form");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("Okta cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded JSON response object with duplicate-member rejection.
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
                "Okta response");
    }

    /**
     * Decodes one bounded top-level Okta JSON array with duplicate-object-member rejection.
     *
     * @param response owned successful HTTP response
     * @return immutable decoded top-level array values
     */
    private List<JsonValue> array(final Response response) {
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("Okta response must be a JSON array");
        }
        return array.values();
    }

    /**
     * Builds safe allow-listed outcome details for one upstream response.
     *
     * @param operation safe enterprise operation label
     * @param status    upstream HTTP status
     * @param headers   headers used only for Retry-After normalization
     * @return immutable allow-listed scalar details
     */
    private Map<String, JsonValue> details(final String operation, final int status, final FabricX.Headers headers) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(OktaManifest.ID.value()));
        details.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(variant.variant().value()));
        details.put(Builder.OPERATION_FIELD, new JsonValue.StringValue(operation));
        details.put(Builder.HTTP_STATUS_FIELD, number(status));
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
     * Defines the complete finite Okta management snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Reads official user pages and emits user resources.
         */
        USERS(1, Realm.Kind.USER),

        /**
         * Replays users and emits their assigned role resources and relations.
         */
        USER_ROLES(2, Realm.Kind.ROLE),

        /**
         * Reads official group pages and emits group resources.
         */
        GROUPS(3, Realm.Kind.GROUP),

        /**
         * Replays group parents and reads their official member pages.
         */
        GROUP_MEMBERS(4, Realm.Kind.GROUP),

        /**
         * Replays groups and emits their assigned role resources and relations.
         */
        GROUP_ROLES(5, Realm.Kind.ROLE);

        /**
         * Stable persisted phase code independent of enum ordinal.
         */
        private final int code;

        /**
         * Resource category that enables this phase.
         */
        private final Realm.Kind kind;

        /**
         * Creates one stable Okta management phase.
         *
         * @param code stable persisted phase code
         * @param kind resource category enabling the phase
         */
        Phase(final int code, final Realm.Kind kind) {
            this.code = code;
            this.kind = kind;
        }

        /**
         * Resolves one stable phase code without using ordinal values.
         *
         * @param code persisted phase code
         * @return exact finite phase
         */
        private static Phase from(final int code) {
            for (Phase phase : values()) {
                if (phase.code == code) {
                    return phase;
                }
            }
            throw new ValidateException("Okta cursor contains an unknown phase code");
        }

        /**
         * Returns the stable persisted phase code.
         *
         * @return phase code
         */
        private int code() {
            return code;
        }

        /**
         * Returns the resource category enabling this phase.
         *
         * @return owning resource category
         */
        private Realm.Kind kind() {
            return kind;
        }

        /**
         * Returns the next declared snapshot phase.
         *
         * @return following phase or {@code null}
         */
        private Phase next() {
            return switch (this) {
                case USERS -> USER_ROLES;
                case USER_ROLES -> GROUPS;
                case GROUPS -> GROUP_MEMBERS;
                case GROUP_MEMBERS -> GROUP_ROLES;
                case GROUP_ROLES -> null;
            };
        }
    }

    /**
     * Carries one canonical Okta management snapshot cursor state.
     *
     * @param phase    exact finite phase
     * @param kinds    requested kinds in stable code order
     * @param position recoverable phase-specific position
     * @author Kimi Liu
     */
    private record CursorState(Phase phase, List<Realm.Kind> kinds, Position position) {

        /**
         * Validates and freezes one canonical cursor state.
         *
         * @param phase    exact finite phase
         * @param kinds    requested kinds
         * @param position recoverable position
         */
        private CursorState {
            phase = Assert.notNull(phase, "Okta cursor phase must not be null");
            Assert.notNull(kinds, "Okta cursor kinds must not be null");
            final List<Realm.Kind> copy = new ArrayList<>(kinds.size());
            int previous = 0;
            for (Realm.Kind kind : kinds) {
                final Realm.Kind checked = Assert.notNull(kind, "Okta cursor kind must not be null");
                if (!SUPPORTED_KINDS.contains(checked) || checked.code() <= previous) {
                    throw new ValidateException("Okta cursor kinds must be supported and in stable code order");
                }
                previous = checked.code();
                copy.add(checked);
            }
            if (copy.isEmpty()) {
                throw new ValidateException("Okta cursor kinds must not be empty");
            }
            kinds = List.copyOf(copy);
            if (!kinds.contains(phase.kind())) {
                throw new ValidateException("Okta cursor phase is not enabled by its requested kinds");
            }
            position = Assert.notNull(position, "Okta cursor position must not be null");
            position.validate(phase);
        }

        /**
         * Creates the first enabled snapshot phase for one validated kind set.
         *
         * @param kinds exact requested kinds in stable code order
         * @return canonical initial cursor state
         */
        private static CursorState initial(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            Phase phase = Phase.USERS;
            while (phase != null && !ordered.contains(phase.kind())) {
                phase = phase.next();
            }
            if (phase == null) {
                throw new ValidateException("Okta snapshot kind set has no enabled phase");
            }
            return new CursorState(phase, ordered, Position.initial());
        }
    }

    /**
     * Carries one recoverable Okta management phase position.
     *
     * @param next           validated absolute Link continuation for paged phases
     * @param parentId       current stable user or group identifier for dependent phases
     * @param relationOffset next role-assignment index within the current parent projection
     * @author Kimi Liu
     */
    private record Position(Optional<String> next, Optional<String> parentId, int relationOffset) {

        /**
         * Validates and freezes one recoverable position.
         *
         * @param next           optional validated absolute Link continuation
         * @param parentId       optional stable user or group identifier
         * @param relationOffset non-negative relation replay index
         */
        private Position {
            next = optional(next, "Okta cursor next URL");
            parentId = optional(parentId, "Okta cursor parent identifier");
            if (relationOffset < 0) {
                throw new ValidateException("Okta cursor relation offset must not be negative");
            }
        }

        /**
         * Creates one empty initial position.
         *
         * @return canonical empty position
         */
        private static Position initial() {
            return new Position(Optional.empty(), Optional.empty(), 0);
        }

        /**
         * Creates one official top-level Link continuation.
         *
         * @param next validated official next URL
         * @return canonical paged position
         */
        private static Position page(final String next) {
            return new Position(Optional.of(next), Optional.empty(), 0);
        }

        /**
         * Creates one dependent parent, Link continuation, and relation offset.
         *
         * @param parentId       stable current or following user or group identifier
         * @param next           validated child Link continuation or {@code null}
         * @param relationOffset next role-assignment index for role phases
         * @return canonical dependent position
         */
        private static Position parent(final String parentId, final String next, final int relationOffset) {
            return new Position(Optional.ofNullable(next), Optional.ofNullable(parentId), relationOffset);
        }

        /**
         * Validates one Bus optional text container without changing its lexical value.
         *
         * @param value optional text container
         * @param label safe semantic label
         * @return detached optional original text
         */
        private static Optional<String> optional(final Optional<String> value, final String label) {
            Assert.notNull(value, label + " container must not be null");
            return value.isPresent() ? Optional.of(requireText(value.getOrNull(), label)) : Optional.empty();
        }

        /**
         * Verifies phase-specific ownership of every position component.
         *
         * @param phase exact finite snapshot phase
         */
        private void validate(final Phase phase) {
            if (phase == Phase.GROUP_MEMBERS) {
                if (relationOffset != 0 || next.isPresent() && parentId.isEmpty()) {
                    throw new ValidateException("Okta group-member cursor is inconsistent");
                }
            } else if (phase == Phase.USER_ROLES || phase == Phase.GROUP_ROLES) {
                if (next.isPresent() || relationOffset > 0 && parentId.isEmpty()) {
                    throw new ValidateException("Okta role-assignment cursor is inconsistent");
                }
            } else if (parentId.isPresent() || relationOffset != 0) {
                throw new ValidateException("Okta top-level page cursor must not contain a parent identifier");
            }
        }

    }

    /**
     * Carries one decoded official Okta collection page.
     *
     * @param items minimal page items
     * @param next  validated official next URL or empty
     * @param <T>   minimal item type
     * @author Kimi Liu
     */
    private record WirePage<T>(List<T> items, Optional<String> next) {

        /**
         * Freezes one decoded Okta collection page.
         *
         * @param items minimal page items
         * @param next  optional validated official next URL
         */
        private WirePage {
            items = List.copyOf(Assert.notNull(items, "Okta page items must not be null"));
            next = Position.optional(next, "Okta next-page URL");
        }
    }

    /**
     * Minimal Okta user projection.
     *
     * @param id          stable user identifier
     * @param login       exact profile login identifier
     * @param displayName profile display name or the login fallback
     * @param state       normalized lifecycle state
     * @author Kimi Liu
     */
    private record User(String id, String login, String displayName, Realm.State state) {

        /**
         * Validates and freezes one user projection.
         *
         * @param id          stable user identifier
         * @param login       exact login identifier
         * @param displayName exact display name or fallback
         * @param state       normalized lifecycle state
         */
        private User {
            id = requireText(id, "Okta user identifier");
            login = requireText(login, "Okta user login");
            displayName = requireText(displayName, "Okta user display name");
            state = Assert.notNull(state, "Okta user state must not be null");
        }
    }

    /**
     * Minimal Okta group projection.
     *
     * @param id   stable group identifier
     * @param name exact group display name
     * @author Kimi Liu
     */
    private record Group(String id, String name) {

        /**
         * Validates one group projection.
         *
         * @param id   stable group identifier
         * @param name exact group display name
         */
        private Group {
            id = requireText(id, "Okta group identifier");
            name = requireText(name, "Okta group display name");
        }
    }

    /**
     * Minimal Okta group-member projection.
     *
     * @param id stable user identifier returned by the member collection
     * @author Kimi Liu
     */
    private record Member(String id) {

        /**
         * Validates one group-member projection.
         *
         * @param id stable member identifier
         */
        private Member {
            id = requireText(id, "Okta group-member identifier");
        }
    }

    /**
     * Minimal Okta administrator-role projection.
     *
     * @param id    stable standard role type or custom role identifier
     * @param label exact role display label
     * @author Kimi Liu
     */
    private record Role(String id, String label) {

        /**
         * Validates one administrator-role projection.
         *
         * @param id    stable role identifier
         * @param label exact role display label
         */
        private Role {
            id = requireText(id, "Okta role identifier");
            label = requireText(label, "Okta role display label");
        }
    }

    /**
     * Minimal Okta role-assignment projection.
     *
     * @param assignmentId stable assignment identifier retained as a relation attribute
     * @param role         normalized assigned role projection
     * @author Kimi Liu
     */
    private record RoleAssignment(String assignmentId, Role role) {

        /**
         * Validates one role-assignment projection.
         *
         * @param assignmentId stable assignment identifier
         * @param role         normalized assigned role
         */
        private RoleAssignment {
            assignmentId = requireText(assignmentId, "Okta role-assignment identifier");
            role = Assert.notNull(role, "Okta role-assignment role must not be null");
        }
    }

    /**
     * Holds one short-lived service-app Okta access token inside the Source-private cache.
     *
     * @param token           non-blank upstream token
     * @param expiresAtMillis absolute expiration in Fabric clock milliseconds or zero when uncacheable
     * @author Kimi Liu
     */
    private record Access(String token, long expiresAtMillis) {

        /**
         * Validates one upstream access value.
         *
         * @param token           non-blank upstream token
         * @param expiresAtMillis positive absolute expiration or zero
         */
        private Access {
            token = requireText(token, "Okta service-app access token");
            if (expiresAtMillis < 0L) {
                throw new ValidateException("Okta access expiration must not be negative");
            }
        }

        /**
         * Returns a fixed representation that cannot disclose token or lifetime.
         *
         * @return root redacted marker
         */
        @Override
        public String toString() {
            return Builder.REDACTED_VALUE;
        }
    }

}
