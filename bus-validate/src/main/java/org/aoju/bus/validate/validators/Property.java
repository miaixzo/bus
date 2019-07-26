package org.aoju.bus.validate.validators;

import org.aoju.bus.core.lang.exception.ValidateException;
import org.aoju.bus.core.text.StrSubstitutor;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.MapUtils;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校验注解所包含的通用属性
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class Property {

    private boolean array = false;
    /**
     * 错误码
     */
    private String errcode;
    /**
     * 错误提示信息
     */
    private String errmsg;
    /**
     * 错误属性名称
     */
    private String field;

    private String name;
    /**
     * 校验组信息
     */
    private String[] group;
    /**
     * 当前注解
     */
    private Annotation annotation;
    /**
     * 校验类
     */
    private Class<?> clazz;
    /**
     * 异常信息
     */
    private Class<? extends ValidateException> exception;
    /**
     * 校验参数
     */
    private Map<String, Object> param;
    /**
     * 校验属性信息
     */
    private List<Property> list;

    public Property() {
        this.list = new ArrayList<>();
        this.param = new HashMap<>();
    }

    /**
     * 添加父级校验注解属性
     */
    public void addParentProperty(Property property) {
        if (CollUtils.isEmpty(this.list)) {
            this.list = new ArrayList<>();
        }
        this.list.add(property);
    }

    /**
     * 添加错误信息的字符串插值参数
     *
     * @param name  插值名称
     * @param value 插值
     */
    public void addParam(String name, Object value) {
        if (MapUtils.isEmpty(this.param)) {
            this.param = new HashMap<>();
        }
        if (this.param.containsKey(name)) {
            throw new IllegalArgumentException("当前异常信息格式化参数已经存在:" + name);
        }
        this.param.put(name, value);
    }

    /**
     * 获取字符串插值后的验证信息
     */
    public String getFormatted() {
        StrSubstitutor sub = new StrSubstitutor(param);
        return sub.replace(this.errmsg);
    }

}
