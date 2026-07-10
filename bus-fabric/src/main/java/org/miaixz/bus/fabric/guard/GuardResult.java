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
package org.miaixz.bus.fabric.guard;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Guard check result.
 *
 * @param passed whether the guard passed
 * @param reason rejection reason
 * @author Kimi Liu
 * @since Java 21+
 */
public record GuardResult(boolean passed, String reason) {

    /**
     * Creates a guard result.
     *
     * @param passed whether the guard passed
     * @param reason rejection reason
     */
    public GuardResult {
        if (passed) {
            reason = null;
        } else {
            reason = validateReason(reason);
        }
    }

    /**
     * Returns a passed result.
     *
     * @return passed result
     */
    public static GuardResult pass() {
        return Instances.get(GuardResult.class.getName() + ".pass", () -> new GuardResult(true, null));
    }

    /**
     * Returns a rejected result.
     *
     * @param reason rejection reason
     * @return rejected result
     */
    public static GuardResult reject(final String reason) {
        return new GuardResult(false, reason);
    }

    /**
     * Throws when this result is rejected.
     */
    public void throwIfRejected() {
        if (!passed) {
            throw new ValidateException(reason);
        }
    }

    /**
     * Validates rejection reason.
     *
     * @param reason rejection reason
     * @return validated reason
     */
    private static String validateReason(final String reason) {
        if (StringKit.isBlank(reason) || StringKit.containsAny(reason, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Guard rejection reason must be non-blank and single-line");
        }
        return reason;
    }

}
