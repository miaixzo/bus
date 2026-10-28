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
package org.miaixz.bus.cortex.setting.delivery;

import java.util.*;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.EnumValue.Format;
import org.miaixz.bus.cortex.Curator;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Keying.SettingSpec;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.release.Release;
import org.miaixz.bus.cortex.release.ReleaseCodec;
import org.miaixz.bus.cortex.release.ReleaseStore;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.curator.ItemEditor;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.item.ItemMerger;
import org.miaixz.bus.cortex.setting.item.ItemQuery;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.cortex.setting.revision.Revision;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.rollout.GrayRuleMatcher;
import org.miaixz.bus.cortex.setting.rollout.Plan;
import org.miaixz.bus.cortex.setting.watch.Watch;
import org.miaixz.bus.cortex.setting.watch.WatchStore;

/**
 * Published-only reads: exact Release graph plus exact immutable snapshots, never current Item/Reference/workflow
 * stores.
 *
 * @author Kimi Liu
 */
public final class ItemQueryService implements Curator {

    /**
     * Formal release authority.
     */
    private final ReleaseStore releases;
    /**
     * Immutable published snapshots.
     */
    private final RevisionStore revisions;
    /**
     * Protected content handling.
     */
    private final ItemEditor editor;
    /**
     * Sole definition parser/renderer.
     */
    private final ItemCodec codec;
    /**
     * Current-process gray selector.
     */
    private final GrayRuleMatcher matcher;
    /**
     * Durable subscription evidence.
     */
    private final WatchStore watches;
    /**
     * Notification transport, not a publication authority.
     */
    private final WatchManager transport;
    /**
     * Invocation and current process data, independent of authentication.
     */
    private final SettingProvider provider;
    /**
     * Optional host access policy, independent of invocation and process data.
     */
    private final CortexGuard guard;

    /**
     * Creates governed delivery.
     *
     * @param releases  formal versions
     * @param revisions exact snapshots
     * @param editor    protection
     * @param codec     parser
     * @param matcher   gray selection
     * @param watches   durable subscriptions
     * @param transport notifications
     * @param provider  invocation and current process data
     * @param guard     optional host access policy
     */
    public ItemQueryService(ReleaseStore releases, RevisionStore revisions, ItemEditor editor, ItemCodec codec,
            GrayRuleMatcher matcher, WatchStore watches, WatchManager transport, SettingProvider provider,
            CortexGuard guard) {
        this.releases = Objects.requireNonNull(releases);
        this.revisions = Objects.requireNonNull(revisions);
        this.editor = Objects.requireNonNull(editor);
        this.codec = Objects.requireNonNull(codec);
        this.matcher = Objects.requireNonNull(matcher);
        this.watches = Objects.requireNonNull(watches);
        this.transport = Objects.requireNonNull(transport);
        this.provider = Objects.requireNonNull(provider);
        this.guard = guard;
    }

    /**
     * Reads exact published content.
     *
     * @param query application-qualified request
     * @return encoded content or genuinely absent
     */
    @Override
    public String get(ItemQuery query) {
        return get(provider.current(), query);
    }

    /**
     * Reads published content for explicit operation coordinates without requiring an ambient login context.
     *
     * @param context operation coordinates
     * @param query   application-qualified request
     * @return encoded published content, or null when absent
     */
    public String get(SettingProvider.Context context, ItemQuery query) {
        return read(context, List.of(query), null).get(key(query));
    }

    /**
     * Groups lookups by selected formal release and loads each exact snapshot set once.
     *
     * @param queries application-qualified requests
     * @return application-qualified values, including null absence
     */
    @Override
    public Map<String, String> batchGet(List<ItemQuery> queries) {
        return read(provider.current(), queries, null);
    }

    /**
     * Applies fallback only to genuine absence.
     *
     * @param query    application request
     * @param fallback caller fallback
     * @return published content or fallback
     */
    @Override
    public String getOrDefault(ItemQuery query, String fallback) {
        String value = get(query);
        return value == null ? fallback : value;
    }

