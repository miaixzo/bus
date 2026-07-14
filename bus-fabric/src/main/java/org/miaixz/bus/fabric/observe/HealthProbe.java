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
package org.miaixz.bus.fabric.observe;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.network.Connection;

/**
 * Lightweight health probe contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface HealthProbe {

    /**
     * Checks target health from local state.
     *
     * @param target target
     * @return true when healthy
     */
    default boolean healthy(final Object target) {
        final Object checkedTarget = Assert
                .notNull(target, () -> new ValidateException("Health target must not be null"));
        try {
            if (checkedTarget instanceof Connection connection) {
                return connection.healthy();
            }
            if (checkedTarget instanceof Session session) {
                return session.opened();
            }
            return false;
        } catch (final SocketException | InternalException | StatefulException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to check target health", e);
        }
    }

    /**
     * Returns probe name.
     *
     * @return probe name
     */
    default String name() {
        return "health";
    }

}
