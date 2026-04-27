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
package org.miaixz.bus.cortex.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;

/**
 * Converters between the public {@link Vector} compatibility model and registry-specific internal scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RegistryScopeMapping {

    /**
     * Prevents utility class instantiation.
     */
    private RegistryScopeMapping() {

    }

    /**
     * Converts a public vector into a registry query scope.
     *
     * @param vector       public compatibility vector
     * @param fallbackType managed registry type
     * @return registry query
     */
    public static RegistryQuery query(Vector vector, Type fallbackType) {
        Vector source = vector == null ? new Vector() : vector;
        RegistryQuery query = new RegistryQuery();
        query.setNamespace_id(RegistryIdentity.namespace(source.getNamespace_id()));
        query.setType(resolveRegistryType(source.getType(), fallbackType));
        query.setId(source.getId());
        query.setApp_id(source.getApp_id());
        query.setMethod(source.getMethod());
        query.setVersion(source.getVersion());
        query.setLabels(source.getLabels() == null ? null : new LinkedHashMap<>(source.getLabels()));
        query.setSelectors(source.getSelectors() == null ? null : new ArrayList<>(source.getSelectors()));
        query.setState(source.getState());
        query.setIncludeDisabled(source.isIncludeDisabled());
        query.setOffset(Math.max(source.getOffset(), 0));
        query.setLimit(source.getLimit() > 0 ? source.getLimit() : 100);
        return query;
    }

    /**
     * Converts a public vector into a registry watch scope.
     *
     * @param vector       public compatibility vector
     * @param fallbackType managed registry type
     * @return registry watch scope
     */
    public static RegistryWatchScope watch(Vector vector, Type fallbackType) {
        RegistryWatchScope scope = new RegistryWatchScope();
        scope.setQuery(query(vector, fallbackType));
        scope.setRequestId(vector == null ? null : vector.getRequestId());
        return scope;
    }

    /**
     * Converts a public vector into a registry refresh scope.
     *
     * @param vector       public compatibility vector
     * @param fallbackType managed registry type
     * @param refreshMode  logical refresh mode
     * @return registry refresh scope
     */
    public static RegistryRefreshScope refresh(Vector vector, Type fallbackType, String refreshMode) {
        Vector source = vector == null ? new Vector() : vector;
        RegistryRefreshScope scope = new RegistryRefreshScope();
        scope.setNamespace_id(RegistryIdentity.namespace(source.getNamespace_id()));
        scope.setType(resolveRegistryType(source.getType(), fallbackType));
        scope.setId(source.getId());
        scope.setRefreshMode(refreshMode);
        scope.setRequestId(source.getRequestId());
        scope.setIncludeDisabled(source.isIncludeDisabled());
        return scope;
    }

    /**
     * Converts a registry query back to the legacy vector shape for existing store implementations.
     *
     * @param query registry query
     * @return compatibility vector
     */
    public static Vector toVector(RegistryQuery query) {
        Vector vector = new Vector();
        if (query == null) {
            vector.setNamespace_id(RegistryIdentity.namespace(null));
            return vector;
        }
        vector.setNamespace_id(RegistryIdentity.namespace(query.getNamespace_id()));
        vector.setType(query.getType() == null ? null : query.getType().key());
        vector.setId(query.getId());
        vector.setApp_id(query.getApp_id());
        vector.setMethod(query.getMethod());
        vector.setVersion(query.getVersion());
        vector.setLabels(query.getLabels() == null ? null : new LinkedHashMap<>(query.getLabels()));
        vector.setSelectors(query.getSelectors() == null ? null : new ArrayList<>(query.getSelectors()));
        vector.setState(query.getState());
        vector.setIncludeDisabled(query.isIncludeDisabled());
        vector.setOffset(Math.max(query.getOffset(), 0));
        vector.setLimit(query.getLimit() > 0 ? query.getLimit() : 100);
        return vector;
    }

    /**
     * Converts a registry refresh scope back to the legacy vector shape.
     *
     * @param scope registry refresh scope
     * @return compatibility vector
     */
    public static Vector toVector(RegistryRefreshScope scope) {
        Vector vector = new Vector();
        if (scope == null) {
            vector.setNamespace_id(RegistryIdentity.namespace(null));
            return vector;
        }
        vector.setNamespace_id(RegistryIdentity.namespace(scope.getNamespace_id()));
        vector.setType(scope.getType() == null ? null : scope.getType().key());
        vector.setId(scope.getId());
        vector.setPurpose(scope.getRefreshMode());
        vector.setRequestId(scope.getRequestId());
        vector.setIncludeDisabled(scope.isIncludeDisabled());
        return vector;
    }

    /**
     * Converts a registry watch scope back to the legacy vector shape.
     *
     * @param scope registry watch scope
     * @return compatibility vector
     */
    public static Vector toVector(RegistryWatchScope scope) {
        Vector vector = toVector(scope == null ? null : scope.getQuery());
        vector.setWatch(true);
        if (scope != null) {
            vector.setRequestId(scope.getRequestId());
        }
        return vector;
    }

    /**
     * Resolves and bounds a raw registry type to the managed fallback type.
     *
     * @param rawType      raw numeric type key
     * @param fallbackType managed registry type
     * @return resolved registry type
     */
    private static Type resolveRegistryType(Integer rawType, Type fallbackType) {
        Type fallback = fallbackType == null ? Type.API : Type.requireRegistry(fallbackType);
        if (rawType == null) {
            return fallback;
        }
        Type requested = Type.requireRegistryKey(rawType);
        return requested.is(fallback) ? requested : fallback;
    }

}
