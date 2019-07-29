package org.aoju.bus.core.loader;

/**
 * 对象加载抽象接口<br>
 * 通过实现此接口自定义实现对象的加载方式，例如懒加载机制、多线程加载等
 *
 * @param <T> 对象类型
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Loader<T> {

    /**
     * 获取一个准备好的对象<br>
     * 通过准备逻辑准备好被加载的对象，然后返回。在准备完毕之前此方法应该被阻塞
     *
     * @return 加载完毕的对象
     */
    T get();

}
