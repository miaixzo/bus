/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.builtin;

/**
 * 定义错误码的地方
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public interface Errors {

    String OPEN_ISV = "open.error_";

    ErrorMeta SUCCESS = new ErrorMeta(OPEN_ISV, "0", "success");
    ErrorMeta SYS_ERROR = new ErrorMeta(OPEN_ISV, "-9", "系统错误");

    /**
     * 调用不存在的服务请求:{0}
     */
    ErrorMeta NO_API = new ErrorMeta(OPEN_ISV, "1", "不存在的服务请求");
    /**
     * 服务请求({0})的参数非法
     */
    ErrorMeta ERROR_PARAM = new ErrorMeta(OPEN_ISV, "2", "参数非法");
    /**
     * 服务请求({0})缺少应用键参数:{1}
     */
    ErrorMeta NO_APP_ID = new ErrorMeta(OPEN_ISV, "3", "缺少应用键参数");
    /**
     * 服务请求({0})的应用键参数{1}无效
     */
    ErrorMeta ERROR_APP_ID = new ErrorMeta(OPEN_ISV, "4", "应用键参数无效");
    /**
     * 服务请求({0})需要签名,缺少签名参数:{1}
     */
    ErrorMeta NO_SIGN_PARAM = new ErrorMeta(OPEN_ISV, "5", "缺少签名参数");
    /**
     * 服务请求({0})的签名无效
     */
    ErrorMeta ERROR_SIGN = new ErrorMeta(OPEN_ISV, "6", "签名无效");
    /**
     * 请求超时
     */
    ErrorMeta TIMEOUT = new ErrorMeta(OPEN_ISV, "7", "请求超时");
    /**
     * 服务请求({0})业务逻辑出错
     */
    ErrorMeta ERROR_BUSI = new ErrorMeta(OPEN_ISV, "8", "业务逻辑出错");
    /**
     * 服务不可用
     */
    ErrorMeta SERVICE_INVALID = new ErrorMeta(OPEN_ISV, "9", "服务不可用");
    /**
     * 请求时间格式错误
     */
    ErrorMeta TIME_INVALID = new ErrorMeta(OPEN_ISV, "10", "请求时间格式错误");
    /**
     * 序列化格式不存在
     */
    ErrorMeta NO_FORMATTER = new ErrorMeta(OPEN_ISV, "11", "序列化格式不存在");
    /**
     * 不支持contectType
     */
    ErrorMeta NO_CONTECT_TYPE_SUPPORT = new ErrorMeta(OPEN_ISV, "12", "不支持contectType");
    /**
     * json格式错误
     */
    ErrorMeta ERROR_JSON_DATA = new ErrorMeta(OPEN_ISV, "13", "json格式错误");
    /**
     * accessToken错误
     */
    ErrorMeta ERROR_ACCESS_TOKEN = new ErrorMeta(OPEN_ISV, "14", "accessToken错误");
    /**
     * accessToken expired
     */
    ErrorMeta EXPIRED_ACCESS_TOKEN = new ErrorMeta(OPEN_ISV, "15", "accessToken expired");
    /**
     * accessToken not found
     */
    ErrorMeta UNSET_ACCESS_TOKEN = new ErrorMeta(OPEN_ISV, "16", "accessToken not found");
    /**
     * jwt操作失败
     */
    ErrorMeta ERROR_OPT_JWT = new ErrorMeta(OPEN_ISV, "17", "jwt操作失败");
    /**
     * jwt错误
     */
    ErrorMeta ERROR_JWT = new ErrorMeta(OPEN_ISV, "18", "jwt错误");
    /**
     * 加密算法不支持
     */
    ErrorMeta ERROR_ALGORITHM = new ErrorMeta(OPEN_ISV, "19", "加密算法不支持");
    /**
     * ssl交互错误
     */
    ErrorMeta ERROR_SSL = new ErrorMeta(OPEN_ISV, "20", "ssl交互错误");
    /**
     * jwt过期
     */
    ErrorMeta ERROR_JWT_EXPIRED = new ErrorMeta(OPEN_ISV, "21", "jwt过期");
    /**
     * 文件上传错误
     */
    ErrorMeta ERROR_UPLOAD_FILE = new ErrorMeta(OPEN_ISV, "22", "文件上传错误");
    /**
     * 无访问权限
     */
    ErrorMeta NO_PERMISSION = new ErrorMeta(OPEN_ISV, "23", "无访问权限");
    /**
     * 新ssl交互不支持
     */
    ErrorMeta NEW_SSL_NOT_SUPPORTED = new ErrorMeta(OPEN_ISV, "24", "新ssl交互不支持");

    /**
     * 业务参数错误
     */
    ErrorMeta BUSI_PARAM_ERROR = new ErrorMeta("100", "业务参数错误");

}
