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
package org.miaixz.bus.auth.nimble.qq;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.miaixz.bus.auth.nimble.AuthorizeScope;

/**
 * QQ authorization scopes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@AllArgsConstructor
public enum QqScope implements AuthorizeScope {

    /**
     * Retrieves the user's nickname, avatar, and gender.
     */
    GET_USER_INFO("get_user_info", "Retrieves the user's nickname, avatar, and gender", true),
    /**
     * Retrieves basic QQ VIP information.
     */
    GET_VIP_INFO("get_vip_info", "Retrieves basic QQ VIP information", false),
    /**
     * Retrieves advanced QQ VIP information.
     */
    GET_VIP_RICH_INFO("get_vip_rich_info", "Retrieves advanced QQ VIP information", false),
    /**
     * Retrieves the user's QQ Zone album list.
     */
    LIST_ALBUM("list_album", "Retrieves the user's QQ Zone album list", false),
    /**
     * Uploads a photo to the QQ Zone album.
     */
    UPLOAD_PIC("upload_pic", "Uploads a photo to the QQ Zone album", false),
    /**
     * Creates a new personal album in the user's QQ Zone.
     */
    ADD_ALBUM("add_album", "Creates a new personal album in the user's QQ Zone", false),
    /**
     * Retrieves the photo list in the user's QQ Zone album.
     */
    LIST_PHOTO("list_photo", "Retrieves the photo list in the user's QQ Zone album", false);

    /**
     * The scope string as defined by QQ.
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
