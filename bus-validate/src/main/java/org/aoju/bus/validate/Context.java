package org.aoju.bus.validate;

import org.aoju.bus.core.lang.exception.ValidateException;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.validate.validators.Checker;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 当前校验的上下文信息
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class Context {

    /**
     * 全局定义的错误码, 如果校验注解使用了-1 作为错误码，默认替换为该全局错误码
     */
    private String errcode = Builder.DEFAULT_ERRCODE;

    /**
     * 当前被激活的校验组
     */
    private List<String> group = new ArrayList<>();

    /**
     * 全局校验异常。
     * <p>
     * 当校验失败时，如果定义了全局校验异常，则抛出全局校验异常；
     * <p>
     * 然后判断如果定义了字段异常，则抛出字段异常；
     * <p>
     * 最后判断如果定义了校验器注解异常，则抛出校验器注解上定义的异常;
     * <p>
     * 如果都没定义，则抛出{@link ValidateException}
     */
    private Class<? extends ValidateException> exception;

    /**
     * 当前被激活的属性
     */
    private String[] field;

    /**
     * 当前需跳过的属性
     */
    private String[] skip;

    /**
     * 校验检查器
     */
    private Checker checker;

    /**
     * 快速失败, 默认：true
     * <p>
     * true: 表示如果参数一旦校验，立刻抛出校验失败异常
     * </P>
     * <p>
     * false: 即使存在参数校验失败，也必须等到该参数所有的校验器执行后，才会抛出异常
     * </P>
     */
    private boolean fast = true;

    /**
     * 是否校验对象内部, 默认：false
     */
    private boolean inside = false;

    public Context() {
    }

    /**
     * 提供一个包含默认校验器注册中心的校验器上下文
     *
     * @return 校验器上下文对象
     */
    public static Context newInstance() {
        Context context = new Context();
        context.setChecker(new Checker());
        return context;
    }

    /**
     * 添加校验组
     *
     * @param groups 校验组
     */
    public void addGroups(String... groups) {
        if (StringUtils.isEmpty(groups) || groups.length == 0) {
            return;
        }
        if (CollUtils.isEmpty(this.group)) {
            this.group = new ArrayList<>();
        }
        this.group.addAll(Arrays.asList(groups));
    }

}
