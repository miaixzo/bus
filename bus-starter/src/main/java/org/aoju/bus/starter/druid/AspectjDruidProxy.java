package org.aoju.bus.starter.druid;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * AOP切面切点
 *
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
@Order(-1)
@Aspect
public class AspectjDruidProxy {

    /**
     * 扫描所有含有@DataSource注解的类
     */
    @Pointcut("@annotation(org.aoju.bus.starter.druid.DataSource)" +
            "||execution(* *(@org.aoju.bus.starter.druid.DataSource (*), ..))")
    public void match() {

    }

    /**
     * 执行结果,使用around方式监控
     *
     * @param point 切点
     * @return 返回结果
     * @throws Throwable 异常
     */
    @Around("match()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取执行方法
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        // 获取方法的@DataSource注解
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (!StringUtils.hasLength(dataSource.value())) {
            // 获取类级别的@DataSource注解
            dataSource = method.getDeclaringClass().getAnnotation(DataSource.class);
        }
        if (null != dataSource) {
            // 设置数据源key值
            DataSourceHolder.setKey(dataSource.value());
            Logger.info("Switch datasource to [{}] in method [{}]",
                    DataSourceHolder.getKey(), point.getSignature());
        }
        // 继续执行该方法
        Object object = point.proceed();
        // 恢复默认数据源
        DataSourceHolder.remove();
        Logger.info("Restore datasource to [{}] in method [{}]",
                DataSourceHolder.getKey(), point.getSignature());
        return object;
    }

}
