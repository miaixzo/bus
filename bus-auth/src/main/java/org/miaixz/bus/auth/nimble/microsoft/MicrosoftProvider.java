/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.nimble.microsoft;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Registry;
import org.miaixz.bus.auth.magic.ErrorCode;

/**
 * Microsoft login provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MicrosoftProvider extends AbstractMicrosoftProvider {

    /**
     * Constructs a {@code MicrosoftProvider} with the specified context.
     *
     * @param context the authentication context
     */
    public MicrosoftProvider(Context context) {
        super(context, Registry.MICROSOFT);
    }

    /**
     * Constructs a {@code MicrosoftProvider} with the specified context and cache.
     *
     * @param context the authentication context
     * @param cache   the cache implementation
     */
    public MicrosoftProvider(Context context, CacheX cache) {
        super(context, Registry.MICROSOFT, cache);
    }

    /**
     * Checks the completeness and validity of the context configuration for Microsoft authentication. Specifically, it
     * ensures that the redirect URI uses HTTPS or is a localhost address.
     *
     * @param context the authentication context
     * @throws AuthorizedException if the redirect URI is invalid
     */
    @Override
    protected void validate(Context context) {
        super.validate(context);
        // Microsoft's redirect uri must use the HTTPS or localhost
        if (Registry.MICROSOFT == this.complex && !Protocol.isHttpsOrLocalHost(context.getRedirectUri())) {
            throw new AuthorizedException(ErrorCode._110005.getKey(), this.complex);
        }
    }

}
