/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.base.consts;

import lombok.Getter;

/**
 * 系统响应码
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum ErrorCode {

    EM_SUCCESS(Consts.EM_SUCCESS, "请求成功", ""),
    EM_FAILURE(Consts.EM_FAILURE, "系统繁忙,请稍后重试", ""),
    EM_LIMITER(Consts.EM_LIMITER, "请求过于频繁", ""),
    EM_100100(Consts.EM_100100, "无效的令牌", ""),
    EM_100101(Consts.EM_100101, "无效的参数", ""),
    EM_100102(Consts.EM_100102, "无效的版本", ""),
    EM_100103(Consts.EM_100103, "无效的方法", ""),
    EM_100104(Consts.EM_100104, "无效的语言", ""),
    EM_100105(Consts.EM_100105, "无效的格式化类型", ""),
    EM_100106(Consts.EM_100106, "缺少token参数", ""),
    EM_100107(Consts.EM_100107, "缺少version参数 ", ""),
    EM_100108(Consts.EM_100108, "缺少method参数", ""),
    EM_100109(Consts.EM_100109, "缺少language参数", ""),
    EM_100110(Consts.EM_100110, "缺少fields参数", ""),
    EM_100111(Consts.EM_100111, "缺少format参数", ""),
    EM_100112(Consts.EM_100112, "code错误", ""),
    EM_100113(Consts.EM_100113, "code过期", ""),
    EM_100114(Consts.EM_100114, "缺少sign参数", ""),
    EM_100115(Consts.EM_100115, "缺少noncestr参数", ""),
    EM_100116(Consts.EM_100116, "缺少timestamp参数", ""),
    EM_100117(Consts.EM_100117, "缺少sign", ""),
    EM_100118(Consts.EM_100118, "token过期", ""),
    EM_100200(Consts.EM_100200, "请使用GET请求", ""),
    EM_100201(Consts.EM_100201, "请使用POST请求", ""),
    EM_100202(Consts.EM_100202, "请使用PUT请求", ""),
    EM_100203(Consts.EM_100203, "请使用DELETE请求", ""),
    EM_100204(Consts.EM_100204, "请使用OPTIONS请求", ""),
    EM_100205(Consts.EM_100205, "请使用HEAD请求", ""),
    EM_100206(Consts.EM_100206, "请使用PATCH请求", ""),
    EM_100207(Consts.EM_100207, "请使用TRACE请求", ""),
    EM_100208(Consts.EM_100208, "请使用CONNECT请求", ""),
    EM_100209(Consts.EM_100209, "请使用HTTPS协议", ""),
    EM_100300(Consts.EM_100300, "暂无数据", ""),
    EM_100400(Consts.EM_100400, "转换JSON/XML错误", ""),
    EM_100500(Consts.EM_100500, "API未授权", ""),
    EM_100501(Consts.EM_100501, "日期格式化错误", ""),
    EM_100502(Consts.EM_100502, "账号已冻结", ""),
    EM_100503(Consts.EM_100503, "账号已存在", ""),
    EM_100504(Consts.EM_100504, "账号不存在", ""),
    EM_100505(Consts.EM_100505, "密码错误", ""),
    EM_100506(Consts.EM_100506, "通用函数,处理异常", ""),
    EM_100507(Consts.EM_100507, "请求方法不支持", ""),
    EM_100508(Consts.EM_100508, "不支持此类型", ""),
    EM_100509(Consts.EM_100509, "未找到资源", ""),
    EM_100510(Consts.EM_100510, "内部处理异常", ""),
    EM_100511(Consts.EM_100511, "验证失败!", ""),
    EM_100512(Consts.EM_100512, "数据已存在", ""),
    EM_100513(Consts.EM_100513, "业务处理失败", ""),
    EM_100514(Consts.EM_100514, "任务执行失败", "");


    @Getter
    public String errcode;
    @Getter
    public String errmsg;
    @Getter
    public String solution;

    ErrorCode(String errcode, String errmsg, String solution) {
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.solution = solution;
    }

    public static ErrorCode of(String errcode) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.errcode.equalsIgnoreCase(errcode)) {
                return errorCode;
            }
        }
        return EM_FAILURE;
    }

    public static String of(ErrorCode errcode) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.equals(errcode)) {
                return errorCode.errcode;
            }
        }
        return "";
    }

}
