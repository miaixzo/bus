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
package org.miaixz.bus.metrics.nimble;

import java.util.Objects;
import java.util.Optional;

/**
 * Stable instance-level capabilities exposed by a metrics provider.
 *
 * @param observable whether observable family registration is supported
 * @param scrape     optional text scrape capability
 * @param snapshot   optional structured snapshot capability
 * @author Kimi Liu
 */
public record ProviderCapabilities(boolean observable, Optional<ScrapeSupport> scrape,
        Optional<SnapshotSupport> snapshot) {

    /**
     * Shared capability set for providers implementing only the legacy API.
     */
    private static final ProviderCapabilities NONE = new ProviderCapabilities(false, Optional.empty(),
            Optional.empty());

    /**
     * Validates optional capability values.
     */
    public ProviderCapabilities {
        scrape = Objects.requireNonNull(scrape, "Scrape capability must not be null");
        snapshot = Objects.requireNonNull(snapshot, "Snapshot capability must not be null");
    }

    /**
     * Returns the capability set used by legacy third-party providers.
     *
     * @return no capabilities
     */
    public static ProviderCapabilities none() {
        return NONE;
    }

}
