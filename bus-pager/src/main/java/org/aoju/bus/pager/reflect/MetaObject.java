package org.aoju.bus.pager.reflect;

import org.aoju.bus.pager.PageException;

import java.lang.reflect.Method;

/**
 * 反射工具
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MetaObject {

    public static Method method;

    static {
        try {
            // 高版本中的 MetaObject.forObject 有 4 个参数，低版本是 1 个
            // 下面这个 MetaObjectWithCache 带反射的缓存信息
            Class<?> metaClass = Class.forName("org.aoju.bus.pager.reflect.MetaObjectWithCache");
            method = metaClass.getDeclaredMethod("forObject", Object.class);
        } catch (Throwable e1) {
            try {
                Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.MetaObject");
                method = metaClass.getDeclaredMethod("forObject", Object.class);
            } catch (Exception e2) {
                throw new PageException(e2);
            }
        }

    }

    public static org.apache.ibatis.reflection.MetaObject forObject(Object object) {
        try {
            return (org.apache.ibatis.reflection.MetaObject) method.invoke(null, object);
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

}
