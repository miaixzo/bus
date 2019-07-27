package org.aoju.bus.cron.task;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ReflectUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * 反射执行任务<br>
 * 通过传入类名#方法名，通过反射执行相应的方法<br>
 * 如果是静态方法直接执行，如果是对象方法，需要类有默认的构造方法。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InvokeTask implements Task {

    private Class<?> clazz;
    private Object obj;
    private Method method;

    /**
     * 构造
     *
     * @param classNameWithMethodName 类名与方法名的字符串表示，方法名和类名使用#隔开或者.隔开
     */
    public InvokeTask(String classNameWithMethodName) {
        int splitIndex = classNameWithMethodName.lastIndexOf('#');
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            throw new CommonException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        //类
        final String className = classNameWithMethodName.substring(0, splitIndex);
        if (StringUtils.isBlank(className)) {
            throw new IllegalArgumentException("Class name is blank !");
        }
        this.clazz = ClassUtils.loadClass(className);
        if (null == this.clazz) {
            throw new IllegalArgumentException("Load class with name of [" + className + "] fail !");
        }
        this.obj = ReflectUtils.newInstanceIfPossible(this.clazz);

        //方法
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);
        if (StringUtils.isBlank(methodName)) {
            throw new IllegalArgumentException("Method name is blank !");
        }
        this.method = ClassUtils.getPublicMethod(this.clazz, methodName);
        if (null == this.method) {
            throw new IllegalArgumentException("No method with name of [" + methodName + "] !");
        }
    }

    @Override
    public void execute() {
        try {
            ReflectUtils.invoke(this.obj, this.method, new Object[]{});
        } catch (CommonException e) {
            throw new CommonException(e.getCause());
        }
    }

}
