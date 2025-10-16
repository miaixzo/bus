/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
    protected void check(Context context) {
        super.check(context);
        // Microsoft's redirect uri must use the HTTPS or localhost
        if (Registry.MICROSOFT == this.complex && !Protocol.isHttpsOrLocalHost(context.getRedirectUri())) {
            throw new AuthorizedException(ErrorCode.ILLEGAL_REDIRECT_URI.getKey(), this.complex);
        }
    }

}
