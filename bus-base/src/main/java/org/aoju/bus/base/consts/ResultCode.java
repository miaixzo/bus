package org.aoju.bus.base.consts;

import lombok.Getter;

/**
 * <p>@description : 系统响应吗枚举类 </p>
 * <p>@author : <a href="mailto:congchun.zheng@gmail.com">sixawn</a> </p>
 * <p>@since : 2019-06-27 </p>
 * <p>@version : 1.0.0 </p>
 */
public enum ResultCode {

    /*************** 系统层面状态码 ****************/
    EM_SUCCESS("0", "请求成功", ""),
    EM_FAILURE("-1", "系统繁忙,请稍后重试", ""),
    EM_LIMITER("-2", "请求过于频繁", ""),
    EM_100100("100100", "无效的令牌", ""),
    EM_100101("100101", "无效的参数", ""),
    EM_100102("100102", "无效的版本", ""),
    EM_100103("100103", "无效的方法", ""),
    EM_100104("100104", "无效的语言", ""),
    EM_100105("100105", "无效的格式化类型", ""),
    EM_100106("100106", "缺少token参数", ""),
    EM_100107("100107", "缺少version参数 ", ""),
    EM_100108("100108", "缺少method参数", ""),
    EM_100109("100109", "缺少language参数", ""),
    EM_100110("100110", "缺少fields参数", ""),
    EM_100111("100111", "缺少format参数", ""),
    EM_100112("100112", "code错误", ""),
    EM_100113("100113", "code过期", ""),
    EM_100114("100114", "缺少sign参数", ""),
    EM_100115("100115", "缺少noncestr参数", ""),
    EM_100116("100116", "缺少timestamp参数", ""),
    EM_100117("100117", "缺少sign", ""),
    EM_100118("100118", "token过期", ""),
    EM_100200("100200", "请使用GET请求", ""),
    EM_100201("100201", "请使用POST请求", ""),
    EM_100202("100202", "请使用PUT请求", ""),
    EM_100203("100203", "请使用DELETE请求", ""),
    EM_100204("100204", "请使用OPTIONS请求", ""),
    EM_100205("100205", "请使用HEAD请求", ""),
    EM_100206("100206", "请使用PATCH请求", ""),
    EM_100207("100207", "请使用TRACE请求", ""),
    EM_100208("100208", "请使用CONNECT请求", ""),
    EM_100209("100209", "请使用HTTPS协议", ""),
    EM_100300("100300", "暂无数据", ""),
    EM_100400("100400", "转换JSON/XML错误", ""),
    EM_100500("100500", "API未授权", ""),
    EM_100501("100501", "日期格式化错误", ""),
    EM_100502("100502", "账号已冻结", ""),
    EM_100503("100503", "账号已存在", ""),
    EM_100504("100504", "账号不存在", ""),
    EM_100505("100505", "密码错误", ""),
    EM_100506("100506", "通用函数,处理异常", ""),
    EM_100507("100507", "请求方法不支持", ""),
    EM_100508("100508", "不支持此类型", ""),
    EM_100509("100509", "未找到资源", ""),
    EM_100510("100510", "内部处理异常", ""),
    EM_100511("100511", "验证失败!", ""),
    EM_100512("100512", "数据已存在", ""),
    EM_100513("100513", "业务处理失败", ""),
    EM_100514("100514", "任务执行失败", ""),


    /**************** 转运业务状态码 2003xx ****************/
    EM_200300("200300", "转运业务流程异常[%s]", ""),
    EM_200302("200301", "转运业务调用外部系统 [%s] 异常.", ""),

    /**************** 会诊业务状态码 2004xx ****************/
    EM_200400("200400", "会诊业务流程异常[%s]", ""),
    EM_200402("200401", "会诊业务调用外部系统 [%s] 异常.", ""),

    /**************** 转诊业务状态码 2005xx ****************/
    EM_200500("200500", "转诊业务流程异常[%s]", ""),
    EM_200502("200501", "转诊业务调用外部系统 [%s] 异常.", "");

    @Getter
    public String errcode;
    @Getter
    public String errmsg;
    @Getter
    public String solution;

    ResultCode(String errcode, String errmsg, String solution) {
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.solution = solution;
    }

    public static ResultCode of(String errcode) {
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.errcode.equalsIgnoreCase(errcode)) {
                return resultCode;
            }
        }
        return EM_FAILURE;
    }

    public static String of(ResultCode errcode) {
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.equals(errcode)) {
                return resultCode.errcode;
            }
        }
        return "";
    }

}
