package org.aoju.bus.spring.tracer;

import org.aoju.bus.tracer.annotation.Cost;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Component
@Aspect
public class TracerCost {

    private static final Logger logger = LoggerFactory.getLogger(TracerCost.class);

    @Around("@annotation(org.aoju.bus.tracer.annotation.Cost)")
    public Object costLogger(ProceedingJoinPoint pjp) throws Throwable {
        Signature sig = pjp.getSignature();
        MethodSignature msig = null;
        Object obj = null;

        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("this annotation only used on the method");
        }

        msig = (MethodSignature) sig;
        Object target = pjp.getTarget();
        Cost costLogger = null;
        try {
            //获取当前方法
            Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
            //获取costLogger注解
            costLogger = currentMethod.getAnnotation(Cost.class);
            //当有costLogger注解时
            if (costLogger != null) {
                //开始时间
                long startTime = System.currentTimeMillis();
                try {
                    //执行方法
                    obj = pjp.proceed();
                } catch (Throwable throwable) {
                    throw throwable;
                }
                //耗时
                long cost = System.currentTimeMillis() - startTime;

                //当设置了超时时间，但耗时小于超时时间，不进行日志打印，直接返回 （只有耗时大于超时时间才进行日志打印）
                if (costLogger.timeout() != -1 && cost < costLogger.timeout()) {
                    return obj;
                }

                //方法名
                String methodName = null;
                if (StringUtils.isEmpty(costLogger.methodName())) {
                    //当methodName为默认值时，用signature(原方法名)代替
                    methodName = pjp.getSignature().toString();
                } else if (costLogger.methodName() != null) {
                    //有值则用值
                    methodName = costLogger.methodName();
                }

                //备注
                String remark = null;
                if (StringUtils.isEmpty(costLogger.remark())) {
                    remark = "";
                } else if (costLogger.remark() != null) {
                    //有值则用值
                    remark = " [" + costLogger.remark() + "]";
                }

                //打印内容
                String printContent = null;
                switch (costLogger.LANGUAGE()){
                    case CN:
                        printContent=methodName+ " 耗时 " + cost + " 毫秒 "+remark;
                        break;
                    case EN:
                        printContent=methodName+ " cost " + cost + " ms "+remark;
                        break;
                }

                //打印级别
                switch (costLogger.LEVEL()){
                    case TRACE:
                        logger.trace(printContent);
                        break;
                    case DEBUG:
                        logger.debug(printContent);
                        break;
                    case INFO:
                        logger.info(printContent);
                        break;
                    case WARN:
                        logger.warn(printContent);
                        break;
                    case ERROR:
                        logger.error(printContent);
                        break;
                }


            } else {
                try {
                    obj = pjp.proceed();
                } catch (Throwable throwable) {
                    throw throwable;
                }
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return obj;
    }

}