    /**
     * Registers a published-change subscription; callback rechecks the captured operation coordinates. Durable evidence
     * records the source Item ID resolved from the selected publication, while transport retains the requested alias.
     * An absent published source retains a transport-only subscription until notifications can resolve its content.
     *
     * @param query    application request
     * @param listener value callback
     * @return exact transport subscription
     */
    @Override
    public String watch(ItemQuery query, ConsumerX<String> listener) {
        SettingProvider.Context context = provider.current();
        validate(context, query);
        context.check(guard, "subscribe", "app", query.getApp_id());
        Objects.requireNonNull(listener);
        ItemQuery captured = ItemQuery.builder().tenant_id(query.getTenant_id()).app_id(query.getApp_id())
                .id(query.getId()).code(query.getCode()).requestId(query.getRequestId()).build();
        Instance instance = instance(context, captured.getApp_id());
        String itemId = null;
        if (instance != null) {
            Release release = select(
                    releases.current(context.tenant(), EnumValue.Track.STABLE.key()),
                    releases.current(context.tenant(), EnumValue.Track.CANARY.key()),
                    instance);
            if (release != null) {
                Loaded state = load(context, release);
                ReleaseCodec.Binding binding = binding(state, captured);
                if (binding != null)
                    itemId = state.snapshots().get(binding.source()).getItem_id();
            }
        }
        Instance expected = instance == null ? null
                : Instance.builder().id(instance.getId()).health(instance.getHealth()).build();
        String id = transport.add(
                Vector.builder().tenant_id(context.tenant()).app_id(captured.getApp_id()).id(key(captured))
                        .type(Type.ITEM.key()).build(),
                event -> listener.accept(read(context, List.of(captured), expected).get(key(captured))));
        try {
            if (instance != null && itemId != null)
                watches.save(
                        Watch.builder().tenant_id(context.tenant()).id(id).instance_id(instance.getId()).item_id(itemId)
                                .session(instance.healthView().getSession()).build());
            return id;
        } catch (RuntimeException failed) {
            transport.remove(id);
            throw failed;
        }
    }

    /**
     * Removes only an tenant-qualified exact subscription.
     *
     * @param watch_id transport identity
     */
    @Override
    public void unwatch(String watch_id) {
        SettingProvider.Context context = provider.current();
        context.check(guard, "unsubscribe", "watch", watch_id);
        var entry = transport.entries().get(watch_id);
        if (entry != null && !context.tenant().equals(entry.getTenant_id()))
            throw new IllegalArgumentException("Watch tenant mismatch");
        if (watches.find(context.tenant(), watch_id) != null)
            watches.delete(context.tenant(), watch_id);
        transport.remove(watch_id);
    }

