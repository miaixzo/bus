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
package org.miaixz.bus.auth;

import java.time.Instant;
import java.util.*;

import org.miaixz.bus.auth.vendor.Vendor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Enumers;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Defines the provider-neutral enterprise directory model exposed by Vendor workers.
 * <p>
 * The model represents stable resources, relations, snapshots, recoverable changes, and direct retrieval without
 * leaking any platform wire representation. Every value validates and detaches caller-owned mutable state at its
 * construction boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public class Realm {

    /**
     * Prevents construction of the enterprise model container.
     */
    public Realm() {
        // No initialization required.
    }

    /**
     * Creates the local capability that describes one Vendor enterprise surface.
     *
     * @param vendor stable Vendor platform identifier
     * @return strongly typed enterprise description capability
     */
    public static Capability<Describe, Description> describe(final Vendor.Id vendor) {
        return capability(vendor, Builder.ENTERPRISE_DESCRIBE_SUFFIX, Describe.class, Description.class);
    }

    /**
     * Creates the capability that reads one recoverable enterprise snapshot page.
     *
     * @param vendor stable Vendor platform identifier
     * @return strongly typed enterprise snapshot capability
     */
    public static Capability<Snapshot, Page> snapshot(final Vendor.Id vendor) {
        return capability(vendor, Builder.ENTERPRISE_SNAPSHOT_SUFFIX, Snapshot.class, Page.class);
    }

    /**
     * Creates the capability that reads one recoverable enterprise change page.
     *
     * @param vendor stable Vendor platform identifier
     * @return strongly typed enterprise change capability
     */
    public static Capability<Changes, ChangePage> changes(final Vendor.Id vendor) {
        return capability(vendor, Builder.ENTERPRISE_CHANGES_SUFFIX, Changes.class, ChangePage.class);
    }

    /**
     * Creates the capability that retrieves one enterprise resource by stable key.
     *
     * @param vendor stable Vendor platform identifier
     * @return strongly typed enterprise retrieval capability
     */
    public static Capability<Retrieve, Retrieved> retrieve(final Vendor.Id vendor) {
        return capability(vendor, Builder.ENTERPRISE_RETRIEVE_SUFFIX, Retrieve.class, Retrieved.class);
    }

    /**
     * Builds one enterprise application capability with the common invocation boundary.
     *
     * @param vendor       stable Vendor platform identifier
     * @param suffix       operation suffix defined by the authentication root constants
     * @param requestType  exact request value type
     * @param responseType exact success value type
     * @param <Q>          request value type
     * @param <S>          success value type
     * @return immutable enterprise capability declaration
     */
    private static <Q, S> Capability<Q, S> capability(
            final Vendor.Id vendor,
            final String suffix,
            final Class<Q> requestType,
            final Class<S> responseType) {
        final Vendor.Id platform = Assert.notNull(vendor, "Enterprise capability Vendor must not be null");
        final String vendorId = requireText(platform.value(), "Enterprise capability Vendor id");
        return new Capability<>(Capability.Key.application(Builder.VENDOR_OPERATION_PREFIX + vendorId + suffix),
                requestType, responseType, Capability.Direction.SOURCE, Set.of(Capability.Interaction.DIRECT),
                Capability.Security.CLIENT_AUTHENTICATED);
    }

    /**
     * Validates a required string without silently normalizing surrounding whitespace.
     *
     * @param value string supplied by the caller
     * @param label semantic field label used by validation failures
     * @return the original validated string
     */
    private static String requireText(final String value, final String label) {
        final String text = Assert.notBlank(value, label + " must not be blank");
        if (!text.equals(text.trim())) {
            throw new ValidateException(label + " must not contain surrounding whitespace");
        }
        return text;
    }

    /**
     * Validates and freezes ordered alternate identifiers without changing their text.
     *
     * @param values caller-supplied identifier map
     * @return insertion-ordered immutable identifier map
     */
    private static Map<String, String> identifiers(final Map<String, String> values) {
        Assert.notNull(values, "Enterprise resource identifiers must not be null");
        final Map<String, String> copy = new LinkedHashMap<>(values.size());
        values.forEach(
                (name, value) -> copy.put(
                        requireText(name, "Enterprise identifier name"),
                        requireText(value, "Enterprise identifier value")));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Rebuilds a provider-neutral JSON object so it cannot share caller-owned map state.
     *
     * @param value caller-supplied JSON object
     * @param label semantic field label used by validation failures
     * @return detached immutable JSON object snapshot
     */
    private static JsonValue.ObjectValue object(final JsonValue.ObjectValue value, final String label) {
        return new JsonValue.ObjectValue(Assert.notNull(value, label + " must not be null").values());
    }

    /**
     * Validates and normalizes an enum set into ascending stable-code order.
     *
     * @param values   caller-supplied enum values
     * @param required whether at least one value is required
     * @param label    semantic field label used by validation failures
     * @param <E>      enum value type carrying a stable external code
     * @return stable-code-ordered immutable set
     */
    private static <E extends Enum<E> & Enumers<E>> Set<E> enumSet(
            final Set<E> values,
            final boolean required,
            final String label) {
        Assert.notNull(values, label + " must not be null");
        if (required && values.isEmpty()) {
            throw new ValidateException(label + " must not be empty");
        }
        final List<E> ordered = new ArrayList<>(values.size());
        for (E value : values) {
            ordered.add(Assert.notNull(value, label + " must not contain null"));
        }
        ordered.sort(Comparator.comparingInt(value -> value.code()));
        return Collections.unmodifiableSet(new LinkedHashSet<>(ordered));
    }

    /**
     * Validates one enterprise request limit against the shared framework boundary.
     *
     * @param value caller-supplied request limit
     * @param label semantic field label used by validation failures
     * @return validated request limit
     */
    private static int limit(final int value, final String label) {
        if (value < 1 || value > Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE) {
            throw new ValidateException(label + " must be within the supported page range");
        }
        return value;
    }

    /**
     * Detaches one Bus Optional container while retaining its immutable value.
     *
     * @param value caller-supplied optional container
     * @param label semantic field label used by validation failures
     * @param <T>   optional value type
     * @return detached optional container
     */
    private static <T> Optional<T> optional(final Optional<T> value, final String label) {
        final Optional<T> source = Assert.notNull(value, label + " must not be null");
        return Optional.ofNullable(source.getOrNull());
    }

    /**
     * Validates and freezes explicit coverage limitations in their declaration order.
     *
     * @param values caller-supplied limitation values
     * @return insertion-ordered immutable limitation list
     */
    private static List<String> limitations(final List<String> values) {
        Assert.notNull(values, "Enterprise description limitations must not be null");
        if (values.isEmpty()) {
            throw new ValidateException("Enterprise description limitations must not be empty");
        }
        final List<String> copy = new ArrayList<>(values.size());
        final Set<String> unique = new LinkedHashSet<>(values.size());
        for (String value : values) {
            final String limitation = requireText(value, "Enterprise description limitation");
            if (!unique.add(limitation)) {
                throw new ValidateException("Enterprise description limitations must be unique");
            }
            copy.add(limitation);
        }
        return Collections.unmodifiableList(copy);
    }

    /**
     * Validates, de-duplicates, and freezes resources by stable key while preserving first occurrence order.
     *
     * @param values caller-supplied resource list
     * @return insertion-ordered immutable resource list
     */
    private static List<Resource> resources(final List<Resource> values) {
        Assert.notNull(values, "Enterprise page resources must not be null");
        final Map<Key, Resource> unique = new LinkedHashMap<>(values.size());
        for (Resource value : values) {
            final Resource resource = Assert.notNull(value, "Enterprise page resource must not be null");
            final Resource previous = unique.putIfAbsent(resource.key(), resource);
            if (previous != null && !previous.equals(resource)) {
                throw new ValidateException("Enterprise page contains conflicting resources for one key");
            }
        }
        return List.copyOf(unique.values());
    }

    /**
     * Validates, de-duplicates, and freezes relations by stable key while preserving first occurrence order.
     *
     * @param values caller-supplied relation list
     * @return insertion-ordered immutable relation list
     */
    private static List<Relation> relations(final List<Relation> values) {
        Assert.notNull(values, "Enterprise page relations must not be null");
        final Map<RelationKey, Relation> unique = new LinkedHashMap<>(values.size());
        for (Relation value : values) {
            final Relation relation = Assert.notNull(value, "Enterprise page relation must not be null");
            final Relation previous = unique.putIfAbsent(relation.key(), relation);
            if (previous != null && !previous.equals(relation)) {
                throw new ValidateException("Enterprise page contains conflicting relations for one key");
            }
        }
        return List.copyOf(unique.values());
    }

    /**
     * Validates, de-duplicates, and freezes changes by affected key while preserving first occurrence order.
     *
     * @param values caller-supplied change list
     * @return insertion-ordered immutable change list
     */
    private static List<Change> changes(final List<Change> values) {
        Assert.notNull(values, "Enterprise change page entries must not be null");
        final List<Change> copy = new ArrayList<>(values.size());
        final Map<Key, Change> resourceChanges = new LinkedHashMap<>();
        final Map<RelationKey, Change> relationChanges = new LinkedHashMap<>();
        for (Change value : values) {
            final Change change = Assert.notNull(value, "Enterprise change page entry must not be null");
            final Change previous;
            if (change instanceof ResourceUpsert upsert) {
                previous = resourceChanges.putIfAbsent(upsert.resource().key(), change);
            } else if (change instanceof ResourceDelete delete) {
                previous = resourceChanges.putIfAbsent(delete.key(), change);
            } else if (change instanceof RelationUpsert upsert) {
                previous = relationChanges.putIfAbsent(upsert.relation().key(), change);
            } else if (change instanceof RelationDelete delete) {
                previous = relationChanges.putIfAbsent(delete.key(), change);
            } else {
                throw new ValidateException("Enterprise change page contains an unsupported change type");
            }
            if (previous == null) {
                copy.add(change);
            } else if (!previous.equals(change)) {
                throw new ValidateException("Enterprise change page contains conflicting changes for one key");
            }
        }
        return List.copyOf(copy);
    }

    /**
     * Identifies the provider-neutral category of an enterprise resource.
     *
     * @author Kimi Liu
     */
    public enum Kind implements Enumers<Kind> {

        /**
         * Human identity managed by the enterprise platform.
         */
        USER(1),

        /**
         * Organizational unit or directory container.
         */
        ORGANIZATION(2),

        /**
         * Explicit group of enterprise identities.
         */
        GROUP(3),

        /**
         * Role definition or role-assignment projection.
         */
        ROLE(4),

        /**
         * Non-human application or service identity.
         */
        SERVICE_ACCOUNT(5);

        /**
         * Stable persisted code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates one resource kind with its stable persisted code.
         *
         * @param code stable persisted code
         */
        Kind(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persisted resource-kind code.
         *
         * @return stable resource-kind code
         */
        @Override
        public int code() {
            return code;
        }
    }

    /**
     * Identifies the provider-neutral semantic of an enterprise relation.
     *
     * @author Kimi Liu
     */
    public enum RelationKind implements Enumers<RelationKind> {

        /**
         * Links a resource to its direct hierarchical parent.
         */
        PARENT(1),

        /**
         * Links an identity or nested group to a group-like container.
         */
        MEMBER(2),

        /**
         * Links an identity to its manager.
         */
        MANAGER(3),

        /**
         * Links an identity or service account to an assigned role.
         */
        ROLE_MEMBER(4),

        /**
         * Links a resource to an owning identity or organization.
         */
        OWNER(5),

        /**
         * Links an identity to an assigned application or service principal.
         */
        APPLICATION_ASSIGNMENT(6);

        /**
         * Stable persisted code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates one relation kind with its stable persisted code.
         *
         * @param code stable persisted code
         */
        RelationKind(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persisted relation-kind code.
         *
         * @return stable relation-kind code
         */
        @Override
        public int code() {
            return code;
        }
    }

    /**
     * Describes the normalized lifecycle state of an enterprise resource.
     *
     * @author Kimi Liu
     */
    public enum State implements Enumers<State> {

        /**
         * Resource is enabled and active at observation time.
         */
        ACTIVE(1),

        /**
         * Resource is disabled, suspended, archived, or otherwise inactive.
         */
        INACTIVE(2),

        /**
         * Upstream data does not provide a reliable normalized state.
         */
        UNKNOWN(3);

        /**
         * Stable persisted code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates one normalized state with its stable persisted code.
         *
         * @param code stable persisted code
         */
        State(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persisted state code.
         *
         * @return stable state code
         */
        @Override
        public int code() {
            return code;
        }
    }

    /**
     * Declares how completely one Variant exposes its documented enterprise surface.
     *
     * @author Kimi Liu
     */
    public enum Coverage implements Enumers<Coverage> {

        /**
         * Variant exposes the complete declared platform surface.
         */
        COMPLETE(1),

        /**
         * Variant exposes a documented subset subject to explicit limitations.
         */
        PARTIAL(2),

        /**
         * Upstream platform does not provide enough information to assert coverage.
         */
        UNKNOWN(3);

        /**
         * Stable persisted code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates one coverage value with its stable persisted code.
         *
         * @param code stable persisted code
         */
        Coverage(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persisted coverage code.
         *
         * @return stable coverage code
         */
        @Override
        public int code() {
            return code;
        }
    }

    /**
     * Identifies one operation in the enterprise capability family.
     *
     * @author Kimi Liu
     */
    public enum Operation implements Enumers<Operation> {

        /**
         * Reads local capability and coverage metadata.
         */
        DESCRIBE(1),

        /**
         * Reads a recoverable full-state snapshot.
         */
        SNAPSHOT(2),

        /**
         * Reads a recoverable change feed.
         */
        CHANGES(3),

        /**
         * Retrieves one resource by stable key.
         */
        RETRIEVE(4);

        /**
         * Stable persisted code independent of enum declaration order.
         */
        private final int code;

        /**
         * Creates one enterprise operation with its stable persisted code.
         *
         * @param code stable persisted code
         */
        Operation(final int code) {
            this.code = code;
        }

        /**
         * Returns the stable persisted operation code.
         *
         * @return stable operation code
         */
        @Override
        public int code() {
            return code;
        }
    }

    /**
     * Marks one immutable resource or relation mutation in an enterprise change feed.
     *
     * @author Kimi Liu
     */
    public sealed interface Change permits ResourceUpsert, ResourceDelete, RelationUpsert, RelationDelete {
    }

    /**
     * Identifies one enterprise resource independently of platform-specific wire structure.
     *
     * @param kind       normalized resource category
     * @param externalId stable upstream identifier within the resource category
     * @author Kimi Liu
     */
    public record Key(Kind kind, String externalId) {

        /**
         * Validates one stable enterprise resource key.
         *
         * @param kind       normalized resource category
         * @param externalId stable upstream identifier
         */
        public Key {
            kind = Assert.notNull(kind, "Enterprise key kind must not be null");
            externalId = requireText(externalId, "Enterprise key external id");
        }
    }

    /**
     * Identifies one directed enterprise relation by semantic kind and endpoints.
     *
     * @param kind normalized relation semantic
     * @param from relation source resource
     * @param to   relation target resource
     * @author Kimi Liu
     */
    public record RelationKey(RelationKind kind, Key from, Key to) {

        /**
         * Validates one directed non-self enterprise relation key.
         *
         * @param kind normalized relation semantic
         * @param from relation source resource
         * @param to   relation target resource
         */
        public RelationKey {
            kind = Assert.notNull(kind, "Enterprise relation kind must not be null");
            from = Assert.notNull(from, "Enterprise relation source must not be null");
            to = Assert.notNull(to, "Enterprise relation target must not be null");
            if (from.equals(to)) {
                throw new ValidateException("Enterprise relation must not form a self-loop");
            }
        }
    }

    /**
     * Carries one immutable provider-neutral enterprise resource observation.
     *
     * @param key         stable resource key
     * @param identifiers ordered non-sensitive alternate identifiers
     * @param displayName required human-readable display name
     * @param state       normalized lifecycle state
     * @param attributes  allow-listed non-sensitive extension attributes
     * @param observedAt  time at which the invocation observed the upstream state
     * @author Kimi Liu
     */
    public record Resource(Key key, Map<String, String> identifiers, String displayName, State state,
            JsonValue.ObjectValue attributes, Instant observedAt) {

        /**
         * Validates and freezes one enterprise resource observation.
         *
         * @param key         stable resource key
         * @param identifiers ordered non-sensitive alternate identifiers
         * @param displayName required human-readable display name
         * @param state       normalized lifecycle state
         * @param attributes  allow-listed non-sensitive extension attributes
         * @param observedAt  invocation observation time
         */
        public Resource {
            key = Assert.notNull(key, "Enterprise resource key must not be null");
            identifiers = Realm.identifiers(identifiers);
            displayName = requireText(displayName, "Enterprise resource display name");
            state = Assert.notNull(state, "Enterprise resource state must not be null");
            attributes = object(attributes, "Enterprise resource attributes");
            observedAt = Assert.notNull(observedAt, "Enterprise resource observation time must not be null");
        }
    }

    /**
     * Carries one immutable provider-neutral enterprise relation observation.
     *
     * @param key        stable relation key
     * @param attributes allow-listed non-sensitive extension attributes
     * @param observedAt time at which the invocation observed the upstream relation
     * @author Kimi Liu
     */
    public record Relation(RelationKey key, JsonValue.ObjectValue attributes, Instant observedAt) {

        /**
         * Validates and freezes one enterprise relation observation.
         *
         * @param key        stable relation key
         * @param attributes allow-listed non-sensitive extension attributes
         * @param observedAt invocation observation time
         */
        public Relation {
            key = Assert.notNull(key, "Enterprise relation key must not be null");
            attributes = object(attributes, "Enterprise relation attributes");
            observedAt = Assert.notNull(observedAt, "Enterprise relation observation time must not be null");
        }
    }

    /**
     * Carries one opaque recoverable enterprise pagination position.
     *
     * @param value canonical unpadded Base64 URL-safe cursor envelope
     * @author Kimi Liu
     */
    public record Cursor(String value) {

        /**
         * Validates one bounded canonical cursor value without altering it.
         *
         * @param value canonical unpadded Base64 URL-safe cursor envelope
         */
        public Cursor {
            value = requireText(value, "Enterprise cursor");
            if (value.length() > Builder.MAXIMUM_ENTERPRISE_CURSOR_LENGTH) {
                throw new ValidateException("Enterprise cursor exceeds the maximum length");
            }
        }

        /**
         * Returns a redacted representation that cannot disclose pagination state through logs.
         *
         * @return fixed redacted value
         */
        @Override
        public String toString() {
            return Builder.REDACTED_VALUE;
        }
    }

    /**
     * Describes the exact enterprise surface exposed by one compiled Vendor Variant.
     *
     * @param kinds           supported resource categories in stable code order
     * @param relations       supported relation semantics in stable code order
     * @param operations      supported enterprise operations in stable code order
     * @param coverage        documented coverage level
     * @param maximumPageSize framework-wide maximum page size
     * @param limitations     ordered explicit coverage limitations
     * @author Kimi Liu
     */
    public record Description(Set<Kind> kinds, Set<RelationKind> relations, Set<Operation> operations,
            Coverage coverage, int maximumPageSize, List<String> limitations) {

        /**
         * Validates and normalizes one immutable enterprise surface description.
         *
         * @param kinds           supported resource categories
         * @param relations       supported relation semantics
         * @param operations      supported enterprise operations
         * @param coverage        documented coverage level
         * @param maximumPageSize framework-wide maximum page size
         * @param limitations     ordered explicit coverage limitations
         */
        public Description {
            kinds = enumSet(kinds, true, "Enterprise description resource kinds");
            relations = enumSet(relations, false, "Enterprise description relation kinds");
            operations = enumSet(operations, true, "Enterprise description operations");
            coverage = Assert.notNull(coverage, "Enterprise description coverage must not be null");
            if (maximumPageSize != Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE) {
                throw new ValidateException("Enterprise description maximum page size must equal the framework limit");
            }
            limitations = Realm.limitations(limitations);
        }
    }

    /**
     * Requests the local description of a compiled enterprise Variant.
     *
     * @author Kimi Liu
     */
    public record Describe() {

        /**
         * Creates the stateless enterprise description request.
         */
        public Describe {
        }
    }

    /**
     * Requests one bounded page from a recoverable enterprise snapshot.
     *
     * @param kinds  non-empty requested resource categories
     * @param limit  separate maximum count for resources and relations
     * @param cursor opaque continuation position, or empty for the first page
     * @author Kimi Liu
     */
    public record Snapshot(Set<Kind> kinds, int limit, Optional<Cursor> cursor) {

        /**
         * Validates and normalizes one enterprise snapshot request.
         *
         * @param kinds  non-empty requested resource categories
         * @param limit  separate maximum count for resources and relations
         * @param cursor opaque continuation position
         */
        public Snapshot {
            kinds = enumSet(kinds, true, "Enterprise snapshot resource kinds");
            limit = Realm.limit(limit, "Enterprise snapshot limit");
            cursor = optional(cursor, "Enterprise snapshot cursor");
        }
    }

    /**
     * Requests one bounded page from a recoverable enterprise change feed.
     *
     * @param kinds  non-empty requested resource categories
     * @param limit  maximum number of returned changes
     * @param cursor opaque continuation position, or empty for the first page
     * @author Kimi Liu
     */
    public record Changes(Set<Kind> kinds, int limit, Optional<Cursor> cursor) {

        /**
         * Validates and normalizes one enterprise change request.
         *
         * @param kinds  non-empty requested resource categories
         * @param limit  maximum number of returned changes
         * @param cursor opaque continuation position
         */
        public Changes {
            kinds = enumSet(kinds, true, "Enterprise change resource kinds");
            limit = Realm.limit(limit, "Enterprise change limit");
            cursor = optional(cursor, "Enterprise change cursor");
        }
    }

    /**
     * Requests direct retrieval of one enterprise resource.
     *
     * @param key stable resource key
     * @author Kimi Liu
     */
    public record Retrieve(Key key) {

        /**
         * Validates one enterprise resource retrieval request.
         *
         * @param key stable resource key
         */
        public Retrieve {
            key = Assert.notNull(key, "Enterprise retrieve key must not be null");
        }
    }

    /**
     * Returns one normalized enterprise snapshot page.
     *
     * @param resources resources in stable output order
     * @param relations relations in stable output order
     * @param next      opaque next position, or empty when complete
     * @author Kimi Liu
     */
    public record Page(List<Resource> resources, List<Relation> relations, Optional<Cursor> next) {

        /**
         * Validates, de-duplicates, and freezes one enterprise snapshot page.
         *
         * @param resources resources in stable output order
         * @param relations relations in stable output order
         * @param next      opaque next position
         */
        public Page {
            resources = Realm.resources(resources);
            relations = Realm.relations(relations);
            if (resources.size() > Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE) {
                throw new ValidateException("Enterprise page contains too many resources");
            }
            if (relations.size() > Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE) {
                throw new ValidateException("Enterprise page contains too many relations");
            }
            next = optional(next, "Enterprise page next cursor");
        }
    }

    /**
     * Returns the result of direct resource retrieval.
     *
     * @param resource retrieved resource, or empty when the upstream explicitly reports absence
     * @author Kimi Liu
     */
    public record Retrieved(Optional<Resource> resource) {

        /**
         * Validates and detaches the retrieval result container.
         *
         * @param resource retrieved resource or empty
         */
        public Retrieved {
            resource = optional(resource, "Enterprise retrieved resource");
        }
    }

    /**
     * Upserts the complete normalized projection of one enterprise resource.
     *
     * @param resource current resource observation
     * @author Kimi Liu
     */
    public record ResourceUpsert(Resource resource) implements Change {

        /**
         * Validates one resource-upsert change.
         *
         * @param resource current resource observation
         */
        public ResourceUpsert {
            resource = Assert.notNull(resource, "Enterprise resource upsert must not be null");
        }
    }

    /**
     * Deletes one enterprise resource identified by its stable key.
     *
     * @param key        stable resource key
     * @param observedAt time at which the invocation observed the deletion
     * @author Kimi Liu
     */
    public record ResourceDelete(Key key, Instant observedAt) implements Change {

        /**
         * Validates one resource-delete change.
         *
         * @param key        stable resource key
         * @param observedAt invocation observation time
         */
        public ResourceDelete {
            key = Assert.notNull(key, "Enterprise resource delete key must not be null");
            observedAt = Assert.notNull(observedAt, "Enterprise resource delete observation time must not be null");
        }
    }

    /**
     * Upserts the complete normalized projection of one enterprise relation.
     *
     * @param relation current relation observation
     * @author Kimi Liu
     */
    public record RelationUpsert(Relation relation) implements Change {

        /**
         * Validates one relation-upsert change.
         *
         * @param relation current relation observation
         */
        public RelationUpsert {
            relation = Assert.notNull(relation, "Enterprise relation upsert must not be null");
        }
    }

    /**
     * Deletes one enterprise relation identified by its stable key.
     *
     * @param key        stable relation key
     * @param observedAt time at which the invocation observed the deletion
     * @author Kimi Liu
     */
    public record RelationDelete(RelationKey key, Instant observedAt) implements Change {

        /**
         * Validates one relation-delete change.
         *
         * @param key        stable relation key
         * @param observedAt invocation observation time
         */
        public RelationDelete {
            key = Assert.notNull(key, "Enterprise relation delete key must not be null");
            observedAt = Assert.notNull(observedAt, "Enterprise relation delete observation time must not be null");
        }
    }

    /**
     * Returns one normalized enterprise change-feed page.
     *
     * @param changes changes in stable output order
     * @param next    opaque next position, or empty when complete
     * @author Kimi Liu
     */
    public record ChangePage(List<Change> changes, Optional<Cursor> next) {

        /**
         * Validates, de-duplicates, and freezes one enterprise change page.
         *
         * @param changes changes in stable output order
         * @param next    opaque next position
         */
        public ChangePage {
            changes = Realm.changes(changes);
            if (changes.size() > Builder.MAXIMUM_ENTERPRISE_PAGE_SIZE) {
                throw new ValidateException("Enterprise change page contains too many changes");
            }
            next = optional(next, "Enterprise change page next cursor");
        }
    }

}
