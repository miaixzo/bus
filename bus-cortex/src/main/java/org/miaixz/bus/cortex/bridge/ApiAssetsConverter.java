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
package org.miaixz.bus.cortex.bridge;

import java.util.Objects;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.registry.RegistryAssets;
import org.miaixz.bus.cortex.registry.api.ApiAssets;

/**
 * Converts cortex API definitions to gateway assets.
 *
 * @author Kimi Liu
 */
public class ApiAssetsConverter {

    /**
     * Creates a new ApiAssetsConverter.
     */
    public ApiAssetsConverter() {
        // No initialization required.
    }

    /**
     * Converts a service definition and runtime instance into a gateway Assets object.
     *
     * @param service  API service definition
     * @param instance shared runtime process associated with the service
     * @return populated Assets ready for gateway registration
     */
    public static Assets convert(ApiAssets service, Instance instance) {
        if (service == null || instance == null || !Objects.equals(service.getApp_id(), instance.getApp_id())) {
            throw new IllegalArgumentException("Instance application does not match the API definition");
        }
        Assets asset = RegistryAssets.copy(service);
        asset.setId(instance.getChecksum());
        asset.setHost(instance.getHost());
        asset.setPort(instance.getPort());
        asset.setWeight(instance.getWeight());
        return asset;
    }

}
