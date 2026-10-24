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
package org.miaixz.bus.cortex.release;

import java.util.*;

import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.rollout.GrayRule;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes, decodes and verifies published release graphs and their immutable snapshot identities.
 *
 * @author Kimi Liu
 */
public final class ReleaseCodec {

    /**
     * Only accepted formal configuration release protocol.
     */
    public static final String FORMAT = "services-release-v3";

    /**
     * Prevents construction.
     */
    private ReleaseCodec() {
        // No initialization required.
    }

    /**
     * Decodes current formal metadata only.
     *
     * @param release formal release
     * @return published document
     */
    public static Document decode(Release release) {
        if (release == null || release.getId() == null || release.getVersion() == null
                || release.getPublished() == null)
            throw new IllegalArgumentException("Formal release required");
        JsonValue raw = ItemCodec.tree(release.getMetadata());
        if (!(raw instanceof JsonValue.ObjectValue object)
                || !Set.of("format", "mode", "rule", "applications", "checksum").containsAll(object.values().keySet())
                || !FORMAT.equals(text(object, "format")))
            throw new IllegalArgumentException("Unsupported formal release document");
        Map<String, List<Binding>> apps = new TreeMap<>();
        if (!(object.values().get("applications") instanceof JsonValue.ObjectValue graph))
            throw new IllegalArgumentException("Published applications missing");
        graph.values().forEach((app, value) -> {
            List<Binding> bindings = new ArrayList<>();
            for (JsonValue node : ((JsonValue.ArrayValue) value).values()) {
                if (!(node instanceof JsonValue.ObjectValue b)
                        || !Set.of("source", "options").equals(b.values().keySet()))
                    throw new IllegalArgumentException("Invalid published binding");
                bindings.add(new Binding(text(b, "source"), ReferenceCodec.options(b.values().get("options"))));
            }
            apps.put(app, bindings);
        });
        if (text(object, "checksum") == null)
            throw new IllegalArgumentException("Release checksum required");
        return new Document(Plan.Mode.valueOf(text(object, "mode")),
                object.values().get("rule") == null ? null
                        : JsonKit.toPojo(ItemCodec.encode(object.values().get("rule")), GrayRule.class),
                apps, text(object, "checksum"));
    }

    /**
     * Encodes self-contained formal content.
     *
     * @param document immutable graph
     * @return metadata JSON
     */
    public static String encode(Document document) {
        return ItemCodec.encode(tree(document, true));
    }

    /**
     * Computes graph and exact snapshot coordinate integrity, excluding its own checksum.
     *
     * @param document  graph
     * @param snapshots exact complete snapshot set
     * @return checksum
     */
    public static String checksum(Document document, Map<String, Revision> snapshots) {
        Set<String> required = new TreeSet<>();
        document.applications().values().forEach(list -> list.forEach(binding -> required.add(binding.source())));
        if (!required.equals(snapshots.keySet()))
            throw new IllegalStateException("Incomplete published snapshot set");
        Map<String, JsonValue> identity = new TreeMap<>();
        for (String id : required) {
            Revision revision = snapshots.get(id);
            if (revision == null || !id.equals(revision.getId()) || revision.getRelease_id() == null)
                throw new IllegalStateException("Invalid published snapshot identity");
            Plan.Definition definition = definition(revision);
            identity.put(
                    id,
                    JsonKit.toValue(List.of(revision.getRelease_id(), definition.item_id(), definition.checksum())));
        }
        return Builder.sha256(
                ItemCodec.encode(
                        new JsonValue.ObjectValue(Map.of(
                                "manifest",
                                tree(document, false),
                                "snapshots",
                                new JsonValue.ObjectValue(identity)))));
    }

    /**
     * Verifies exact tenant/release ownership and frozen graph digest.
     *
     * @param release   formal record
     * @param snapshots complete exact snapshot set
     * @return verified release document
     */
    public static Document verify(Release release, Map<String, Revision> snapshots) {
        Document document = decode(release);
        snapshots.values().forEach(r -> {
            if (!Objects.equals(release.getTenant_id(), r.getTenant_id())
                    || !Objects.equals(release.getId(), r.getRelease_id()))
                throw new IllegalStateException("Snapshot ownership mismatch");
        });
        if (!document.checksum().equals(checksum(document, snapshots)))
            throw new IllegalStateException("Release checksum mismatch");
        return document;
    }

