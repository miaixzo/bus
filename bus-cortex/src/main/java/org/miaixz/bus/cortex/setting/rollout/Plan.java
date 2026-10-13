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

import org.miaixz.bus.cortex.setting.item.GrayRule;
import org.miaixz.bus.cortex.setting.item.GrayType;
import org.miaixz.bus.cortex.setting.item.ItemNormalizer;
import org.miaixz.bus.cortex.setting.reference.Reference;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Immutable application release manifest. Selected applications replace their complete baseline manifests; unselected
 * applications retain their baseline revisions. Deletions use exact tombstone revision identifiers. Relationship and
 * approval inputs are frozen by value. The host commits this manifest, approvals, version activation, and notification
 * outbox in one transaction.
 */
public final class Plan {

    private final String tenant;
    private final String baseline;
    private final String version;
    private final String mode;
    private final String policy;
    private final String rule;
    private final String references;
    private final Map<String, Map<String, String>> selected;
    private final Map<String, Map<String, String>> applications;
    private final String checksum;

    /**
     * Freezes exact revision identifiers, never dynamic LATEST lookups. A deletion must freeze its tombstone revision
     * rather than silently omit an item. Shared-source changes alone never activate consumers.
     */
    public Plan(String tenant, String baseline, String version, String mode, String policy, GrayRule rule,
            Map<String, Map<String, String>> previous, Map<String, Map<String, String>> selected,
            List<Reference> references) {
        ItemNormalizer.requireTenant(tenant);
        require(version, "Server-generated version");
        if (!version.matches("v\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{6}\\.\\d{3}(?:\\.\\d+)?")) {
            throw new IllegalArgumentException("Release version must be generated from server date and time");
        }
        Rollout.Mode parsed = Rollout.Mode.valueOf(mode);
        require(policy, "Approval policy");
        if (parsed == Rollout.Mode.GRAY && (rule == null || rule.getType() == null)) {
            throw new IllegalArgumentException("Gray publication requires a selector");
        }
        if (parsed == Rollout.Mode.FORMAL && rule != null) {
            throw new IllegalArgumentException("Formal publication cannot contain a gray selector");
        }
        if (selected == null || selected.isEmpty()) {
            throw new IllegalArgumentException("At least one application must be selected");
        }
        if (rule != null) {
            rule.validate();
            if (rule.getType() == GrayType.TENANT && !tenant.equals(rule.getTenant())) {
                throw new IllegalArgumentException("Gray selector cannot target a different tenant");
            }
        }
        this.tenant = tenant;
        this.baseline = baseline;
        this.version = version;
        this.mode = mode;
        this.policy = policy;
        this.rule = rule == null ? null : JsonKit.toJsonString(rule);
        this.selected = freeze(selected);
        if (this.selected.values().stream().anyMatch(Map::isEmpty)) {
            throw new IllegalArgumentException("Selected applications must contain explicit revisions or tombstones");
        }
        Map<String, Map<String, String>> merged = new TreeMap<>(freeze(previous == null ? Map.of() : previous));
        this.selected.forEach((app, items) -> {
            if (!items.keySet().containsAll(merged.getOrDefault(app, Map.of()).keySet())) {
                throw new IllegalArgumentException("Baseline items require explicit revisions or tombstones");
            }
        });
        merged.putAll(this.selected);
        this.applications = Collections.unmodifiableMap(merged);
        Map<String, Map<String, String>> frozenReferences = new TreeMap<>();
        for (Reference reference : references == null ? List.<Reference>of() : references) {
            reference.validate();
            if (!tenant.equals(reference.getTenant_id()) || !Reference.Type.ITEM_APP.name().equals(reference.getType())
                    || !this.selected.containsKey(reference.getTarget_id())) {
                throw new IllegalArgumentException("Reference must belong to a selected application and tenant");
            }
            String key = segment(reference.getTarget_id()) + segment(reference.getAlias());
            Map<String, String> value = new TreeMap<>();
            value.put("source_id", reference.getSource_id());
            value.put("target_id", reference.getTarget_id());
            value.put("alias", reference.getAlias());
            value.put(
                    "strategy",
                    reference.getStrategy() == null ? Reference.Strategy.LATEST.name() : reference.getStrategy());
            if (reference.getRevision_id() != null) {
                value.put("revision_id", reference.getRevision_id());
            }
            if (reference.getEdition() != null) {
                value.put("edition", reference.getEdition().toString());
            }
            if (frozenReferences.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Duplicate application-local reference alias");
            }
        }
        this.references = JsonKit.toJsonString(frozenReferences);
        this.checksum = Builder.sha256(toJson());
    }

