/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.metric.pinterest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.miaixz.bus.oauth.metric.AuthorizeScope;

/**
 * Pinterest 授权范围
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum PinterestScope implements AuthorizeScope {

    /**
     * {@code scope} 含义，以{@code description} 为准
     */
    READ_PUBLIC("read_public", "Use GET method on a user’s Pins, boards.", true),
    WRITE_PUBLIC("write_public", "Use PATCH, POST and DELETE methods on a user’s Pins and boards.", false),
    READ_RELATIONSHIPS("read_relationships", "Use GET method on a user’s follows and followers (on boards, users and interests).", false),
    WRITE_RELATIONSHIPS("write_relationships", "Use PATCH, POST and DELETE methods on a user’s follows and followers (on boards, users and interests).", false);

    private final String scope;
    private final String description;
    private final boolean isDefault;

}
