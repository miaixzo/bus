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
package org.miaixz.bus.auth.source.vendor.gitlab;

import java.util.List;

import org.miaixz.bus.auth.source.vendor.Vendor;
import org.miaixz.bus.auth.source.vendor.VendorConnector;
import org.miaixz.bus.auth.source.vendor.VendorRegistry;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Connects the complete GitLab platform manifest and its exact variant factories.
 *
 * @author Kimi Liu
 */
public class GitLabConnector implements VendorConnector {

    /**
     * Creates a stateless GitLab SPI connector.
     */
    public GitLabConnector() {
        // No initialization required.
    }

    /**
     * Returns the stable GitLab platform key.
     *
     * @return stable GitLab platform key
     */
    @Override
    public Vendor.Id key() {
        return GitLabManifest.ID;
    }

    /**
     * Binds the GitLab manifest, Options factory, and all adapter factories as one registration.
     *
     * @param registry active Vendor registry
     */
    @Override
    public void connect(final VendorRegistry registry) {
        Assert.notNull(registry, "Vendor registry must not be null").bind(
                new GitLabManifest(),
                (variant, clientId, credential, callback, scopes, parameters) -> new GitLabOptions(variant.platform(),
                        variant.variant(), clientId, credential, callback, scopes,
                        VendorRegistry.string(parameters, "instance", Normal.EMPTY),
                        VendorRegistry.string(parameters, "topLevelGroup", Normal.EMPTY)),
                List.of(
                        VendorRegistry.adapter(GitLabManifest.class, GitLabSourceAdapter::new, GitLabManifest.DEFAULT),
                        VendorRegistry
                                .adapter(GitLabManifest.class, GitLabRealmAdapter::new, GitLabManifest.ENTERPRISE)));
    }

}
