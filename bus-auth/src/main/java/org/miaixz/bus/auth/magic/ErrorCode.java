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

import org.miaixz.bus.core.basic.normal.ErrorRegistry;
import org.miaixz.bus.core.basic.normal.Errors;

/**
 * 授权错误码: 110xxx
 *
 * @author Kimi Liu
 * @since Java 17+
 */

public class ErrorCode extends org.miaixz.bus.core.basic.normal.ErrorCode {

    /**
     * 通知操作未实现。
     */
    public static final Errors _NOT_IMPLEMENTED = ErrorRegistry.builder().key("5001").value("未实现").build();

    /**
     * 通知参数不完整。
     */
    public static final Errors PARAMETER_INCOMPLETE = ErrorRegistry.builder().key("5002").value("参数不完整").build();

    /**
     * 通知注册中心不能为空。
     */
    public static final Errors NO_AUTH_SOURCE = ErrorRegistry.builder().key("5004").value("注册中心不能为空").build();

    /**
     * 无法识别的通知平台。
     */
    public static final Errors UNIDENTIFIED_PLATFORM = ErrorRegistry.builder().key("5005").value("无法识别的平台").build();

    /**
     * 非法通知重定向URI。
     */
    public static final Errors ILLEGAL_REDIRECT_URI = ErrorRegistry.builder().key("5006").value("非法的重定向URI").build();

    /**
     * 非法通知提供者。
     */
    public static final Errors ILLEGAL_REQUEST = ErrorRegistry.builder().key("5007").value("非法的提供者").build();

    /**
     * 非法通知代码。
     */
    public static final Errors ILLEGAL_CODE = ErrorRegistry.builder().key("5008").value("非法的代码").build();

    /**
     * 非法通知状态。
     */
    public static final Errors ILLEGAL_STATUS = ErrorRegistry.builder().key("5009").value("非法状态").build();

    /**
     * 通知刷新令牌为必需，且不能为空。
     */
    public static final Errors REQUIRED_REFRESH_TOKEN = ErrorRegistry.builder().key("5010").value("刷新令牌为必需，且不能为空")
            .build();

    /**
     * 无效的通知令牌。
     */
    public static final Errors ILLEGAL_TOKEN = ErrorRegistry.builder().key("5011").value("无效的令牌").build();

    /**
     * 无效的通知密钥标识符（kid）。
     */
    public static final Errors ILLEGAL_KID = ErrorRegistry.builder().key("5012").value("无效的密钥标识符(kid)").build();

    /**
     * 无效的通知团队ID。
     */
    public static final Errors ILLEGAL_TEAM_ID = ErrorRegistry.builder().key("5013").value("无效的团队ID").build();

    /**
     * 无效的通知客户端ID。
     */
    public static final Errors ILLEGAL_CLIENT_ID = ErrorRegistry.builder().key("5014").value("无效的客户端ID").build();

    /**
     * 无效的通知客户端密钥。
     */
    public static final Errors ILLEGAL_CLIENT_SECRET = ErrorRegistry.builder().key("5015").value("无效的客户端密钥").build();

    /**
     * 非法通知微信代理ID。
     */
    public static final Errors ILLEGAL_WECHAT_AGENT_ID = ErrorRegistry.builder().key("5016").value("非法的微信代理ID").build();

    /**
     * 今日头条授权登录时的异常状态码
     */

    public class Toutiao {
        /**
         * 通知接口调用成功。
         */
        public static final Errors EC0 = ErrorRegistry.builder().key("0").value("接口调用成功").build();

        /**
         * API配置错误，未传入Client Key。
         */
        public static final Errors EC1 = ErrorRegistry.builder().key("1").value("API配置错误，未传入Client Key").build();

        /**
         * API配置错误，Client Key错误，请检查是否和开放平台的ClientKey一致。
         */
        public static final Errors EC2 = ErrorRegistry.builder().key("2")
                .value("API配置错误，Client Key错误，请检查是否和开放平台的ClientKey一致").build();

        /**
         * 通知缺少授权信息。
         */
        public static final Errors EC3 = ErrorRegistry.builder().key("3").value("没有授权信息").build();

        /**
         * 通知响应类型错误。
         */
        public static final Errors EC4 = ErrorRegistry.builder().key("4").value("响应类型错误").build();

        /**
         * 通知授权类型错误。
         */
        public static final Errors EC5 = ErrorRegistry.builder().key("5").value("授权类型错误").build();

        /**
         * 通知client_secret错误。
         */
        public static final Errors EC6 = ErrorRegistry.builder().key("6").value("client_secret错误").build();

        /**
         * 通知authorize_code过期。
         */
        public static final Errors EC7 = ErrorRegistry.builder().key("7").value("authorize_code过期").build();

        /**
         * 通知指定url的scheme不是https。
         */
        public static final Errors EC8 = ErrorRegistry.builder().key("8").value("指定url的scheme不是https").build();

        /**
         * 通知接口内部错误，请联系头条技术。
         */
        public static final Errors EC9 = ErrorRegistry.builder().key("9").value("接口内部错误，请联系头条技术").build();

        /**
         * 通知access_token过期。
         */
        public static final Errors EC10 = ErrorRegistry.builder().key("10").value("access_token过期").build();

        /**
         * 通知缺少access_token。
         */
        public static final Errors EC11 = ErrorRegistry.builder().key("11").value("缺少access_token").build();

        /**
         * 通知参数缺失。
         */
        public static final Errors EC12 = ErrorRegistry.builder().key("12").value("参数缺失").build();

        /**
         * 通知url错误。
         */
        public static final Errors EC13 = ErrorRegistry.builder().key("13").value("url错误").build();

        /**
         * 通知域名与登记域名不匹配。
         */
        public static final Errors EC21 = ErrorRegistry.builder().key("21").value("域名与登记域名不匹配").build();

        /**
         * 通知未知错误，请联系头条技术。
         */
        public static final Errors EC999 = ErrorRegistry.builder().key("999").value("未知错误，请联系头条技术").build();

        /**
         * 根据错误码获取对应的错误对象。
         *
         * @param errorCode 错误码
         * @return 对应的错误对象，若无匹配则返回EC999
         */
        public static Errors getErrorCode(String errorCode) {
            Errors[] errorCodes = new Errors[] { EC0, EC1, EC2, EC3, EC4, EC5, EC6, EC7, EC8, EC9, EC10, EC11, EC12,
                    EC13, EC21, EC999 };
            for (Errors code : errorCodes) {
                if (errorCode.equals(code.getKey())) {
                    return code;
                }
            }
            return EC999;
        }
    }

}
