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
package org.miaixz.bus.vortex.provider;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.magic.Delegate;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.magic.Principal;

/**
 * Interface for access authorization providers, defining methods for authentication and authorization operations.
 * Implementations of this interface handle validation of credentials such as tokens, API keys, or licenses, and return
 * the result of the authorization process.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface AuthorizeProvider {

    /**
     * Validates the provided principal and performs the authorization process. This method checks the principal's type
     * and delegates to the appropriate authorization method (e.g., token, API key, or license). If the principal is
     * invalid or the type is unsupported, an appropriate error is returned.
     *
     * @param principal The {@link Principal} object containing authentication details, such as a token, API key, or
     *                  license.
     * @return A {@link Delegate} object encapsulating the authorization result, including a {@link Message} with status
     *         information and an optional {@link Authorize} object with authorization details.
     * @throws ValidateException If the principal is null or empty, with error code {@link ErrorCode#_100806}.
     */
    default Delegate authorize(Principal principal) {
        // Validate the principal parameter
        if (ObjectKit.isEmpty(principal)) {
            Logger.warn("Authorization failed: empty principal entity");
            throw new ValidateException(ErrorCode._100806);
        }

        // Delegate to specific authorization method based on principal type
        if (Consts.ONE.equals(principal.getType())) {
            return this.token(principal);
        } else if (Consts.TWO.equals(principal.getType())) {
            return this.apiKey(principal);
        } else if (Consts.THREE.equals(principal.getType())) {
            return this.license(principal);
        }

        // Handle unsupported authorization type
        Logger.warn("Unsupported authorization type: {}", principal.getType());
        return Delegate.builder().message(
                Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build())
                .build();
    }

    /**
     * Validates a token-based principal and returns the authorization result. This method is invoked when the principal
     * type is {@link Consts#ONE}. The default implementation returns a successful authorization result with an empty
     * {@link Authorize} object. Implementations should override this method to provide specific token validation logic.
     *
     * <p>
     * 1. By default, all returns are successful 2. In actual business, please override this method according to
     * business rules
     *
     * @param principal The {@link Principal} object containing token-based authentication details.
     * @return A {@link Delegate} object containing the authorization result, including a {@link Message} indicating
     *         success or failure and an optional {@link Authorize} object with authorization details.
     */
    default Delegate token(Principal principal) {
        return Delegate.builder().message(
                Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build())
                .authorize(Authorize.builder().build()).build();
    }

    /**
     * Validates an API key-based principal and returns the authorization result. This method is invoked when the
     * principal type is {@link Consts#TWO}. The default implementation returns a successful authorization result with
     * an empty {@link Authorize} object. Implementations should override this method to provide specific API key
     * validation logic.
     *
     * <p>
     * 1. By default, all returns are successful 2. In actual business, please override this method according to
     * business rules
     *
     * @param principal The {@link Principal} object containing API key-based authentication details.
     * @return A {@link Delegate} object containing the authorization result, including a {@link Message} indicating
     *         success or failure and an optional {@link Authorize} object with authorization details.
     */
    default Delegate apiKey(Principal principal) {
        return Delegate.builder().message(
                Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build())
                .authorize(Authorize.builder().build()).build();
    }

    /**
     * Validates a license-based principal and returns the authorization result. This method is invoked when the
     * principal type is {@link Consts#THREE}. The default implementation returns a successful authorization result with
     * an empty {@link Authorize} object. Implementations should override this method to provide specific license
     * validation logic.
     *
     * <p>
     * 1. By default, all returns are successful 2. In actual business, please override this method according to
     * business rules
     *
     * @param principal The {@link Principal} object containing license-based authentication details.
     * @return A {@link Delegate} object containing the authorization result, including a {@link Message} indicating
     *         success or failure and an optional {@link Authorize} object with authorization details.
     */
    default Delegate license(Principal principal) {
        return Delegate.builder().message(
                Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build())
                .authorize(Authorize.builder().build()).build();
    }

}
