package org.aoju.bus.core.lang.caller;

import org.aoju.bus.core.utils.CallerUtils;

/**
 * 调用者接口<br>
 * 可以通过此接口的实现类方法获取调用者、多级调用者以及判断是否被调用
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Caller {

    /**
     * 获得调用者
     *
     * @return 调用者
     */
    Class<?> getCaller();

    /**
     * 获得调用者的调用者
     *
     * @return 调用者的调用者
     */
    Class<?> getCallers();

    /**
     * 获得调用者，指定第几级调用者 调用者层级关系：
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
    Class<?> getCaller(int depth);

    /**
     * 是否被指定类调用
     *
     * @param clazz 调用者类
     * @return 是否被调用
     */
    boolean isCalledBy(Class<?> clazz);

}
