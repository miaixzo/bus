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

import java.util.List;

import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.setting.approval.Approval;
import org.miaixz.bus.cortex.setting.rollout.Plan;

/**
 * Mandatory host authentication and authorization; no permissive defaults.
 *
 * @author Kimi Liu
 */
public interface SettingEnforcer {

    /**
     * Returns the authenticated invocation identity, never request-supplied authorization.
     *
     * @return trusted context
     */
    Context current();

    /**
     * Loads and verifies the current authoritative instance for the authenticated invocation. The host derives the
     * instance identifier and claimed session from trusted authentication evidence, reads its current entity and
     * verifies the tenant, requested application, exact session and active heartbeat lease before returning it.
     * Request-supplied instance fields and previously authenticated snapshots are not authoritative.
     * <p>
     * Each call must recheck the current session, including calls made by subscription callbacks with a captured
     * context. An absent, retired, replaced or expired instance session must be rejected rather than treated as a
     * non-instance reader. Instance creation, renewal, replacement and deletion remain host responsibilities.
     *
     * @param context authenticated identity, including when captured for a subscription
     * @param app     authorized application
     * @return the current verified instance, or {@code null} only for an authorized non-instance reader
     * @throws SecurityException if the instance identity, tenant, application, session or lease is invalid
     */
    Instance instance(Context context, String app);

    /**
     * Requires permission for the exact tenant resource.
     *
     * @param context  trusted identity
     * @param action   requested operation
     * @param resource domain type
     * @param id       exact resource
     */
    void require(Context context, String action, String resource, String id);

    /**
     * Rejects unregistered approval policies.
     *
     * @param context trusted identity
     * @param policy  registered policy
     */
    void requirePolicy(Context context, String policy);

    /**
     * Evaluates the existing host approval policy.
     *
     * @param context   trusted identity
     * @param plan      frozen input
     * @param decisions immutable evidence
     * @return whether required approval stages are complete
     */
    boolean approvalComplete(Context context, Plan.Frozen plan, List<Approval> decisions);

    /**
     * Authenticated invocation coordinates.
     *
     * @param tenant  authenticated tenant
     * @param actor   authenticated principal
     * @param request idempotency identity
     * @author Kimi Liu
     */
    record Context(String tenant, String actor, String request) {

        /**
         * Requires complete authenticated coordinates.
         *
         * @param tenant  tenant
         * @param actor   principal
         * @param request request identity
         */
        public Context {
            if (tenant == null || tenant.isBlank() || actor == null || actor.isBlank() || request == null
                    || request.isBlank())
                throw new IllegalArgumentException("Authenticated setting context required");
        }

    }

}
