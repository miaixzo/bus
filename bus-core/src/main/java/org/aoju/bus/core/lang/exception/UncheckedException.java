package org.aoju.bus.core.lang.exception;

import lombok.Data;

import java.lang.reflect.InvocationTargetException;

/**
 * 自定义异常: 未受检异常
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@Data
public class UncheckedException extends RuntimeException {

    /**
     * 错误码
     */
    protected String errcode;
    /**
     * 错误信息
     */
    protected String errmsg;

    public UncheckedException() {
        super();
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加自己的描述
     *
     * @param message 打印信息
     */
    public UncheckedException(String message) {
        super(message);
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加自己的描述
     *
     * @param cause 抛出对象
     */
    public UncheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加自己的描述
     *
     * @param message 打印信息
     * @param cause   抛出对象
     */
    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加自己的描述
     *
     * @param errcode 错误编码
     * @param errmsg  错误提示
     */
    public UncheckedException(String errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    /**
     * 根据格式化字符串，生成运行时异常
     *
     * @param format 格式
     * @param args   参数
     */
    public UncheckedException(String format, Object... args) {
        super(String.format(format, args));
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加自己的描述
     *
     * @param e    抛出对象
     * @param fmt  格式
     * @param args 参数
     */
    public UncheckedException(Throwable e, String fmt, Object... args) {
        super(String.format(fmt, args), e);
    }

    /**
     * 生成一个未实现的运行时异常
     *
     * @return 一个未实现的运行时异常
     */
    public static UncheckedException noImplement() {
        return new UncheckedException("Not implement yet!");
    }

    /**
     * 生成一个不可能的运行时异常
     *
     * @return 一个不可能的运行时异常
     */
    public static UncheckedException impossible() {
        return new UncheckedException("r u kidding me?! It is impossible!");
    }

    public static Throwable unwrapThrow(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (itE.getTargetException() != null)
                return unwrapThrow(itE.getTargetException());
        }
        if (e instanceof RuntimeException && e.getCause() != null)
            return unwrapThrow(e.getCause());
        return e;
    }

    public static boolean isCauseBy(Throwable e, Class<? extends Throwable> causeType) {
        if (e.getClass() == causeType)
            return true;
        Throwable cause = e.getCause();
        if (null == cause)
            return false;
        return isCauseBy(cause, causeType);
    }

}
