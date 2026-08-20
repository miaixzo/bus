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
package org.miaixz.bus.auth.vendor;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.ExecutionServices;

/**
 * Defines the executable contract implemented by one compiled third-party platform variant.
 * <p>
 * Implementations expose only capabilities declared by their immutable variant definition. Standard OAuth and OpenID
 * Connect operations retain the request and response types defined by their protocol packages; this contract does not
 * introduce replacement token, callback, refresh, revocation, or user-information operations.
 * </p>
 *
 * @author Kimi Liu
 */
public interface VendorAdapter {

    /**
     * Returns the exact immutable capabilities implemented by this adapter.
     *
     * @return adapter capability manifest
     */
    Capability.Manifest manifest();

    /**
     * Executes one declared strongly typed capability within the caller's existing time budget.
     *
     * @param capability capability declared by this adapter
     * @param request    exact request accepted by the capability
     * @param context    current non-secret invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        capability request type
     * @param <S>        capability success type
     * @return asynchronous framework outcome
     */
    <Q, S> CompletionStage<Outcome<S>> invoke(
            Capability<Q, S> capability,
            Q request,
            Context context,
            Timeout.Budget timeout);

    /**
     * Creates the exact adapter paired with one platform variant during immutable Registry compilation.
     *
     * @param <S> exact platform settings type accepted by the paired Vendor definition
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Factory<S extends VendorSettings> {

        /**
         * Creates one Source-isolated platform adapter from validated immutable inputs.
         *
         * @param namespaceId       namespace identifier copied from the Source registration
         * @param sourceId          Source identifier copied from the Source registration
         * @param vendorDefinition  exact Vendor definition
         * @param variantDefinition exact selected platform variant definition
         * @param settings          decoded immutable platform settings
         * @param services          complete externally supplied runtime dependency set
         * @return non-null adapter whose manifest equals the definition manifest
         */
        VendorAdapter create(
                String namespaceId,
                String sourceId,
                VendorDefinition<S> vendorDefinition,
                VendorDefinition.Definition variantDefinition,
                S settings,
                ExecutionServices services);

    }

}
