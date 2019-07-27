package org.aoju.bus.mapper.reflection;

import org.aoju.bus.mapper.MapperException;
import org.apache.ibatis.mapping.MappedStatement;


/**
 * 反射工具
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Reflector {

    /**
     * 根据msId获取接口类
     *
     * @param msId
     * @return
     */
    public static Class<?> getMapperClass(String msId) {
        if (msId.indexOf(".") == -1) {
            throw new MapperException("当前MappedStatement的id=" + msId + ",不符合MappedStatement的规则!");
        }
        String mapperClassStr = msId.substring(0, msId.lastIndexOf("."));
        ClassLoader[] classLoader = getClassLoaders();
        Class<?> mapperClass = null;
        for (ClassLoader cl : classLoader) {
            if (null != cl) {
                try {
                    mapperClass = Class.forName(mapperClassStr, true, cl);
                    if (mapperClass != null) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    // we'll ignore this until all class loaders fail to locate the class
                }
            }
        }
        if (mapperClass == null) {
            throw new MapperException("class loaders failed to locate the class " + mapperClassStr);
        }
        return mapperClass;
    }

    private static ClassLoader[] getClassLoaders() {
        return new ClassLoader[]{Thread.currentThread().getContextClassLoader(), Reflector.class.getClassLoader()};
    }

    /**
     * 获取执行的方法名
     *
     * @param ms
     * @return
     */
    public static String getMethodName(MappedStatement ms) {
        return getMethodName(ms.getId());
    }

    /**
     * 获取执行的方法名
     *
     * @param msId
     * @return
     */
    public static String getMethodName(String msId) {
        return msId.substring(msId.lastIndexOf(".") + 1);
    }

}
