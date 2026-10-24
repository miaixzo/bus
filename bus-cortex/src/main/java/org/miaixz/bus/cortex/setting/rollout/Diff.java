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

import org.miaixz.bus.cortex.release.Manifest;
import org.miaixz.bus.cortex.release.Release;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.crypto.Builder;

/**
 * Pure published-versus-frozen graph comparison; no per-save history and no plaintext diff cache.
 *
 * @author Kimi Liu
 */
public final class Diff {

    /**
     * Prevents construction.
     */
    private Diff() {
        // No initialization required.
    }

    /**
     * Compares exact approved candidate against its actual formal baseline.
     *
     * @param candidate frozen input
     * @param baseline  exact baseline, absent for first publish
     * @param published baseline graph
     * @param snapshots exact baseline snapshots
     * @return safe summary
     */
    public static Result compare(
            Plan.Frozen candidate,
            Release baseline,
            Manifest.Document published,
            Map<String, Revision> snapshots) {
        if (!Objects.equals(candidate.baseline(), baseline == null ? null : baseline.getId()))
            throw new IllegalArgumentException("Diff baseline mismatch");
        if (baseline != null) {
            Manifest.Document verified = Manifest.verify(baseline, snapshots);
            if (!Manifest.encode(verified).equals(Manifest.encode(published)))
                throw new IllegalStateException("Diff manifest mismatch");
        } else if (published != null || !snapshots.isEmpty())
            throw new IllegalArgumentException("Initial diff cannot have historical state");
        Map<String, Map<String, String>> before = new TreeMap<>(), after = new TreeMap<>();
        if (published != null)
            published.applications().forEach((app, bindings) -> {
                Map<String, String> values = new TreeMap<>();
                for (Manifest.Binding binding : bindings) {
                    Plan.Definition source = Manifest.definition(snapshots.get(binding.source()));
                    String alias = binding.options().alias().isEmpty() ? source.code() : binding.options().alias();
                    if (values.put(alias, digest(source.key(), binding.options())) != null)
                        throw new IllegalStateException("Duplicate baseline alias");
                }
                before.put(app, values);
            });
        candidate.applications().forEach((app, bindings) -> {
            Map<String, String> values = new TreeMap<>();
            for (Plan.Binding binding : bindings) {
                Plan.Definition source = candidate.definitions().get(binding.source());
                String alias = binding.options().alias().isEmpty() ? source.code() : binding.options().alias();
                values.put(alias, digest(source.key(), binding.options()));
            }
            after.put(app, values);
        });
        Set<String> apps = new TreeSet<>(before.keySet());
        apps.addAll(after.keySet());
        List<Change> changes = new ArrayList<>();
        int added = 0, modified = 0, deleted = 0;
        for (String app : apps) {
            Map<String, String> old = before.getOrDefault(app, Map.of()), next = after.getOrDefault(app, Map.of());
            Set<String> aliases = new TreeSet<>(old.keySet());
            aliases.addAll(next.keySet());
            for (String alias : aliases)
                if (!Objects.equals(old.get(alias), next.get(alias))) {
                    Kind kind;
                    if (!old.containsKey(alias)) {
                        kind = Kind.ADDED;
                        added++;
                    } else if (!next.containsKey(alias)) {
                        kind = Kind.DELETED;
                        deleted++;
                    } else {
                        kind = Kind.MODIFIED;
                        modified++;
                    }
                    changes.add(new Change(app, alias, kind, old.get(alias), next.get(alias)));
                }
        }
        return new Result(changes, added, modified, deleted);
    }

    /**
     * Digests frozen source and usage without revealing values.
     *
     * @param source  source digest
     * @param options usage
     * @return semantic binding digest
     */
    private static String digest(String source, ReferenceCodec.Options options) {
        return Builder.sha256(source + ItemCodec.encode(ReferenceCodec.tree(options)));
    }

    /**
     * Configuration graph change.
     *
     * @author Kimi Liu
     */
    public enum Kind {
        /**
         * Newly included configuration.
         */
        ADDED,
        /**
         * Changed content or usage.
         */
        MODIFIED,
        /**
         * Removed configuration.
         */
        DELETED

    }

    /**
     * Safe application-local change coordinates.
     *
     * @param app    application
     * @param alias  logical config name
     * @param kind   change type
     * @param before prior digest
     * @param after  candidate digest
     * @author Kimi Liu
     */
    public record Change(String app, String alias, Kind kind, String before, String after) {

    }

    /**
     * Summary with safe changes.
     *
     * @param changes  changed bindings
     * @param added    new count
     * @param modified changed count
     * @param deleted  removed count
     * @author Kimi Liu
     */
    public record Result(List<Change> changes, int added, int modified, int deleted) {

        /**
         * Freezes safe comparison output.
         *
         * @param changes  safe changes
         * @param added    new count
         * @param modified changed count
         * @param deleted  removed count
         */
        public Result {
            changes = List.copyOf(changes);
        }

    }

}
