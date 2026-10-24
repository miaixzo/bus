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
package org.miaixz.bus.cortex.setting.rollout;

import java.util.*;

import org.miaixz.bus.core.lang.EnumValue.Format;
import org.miaixz.bus.cortex.setting.item.Item;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Immutable candidate protocol: workflow input without any formal version.
 *
 * @author Kimi Liu
 */
public final class Plan {

    /**
     * Only accepted candidate protocol.
     */
    public static final String FORMAT = "services-rollout-v3";

    /**
     * Prevents construction.
     */
    private Plan() {
        // No initialization required.
    }

    /**
     * Encodes draft selection.
     *
     * @param selection explicit draft input
     * @return current protocol JSON
     */
    public static String selection(Selection selection) {
        return ItemCodec.encode(envelope("selection", selectionTree(selection)));
    }

    /**
     * Decodes only current draft protocol.
     *
     * @param encoded draft JSON
     * @return selection
     */
    public static Selection selection(String encoded) {
        return readSelection(payload(encoded, "selection"));
    }

    /**
     * Encodes complete immutable review material.
     *
     * @param frozen candidate
     * @return protected protocol JSON
     */
    public static String frozen(Frozen frozen) {
        return ItemCodec.encode(envelope("frozen", frozenTree(frozen)));
    }

    /**
     * Decodes complete immutable review material.
     *
     * @param encoded protected JSON
     * @return frozen candidate
     */
    public static Frozen frozen(String encoded) {
        JsonValue.ObjectValue raw = object(
                payload(encoded, "frozen"),
                Set.of("baseline", "selection", "definitions", "applications"));
        Map<String, Definition> definitions = new TreeMap<>();
        object(raw.values().get("definitions"), null).values().forEach(
                (key, value) -> definitions.put(key, JsonKit.toPojo(ItemCodec.encode(value), Definition.class)));
        Map<String, List<Binding>> apps = new TreeMap<>();
        object(raw.values().get("applications"), null).values().forEach((app, value) -> {
            List<Binding> bindings = new ArrayList<>();
            for (JsonValue node : ((JsonValue.ArrayValue) value).values()) {
                JsonValue.ObjectValue b = object(node, Set.of("source", "options", "pinnedRevision"));
                bindings.add(
                        new Binding(text(b, "source"), ReferenceCodec.options(b.values().get("options")),
                                text(b, "pinnedRevision")));
            }
            apps.put(app, bindings);
        });
        return new Frozen(text(raw, "baseline"), readSelection(raw.values().get("selection")), definitions, apps);
    }

    /**
     * Computes the exact approved input digest without future server version/time.
     *
     * @param frozen approved input
     * @return checksum
     */
    public static String checksum(Frozen frozen) {
        return Builder.sha256(frozen(frozen));
    }

    /**
     * Encodes a strict protocol wrapper.
     *
     * @param stage   selection/frozen
     * @param payload exact content
     * @return envelope
     */
    private static JsonValue.ObjectValue envelope(String stage, JsonValue payload) {
        return new JsonValue.ObjectValue(Map.of(
                "format",
                new JsonValue.StringValue(FORMAT),
                "stage",
                new JsonValue.StringValue(stage),
                "payload",
                payload));
    }

    /**
     * Validates wrapper and stage.
     *
     * @param encoded JSON
     * @param stage   required stage
     * @return payload
     */
    private static JsonValue payload(String encoded, String stage) {
        JsonValue.ObjectValue raw = object(ItemCodec.tree(encoded), Set.of("format", "stage", "payload"));
        if (!FORMAT.equals(text(raw, "format")) || !stage.equals(text(raw, "stage")))
            throw new IllegalArgumentException("Unsupported candidate protocol");
        return Objects.requireNonNull(raw.values().get("payload"));
    }

