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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Persistent rollout attributes shared by configuration-center implementations.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Rollout extends Tenant {

    /**
     * Persisted rollout mode from Mode.
     */
    private String mode;

    /**
     * Persisted workflow state from State, distinct from the inherited record status.
     */
    private String state;

    /**
     * Human-readable rollout title.
     */
    private String title;

    /**
     * Human-readable rollout description.
     */
    private String description;

    /**
     * Approval policy identifier or serialized workflow, frozen on submission.
     */
    private String policy;

    /**
     * Digest of the frozen application selection, exact revisions, approval policy, mode, and gray rule.
     */
    private String checksum;

    /** Canonical immutable manifest containing selected scope, baseline, exact revisions, and reference policies. */
    private String manifest;

    /**
     * Creator-scoped idempotency key.
     */
    private String request;

    /**
     * Digest of the original semantic command.
     */
    private String digest;

    /**
     * Active gray selector JSON when present.
     */
    private String rule;

    /**
     * Optimistic locking edition for every change to the rollout, including workflow transitions.
     */
    private Long edition;

    /**
     * Idempotency key of the latest publication attempt, separate from draft creation.
     */
    private String publication;

    /**
     * Authenticated actor who requested the latest publication attempt.
     */
    private String publisher;

    /**
     * Redacted failure code of the latest publication attempt.
     */
    private String error;

    /**
     * Terminal completion timestamp when present.
     */
    private Long finished;

    /**
     * Creates an empty rollout entity.
     */
    public Rollout() {
        // No initialization required.
    }

    /** Supported publication modes. */
    public enum Mode {
        /** Publishes to all instances within the selected application scope. */
        FORMAL,
        /** Publishes to instances matching the frozen gray selector. */
        GRAY
    }

    /** Supported rollout workflow states. */
    public enum State {

        /** Editable publication plan. */
        DRAFT,
        /** Frozen plan awaiting approval decisions. */
        REVIEWING,
        /** Approved plan awaiting execution. */
        APPROVED,
        /** Plan rejected during approval. */
        REJECTED,
        /** Frozen publication awaits or is undergoing atomic activation; recoverable after restart. */
        PUBLISHING,
        /** Publication failed. */
        FAILED,
        /** Publication completed and the version is active. */
        PUBLISHED;

        /**
         * Tests whether the workflow permits a transition, including an edition-guarded update within one state.
         *
         * @param target requested workflow state
         * @return whether the transition is supported
         */
        public boolean canTransitionTo(State target) {
            if (target == this) {
                return true;
            }
            return switch (this) {
                case DRAFT -> target == REVIEWING;
                case REVIEWING -> target == APPROVED || target == REJECTED;
                case APPROVED, FAILED -> target == PUBLISHING;
                case PUBLISHING -> target == FAILED || target == PUBLISHED;
                case REJECTED, PUBLISHED -> false;
            };
        }
    }

}
