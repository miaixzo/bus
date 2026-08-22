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
package org.miaixz.bus.auth.vendor.figma;

import java.math.BigDecimal;
import java.time.Instant;
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
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the provider-neutral Figma administrator SCIM realm surface.
 * <p>
 * Snapshot pagination advances through users and groups. Group pages emit group resources and their embedded member
 * relations; the cursor replays only the current official page when a member list exceeds the outward relation limit.
 * Every network invocation uses one freshly loaded SCIM administrator-token lease that is closed at the single terminal
 * stage boundary and is never cached.
 * </p>
 *
 * @author Kimi Liu
 */
public final class FigmaRealmAdapter implements VendorAdapter {

    /**
     * Official SCIM error schema identifier.
     */
    private static final String SCIM_ERROR_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";

    /**
     * Empty immutable JSON attributes used by every Figma projection.
     */
    private static final JsonValue.ObjectValue EMPTY_ATTRIBUTES = new JsonValue.ObjectValue(Map.of());

    /**
     * Exact resource categories exposed by Figma SCIM.
     */
    private static final Set<Realm.Kind> SUPPORTED_KINDS = Set.of(Realm.Kind.USER, Realm.Kind.GROUP);

    /**
     * Ordered management-target closure required from the Figma SCIM manifest.
     */
    private static final List<String> MANAGEMENT_TARGETS = List
            .of(Builder.ENTERPRISE_USERS, Builder.ENTERPRISE_USER, Builder.ENTERPRISE_GROUPS, Builder.ENTERPRISE_GROUP);

    /**
     * Selected immutable Figma SCIM Variant.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated Figma SCIM deployment options.
     */
    private final FigmaOptions options;

    /**
     * Caller-owned execution services.
     */
    private final DriverServices services;

    /**
     * Resolved official SCIM resource targets.
     */
    private final VendorTargets.Resolved targets;

    /**
     * Creates one Source-isolated Figma administrator SCIM realm adapter.
     *
     * @param spaceId  registration space used for isolation
     * @param sourceId registered Source identifier
     * @param manifest exact Figma manifest
     * @param variant  selected SCIM Variant
     * @param options  validated SCIM options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or collaborator is {@code null}
     * @throws ValidateException        if the manifest, Variant, options, protocol, or targets are inconsistent
     */
    public FigmaRealmAdapter(final String spaceId, final String sourceId, final FigmaManifest manifest,
            final VariantManifest.Variant variant, final FigmaOptions options, final DriverServices services) {
        Assert.notBlank(spaceId, "Figma SCIM space id must not be blank");
        Assert.notBlank(sourceId, "Figma SCIM Source id must not be blank");
        final FigmaManifest selectedManifest = Assert.notNull(manifest, "Figma manifest must not be null");
        this.variant = Assert.notNull(variant, "Figma SCIM Variant must not be null");
        this.options = Assert.notNull(options, "Figma SCIM options must not be null");
        this.services = Assert.notNull(services, "Figma SCIM services must not be null");
        if (!FigmaManifest.ID.equals(selectedManifest.vendor()) || !FigmaManifest.SCIM.equals(this.variant.variant())
                || !selectedManifest.variant(this.variant.variant()).equals(this.variant)
                || this.variant.protocol() != Protocol.SCIM || !FigmaManifest.ID.equals(this.options.vendor())
                || !FigmaManifest.SCIM.equals(this.options.variant()) || this.options.redirectUri().isPresent()
                || !this.options.scopes().isEmpty()
                || this.options.credential().type() != org.miaixz.bus.auth.Credential.Type.SHARED_SECRET) {
            throw new ValidateException("Figma realm adapter requires the frozen administrator SCIM Variant");
        }
        this.services.securityBaseline().require(this.variant.protocol());
        this.targets = this.variant.targets().resolve(this.options);
        if (!List.copyOf(this.targets.management().keySet()).equals(MANAGEMENT_TARGETS)) {
            throw new ValidateException("Figma SCIM manifest has an invalid management target set");
        }
    }

