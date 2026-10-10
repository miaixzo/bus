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
package org.miaixz.bus.cortex.setting.task;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Durable asynchronous service-center task.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Task extends Tenant {

    /**
     * Parent task identifier.
     */
    private String parent_id;
    /**
     * Related rollout identifier.
     */
    private String rollout_id;
    /**
     * Related revision identifier.
     */
    private String revision_id;
    /**
     * Related watch identifier.
     */
    private String watch_id;
    /**
     * Actor that initiated the task.
     */
    private String actor_id;
    /**
     * Task kind.
     */
    private String kind;
    /**
     * Runtime session identifier.
     */
    private String session;
    /**
     * Runtime generation.
     */
    private Long generation;
    /**
     * Task state.
     */
    private String state;
    /**
     * Idempotency request identifier.
     */
    private String request;
    /**
     * Semantic request digest.
     */
    private String digest;
    /**
     * Task input stored as text.
     */
    @Lob
    private String payload;
    /**
     * Task result stored as text.
     */
    @Lob
    private String result;
    /**
     * Number of execution attempts.
     */
    private Integer attempts;
    /**
     * Earliest execution time.
     */
    private Long scheduled;
    /**
     * Current worker claim token.
     */
    private String claim;
    /**
     * Claim deadline.
     */
    private Long deadline;
    /**
     * Task expiration time.
     */
    private Long expires;

    /**
     * Creates an empty asynchronous task.
     */
    public Task() {
        // No initialization required.
    }

}
