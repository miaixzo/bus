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
package org.miaixz.bus.auth.vendor.aliyun;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Realm;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorTargets;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.RFC3986;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the provider-neutral Alibaba Cloud RAM realm surface.
 * <p>
 * Snapshot pagination advances through RAM users, groups, group members, roles, and role trust policies. Every RPC is
 * signed with the frozen ACS3-HMAC-SHA256 V3 template using Bus encoding, hashing, HMAC, time, and randomness services.
 * Each invocation loads one AccessKey Secret lease, never caches it, and closes it at the single terminal stage
 * boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AliyunRealmAdapter implements VendorAdapter {

    /**
     * Frozen Alibaba Cloud RAM RPC API version.
     */
    private static final String API_VERSION = "2015-05-01";

    /**
     * Official Alibaba Cloud signature V3 algorithm label.
     */
    private static final String SIGNATURE_ALGORITHM = "ACS3-HMAC-SHA256";

    /**
     * Lowercase host Header name required by signature V3.
     */
    private static final String HOST_HEADER = "host";

    /**
     * Lowercase RAM action Header name required by signature V3.
     */
    private static final String ACTION_HEADER = "x-acs-action";

    /**
     * Lowercase payload-hash Header name required by signature V3.
     */
    private static final String CONTENT_HASH_HEADER = "x-acs-content-sha256";

    /**
     * Lowercase request-time Header name required by signature V3.
     */
    private static final String DATE_HEADER = "x-acs-date";

    /**
     * Lowercase request-nonce Header name required by signature V3.
     */
    private static final String NONCE_HEADER = "x-acs-signature-nonce";

    /**
     * Lowercase RAM API-version Header name required by signature V3.
     */
    private static final String VERSION_HEADER = "x-acs-version";

    /**
     * Lowercase signed-header names in mandatory lexical order.
     */
    private static final List<String> SIGNED_HEADER_NAMES = List
            .of(HOST_HEADER, ACTION_HEADER, CONTENT_HASH_HEADER, DATE_HEADER, NONCE_HEADER, VERSION_HEADER);

    /**
     * Empty immutable JSON attributes used by projections without declared extensions.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by RAM management.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set.of(Realm.Kind.USER, Realm.Kind.GROUP, Realm.Kind.ROLE);

    /**
     * Ordered management-target closure required from the Alibaba Cloud RAM manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List.of(
            Builder.ENTERPRISE_USERS,
            Builder.ENTERPRISE_USER,
            Builder.ENTERPRISE_GROUPS,
            Builder.ENTERPRISE_GROUP,
            Builder.ENTERPRISE_GROUP_MEMBERS,
            Builder.ENTERPRISE_ROLES,
            Builder.ENTERPRISE_ROLE);

    /**
     * Selected immutable Alibaba Cloud Enterprise Variant.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated Alibaba Cloud Enterprise deployment options.
     */
    private final AliyunOptions options;

    /**
     * Caller-owned execution services.
     */
    private final DriverServices services;

    /**
     * Resolved official Alibaba Cloud Enterprise resource targets.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Creates one Source-isolated Alibaba Cloud RAM realm adapter.
     *
     * @param spaceId  registration space used for isolation
     * @param sourceId registered Source identifier
     * @param manifest exact Alibaba Cloud manifest
     * @param variant  selected Enterprise Variant
     * @param options  validated Enterprise options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or collaborator is {@code null}
     * @throws ValidateException        if the manifest, Variant, options, protocol, or targets are inconsistent
     */
    public AliyunRealmAdapter(final String spaceId, final String sourceId, final AliyunManifest manifest,
            final VariantManifest.Variant variant, final AliyunOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "Alibaba Cloud Enterprise space id must not be blank");
        Assert.notBlank(sourceId, "Alibaba Cloud Enterprise Source id must not be blank");
        final AliyunManifest selectedManifest = Assert.notNull(manifest, "Alibaba Cloud manifest must not be null");
        this.variant = Assert.notNull(variant, "Alibaba Cloud Enterprise Variant must not be null");
        this.options = Assert.notNull(options, "Alibaba Cloud Enterprise options must not be null");
        this.services = Assert.notNull(services, "Alibaba Cloud Enterprise services must not be null");
        if (!AliyunManifest.ID.equals(selectedManifest.vendor()) || !AliyunManifest.RAM.equals(this.variant.variant())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || this.variant.protocol() != Protocol.HTTPS || !AliyunManifest.ID.equals(this.options.vendor())
                || !AliyunManifest.RAM.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || !this.options.scopes().isEmpty()
                || this.options.credential().type() != org.miaixz.bus.auth.Credential.Type.SHARED_SECRET) {
            throw new ValidateException("Alibaba Cloud realm adapter requires the frozen management Variant");
        }
        this.services.securityBaseline().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (!List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("Alibaba Cloud Enterprise manifest has an invalid management target set");
        }
    }

    /**
     * Creates one official marker-based RAM page query.
     *
     * @param marker optional official continuation marker
     * @param limit  outward page limit
     * @return mutable query map owned by the caller
     */
    private static Map<String, String> pageQuery(final Optional<String> marker, final int limit) {
        final Map<String, String> query = new LinkedHashMap<>();
        query.put("MaxItems", Integer.toString(Math.min(limit, Normal._1000)));
        if (marker.isPresent()) {
            query.put("Marker", requireText(marker.getOrNull(), "Alibaba Cloud RAM page marker"));
        }
        return query;
    }

    /**
     * Selects the initial or requested stable parent from a deterministic replay.
     *
     * @param items    replayed parent projections
     * @param parentId optional selected identifier
     * @param <T>      parent projection type
     * @return validated parent replay result
     */
    private static <T extends Parent> ParentPage<T> parent(final List<T> items, final Optional<String> parentId) {
        if (items.isEmpty()) {
            return new ParentPage<>(items, -1, Optional.empty());
        }
        if (parentId.isEmpty()) {
            return new ParentPage<>(items, 0, Optional.of(items.get(0)));
        }
        for (int index = 0; index < items.size(); index++) {
            if (parentId.getOrNull().equals(items.get(index).id())) {
                return new ParentPage<>(items, index, Optional.of(items.get(index)));
            }
        }
        throw new ValidateException("Alibaba Cloud RAM replay parent is no longer visible");
    }

    /**
     * Resolves a RAM UserName from one supported user Principal ARN.
     *
     * @param principal official trust-policy Principal value
     * @return exact UserName when the Principal denotes a RAM User
     */
    private static Optional<String> principalUserName(final String principal) {
        final String value = requireText(principal, "Alibaba Cloud RAM policy Principal");
        final String prefix = "acs:ram::";
        final String separator = ":user/";
        if (!value.startsWith(prefix)) {
            return Optional.empty();
        }
        final int split = value.indexOf(separator, prefix.length());
        if (split <= prefix.length() || split + separator.length() >= value.length()) {
            return Optional.empty();
        }
        final String name = UrlDecoder.decode(value.substring(split + separator.length()), Charset.UTF_8);
        return StringKit.hasText(name) && name.equals(StringKit.trim(name)) ? Optional.of(name) : Optional.empty();
    }

    /**
     * Canonicalizes RAM query names and values by strict RFC 3986 encoding and lexical sorting.
     *
     * @param query exact query members
     * @return canonical query string
     */
    private static String canonicalQuery(final Map<String, String> query) {
        final List<String> members = new ArrayList<>();
        for (Map.Entry<String, String> entry : query.entrySet()) {
            final String name = RFC3986.QUERY_PARAM_NAME_STRICT
                    .encode(requireText(entry.getKey(), "RAM query name"), Charset.UTF_8);
            final String value = RFC3986.QUERY_PARAM_VALUE_STRICT
                    .encode(requireText(entry.getValue(), "RAM query value"), Charset.UTF_8);
            members.add(name + Symbol.EQUAL + value);
        }
        members.sort(Comparator.naturalOrder());
        return String.join(Symbol.AND, members);
    }

    /**
     * Parses one minimal RAM User.
     *
     * @param value decoded object
     * @return validated User projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        final String name = requiredString(value, "UserName");
        return new User(requiredString(value, "UserId"), name, fallback(value, "DisplayName", () -> name));
    }

    /**
     * Parses one minimal RAM Group.
     *
     * @param value decoded object
     * @return validated Group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        return new Group(requiredString(value, "GroupId"), requiredString(value, "GroupName"));
    }

    /**
     * Parses one minimal RAM Role.
     *
     * @param value decoded object
     * @return validated Role projection
     */
    private static Role role(final JsonValue.ObjectValue value) {
        return new Role(requiredString(value, "RoleId"), requiredString(value, "RoleName"));
    }

    /**
     * Converts one user projection to a normalized resource.
     *
     * @param user       user projection
     * @param observedAt observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()), orderedIdentifier("UserName", user.name()),
                user.displayName(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Converts one RAM Group projection to a normalized resource.
     *
     * @param group      group projection
     * @param observedAt observation instant
     * @return immutable Group resource
     */
    private static Realm.Resource groupResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.GROUP, group.id()),
                orderedIdentifier("GroupName", group.name()), group.name(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES,
                observedAt);
    }

    /**
     * Converts one RAM Role projection to a normalized resource.
     *
     * @param role       role projection
     * @param observedAt observation instant
     * @return immutable Role resource
     */
    private static Realm.Resource roleResource(final Role role, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.ROLE, role.id()), orderedIdentifier("RoleName", role.name()),
                role.name(), Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one normalized User-to-Group membership relation.
     *
     * @param userId     stable User identifier
     * @param groupId    stable Group identifier
     * @param observedAt observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(final String userId, final String groupId, final Instant observedAt) {
        return new Realm.Relation(new Realm.RelationKey(Realm.RelationKind.MEMBER,
                new Realm.Key(Realm.Kind.USER, userId), new Realm.Key(Realm.Kind.GROUP, groupId)), EMPTY_ATTRIBUTES,
                observedAt);
    }

    /**
     * Creates one normalized User-to-Role assignment relation.
     *
     * @param userId     stable User identifier
     * @param roleId     stable Role identifier
     * @param principal  exact non-secret official Principal ARN
     * @param observedAt observation instant
     * @return immutable role assignment relation
     */
    private static Realm.Relation roleRelation(
            final String userId,
            final String roleId,
            final String principal,
            final Instant observedAt) {
        final JsonValue.ObjectValue attributes = new JsonValue.ObjectValue(
                Map.of("principal", new JsonValue.StringValue(principal)));
        return new Realm.Relation(new Realm.RelationKey(Realm.RelationKind.ROLE_MEMBER,
                new Realm.Key(Realm.Kind.USER, userId), new Realm.Key(Realm.Kind.ROLE, roleId)), attributes,
                observedAt);
    }

    /**
     * Creates one immutable single identifier map.
     *
     * @param name  identifier name
     * @param value identifier value
     * @return immutable ordered map
     */
    private static Map<String, String> orderedIdentifier(final String name, final String value) {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put(name, value);
        return Collections.unmodifiableMap(result);
    }

    /**
     * Adds one resource with page-local conflict detection.
     *
     * @param values   resource map
     * @param resource candidate resource
     * @param label    projection label
     */
    private static void putResource(
            final Map<Realm.Key, Realm.Resource> values,
            final Realm.Resource resource,
            final String label) {
        final Realm.Resource previous = values.putIfAbsent(resource.key(), resource);
        if (previous != null && !previous.equals(resource)) {
            throw new ValidateException(label + " contains a conflicting resource key");
        }
    }

    /**
     * Adds one relation with page-local conflict detection.
     *
     * @param values   relation map
     * @param relation candidate relation
     * @param label    projection label
     */
    private static void putRelation(
            final Map<Realm.RelationKey, Realm.Relation> values,
            final Realm.Relation relation,
            final String label) {
        final Realm.Relation previous = values.putIfAbsent(relation.key(), relation);
        if (previous != null && !previous.equals(relation)) {
            throw new ValidateException(label + " contains a conflicting relation key");
        }
    }

    /**
     * Reads a required object member.
     *
     * @param object parent object
     * @param name   member name
     * @return object member
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        return requiredObject(object.values().get(name), name);
    }

    /**
     * Narrows one value to an object.
     *
     * @param value decoded value
     * @param label value label
     * @return object value
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue value, final String label) {
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException(label + " must be a JSON object");
        }
        return object;
    }

    /**
     * Reads a required array member.
     *
     * @param object parent object
     * @param name   member name
     * @return array values
     */
    private static List<JsonValue> requiredArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException(name + " must be a JSON array");
        }
        return array.values();
    }

    /**
     * Reads a required non-blank string member.
     *
     * @param object parent object
     * @param name   member name
     * @return validated string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string");
        }
        return requireText(text.value(), name);
    }

    /**
     * Reads one optional non-blank string member.
     *
     * @param object parent object
     * @param name   member name
     * @return validated optional string
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a JSON string or null");
        }
        return StringKit.hasText(text.value()) ? Optional.of(requireText(text.value(), name)) : Optional.empty();
    }

    /**
     * Reads one required boolean member.
     *
     * @param object parent object
     * @param name   member name
     * @return exact boolean value
     */
    private static boolean requiredBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.BooleanValue flag)) {
            throw new ValidateException(name + " must be a JSON boolean");
        }
        return flag.value();
    }

    /**
     * Reads a required integer member.
     *
     * @param object parent object
     * @param name   member name
     * @return exact integer
     */
    private static int requiredInt(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.NumberValue number) {
            try {
                return number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException(name + " must be an exact integer", cause);
            }
        }
        if (value instanceof JsonValue.StringValue text) {
            try {
                return Integer.parseInt(text.value());
            } catch (NumberFormatException cause) {
                throw new ValidateException(name + " must be an exact integer", cause);
            }
        }
        throw new ValidateException(name + " must be an integer");
    }

    /**
     * Reads a non-negative integer member.
     *
     * @param object parent object
     * @param name   member name
     * @return non-negative integer
     */
    private static int nonNegativeInt(final JsonValue.ObjectValue object, final String name) {
        final int value = requiredInt(object, name);
        if (value < 0) {
            throw new ValidateException(name + " must not be negative");
        }
        return value;
    }

    /**
     * Reads an explicit nullable cursor string.
     *
     * @param object parent object
     * @param name   member name
     * @return optional string
     */
    private static Optional<String> nullableString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a string or null");
        }
        return Optional.of(requireText(text.value(), name));
    }

    /**
     * Resolves a primary display string or invokes its declared fallback.
     *
     * @param object   parent object
     * @param name     primary member name
     * @param fallback fallback supplier
     * @return validated primary or fallback string
     */
    private static String fallback(
            final JsonValue.ObjectValue object,
            final String name,
            final java.util.function.Supplier<String> fallback) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return fallback.get();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a string or null");
        }
        return StringKit.hasText(text.value()) ? requireText(text.value(), name) : fallback.get();
    }

    /**
     * Validates an exact object member closure.
     *
     * @param object   decoded object
     * @param expected expected names
     */
    private static void exact(final JsonValue.ObjectValue object, final Set<String> expected) {
        if (!object.values().keySet().equals(expected)) {
            throw new ValidateException("Alibaba Cloud cursor contains an invalid member set");
        }
    }

    /**
     * Decodes canonical requested kinds.
     *
     * @param values kind code values
     * @return stable ordered kinds
     */
    private static List<Realm.Kind> kinds(final List<JsonValue> values) {
        final LinkedHashSet<Realm.Kind> result = new LinkedHashSet<>();
        int previous = 0;
        for (JsonValue value : values) {
            if (!(value instanceof JsonValue.NumberValue number)) {
                throw new ValidateException("Alibaba Cloud cursor kind must be numeric");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Alibaba Cloud cursor kind is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !result.add(kind)) {
                throw new ValidateException("Alibaba Cloud cursor kinds are not canonical");
            }
            previous = code;
        }
        return List.copyOf(result);
    }

    /**
     * Resolves one supported kind code.
     *
     * @param code stable code
     * @return supported kind
     */
    private static Realm.Kind kind(final int code) {
        for (Realm.Kind kind : SUPPORTED_KINDS) {
            if (kind.code() == code) {
                return kind;
            }
        }
        throw new ValidateException("Alibaba Cloud cursor contains an unsupported kind");
    }

    /**
     * Validates the requested kind closure.
     *
     * @param kinds requested kinds
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("Alibaba Cloud snapshot contains an unsupported kind");
        }
    }

    /**
     * Validates text without trimming it.
     *
     * @param value text value
     * @param label semantic label
     * @return original text
     */
    private static String requireText(final String value, final String label) {
        final String result;
        try {
            result = Assert.notBlank(value, label + " must not be blank");
        } catch (IllegalArgumentException cause) {
            throw new ValidateException(label + " must not be blank", cause);
        }
        if (!result.equals(StringKit.trim(result))) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return result;
    }

    /**
     * Converts an optional string to JSON null or string.
     *
     * @param value optional string
     * @return JSON value
     */
    private static JsonValue nullable(final Optional<String> value) {
        return value.isPresent() ? new JsonValue.StringValue(value.getOrNull()) : JsonValue.NullValue.instance();
    }

    /**
     * Creates one JSON integer.
     *
     * @param value integer value
     * @return JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Narrows one outcome stage through the declared response class.
     *
     * @param stage        source stage
     * @param responseType response class
     * @param <S>          response type
     * @return narrowed stage
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "Alibaba Cloud delegated outcome is unsupported");
        });
    }

    /**
     * Propagates a non-success outcome.
     *
     * @param outcome source outcome
     * @param <T>     target type
     * @return propagated outcome
     */
    private static <T> Outcome<T> propagate(final Outcome<?> outcome) {
        return switch (outcome) {
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> failed(ErrorCode._500, "Alibaba Cloud internal outcome cannot be propagated");
        };
    }

    /**
     * Creates a completed stage.
     *
     * @param outcome outcome
     * @param <T>     value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates one rejection.
     *
     * @param code        error code
     * @param description description
     * @param <T>         value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final Errors code, final String description) {
        return rejected(code, description, Map.of());
    }

    /**
     * Creates one detailed rejection.
     *
     * @param code        error code
     * @param description description
     * @param details     safe details
     * @param <T>         value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.rejected(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Creates one failure.
     *
     * @param code        error code
     * @param description description
     * @param <T>         value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates one detailed failure.
     *
     * @param code        error code
     * @param description description
     * @param details     safe details
     * @param <T>         value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the selected enterprise capability manifest.
     *
     * @return immutable describe, snapshot, and retrieve manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes one exact Alibaba Cloud Enterprise management capability.
     *
     * @param capability runtime-selected capability
     * @param request    capability-specific request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        response type
     * @return asynchronous typed outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Alibaba Cloud enterprise capability must not be null");
        Assert.notNull(context, "Alibaba Cloud enterprise context must not be null");
        Assert.notNull(timeout, "Alibaba Cloud enterprise timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected(ErrorCode._400, "Alibaba Cloud enterprise capability is not declared"));
        }
        if (capability.equals(Realm.describe(AliyunManifest.ID)) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(AliyunManifest.enterpriseDescription())));
        }
        if (capability.equals(Realm.snapshot(AliyunManifest.ID)) && request instanceof Realm.Snapshot value) {
            return narrow(snapshot(value, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.retrieve(AliyunManifest.ID)) && request instanceof Realm.Retrieve value) {
            return narrow(retrieve(value, context, timeout), capability.responseType());
        }
        return completed(
                rejected(ErrorCode._400, "Alibaba Cloud enterprise request does not match the capability contract"));
    }

    /**
     * Performs no action because this adapter owns no cache or shared service.
     */
    @Override
    public void close() {
    }

    /**
     * Validates and executes one snapshot under a fresh Secret lease.
     *
     * @param request snapshot request
     * @param context invocation context
     * @param timeout shared timeout
     * @return asynchronous snapshot outcome
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
            return completed(rejected(ErrorCode._400, "Alibaba Cloud Enterprise snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Alibaba Cloud Enterprise snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return withSecret(context, timeout, secret -> switch (state.phase()) {
            case USERS -> users(secret, request, state, observedAt, timeout);
            case GROUPS -> groups(secret, request, state, observedAt, timeout);
            case GROUP_MEMBERS -> groupMembers(secret, request, state, observedAt, timeout);
            case ROLES -> roles(secret, request, state, observedAt, timeout);
            case ROLE_TRUST -> roleTrust(secret, request, state, observedAt, timeout);
        });
    }

    /**
     * Reads one RAM user page.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      user cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized user page
     */
    private Outcome<Realm.Page> users(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<WirePage<User>> fetched = userPage(
                state.position().next(),
                request.limit(),
                secret,
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        for (User user : success.value().items()) {
            putResource(resources, userResource(user, observedAt), "Alibaba Cloud RAM user page");
        }
        return continueOrComplete(resources, Map.of(), state, success.value().next());
    }

    /**
     * Reads one RAM group page.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      group cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized group page
     */
    private Outcome<Realm.Page> groups(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<WirePage<Group>> fetched = groupPage(
                state.position().next(),
                request.limit(),
                secret,
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        for (Group group : success.value().items()) {
            putResource(resources, groupResource(group, observedAt), "Alibaba Cloud RAM group page");
        }
        return continueOrComplete(resources, Map.of(), state, success.value().next());
    }

    /**
     * Reads one RAM group-membership page under deterministic group replay.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      membership cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized membership relations
     */
    private Outcome<Realm.Page> groupMembers(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<ParentPage<Group>> replayed = groupParent(
                state.position().parentId(),
                secret,
                timeout,
                "snapshot");
        if (!(replayed instanceof Outcome.Succeeded<ParentPage<Group>> replaySuccess)) {
            return propagate(replayed);
        }
        if (replaySuccess.value().parent().isEmpty()) {
            return complete(List.of(), List.of(), state);
        }
        final Group group = replaySuccess.value().parent().getOrNull();
        final Outcome<WirePage<User>> fetched = groupMemberPage(
                group.name(),
                state.position().next(),
                request.limit(),
                secret,
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.RelationKey, Realm.Relation> relations = new LinkedHashMap<>();
        for (User user : success.value().items()) {
            putRelation(
                    relations,
                    memberRelation(user.id(), group.id(), observedAt),
                    "Alibaba Cloud RAM group-member page");
        }
        if (success.value().next().isPresent()) {
            return output(
                    List.of(),
                    List.copyOf(relations.values()),
                    state,
                    Position.dependent(success.value().next(), group.id(), 0));
        }
        return advanceParent(List.of(), List.copyOf(relations.values()), state, replaySuccess.value());
    }

    /**
     * Reads one RAM role page.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      role cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized role page
     */
    private Outcome<Realm.Page> roles(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<WirePage<Role>> fetched = rolePage(
                state.position().next(),
                request.limit(),
                secret,
                timeout,
                "snapshot");
        if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        for (Role role : success.value().items()) {
            putResource(resources, roleResource(role, observedAt), "Alibaba Cloud RAM role page");
        }
        return continueOrComplete(resources, Map.of(), state, success.value().next());
    }

    /**
     * Reads one role trust policy and emits resolvable RAM user assignments with bounded replay.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      role-trust cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized role-member relations
     */
    private Outcome<Realm.Page> roleTrust(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<ParentPage<Role>> replayed = roleParent(state.position().parentId(), secret, timeout, "snapshot");
        if (!(replayed instanceof Outcome.Succeeded<ParentPage<Role>> replaySuccess)) {
            return propagate(replayed);
        }
        if (replaySuccess.value().parent().isEmpty()) {
            return complete(List.of(), List.of(), state);
        }
        final Role role = replaySuccess.value().parent().getOrNull();
        final Outcome<RoleTrust> fetched = roleTrust(role.name(), secret, timeout, "snapshot", false);
        if (!(fetched instanceof Outcome.Succeeded<RoleTrust> trustSuccess)) {
            return propagate(fetched);
        }
        final Outcome<Map<String, User>> users = usersByName(secret, timeout, "snapshot");
        if (!(users instanceof Outcome.Succeeded<Map<String, User>> userSuccess)) {
            return propagate(users);
        }
        final List<Realm.Relation> projected = new ArrayList<>();
        final Map<Realm.RelationKey, Realm.Relation> unique = new LinkedHashMap<>();
        for (String principal : trustSuccess.value().principals()) {
            final Optional<String> name = principalUserName(principal);
            if (name.isEmpty()) {
                continue;
            }
            final User user = userSuccess.value().get(name.getOrNull());
            if (user != null) {
                putRelation(
                        unique,
                        roleRelation(user.id(), role.id(), principal, observedAt),
                        "Alibaba Cloud RAM role trust");
            }
        }
        projected.addAll(unique.values());
        final int offset = state.position().relationOffset();
        if (offset > projected.size()) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM role relation replay offset is invalid");
        }
        final int end = Math.min(projected.size(), offset + request.limit());
        final List<Realm.Relation> output = List.copyOf(projected.subList(offset, end));
        if (end < projected.size()) {
            return output(List.of(), output, state, Position.dependent(Optional.empty(), role.id(), end));
        }
        return advanceParent(List.of(), output, state, replaySuccess.value());
    }

    /**
     * Validates and executes one stable-key retrieval under a fresh Secret lease.
     *
     * @param request retrieval request
     * @param context invocation context
     * @param timeout shared timeout
     * @return asynchronous retrieval outcome
     */
    private CompletionStage<Outcome<Realm.Retrieved>> retrieve(
            final Realm.Retrieve request,
            final Context context,
            final Timeout timeout) {
        if (!SUPPORTED_KINDS.contains(request.key().kind())) {
            return completed(rejected(ErrorCode._400, "Alibaba Cloud RAM retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Alibaba Cloud RAM retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return withSecret(context, timeout, secret -> retrieve(secret, request.key(), observedAt, timeout));
    }

    /**
     * Retrieves one RAM User, Group, or Role by stable identifier through list-to-name resolution.
     *
     * @param secret     open administrator-token lease
     * @param key        stable resource key
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return retrieved resource or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieve(
            final SecretLease secret,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        return switch (key.kind()) {
            case USER -> retrieveUser(secret, key, observedAt, timeout);
            case GROUP -> retrieveGroup(secret, key, observedAt, timeout);
            case ROLE -> retrieveRole(secret, key, observedAt, timeout);
            default -> rejected(ErrorCode._400, "Alibaba Cloud RAM retrieve kind is unsupported");
        };
    }

    /**
     * Advances a dependent phase to the following stable parent.
     *
     * @param resources normalized resources
     * @param relations normalized relations
     * @param state     current dependent phase state
     * @param parent    replayed parent collection
     * @param <T>       parent projection type
     * @return continued or completed normalized page
     */
    private <T extends Parent> Outcome<Realm.Page> advanceParent(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final ParentPage<T> parent) {
        final int following = parent.index() + 1;
        if (following < parent.items().size()) {
            return output(
                    resources,
                    relations,
                    state,
                    Position.dependent(Optional.empty(), parent.items().get(following).id(), 0));
        }
        return complete(resources, relations, state);
    }

    /**
     * Loads, validates, executes with, and closes one administrator-token lease.
     *
     * @param context   invocation context
     * @param timeout   shared timeout
     * @param operation synchronous operation using the open lease
     * @param <T>       successful value type
     * @return asynchronous operation outcome
     */
    private <T> CompletionStage<Outcome<T>> withSecret(
            final Context context,
            final Timeout timeout,
            final Function<SecretLease, Outcome<T>> operation) {
        final CompletionStage<Outcome<SecretLoader.Record>> loaded;
        try {
            loaded = services.secretLoader()
                    .load(new SecretLoader.Request(services.registration(), options.credential()), context, timeout);
        } catch (RuntimeException ignored) {
            return completed(
                    failed(ErrorCode._500, "Alibaba Cloud Enterprise Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "Alibaba Cloud Enterprise Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "Alibaba Cloud Enterprise Secret Loader stage failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> execute(success.value(), timeout, operation);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(
                                    ErrorCode._500,
                                    "Alibaba Cloud Enterprise Secret Loader returned an unsupported outcome"));
                });
    }

    /**
     * Parses one loaded record and binds its lease to one terminal asynchronous close point.
     *
     * @param loaded    loaded Secret record
     * @param timeout   shared timeout
     * @param operation synchronous operation using the lease
     * @param <T>       successful value type
     * @return asynchronous closed operation outcome
     */
    private <T> CompletionStage<Outcome<T>> execute(
            final SecretLoader.Record loaded,
            final Timeout timeout,
            final Function<SecretLease, Outcome<T>> operation) {
        final SecretLease raw = loaded == null ? null : loaded.lease();
        final SecretLease secret;
        try {
            secret = services.secretParser().parse(services.registration(), options.credential(), loaded);
        } catch (RuntimeException ignored) {
            if (raw != null) {
                raw.close();
            }
            return completed(failed(ErrorCode._500, "Alibaba Cloud Enterprise loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Alibaba Cloud Enterprise operation has no remaining timeout");
                }
                try {
                    return Assert
                            .notNull(operation.apply(secret), "Alibaba Cloud Enterprise operation returned no outcome");
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "Alibaba Cloud Enterprise operation timed out");
                } catch (ValidateException ignored) {
                    return failed(ErrorCode._502, "Alibaba Cloud Enterprise returned an invalid projection");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "Alibaba Cloud Enterprise transport is unavailable");
                }
            }, services.executor()).whenComplete((ignored, cause) -> secret.close());
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "Alibaba Cloud Enterprise operation could not be scheduled"));
        }
    }

    /**
     * Reads one official RAM user page.
     *
     * @param marker    optional official continuation marker
     * @param limit     outward page limit
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated RAM user page
     */
    private Outcome<WirePage<User>> userPage(
            final Optional<String> marker,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        return collection(
                "ListUsers",
                Builder.ENTERPRISE_USERS,
                pageQuery(marker, limit),
                "Users",
                "User",
                secret,
                timeout,
                operation,
                AliyunRealmAdapter::user);
    }

    /**
     * Reads one official RAM group page.
     *
     * @param marker    optional official continuation marker
     * @param limit     outward page limit
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated RAM group page
     */
    private Outcome<WirePage<Group>> groupPage(
            final Optional<String> marker,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        return collection(
                "ListGroups",
                Builder.ENTERPRISE_GROUPS,
                pageQuery(marker, limit),
                "Groups",
                "Group",
                secret,
                timeout,
                operation,
                AliyunRealmAdapter::group);
    }

    /**
     * Reads one official RAM group-member page.
     *
     * @param groupName stable RAM group name
     * @param marker    optional official continuation marker
     * @param limit     outward page limit
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated RAM member page
     */
    private Outcome<WirePage<User>> groupMemberPage(
            final String groupName,
            final Optional<String> marker,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Map<String, String> query = new LinkedHashMap<>(pageQuery(marker, limit));
        query.put("GroupName", requireText(groupName, "Alibaba Cloud RAM group name"));
        return collection(
                "ListUsersForGroup",
                Builder.ENTERPRISE_GROUP_MEMBERS,
                query,
                "Users",
                "User",
                secret,
                timeout,
                operation,
                AliyunRealmAdapter::user);
    }

    /**
     * Reads one official RAM role page.
     *
     * @param marker    optional official continuation marker
     * @param limit     outward page limit
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return validated RAM role page
     */
    private Outcome<WirePage<Role>> rolePage(
            final Optional<String> marker,
            final int limit,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        return collection(
                "ListRoles",
                Builder.ENTERPRISE_ROLES,
                pageQuery(marker, limit),
                "Roles",
                "Role",
                secret,
                timeout,
                operation,
                AliyunRealmAdapter::role);
    }

    /**
     * Reads one official marker-paged RAM collection.
     *
     * @param action        official RAM action
     * @param targetName    manifest target key
     * @param query         action query
     * @param containerName collection container member
     * @param itemName      collection item member
     * @param secret        open AccessKey Secret lease
     * @param timeout       shared timeout
     * @param operation     safe operation label
     * @param parser        minimal item parser
     * @param <T>           item projection type
     * @return validated collection page
     */
    private <T> Outcome<WirePage<T>> collection(
            final String action,
            final String targetName,
            final Map<String, String> query,
            final String containerName,
            final String itemName,
            final SecretLease secret,
            final Timeout timeout,
            final String operation,
            final Function<JsonValue.ObjectValue, T> parser) {
        final Outcome<Optional<JsonValue.ObjectValue>> fetched = rpc(
                action,
                targetName,
                query,
                secret,
                timeout,
                operation,
                false);
        if (!(fetched instanceof Outcome.Succeeded<Optional<JsonValue.ObjectValue>> success)) {
            return propagate(fetched);
        }
        try {
            final JsonValue.ObjectValue response = success.value().getOrNull();
            final JsonValue.ObjectValue container = requiredObject(response, containerName);
            final List<T> items = new ArrayList<>();
            for (JsonValue value : requiredArray(container, itemName)) {
                items.add(parser.apply(requiredObject(value, "Alibaba Cloud RAM collection item")));
            }
            final boolean truncated = requiredBoolean(response, "IsTruncated");
            final Optional<String> marker = optionalString(response, "Marker");
            if (truncated != marker.isPresent()
                    || marker.isPresent() && marker.getOrNull().equals(query.get("Marker"))) {
                throw new ValidateException("Alibaba Cloud RAM continuation marker is inconsistent");
            }
            return Outcome.succeeded(new WirePage<>(items, marker));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an invalid collection response");
        }
    }

    /**
     * Retrieves one RAM User by its stable identifier.
     *
     * @param secret     open AccessKey Secret lease
     * @param key        requested stable key
     * @param observedAt observation instant
     * @param timeout    shared timeout
     * @return retrieved User or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveUser(
            final SecretLease secret,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<User>> resolved = findUser(key.externalId(), secret, timeout, "retrieve");
        if (!(resolved instanceof Outcome.Succeeded<Optional<User>> success)) {
            return propagate(resolved);
        }
        if (success.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Map<String, String> query = Map.of("UserName", success.value().getOrNull().name());
        final Outcome<Optional<JsonValue.ObjectValue>> fetched = rpc(
                "GetUser",
                Builder.ENTERPRISE_USER,
                query,
                secret,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<Optional<JsonValue.ObjectValue>> response)) {
            return propagate(fetched);
        }
        if (response.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        try {
            final User user = user(requiredObject(response.value().getOrNull(), "User"));
            if (!key.externalId().equals(user.id())) {
                return failed(ErrorCode._502, "Alibaba Cloud RAM returned a conflicting User identifier");
            }
            return Outcome.succeeded(new Realm.Retrieved(Optional.of(userResource(user, observedAt))));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an invalid User response");
        }
    }

    /**
     * Retrieves one RAM Group by its stable identifier.
     *
     * @param secret     open AccessKey Secret lease
     * @param key        requested stable key
     * @param observedAt observation instant
     * @param timeout    shared timeout
     * @return retrieved Group or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveGroup(
            final SecretLease secret,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<Group>> resolved = findGroup(key.externalId(), secret, timeout, "retrieve");
        if (!(resolved instanceof Outcome.Succeeded<Optional<Group>> success)) {
            return propagate(resolved);
        }
        if (success.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Map<String, String> query = Map.of("GroupName", success.value().getOrNull().name());
        final Outcome<Optional<JsonValue.ObjectValue>> fetched = rpc(
                "GetGroup",
                Builder.ENTERPRISE_GROUP,
                query,
                secret,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<Optional<JsonValue.ObjectValue>> response)) {
            return propagate(fetched);
        }
        if (response.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        try {
            final Group group = group(requiredObject(response.value().getOrNull(), "Group"));
            if (!key.externalId().equals(group.id())) {
                return failed(ErrorCode._502, "Alibaba Cloud RAM returned a conflicting Group identifier");
            }
            return Outcome.succeeded(new Realm.Retrieved(Optional.of(groupResource(group, observedAt))));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an invalid Group response");
        }
    }

    /**
     * Retrieves one RAM Role by its stable identifier.
     *
     * @param secret     open AccessKey Secret lease
     * @param key        requested stable key
     * @param observedAt observation instant
     * @param timeout    shared timeout
     * @return retrieved Role or explicit absence
     */
    private Outcome<Realm.Retrieved> retrieveRole(
            final SecretLease secret,
            final Realm.Key key,
            final Instant observedAt,
            final Timeout timeout) {
        final Outcome<Optional<Role>> resolved = findRole(key.externalId(), secret, timeout, "retrieve");
        if (!(resolved instanceof Outcome.Succeeded<Optional<Role>> success)) {
            return propagate(resolved);
        }
        if (success.value().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Outcome<RoleTrust> fetched = roleTrust(
                success.value().getOrNull().name(),
                secret,
                timeout,
                "retrieve",
                true);
        if (!(fetched instanceof Outcome.Succeeded<RoleTrust> response)) {
            return propagate(fetched);
        }
        if (response.value().role().isEmpty()) {
            return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
        }
        final Role role = response.value().role().getOrNull();
        if (!key.externalId().equals(role.id())) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned a conflicting Role identifier");
        }
        return Outcome.succeeded(new Realm.Retrieved(Optional.of(roleResource(role, observedAt))));
    }

    /**
     * Finds one RAM User by stable identifier without imposing a local collection bound.
     *
     * @param id        stable User identifier
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return matching User or explicit absence
     */
    private Outcome<Optional<User>> findUser(
            final String id,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        Optional<String> marker = Optional.empty();
        do {
            final Outcome<WirePage<User>> fetched = userPage(marker, Normal._1000, secret, timeout, operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
                return propagate(fetched);
            }
            for (User user : success.value().items()) {
                if (id.equals(user.id())) {
                    return Outcome.succeeded(Optional.of(user));
                }
            }
            marker = success.value().next();
        } while (marker.isPresent());
        return Outcome.succeeded(Optional.empty());
    }

    /**
     * Finds one RAM Group by stable identifier without imposing a local collection bound.
     *
     * @param id        stable Group identifier
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return matching Group or explicit absence
     */
    private Outcome<Optional<Group>> findGroup(
            final String id,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        Optional<String> marker = Optional.empty();
        do {
            final Outcome<WirePage<Group>> fetched = groupPage(marker, Normal._1000, secret, timeout, operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
                return propagate(fetched);
            }
            for (Group group : success.value().items()) {
                if (id.equals(group.id())) {
                    return Outcome.succeeded(Optional.of(group));
                }
            }
            marker = success.value().next();
        } while (marker.isPresent());
        return Outcome.succeeded(Optional.empty());
    }

    /**
     * Finds one RAM Role by stable identifier without imposing a local collection bound.
     *
     * @param id        stable Role identifier
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return matching Role or explicit absence
     */
    private Outcome<Optional<Role>> findRole(
            final String id,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        Optional<String> marker = Optional.empty();
        do {
            final Outcome<WirePage<Role>> fetched = rolePage(marker, Normal._1000, secret, timeout, operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> success)) {
                return propagate(fetched);
            }
            for (Role role : success.value().items()) {
                if (id.equals(role.id())) {
                    return Outcome.succeeded(Optional.of(role));
                }
            }
            marker = success.value().next();
        } while (marker.isPresent());
        return Outcome.succeeded(Optional.empty());
    }

    /**
     * Replays the complete RAM Group collection and selects one stable parent.
     *
     * @param parentId  optional stable parent identifier
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return deterministic Group replay result
     */
    private Outcome<ParentPage<Group>> groupParent(
            final Optional<String> parentId,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Outcome<List<Group>> fetched = allGroups(secret, timeout, operation);
        return fetched instanceof Outcome.Succeeded<List<Group>> success
                ? Outcome.succeeded(parent(success.value(), parentId))
                : propagate(fetched);
    }

    /**
     * Replays the complete RAM Role collection and selects one stable parent.
     *
     * @param parentId  optional stable parent identifier
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return deterministic Role replay result
     */
    private Outcome<ParentPage<Role>> roleParent(
            final Optional<String> parentId,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Outcome<List<Role>> fetched = allRoles(secret, timeout, operation);
        return fetched instanceof Outcome.Succeeded<List<Role>> success
                ? Outcome.succeeded(parent(success.value(), parentId))
                : propagate(fetched);
    }

    /**
     * Reads every visible RAM Group by following only official markers.
     *
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return immutable visible Group collection
     */
    private Outcome<List<Group>> allGroups(final SecretLease secret, final Timeout timeout, final String operation) {
        final List<Group> groups = new ArrayList<>();
        Optional<String> marker = Optional.empty();
        do {
            final Outcome<WirePage<Group>> fetched = groupPage(marker, Normal._1000, secret, timeout, operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Group>> success)) {
                return propagate(fetched);
            }
            groups.addAll(success.value().items());
            marker = success.value().next();
        } while (marker.isPresent());
        return Outcome.succeeded(List.copyOf(groups));
    }

    /**
     * Reads every visible RAM Role by following only official markers.
     *
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return immutable visible Role collection
     */
    private Outcome<List<Role>> allRoles(final SecretLease secret, final Timeout timeout, final String operation) {
        final List<Role> roles = new ArrayList<>();
        Optional<String> marker = Optional.empty();
        do {
            final Outcome<WirePage<Role>> fetched = rolePage(marker, Normal._1000, secret, timeout, operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<Role>> success)) {
                return propagate(fetched);
            }
            roles.addAll(success.value().items());
            marker = success.value().next();
        } while (marker.isPresent());
        return Outcome.succeeded(List.copyOf(roles));
    }

    /**
     * Reads and indexes every visible RAM User by exact UserName.
     *
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return immutable UserName index
     */
    private Outcome<Map<String, User>> usersByName(
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        final Map<String, User> users = new LinkedHashMap<>();
        Optional<String> marker = Optional.empty();
        do {
            final Outcome<WirePage<User>> fetched = userPage(marker, Normal._1000, secret, timeout, operation);
            if (!(fetched instanceof Outcome.Succeeded<WirePage<User>> success)) {
                return propagate(fetched);
            }
            for (User user : success.value().items()) {
                final User previous = users.putIfAbsent(user.name(), user);
                if (previous != null && !previous.equals(user)) {
                    return failed(ErrorCode._502, "Alibaba Cloud RAM returned a conflicting UserName");
                }
            }
            marker = success.value().next();
        } while (marker.isPresent());
        return Outcome.succeeded(Collections.unmodifiableMap(users));
    }

    /**
     * Reads one RAM Role and its official assume-role policy.
     *
     * @param roleName  exact RAM RoleName
     * @param secret    open AccessKey Secret lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @param missing   whether EntityNotExist represents normal absence
     * @return validated Role and normalized trust principals
     */
    private Outcome<RoleTrust> roleTrust(
            final String roleName,
            final SecretLease secret,
            final Timeout timeout,
            final String operation,
            final boolean missing) {
        final Outcome<Optional<JsonValue.ObjectValue>> fetched = rpc(
                "GetRole",
                Builder.ENTERPRISE_ROLE,
                Map.of("RoleName", requireText(roleName, "Alibaba Cloud RAM role name")),
                secret,
                timeout,
                operation,
                missing);
        if (!(fetched instanceof Outcome.Succeeded<Optional<JsonValue.ObjectValue>> success)) {
            return propagate(fetched);
        }
        if (success.value().isEmpty()) {
            return Outcome.succeeded(new RoleTrust(Optional.empty(), List.of()));
        }
        try {
            final JsonValue.ObjectValue raw = requiredObject(success.value().getOrNull(), "Role");
            final Role role = role(raw);
            final String encoded = requiredString(raw, "AssumeRolePolicyDocument");
            return Outcome.succeeded(new RoleTrust(Optional.of(role), principals(encoded)));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an invalid Role response");
        }
    }

    /**
     * Extracts RAM Principal members from one URL-encoded assume-role policy.
     *
     * @param encoded URL-encoded policy document
     * @return immutable ordered Principal closure
     */
    private List<String> principals(final String encoded) {
        final String decoded = UrlDecoder
                .decode(requireText(encoded, "Alibaba Cloud RAM assume-role policy"), Charset.UTF_8);
        final JsonValue.ObjectValue policy = requiredObject(
                services.jsonProvider()
                        .readValue(decoded.getBytes(Charset.UTF_8), Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true),
                "Alibaba Cloud RAM assume-role policy");
        final JsonValue statements = policy.values().get("Statement");
        final List<JsonValue> values;
        if (statements instanceof JsonValue.ArrayValue array) {
            values = array.values();
        } else if (statements instanceof JsonValue.ObjectValue object) {
            values = List.of(object);
        } else {
            throw new ValidateException("Alibaba Cloud RAM policy Statement must be an object or array");
        }
        final LinkedHashSet<String> principals = new LinkedHashSet<>();
        for (JsonValue value : values) {
            final JsonValue.ObjectValue statement = requiredObject(value, "Alibaba Cloud RAM policy Statement");
            final JsonValue principalValue = statement.values().get("Principal");
            if (!(principalValue instanceof JsonValue.ObjectValue principal)) {
                continue;
            }
            final JsonValue ram = principal.values().get("RAM");
            if (ram instanceof JsonValue.StringValue text) {
                principals.add(requireText(text.value(), "Alibaba Cloud RAM policy Principal"));
            } else if (ram instanceof JsonValue.ArrayValue array) {
                for (JsonValue member : array.values()) {
                    if (!(member instanceof JsonValue.StringValue text)) {
                        throw new ValidateException("Alibaba Cloud RAM policy Principal must contain strings");
                    }
                    principals.add(requireText(text.value(), "Alibaba Cloud RAM policy Principal"));
                }
            } else if (ram != null && !(ram instanceof JsonValue.NullValue)) {
                throw new ValidateException("Alibaba Cloud RAM policy Principal is invalid");
            }
        }
        return List.copyOf(principals);
    }

    /**
     * Executes one signed Alibaba Cloud RAM RPC.
     *
     * @param action     official RAM action
     * @param targetName manifest target key
     * @param query      exact action query
     * @param secret     open AccessKey Secret lease
     * @param timeout    shared timeout
     * @param operation  safe operation label
     * @param missing    whether EntityNotExist represents normal absence
     * @return decoded response object or explicit absence
     */
    private Outcome<Optional<JsonValue.ObjectValue>> rpc(
            final String action,
            final String targetName,
            final Map<String, String> query,
            final SecretLease secret,
            final Timeout timeout,
            final String operation,
            final boolean missing) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Alibaba Cloud RAM request has no remaining timeout");
        }
        final Endpoint endpoint = target(targetName);
        final Url base = endpoint.url();
        final String host = requireText(base.host(), "Alibaba Cloud RAM host");
        final String canonicalQuery = canonicalQuery(query);
        final String payloadHash = org.miaixz.bus.crypto.Builder.sha256Hex(Normal.EMPTY);
        final String timestamp = DateTimeFormatter.ISO_INSTANT
                .format(FabricX.clock(services.fabric()).now().truncatedTo(ChronoUnit.SECONDS));
        final String nonce = RandomKit.randomString(Normal._32);
        final Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HOST_HEADER, host);
        headers.put(ACTION_HEADER, requireText(action, "Alibaba Cloud RAM action"));
        headers.put(CONTENT_HASH_HEADER, payloadHash);
        headers.put(DATE_HEADER, timestamp);
        headers.put(NONCE_HEADER, nonce);
        headers.put(VERSION_HEADER, API_VERSION);
        final StringBuilder canonicalHeaders = new StringBuilder();
        for (String name : SIGNED_HEADER_NAMES) {
            canonicalHeaders.append(name).append(Symbol.C_COLON).append(headers.get(name)).append(Symbol.C_LF);
        }
        final String signedHeaders = String.join(Symbol.SEMICOLON, SIGNED_HEADER_NAMES);
        final String canonicalRequest = Http.Method.POST + Symbol.LF + Symbol.C_SLASH + Symbol.LF + canonicalQuery
                + Symbol.LF + canonicalHeaders + Symbol.LF + signedHeaders + Symbol.LF + payloadHash;
        final String stringToSign = SIGNATURE_ALGORITHM + Symbol.LF
                + org.miaixz.bus.crypto.Builder.sha256Hex(canonicalRequest);
        final String signature = org.miaixz.bus.crypto.Builder.hmacSha256(new String(secret.material()))
                .digestHex(stringToSign);
        final String authorization = SIGNATURE_ALGORITHM + " Credential=" + options.clientId() + ",SignedHeaders="
                + signedHeaders + ",Signature=" + signature;
        final String url = base.toString()
                + (canonicalQuery.isEmpty() ? Normal.EMPTY : Symbol.QUESTION_MARK + canonicalQuery);
        final Response response;
        try {
            response = FabricX.http(services.fabric(), Protocol.HTTPS, timeout).url(url).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).header(ACTION_HEADER, action)
                    .header(CONTENT_HASH_HEADER, payloadHash).header(DATE_HEADER, timestamp).header(NONCE_HEADER, nonce)
                    .header(VERSION_HEADER, API_VERSION).header(Http.Header.AUTHORIZATION, authorization)
                    .body(Normal.EMPTY_BYTE_ARRAY, MediaType.APPLICATION_JSON_TYPE).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Alibaba Cloud RAM request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Alibaba Cloud RAM endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return rpcFailure(response, operation, missing);
            }
            final JsonValue.ObjectValue value = object(response);
            final JsonValue error = value.values().get("Error");
            if (error != null && !(error instanceof JsonValue.NullValue)) {
                return rpcError(
                        requiredObject(error, "Alibaba Cloud RAM Error"),
                        response.code(),
                        response.headers(),
                        operation,
                        missing);
            }
            return Outcome.succeeded(Optional.of(value));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an invalid response");
        }
    }

    /**
     * Decodes and classifies one unsuccessful RAM HTTP response.
     *
     * @param response  owned unsuccessful response
     * @param operation safe operation label
     * @param missing   whether EntityNotExist represents normal absence
     * @param <T>       expected success type
     * @return classified failure, rejection, or explicit absence
     */
    private <T> Outcome<T> rpcFailure(final Response response, final String operation, final boolean missing) {
        try {
            return rpcError(object(response), response.code(), response.headers(), operation, missing);
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an invalid error envelope");
        }
    }

    /**
     * Classifies one validated RAM error envelope without exposing its message or request signature.
     *
     * @param error     decoded error object
     * @param status    HTTP status
     * @param headers   response headers
     * @param operation safe operation label
     * @param missing   whether EntityNotExist represents normal absence
     * @param <T>       expected success type
     * @return classified failure, rejection, or explicit absence
     */
    private <T> Outcome<T> rpcError(
            final JsonValue.ObjectValue error,
            final int status,
            final FabricX.Headers headers,
            final String operation,
            final boolean missing) {
        final String code = requiredString(error, "Code");
        requiredString(error, "Message");
        if (missing && code.contains("EntityNotExist")) {
            @SuppressWarnings("unchecked")
            final T absent = (T) Optional.<JsonValue.ObjectValue>empty();
            return Outcome.succeeded(absent);
        }
        final Map<String, JsonValue> values = details(operation, status, headers);
        values.put(Builder.ERROR_CODE_FIELD, new JsonValue.StringValue(code));
        if (status == Http.Status.UNAUTHORIZED || code.contains("InvalidAccessKey")) {
            return rejected(ErrorCode._401, "Alibaba Cloud RAM rejected the AccessKey", values);
        }
        if (status == Http.Status.FORBIDDEN || status == Http.Status.NOT_FOUND || code.contains("Forbidden")
                || code.contains("NoPermission")) {
            return rejected(ErrorCode._403, "Alibaba Cloud RAM visibility or permission is insufficient", values);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS || code.contains("Throttl")) {
            return failed(ErrorCode._429, "Alibaba Cloud RAM is rate limited", values);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, "Alibaba Cloud RAM returned an upstream error", values);
        }
        return rejected(ErrorCode._400, "Alibaba Cloud RAM rejected the request", values);
    }

    /**
     * Resolves one required management endpoint.
     *
     * @param name target key
     * @return resolved endpoint
     */
    private Endpoint target(final String name) {
        final Endpoint endpoint = targets.management().get(name);
        if (endpoint == null) {
            throw new ValidateException("Alibaba Cloud Enterprise manifest omits a required management target");
        }
        return endpoint;
    }

    /**
     * Emits collection results and either continues the marker or completes the phase.
     *
     * @param resources normalized resources
     * @param relations normalized relations
     * @param state     current cursor state
     * @param next      official continuation marker
     * @return successful continued or completed page
     */
    private Outcome<Realm.Page> continueOrComplete(
            final Map<Realm.Key, Realm.Resource> resources,
            final Map<Realm.RelationKey, Realm.Relation> relations,
            final CursorState state,
            final Optional<String> next) {
        return next.isPresent()
                ? output(List.copyOf(resources.values()), List.copyOf(relations.values()), state, Position.page(next))
                : complete(List.copyOf(resources.values()), List.copyOf(relations.values()), state);
    }

    /**
     * Emits a page with one canonical continuation cursor.
     *
     * @param resources resources
     * @param relations relations
     * @param state     current state
     * @param position  next position
     * @return successful page
     */
    private Outcome<Realm.Page> output(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state,
            final Position position) {
        return Outcome.succeeded(
                new Realm.Page(resources, relations,
                        Optional.of(encode(new CursorState(state.phase(), state.kinds(), position)))));
    }

    /**
     * Completes one phase and advances to the next requested phase.
     *
     * @param resources resources
     * @param relations relations
     * @param state     completed state
     * @return successful completed or continued page
     */
    private Outcome<Realm.Page> complete(
            final List<Realm.Resource> resources,
            final List<Realm.Relation> relations,
            final CursorState state) {
        Phase next = state.phase().next();
        while (next != null && !state.kinds().contains(next.kind)) {
            next = next.next();
        }
        return Outcome.succeeded(
                new Realm.Page(resources, relations, next == null ? Optional.empty()
                        : Optional.of(encode(new CursorState(next, state.kinds(), Position.initial())))));
    }

    /**
     * Encodes one canonical six-field cursor.
     *
     * @param state cursor state
     * @return opaque cursor
     */
    private Realm.Cursor encode(final CursorState state) {
        final Map<String, JsonValue> envelope = new LinkedHashMap<>();
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(AliyunManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(AliyunManifest.RAM.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code));
        final List<JsonValue> kinds = new ArrayList<>();
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        final Map<String, JsonValue> position = new LinkedHashMap<>();
        position.put(Builder.CURSOR_NEXT_FIELD, nullable(state.position().next()));
        if (state.phase() == Phase.GROUP_MEMBERS || state.phase() == Phase.ROLE_TRUST) {
            position.put(Builder.CURSOR_PARENT_ID_FIELD, nullable(state.position().parentId()));
            position.put(Builder.CURSOR_RELATION_OFFSET_FIELD, number(state.position().relationOffset()));
        }
        envelope.put(Builder.CURSOR_POSITION_FIELD, new JsonValue.ObjectValue(position));
        return new Realm.Cursor(
                Base64.encodeUrlSafe(services.jsonProvider().writeValue(new JsonValue.ObjectValue(envelope))));
    }

    /**
     * Decodes and canonicalizes one cursor.
     *
     * @param cursor supplied cursor
     * @param kinds  current requested kinds
     * @return validated state
     */
    private CursorState decode(final Realm.Cursor cursor, final Set<Realm.Kind> kinds) {
        try {
            final JsonValue.ObjectValue envelope = requiredObject(
                    services.jsonProvider()
                            .readValue(Base64.decode(cursor.value()), Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true),
                    "Alibaba Cloud cursor");
            exact(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD));
            if (!AliyunManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !AliyunManifest.RAM.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredInt(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("Alibaba Cloud cursor context is invalid");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("Alibaba Cloud cursor kinds do not match the request");
            }
            final JsonValue.ObjectValue raw = requiredObject(envelope, Builder.CURSOR_POSITION_FIELD);
            final Position position;
            if (phase == Phase.USERS || phase == Phase.GROUPS || phase == Phase.ROLES) {
                exact(raw, Set.of(Builder.CURSOR_NEXT_FIELD));
                position = Position.page(nullableString(raw, Builder.CURSOR_NEXT_FIELD));
            } else {
                exact(
                        raw,
                        Set.of(
                                Builder.CURSOR_NEXT_FIELD,
                                Builder.CURSOR_PARENT_ID_FIELD,
                                Builder.CURSOR_RELATION_OFFSET_FIELD));
                position = Position.dependent(
                        nullableString(raw, Builder.CURSOR_NEXT_FIELD),
                        nullableString(raw, Builder.CURSOR_PARENT_ID_FIELD),
                        nonNegativeInt(raw, Builder.CURSOR_RELATION_OFFSET_FIELD));
            }
            final CursorState state = new CursorState(phase, decodedKinds, position);
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("Alibaba Cloud cursor is not canonical");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("Alibaba Cloud cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded response object.
     *
     * @param response successful response
     * @return decoded object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(value(response), "Alibaba Cloud Enterprise response");
    }

    /**
     * Decodes one bounded Alibaba Cloud JSON response.
     *
     * @param response successful or error response
     * @return decoded JSON value
     */
    private JsonValue value(final Response response) {
        return services.jsonProvider()
                .readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH, true);
    }

    /**
     * Builds safe failure details.
     *
     * @param operation operation label
     * @param status    HTTP status
     * @param headers   response headers
     * @return mutable allow-listed details for optional error code insertion
     */
    private Map<String, JsonValue> details(final String operation, final int status, final FabricX.Headers headers) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(AliyunManifest.ID.value()));
        values.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(variant.variant().value()));
        values.put(Builder.OPERATION_FIELD, new JsonValue.StringValue(operation));
        values.put(Builder.HTTP_STATUS_FIELD, number(status));
        final String retry = headers.get(Http.Header.RETRY_AFTER);
        if (retry != null) {
            try {
                final long seconds = Long.parseLong(retry);
                if (seconds >= 0L) {
                    values.put(Builder.RETRY_AFTER_SECONDS_FIELD, number(seconds));
                }
            } catch (NumberFormatException ignored) {
                values.remove(Builder.RETRY_AFTER_SECONDS_FIELD);
            }
        }
        return values;
    }

    /**
     * Defines the finite Alibaba Cloud snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Reads visible RAM Users.
         */
        USERS(1, Realm.Kind.USER),

        /**
         * Reads visible RAM Groups.
         */
        GROUPS(2, Realm.Kind.GROUP),

        /**
         * Reads RAM User-to-Group memberships.
         */
        GROUP_MEMBERS(3, Realm.Kind.GROUP),

        /**
         * Reads visible RAM Roles.
         */
        ROLES(4, Realm.Kind.ROLE),

        /**
         * Reads resolvable RAM User principals from Role trust policies.
         */
        ROLE_TRUST(5, Realm.Kind.ROLE);

        /**
         * Stable phase code.
         */
        private final int code;

        /**
         * Resource kind enabling the phase.
         */
        private final Realm.Kind kind;

        /**
         * Creates one phase.
         *
         * @param code stable code
         * @param kind enabling kind
         */
        Phase(final int code, final Realm.Kind kind) {
            this.code = code;
            this.kind = kind;
        }

        /**
         * Resolves one stable phase code.
         *
         * @param code stable code
         * @return phase
         */
        private static Phase from(final int code) {
            for (Phase phase : values()) {
                if (phase.code == code) {
                    return phase;
                }
            }
            throw new ValidateException("Alibaba Cloud cursor phase is unknown");
        }

        /**
         * Returns the following frozen phase.
         *
         * @return following phase or {@code null}
         */
        private Phase next() {
            return switch (this) {
                case USERS -> GROUPS;
                case GROUPS -> GROUP_MEMBERS;
                case GROUP_MEMBERS -> ROLES;
                case ROLES -> ROLE_TRUST;
                case ROLE_TRUST -> null;
            };
        }
    }

    /**
     * Exposes the stable identifier shared by dependent Group and Role projections.
     *
     * @author Kimi Liu
     */
    private interface Parent {

        /**
         * Returns the stable RAM identifier used by replay cursors.
         *
         * @return stable RAM identifier
         */
        String id();
    }

    /**
     * Carries one canonical snapshot state.
     *
     * @param phase    phase
     * @param kinds    requested kinds
     * @param position recoverable position
     * @author Kimi Liu
     */
    private record CursorState(Phase phase, List<Realm.Kind> kinds, Position position) {

        /**
         * Validates one cursor state.
         */
        private CursorState {
            phase = Assert.notNull(phase, "Alibaba Cloud cursor phase must not be null");
            kinds = List.copyOf(Assert.notNull(kinds, "Alibaba Cloud cursor kinds must not be null"));
            position = Assert.notNull(position, "Alibaba Cloud cursor position must not be null");
            if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds) || !kinds.contains(phase.kind)) {
                throw new ValidateException("Alibaba Cloud cursor kinds or phase are invalid");
            }
            position.validate(phase);
        }

        /**
         * Creates the first enabled phase.
         *
         * @param kinds requested kinds
         * @return initial state
         */
        private static CursorState initial(final Set<Realm.Kind> kinds) {
            final List<Realm.Kind> ordered = List.copyOf(kinds);
            final Phase initial = ordered.contains(Realm.Kind.USER) ? Phase.USERS
                    : ordered.contains(Realm.Kind.GROUP) ? Phase.GROUPS : Phase.ROLES;
            return new CursorState(initial, ordered, Position.initial());
        }
    }

    /**
     * Carries one RAM marker and dependent relation position.
     *
     * @param next           optional official page marker
     * @param parentId       current replay group identifier
     * @param relationOffset emitted relation offset for one Role
     * @author Kimi Liu
     */
    private record Position(Optional<String> next, Optional<String> parentId, int relationOffset) {

        /**
         * Validates one position.
         */
        private Position {
            next = Assert.notNull(next, "Alibaba Cloud cursor marker container must not be null");
            next = next.isPresent() ? Optional.of(requireText(next.getOrNull(), "Alibaba Cloud cursor marker"))
                    : Optional.empty();
            if (relationOffset < 0) {
                throw new ValidateException("Alibaba Cloud cursor position is out of range");
            }
            parentId = Assert.notNull(parentId, "Alibaba Cloud cursor parent container must not be null");
            parentId = parentId.isPresent()
                    ? Optional.of(requireText(parentId.getOrNull(), "Alibaba Cloud cursor parent identifier"))
                    : Optional.empty();
        }

        /**
         * Creates the initial position.
         *
         * @return initial position
         */
        private static Position initial() {
            return page(Optional.empty());
        }

        /**
         * Creates a top-level page position.
         *
         * @param next optional official marker
         * @return page position
         */
        private static Position page(final Optional<String> next) {
            return new Position(next, Optional.empty(), 0);
        }

        /**
         * Creates a dependent relation position for a known stable parent.
         *
         * @param next           optional official child marker
         * @param parentId       replay group identifier
         * @param relationOffset emitted relation offset
         * @return dependent position
         */
        private static Position dependent(
                final Optional<String> next,
                final String parentId,
                final int relationOffset) {
            return new Position(next, Optional.of(requireText(parentId, "Alibaba Cloud cursor parent identifier")),
                    relationOffset);
        }

        /**
         * Creates a decoded dependent relation position.
         *
         * @param next           optional official child marker
         * @param parentId       optional replay parent identifier
         * @param relationOffset emitted relation offset
         * @return dependent position
         */
        private static Position dependent(
                final Optional<String> next,
                final Optional<String> parentId,
                final int relationOffset) {
            return new Position(next, parentId, relationOffset);
        }

        /**
         * Validates phase-specific fields.
         *
         * @param phase owning phase
         */
        private void validate(final Phase phase) {
            final boolean topLevel = phase == Phase.USERS || phase == Phase.GROUPS || phase == Phase.ROLES;
            final boolean groupMember = phase == Phase.GROUP_MEMBERS;
            final boolean roleTrust = phase == Phase.ROLE_TRUST;
            if (topLevel && (parentId.isPresent() || relationOffset != 0)
                    || groupMember && (parentId.isEmpty() && (next.isPresent() || relationOffset != 0)
                            || parentId.isPresent() && relationOffset != 0)
                    || roleTrust && (next.isPresent() || parentId.isEmpty() && relationOffset != 0)) {
                throw new ValidateException("Alibaba Cloud cursor position does not belong to its phase");
            }
        }
    }

    /**
     * Carries one validated Alibaba Cloud marker-paged collection page.
     *
     * @param items minimal items
     * @param next  optional official continuation marker
     * @param <T>   item type
     * @author Kimi Liu
     */
    private record WirePage<T>(List<T> items, Optional<String> next) {

        /**
         * Freezes one page.
         */
        private WirePage {
            items = List.copyOf(Assert.notNull(items, "Alibaba Cloud collection items must not be null"));
            next = Assert.notNull(next, "Alibaba Cloud collection marker container must not be null");
            next = next.isPresent() ? Optional.of(requireText(next.getOrNull(), "Alibaba Cloud collection marker"))
                    : Optional.empty();
        }
    }

    /**
     * Carries one replayed parent collection and selected dependent parent.
     *
     * @param items  replayed parent projections
     * @param index  selected index or negative at natural exhaustion
     * @param parent selected projection or empty at natural exhaustion
     * @param <T>    parent projection type
     * @author Kimi Liu
     */
    private record ParentPage<T extends Parent>(List<T> items, int index, Optional<T> parent) {

        /**
         * Freezes and validates one replay result.
         */
        private ParentPage {
            items = List.copyOf(Assert.notNull(items, "Alibaba Cloud replay parents must not be null"));
            parent = Assert.notNull(parent, "Alibaba Cloud replay parent container must not be null");
            if (index < -1 || index >= items.size() || parent.isPresent() != (index >= 0)
                    || parent.isPresent() && !parent.getOrNull().equals(items.get(index))) {
                throw new ValidateException("Alibaba Cloud replay parent state is inconsistent");
            }
        }
    }

    /**
     * Minimal Alibaba Cloud RAM User projection.
     *
     * @param id          stable UserId
     * @param name        exact UserName
     * @param displayName DisplayName with UserName fallback
     * @author Kimi Liu
     */
    private record User(String id, String name, String displayName) {

        /**
         * Validates one RAM User projection.
         */
        private User {
            id = requireText(id, "Alibaba Cloud RAM UserId");
            name = requireText(name, "Alibaba Cloud RAM UserName");
            displayName = requireText(displayName, "Alibaba Cloud RAM User display name");
        }
    }

    /**
     * Minimal Alibaba Cloud RAM Group projection.
     *
     * @param id   stable GroupId
     * @param name exact GroupName
     * @author Kimi Liu
     */
    private record Group(String id, String name) implements Parent {

        /**
         * Validates one RAM Group projection.
         */
        private Group {
            id = requireText(id, "Alibaba Cloud RAM GroupId");
            name = requireText(name, "Alibaba Cloud RAM GroupName");
        }
    }

    /**
     * Minimal Alibaba Cloud RAM Role projection.
     *
     * @param id   stable RoleId
     * @param name exact RoleName
     * @author Kimi Liu
     */
    private record Role(String id, String name) implements Parent {

        /**
         * Validates one RAM Role projection.
         */
        private Role {
            id = requireText(id, "Alibaba Cloud RAM RoleId");
            name = requireText(name, "Alibaba Cloud RAM RoleName");
        }
    }

    /**
     * Carries one RAM Role and the trust-policy Principal closure resolved from its official response.
     *
     * @param role       Role projection or empty when retrieval reports EntityNotExist
     * @param principals ordered unique Principal values
     * @author Kimi Liu
     */
    private record RoleTrust(Optional<Role> role, List<String> principals) {

        /**
         * Freezes and validates one Role trust projection.
         */
        private RoleTrust {
            role = Assert.notNull(role, "Alibaba Cloud RAM Role container must not be null");
            principals = List
                    .copyOf(Assert.notNull(principals, "Alibaba Cloud RAM Role Principal collection must not be null"));
            if (role.isEmpty() && !principals.isEmpty()) {
                throw new ValidateException("Alibaba Cloud RAM absent Role must not expose principals");
            }
        }
    }

}
