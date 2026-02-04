/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import java.util.Arrays;

/**
 * Enumerates the possible source channels of a request, allowing for channel-specific logic and authorization.
 * <p>
 * This enum is used by the {@link QualifierStrategy} to identify the client type (e.g., Web, App) and can be used to
 * apply different validation rules or business logic based on the request's origin.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Channel {

    /**
     * Indicates a request initiated from a standard web browser or web page.
     */
    WEB("1", 0),

    /**
     * Indicates a request initiated from a native mobile application (iOS, Android).
     */
    APP("2", 1),

    /**
     * Indicates a request initiated from the DingTalk platform (e.g., a mini-app or bot).
     */
    DINGTALK("3", 1),

    /**
     * Indicates a request initiated from the WeChat platform (e.g., a mini-program or official account).
     */
    WECHAT("4", 1),

    /**
     * A fallback value for requests from an unknown or unspecified channel.
     */
    OTHER("5", 0);

    /**
     * The raw string value representing the channel, typically sent in a request header like {@code x_remote_channel}.
     */
    private String value;

    /**
     * A type identifier used to select different token handling or authorization logic for this channel. For example, a
     * value of {@code 1} might signify a channel that uses a specific type of OAuth token.
     */
    private Integer type;

    /**
     * Safely retrieves a {@code Channel} enum instance from its string representation.
     * <p>
     * This method searches for a matching enum constant by its {@code value}. If no match is found, it returns
     * {@link #OTHER} as a safe default, preventing null pointer exceptions.
     *
     * @param value The string value of the channel (e.g., "1", "2").
     * @return The corresponding {@link Channel} instance, or {@link #OTHER} if the value is invalid or null.
     */
    public static Channel get(String value) {
        return Arrays.stream(Channel.values()).filter(c -> c.getValue().equals(value)).findFirst()
                .orElse(Channel.OTHER);
    }

}
