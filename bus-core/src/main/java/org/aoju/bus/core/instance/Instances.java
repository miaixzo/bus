package org.aoju.bus.core.instance;


import org.aoju.bus.core.annotation.ThreadSafe;

/**
 * 实例化工具类
 * 对于 {@link InstanceFactory} 的便于使用
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
@ThreadSafe
public final class Instances {

    private Instances() {
    }

    /**
     * 静态方法单例
     *
     * @param clazz 类信息
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T singletion(Class<T> clazz) {
        return InstanceFactory.getInstance().singleton(clazz);
    }

    /**
     * 静态方法单例
     *
     * @param clazz     类信息
     * @param groupName 分组名称
     * @param <T>       泛型
     * @return 结果
     */
    public static <T> T singletion(Class<T> clazz, final String groupName) {
        return InstanceFactory.getInstance().singleton(clazz, groupName);
    }

    /**
     * threadLocal 同一个线程对应的实例一致
     *
     * @param clazz class
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T threadLocal(Class<T> clazz) {
        return InstanceFactory.getInstance().threadLocal(clazz);
    }

    /**
     * {@link ThreadSafe} 线程安全标示的使用单例，或者使用多例
     *
     * @param clazz class
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T threadSafe(Class<T> clazz) {
        return InstanceFactory.getInstance().threadSafe(clazz);
    }

    /**
     * 多例
     *
     * @param clazz class
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T multiple(Class<T> clazz) {
        return InstanceFactory.getInstance().multiple(clazz);
    }

}