    /**
     * Parses one minimal SCIM user.
     *
     * @param value decoded object
     * @return validated user projection
     */
    private static User user(final JsonValue.ObjectValue value) {
        return new User(requiredString(value, "id"), requiredString(value, "userName"),
                requiredString(value, "displayName"), requiredBoolean(value, "active"), optionalText(value, "title"));
    }

    /**
     * Parses one minimal SCIM group and embedded member identifiers.
     *
     * @param value decoded object
     * @return validated group projection
     */
    private static Group group(final JsonValue.ObjectValue value) {
        final List<String> members = new ArrayList<>();
        final JsonValue raw = value.values().get("members");
        if (raw != null && !(raw instanceof JsonValue.NullValue)) {
            if (!(raw instanceof JsonValue.ArrayValue array)) {
                throw new ValidateException("members must be a JSON array");
            }
            final LinkedHashSet<String> unique = new LinkedHashSet<>();
            for (JsonValue member : array.values()) {
                unique.add(requiredString(requiredObject(member, "SCIM group member"), "value"));
            }
            members.addAll(unique);
        }
        return new Group(requiredString(value, "id"), requiredString(value, "displayName"), members);
    }

    /**
     * Converts one user projection to a normalized resource.
     *
     * @param user       user projection
     * @param observedAt observation instant
     * @return immutable user resource
     */
    private static Realm.Resource userResource(final User user, final Instant observedAt) {
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        if (user.title().isPresent()) {
            attributes.put("title", new JsonValue.StringValue(user.title().getOrNull()));
        }
        return new Realm.Resource(new Realm.Key(Realm.Kind.USER, user.id()),
                orderedIdentifier("userName", user.userName()), user.displayName(),
                user.active() ? Realm.State.ACTIVE : Realm.State.INACTIVE, new JsonValue.ObjectValue(attributes),
                observedAt);
    }

    /**
     * Converts one group projection to a normalized resource.
     *
     * @param group      group projection
     * @param observedAt observation instant
     * @return immutable group resource
     */
    private static Realm.Resource groupResource(final Group group, final Instant observedAt) {
        return new Realm.Resource(new Realm.Key(Realm.Kind.GROUP, group.id()), Map.of(), group.displayName(),
                Realm.State.UNKNOWN, EMPTY_ATTRIBUTES, observedAt);
    }

