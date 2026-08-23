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
package org.miaixz.bus.auth.source.vendor.github;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;

/**
 * Connects the complete GitHub platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class GitHubConnector implements VendorConnector {

    /**
     * Creates a stateless GitHub SPI connector.
     */
    public GitHubConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable GitHub platform key.
     *
     * @return stable GitHub platform key
     */
    @Override
    public Vendor.Id key() {
        return GitHubManifest.ID;
    }

    /**
     * Binds the GitHub manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new GitHubManifest(),
                VendorRegistry.options(GitHubOptions::new),
                List.of(
                        VendorRegistry.adapter(GitHubManifest.class, GitHubSourceAdapter::new, GitHubManifest.DEFAULT),
                        VendorRegistry
                                .adapter(GitHubManifest.class, GitHubRealmAdapter::new, GitHubManifest.ENTERPRISE)));
    }

}
