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
package org.miaixz.bus.auth.magic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.core.lang.Gender;

import java.io.Serializable;

/**
 * User information after successful authorization. The completeness of the data obtained varies depending on the
 * authorization platform.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Claims implements Serializable {

    /**
     * The unique ID of the user in the third-party system. When integrating this component, the user can be uniquely
     * identified by combining uuid + source.
     */
    private String uuid;
    /**
     * User's username.
     */
    private String username;
    /**
     * User's nickname.
     */
    private String nickname;
    /**
     * User's avatar URL.
     */
    private String avatar;
    /**
     * User's blog or personal website URL.
     */
    private String blog;
    /**
     * User's company.
     */
    private String company;
    /**
     * User's location.
     */
    private String location;
    /**
     * User's email address.
     */
    private String email;
    /**
     * User's remarks (personal introduction from various platforms).
     */
    private String remark;
    /**
     * User's gender.
     */
    private Gender gender;
    /**
     * User's source platform.
     */
    private String source;
    /**
     * User's authorization token information.
     */
    private Authorization token;
    /**
     * Raw user information returned by the third-party platform.
     */
    private String rawJson;

    /**
     * WeChat Official Account - available for web authorization login. WeChat adds a snapshot page logic for web
     * authorization login, where the uid, oid, avatar, and nickname obtained are virtual information.
     */
    private boolean snapshotUser;

}
