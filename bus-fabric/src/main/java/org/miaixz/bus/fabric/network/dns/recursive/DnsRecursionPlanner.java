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
package org.miaixz.bus.fabric.network.dns.recursive;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.forward.DnsUpstream;
import org.miaixz.bus.fabric.network.dns.message.DnsCodec;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuery;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;

/**
 * Immutable QNAME-minimization plan builder for recursive DNS lookups.
 *
 * <p>
 * The planner does not open sockets and does not mutate resolver state. It turns a client question into a stable list
 * of delegation-discovery names that begins at root hints, retains the original question for the final client-visible
 * answer, and supports CNAME or DNAME alias continuation without losing that original question.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsRecursionPlanner {

    /**
     * Root-hint upstreams used by the first minimized step.
     */
    private final List<DnsUpstream> rootHints;

    /**
     * Creates a recursion planner.
     *
     * @param rootHints root-hint upstreams used as the first minimized step
     */
    public DnsRecursionPlanner(final List<DnsUpstream> rootHints) {
        this.rootHints = immutableRootHints(rootHints);
    }

    /**
     * Builds an immutable plan for one decoded client query.
     *
     * @param query decoded client query
     * @return immutable recursion plan
     */
    public DnsRecursionPlan plan(final DnsQuery query) {
        if (query == null) {
            throw new ValidateException("DNS recursion plan query must not be null");
        }
        return DnsRecursionPlan.create(query.question(), query.question(), rootHints, List.of());
    }

    /**
     * Returns the immutable root-hint upstreams captured by this planner.
     *
     * @return root-hint upstreams
     */
    public List<DnsUpstream> rootHints() {
        return rootHints;
    }

    /**
     * Validates and copies root-hint upstreams.
     *
     * @param rootHints source root hints
     * @return immutable root hints
     */
    private static List<DnsUpstream> immutableRootHints(final List<DnsUpstream> rootHints) {
        if (rootHints == null || rootHints.isEmpty()) {
            throw new ValidateException("DNS recursion planner root hints must not be empty");
        }
        for (final DnsUpstream rootHint : rootHints) {
            if (rootHint == null) {
                throw new ValidateException("DNS recursion planner root hints must not contain null");
            }
        }
        return List.copyOf(rootHints);
    }

    /**
     * Creates QNAME-minimized delegation names for a canonical DNS name.
     *
     * @param name active query name
     * @return immutable delegation names beginning at root
     */
    private static List<String> minimizedNames(final String name) {
        final String normalized = DnsName.normalize(name);
        final ArrayList<String> names = new ArrayList<>();
        names.add(DnsName.ROOT);
        if (DnsName.ROOT.equals(normalized)) {
            return List.copyOf(names);
        }
        final String[] labels = DnsName.labels(normalized);
        for (int index = labels.length - 1; index > 0; index--) {
            names.add(DnsName.fromLabels(labels, index, labels.length));
        }
        return List.copyOf(names);
    }

    /**
     * Synthesizes the next active query target produced by a DNAME record.
     *
     * @param activeName active query name
     * @param ownerName  DNAME owner name
     * @param targetName DNAME target suffix
     * @return synthesized CNAME target
     */
    private static String synthesizeDnameTarget(
            final String activeName,
            final String ownerName,
            final String targetName) {
        final String active = DnsName.normalize(activeName);
        final String owner = DnsName.normalize(ownerName);
        final String target = DnsName.normalize(targetName);
        if (DnsName.ROOT.equals(owner)) {
            throw new ValidateException("DNS DNAME owner must not be root");
        }
        if (active.equals(owner)) {
            return target;
        }
        if (!DnsName.descendantOf(active, owner)) {
            throw new ValidateException("DNS DNAME owner must cover the active query name");
        }
        final String prefix = active.substring(0, active.length() - owner.length());
        return DnsName.normalize(prefix + target);
    }

    /**
     * Immutable recursion plan for one original client question and one active target.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class DnsRecursionPlan {

        /**
         * Client-visible original question.
         */
        private final DnsQuestion originalQuestion;

        /**
         * Current target question after following aliases.
         */
        private final DnsQuestion activeQuestion;

        /**
         * Root-hint upstreams used by the first minimized step.
         */
        private final List<DnsUpstream> rootHints;

        /**
         * QNAME-minimized delegation-discovery steps.
         */
        private final List<DnsRecursionStep> steps;

        /**
         * Alias transitions followed before the active question.
         */
        private final List<DnsAliasStep> aliases;

        /**
         * Creates a recursion plan.
         *
         * @param originalQuestion client-visible original question
         * @param activeQuestion   current active question
         * @param rootHints        root-hint upstreams
         * @param aliases          alias transitions already followed
         */
        private DnsRecursionPlan(final DnsQuestion originalQuestion, final DnsQuestion activeQuestion,
                final List<DnsUpstream> rootHints, final List<DnsAliasStep> aliases) {
            if (originalQuestion == null) {
                throw new ValidateException("DNS recursion original question must not be null");
            }
            if (activeQuestion == null) {
                throw new ValidateException("DNS recursion active question must not be null");
            }
            this.originalQuestion = originalQuestion;
            this.activeQuestion = activeQuestion;
            this.rootHints = immutableRootHints(rootHints);
            this.steps = stepsFor(activeQuestion);
            this.aliases = immutableAliases(aliases);
        }

        /**
         * Creates a recursion plan.
         *
         * @param originalQuestion client-visible original question
         * @param activeQuestion   current active question
         * @param rootHints        root-hint upstreams
         * @param aliases          alias transitions already followed
         * @return immutable recursion plan
         */
        private static DnsRecursionPlan create(
                final DnsQuestion originalQuestion,
                final DnsQuestion activeQuestion,
                final List<DnsUpstream> rootHints,
                final List<DnsAliasStep> aliases) {
            return new DnsRecursionPlan(originalQuestion, activeQuestion, rootHints, aliases);
        }

        /**
         * Returns a new plan that follows a CNAME target.
         *
         * @param targetName CNAME target name
         * @return plan whose active question targets the CNAME destination
         */
        public DnsRecursionPlan followCname(final String targetName) {
            final DnsQuestion target = new DnsQuestion(targetName, activeQuestion.typeCode(),
                    activeQuestion.recordClass());
            final ArrayList<DnsAliasStep> nextAliases = new ArrayList<>(aliases);
            nextAliases.add(DnsAliasStep.cname(activeQuestion.name(), target.name()));
            return create(originalQuestion, target, rootHints, nextAliases);
        }

        /**
         * Returns a new plan that follows a DNAME by synthesizing the required CNAME target.
         *
         * @param ownerName  DNAME owner name
         * @param targetName DNAME target suffix
         * @return plan whose active question targets the synthesized CNAME destination
         */
        public DnsRecursionPlan followDname(final String ownerName, final String targetName) {
            final String target = synthesizeDnameTarget(activeQuestion.name(), ownerName, targetName);
            final DnsQuestion targetQuestion = new DnsQuestion(target, activeQuestion.typeCode(),
                    activeQuestion.recordClass());
            final ArrayList<DnsAliasStep> nextAliases = new ArrayList<>(aliases);
            nextAliases.add(DnsAliasStep.dname(activeQuestion.name(), targetQuestion.name()));
            return create(originalQuestion, targetQuestion, rootHints, nextAliases);
        }

        /**
         * Returns the original client-visible question.
         *
         * @return original question
         */
        public DnsQuestion originalQuestion() {
            return originalQuestion;
        }

        /**
         * Returns the active question that the resolver must currently answer.
         *
         * @return active question
         */
        public DnsQuestion activeQuestion() {
            return activeQuestion;
        }

        /**
         * Returns root-hint upstreams used by the first minimized step.
         *
         * @return immutable root hints
         */
        public List<DnsUpstream> rootHints() {
            return rootHints;
        }

        /**
         * Returns QNAME-minimized delegation-discovery steps.
         *
         * @return immutable minimized steps
         */
        public List<DnsRecursionStep> steps() {
            return steps;
        }

        /**
         * Returns alias transitions followed by this plan.
         *
         * @return immutable alias transitions
         */
        public List<DnsAliasStep> aliases() {
            return aliases;
        }

        /**
         * Returns only the minimized query names in step order.
         *
         * @return immutable minimized query names
         */
        public List<String> minimizedNames() {
            final ArrayList<String> names = new ArrayList<>(steps.size());
            for (final DnsRecursionStep step : steps) {
                names.add(step.queryName());
            }
            return List.copyOf(names);
        }

        /**
         * Creates minimized steps for one active question.
         *
         * @param question active question
         * @return immutable minimized steps
         */
        private static List<DnsRecursionStep> stepsFor(final DnsQuestion question) {
            final List<String> names = DnsRecursionPlanner.minimizedNames(question.name());
            final ArrayList<DnsRecursionStep> result = new ArrayList<>(names.size());
            for (int index = 0; index < names.size(); index++) {
                result.add(
                        new DnsRecursionStep(index, names.get(index), DnsRecordType.NS.code(), question.recordClass(),
                                index == 0));
            }
            return List.copyOf(result);
        }

        /**
         * Validates and copies alias transitions.
         *
         * @param aliases source aliases
         * @return immutable aliases
         */
        private static List<DnsAliasStep> immutableAliases(final List<DnsAliasStep> aliases) {
            if (aliases == null) {
                throw new ValidateException("DNS recursion aliases must not be null");
            }
            for (final DnsAliasStep alias : aliases) {
                if (alias == null) {
                    throw new ValidateException("DNS recursion aliases must not contain null");
                }
            }
            return List.copyOf(aliases);
        }

    }

    /**
     * Immutable QNAME-minimized delegation-discovery step.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class DnsRecursionStep {

        /**
         * Zero-based step order.
         */
        private final int index;

        /**
         * Minimized query name.
         */
        private final String queryName;

        /**
         * Numeric query type code used for delegation discovery.
         */
        private final int typeCode;

        /**
         * Numeric query class code copied from the active question.
         */
        private final int recordClass;

        /**
         * Whether this step is sent to configured root hints.
         */
        private final boolean rootHintStep;

        /**
         * Creates a minimized recursion step.
         *
         * @param index        zero-based step order
         * @param queryName    minimized query name
         * @param typeCode     numeric query type code
         * @param recordClass  numeric query class code
         * @param rootHintStep whether this step is sent to root hints
         */
        private DnsRecursionStep(final int index, final String queryName, final int typeCode, final int recordClass,
                final boolean rootHintStep) {
            if (index < 0) {
                throw new ValidateException("DNS recursion step index must be non-negative");
            }
            this.index = index;
            this.queryName = DnsName.normalize(queryName);
            this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS recursion step type");
            this.recordClass = DnsCodec.validateUnsignedShort(recordClass, "DNS recursion step class");
            this.rootHintStep = rootHintStep;
        }

        /**
         * Returns the zero-based step order.
         *
         * @return step order
         */
        public int index() {
            return index;
        }

        /**
         * Returns the minimized query name.
         *
         * @return minimized query name
         */
        public String queryName() {
            return queryName;
        }

        /**
         * Returns the numeric query type code.
         *
         * @return query type code
         */
        public int typeCode() {
            return typeCode;
        }

        /**
         * Returns the numeric query class code.
         *
         * @return query class code
         */
        public int recordClass() {
            return recordClass;
        }

        /**
         * Returns whether this step is sent to root hints.
         *
         * @return true when this is the root-hint step
         */
        public boolean rootHintStep() {
            return rootHintStep;
        }

    }

    /**
     * Immutable alias transition followed while retaining the original client question.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class DnsAliasStep {

        /**
         * Alias source owner name.
         */
        private final String sourceName;

        /**
         * Alias target owner name.
         */
        private final String targetName;

        /**
         * Alias record type code.
         */
        private final int typeCode;

        /**
         * Creates an alias transition.
         *
         * @param sourceName alias source owner name
         * @param targetName alias target owner name
         * @param typeCode   alias record type code
         */
        private DnsAliasStep(final String sourceName, final String targetName, final int typeCode) {
            this.sourceName = DnsName.normalize(sourceName);
            this.targetName = DnsName.normalize(targetName);
            this.typeCode = DnsCodec.validateUnsignedShort(typeCode, "DNS recursion alias type");
        }

        /**
         * Creates a CNAME alias transition.
         *
         * @param sourceName CNAME owner name
         * @param targetName CNAME target name
         * @return immutable alias transition
         */
        private static DnsAliasStep cname(final String sourceName, final String targetName) {
            return new DnsAliasStep(sourceName, targetName, DnsRecordType.CNAME.code());
        }

        /**
         * Creates a DNAME-synthesized CNAME alias transition.
         *
         * @param sourceName active query name before DNAME synthesis
         * @param targetName synthesized target name
         * @return immutable alias transition
         */
        private static DnsAliasStep dname(final String sourceName, final String targetName) {
            return new DnsAliasStep(sourceName, targetName, DnsRecordType.DNAME.code());
        }

        /**
         * Returns the alias source owner name.
         *
         * @return source owner name
         */
        public String sourceName() {
            return sourceName;
        }

        /**
         * Returns the alias target owner name.
         *
         * @return target owner name
         */
        public String targetName() {
            return targetName;
        }

        /**
         * Returns the alias record type code.
         *
         * @return alias record type code
         */
        public int typeCode() {
            return typeCode;
        }

    }

}
