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
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Material implements Serializable {

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
    private AuthToken token;
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