    /**
     * Serializes selection using exact JSON trees.
     *
     * @param selection draft input
     * @return tree
     */
    private static JsonValue selectionTree(Selection selection) {
        Map<String, JsonValue> fields = new TreeMap<>();
        fields.put("applications", JsonKit.toValue(selection.applications()));
        fields.put("mode", new JsonValue.StringValue(selection.mode().name()));
        fields.put("policy", new JsonValue.StringValue(selection.policy()));
        fields.put("notifyOwners", new JsonValue.BooleanValue(selection.notifyOwners()));
        if (selection.rule() != null)
            fields.put("rule", JsonKit.toValue(selection.rule()));
        return new JsonValue.ObjectValue(fields);
    }

    /**
     * Deserializes exact selection members.
     *
     * @param value tree
     * @return selection
     */
    private static Selection readSelection(JsonValue value) {
        JsonValue.ObjectValue raw = object(value, Set.of("applications", "mode", "policy", "rule", "notifyOwners"));
        Set<String> apps = new TreeSet<>();
        for (JsonValue app : ((JsonValue.ArrayValue) raw.values().get("applications")).values())
            apps.add(((JsonValue.StringValue) app).value());
        return new Selection(apps, Mode.valueOf(text(raw, "mode")), text(raw, "policy"),
                raw.values().get("rule") == null ? null
                        : JsonKit.toPojo(ItemCodec.encode(raw.values().get("rule")), GrayRule.class),
                ((JsonValue.BooleanValue) raw.values().get("notifyOwners")).value());
    }

    /**
     * Builds canonical candidate content.
     *
     * @param frozen candidate
     * @return protected tree
     */
    private static JsonValue frozenTree(Frozen frozen) {
        Map<String, JsonValue> fields = new TreeMap<>(), definitions = new TreeMap<>(), apps = new TreeMap<>();
        if (frozen.baseline() != null)
            fields.put("baseline", new JsonValue.StringValue(frozen.baseline()));
        fields.put("selection", selectionTree(frozen.selection()));
        frozen.definitions().forEach((key, value) -> definitions.put(key, JsonKit.toValue(value)));
        frozen.applications().forEach((app, bindings) -> {
            List<JsonValue> values = new ArrayList<>();
            for (Binding binding : bindings) {
                Map<String, JsonValue> b = new TreeMap<>();
                b.put("source", new JsonValue.StringValue(binding.source()));
                b.put("options", ReferenceCodec.tree(binding.options()));
                if (binding.pinnedRevision() != null)
                    b.put("pinnedRevision", new JsonValue.StringValue(binding.pinnedRevision()));
                values.add(new JsonValue.ObjectValue(b));
            }
            apps.put(app, new JsonValue.ArrayValue(values));
        });
        fields.put("definitions", new JsonValue.ObjectValue(definitions));
        fields.put("applications", new JsonValue.ObjectValue(apps));
        return new JsonValue.ObjectValue(fields);
    }

    /**
     * Requires an object and rejects unrecognized protocol members.
     *
     * @param value   tree
     * @param allowed allowed members or unrestricted map
     * @return object
     */
    private static JsonValue.ObjectValue object(JsonValue value, Set<String> allowed) {
        if (!(value instanceof JsonValue.ObjectValue object)
                || allowed != null && !allowed.containsAll(object.values().keySet()))
            throw new IllegalArgumentException("Invalid candidate members");
        return object;
    }

    /**
     * Reads an optional text member.
     *
     * @param object object
     * @param name   member
     * @return text or absent
     */
    private static String text(JsonValue.ObjectValue object, String name) {
        JsonValue value = object.values().get(name);
        return value == null ? null : ((JsonValue.StringValue) value).value();
    }

    /**
     * Publication track choice.
     *
     * @author Kimi Liu
     */
    public enum Mode {
        /**
         * Stable publication.
         */
        FORMAL,
        /**
         * Frozen instance-selected gray publication.
         */
        GRAY

    }

