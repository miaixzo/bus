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
package org.miaixz.bus.oauth.magic;

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
    ILLEGAL_TOKEN("5011", "Invalid token"), ILLEGAL_KID("5012", "Invalid key identifier(kid)"),
    ILLEGAL_TEAM_ID("5013", "Invalid team id"), ILLEGAL_CLIENT_ID("5014", "Invalid client id"),
    ILLEGAL_CLIENT_SECRET("5015", "Invalid client secret");

    private final String code;
    private final String desc;

    /**
     * 今日头条授权登录时的异常状态码
     */
    @Getter
    @AllArgsConstructor
    public enum Toutiao {
        /**
         * 0：正常； other：调用异常，具体异常内容见{@code desc}
         */
        EC0("0", "接口调用成功"), EC1("1", "API配置错误，未传入Client Key"), EC2("2", "API配置错误，Client Key错误，请检查是否和开放平台的ClientKey一致"),
        EC3("3", "没有授权信息"), EC4("4", "响应类型错误"), EC5("5", "授权类型错误"), EC6("6", "client_secret错误"),
        EC7("7", "authorize_code过期"), EC8("8", "指定url的scheme不是https"), EC9("9", "接口内部错误，请联系头条技术"),
        EC10("10", "access_token过期"), EC11("11", "缺少access_token"), EC12("12", "参数缺失"), EC13("13", "url错误"),
        EC21("21", "域名与登记域名不匹配"), EC999("999", "未知错误，请联系头条技术");

        private String code;
        private String desc;

        public static Toutiao getErrorCode(String errorCode) {
            Toutiao[] errorCodes = Toutiao.values();
            for (Toutiao code : errorCodes) {
                if (errorCode.equals(code.getCode())) {
                    return code;
                }
            }
            return EC999;
        }
    }

}
