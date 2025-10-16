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
        // Validate the principal object itself.
        if (ObjectKit.isEmpty(principal)) {
            Logger.warn("Authorization failed: The principal entity is null or empty.");
            throw new ValidateException(ErrorCode._100806);
        }

        // Default workflow: Dispatch based on credential type.
        if (Consts.ONE.equals(principal.getType())) {
            return this.token(principal);
        }
        if (Consts.TWO.equals(principal.getType())) {
            return this.apiKey(principal);
        }

        // The default implementation does not handle other types (e.g., type '3' for licenses).
        // To support them, the user must override this `authorize` method.
        Logger.warn(
                "==>     Provider: Unsupported or unhandled principal type: {}. "
                        + "Override the 'authorize' method in your provider to handle custom types.",
                principal.getType());
        return Delegate.builder()
                .message(
                        Message.builder().errcode(ErrorCode._100802.getKey())
                                .errmsg("Unsupported credential type: " + principal.getType()).build())
                .build();
    }

    /**
     * Validates a token-based principal. Users should override this method to implement their specific token validation
     * logic (e.g., JWT parsing, database lookup, Redis cache check).
     * <p>
     * The default implementation always returns a successful result, effectively "skipping" the validation. This is
     * useful for deployments that do not use token-based authentication.
     *
     * @param principal The {@link Principal} object containing the token in its {@code key} field.
     * @return A {@link Delegate} object containing the authorization result.
     */
    default Delegate token(Principal principal) {
        Logger.debug("Executing default `token` method. Returning success without validation.");
        return Delegate.builder().message(
                Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build())
                .authorize(Authorize.builder().build()).build();
    }

    /**
     * Validates an API key-based principal. Users should override this method to implement their specific API key
     * validation logic (e.g., database lookup).
     * <p>
     * The default implementation always returns a successful result, effectively "skipping" the validation.
     *
     * @param principal The {@link Principal} object containing the API key in its {@code key} field.
     * @return A {@link Delegate} object containing the authorization result.
     */
    default Delegate apiKey(Principal principal) {
        Logger.debug("Executing default `apiKey` method. Returning success without validation.");
        return Delegate.builder().message(
                Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build())
                .authorize(Authorize.builder().build()).build();
    }

}
