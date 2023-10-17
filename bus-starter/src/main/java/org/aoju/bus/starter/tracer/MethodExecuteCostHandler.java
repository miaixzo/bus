/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.tracer;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 方法执行耗时统计
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@Aspect
public class MethodExecuteCostHandler {
 /*
    @Around("@annotation(org.aoju.bus.tracer.annotation.TacerCost)")
    public Object costLogger(ProceedingJoinPoint pjp) throws Throwable {
        Signature sig = pjp.getSignature();
        MethodSignature msig;
        Object object = null;

        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("this annotation only used on the method");
        }

        msig = (MethodSignature) sig;
        Object target = pjp.getTarget();
        TacerCost tacerCostLogger;
        try {
            //获取当前方法
            Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
            //获取costLogger注解
            tacerCostLogger = currentMethod.getAnnotation(TacerCost.class);
            //当有costLogger注解时
            if (null != tacerCostLogger) {
                //开始时间
                long startTime = System.currentTimeMillis();
                try {
                    //执行方法
                    object = pjp.proceed();
                } catch (Throwable throwable) {
                    throw throwable;
                }
                //耗时
                long cost = System.currentTimeMillis() - startTime;

                //当设置了超时时间,但耗时小于超时时间,不进行日志打印,直接返回 (只有耗时大于超时时间才进行日志打印)
                if (tacerCostLogger.timeout() != -1 && cost < tacerCostLogger.timeout()) {
                    return object;
                }

                //方法名
                String methodName = null;
                if (StringKit.isEmpty(tacerCostLogger.methodName())) {
                    //当methodName为默认值时,用signature(原方法名)代替
                    methodName = pjp.getSignature().toString();
                } else if (null != tacerCostLogger.methodName()) {
                    //有值则用值
                    methodName = tacerCostLogger.methodName();
                }

                //备注
                String remark = null;
                if (StringKit.isEmpty(tacerCostLogger.remark())) {
                    remark = "";
                } else if (null != tacerCostLogger.remark()) {
                    //有值则用值
                    remark = " [" + tacerCostLogger.remark() + "]";
                }

                //打印内容
                String printContent = null;
                switch (tacerCostLogger.language()) {
                    case CN:
                        printContent=methodName+ " 耗时 " + cost + " 毫秒 "+remark;
                        break;
                    case EN:
                        printContent=methodName+ " cost " + cost + " ms "+remark;
                        break;
                }

                //打印级别
                switch (tacerCostLogger.level()) {
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
                    object = pjp.proceed();
                } catch (Throwable throwable) {
                    throw throwable;
                }
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return object;
    }
*/
}
