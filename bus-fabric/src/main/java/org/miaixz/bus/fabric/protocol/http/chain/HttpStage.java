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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Internal HTTP chain stage contract.
 *
 * <p>
 * This contract is used by the built-in HTTP runtime stages. User code should configure protocol behavior through the
 * public builder hooks instead of installing HTTP stages directly.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface HttpStage {

    /**
     * Returns the stage name.
     *
     * @return trimmed lowercase simple class name, or {@code http-stage} for an unnamed implementation
     */
    default String name() {
        final String rawName = getClass().getSimpleName();
        return normalizeName(StringKit.isBlank(rawName) ? "http-stage" : rawName);
    }

    /**
     * Executes this stage.
     *
     * @param request request presented to this stage
     * @param chain   continuation used to invoke the remaining stages
     * @return response produced by this stage or the remaining chain
     */
    HttpResponse execute(HttpRequest request, HttpChain chain);

    /**
     * Returns whether this stage touches the network.
     *
     * @return true when the stage performs network I/O; false by default
     */
    default boolean network() {
        return false;
    }

    /**
     * Normalizes a stage name.
     *
     * @param name non-blank, single-line stage name
     * @return trimmed stage name converted to lowercase with the root locale
     */
    private static String normalizeName(final String name) {
        Assert.isFalse(
                StringKit.isBlank(name) || StringKit.containsAny(name, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("HTTP stage name must be non-blank and single-line"));
        return StringKit.trim(name).toLowerCase(Locale.ROOT);
    }

}
