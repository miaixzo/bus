/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;

import java.io.Serial;
import java.io.Serializable;

/**
 * This class is used by the ProtectedBranchesAPi to set up the allowed_to_push, allowed_to_merge, and
 * allowed_to_unprotect values.
 */
public class AllowedTo implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852252997683L;

    private AccessLevel accessLevel;
    private Long userId;
    private Long groupId;

    public AllowedTo(AccessLevel accessLevel, Long userId, Long groupId) {
        this.accessLevel = accessLevel;
        this.userId = userId;
        this.groupId = groupId;
    }

    public AllowedTo withAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        return (this);
    }

    public AllowedTo withUserId(Long userId) {
        this.userId = userId;
        return (this);
    }

    public AllowedTo withGroupId(Long groupId) {
        this.groupId = groupId;
        return (this);
    }

    public GitLabForm getForm(GitLabForm form, String allowedToName) {

        if (form == null) {
            form = new GitLabForm();
        }

        return (form.withParam(allowedToName + "[][access_level]", accessLevel)
                .withParam(allowedToName + "[][user_id]", userId).withParam(allowedToName + "[][group_id]", groupId));
    }

}
