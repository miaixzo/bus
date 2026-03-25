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
package org.miaixz.bus.auth.nimble.pinterest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * Pinterest authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum PinterestScope implements AuthorizeScope {

    /**
     * Use GET method on a user’s Pins, boards. The meaning of {@code scope} is subject to {@code description}.
     */
    READ_PUBLIC("read_public", "Use GET method on a user’s Pins, boards.", true),
    /**
     * Use PATCH, POST and DELETE methods on a user’s Pins and boards.
     */
    WRITE_PUBLIC("write_public", "Use PATCH, POST and DELETE methods on a user’s Pins and boards.", false),
    /**
     * Use GET method on a user’s follows and followers (on boards, users and interests).
     */
    READ_RELATIONSHIPS("read_relationships",
            "Use GET method on a user’s follows and followers (on boards, users and interests).", false),
    /**
     * Use PATCH, POST and DELETE methods on a user’s follows and followers (on boards, users and interests).
     */
    WRITE_RELATIONSHIPS("write_relationships",
            "Use PATCH, POST and DELETE methods on a user’s follows and followers (on boards, users and interests).",
            false);

    /**
     * The scope string as defined by Pinterest.
     */
    private final String scope;
    /**
     * A description of what the scope grants access to.
     */
    private final String description;
    /**
     * Indicates if this scope is enabled by default.
     */
    private final boolean isDefault;

}
