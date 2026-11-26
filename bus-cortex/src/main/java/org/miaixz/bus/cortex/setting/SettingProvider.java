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
package org.miaixz.bus.cortex.setting;

import org.miaixz.bus.cortex.Instance;

/**
 * Supplies invocation coordinates and current process data without authenticating callers or evaluating permissions.
 *
 * @author Kimi Liu
 */
public interface SettingProvider {

    /**
     * Returns the host invocation coordinates; these coordinates are not authentication evidence.
     *
     * @return current operation scope
     */
    Context current();

    /**
     * Reads the current process for this invocation and application. The host owns process creation, renewal,
     * replacement and retirement. Each call, including a subscription callback, must read current session data; a
     * retired, missing, expired or replaced bound process must be rejected rather than returned as null or as a cached
     * active snapshot. The host authenticates callers and authorizes access to the requested application.
     *
     * @param context invocation coordinates
     * @param app     requested application
     * @return current process, or null only for an invocation without a bound process
     */
    Instance instance(Context context, String app);

    /**
     * Operation coordinates, independent of a login or credential model. Tenant is required for data partitioning;
     * actor and request are required only by operations that record decisions or idempotent writes.
     *
     * @param tenant  data partition
     * @param actor   optional audit actor
     * @param request optional idempotency coordinate
     * @author Kimi Liu
     */
    record Context(String tenant, String actor, String request) {

        /**
         * Requires only the data partition; operation-specific methods validate audit and receipt coordinates.
         *
         * @param tenant  data partition
         * @param actor   optional audit actor
         * @param request optional idempotency coordinate
         */
        public Context {
            if (tenant == null || tenant.isBlank())
                throw new IllegalArgumentException("Setting tenant required");
        }

        /**
         * Requires an explicit request coordinate for an idempotent mutation without authenticating its origin.
         */
        public void requireRequest() {
            if (request == null || request.isBlank())
                throw new IllegalArgumentException("Setting request required");
        }

        /**
         * Requires an actor identifier for auditing a flow stage decision. The caller verifies the actor's identity.
         */
        public void requireActor() {
            if (actor == null || actor.isBlank())
                throw new IllegalArgumentException("Setting actor required");
        }

    }

}
