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
package org.miaixz.bus.vortex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * Enumerates different request channels, defining various request sources and their attributes.
 * <p>
 * This enum class identifies the source channel of a request (e.g., WEB, APP, DingTalk, WeChat) and associates a string
 * value and a token type with each channel. Each enum value initializes its properties through the constructor and
 * provides a static method {@link #get(String)} to retrieve the corresponding enum instance based on its string value.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Channel {

    /**
     * WEB request, indicating a request initiated via a browser or web page.
     */
    WEB("1", 0),

    /**
     * APP request, indicating a request initiated via a mobile application.
     */
    APP("2", 1),

    /**
     * DingTalk request, indicating a request initiated via the DingTalk platform.
     */
    DINGTALK("3", 1),

    /**
     * WeChat request, indicating a request initiated via the WeChat platform.
     */
    WECHAT("4", 1),

    /**
     * Other request, indicating a request that cannot be categorized into a specific channel, serving as a default
     * fallback.
     */
    OTHER("5", 0);

    /**
     * The string value of the channel, used to uniquely identify the channel.
     */
    private String value;

    /**
     * The token type, used to distinguish token handling methods for different channels.
     * <p>
     * A value of 0 indicates no special token handling is required, while a value of 1 indicates specific token
     * handling logic is needed.
     * </p>
     */
    private Integer type;

    /**
     * Retrieves the corresponding channel enum instance based on the channel value.
     * <p>
     * This method searches for a matching enum instance using the given string value. If no matching channel is found,
     * it returns {@link #OTHER} as the default value.
     * </p>
     *
     * @param value The string value of the channel.
     * @return The matching {@link Channel} enum instance, or {@link #OTHER} if no match is found.
     */
    public static Channel get(String value) {
        return Arrays.stream(Channel.values()).filter(c -> c.getValue().equals(value)).findFirst()
                .orElse(Channel.OTHER);
    }

}
