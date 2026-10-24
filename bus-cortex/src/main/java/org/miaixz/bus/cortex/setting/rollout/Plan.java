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

import org.miaixz.bus.core.xyz.PatternKit;
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
 *
 * @author Kimi Liu
 */
public final class Plan {

    /**
     * Tenant that owns the publication plan.
     */
    private final String tenant;
    /**
     * Baseline version from which the plan was assembled.
     */
    private final String baseline;
    /**
     * Server-generated target version.
     */
    private final String version;
    /**
     * Formal or gray publication mode.
     */
    private final String mode;
    /**
     * Approval policy frozen with the plan.
     */
    private final String policy;
    /**
     * Serialized gray-selection rule, when applicable.
     */
    private final String rule;
    /**
     * Serialized application-local references frozen with the plan.
     */
    private final String references;
    /**
     * Explicit revisions selected for replacement by application.
     */
    private final Map<String, Map<String, String>> selected;
    /**
     * Complete merged application manifest executed by the host.
     */
    private final Map<String, Map<String, String>> applications;
    /**
     * SHA-256 checksum of the canonical serialized plan.
     */
    private final String checksum;

    /**
     * Freezes exact revision identifiers, never dynamic LATEST lookups. A deletion must freeze its tombstone revision
     * rather than silently omit an item. Shared-source changes alone never activate consumers.
     *
     * @param tenant tenant owning the publication
     * @param baseline expected previously active version, or null
     * @param version required server-generated numeric target version
     * @param mode FORMAL or GRAY publication mode
     * @param policy required approval policy identifier
     * @param rule gray selector required only for GRAY publication
     * @param previous previous application manifests, or null for an empty baseline
     * @param selected complete replacement manifests for selected applications
     * @param references shared-reference policies to freeze into the plan
     * @throws IllegalArgumentException if version, publication mode, selector, selected manifests, or reference policies are invalid
     */
    public Plan(String tenant, String baseline, String version, String mode, String policy, GrayRule rule,
            Map<String, Map<String, String>> previous, Map<String, Map<String, String>> selected,
            List<Reference> references) {
        ItemNormalizer.requireTenant(tenant);
        require(version, "Server-generated version");
        // Naming belongs to the host allocator; historical date versions remain valid identifiers.
        if (!PatternKit.isMatch("v[0-9]+(?:\\.[0-9]+)+", version)) {
            throw new IllegalArgumentException("Release version must be a server-generated numeric identifier");
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

    /**
     * Validates and deeply freezes an application revision manifest.
     *
     * @param input mutable application manifest
     * @return immutable, deterministically ordered manifest
     * @throws IllegalArgumentException if an application manifest is null or has invalid coordinates
     */
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

    /**
     * Requires a nonblank manifest value.
     *
     * @param value value to validate
     * @param name semantic name used in the failure message
     * @throws IllegalArgumentException if a required coordinate is null or blank
     */
    private static void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    /**
     * Encodes a length-prefixed value for an unambiguous composite key.
     *
     * @param value value to encode
     * @return length-prefixed segment
     */
    private static String segment(String value) {
        return value.length() + ":" + value;
    }

    /**
     * Rebuilds a frozen plan after restart and rejects corrupted or re-serialized approval inputs.
     *
     * @param rollout persisted rollout carrying the canonical manifest and checksum
     * @return reconstructed plan verified against the rollout
     * @throws IllegalArgumentException if the stored manifest, checksum, or reconstructed plan is invalid
     */
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

    /**
     * Serializes the canonical frozen input used for approval and execution checksums.
     * @return canonical JSON representation of all frozen plan inputs
     */
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

    /**
     * Returns the tenant that owns this plan.
     *
     * @return tenant identifier
     */
    public String getTenant_id() {
        return tenant;
    }

    /**
     * Returns the expected baseline version.
     *
     * @return baseline version
     */
    public String getBaseline() {
        return baseline;
    }

    /**
     * Returns the target version allocated for this plan.
     *
     * @return target version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the checksum of the canonical frozen plan.
     *
     * @return canonical plan checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Returns the complete immutable application manifest.
     *
     * @return complete immutable application manifest
     */
    public Map<String, Map<String, String>> getApplications() {
        return applications;
    }

    /**
     * Returns the immutable manifests of explicitly selected applications.
     *
     * @return immutable selected-application manifest
     */
    public Map<String, Map<String, String>> getSelected() {
        return selected;
    }

    /**
     * Rejects mutation, stale approval, or a publication under another tenant.
     *
     * @param rollout rollout whose tenant, manifest, policy and checksum must match this plan
     * @throws IllegalArgumentException if the rollout no longer matches this frozen plan
     */
    public void verify(Rollout rollout) {
        if (rollout == null || !tenant.equals(rollout.getTenant_id()) || !checksum.equals(rollout.getChecksum())
                || !toJson().equals(rollout.getManifest()) || !mode.equals(rollout.getMode())
                || !policy.equals(rollout.getPolicy()) || !Objects.equals(rule, rollout.getRule())) {
            throw new IllegalArgumentException("Publication no longer matches its frozen plan");
        }
    }

    /**
     * Freezes only an editable draft; the host persists this result with an edition compare-and-set.
     *
     * @param rollout tenant-owned draft to populate with the frozen plan and mark REVIEWING
     * @throws IllegalArgumentException if the rollout is not a draft owned by this tenant
     */
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

    /**
     * Tests membership in the selected scope rather than the merged baseline.
     * @param app selected application identifier
     * @param item item identifier within that application
     * @param revision exact immutable revision identifier
     * @return true when the selected application explicitly contains that item revision
     */
    public boolean includes(String app, String item, String revision) {
        return app != null && revision != null && revision.equals(selected.getOrDefault(app, Map.of()).get(item));
    }

}