    /**
     * Editable draft input.
     *
     * @param applications explicitly selected apps
     * @param mode         publication track
     * @param policy       registered approval policy
     * @param rule         gray selector
     * @param notifyOwners owner notification option
     * @author Kimi Liu
     */
    public record Selection(Set<String> applications, Mode mode, String policy, GrayRule rule, boolean notifyOwners) {

        /**
         * Requires explicit nonempty selection and registered policy identity.
         *
         * @param applications selected apps
         * @param mode         track
         * @param policy       approval policy
         * @param rule         gray selector
         * @param notifyOwners notification option
         */
        public Selection {
            applications = Collections.unmodifiableSet(new TreeSet<>(applications));
            Objects.requireNonNull(mode);
            if (applications.isEmpty() || applications.stream().anyMatch(a -> a == null || a.isBlank())
                    || policy == null || policy.isBlank())
                throw new IllegalArgumentException("Explicit apps and policy required");
            if (mode == Mode.GRAY) {
                Objects.requireNonNull(rule).validate();
            } else if (rule != null)
                throw new IllegalArgumentException("Formal publication cannot carry gray routing");
            rule = rule == null ? null : rule.copy();
        }

        /**
         * Returns a defensive rule copy.
         *
         * @return frozen rule
         */
        @Override
        public GrayRule rule() {
            return rule == null ? null : rule.copy();
        }

    }

    /**
     * Exact protected definition frozen for review.
     *
     * @param item_id     source identity
     * @param type        category
     * @param code        source code
     * @param content     protected definition
     * @param format      delivery format
     * @param schema      protected Schema
     * @param encrypted   protection flag
     * @param extension   protected metadata
     * @param checksum    definition digest
     * @param description source description
     * @author Kimi Liu
     */
    public record Definition(String item_id, Integer type, String code, String content, String format, String schema,
            Integer encrypted, String extension, String checksum, String description) {

        /**
         * Requires complete protected source identity and checksum.
         *
         * @param item_id     source
         * @param type        category
         * @param code        code
         * @param content     definition
         * @param format      delivery format
         * @param schema      Schema
         * @param encrypted   protection flag
         * @param extension   metadata
         * @param checksum    digest
         * @param description description
         */
        public Definition {
            if (item_id == null || item_id.isBlank() || code == null || code.isBlank() || content == null
                    || checksum == null)
                throw new IllegalArgumentException("Incomplete frozen definition");
            Item.Type.of(type);
            Format.valueOf(format);
            if (!Integer.valueOf(0).equals(encrypted) && !Integer.valueOf(1).equals(encrypted))
                throw new IllegalArgumentException("Invalid frozen protection");
            Item test = Item.builder().type(type).code(code).content(content).format(format).schema(schema)
                    .encrypted(encrypted).extension(extension).description(description).build();
            if (!checksum.equals(new ItemCodec(64, 100000).checksum(test)))
                throw new IllegalStateException("Frozen definition checksum mismatch");
        }

        /**
         * Captures current protected content without editing metadata.
         *
         * @param item source
         * @return frozen definition
         */
        public static Definition of(Item item) {
            return new Definition(item.getId(), item.getType(), item.getCode(), item.getContent(), item.getFormat(),
                    item.getSchema(), item.getEncrypted(), item.getExtension(), item.getChecksum(),
                    item.getDescription());
        }

        /**
         * Creates detached source coordinates for verified authorized materialization.
         *
         * @param tenant owning tenant
         * @return protected definition
         */
        public Item item(String tenant) {
            return Item.builder().tenant_id(tenant).id(item_id).type(type).code(code).content(content).format(format)
                    .schema(schema).encrypted(encrypted).extension(extension).checksum(checksum)
                    .description(description).build();
        }

        /**
         * Distinguishes simultaneous fixed/current content for the same source.
         *
         * @return opaque candidate source key
         */
        public String key() {
            return Builder.sha256(ItemCodec.encode(JsonKit.toValue(List.of(item_id, checksum))));
        }

    }

