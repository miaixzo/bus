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
package org.miaixz.bus.cortex;

import java.util.List;

/**
 * Base persistent registrar contract.
 *
 * @param <T> registered asset type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Registry<T extends Assets> {

    /**
     * Registers a single entry.
     *
     * @param entry entry to register
     */
    void register(T entry);

    /**
     * Registers a runtime instance under an existing service definition.
     *
     * @param service  service definition owning the instance
     * @param instance runtime instance to register
     */
    default void register(T service, Instance instance) {
        throw new UnsupportedOperationException("Runtime registration not supported");
    }

    /**
     * Deregisters a single entry.
     *
     * @param namespace entry namespace
     * @param id        entry identifier
     */
    void deregister(String namespace, String id);

    /**
     * Deregisters a single runtime instance by fingerprint.
     *
     * @param namespace   namespace containing the instance
     * @param method      service method name
     * @param version     service version
     * @param fingerprint unique runtime instance fingerprint
     */
    default void deregisterInstance(String namespace, String method, String version, String fingerprint) {
        throw new UnsupportedOperationException("Runtime deregistration not supported");
    }

    /**
     * Queries entries matching the supplied condition.
     *
     * @param vector vector condition
     * @return matching entries
     */
    List<T> query(Vector vector);

    /**
     * Queries runtime instances for the given service identity.
     *
     * @param namespace namespace containing the service
     * @param method    service method name
     * @param version   service version
     * @return matching runtime instances
     */
    default List<Instance> queryInstances(String namespace, String method, String version) {
        return List.of();
    }

    /**
     * Subscribes to matching entry changes.
     *
     * @param vector   vector condition
     * @param listener listener to notify
     * @return watch identifier
     */
    String watch(Vector vector, Listener<T> listener);

    /**
     * Cancels a prior watch.
     *
     * @param watchId watch identifier
     */
    void unwatch(String watchId);

}
