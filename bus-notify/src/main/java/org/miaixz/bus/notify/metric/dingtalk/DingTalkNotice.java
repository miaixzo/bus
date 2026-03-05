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
package org.miaixz.bus.notify.metric.dingtalk;

import org.miaixz.bus.notify.magic.Notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Notice for DingTalk notification messages.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DingTalkNotice extends Notice {

    /**
     * The agent ID of the application.
     */
    private String agentId;
    /**
     * A comma-separated list of recipient user IDs. Maximum list length: 100.
     */
    private String userIdList;
    /**
     * A comma-separated list of recipient department IDs. Maximum list length: 20. All users within these departments
     * (including sub-departments) will receive the message.
     */
    private String deptIdList;
    /**
     * Indicates whether to send the message to all users in the enterprise. {@code true} or {@code false}.
     */
    private boolean toAllUser;
    /**
     * A comma-separated list of user IDs in the whitelist.
     */
    private String whiteList;
    /**
     * The message content in JSON string format.
     */
    private String msg;

    /**
     * The DingTalk access token.
     */
    private String token;

}
