package org.aoju.bus.spring.validate;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.validate.Builder;
import org.aoju.bus.validate.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 自动进行参数处理实现类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class AutoValidateAdvice {

    /**
     * 自动进行参数处理
     *
     * @param proxyChain 切面
     * @return 返回执行结果
     */
    public Object access(AspectjProxyChain proxyChain) throws Throwable {
        Object[] agruements = proxyChain.getArgs();
        Method method = proxyChain.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = proxyChain.getTarget().getClass().getDeclaredMethod(method.getName(),
                        method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                Logger.info("无法在实现类中找到指定的方法，所以无法实现校验器验证，method：" + method.getName());
                return proxyChain.doProxyChain(agruements);
            }
        }
        Annotation[][] annotations = method.getParameterAnnotations();
        Object[] names = proxyChain.getNames();
        for (int i = 0; i < agruements.length; i++) {
            Builder.on(agruements[i], annotations[i], Context.newInstance(), StringUtils.toString(names[i]));
        }
        return proxyChain.doProxyChain(agruements);
    }

}