    /**
     * Creates one normalized user-to-group membership relation.
     *
     * @param userId     stable user identifier
     * @param groupId    stable group identifier
     * @param observedAt observation instant
     * @return immutable membership relation
     */
    private static Realm.Relation memberRelation(final String userId, final String groupId, final Instant observedAt) {
        return new Realm.Relation(new Realm.RelationKey(Realm.RelationKind.MEMBER,
                new Realm.Key(Realm.Kind.USER, userId), new Realm.Key(Realm.Kind.GROUP, groupId)), EMPTY_ATTRIBUTES,
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
     * Reads a required boolean member.
     *
     * @param object parent object
     * @param name   member name
     * @return boolean value
     */
    private static boolean requiredBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.BooleanValue result)) {
            throw new ValidateException(name + " must be a JSON boolean");
        }
        return result.value();
    }

    /**
     * Reads an optional integer member without inventing a missing SCIM field.
     *
     * @param object parent object
     * @param name   member name
     * @return exact integer when present
     */
    private static Optional<Integer> optionalInt(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        return Optional.of(requiredInt(object, name));
    }

    /**
     * Reads an optional nonempty string while preserving its exact lexical value.
     *
     * @param object parent object
     * @param name   member name
     * @return exact string when present and nonempty
     */
    private static Optional<String> optionalText(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException(name + " must be a string or null");
        }
        return text.value().isEmpty() ? Optional.empty() : Optional.of(requireText(text.value(), name));
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
     * Parses one canonical positive decimal cursor value.
     *
     * @param value lexical decimal
     * @return positive integer
     */
    private static int positiveDecimal(final String value) {
        try {
            final int result = Integer.parseInt(value);
            if (result <= 0 || !Integer.toString(result).equals(value)) {
                throw new ValidateException("Figma cursor index must be canonical and positive");
            }
            return result;
        } catch (NumberFormatException cause) {
            throw new ValidateException("Figma cursor index is invalid", cause);
        }
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
     * Reads the SCIM error status member.
     *
     * @param object error object
     * @return exact HTTP status
     */
    private static int status(final JsonValue.ObjectValue object) {
        return requiredInt(object, "status");
    }

    /**
     * Validates an exact object member closure.
     *
     * @param object   decoded object
     * @param expected expected names
     */
    private static void exact(final JsonValue.ObjectValue object, final Set<String> expected) {
        if (!object.values().keySet().equals(expected)) {
            throw new ValidateException("Figma cursor contains an invalid member set");
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
                throw new ValidateException("Figma cursor kind must be numeric");
            }
            final int code;
            try {
                code = number.value().intValueExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Figma cursor kind is invalid", cause);
            }
            final Realm.Kind kind = kind(code);
            if (code <= previous || !result.add(kind)) {
                throw new ValidateException("Figma cursor kinds are not canonical");
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
        throw new ValidateException("Figma cursor contains an unsupported kind");
    }

    /**
     * Validates the requested kind closure.
     *
     * @param kinds requested kinds
     */
    private static void requireKinds(final Set<Realm.Kind> kinds) {
        if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds)) {
            throw new ValidateException("Figma snapshot contains an unsupported kind");
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
            default -> failed(ErrorCode._500, "Figma delegated outcome is unsupported");
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
            default -> failed(ErrorCode._500, "Figma internal outcome cannot be propagated");
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
     * Routes one exact Figma SCIM enterprise capability.
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
        Assert.notNull(capability, "Figma enterprise capability must not be null");
        Assert.notNull(context, "Figma enterprise context must not be null");
        Assert.notNull(timeout, "Figma enterprise timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected(ErrorCode._400, "Figma enterprise capability is not declared"));
        }
        if (capability.equals(Realm.describe(FigmaManifest.ID)) && request instanceof Realm.Describe) {
            return completed(Outcome.succeeded(capability.responseType().cast(FigmaManifest.enterpriseDescription())));
        }
        if (capability.equals(Realm.snapshot(FigmaManifest.ID)) && request instanceof Realm.Snapshot value) {
            return narrow(snapshot(value, context, timeout), capability.responseType());
        }
        if (capability.equals(Realm.retrieve(FigmaManifest.ID)) && request instanceof Realm.Retrieve value) {
            return narrow(retrieve(value, context, timeout), capability.responseType());
        }
        return completed(rejected(ErrorCode._400, "Figma enterprise request does not match the capability contract"));
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
            return completed(rejected(ErrorCode._400, "Figma SCIM snapshot request is invalid"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Figma SCIM snapshot has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return withSecret(
                context,
                timeout,
                secret -> state.phase() == Phase.USERS ? users(secret, request, state, observedAt, timeout)
                        : groups(secret, request, state, observedAt, timeout));
    }

    /**
     * Reads one bounded Figma SCIM user page.
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
        final int count = Math.min(request.limit(), Normal._3000);
        final int start = state.position().start();
        final Outcome<ScimPage<User>> fetched = page(
                target(Builder.ENTERPRISE_USERS).url(),
                secret,
                count,
                start,
                timeout,
                "snapshot",
                FigmaRealmAdapter::user);
        if (!(fetched instanceof Outcome.Succeeded<ScimPage<User>> success)) {
            return propagate(fetched);
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        for (User user : success.value().items()) {
            putResource(resources, userResource(user, observedAt), "Figma user page");
        }
        final int next = success.value().next();
        if (next > 0) {
            return output(List.copyOf(resources.values()), List.of(), state, Position.page(next));
        }
        return complete(List.copyOf(resources.values()), List.of(), state);
    }

    /**
     * Reads one Figma SCIM group page and resumes embedded members without duplication.
     *
     * @param secret     open administrator-token lease
     * @param request    snapshot request
     * @param state      group cursor state
     * @param observedAt shared observation instant
     * @param timeout    shared timeout
     * @return normalized group and member page
     */
    private Outcome<Realm.Page> groups(
            final SecretLease secret,
            final Realm.Snapshot request,
            final CursorState state,
            final Instant observedAt,
            final Timeout timeout) {
        final int count = Math.min(request.limit(), Normal._3000);
        final int start = state.position().start();
        final Outcome<ScimPage<Group>> fetched = page(
                target(Builder.ENTERPRISE_GROUPS).url(),
                secret,
                count,
                start,
                timeout,
                "snapshot",
                FigmaRealmAdapter::group);
        if (!(fetched instanceof Outcome.Succeeded<ScimPage<Group>> success)) {
            return propagate(fetched);
        }
        final List<Group> groups = success.value().items();
        int groupIndex = 0;
        int memberOffset = 0;
        if (state.position().parentId().isPresent()) {
            while (groupIndex < groups.size()
                    && !state.position().parentId().getOrNull().equals(groups.get(groupIndex).id())) {
                groupIndex++;
            }
            if (groupIndex == groups.size()) {
                return failed(ErrorCode._502, "Figma SCIM replay group no longer exists on its official page");
            }
            memberOffset = state.position().relationOffset();
        }
        final Map<Realm.Key, Realm.Resource> resources = new LinkedHashMap<>();
        final Map<Realm.RelationKey, Realm.Relation> relations = new LinkedHashMap<>();
        while (groupIndex < groups.size()) {
            final Group group = groups.get(groupIndex);
            if (memberOffset == 0) {
                putResource(resources, groupResource(group, observedAt), "Figma group page");
            }
            if (memberOffset > group.members().size()) {
                return failed(ErrorCode._502, "Figma SCIM group-member replay offset exceeds the current projection");
            }
            while (memberOffset < group.members().size() && relations.size() < request.limit()) {
                putRelation(
                        relations,
                        memberRelation(group.members().get(memberOffset++), group.id(), observedAt),
                        "Figma group page");
            }
            if (memberOffset < group.members().size()) {
                return output(
                        List.copyOf(resources.values()),
                        List.copyOf(relations.values()),
                        state,
                        Position.group(start, group.id(), memberOffset));
            }
            groupIndex++;
            memberOffset = 0;
            if (groupIndex < groups.size()
                    && (resources.size() >= request.limit() || relations.size() >= request.limit())) {
                return output(
                        List.copyOf(resources.values()),
                        List.copyOf(relations.values()),
                        state,
                        Position.group(start, groups.get(groupIndex).id(), 0));
            }
        }
        if (success.value().next() > 0) {
            return output(
                    List.copyOf(resources.values()),
                    List.copyOf(relations.values()),
                    state,
                    Position.page(success.value().next()));
        }
        return complete(List.copyOf(resources.values()), List.copyOf(relations.values()), state);
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
            return completed(rejected(ErrorCode._400, "Figma SCIM retrieve kind is unsupported"));
        }
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Figma SCIM retrieve has no remaining timeout"));
        }
        final Instant observedAt = FabricX.clock(services.fabric()).now();
        return withSecret(context, timeout, secret -> retrieve(secret, request.key(), observedAt, timeout));
    }

    /**
     * Scans the official SCIM collection for one stable User or Group identifier.
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
        final int count = Math.min(Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE, Normal._3000);
        int start = Normal._1;
        while (true) {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Figma SCIM retrieve has no remaining timeout");
            }
            if (key.kind() == Realm.Kind.USER) {
                final Outcome<ScimPage<User>> fetched = page(
                        target(Builder.ENTERPRISE_USERS).url(),
                        secret,
                        count,
                        start,
                        timeout,
                        "retrieve",
                        FigmaRealmAdapter::user);
                if (!(fetched instanceof Outcome.Succeeded<ScimPage<User>> success)) {
                    return propagate(fetched);
                }
                for (User user : success.value().items()) {
                    final Realm.Resource resource = userResource(user, observedAt);
                    if (key.equals(resource.key())) {
                        return Outcome.succeeded(new Realm.Retrieved(Optional.of(resource)));
                    }
                }
                if (success.value().next() == 0) {
                    return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
                }
                start = success.value().next();
            } else {
                final Outcome<ScimPage<Group>> fetched = page(
                        target(Builder.ENTERPRISE_GROUPS).url(),
                        secret,
                        count,
                        start,
                        timeout,
                        "retrieve",
                        FigmaRealmAdapter::group);
                if (!(fetched instanceof Outcome.Succeeded<ScimPage<Group>> success)) {
                    return propagate(fetched);
                }
                for (Group group : success.value().items()) {
                    final Realm.Resource resource = groupResource(group, observedAt);
                    if (key.equals(resource.key())) {
                        return Outcome.succeeded(new Realm.Retrieved(Optional.of(resource)));
                    }
                }
                if (success.value().next() == 0) {
                    return Outcome.succeeded(new Realm.Retrieved(Optional.empty()));
                }
                start = success.value().next();
            }
        }
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
            return completed(failed(ErrorCode._500, "Figma SCIM Secret Loader failed before returning a stage"));
        }
        if (loaded == null) {
            return completed(failed(ErrorCode._500, "Figma SCIM Secret Loader returned no stage"));
        }
        return loaded.<Outcome<SecretLoader.Record>>handle(
                (outcome, cause) -> cause == null && outcome != null ? outcome
                        : failed(ErrorCode._500, "Figma SCIM Secret Loader stage failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLoader.Record> success -> execute(success.value(), timeout, operation);
                    case Outcome.Rejected<SecretLoader.Record> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLoader.Record> failed -> completed(Outcome.failed(failed.failure()));
                    default -> completed(
                            failed(ErrorCode._500, "Figma SCIM Secret Loader returned an unsupported outcome"));
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
            return completed(failed(ErrorCode._500, "Figma SCIM loaded Secret could not be validated"));
        }
        try {
            return CompletableFuture.<Outcome<T>>supplyAsync(() -> {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Figma SCIM operation has no remaining timeout");
                }
                try {
                    return Assert.notNull(operation.apply(secret), "Figma SCIM operation returned no outcome");
                } catch (TimeoutException ignored) {
                    return failed(ErrorCode._408, "Figma SCIM operation timed out");
                } catch (RuntimeException ignored) {
                    return failed(ErrorCode._503, "Figma SCIM transport is unavailable");
                }
            }, services.executor()).whenComplete((ignored, cause) -> secret.close());
        } catch (RuntimeException ignored) {
            secret.close();
            return completed(failed(ErrorCode._500, "Figma SCIM operation could not be scheduled"));
        }
    }

    /**
     * Reads and validates one official SCIM collection envelope.
     *
     * @param base      fixed collection target
     * @param secret    open token lease
     * @param count     requested bounded count
     * @param start     requested one-based start index
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @param parser    minimal resource parser
     * @param <T>       minimal item type
     * @return validated SCIM page
     */
    private <T> Outcome<ScimPage<T>> page(
            final Url base,
            final SecretLease secret,
            final int count,
            final int start,
            final Timeout timeout,
            final String operation,
            final Function<JsonValue.ObjectValue, T> parser) {
        final Url request = base.newBuilder().query("count", Integer.toString(count))
                .query("startIndex", Integer.toString(start)).build();
        final Outcome<JsonValue.ObjectValue> fetched = get(request, secret, timeout, operation);
        if (!(fetched instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(fetched);
        }
        try {
            final JsonValue.ObjectValue envelope = success.value();
            final int total = nonNegativeInt(envelope, "totalResults");
            final List<JsonValue> values = requiredArray(envelope, "Resources");
            final Optional<Integer> returnedStart = optionalInt(envelope, "startIndex");
            final Optional<Integer> itemsPerPage = optionalInt(envelope, "itemsPerPage");
            final int consumed = Math.max(0, start - 1) + values.size();
            if (values.size() > count || total < consumed
                    || returnedStart.isPresent() && returnedStart.getOrNull() != start
                    || itemsPerPage.isPresent() && itemsPerPage.getOrNull() != values.size()
                    || values.isEmpty() && consumed < total) {
                return failed(ErrorCode._502, "Figma SCIM pagination envelope is inconsistent");
            }
            final List<T> items = new ArrayList<>(values.size());
            for (JsonValue value : values) {
                items.add(parser.apply(requiredObject(value, "Figma SCIM resource")));
            }
            final int next = consumed < total ? start + values.size() : 0;
            return Outcome.succeeded(new ScimPage<>(items, next));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Figma SCIM returned an invalid collection envelope");
        }
    }

    /**
     * Executes one bounded SCIM GET and decodes a successful object.
     *
     * @param url       fixed or manifest-derived URL
     * @param secret    open token lease
     * @param timeout   shared timeout
     * @param operation safe operation label
     * @return decoded object or classified failure
     */
    private Outcome<JsonValue.ObjectValue> get(
            final Url url,
            final SecretLease secret,
            final Timeout timeout,
            final String operation) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Figma SCIM request has no remaining timeout");
        }
        final Response response;
        try {
            response = FabricX.http(services.fabric(), Protocol.SCIM, timeout).url(url.toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_SCIM_JSON)
                    .header(Http.Header.AUTHORIZATION, "Bearer " + new String(secret.material())).execute();
        } catch (TimeoutException ignored) {
            return failed(ErrorCode._408, "Figma SCIM request timed out");
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._503, "Figma SCIM endpoint is unavailable");
        }
        try (response) {
            if (!response.successful()) {
                return httpFailure(response, operation);
            }
            return Outcome.succeeded(object(response));
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Figma SCIM returned an invalid response");
        }
    }

    /**
     * Validates and classifies one official SCIM error response.
     *
     * @param response  owned unsuccessful response
     * @param operation safe operation label
     * @return classified failure or rejection
     */
    private Outcome<JsonValue.ObjectValue> httpFailure(final Response response, final String operation) {
        final int status = response.code();
        final JsonValue.ObjectValue error;
        try {
            error = object(response);
            final List<JsonValue> schemas = requiredArray(error, "schemas");
            if (schemas.size() != 1 || !(schemas.getFirst() instanceof JsonValue.StringValue schema)
                    || !SCIM_ERROR_SCHEMA.equals(schema.value()) || status(error) != status) {
                return failed(ErrorCode._502, "Figma SCIM returned an invalid error envelope");
            }
        } catch (RuntimeException ignored) {
            return failed(ErrorCode._502, "Figma SCIM returned an invalid error envelope");
        }
        final Map<String, JsonValue> details = details(operation, status, response.headers());
        details.put(Builder.ERROR_CODE_FIELD, number(status));
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.CONFLICT
                || status == Http.Status.UNPROCESSABLE_CONTENT) {
            return rejected(ErrorCode._400, "Figma SCIM rejected the request", details);
        }
        if (status == Http.Status.UNAUTHORIZED) {
            return rejected(ErrorCode._401, "Figma SCIM rejected the administrator token", details);
        }
        if (status == Http.Status.FORBIDDEN) {
            return rejected(ErrorCode._403, "Figma SCIM plan, installation, or permission is insufficient", details);
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Figma SCIM is rate limited", details);
        }
        return failed(ErrorCode._502, "Figma SCIM returned an upstream error", details);
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
            throw new ValidateException("Figma SCIM manifest omits a required management target");
        }
        return endpoint;
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
        final Phase next = state.phase() == Phase.USERS && state.kinds().contains(Realm.Kind.GROUP) ? Phase.GROUPS
                : null;
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
        envelope.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(FigmaManifest.ID.value()));
        envelope.put(Builder.VARIANT_FIELD, new JsonValue.StringValue(FigmaManifest.SCIM.value()));
        envelope.put(Builder.OPERATION_FIELD, number(Realm.Operation.SNAPSHOT.code()));
        envelope.put(Builder.CURSOR_PHASE_FIELD, number(state.phase().code));
        final List<JsonValue> kinds = new ArrayList<>();
        for (Realm.Kind kind : state.kinds()) {
            kinds.add(number(kind.code()));
        }
        envelope.put(Builder.CURSOR_KIND_FIELD, new JsonValue.ArrayValue(kinds));
        final Map<String, JsonValue> position = new LinkedHashMap<>();
        position.put(Builder.CURSOR_NEXT_FIELD, new JsonValue.StringValue(Integer.toString(state.position().start())));
        if (state.phase() == Phase.GROUPS) {
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
                    "Figma cursor");
            exact(
                    envelope,
                    Set.of(
                            Builder.VENDOR_FIELD,
                            Builder.VARIANT_FIELD,
                            Builder.OPERATION_FIELD,
                            Builder.CURSOR_PHASE_FIELD,
                            Builder.CURSOR_KIND_FIELD,
                            Builder.CURSOR_POSITION_FIELD));
            if (!FigmaManifest.ID.value().equals(requiredString(envelope, Builder.VENDOR_FIELD))
                    || !FigmaManifest.SCIM.value().equals(requiredString(envelope, Builder.VARIANT_FIELD))
                    || requiredInt(envelope, Builder.OPERATION_FIELD) != Realm.Operation.SNAPSHOT.code()) {
                throw new ValidateException("Figma cursor context is invalid");
            }
            final Phase phase = Phase.from(requiredInt(envelope, Builder.CURSOR_PHASE_FIELD));
            final List<Realm.Kind> decodedKinds = kinds(requiredArray(envelope, Builder.CURSOR_KIND_FIELD));
            if (!decodedKinds.equals(List.copyOf(kinds))) {
                throw new ValidateException("Figma cursor kinds do not match the request");
            }
            final JsonValue.ObjectValue raw = requiredObject(envelope, Builder.CURSOR_POSITION_FIELD);
            final Position position;
            if (phase == Phase.USERS) {
                exact(raw, Set.of(Builder.CURSOR_NEXT_FIELD));
                position = Position.page(positiveDecimal(requiredString(raw, Builder.CURSOR_NEXT_FIELD)));
            } else {
                exact(
                        raw,
                        Set.of(
                                Builder.CURSOR_NEXT_FIELD,
                                Builder.CURSOR_PARENT_ID_FIELD,
                                Builder.CURSOR_RELATION_OFFSET_FIELD));
                position = Position.group(
                        positiveDecimal(requiredString(raw, Builder.CURSOR_NEXT_FIELD)),
                        nullableString(raw, Builder.CURSOR_PARENT_ID_FIELD).getOrNull(),
                        nonNegativeInt(raw, Builder.CURSOR_RELATION_OFFSET_FIELD));
            }
            final CursorState state = new CursorState(phase, decodedKinds, position);
            if (!encode(state).value().equals(cursor.value())) {
                throw new ValidateException("Figma cursor is not canonical");
            }
            return state;
        } catch (ValidateException cause) {
            throw cause;
        } catch (RuntimeException cause) {
            throw new ValidateException("Figma cursor is invalid", cause);
        }
    }

    /**
     * Decodes one bounded response object.
     *
     * @param response successful response
     * @return decoded object
     */
    private JsonValue.ObjectValue object(final Response response) {
        return requiredObject(
                services.jsonProvider().readValue(
                        response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES),
                        Builder.MAXIMUM_ENTERPRISE_JSON_DEPTH,
                        true),
                "Figma SCIM response");
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
        values.put(Builder.VENDOR_FIELD, new JsonValue.StringValue(FigmaManifest.ID.value()));
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
     * Defines the finite Figma snapshot phase order.
     *
     * @author Kimi Liu
     */
    private enum Phase {

        /**
         * Reads SCIM users.
         */
        USERS(1, Realm.Kind.USER),

        /**
         * Reads SCIM groups and members.
         */
        GROUPS(2, Realm.Kind.GROUP);

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
            throw new ValidateException("Figma cursor phase is unknown");
        }
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
            phase = Assert.notNull(phase, "Figma cursor phase must not be null");
            kinds = List.copyOf(Assert.notNull(kinds, "Figma cursor kinds must not be null"));
            position = Assert.notNull(position, "Figma cursor position must not be null");
            if (kinds.isEmpty() || !SUPPORTED_KINDS.containsAll(kinds) || !kinds.contains(phase.kind)) {
                throw new ValidateException("Figma cursor kinds or phase are invalid");
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
            return new CursorState(ordered.contains(Realm.Kind.USER) ? Phase.USERS : Phase.GROUPS, ordered,
                    Position.initial());
        }
    }

    /**
     * Carries one SCIM page and embedded-member replay position.
     *
     * @param start          one-based official start index
     * @param parentId       current replay group identifier
     * @param relationOffset next embedded member index
     * @author Kimi Liu
     */
    private record Position(int start, Optional<String> parentId, int relationOffset) {

        /**
         * Validates one position.
         */
        private Position {
            if (start <= 0 || relationOffset < 0) {
                throw new ValidateException("Figma cursor position is out of range");
            }
            parentId = Assert.notNull(parentId, "Figma cursor parent container must not be null");
            parentId = parentId.isPresent()
                    ? Optional.of(requireText(parentId.getOrNull(), "Figma cursor parent identifier"))
                    : Optional.empty();
        }

        /**
         * Creates the initial position.
         *
         * @return initial position
         */
        private static Position initial() {
            return page(Normal._1);
        }

        /**
         * Creates a top-level page position.
         *
         * @param start official start index
         * @return page position
         */
        private static Position page(final int start) {
            return new Position(start, Optional.empty(), 0);
        }

        /**
         * Creates a group-member replay position.
         *
         * @param start          official page start
         * @param parentId       replay group identifier
         * @param relationOffset next member index
         * @return group position
         */
        private static Position group(final int start, final String parentId, final int relationOffset) {
            return new Position(start, Optional.ofNullable(parentId), relationOffset);
        }

        /**
         * Validates phase-specific fields.
         *
         * @param phase owning phase
         */
        private void validate(final Phase phase) {
            if (phase == Phase.USERS && (parentId.isPresent() || relationOffset != 0)
                    || phase == Phase.GROUPS && relationOffset > 0 && parentId.isEmpty()) {
                throw new ValidateException("Figma cursor position does not belong to its phase");
            }
        }
    }

    /**
     * Carries one validated SCIM collection page.
     *
     * @param items minimal items
     * @param next  next start index or zero
     * @param <T>   item type
     * @author Kimi Liu
     */
    private record ScimPage<T>(List<T> items, int next) {

        /**
         * Freezes one page.
         */
        private ScimPage {
            items = List.copyOf(Assert.notNull(items, "Figma SCIM items must not be null"));
            if (next < 0) {
                throw new ValidateException("Figma SCIM next index must not be negative");
            }
        }
    }

    /**
     * Minimal Figma SCIM user projection.
     *
     * @param id          stable identifier
     * @param userName    login identifier
     * @param displayName display name
     * @param active      active flag
     * @param title       optional organization title
     * @author Kimi Liu
     */
    private record User(String id, String userName, String displayName, boolean active, Optional<String> title) {

        /**
         * Validates one user.
         */
        private User {
            id = requireText(id, "Figma user identifier");
            userName = requireText(userName, "Figma user name");
            displayName = requireText(displayName, "Figma user display name");
            title = Assert.notNull(title, "Figma user title container must not be null");
        }
    }

    /**
     * Minimal Figma SCIM group projection.
     *
     * @param id          stable identifier
     * @param displayName display name
     * @param members     embedded stable user identifiers
     * @author Kimi Liu
     */
    private record Group(String id, String displayName, List<String> members) {

        /**
         * Validates one group.
         */
        private Group {
            id = requireText(id, "Figma group identifier");
            displayName = requireText(displayName, "Figma group display name");
            members = List.copyOf(Assert.notNull(members, "Figma group members must not be null"));
        }
    }

}
