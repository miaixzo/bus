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
package org.miaixz.bus.auth.source.vendor;

import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.SourceWorker;

/**
 * Defines the executable contract implemented by one compiled third-party platform {@link VendorManifest.Variant}.
 * <p>
 * An adapter executes capabilities only. It does not own platform metadata, variant routing, deployment options,
 * factory registration, or Source compilation. Standard OAuth and OpenID Connect operations retain the request and
 * response types defined by their protocol packages; this contract does not introduce replacement protocol models.
 * </p>
 *
 * @author Kimi Liu
 */
public interface VendorAdapter extends SourceWorker {

    /**
     * Creates the exact adapter paired with one platform variant during immutable Source compilation.
     *
     * @param <O> exact deployment options type accepted by the paired {@link VendorManifest}
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Factory<O extends VendorOptions<?>> {

        /**
         * Creates one Source-isolated platform adapter from validated immutable inputs.
         *
         * @param spaceId  space identifier copied from the Source configuration
         * @param sourceId Source identifier copied from the Source configuration
         * @param manifest exact platform manifest
         * @param variant  exact variant selected from that manifest
         * @param options  validated immutable deployment options
         * @param services complete externally supplied runtime dependency set
         * @return non-null adapter whose capability manifest equals the selected variant's capability manifest
         */
        VendorAdapter create(
                String spaceId,
                String sourceId,
                VendorManifest<O> manifest,
                VendorManifest.Variant variant,
                O options,
                DriverServices services);

    }

}
