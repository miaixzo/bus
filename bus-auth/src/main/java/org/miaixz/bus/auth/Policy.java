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
package org.miaixz.bus.auth;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;

/**
 * Immutable authentication policy that contributes typed Fabric options.
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface Policy extends org.miaixz.bus.fabric.Policy {

    /**
     * Applies this authentication policy to an immutable option snapshot.
     *
     * @param options source options
     * @return updated immutable options
     */
    @Override
    Options from(Options options);

    /**
     * Composes this policy with a policy applied after it.
     *
     * @param next policy applied to this policy's result
     * @return composed immutable policy
     * @throws ValidateException if {@code next} is null
     */
    default Policy and(final Policy next) {
        final Policy checked = Assert.notNull(next, () -> new ValidateException("Policy must not be null"));
        return options -> checked.from(from(options));
    }

}
