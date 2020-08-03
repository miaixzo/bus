/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org Greg Messner and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.GitLabApiForm;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
public class ApprovalRuleParams {

    private String name;
    private Integer approvalsRequired;
    private List<Integer> userIds;
    private List<Integer> groupIds;

    public ApprovalRuleParams withName(String name) {
        this.name = name;
        return (this);
    }

    public ApprovalRuleParams withApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
        return (this);
    }

    public ApprovalRuleParams withUserIds(List<Integer> userIds) {
        this.userIds = userIds;
        return (this);
    }

    public ApprovalRuleParams withGroupIds(List<Integer> groupIds) {
        this.groupIds = groupIds;
        return (this);
    }

    /**
     * Get the form params specified by this instance.
     *
     * @return a GitLabApiForm instance holding the form parameters for this ApprovalRuleParams instance
     */
    public GitLabApiForm getForm() {
        return new GitLabApiForm()
                .withParam("name", name)
                .withParam("approvals_required", approvalsRequired, true)
                .withParam("user_ids", userIds)
                .withParam("group_ids", groupIds);
    }
}
