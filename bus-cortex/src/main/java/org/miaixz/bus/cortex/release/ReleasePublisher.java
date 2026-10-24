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

import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.rollout.Rollout;
import org.miaixz.bus.cortex.setting.rollout.RolloutStore;

/**
 * Executes approved frozen configuration input through the host's atomic publication boundary.
 *
 * @author Kimi Liu
 */
public final class ReleasePublisher {

    /**
     * Sole formal publication transaction.
     */
    private final PublicationStore publications;
    /**
     * Immutable workflow and intent authority.
     */
    private final RolloutStore rollouts;
    /**
     * Mandatory authorization.
     */
    private final SettingEnforcer enforcer;
    /**
     * Exact historical release reads for rollback verification.
     */
    private final ReleaseStore releases;
    /**
     * Exact historical snapshot reads for rollback verification.
     */
    private final RevisionStore revisions;

    /**
     * Creates formal publication execution without draft-version APIs.
     *
     * @param publications final atomic commit
     * @param rollouts     workflow intents
     * @param enforcer     permissions
     * @param releases     formal releases
     * @param revisions    exact snapshots
     */
    public ReleasePublisher(PublicationStore publications, RolloutStore rollouts, SettingEnforcer enforcer,
            ReleaseStore releases, RevisionStore revisions) {
        this.publications = Objects.requireNonNull(publications);
        this.rollouts = Objects.requireNonNull(rollouts);
        this.enforcer = Objects.requireNonNull(enforcer);
        this.releases = Objects.requireNonNull(releases);
        this.revisions = Objects.requireNonNull(revisions);
    }

    /**
     * Publishes exactly approved frozen input.
     *
     * @param context trusted identity/idempotency
     * @param rollout workflow
     * @param edition expected CAS
     * @return exact committed formal release
     */
    public Release publish(SettingEnforcer.Context context, String rollout, long edition) {
        enforcer.require(context, "publish", "rollout", rollout);
        Rollout current = required(context, rollout);
        Plan.Frozen frozen = Plan.frozen(current.getManifest());
        if (!Objects.equals(current.getChecksum(), Plan.checksum(frozen)))
            throw new IllegalStateException("Approved input checksum mismatch");
        for (String app : frozen.selection().applications())
            enforcer.require(context, "publish", "app", app);
        Rollout intent = rollouts.beginPublication(context, rollout, edition, current.getChecksum());
        if (intent.getPublication() == null || !context.tenant().equals(intent.getTenant_id())
                || !rollout.equals(intent.getId()) || !Objects.equals(current.getChecksum(), intent.getChecksum())
                || !(Objects.equals(Rollout.State.PUBLISHING.key(), intent.getStatus())
                        || Objects.equals(Rollout.State.PUBLISHED.key(), intent.getStatus())))
            throw new IllegalStateException("Publication intent mismatch");
        try {
            return publications
                    .commit(context, rollout, intent.getEdition(), intent.getPublication(), intent.getChecksum());
        } catch (RuntimeException failure) {
            try {
                rollouts.failPublication(
                        context,
                        rollout,
                        intent.getPublication(),
                        "Publication outcome requires receipt reconciliation");
            } catch (RuntimeException recording) {
                failure.addSuppressed(recording);
            }
            throw failure;
        }
    }

    /**
     * Re-publishes exact historical content only through a separately approved workflow.
     *
     * @param context          trusted identity
     * @param publishedRelease exact historical release
     * @param approvedRollout  separately approved frozen input
     * @return new formal release
     */
    public Release rollback(SettingEnforcer.Context context, String publishedRelease, String approvedRollout) {
        enforcer.require(context, "rollback", "release", publishedRelease);
        Release target = releases.byId(context.tenant(), publishedRelease);
        if (target == null)
            throw new IllegalArgumentException("Published rollback target required");
        ReleaseCodec.Document document = ReleaseCodec.decode(target);
        Set<String> ids = new TreeSet<>();
        document.applications().values().forEach(list -> list.forEach(b -> ids.add(b.source())));
        Map<String, Revision> snapshots = new TreeMap<>();
        for (Revision revision : revisions.findAll(context.tenant(), ids))
            snapshots.put(revision.getId(), revision);
        ReleaseCodec.verify(target, snapshots);
        Rollout workflow = required(context, approvedRollout);
        Plan.Frozen frozen = Plan.frozen(workflow.getManifest());
        if (!frozen.applications().keySet().equals(document.applications().keySet()))
            throw new IllegalArgumentException("Approved rollback graph differs");
        for (var app : document.applications().entrySet()) {
            Set<String> expected = new TreeSet<>(), approved = new TreeSet<>();
            for (ReleaseCodec.Binding binding : app.getValue())
                expected.add(
                        ReleaseCodec.definition(snapshots.get(binding.source())).key()
                                + ItemCodec.encode(ReferenceCodec.tree(binding.options())));
            for (Plan.Binding binding : frozen.applications().get(app.getKey()))
                approved.add(binding.source() + ItemCodec.encode(ReferenceCodec.tree(binding.options())));
            if (!expected.equals(approved))
                throw new IllegalArgumentException("Approved rollback content differs");
        }
        return publish(context, approvedRollout, workflow.getEdition());
    }

    /**
     * Resolves an exact tenant-qualified workflow.
     *
     * @param context trusted identity
     * @param id      workflow
     * @return workflow
     */
    private Rollout required(SettingEnforcer.Context context, String id) {
        Rollout value = rollouts.find(context.tenant(), id);
        if (value == null || !id.equals(value.getId()) || !context.tenant().equals(value.getTenant_id()))
            throw new IllegalArgumentException("Workflow not found");
        return value;
    }

}
