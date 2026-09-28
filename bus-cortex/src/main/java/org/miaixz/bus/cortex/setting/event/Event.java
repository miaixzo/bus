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
package org.miaixz.bus.cortex.setting.event;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.logger.Level;

/**
 * Immutable service-center event record.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Event extends Tenant {

    /**
     * Actor that produced the event.
     */
    private String actor_id;
    /**
     * Notification target identifier.
     */
    private String target_id;
    /**
     * Related runtime instance.
     */
    private String instance_id;
    /**
     * Related watch.
     */
    private String watch_id;
    /**
     * Related rollout.
     */
    private String rollout_id;
    /**
     * Related asynchronous task.
     */
    private String task_id;
    /**
     * Related setting item.
     */
    private String item_id;
    /**
     * Related immutable revision.
     */
    private String revision_id;
    /**
     * Primary affected resource.
     */
    private String resource_id;
    /**
     * Event category.
     */
    private String category;
    /**
     * Event action.
     */
    private String action;
    /**
     * Runtime session.
     */
    private String session;
    /**
     * Runtime generation.
     */
    private Long generation;
    /**
     * Idempotency request identifier.
     */
    private String request;
    /**
     * Stable event fingerprint.
     */
    private String fingerprint;
    /**
     * Affected resource type.
     */
    private String resource;
    /**
     * Event severity level.
     */
    private Level level;
    /**
     * Source occurrence time.
     */
    private Long occurred;
    /**
     * Server receipt time.
     */
    private Long received;
    /**
     * Event result.
     */
    private String result;
    /**
     * Human-readable message.
     */
    private String message;
    /**
     * Structured event detail stored as text.
     */
    @Lob
    private String detail;

    /**
     * Creates an empty event record.
     */
    public Event() {
        // No initialization required.
    }

}
