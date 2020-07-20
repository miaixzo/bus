/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.base.consts;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统响应码
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
public class ErrorCode {

    /**
     * 全局错误码
     */
    public static String EM_SUCCESS = Symbol.ZERO;
    public static String EM_FAILURE = "-1";
    public static String EM_LIMITER = "-2";
    public static String EM_100100 = "100100";
    public static String EM_100101 = "100101";
    public static String EM_100102 = "100102";
    public static String EM_100103 = "100103";
    public static String EM_100104 = "100104";
    public static String EM_100105 = "100105";
    public static String EM_100106 = "100106";
    public static String EM_100107 = "100107";
    public static String EM_100108 = "100108";
    public static String EM_100109 = "100109";
    public static String EM_100110 = "100110";
    public static String EM_100111 = "100111";
    public static String EM_100112 = "100112";
    public static String EM_100113 = "100113";
    public static String EM_100114 = "100114";
    public static String EM_100115 = "100115";
    public static String EM_100116 = "100116";
    public static String EM_100117 = "100117";
    public static String EM_100118 = "100118";
    public static String EM_100200 = "100200";
    public static String EM_100201 = "100201";
    public static String EM_100202 = "100202";
    public static String EM_100203 = "100203";
    public static String EM_100204 = "100204";
    public static String EM_100205 = "100205";
    public static String EM_100206 = "100206";
    public static String EM_100207 = "100207";
    public static String EM_100208 = "100208";
    public static String EM_100209 = "100209";
    public static String EM_100300 = "100300";
    public static String EM_100400 = "100400";
    public static String EM_100500 = "100500";
    public static String EM_100501 = "100501";
    public static String EM_100502 = "100502";
    public static String EM_100503 = "100503";
    public static String EM_100504 = "100504";
    public static String EM_100505 = "100505";
    public static String EM_100506 = "100506";
    public static String EM_100507 = "100507";
    public static String EM_100508 = "100508";
    public static String EM_100509 = "100509";
    public static String EM_100510 = "100510";
    public static String EM_100511 = "100511";
    public static String EM_100512 = "100512";
    public static String EM_100513 = "100513";
    public static String EM_100514 = "100514";

    /**
     * 错误码缓存
     */
    private static Map<String, String> ERRORCODE_CACHE = new ConcurrentHashMap<>();

    static {
        register(EM_SUCCESS, "请求成功");
        register(EM_FAILURE, "系统繁忙,请稍后重试");
        register(EM_LIMITER, "请求过于频繁");
        register(EM_100100, "无效的令牌");
        register(EM_100101, "无效的参数");
        register(EM_100102, "无效的版本");
        register(EM_100103, "无效的方法");
        register(EM_100104, "无效的语言");
        register(EM_100105, "无效的格式化类型");
        register(EM_100106, "缺少token参数");
        register(EM_100107, "缺少version参数 ");
        register(EM_100108, "缺少method参数");
        register(EM_100109, "缺少language参数");
        register(EM_100110, "缺少fields参数");
        register(EM_100111, "缺少format参数");
        register(EM_100112, "code错误");
        register(EM_100113, "code过期");
        register(EM_100114, "缺少sign参数");
        register(EM_100115, "缺少noncestr参数");
        register(EM_100116, "缺少timestamp参数");
        register(EM_100117, "缺少sign");
        register(EM_100118, "token过期");
        register(EM_100200, "请使用GET请求");
        register(EM_100201, "请使用POST请求");
        register(EM_100202, "请使用PUT请求");
        register(EM_100203, "请使用DELETE请求");
        register(EM_100204, "请使用OPTIONS请求");
        register(EM_100205, "请使用HEAD请求");
        register(EM_100206, "请使用PATCH请求");
        register(EM_100207, "请使用TRACE请求");
        register(EM_100208, "请使用CONNECT请求");
        register(EM_100209, "请使用HTTPS协议");
        register(EM_100300, "暂无数据");
        register(EM_100400, "转换JSON/XML错误");
        register(EM_100500, "API未授权");
        register(EM_100501, "日期格式化错误");
        register(EM_100502, "账号已冻结");
        register(EM_100503, "账号已存在");
        register(EM_100504, "账号不存在");
        register(EM_100505, "密码错误");
        register(EM_100506, "通用函数,处理异常");
        register(EM_100507, "请求方法不支持");
        register(EM_100508, "不支持此类型");
        register(EM_100509, "未找到资源");
        register(EM_100510, "内部处理异常");
        register(EM_100511, "验证失败!");
        register(EM_100512, "数据已存在");
        register(EM_100513, "业务处理失败");
        register(EM_100514, "任务执行失败");
    }

    /**
     * 注册组件
     *
     * @param key   错误码
     * @param value 错误信息
     */
    public static void register(String key, String value) {
        if (ERRORCODE_CACHE.containsKey(key)) {
            throw new InstrumentException("重复注册同名称的错误码：" + key);
        }
        ERRORCODE_CACHE.putIfAbsent(key, value);
    }

    /**
     * 是否包含指定名称的错误码
     *
     * @param name 错误码名称
     * @return true：包含, false：不包含
     */
    public static boolean contains(String name) {
        return ERRORCODE_CACHE.containsKey(name);
    }

    /**
     * 根据错误码名称获取错误码
     *
     * @param name 错误码名称
     * @return 错误码对象, 找不到时返回null
     */
    public static String require(String name) {
        return ERRORCODE_CACHE.get(name);
    }

}