    /**
     * Frozen application usage.
     *
     * @param source         opaque definition key
     * @param options        typed usage options
     * @param pinnedRevision original fixed published pin, if any
     * @author Kimi Liu
     */
    public record Binding(String source, ReferenceCodec.Options options, String pinnedRevision) {

        /**
         * Requires complete source usage.
         *
         * @param source         source key
         * @param options        options
         * @param pinnedRevision original pin
         */
        public Binding {
            Objects.requireNonNull(source);
            Objects.requireNonNull(options);
        }

    }

    /**
     * Approved full resulting graph; no formal version.
     *
     * @param baseline     expected stable Release ID
     * @param selection    selected app input
     * @param definitions  exact protected content
     * @param applications full post-publication graph
     * @author Kimi Liu
     */
    public record Frozen(String baseline, Selection selection, Map<String, Definition> definitions,
            Map<String, List<Binding>> applications) {

        /**
         * Freezes membership and verifies all source bindings.
         *
         * @param baseline     stable baseline
         * @param selection    draft input
         * @param definitions  source definitions
         * @param applications complete graph
         */
        public Frozen {
            Objects.requireNonNull(selection);
            definitions = Collections.unmodifiableMap(new TreeMap<>(definitions));
            Map<String, List<Binding>> graph = new TreeMap<>();
            Map<String, String> owners = new TreeMap<>();
            Set<String> shared = new HashSet<>();
            Set<String> used = new HashSet<>();
            for (var app : applications.entrySet()) {
                if (app.getKey() == null || app.getKey().isBlank())
                    throw new IllegalArgumentException("Invalid application identity");
                Set<String> aliases = new HashSet<>();
                for (Binding binding : app.getValue()) {
                    Definition definition = definitions.get(binding.source());
                    if (definition == null || !definition.key().equals(binding.source()))
                        throw new IllegalStateException("Frozen source missing or changed");
                    String alias = binding.options().alias().isEmpty() ? definition.code() : binding.options().alias();
                    if (!aliases.add(alias))
                        throw new IllegalArgumentException("Duplicate application alias");
                    if (binding.options().type() == Reference.Type.ITEM_OWNER) {
                        String owner = owners.putIfAbsent(definition.item_id(), app.getKey());
                        if (owner != null && !owner.equals(app.getKey()))
                            throw new IllegalArgumentException("Multiple source owners");
                    } else
                        shared.add(definition.item_id());
                    used.add(binding.source());
                }
                List<Binding> sorted = new ArrayList<>(app.getValue());
                sorted.sort(Comparator.comparing((Binding b) -> b.options().alias()).thenComparing(Binding::source));
                graph.put(app.getKey(), List.copyOf(sorted));
            }
            if (!graph.keySet().containsAll(selection.applications()) || !used.equals(definitions.keySet()))
                throw new IllegalArgumentException("Incomplete or unused frozen graph");
            if (owners.keySet().stream().anyMatch(shared::contains))
                throw new IllegalArgumentException("Owned source cannot also be shared");
            applications = Collections.unmodifiableMap(graph);
        }

    }

    /**
     * Submission concurrency evidence, not persisted editing history.
     *
     * @param items       source editions
     * @param references  relation editions
     * @param apps        application state digests
     * @param memberships complete relation membership digests
     * @param baseline    stable Release ID
     * @author Kimi Liu
     */
    public record ReadSet(Map<String, Long> items, Map<String, Long> references, Map<String, String> apps,
            Map<String, String> memberships, String baseline) {

        /**
         * Freezes full read membership.
         *
         * @param items       editions
         * @param references  editions
         * @param apps        application states
         * @param memberships relation memberships
         * @param baseline    expected stable release
         */
        public ReadSet {
            items = Map.copyOf(items);
            references = Map.copyOf(references);
            apps = Map.copyOf(apps);
            memberships = Map.copyOf(memberships);
        }

    }

}
