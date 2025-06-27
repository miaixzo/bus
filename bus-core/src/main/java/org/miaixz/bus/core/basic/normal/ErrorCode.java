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
package org.miaixz.bus.core.basic.normal;

import org.miaixz.bus.core.lang.Symbol;

/**
 * 错误码，定义全局通用的错误码，可被继承以扩展产品特定错误码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ErrorCode {

    /**
     * 请求成功
     */
    public static final Errors _SUCCESS = ErrorRegistry.builder().key(Symbol.ZERO).value("请求成功").build();

    /**
     * 系统繁忙，请稍后重试
     */
    public static final Errors _FAILURE = ErrorRegistry.builder().key("-1").value("系统繁忙,请稍后重试").build();

    /**
     * 请求过于频繁
     */
    public static final Errors _LIMITER = ErrorRegistry.builder().key("-2").value("请求过于频繁，请稍候后再试").build();

    /**
     * 无效的令牌
     */
    public static final Errors _100100 = ErrorRegistry.builder().key("100100").value("无效的令牌").build();

    /**
     * 无效的参数
     */
    public static final Errors _100101 = ErrorRegistry.builder().key("100101").value("无效的参数").build();

    /**
     * 无效的版本
     */
    public static final Errors _100102 = ErrorRegistry.builder().key("100102").value("无效的版本").build();

    /**
     * 无效的方法
     */
    public static final Errors _100103 = ErrorRegistry.builder().key("100103").value("无效的方法").build();

    /**
     * 无效的语言
     */
    public static final Errors _100104 = ErrorRegistry.builder().key("100104").value("无效的语言").build();

    /**
     * 无效的格式化类型
     */
    public static final Errors _100105 = ErrorRegistry.builder().key("100105").value("无效的格式化类型").build();

    /**
     * 缺少token参数
     */
    public static final Errors _100106 = ErrorRegistry.builder().key("100106").value("缺少token参数").build();

    /**
     * 缺少version参数
     */
    public static final Errors _100107 = ErrorRegistry.builder().key("100107").value("缺少version参数").build();

    /**
     * 缺少method参数
     */
    public static final Errors _100108 = ErrorRegistry.builder().key("100108").value("缺少method参数").build();

    /**
     * 缺少language参数
     */
    public static final Errors _100109 = ErrorRegistry.builder().key("100109").value("缺少language参数").build();

    /**
     * 缺少fields参数
     */
    public static final Errors _100110 = ErrorRegistry.builder().key("100110").value("缺少fields参数").build();

    /**
     * 缺少format参数
     */
    public static final Errors _100111 = ErrorRegistry.builder().key("100111").value("缺少format参数").build();

    /**
     * 缺少sign参数
     */
    public static final Errors _100112 = ErrorRegistry.builder().key("100112").value("缺少sign参数").build();

    /**
     * 缺少noncestr参数
     */
    public static final Errors _100113 = ErrorRegistry.builder().key("100113").value("缺少noncestr参数").build();

    /**
     * 缺少timestamp参数
     */
    public static final Errors _100114 = ErrorRegistry.builder().key("100114").value("缺少timestamp参数").build();

    /**
     * 缺少sign参数（重复，需检查）
     */
    public static final Errors _100115 = ErrorRegistry.builder().key("100115").value("缺少sign参数").build();

    /**
     * 当前令牌已过期
     */
    public static final Errors _100116 = ErrorRegistry.builder().key("100116").value("当前令牌已过期").build();

    /**
     * 当前账号已登录
     */
    public static final Errors _100117 = ErrorRegistry.builder().key("100117").value("当前账号已登录").build();

    /**
     * 无效的签名
     */
    public static final Errors _100118 = ErrorRegistry.builder().key("100118").value("无效的签名").build();

    /**
     * 请使用GET请求
     */
    public static final Errors _100200 = ErrorRegistry.builder().key("100200").value("请使用GET请求").build();

    /**
     * 请使用POST请求
     */
    public static final Errors _100201 = ErrorRegistry.builder().key("100201").value("请使用POST请求").build();

    /**
     * 请使用PUT请求
     */
    public static final Errors _100202 = ErrorRegistry.builder().key("100202").value("请使用PUT请求").build();

    /**
     * 请使用DELETE请求
     */
    public static final Errors _100203 = ErrorRegistry.builder().key("100203").value("请使用DELETE请求").build();

    /**
     * 请使用OPTIONS请求
     */
    public static final Errors _100204 = ErrorRegistry.builder().key("100204").value("请使用OPTIONS请求").build();

    /**
     * 请使用HEAD请求
     */
    public static final Errors _100205 = ErrorRegistry.builder().key("100205").value("请使用HEAD请求").build();

    /**
     * 请使用PATCH请求
     */
    public static final Errors _100206 = ErrorRegistry.builder().key("100206").value("请使用PATCH请求").build();

    /**
     * 请使用TRACE请求
     */
    public static final Errors _100207 = ErrorRegistry.builder().key("100207").value("请使用TRACE请求").build();

    /**
     * 请使用CONNECT请求
     */
    public static final Errors _100208 = ErrorRegistry.builder().key("100208").value("请使用CONNECT请求").build();

    /**
     * 请使用HTTPS协议
     */
    public static final Errors _100209 = ErrorRegistry.builder().key("100209").value("请使用HTTPS协议").build();

    /**
     * 暂无数据
     */
    public static final Errors _100300 = ErrorRegistry.builder().key("100300").value("暂无数据").build();

    /**
     * 转换JSON/XML错误
     */
    public static final Errors _100400 = ErrorRegistry.builder().key("100400").value("转换JSON/XML错误").build();

    /**
     * API未授权
     */
    public static final Errors _100500 = ErrorRegistry.builder().key("100500").value("API未授权").build();

    /**
     * 日期格式化错误
     */
    public static final Errors _100501 = ErrorRegistry.builder().key("100501").value("日期格式化错误").build();

    /**
     * 账号已冻结
     */
    public static final Errors _100502 = ErrorRegistry.builder().key("100502").value("账号已冻结").build();

    /**
     * 账号已存在
     */
    public static final Errors _100503 = ErrorRegistry.builder().key("100503").value("账号已存在").build();

    /**
     * 账号不存在
     */
    public static final Errors _100504 = ErrorRegistry.builder().key("100504").value("账号不存在").build();

    /**
     * 密码错误
     */
    public static final Errors _100505 = ErrorRegistry.builder().key("100505").value("密码错误").build();

    /**
     * 通用函数，处理异常
     */
    public static final Errors _100506 = ErrorRegistry.builder().key("100506").value("通用函数,处理异常").build();

    /**
     * 请求方法不支持
     */
    public static final Errors _100507 = ErrorRegistry.builder().key("100507").value("请求方法不支持").build();

    /**
     * 不支持此类型
     */
    public static final Errors _100508 = ErrorRegistry.builder().key("100508").value("不支持此类型").build();

    /**
     * 未找到资源
     */
    public static final Errors _100509 = ErrorRegistry.builder().key("100509").value("未找到资源").build();

    /**
     * 内部处理异常
     */
    public static final Errors _100510 = ErrorRegistry.builder().key("100510").value("内部处理异常").build();

    /**
     * 验证失败
     */
    public static final Errors _100511 = ErrorRegistry.builder().key("100511").value("验证失败!").build();

    /**
     * 数据已存在
     */
    public static final Errors _100512 = ErrorRegistry.builder().key("100512").value("数据已存在").build();

    /**
     * 业务处理失败
     */
    public static final Errors _100513 = ErrorRegistry.builder().key("100513").value("业务处理失败").build();

    /**
     * 不支持的操作
     */
    public static final Errors _100514 = ErrorRegistry.builder().key("100514").value("不支持的操作").build();

    /**
     * 数据不存在
     */
    public static final Errors _100515 = ErrorRegistry.builder().key("100515").value("数据不存在").build();

    /**
     * 链接已过期
     */
    public static final Errors _100516 = ErrorRegistry.builder().key("100516").value("链接已过期").build();

    /**
     * 文件格式错误
     */
    public static final Errors _100517 = ErrorRegistry.builder().key("100517").value("文件格式错误").build();

    /**
     * 未绑定帐号
     */
    public static final Errors _100518 = ErrorRegistry.builder().key("100518").value("未绑定帐号").build();

    /**
     * 名称重复
     */
    public static final Errors _100519 = ErrorRegistry.builder().key("100519").value("名称重复").build();

    /**
     * 帐号不存在
     */
    public static final Errors _100520 = ErrorRegistry.builder().key("100520").value("帐号不存在").build();

    /**
     * 账号异常请联系管理员
     */
    public static final Errors _100521 = ErrorRegistry.builder().key("100521").value("账号异常请联系管理员").build();

    /**
     * 帐号已锁定,请稍后再试
     */
    public static final Errors _100522 = ErrorRegistry.builder().key("100522").value("帐号已锁定,请稍后再试").build();

    /**
     * 用户名或密码错误
     */
    public static final Errors _100523 = ErrorRegistry.builder().key("100523").value("用户名或密码错误").build();

    /**
     * 发送验证码失败
     */
    public static final Errors _100524 = ErrorRegistry.builder().key("100524").value("发送验证码失败").build();

    /**
     * 验证码错误
     */
    public static final Errors _100525 = ErrorRegistry.builder().key("100525").value("验证码错误").build();

    /**
     * 无权操作
     */
    public static final Errors _100526 = ErrorRegistry.builder().key("100526").value("无权操作").build();

    /**
     * 密码长度不符合
     */
    public static final Errors _100527 = ErrorRegistry.builder().key("100527").value("密码长度不符合").build();

    /**
     * 密码需要包含大小写
     */
    public static final Errors _100528 = ErrorRegistry.builder().key("100528").value("密码需要包含大小写").build();

    /**
     * 密码需要包含特殊字符
     */
    public static final Errors _100529 = ErrorRegistry.builder().key("100529").value("密码需要包含特殊字符").build();

    /**
     * 手机号重复
     */
    public static final Errors _100530 = ErrorRegistry.builder().key("100530").value("手机号重复").build();

    /**
     * 租户名称重复
     */
    public static final Errors _100531 = ErrorRegistry.builder().key("100531").value("租户名称重复").build();

    /**
     * 无效的凭证
     */
    public static final Errors _100532 = ErrorRegistry.builder().key("100532").value("无效的凭证").build();

    /**
     * 部门已存在
     */
    public static final Errors _100533 = ErrorRegistry.builder().key("100533").value("部门已存在").build();

    /**
     * 工号已存在
     */
    public static final Errors _100534 = ErrorRegistry.builder().key("100534").value("工号已存在").build();

    /**
     * 错误的免登授权码
     */
    public static final Errors _100535 = ErrorRegistry.builder().key("100535").value("错误的免登授权码").build();

    /**
     * 未绑定手机号码
     */
    public static final Errors _100536 = ErrorRegistry.builder().key("100536").value("未绑定手机号码").build();

    /**
     * id不能为空
     */
    public static final Errors _100537 = ErrorRegistry.builder().key("100537").value("id不能为空").build();

    /**
     * 任务执行失败
     */
    public static final Errors _100600 = ErrorRegistry.builder().key("100600").value("任务执行失败").build();

}