    private static Map<String, Map<String, String>> freeze(Map<String, Map<String, String>> input) {
        Map<String, Map<String, String>> result = new TreeMap<>();
        input.forEach((app, items) -> {
            require(app, "Application identifier");
            if (items == null) {
                throw new IllegalArgumentException("Application manifest is required");
            }
            Map<String, String> copy = new TreeMap<>();
            items.forEach((item, revision) -> {
                require(item, "Item identifier");
                require(revision, "Exact revision identifier");
                copy.put(item, revision);
            });
            result.put(app, Collections.unmodifiableMap(copy));
        });
        return Collections.unmodifiableMap(result);
    }

    private static void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    private static String segment(String value) {
        return value.length() + ":" + value;
    }

    /** Serializes the canonical frozen input used for approval and execution checksums. */
    public String toJson() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("tenant_id", tenant);
        value.put("baseline", baseline);
        value.put("version", version);
        value.put("mode", mode);
        value.put("policy", policy);
        value.put("rule", rule);
        value.put("references", references);
        value.put("selected", selected);
        value.put("applications", applications);
        return JsonKit.toJsonString(value);
    }

    public String getTenant_id() {
        return tenant;
    }

    public String getBaseline() {
        return baseline;
    }

    public String getVersion() {
        return version;
    }

    public String getChecksum() {
        return checksum;
    }

    public Map<String, Map<String, String>> getApplications() {
        return applications;
    }

    public Map<String, Map<String, String>> getSelected() {
        return selected;
    }

    /** Rebuilds a frozen plan after restart and rejects corrupted or re-serialized approval inputs. */
    @SuppressWarnings("unchecked")
    public static Plan restore(Rollout rollout) {
        if (rollout == null || rollout.getManifest() == null || rollout.getChecksum() == null
                || !Builder.sha256(rollout.getManifest()).equals(rollout.getChecksum())) {
            throw new IllegalArgumentException("Persisted publication manifest is missing or corrupted");
        }
        Map<String, Object> root = JsonKit.toPojo(rollout.getManifest(), Map.class);
        List<Reference> references = new ArrayList<>();
        Map<String, Map<String, String>> stored = JsonKit.toPojo((String) root.get("references"), Map.class);
        for (Map<String, String> value : stored.values()) {
            references.add(
                    Reference.builder().tenant_id(rollout.getTenant_id()).source_id(value.get("source_id"))
                            .target_id(value.get("target_id")).type(Reference.Type.ITEM_APP.name())
                            .alias(value.get("alias")).strategy(value.get("strategy"))
                            .revision_id(value.get("revision_id"))
                            .edition(value.get("edition") == null ? null : Long.valueOf(value.get("edition"))).build());
        }
        String rule = (String) root.get("rule");
        Plan plan = new Plan((String) root.get("tenant_id"), (String) root.get("baseline"),
                (String) root.get("version"), (String) root.get("mode"), (String) root.get("policy"),
                rule == null ? null : JsonKit.toPojo(rule, GrayRule.class),
                (Map<String, Map<String, String>>) root.get("applications"),
                (Map<String, Map<String, String>>) root.get("selected"), references);
        plan.verify(rollout);
        return plan;
    }

    /** Rejects mutation, stale approval, or a publication under another tenant. */
    public void verify(Rollout rollout) {
        if (rollout == null || !tenant.equals(rollout.getTenant_id()) || !checksum.equals(rollout.getChecksum())
                || !toJson().equals(rollout.getManifest()) || !mode.equals(rollout.getMode())
                || !policy.equals(rollout.getPolicy()) || !Objects.equals(rule, rollout.getRule())) {
            throw new IllegalArgumentException("Publication no longer matches its frozen plan");
        }
    }

    /** Freezes only an editable draft; the host persists this result with an edition compare-and-set. */
    public void submit(Rollout rollout) {
        if (rollout == null || !tenant.equals(rollout.getTenant_id())
                || !Rollout.State.DRAFT.name().equals(rollout.getState())) {
            throw new IllegalArgumentException("Only a tenant-owned draft can be submitted");
        }
        rollout.setMode(mode);
        rollout.setPolicy(policy);
        rollout.setRule(rule);
        rollout.setManifest(toJson());
        rollout.setChecksum(checksum);
        rollout.setState(Rollout.State.REVIEWING.name());
    }

    /** Tests membership in the selected scope rather than the merged baseline. */
    public boolean includes(String app, String item, String revision) {
        return app != null && revision != null && revision.equals(selected.getOrDefault(app, Map.of()).get(item));
    }
}
