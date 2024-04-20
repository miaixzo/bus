package org.miaixz.bus.core.loader;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源类加载器，可以加载任意类型的资源类
 *
 * @param <T> {@link Resource}接口实现类
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClazzLoader<T extends Resource> extends SecureClassLoader {

    private final Map<String, T> resourceMap;
    /**
     * 缓存已经加载的类
     */
    private final Map<String, Class<?>> cacheClassMap;

    /**
     * 构造
     *
     * @param parentClassLoader 父类加载器，null表示默认当前上下文加载器
     * @param resourceMap       资源map
     */
    public ClazzLoader(ClassLoader parentClassLoader, Map<String, T> resourceMap) {
        super(ObjectKit.defaultIfNull(parentClassLoader, ClassKit::getClassLoader));
        this.resourceMap = ObjectKit.defaultIfNull(resourceMap, HashMap::new);
        this.cacheClassMap = new HashMap<>();
    }

    /**
     * 增加需要加载的类资源
     *
     * @param resource 资源，可以是文件、流或者字符串
     * @return this
     */
    public ClazzLoader<T> addResource(T resource) {
        this.resourceMap.put(resource.getName(), resource);
        return this;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        final Class<?> clazz = cacheClassMap.computeIfAbsent(name, this::defineByName);
        if (clazz == null) {
            return super.findClass(name);
        }
        return clazz;
    }

    /**
     * 从给定资源中读取class的二进制流，然后生成类
     * 如果这个类资源不存在，返回{@code null}
     *
     * @param name 类名
     * @return 定义的类
     */
    private Class<?> defineByName(String name) {
        final Resource resource = resourceMap.get(name);
        if (null != resource) {
            final byte[] bytes = resource.readBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }

}
