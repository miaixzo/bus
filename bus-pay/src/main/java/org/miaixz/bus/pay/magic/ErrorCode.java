/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.pay.magic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用的状态码对照表
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 2000：正常； other：调用异常，具体异常内容见{@code msg}
     */
    SUCCESS("2000", "Success"), FAILURE("5000", "Failure"), NOT_IMPLEMENTED("5001", "Not Implemented"),
    PARAMETER_INCOMPLETE("5002", "Parameter incomplete"), UNSUPPORTED("5003", "Unsupported operation"),
    NO_AUTH_SOURCE("5004", "Registry cannot be null"), UNIDENTIFIED_PLATFORM("5005", "Unidentified platform"),
    ILLEGAL_REDIRECT_URI("5006", "Illegal redirect uri"), ILLEGAL_REQUEST("5007", "Illegal provider"),
    ILLEGAL_CODE("5008", "Illegal code"), ILLEGAL_STATUS("5009", "Illegal state"),
    REQUIRED_REFRESH_TOKEN("5010", "The refresh token is required; it must not be null"),
    ILLEGAL_TOKEN("5011", "Invalid token");

    private final String code;
    private final String desc;

}
