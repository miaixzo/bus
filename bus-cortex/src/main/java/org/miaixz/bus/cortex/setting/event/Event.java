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

import java.io.Serial;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Value-free authenticated operation audit and exact idempotency receipt.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Event extends Tenant {

    @Serial
    private static final long serialVersionUID = 2536998889000L;

    /**
     * Identifier of the authenticated actor performing the operation.
     */
    private String actor_id;

    /**
     * Exact identifier of the operation target.
     */
    private String resource_id;

    /**
     * Resource category of the operation target.
     */
    private String resource;

    /**
     * Name of the audited operation.
     */
    private String action;

    /**
     * Request identifier used for idempotency.
     */
    private String request;

    /**
     * Checksum binding the operation identity and authorized inputs.
     */
    private String checksum;

    /**
     * Audit severity level.
     */
    private String level;

    /**
     * Operation outcome or exact result identifier; successful publication records the release identifier.
     */
    private String result;

    /**
     * Outcome description containing no sensitive values.
     */
    private String message;

    /**
     * Action-specific audit metadata containing no configuration values.
     */
    @Lob
    private String detail;

    /**
     * Operation occurrence time in epoch milliseconds.
     */
    private Long occurred;

    /**
     * Service receipt time in epoch milliseconds.
     */
    private Long received;

    /**
     * Creates an empty operation audit event.
     */
    public Event() {
        // No initialization required.
    }

}
