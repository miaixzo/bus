package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.caller.Caller;
import org.aoju.bus.core.lang.caller.SecurityManagerCaller;
import org.aoju.bus.core.lang.caller.StackTraceCaller;

/**
 * 调用者。可以通过此类的方法获取调用者、多级调用者以及判断是否被调用
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CallerUtils {

    private static final Caller INSTANCE;

    static {
        INSTANCE = tryCreateCaller();
    }

    /**
     * 获得调用者
     *
     * @return 调用者
     */
    public static Class<?> getCaller() {
        return INSTANCE.getCaller();
    }

    /**
     * 获得调用者的调用者
     *
     * @return 调用者的调用者
     */
    public static Class<?> getCallers() {
        return INSTANCE.getCallers();
    }

    /**
     * 获得调用者，指定第几级调用者<br>
     * 调用者层级关系：
     *
     * <pre>
     * 0 {@link CallerUtils}
     * 1 调用{@link CallerUtils}中方法的类
     * 2 调用者的调用者
     * ...
     * </pre>
     *
     * @param depth 层级。0表示{@link CallerUtils}本身，1表示调用{@link CallerUtils}的类，2表示调用者的调用者，依次类推
     * @return 第几级调用者
     */
    public static Class<?> getCaller(int depth) {
        return INSTANCE.getCaller(depth);
    }

    /**
     * 是否被指定类调用
     *
     * @param clazz 调用者类
     * @return 是否被调用
     */
    public static boolean isCalledBy(Class<?> clazz) {
        return INSTANCE.isCalledBy(clazz);
    }

    /**
     * 尝试创建{@link Caller}实现
     *
     * @return {@link Caller}实现
     */
    private static Caller tryCreateCaller() {
        try {
            return new SecurityManagerCaller();
        } catch (Throwable e) {
        }
        return new StackTraceCaller();
    }

}