    /**
     * Resolves a batch with invocation-local parsed state only.
     *
     * @param context  captured operation coordinates
     * @param queries  requested aliases
     * @param expected original process session for a bound subscription, or null for an ordinary read
     * @return qualified values
     */
    private Map<String, String> read(SettingProvider.Context context, List<ItemQuery> queries, Instance expected) {
        if (queries.size() > 1000)
            throw new IllegalArgumentException("Delivery batch too large");
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, Release> selected = new HashMap<>();
        Map<String, Loaded> loaded = new HashMap<>();
        Release stable = null, canary = null;
        boolean tracks = false;
        for (ItemQuery query : queries) {
            validate(context, query);
            if (!selected.containsKey(query.getApp_id())) {
                Instance runtime = instance(context, query.getApp_id());
                if (expected != null && (runtime == null || !expected.getId().equals(runtime.getId())
                        || !expected.healthView().getSession().equals(runtime.healthView().getSession())))
                    throw new IllegalStateException("Subscription process session changed");
                if (!tracks) {
                    stable = releases.current(context.tenant(), EnumValue.Track.STABLE.key());
                    canary = releases.current(context.tenant(), EnumValue.Track.CANARY.key());
                    tracks = true;
                }
                selected.put(query.getApp_id(), select(stable, canary, runtime));
            }
            Release release = selected.get(query.getApp_id());
            String value = null;
            if (release != null) {
                Loaded state = loaded.computeIfAbsent(release.getId(), ignored -> load(context, release));
                ReleaseCodec.Binding binding = binding(state, query);
                if (binding != null) {
                    Revision snapshot = state.snapshots().get(binding.source());
                    context.check(guard, "deliver", "item", snapshot.getItem_id());
                    if (Integer.valueOf(1).equals(snapshot.getEncrypted()))
                        context.check(guard, "deliver-secret", "item", snapshot.getItem_id());
                    ItemCodec.Document parsed = state.definitions().computeIfAbsent(
                            snapshot.getId(),
                            ignored -> editor.open(ReleaseCodec.definition(snapshot).item(context.tenant())));
                    ReferenceCodec.Options options = binding.options();
                    ReferenceCodec.Options clear = new ReferenceCodec.Options(options.type(), options.alias(),
                            options.prefix(), options.override(), options.keys(),
                            editor.openOverlay(options.overlay()));
                    ItemMerger.Result merged = ItemMerger.merge(
                            List.of(new ItemMerger.Source(snapshot.getId(), parsed)),
                            List.of(clear),
                            64,
                            100000);
                    value = codec.format(merged.value(), Format.valueOf(snapshot.getFormat()));
                }
            }
            String key = key(query);
            if (result.containsKey(key))
                throw new IllegalArgumentException("Duplicate delivery request identity");
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Reads current process coordinates for selection and subscription evidence. This checks data scope and the current
     * heartbeat lease; credentials and principal-to-process access belong to the host guard. The provider must not
     * return retired sessions as active process data.
     *
     * @param context invocation coordinates
     * @param app     requested application
     * @return current process, or null for a process-free read
     */
    private Instance instance(SettingProvider.Context context, String app) {
        Instance value = provider.instance(context, app);
        if (value != null && (!context.tenant().equals(value.getTenant_id()) || !app.equals(value.getApp_id())
                || value.getId() == null || value.getId().isBlank() || !value.isOnline(System.currentTimeMillis())))
            throw new IllegalStateException("Current process coordinates mismatch");
        return value;
    }

    /**
     * Selects the same published track for reads and subscription identity resolution.
     *
     * @param stable   current stable release or absent
     * @param canary   current gray release or absent
     * @param instance current process or a process-free reader
     * @return selected formal release or absent
     */
    private Release select(Release stable, Release canary, Instance instance) {
        if (canary != null) {
            ReleaseCodec.Document gray = ReleaseCodec.decode(canary);
            if (gray.mode() != Plan.Mode.GRAY)
                throw new IllegalStateException("Canary release mode mismatch");
            if (instance != null && matcher.matches(gray.rule(), instance))
                return canary;
        }
        return stable;
    }

    /**
     * Resolves an exact source ID or application-local alias against the verified published graph.
     *
     * @param state verified graph and immutable snapshots
     * @param query application-qualified source request
     * @return exact published usage or absent
     */
    private ReleaseCodec.Binding binding(Loaded state, ItemQuery query) {
        ReleaseCodec.Binding binding = null;
        for (ReleaseCodec.Binding candidate : state.document().applications()
                .getOrDefault(query.getApp_id(), List.of())) {
            Revision snapshot = state.snapshots().get(candidate.source());
            String alias = candidate.options().alias().isEmpty() ? snapshot.getCode() : candidate.options().alias();
            if (query.getId() != null ? query.getId().equals(snapshot.getItem_id())
                    : Objects.equals(query.getCode(), alias)) {
                if (binding != null)
                    throw new IllegalArgumentException("Ambiguous published source; use an alias");
                binding = candidate;
            }
        }
        return binding;
    }

    /**
     * Loads/verifies the exact complete immutable graph once.
     *
     * @param context operation coordinates
     * @param release selected formal record
     * @return invocation-local source set
     */
    private Loaded load(SettingProvider.Context context, Release release) {
        if (!context.tenant().equals(release.getTenant_id()) || !ReleaseStore.isServable(release.getStatus()))
            throw new IllegalStateException("Release not servable");
        ReleaseCodec.Document document = ReleaseCodec.decode(release);
        Set<String> ids = new TreeSet<>();
        document.applications().values().forEach(list -> list.forEach(b -> ids.add(b.source())));
        Map<String, Revision> snapshots = new TreeMap<>();
        for (Revision revision : revisions.findAll(context.tenant(), ids))
            if (snapshots.put(revision.getId(), revision) != null)
                throw new IllegalStateException("Duplicate published snapshot");
        return new Loaded(ReleaseCodec.verify(release, snapshots), Map.copyOf(snapshots), new HashMap<>());
    }

    /**
     * Rejects management/foreign request coordinates before any lookup.
     *
     * @param context operation coordinates
     * @param query   application request
     */
    private void validate(SettingProvider.Context context, ItemQuery query) {
        if (query == null || !context.tenant().equals(query.getTenant_id()) || query.getApp_id() == null
                || query.getApp_id().isBlank() || query.isIncludeDeleted() || query.getOffset() != 0
                || (query.getId() == null || query.getId().isBlank())
                        && (query.getCode() == null || query.getCode().isBlank()))
            throw new IllegalArgumentException("Exact runtime application query required");
        context.check(guard, "deliver", "app", query.getApp_id());
    }

    /**
     * Uses existing application-qualified identity encoding.
     *
     * @param query exact request
     * @return qualified key
     */
    private String key(ItemQuery query) {
        return SettingGenerator.INSTANCE.key(SettingSpec.export(query));
    }

    /**
     * Invocation-local parsed formal state; never cached globally.
     *
     * @param document    verified graph
     * @param snapshots   exact snapshots
     * @param definitions parsed protected definitions
     * @author Kimi Liu
     */
    private record Loaded(ReleaseCodec.Document document, Map<String, Revision> snapshots,
            Map<String, ItemCodec.Document> definitions) {

    }

}
