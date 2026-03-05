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
package org.miaixz.bus.vortex.magic;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Consts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A standard response wrapper for service providers, encapsulating the result of an operation like authorization.
 * <p>
 * This class acts as a container that holds either the successful result data (in the {@link #authorize} field) or an
 * error message (in the {@link #message} field). It provides a consistent return type for provider methods, simplifying
 * error handling for the caller.
 *
 * @author Kimi Liu
 * @see org.miaixz.bus.vortex.provider.AuthorizeProvider
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Delegate {

    /**
     * The message object containing the result status. On failure, it holds the error code and error message. On
     * success, it typically holds a success code (e.g., "0").
     */
    private Message message;

    /**
     * The detailed authorization information, populated only on successful authentication. This contains data about the
     * authenticated principal, such as user ID, roles, and permissions.
     */
    private Authorize authorize;

    /**
     * A convenience method to check if the operation was successful.
     *
     * @return {@code true} if the message's error code is {@link Consts#ZERO} (the convention for success),
     *         {@code false} otherwise.
     */
    public boolean isOk() {
        return message != null && Consts.ZERO.toString().equals(message.getErrcode());
    }

}
