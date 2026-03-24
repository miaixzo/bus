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

import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.registry.api.ApiDefinition;

/**
 * Converts cortex API definitions to gateway assets.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ApiAssetsConverter {

    /**
     * Creates a new ApiAssetsConverter.
     */
    private ApiAssetsConverter() {

    }

    /**
     * Converts a service definition and runtime instance into a gateway Assets object.
     *
     * @param service  API service definition
     * @param instance runtime instance associated with the service
     * @return populated Assets ready for gateway registration
     */
    public static Assets convert(ApiDefinition service, Instance instance) {
        Assets asset = new Assets();
        asset.setId(instance.getFingerprint());
        asset.setName(service.getName());
        asset.setIcon(service.getIcon());
        asset.setHost(instance.getHost());
        asset.setPort(instance.getPort());
        asset.setPath(service.getPath());
        asset.setUrl(service.getUrl());
        asset.setMethod(service.getMethod());
        asset.setMode(service.getMode());
        asset.setStream(service.getStream());
        asset.setType(service.getType());
        asset.setPolicy(service.getPolicy());
        asset.setSign(service.getSign());
        asset.setScope(service.getScope());
        asset.setRetries(service.getRetries());
        asset.setTimeout(service.getTimeout());
        asset.setThrottle(service.getThrottle());
        asset.setBalance(service.getBalance());
        asset.setWeight(instance.getWeight());
        asset.setSort(service.getSort());
        asset.setMock(service.getMock());
        asset.setResult(service.getResult());
        asset.setVersion(service.getVersion());
        asset.setMetadata(service.getMetadata());
        asset.setDescription(service.getDescription());
        return asset;
    }

}