    /**
     * Maps immutable published content without Item editing state.
     *
     * @param revision published snapshot
     * @return frozen definition
     */
    public static Plan.Definition definition(Revision revision) {
        return new Plan.Definition(revision.getItem_id(), revision.getType(), revision.getCode(), revision.getContent(),
                revision.getFormat(), revision.getSchema(), revision.getEncrypted(), revision.getExtension(),
                revision.getChecksum(), revision.getDescription());
    }

    /**
     * Builds an exact protocol tree.
     *
     * @param document        graph
     * @param includeChecksum whether to emit stored digest
     * @return tree
     */
    private static JsonValue.ObjectValue tree(Document document, boolean includeChecksum) {
        Map<String, JsonValue> fields = new TreeMap<>(), apps = new TreeMap<>();
        fields.put("format", new JsonValue.StringValue(FORMAT));
        fields.put("mode", new JsonValue.StringValue(document.mode().name()));
        if (document.rule() != null)
            fields.put("rule", JsonKit.toValue(document.rule()));
        document.applications().forEach((app, bindings) -> {
            List<JsonValue> list = new ArrayList<>();
            for (Binding binding : bindings)
                list.add(
                        new JsonValue.ObjectValue(Map.of(
                                "source",
                                new JsonValue.StringValue(binding.source()),
                                "options",
                                ReferenceCodec.tree(binding.options()))));
            apps.put(app, new JsonValue.ArrayValue(list));
        });
        fields.put("applications", new JsonValue.ObjectValue(apps));
        if (includeChecksum)
            fields.put("checksum", new JsonValue.StringValue(Objects.requireNonNull(document.checksum())));
        return new JsonValue.ObjectValue(fields);
    }

    /**
     * Reads an optional protocol text member.
     *
     * @param object tree
     * @param name   member
     * @return string or absent
     */
    private static String text(JsonValue.ObjectValue object, String name) {
        JsonValue value = object.values().get(name);
        return value == null ? null : ((JsonValue.StringValue) value).value();
    }

    /**
     * Published exact usage.
     *
     * @param source  exact committed Revision ID
     * @param options frozen relationship options
     * @author Kimi Liu
     */
    public record Binding(String source, ReferenceCodec.Options options) {

        /**
         * Requires exact source usage.
         *
         * @param source  exact snapshot
         * @param options usage options
         */
        public Binding {
            if (source == null || source.isBlank())
                throw new IllegalArgumentException("Published snapshot required");
            Objects.requireNonNull(options);
        }

    }

    /**
     * Immutable full published application graph.
     *
     * @param mode         publication track
     * @param rule         frozen selector
     * @param applications full graph
     * @param checksum     graph and snapshot digest
     * @author Kimi Liu
     */
    public record Document(Plan.Mode mode, GrayRule rule, Map<String, List<Binding>> applications, String checksum) {

        /**
         * Freezes graph and selector.
         *
         * @param mode         publication track
         * @param rule         selector
         * @param applications full graph
         * @param checksum     digest
         */
        public Document {
            Objects.requireNonNull(mode);
            if (mode == Plan.Mode.GRAY)
                Objects.requireNonNull(rule).validate();
            else if (rule != null)
                throw new IllegalArgumentException("Formal gray selector");
            rule = rule == null ? null : rule.copy();
            Map<String, List<Binding>> graph = new TreeMap<>();
            applications.forEach((app, bindings) -> {
                if (app == null || app.isBlank())
                    throw new IllegalArgumentException("Application required");
                graph.put(app, List.copyOf(bindings));
            });
            applications = Collections.unmodifiableMap(graph);
        }

        /**
         * Returns detached selector data.
         *
         * @return frozen rule
         */
        @Override
        public GrayRule rule() {
            return rule == null ? null : rule.copy();
        }

    }

}
