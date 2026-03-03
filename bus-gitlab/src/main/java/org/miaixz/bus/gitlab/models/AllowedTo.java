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
    private static final long serialVersionUID = 2852235756710L;

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
